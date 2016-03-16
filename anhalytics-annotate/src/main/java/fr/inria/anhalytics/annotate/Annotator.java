package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.annotate.properties.AnnotateProperties;
import fr.inria.anhalytics.commons.exceptions.UnreachableNerdServiceException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.UnknownHostException;
import java.util.concurrent.*;

/**
 * Handles threads used to annotate the tei collection from MongoDB.
 *
 * @author Patrice
 */
public class Annotator {

    private static final Logger logger = LoggerFactory.getLogger(Annotator.class);

    private final MongoFileManager mm;

    public enum Annotator_Type {
        NERD("NERD"),
        KEYTERM("KeyTerm");

        private String name;

        private Annotator_Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public Annotator() throws UnknownHostException {
        this.mm = MongoFileManager.getInstance(false);
    }

    public void annotate(Annotator_Type annotator_type) {
        try {
            if (AnnotateProperties.isIsMultiThread()) {
                annotateTeiCollectionMultiThreaded(annotator_type);
            } else {
                annotateTeiCollection(annotator_type);
            }
        } catch (Exception e) {
            logger.error("Error when setting-up the annotator.");
            e.printStackTrace();
        }
    }

    /**
     * Annotates tei collection entries with fulltext.
     */
    private void annotateTeiCollection(Annotator_Type annotator_type) throws UnreachableNerdServiceException {
        int nb = 0;
        try {
            if (NerdService.isNerdReady()) {
                for (String date : Utilities.getDates()) {
                    if (mm.initTeis(date)) {
                        logger.debug("processing teis for :" + date);
                        while (mm.hasMoreTeis()) {
                            String tei = mm.nextTeiDocument();
                            String id = mm.getCurrentRepositoryDocId();
                            String docID = mm.getCurrentDocId();
                            if (!mm.isWithFulltext(id)) {
                                continue;
                            }
                            // check if the document is already annotated
                            if (!AnnotateProperties.isReset()) {
                                if (mm.isAnnotated()) {
                                    logger.debug("skipping " + id + ": already annotated");
                                    continue;
                                }
                            }

                            // filter based on document size... we should actually annotate only 
                            // a given length and then stop
                            if (tei.length() > 300000) {
                                logger.debug("skipping " + id + ": file too large");
                                continue;
                            }
                            Runnable worker = null;
                            if (annotator_type == Annotator_Type.NERD) {
                                worker = new NerdAnnotatorWorker(mm, id, docID, tei, date);
                            } else if (annotator_type == Annotator_Type.KEYTERM) {
                                worker = new KeyTermAnnotatorWorker(mm, id, docID, tei, date);    
                            }
                            worker.run();
                            nb++;
                        }
                    }
                }
            }
            logger.debug("Total: " + nb + " annotations produced.");
        } finally {
            mm.close();
        }
    }

    /**
     * Annotates tei collection entries with fulltext (multithread process).
     */
    private void annotateTeiCollectionMultiThreaded(Annotator_Type annotator_type) throws UnreachableNerdServiceException {
        int nb = 0;
        try {
            if (NerdService.isNerdReady()) {
                ThreadPoolExecutor executor = getThreadsExecutor(annotator_type);
                for (String date : Utilities.getDates()) {
                    if (mm.initTeis(date)) {
                        //logger.debug("processing teis for :" + date);
                        while (mm.hasMoreTeis()) {
                            String tei = mm.nextTeiDocument();
                            String id = mm.getCurrentRepositoryDocId();
                            String docID = mm.getCurrentDocId();
                            if (!mm.isWithFulltext(id)) {
                                continue;
                            }
                            // check if the document is already annotated
                            if (!AnnotateProperties.isReset()) {
                                if (mm.isAnnotated()) {
                                    logger.debug("skipping " + id + ": already annotated");
                                    continue;
                                }
                            }

                            // filter based on document size... we should actually annotate only 
                            // a given length and then stop
                            if (tei.length() > 300000) {
                                logger.debug("skipping " + id + ": file too large");
                                continue;
                            }

                            Runnable worker = null;
                            if (annotator_type == Annotator_Type.NERD) {
                                worker = new NerdAnnotatorWorker(mm, id, docID, tei, date);
                                worker.run();
                                nb++;
                            }
                        }
                    }
                }
                executor.shutdown();
                while (!executor.isTerminated()) {
                }
                logger.info("Finished all threads");
                logger.debug("Total: " + nb + " annotations produced.");
            }
        } finally {
            mm.close();
        }
    }

    private ThreadPoolExecutor getThreadsExecutor(Annotator_Type annotator_type) {
        // max queue of tasks of 50 
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(50);
        int nbThreads = 1;
        if (annotator_type == annotator_type.NERD)
            nbThreads = AnnotateProperties.getNerdNbThreads();
        else if (annotator_type == annotator_type.KEYTERM)
            nbThreads = AnnotateProperties.getKeytermNbThreads();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(nbThreads, nbThreads, 60000,
                TimeUnit.MILLISECONDS, blockingQueue);

        // this is for handling rejected tasks (e.g. queue is full)
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r,
                    ThreadPoolExecutor executor) {
                logger.info("Task Rejected : "
                        + ((AnnotatorWorker) r).getdocumentId());
                logger.info("Waiting for 60 second !!");
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("Lets add another time : "
                        + ((AnnotatorWorker) r).getdocumentId());
                executor.execute(r);
            }
        });
        executor.prestartAllCoreThreads();
        return executor;
    }

}
