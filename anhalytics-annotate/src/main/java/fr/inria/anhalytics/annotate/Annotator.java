package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.annotate.properties.AnnotateProperties;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.UnknownHostException;
import java.util.concurrent.*;

/**
 * Use the NERD REST service for annotating HAL TEI documents. Resulting JSON
 * annotations are then stored in MongoDB as persistent storage.
 *
 * @author Patrice Lopez
 */
public class Annotator {

    private static final Logger logger = LoggerFactory.getLogger(Annotator.class);

    private final MongoFileManager mm;

    public Annotator() throws UnknownHostException {
        this.mm = MongoFileManager.getInstance(false);        
    }

    public int annotateCollection() {
        int nb = 0;
        try {
            mm.setGridFS(MongoCollectionsInterface.FINAL_TEIS);
            for (String date : Utilities.getDates()) {
                if (mm.initTeiFiles(date)) {
                    //logger.debug("processing teis for :" + date);
                    while (mm.hasMoreDocuments()) {
                        String tei = mm.nextDocument();
                        String filename = mm.getCurrentFilename();
                        String halID = mm.getCurrentHalID();

                        try {
                            // check if the document is already annotated
                            if (!(AnnotateProperties.isReset() | !mm.isAnnotated())) {// I know not easy to catch up..
                                logger.debug("skipping " + filename + ": already annotated");
                                continue;
                            }

                            
                            // filter based on document size... we should actually annotate only 
                            // a given length and then stop
                            if (tei.length() > 300000) {
                                logger.debug("skipping " + filename + ": file too large");
                                continue;
                            }
                            AnnotatorWorker worker
                                    = new AnnotatorWorker(mm, filename, halID, tei);
                            worker.run();
                            nb++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mm.close();
        }
        return nb;
    }

    public int annotateCollectionMultiThreaded() {
        ThreadPoolExecutor executor = getThreadsExecutor();
        int nb = 0;
        try {
            mm.setGridFS(MongoCollectionsInterface.FINAL_TEIS);
            for (String date : Utilities.getDates()) {
                if (mm.initTeiFiles(date)) {
                    //logger.debug("processing teis for :" + date);
                    while (mm.hasMoreDocuments()) {
                        String tei = mm.nextDocument();
                        
                        String filename = mm.getCurrentFilename();
                        String halID = mm.getCurrentHalID();
                        // check if the document is already annotated
                        if (!(AnnotateProperties.isReset() | !mm.isAnnotated())) { // I know not easy to catch up..
                            logger.debug("skipping " + filename + ": already annotated");
                            continue;
                        }
                        
                        // filter based on document size... we should actually annotate only 
                        // a given length and then stop
                        if (tei.length() > 300000) {
                            logger.debug("skipping " + filename + ": file too large");
                            continue;
                        }

                        Runnable worker
                                = new AnnotatorWorker(mm, filename, halID, tei);
                        executor.execute(worker);
                        nb++;
                    }
                }
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            System.out.println("Finished all threads");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mm.close();
        }
        return nb;
    }

    private ThreadPoolExecutor getThreadsExecutor() {
        // max queue of tasks of 50 
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(50);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(AnnotateProperties.getNbThreads(), AnnotateProperties.getNbThreads(), 60000,
                TimeUnit.MILLISECONDS, blockingQueue);

        // this is for handling rejected tasks (e.g. queue is full)
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r,
                    ThreadPoolExecutor executor) {
                System.out.println("Task Rejected : "
                        + ((AnnotatorWorker) r).getFilename());
                System.out.println("Waiting for 60 second !!");
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Lets add another time : "
                        + ((AnnotatorWorker) r).getFilename());
                executor.execute(r);
            }
        });
        executor.prestartAllCoreThreads();
        return executor;
    }
    
}
