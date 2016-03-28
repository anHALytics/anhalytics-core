package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
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
 * Runnable that uses the NERD REST service for annotating HAL TEI
 * documents.Resulting JSON annotations are then stored in MongoDB as persistent
 * storage.
 *
 * The content of every TEI elements having an attribute @xml:id randomly generated 
 * will be annotated. The annotations follow a stand-off representation that is using 
*  the @xml:id as base and offsets to identified the annotated chunk of text. 
 * 
 * @author Achraf, Patrice
 */
public class KeyTermAnnotatorWorker extends AnnotatorWorker {

    private static final Logger logger = LoggerFactory.getLogger(KeyTermAnnotatorWorker.class);
    private String tei = null;

    public KeyTermAnnotatorWorker(MongoFileManager mongoManager,
            String documentId,
            String anhalyticsId,
            String tei,
            String date) {
        super(mongoManager, documentId, anhalyticsId, date, MongoCollectionsInterface.KEYTERM_ANNOTATIONS);
        this.tei = tei;
    }

    @Override
    protected void processCommand() {        
        // NOTE: the part bellow should be used in the future for improving the keyterm disambiguation 
        // by setting a custom domain context which helps the disambiguation (so don't remove it ;)
        
        /*List<String> halDomainTexts = new ArrayList<String>();
        List<String> halDomains = new ArrayList<String>();
        List<String> meSHDescriptors = new ArrayList<String>();

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
        }*/

        mm.insertAnnotation(annotateDocument(tei, documentId, anhalyticsId), annotationsCollection);
        logger.debug("\t\t " + documentId + " annotated by the KeyTerm extraction and disambiguation service.");
    }

    /**
     * Annotation of a complete document with extacted disambiguated key terms
     */
    private String annotateDocument(String tei,
            String documentId, String docId) {
        StringBuffer json = new StringBuffer();
        json.append("{ \"repositoryDocId\" : \"" + documentId
                +"\",\"anhalyticsId\" : \"" + anhalyticsId
                +"\", \"date\" :\"" + date
                +"\", \"keyterm\" : ");
        String jsonText = null;
        try {
            KeyTermExtractionService keyTermService = new KeyTermExtractionService(documentId, tei);
            jsonText = keyTermService.runKeyTermExtraction();
        } catch (Exception ex) {
            logger.error("TEI could not be processed by the keyterm extractor: " + documentId);
            ex.printStackTrace();
        }
        if (jsonText != null) {
            json.append(jsonText).append("}");
        }
        else
            json.append("{} }");
        return json.toString();
    }
}
