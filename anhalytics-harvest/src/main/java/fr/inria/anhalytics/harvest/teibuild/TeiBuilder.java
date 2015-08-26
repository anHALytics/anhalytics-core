package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.w3c.dom.Attr;

public class TeiBuilder {

    public static String generateTeiCorpus(InputStream additionalTei, InputStream grobidTei, boolean update) throws ParserConfigurationException, IOException, XPathExpressionException {
        String teiString = null;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);

        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document docAdditionalTei = null;
        try {
            docAdditionalTei = docBuilder.parse(additionalTei);
        } catch (SAXException e) {
            e.printStackTrace();
        }
//System.out.println(Utilities.toString(docAdditionalTei));
        NodeList teiHeader = halTeiExtractor.getTeiHeader(docAdditionalTei);

        Document doc = null;
        try {
            doc = docBuilder.parse(grobidTei);

            updateGrobidTEI(doc);
            
            teiString = createTEICorpus(doc, teiHeader);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        teiString = Utilities.formatXMLString(teiString);
        return teiString;
    }

    private static void addAdditionalTeiHeader(NodeList biblFull, Node header, Document doc) {
        if (biblFull.getLength() == 0) {
            return;
        }
        Node biblFullRoot = biblFull.item(0);
        for (int i = 0; i < biblFullRoot.getChildNodes().getLength(); i++) {
            Node localNode = doc.importNode(biblFullRoot.getChildNodes().item(i), true);
            header.appendChild(localNode);
        }
    }

    private static String createTEICorpus(Document doc, NodeList biblFull) {        
        Element teiHeader = doc.createElement("teiHeader");
        Element tei = (Element) doc.getDocumentElement();
        Attr attr = tei.getAttributeNode("xmlns");
        tei.removeAttributeNode(attr);
        tei.setAttribute("type", "main");
        Element teiCorpus = doc.createElement("teiCorpus");
        addAdditionalTeiHeader(biblFull, teiHeader, doc);
        teiCorpus.appendChild(teiHeader);
        teiCorpus.appendChild(tei);
        
        teiCorpus.setAttributeNode(attr);
        
        doc.appendChild(teiCorpus);
        // add random xml:id on textual elements
        Utilities.generateIDs(doc);
        return Utilities.toString(doc);
    }

    /**
     * Updates the extracted TEI with the authors data.
     */
    private static void updateGrobidTEI(Document doc) {
        NodeList nl = doc.getElementsByTagName("teiHeader");
        Node n = nl.item(0);
        while (n.hasChildNodes()) {
            n.removeChild(n.getFirstChild());
        }
    }
}
