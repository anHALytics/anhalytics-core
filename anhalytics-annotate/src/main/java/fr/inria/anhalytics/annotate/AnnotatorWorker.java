package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.commons.managers.MongoFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runnable for annotating HAL documents.
 * 
 * @author Achraf, Patrice
 */
public class AnnotatorWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AnnotatorWorker.class);
    protected MongoFileManager mm = null;
    protected String documentId = null;
    protected String anhalyticsId = null;
    protected String date;
    protected String annotationsCollection;

    public AnnotatorWorker(MongoFileManager mongoManager,
            String documentId,
            String anhalyticsId,
            String date, 
            String annotationsCollection) {
        this.mm = mongoManager;
        this.documentId = documentId;
        this.anhalyticsId = anhalyticsId;
        this.date = date;
        this.annotationsCollection = annotationsCollection;
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();
        logger.info(Thread.currentThread().getName() + " Start. Processing = " + documentId);
        processCommand();
        long endTime = System.nanoTime();
        logger.info(Thread.currentThread().getName() + " End. :" + (endTime - startTime) / 1000000 + " ms");
    }

    protected void processCommand() {
    }

    /**
     * return documentId of the file being annotated.
     */
    public String getdocumentId() {
        return documentId;
    }

    @Override
    public String toString() {
        return this.documentId;
    }
}
