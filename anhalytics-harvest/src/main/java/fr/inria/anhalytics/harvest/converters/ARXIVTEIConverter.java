package fr.inria.anhalytics.harvest.converters;

import fr.inria.anhalytics.commons.data.BiblioObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class ARXIVTEIConverter implements MetadataConverter {
    @Override
    public Element convertMetadataToTEIHeader(Document metadata, Document newTEIcorpus, BiblioObject biblio) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        Element teiHeader = null;
        try {
            teiHeader = (Element) xPath.compile("/TEI/teiHeader").evaluate(metadata, XPathConstants.NODE);
            teiHeader = (Element) newTEIcorpus.importNode(teiHeader, true);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return teiHeader;
    }
}
