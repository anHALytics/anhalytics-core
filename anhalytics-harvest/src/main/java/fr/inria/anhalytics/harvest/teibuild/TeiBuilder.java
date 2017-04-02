package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.exceptions.SystemException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.converters.HalTEIConverter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Functions that builds TEICorpus. 1. Creates Tei corpus (we use this format to
 * append other extracted metadata from annexes, annotation standoffs..) 2.
 * Appends Extracted Grobid Tei to TEI Corpus
 *
 * @author Achraf
 */
public class TeiBuilder {

    private DocumentBuilder docBuilder;

    public TeiBuilder() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);

        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new SystemException("Cannot instantiate TeiBuilder", e);
        }

    }

    /**
     * returns new Document representing TEICorpus with harvested metadata in
     * TEI header.
     */
    public Document createTEICorpus(String metadata) {

        Document metadataDoc = null;
        HalTEIConverter htc = new HalTEIConverter();
        try {
            metadataDoc = docBuilder.parse(new InputSource(new ByteArrayInputStream(metadata.getBytes("utf-8"))));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Document newTEICorpus = docBuilder.newDocument();
        Element teiHeader = htc.convertMetadataToTEIHeader(metadataDoc, newTEICorpus);
        Element teiCorpus = newTEICorpus.createElement("teiCorpus");
        teiHeader.setAttribute("xml:id", "hal");
        teiCorpus.appendChild(teiHeader);
        teiCorpus.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0");
        newTEICorpus.appendChild(teiCorpus);
        htc.updatePublicationDate(teiHeader, newTEICorpus);
        Utilities.generateIDs(newTEICorpus);
        return newTEICorpus;
    }

    /**
     * appends fulltext grobid tei to existing TEICorpus.
     */
    public Document addGrobidTEIToTEICorpus(String teiCorpus, String grobidTei) {
        Document resultTei = null;
        Document teiCorpusDoc = null;
        HalTEIConverter htc = new HalTEIConverter();
        try {
            teiCorpusDoc = docBuilder.parse(new InputSource(new ByteArrayInputStream(teiCorpus.getBytes("utf-8"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Document doc = null;

        //Remove if already existing.
        NodeList elements = (NodeList) teiCorpusDoc.getElementsByTagName("TEI");
        if (elements != null) {

            Element tei = null;
            for (int i = elements.getLength() - 1; i >= 0; i--) {
                if (elements.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    tei = (Element) elements.item(i);
                    if (tei.getAttribute("xml:id").equals("grobid")) {
                        tei.getParentNode().removeChild(tei);
                    }
                }
            }
        }
        try {
            Element grobidTeiElement = null;
            if (grobidTei != null) {
                doc = docBuilder.parse(new InputSource(new ByteArrayInputStream(grobidTei.getBytes("utf-8"))));
                grobidTeiElement = (Element) doc.getDocumentElement();
                Attr attr = grobidTeiElement.getAttributeNode("xmlns");
                grobidTeiElement.removeAttributeNode(attr);
                grobidTeiElement.setAttribute("type", "main");
                grobidTeiElement.setAttribute("xml:id", "grobid");
                htc.fillAbstract(doc, teiCorpusDoc);
                htc.fillKeywords(doc, teiCorpusDoc);
                htc.fillPubDate(doc, teiCorpusDoc);
                htc.fillAuthors(doc, teiCorpusDoc);
            }
            resultTei = addNewElementToTEI(teiCorpusDoc, grobidTeiElement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // add random xml:id on textual elements

        return resultTei;
    }

    /**
     * Appends new element to TEICorpus.
     */
    private static Document addNewElementToTEI(Document doc, Node newNode) {

        if (newNode != null) {
            newNode = (Element) doc.importNode(newNode, true);
            Element teiCorpus = (Element) doc.getElementsByTagName("teiCorpus").item(0);
            teiCorpus.appendChild(newNode);
        }
        return doc;
    }
}
