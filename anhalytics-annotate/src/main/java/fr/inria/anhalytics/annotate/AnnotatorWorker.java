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
 ** Runnable that uses the NERD REST service for annotating HAL TEI
 * documents.Resulting JSON annotations are then stored in MongoDB as persistent
 * storage.
 * 
 * @author Achraf
 */
public class AnnotatorWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AnnotatorWorker.class);
    private MongoFileManager mm = null;
    private String tei = null;
    private String documentId = null;
    private String date;

    public AnnotatorWorker(MongoFileManager mongoManager,
            String documentId,
            String tei,
            String date) {
        this.mm = mongoManager;
        this.documentId = documentId;
        this.tei = tei;
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

    private void processCommand() {
        List<String> halDomainTexts = new ArrayList<String>();
        List<String> halDomains = new ArrayList<String>();
        List<String> meSHDescriptors = new ArrayList<String>();
        // DocumentBuilderFactory and DocumentBuilder are not thread safe, 
        // so one per task
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        Document docTei = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            // parse the TEI
            docTei = docBuilder.parse(new InputSource(new ByteArrayInputStream(tei.getBytes("utf-8"))));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // get the HAL domain 
        NodeList classes = docTei.getElementsByTagName("classCode");
        for (int p = 0; p < classes.getLength(); p++) {
            Node node = classes.item(p);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) (node);
                // filter on attribute @scheme="halDomain"
                String scheme = e.getAttribute("scheme");
                if ((scheme != null) && scheme.equals("halDomain")) {
                    halDomainTexts.add(e.getTextContent());
                    String n_att = e.getAttribute("n");
                    halDomains.add(n_att);
                } else if ((scheme != null) && scheme.equals("mesh")) {
                    meSHDescriptors.add(e.getTextContent());
                }
            }
        }
        // get all the elements having an attribute id and annotate their text content
        String jsonAnnotations = annotateDocument(docTei, documentId);
        mm.insertAnnotation(jsonAnnotations);
        logger.debug("\t\t " + documentId + " annotated.");
    }

    /**
     * Annotation of a complete document.
     */
    public String annotateDocument(Document doc,
            String documentId) {
        StringBuffer json = new StringBuffer();
        json.append("{ \"repositoryDocId\" : \"" + documentId
                +"\", \"date\" :\"" + date
                +"\", \"nerd\" : [");
        annotateNode(doc.getDocumentElement(), true, json);
        json.append("] }");
        return json.toString();
    }

    /**
     * Recursive tree walk for annotating every nodes having a random xml:id.
     */
    public boolean annotateNode(Node node,
            boolean first,
            StringBuffer json) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element) (node);
            String id = e.getAttribute("xml:id");
            if (id.startsWith("_") && (id.length() == 8)) {
                // get the textual content of the element
                // annotate
                String text = e.getTextContent();
                String jsonText = null;
                try {
                    NerdService nerdService = new NerdService(text);
                    jsonText = nerdService.runNerd();
                } catch (Exception ex) {
                    logger.error("Text could not be annotated by NERD: " + text);
                    ex.printStackTrace();
                }
                if (jsonText != null) {
                    // resulting annotations, with the corresponding id
                    if (first) {
                        first = false;
                    } else {
                        json.append(", ");
                    }
                    json.append("{ \"xml:id\" : \"" + id + "\", \"nerd\" : " + jsonText + " }");
                }
            }
        }
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            first = annotateNode(currentNode, first, json);
        }
        return first;
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
