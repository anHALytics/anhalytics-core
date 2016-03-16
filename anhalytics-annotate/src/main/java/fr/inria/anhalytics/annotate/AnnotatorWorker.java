package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.commons.managers.MongoFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Runnable for annotating HAL documents.
 * 
 * @author Achraf, Patrice
 */
public class AnnotatorWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AnnotatorWorker.class);
    protected MongoFileManager mm = null;
    protected String documentId = null;
    protected String docId = null;
    protected String date;

    public AnnotatorWorker(MongoFileManager mongoManager,
            String documentId,
            String docId,
            //String tei,
            String date) {
        this.mm = mongoManager;
        this.documentId = documentId;
        this.docId = docId;
        //this.tei = tei;
        this.date = date;
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
