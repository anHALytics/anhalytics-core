package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.commons.properties.AnnotateProperties;
import fr.inria.anhalytics.annotate.exceptions.UnreachableAnnotateServiceException;
import fr.inria.anhalytics.annotate.exceptions.AnnotatorNotAvailableException;
import fr.inria.anhalytics.annotate.services.AnnotateService;
import fr.inria.anhalytics.commons.data.IstexFile;
import fr.inria.anhalytics.commons.data.TEIFile;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
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

    public enum Annotator_Type {

        NERD("NERD"),
        KEYTERM("KeyTerm"),
        QUANTITIES("Quantities"),
        PDFQUANTITIES("PDFQuantities");

        private String name;

        private Annotator_Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public Annotator() {
        this.mm = MongoFileManager.getInstance(false);
    }

    public void annotate(Annotator_Type annotator_type) {
        try {
            if (annotator_type == Annotator_Type.PDFQUANTITIES) {
                annotatePDFQuantitiesCollection();
            } else if (AnnotateProperties.isIsMultiThread()) {
                /*if (annotator_type == Annotator_Type.QUANTITIES) {
                    annotateQuantitiesTeiCollectionMultiThreaded();
                } else */{
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

    private void annotatePDFQuantitiesCollection()
            throws UnreachableAnnotateServiceException, AnnotatorNotAvailableException {
        try {
            if (AnnotateService.isAnnotateServiceReady(Annotator_Type.PDFQUANTITIES)) {
                if (mm.initIstexPdfs()) {

                    logger.info("processing...");
                    while (mm.hasMore()) {
                        IstexFile istexFile = mm.nextIstexBinaryDocument();
                        Runnable worker = new PDFQuantitiesAnnotatorWorker(mm, istexFile, null);
                        worker.run();
//                            nb++;

                    }

                }

            }
        } finally {
            mm.close();
        }
    }

    /**
     * Annotates quantities tei collection entries with text (multithread
     * process).
     */
    private void annotateQuantitiesTeiCollectionMultiThreaded() {
        try {
            if (AnnotateService.isAnnotateServiceReady(Annotator_Type.QUANTITIES)) {

            }
            if (mm.initIstexTeis()) {

                logger.info("processing...");
                while (mm.hasMore()) {
                    TEIFile istexFile = mm.nextIstexTeiDocument();
                    Runnable worker = new QuantitiesAnnotatorWorker(mm, istexFile, null);
                    worker.run();
//                            nb++;

                }

            }

        } finally {
            mm.close();
        }

    }

    /**
     * Annotates tei collection entries with fulltext.
     */
    private void annotateTeiCollection(Annotator_Type annotator_type)
            throws UnreachableAnnotateServiceException, AnnotatorNotAvailableException {
        int nb = 0;
        String annotationsCollection = null, teiCollection = null;
        if (annotator_type == Annotator_Type.NERD) {
            annotationsCollection = MongoCollectionsInterface.NERD_ANNOTATIONS;
            teiCollection = MongoCollectionsInterface.METADATA_WITHFULLTEXT_TEIS;
        } else if (annotator_type == Annotator_Type.KEYTERM) {
            annotationsCollection = MongoCollectionsInterface.KEYTERM_ANNOTATIONS;
            teiCollection = MongoCollectionsInterface.GROBID_TEIS;
        } else if (annotator_type == Annotator_Type.QUANTITIES) {
            annotationsCollection = MongoCollectionsInterface.QUANTITIES_ANNOTATIONS;
            teiCollection = MongoCollectionsInterface.GROBID_TEIS;
        } else {
            throw new AnnotatorNotAvailableException("type of annotations not available: " + annotator_type);
        }
        try {
            if (AnnotateService.isAnnotateServiceReady(annotator_type)) {
                for (String date : Utilities.getDates()) {

                    if (!AnnotateProperties.isProcessByDate()) {
                        date = null;
                    }
                    if (mm.initTeis(date, teiCollection)) {
                        logger.info("processing teis for :" + date);
                        while (mm.hasMore()) {
                            TEIFile tei = mm.nextTeiDocument();
                            if (tei.getAnhalyticsId() == null || tei.getAnhalyticsId().isEmpty()) {
                                logger.info("skipping " + tei.getRepositoryDocId() + " No anHALytics id provided");
                                continue;
                            }
                            // check if the document is already annotated
                            if (!AnnotateProperties.isReset()) {
                                if (mm.isAnnotated(annotationsCollection, tei.getAnhalyticsId())) {
                                    logger.info("skipping " + tei.getRepositoryDocId() + ": already annotated");
                                    continue;
                                }
                            }

                            // filter based on document size... we should actually annotate only 
                            // a given length and then stop
                            if (tei.getTei().length() > 300000) {
                                logger.info("skipping " + tei.getRepositoryDocId() + ": file too large");
                                continue;
                            }
                            Runnable worker = null;
                            if (annotator_type == Annotator_Type.NERD) {
                                worker = new NerdAnnotatorWorker(mm, tei, date);
                            } else if (annotator_type == Annotator_Type.KEYTERM) {
                                worker = new KeyTermAnnotatorWorker(mm, tei, date);
                            } else if (annotator_type == Annotator_Type.QUANTITIES) {
                                worker = new QuantitiesAnnotatorWorker(mm, tei, date);
                            }
                            worker.run();
                            nb++;
                        }
                    }
                    if (!AnnotateProperties.isProcessByDate()) {
                        break;
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
        String annotationsCollection = null, teiCollection = null;
        if (annotator_type == Annotator_Type.NERD) {
            annotationsCollection = MongoCollectionsInterface.NERD_ANNOTATIONS;
            teiCollection = MongoCollectionsInterface.METADATA_WITHFULLTEXT_TEIS;
        } else if (annotator_type == Annotator_Type.KEYTERM) {
            annotationsCollection = MongoCollectionsInterface.KEYTERM_ANNOTATIONS;
            teiCollection = MongoCollectionsInterface.GROBID_TEIS;
        } else if (annotator_type == Annotator_Type.QUANTITIES) {
            annotationsCollection = MongoCollectionsInterface.QUANTITIES_ANNOTATIONS;
            teiCollection = MongoCollectionsInterface.GROBID_TEIS;
        } else {
            throw new AnnotatorNotAvailableException("type of annotations not supported: " + annotator_type);
        }
        try {
            if (AnnotateService.isAnnotateServiceReady(annotator_type)) {
                ThreadPoolExecutor executor = getThreadsExecutor(annotator_type);
                for (String date : Utilities.getDates()) {
                    if (!AnnotateProperties.isProcessByDate()) {
                        date = null;
                    }
                    if (mm.initTeis(date, teiCollection)) {
                        //logger.info("processing teis for :" + date);
                        while (mm.hasMore()) {
                            TEIFile tei = mm.nextTeiDocument();

                            if (tei.getAnhalyticsId() == null || tei.getAnhalyticsId().isEmpty()) {
                                logger.info("skipping " + tei.getRepositoryDocId() + " No anHALytics id provided");
                                continue;
                            }
                            // check if the document is already annotated
                            if (!AnnotateProperties.isReset()) {
                                if (mm.isAnnotated(annotationsCollection, tei.getAnhalyticsId())) {
                                    logger.info("skipping " + tei.getRepositoryDocId() + ": already annotated");
                                    continue;
                                }
                            }

                            // filter based on document size... we should actually annotate only 
                            // a given length and then stop
                            if (tei.getTei().length() > 300000) {
                                logger.info("skipping " + tei.getRepositoryDocId() + ": file too large");
                                continue;
                            }
                            Runnable worker = null;
                            if (annotator_type == Annotator_Type.NERD) {
                                worker = new NerdAnnotatorWorker(mm, tei, date);
                            } else if (annotator_type == Annotator_Type.KEYTERM) {
                                worker = new KeyTermAnnotatorWorker(mm, tei, date);
                            } else if (annotator_type == Annotator_Type.QUANTITIES) {
                                worker = new QuantitiesAnnotatorWorker(mm, tei, date);
                            }

                            executor.execute(worker);
                            nb++;
                        }
                    }
                    if (!AnnotateProperties.isProcessByDate()) {
                        break;
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
