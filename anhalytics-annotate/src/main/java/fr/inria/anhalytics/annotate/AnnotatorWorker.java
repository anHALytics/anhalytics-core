package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.managers.MongoFileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runnable for annotating HAL documents.
 * 
 * @author Achraf, Patrice
 */
public abstract class AnnotatorWorker implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotatorWorker.class);
    protected MongoFileManager mm = null;
    protected BiblioObject biblioObject = null;
    protected String annotationsCollection;

    public AnnotatorWorker(MongoFileManager mongoManager,
            BiblioObject biblioObject,
            String annotationsCollection) {
        this.mm = mongoManager;
        this.biblioObject = biblioObject;
        this.annotationsCollection = annotationsCollection;
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();
        LOGGER.info("\t\t " + Thread.currentThread().getName() + " Start. Processing = "+biblioObject.getRepositoryDocId());
        processCommand();
        long endTime = System.nanoTime();
        LOGGER.info("\t\t " + Thread.currentThread().getName() + " End. :" + (endTime - startTime) / 1000000 + " ms");
    }
    protected abstract void processCommand() ;
    protected abstract String annotateDocument() ;

    /**
     * return documentId of the file being annotated.
     */
    public String getRepositoryDocId() {
        return biblioObject.getRepositoryDocId();
    }

    @Override
    public String toString() {
        return this.biblioObject.getRepositoryDocId();
    }
}
