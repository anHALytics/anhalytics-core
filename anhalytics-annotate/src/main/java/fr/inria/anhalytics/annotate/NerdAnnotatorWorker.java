package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
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
public class NerdAnnotatorWorker extends AnnotatorWorker {

    private static final Logger logger = LoggerFactory.getLogger(NerdAnnotatorWorker.class);
    private String tei = null;

    public NerdAnnotatorWorker(MongoFileManager mongoManager,
            String documentId,
            String anhalyticsId,
            String tei,
            String date) {
        super(mongoManager, documentId, anhalyticsId, date, MongoCollectionsInterface.NERD_ANNOTATIONS);
        this.tei = tei;
    }

    @Override
    protected void processCommand() {
        // DocumentBuilderFactory and DocumentBuilder are not thread safe, 
        // so one per task
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        Document docTei = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            // parse the TEI
            docTei = docBuilder.parse(new InputSource(new ByteArrayInputStream(tei.getBytes("UTF-8"))));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        // NOTE: the part bellow should be used in the future for improving the NERD by setting a custom 
        // domain context which helps the disambiguation
        
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
        // get all the elements having an attribute id and annotate their text content
        mm.insertAnnotation(annotateDocument(docTei, documentId, anhalyticsId), annotationsCollection);
        logger.debug("\t\t " + documentId + " annotated by the NERD service.");
    }

    /**
     * Annotation of a complete document.
     */
    private String annotateDocument(Document doc,
            String documentId, String docId) {
        StringBuffer json = new StringBuffer();
        json.append("{ \"repositoryDocId\" : \"" + documentId
                +"\",\"anhalyticsId\" : \"" + anhalyticsId
                +"\", \"date\" :\"" + date
                +"\", \"nerd\" : [");
        annotateNode(doc.getDocumentElement(), true, json);
        json.append("] }");
        return json.toString();
    }

    /**
     * Recursive tree walk for annotating every nodes having a random xml:id.
     */
    private boolean annotateNode(Node node,
            boolean first,
            StringBuffer json) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element) (node);
            String id = e.getAttribute("xml:id");
            if (id.startsWith("_") && (id.length() == 8)) {
                // get the textual content of the element
                // annotate
                String text = e.getTextContent();
				if ( (text != null) && (text.trim().length() > 1)) {
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
        }
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            first = annotateNode(currentNode, first, json);
        }
        return first;
    }
}
