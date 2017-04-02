package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.commons.properties.AnnotateProperties;
import fr.inria.anhalytics.annotate.exceptions.UnreachableAnnotateServiceException;
import fr.inria.anhalytics.annotate.exceptions.AnnotatorNotAvailableException;
import fr.inria.anhalytics.annotate.services.AnnotateService;
import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.data.BinaryFile;
import fr.inria.anhalytics.commons.managers.MongoFileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Handles threads used to annotate the tei collection from MongoDB.
 *
 * @author Patrice, Achraf
 */
public class Annotator {

    private static final Logger logger = LoggerFactory.getLogger(Annotator.class);

    private MongoFileManager mm;

    private boolean isAnnotated(BiblioObject biblioObject, Annotator_Type annotator_type) {
        if (annotator_type == Annotator_Type.NERD) {
            return biblioObject.getIsProcessedByNerd();
        } else if (annotator_type == Annotator_Type.KEYTERM) {
            return biblioObject.getIsProcessedByKeyterm();
        } else if (annotator_type == Annotator_Type.QUANTITIES) {
            return biblioObject.getIsProcessedByTextQuantities();
        } else if (annotator_type == Annotator_Type.PDFQUANTITIES) {
            return biblioObject.getIsProcessedByPDFQuantities();
        } else {
            throw new AnnotatorNotAvailableException("type of annotations not available: " + annotator_type);
        }
    }

    public enum Annotator_Type {

        NERD("NERD"),
        KEYTERM("KEYTERM"),
        QUANTITIES("QUANTITIES"),
        PDFQUANTITIES("PDFQUANTITIES");

        private String name;

        private Annotator_Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static boolean contains(String test) {
            for (Annotator_Type c : Annotator_Type.values()) {
                if (c.name().equals(test)) {
                    return true;
                }
            }
            return false;
        }
    }

    public Annotator() {
        this.mm = MongoFileManager.getInstance(false);
    }

    public void annotate(Annotator_Type annotator_type) {
        try {
            /*if (annotator_type == Annotator_Type.PDFQUANTITIES) {
                annotatePDFQuantitiesCollection();
            } else */
            if (AnnotateProperties.isIsMultiThread()) {
                /*if (annotator_type == Annotator_Type.QUANTITIES) {
                    annotateQuantitiesTeiCollectionMultiThreaded();
                } else */
                {
                    annotateTeiCollectionMultiThreaded(annotator_type);
                }
            } else {
                annotateTeiCollection(annotator_type);
            }
        } catch (UnreachableAnnotateServiceException | AnnotatorNotAvailableException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /*private void annotatePDFQuantitiesCollection()
            throws UnreachableAnnotateServiceException, AnnotatorNotAvailableException {
        try {
            if (AnnotateService.isAnnotateServiceReady(Annotator_Type.PDFQUANTITIES)) {
                if (mm.initIstexPdfs()) {

                    logger.info("processing...");
                    while (mm.hasMore()) {
                        IstexFile istexFile = mm.nextIstexBinaryDocument();
                        Runnable worker = new PDFQuantitiesAnnotatorWorker(mm, istexFile, null);
                        worker.run();
                    }

                }

            }
        } finally {
            mm.close();
        }
    }*/
    /**
     * Annotates quantities tei collection entries with text (multithread
     * process).
     */
    /*private void annotateQuantitiesTeiCollectionMultiThreaded() {
        try {
            if (AnnotateService.isAnnotateServiceReady(Annotator_Type.QUANTITIES)) {

            }
            if (mm.initIstexTeis()) {

                logger.info("processing...");
                while (mm.hasMore()) {
                    TEIFile istexFile = mm.nextIstexTeiDocument();
                    Runnable worker = new QuantitiesAnnotatorWorker(mm, istexFile, null);
                    worker.run();
                }

            }

        } finally {
            mm.close();
        }
    }*/
    /**
     * Annotates tei collection entries with fulltext.
     */
    private void annotateTeiCollection(Annotator_Type annotator_type)
            throws UnreachableAnnotateServiceException, AnnotatorNotAvailableException {
        int nb = 0;
        String annotationsCollection = null, teiCollection = null;
        boolean loadPDF = false;
        // Note: why not MongoCollectionsInterface.METADATA_WITHFULLTEXT_TEIS all the time?
        if (!Annotator_Type.contains(annotator_type.getName())) {
            throw new AnnotatorNotAvailableException("type of annotations not available: " + annotator_type);
        }
        try {
            if (AnnotateService.isAnnotateServiceReady(annotator_type)) {
                if (mm.initObjects()) {
                    //logger.info("processing teis for :" + date);
                    while (mm.hasMore()) {
                        BiblioObject biblioObject = mm.nextBiblioObject();
                        System.out.println(this.isAnnotated(biblioObject, annotator_type));
                        if (!AnnotateProperties.isReset() && this.isAnnotated(biblioObject, annotator_type)) {
                            logger.info("\t\t Already annotated by " + annotator_type + ", Skipping...");
                            continue;
                        }

                        // filter based on document size... we should actually annotate only 
                        // a given length and then stop
//                        if (biblioObject.getTeiCorpus().length() > 300000) {
//                            logger.info("skipping " + biblioObject.getRepositoryDocId() + ": file too large");
//                            continue;
//                        }
                        Runnable worker = null;
                        if (annotator_type == Annotator_Type.NERD) {
                            if (biblioObject.getIsProcessedByPub2TEI()) {
                                biblioObject.setTeiCorpus(mm.getTEICorpus(biblioObject));
                                worker = new NerdAnnotatorWorker(mm, biblioObject);
                            } else {
                                logger.info("\t\t No TEI available for " + biblioObject.getRepositoryDocId());
                            }
                        } else if (annotator_type == Annotator_Type.KEYTERM) {
                            if (biblioObject.getIsProcessedByGrobid()) {
                                biblioObject.setGrobidTei(mm.getGrobidTei(biblioObject));
                                worker = new KeyTermAnnotatorWorker(mm, biblioObject);
                            } else {
                                logger.info("\t\t No Grobid TEI available for " + biblioObject.getRepositoryDocId());
                            }
                        } else if (annotator_type == Annotator_Type.QUANTITIES) {
                            if (biblioObject.getIsProcessedByGrobid()) {
                                biblioObject.setGrobidTei(mm.getGrobidTei(biblioObject));
                                worker = new QuantitiesAnnotatorWorker(mm, biblioObject);
                            } else {
                                logger.info("\t\t No Grobid TEI available for " + biblioObject.getRepositoryDocId());
                            }
                        } else if (annotator_type == Annotator_Type.PDFQUANTITIES) {
                            if (biblioObject.getIsWithFulltext()) {
                                BinaryFile bf = new BinaryFile();
                                bf.setStream(mm.getFulltext(biblioObject));
                                biblioObject.setPdf(bf);
                                worker = new PDFQuantitiesAnnotatorWorker(mm, biblioObject);
                            } else {
                                logger.info("\t\t No fulltext available for " + biblioObject.getRepositoryDocId());
                            }
                        }
                        if (worker != null) {
                            worker.run();
                        }
                        nb++;
                    }
                }
            }
            logger.info("Total: " + nb + " documents annotated.");
        } finally {
            mm.close();
        }
    }

    /**
     * Annotates tei collection entries with fulltext (multithread process).
     */
    private void annotateTeiCollectionMultiThreaded(Annotator_Type annotator_type)
            throws UnreachableAnnotateServiceException, AnnotatorNotAvailableException {
        int nb = 0;
        boolean loadPDF = false;
        if (!Annotator_Type.contains(annotator_type.getName())) {
            throw new AnnotatorNotAvailableException("type of annotations not available: " + annotator_type);
        }
        try {
            if (AnnotateService.isAnnotateServiceReady(annotator_type)) {
                ThreadPoolExecutor executor = getThreadsExecutor(annotator_type);

                if (mm.initObjects()) {
                    //logger.info("processing teis for :" + date);
                    while (mm.hasMore()) {
                        BiblioObject biblioObject = mm.nextBiblioObject();
                        if (!AnnotateProperties.isReset() && this.isAnnotated(biblioObject, annotator_type)) {
                            logger.info("\t\t Already annotated by " + annotator_type + ", Skipping...");
                            continue;
                        }

                        // filter based on document size... we should actually annotate only 
                        // a given length and then stop
                        if (biblioObject.getTeiCorpus().length() > 300000) {
                            logger.info("skipping " + biblioObject.getRepositoryDocId() + ": file too large");
                            continue;
                        }
                        Runnable worker = null;
                        if (annotator_type == Annotator_Type.NERD) {
                            if (biblioObject.getIsProcessedByPub2TEI()) {
                                biblioObject.setTeiCorpus(mm.getTEICorpus(biblioObject));
                                worker = new NerdAnnotatorWorker(mm, biblioObject);
                            } else {
                                logger.info("\t\t No TEI available for " + biblioObject.getRepositoryDocId());
                            }
                        } else if (annotator_type == Annotator_Type.KEYTERM) {
                            if (biblioObject.getIsProcessedByGrobid()) {
                                biblioObject.setGrobidTei(mm.getGrobidTei(biblioObject));
                                worker = new KeyTermAnnotatorWorker(mm, biblioObject);
                            } else {
                                logger.info("\t\t No Grobid TEI available for " + biblioObject.getRepositoryDocId());
                            }
                        } else if (annotator_type == Annotator_Type.QUANTITIES) {
                            if (biblioObject.getIsProcessedByGrobid()) {
                                biblioObject.setGrobidTei(mm.getGrobidTei(biblioObject));
                                worker = new QuantitiesAnnotatorWorker(mm, biblioObject);
                            } else {
                                logger.info("\t\t No Grobid TEI available for " + biblioObject.getRepositoryDocId());
                            }
                        } else if (annotator_type == Annotator_Type.PDFQUANTITIES) {
                            if (biblioObject.getIsWithFulltext()) {
                                BinaryFile bf = new BinaryFile();
                                bf.setStream(mm.getFulltext(biblioObject));
                                biblioObject.setPdf(bf);
                                worker = new PDFQuantitiesAnnotatorWorker(mm, biblioObject);
                            } else {
                                logger.info("\t\t No fulltext available for " + biblioObject.getRepositoryDocId());
                            }
                        }
                        if (worker != null) {
                            executor.execute(worker);
                        }
                        nb++;
                    }
                }
                executor.shutdown();
                while (!executor.isTerminated()) {
                }
                logger.info("Finished all threads");
                logger.info("Total: " + nb + " documents annotated.");
            }
        } finally {
            mm.close();
        }
    }

    private ThreadPoolExecutor getThreadsExecutor(Annotator_Type annotator_type) {
        // max queue of tasks of 50 
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(50);
        int nbThreads = 1;
        if (annotator_type == annotator_type.NERD) {
            nbThreads = AnnotateProperties.getNerdNbThreads();
        } else if (annotator_type == annotator_type.KEYTERM) {
            nbThreads = AnnotateProperties.getKeytermNbThreads();
        } else if (annotator_type == annotator_type.QUANTITIES) {
            nbThreads = AnnotateProperties.getQuantitiesNbThreads();
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(nbThreads, nbThreads, 60000,
                TimeUnit.MILLISECONDS, blockingQueue);

        // this is for handling rejected tasks (e.g. queue is full)
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r,
                    ThreadPoolExecutor executor) {
                logger.info("Task Rejected : "
                        + ((AnnotatorWorker) r).getRepositoryDocId());
                logger.info("Waiting for 60 second !!");
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("Lets add another time : "
                        + ((AnnotatorWorker) r).getRepositoryDocId());
                executor.execute(r);
            }
        });
        executor.prestartAllCoreThreads();
        return executor;
    }

}
