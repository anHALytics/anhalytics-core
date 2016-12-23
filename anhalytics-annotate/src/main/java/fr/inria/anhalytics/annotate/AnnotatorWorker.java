package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.commons.data.TEIFile;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runnable for annotating HAL documents.
 * 
 * @author Achraf, Patrice
 */
public abstract class AnnotatorWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AnnotatorWorker.class);
    protected MongoFileManager mm = null;
    protected TEIFile tei = null;
    protected String date;
    protected String annotationsCollection;

    public AnnotatorWorker(MongoFileManager mongoManager,
            TEIFile tei,
            String date, 
            String annotationsCollection) {
        this.mm = mongoManager;
        this.tei = tei;
        this.date = date;
        this.annotationsCollection = annotationsCollection;
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();
        logger.info("\t\t " + Thread.currentThread().getName() + " Start. Processing = " + tei.getRepositoryDocId());
        processCommand();
        long endTime = System.nanoTime();
        logger.info("\t\t " + Thread.currentThread().getName() + " End. :" + (endTime - startTime) / 1000000 + " ms");
    }
    protected abstract void processCommand() ;
    protected abstract String annotateDocument() ;

    /**
     * return documentId of the file being annotated.
     */
    public String getRepositoryDocId() {
        return tei.getRepositoryDocId();
    }

    @Override
    public String toString() {
        return this.tei.getRepositoryDocId();
    }
}
