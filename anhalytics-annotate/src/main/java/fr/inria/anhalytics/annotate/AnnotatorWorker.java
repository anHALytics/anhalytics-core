package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.commons.data.File;
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
    protected File file = null;
    protected String date;
    protected String annotationsCollection;

    public AnnotatorWorker(MongoFileManager mongoManager,
            File file,
            String date, 
            String annotationsCollection) {
        this.mm = mongoManager;
        this.file = file;
        this.date = date;
        this.annotationsCollection = annotationsCollection;
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();
        logger.info("\t\t " + Thread.currentThread().getName() + " Start. Processing = "+file.getRepositoryDocId());
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
        return file.getRepositoryDocId();
    }

    @Override
    public String toString() {
        return this.file.getRepositoryDocId();
    }
}
