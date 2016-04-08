package fr.inria.anhalytics.harvest.teibuild;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.xml.sax.InputSource;

/**
 * Functions that builds tei corpus, eventually appends fulltext.
 * 
 * @author Achraf
 */
public class TeiBuilder {

    /**
     * appends fulltext grobid tei to existing metadata.
    */
    public static Document addGrobidTeiToTei(String finalTei, String grobidTei) {
        Document resultTei = null;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);

        DocumentBuilder docBuilder = null;
        Document tei = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            tei = docBuilder.parse(new InputSource(new ByteArrayInputStream(finalTei.getBytes("utf-8"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Document doc = null;

        if (isAlreadyAdded(tei)) {
            return tei;
        }

        try {
            Element grobidTeiElement = null;
            if (grobidTei != null) {
                doc = docBuilder.parse(new InputSource(new ByteArrayInputStream(grobidTei.getBytes("utf-8"))));
                grobidTeiElement = (Element) doc.getDocumentElement();
                Attr attr = grobidTeiElement.getAttributeNode("xmlns");
                grobidTeiElement.removeAttributeNode(attr);
                grobidTeiElement.setAttribute("type", "main");
            }
            resultTei = addNodeToTei(tei, grobidTeiElement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // add random xml:id on textual elements

        return resultTei;
    }

    private static boolean isAlreadyAdded(Document tei) {
        NodeList teiElements = tei.getElementsByTagName("tei");
        Element teiElement = null;
        for (int i = teiElements.getLength() - 1; i >= 0; i--) {
            teiElement = (Element) teiElements.item(i);
            if (teiElement.hasAttribute("type") && teiElement.getAttribute("type").equals("main")) {
                return true;
            }
        }
        return false;
    }

    private static Element createMetadataTeiHeader(NodeList biblFull, Document doc) {
        Element teiHeader = doc.createElement("teiHeader");
        if (biblFull.getLength() == 0) {
            return teiHeader;
        }
        Node biblFullRoot = biblFull.item(0);
        for (int i = 0; i < biblFullRoot.getChildNodes().getLength(); i++) {
            Node localNode = doc.importNode(biblFullRoot.getChildNodes().item(i), true);
            teiHeader.appendChild(localNode);
        }
        return teiHeader;
    }

    /**
    * returns Document containing tei corpus with given metadata.
    */
    public static Document createTEICorpus(InputStream metadataTeiStream) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);
        Document metadataTei = null;
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();

            metadataTei = docBuilder.parse(metadataTeiStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
        NodeList metadataTeiHeader = halTeiExtractor.extractHalMetadata(metadataTei);
        Document doc = docBuilder.newDocument();
        Element teiCorpus = doc.createElement("teiCorpus");
        Element teiHeader = createMetadataTeiHeader(metadataTeiHeader, doc);
        teiCorpus.appendChild(teiHeader);
        teiCorpus.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0");
        doc.appendChild(teiCorpus);
        return doc;
    }

    private static Document addNodeToTei(Document doc, Node newNode) {
        if (newNode != null) {
            newNode = (Element) doc.importNode(newNode, true);
            Element teiCorpus = (Element) doc.getElementsByTagName("teiCorpus").item(0);
            teiCorpus.appendChild(newNode);
        }
        return doc;
    }
}
