package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.annotate.services.GrobidSuperconductorsService;
import fr.inria.anhalytics.annotate.services.QuantitiesService;
import fr.inria.anhalytics.commons.data.AnnotatorType;
import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Annotates the quantities extracted from each text element in the TEICorpus using Grobid Quantities.
 * <p>
 * The content of every TEI elements having an attribute @xml:id randomly
 * generated will be annotated. The annotations follow a stand-off
 * representation that is using the @xml:id as base and offsets to identified
 * the annotated chunk of text.
 */
public class SuperconductorsAnnotatorWorker extends AnnotatorWorker {

    private static final Logger logger = LoggerFactory.getLogger(SuperconductorsAnnotatorWorker.class);

    public SuperconductorsAnnotatorWorker(MongoFileManager mongoManager,
                                          BiblioObject biblioObject) {
        super(mongoManager, biblioObject, MongoCollectionsInterface.SUPERCONDUCTORS_ANNOTATIONS);
    }

    @Override
    protected void processCommand() {
        // get all the elements having an attribute id and annotate their text content
        boolean inserted = mm.insertAnnotation(annotateDocument(), annotationsCollection);
        if (inserted) {
            mm.updateBiblioObjectStatus(biblioObject, AnnotatorType.SUPERCONDUCTORS, false);
            logger.info("\t\t " + Thread.currentThread().getName() + ": "
                    + biblioObject.getRepositoryDocId() + " annotated by the " + AnnotatorType.SUPERCONDUCTORS + " service.");
        } else {
            logger.info("\t\t " + Thread.currentThread().getName() + ": "
                    + biblioObject.getRepositoryDocId() + " error occurred trying to annotate with SUPERCONDUCTORS.");
        }
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
            String tei = biblioObject.getGrobidTei() != null ? biblioObject.getGrobidTei() : biblioObject.getTeiCorpus();
            // parse the TEI
            docTei = docBuilder.parse(new InputSource(new ByteArrayInputStream(tei.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        StringBuffer json = new StringBuffer();
        json.append("{ \"repositoryDocId\" : \"")
                .append(biblioObject.getRepositoryDocId())
                .append("\",\"anhalyticsId\" : \"")
                .append(biblioObject.getAnhalyticsId()
                        //                    + "\", \"date\" :\"" + date
                ).append("\", \"isIndexed\" :\"")
                .append(false).append("\", \"annotation\" : [ ");

        //check if any thing was added, throw exception if not (not insert entry)
        assert docTei != null;
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
                        GrobidSuperconductorsService service = new GrobidSuperconductorsService(IOUtils.toInputStream(text, "UTF-8"));
                        jsonText = service.process();
                    } catch (Exception ex) {
                        logger.error("\t\t " + Thread.currentThread().getName() + ": Text could not be annotated by SUPERCONDUCTORS: " + text, ex);
                    }
                    if (jsonText == null) {
                        logger.error("\t\t " + Thread.currentThread().getName() + ": SUPERCONDUCTORS failed annotating text : " + text);
                    }
                    if (jsonText != null) {
                        // resulting annotations, with the corresponding id
                        if (first) {
                            first = false;
                        } else {
                            json.append(", ");
                        }
                        json.append("{ \"xml:id\" : \"" + id + "\", \"value\" : " + jsonText + " }");
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
