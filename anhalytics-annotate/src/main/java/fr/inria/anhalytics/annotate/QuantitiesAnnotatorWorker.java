package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.annotate.services.QuantitiesService;
import fr.inria.anhalytics.commons.data.TEIFile;
import fr.inria.anhalytics.commons.exceptions.DataException;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.apache.commons.io.IOUtils;

/**
 *
 * @author azhar
 */
public class QuantitiesAnnotatorWorker extends AnnotatorWorker {

    private static final Logger logger = LoggerFactory.getLogger(QuantitiesAnnotatorWorker.class);

    public QuantitiesAnnotatorWorker(MongoFileManager mongoManager,
            TEIFile tei,
            String date) {
        super(mongoManager, tei, null, MongoCollectionsInterface.QUANTITIES_ANNOTATIONS);
    }

    @Override
    protected void processCommand() {

        // get all the elements having an attribute id and annotate their text content
        mm.insertAnnotation(annotateDocument(), annotationsCollection);
        logger.info("\t\t " + Thread.currentThread().getName() + ": " + 
            file.getRepositoryDocId() + " annotated by the QUANTITIES service.");
           
    }

    @Override
    protected String annotateDocument() {
        // DocumentBuilderFactory and DocumentBuilder are not thread safe, 
        // so one per task
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        Document docTei = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            // parse the TEI
            docTei = docBuilder.parse(new InputSource(new ByteArrayInputStream(((TEIFile)file).getTei().getBytes("UTF-8"))));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        StringBuffer json = new StringBuffer();
            json.append("{ \"repositoryDocId\" : \"" + file.getRepositoryDocId() 
 //                   + "\", \"category\" :\"" + "titi"
                    + "\", \"measurements\" : [ ");
            
            //check if any thing was added, throw exception if not (not insert entry)
        annotateNode(docTei.getDocumentElement(), true, json, null);
        json.append("] }");      
        
        return json.toString();
    }
    
     /**
     * Recursive tree walk for annotating every nodes having a random xml:id.
     */
    private boolean annotateNode(Node node,
            boolean first,
            StringBuffer json,
            String language) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element) (node);
            String id = e.getAttribute("xml:id");
            String new_language = e.getAttribute("xml:lang");
            if ((new_language != null) && (new_language.length() > 0)) {
                language = new_language;
            }
            if (id.startsWith("_") && (id.length() == 8)) {
                // get the textual content of the element
                // annotate
                String text = e.getTextContent();
                if ((text != null) && (text.trim().length() > 1)) {
                    String jsonText = null;
                    try {
                        QuantitiesService quantitiesService = new QuantitiesService(IOUtils.toInputStream(text, "UTF-8"));
                        jsonText = quantitiesService.processTextQuantities();
                    } catch (Exception ex) {
                        logger.error("\t\t " + Thread.currentThread().getName() + ": Text could not be annotated by QUANTITIES: " + text);
                        ex.printStackTrace();
                    }
                    if (jsonText == null) {
                        logger.error("\t\t " + Thread.currentThread().getName() + ": QUANTITIES failed annotating text : " + text);
                    }
                    if (jsonText != null) {
                        // resulting annotations, with the corresponding id
                        if (first) {
                            first = false;
                        } else {
                            json.append(", ");
                        }
                        json.append("{ \"xml:id\" : \"" + id + "\", \"quantities\" : " + jsonText + " }");
                    }
                }
            }
        }
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            first = annotateNode(currentNode, first, json, language);
        }
        return first;
    }

}
