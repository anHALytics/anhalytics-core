package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.converters.HalTEIConverter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.xml.sax.InputSource;

/**
 * Functions that builds TEICorpus. 
 * 1. Creates Tei corpus (we use this format to append other extracted metadata from annexes, annotation standoffs..) 
 * 2. Appends Extracted Grobid Tei to TEI Corpus
 *
 * @author Achraf
 */
public class TeiBuilder {
    
    /**
     * returns new Document representing TEICorpus with harvested metadata in
     * TEI header.
     */
    public static Document createTEICorpus(InputStream metadataStream) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        HalTEIConverter htc = new HalTEIConverter();//
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);
        Document metadata = null;
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            metadata = docBuilder.parse(metadataStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Document newTEICorpus = docBuilder.newDocument();
        Element teiHeader = htc.convertMetadataToTEIHeader(metadata, newTEICorpus);
        Element teiCorpus = newTEICorpus.createElement("teiCorpus");
        teiCorpus.appendChild(teiHeader);
        teiCorpus.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0");
        newTEICorpus.appendChild(teiCorpus);
        return newTEICorpus;
    }

    /**
     * appends fulltext grobid tei to existing TEICorpus.
     */
    public static Document addGrobidTEIToTEICorpus(String teiCorpus, String grobidTei) {
        Document resultTei = null;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);

        DocumentBuilder docBuilder = null;
        Document teiCorpusDoc = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            teiCorpusDoc = docBuilder.parse(new InputSource(new ByteArrayInputStream(teiCorpus.getBytes("utf-8"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Document doc = null;

        try {
            Element grobidTeiElement = null;
            if (grobidTei != null) {
                doc = docBuilder.parse(new InputSource(new ByteArrayInputStream(grobidTei.getBytes("utf-8"))));
                grobidTeiElement = (Element) doc.getDocumentElement();
                Attr attr = grobidTeiElement.getAttributeNode("xmlns");
                grobidTeiElement.removeAttributeNode(attr);
                grobidTeiElement.setAttribute("type", "main");
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
