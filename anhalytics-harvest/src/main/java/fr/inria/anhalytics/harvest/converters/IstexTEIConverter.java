package fr.inria.anhalytics.harvest.converters;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.grobid.GrobidService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author azhar
 */
public class IstexTEIConverter implements MetadataConverter {

    @Override
    public Element convertMetadataToTEIHeader(Document metadata, Document newTEIcorpus) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        Element teiHeader = null;
        try {
            teiHeader = (Element) xPath.compile("/TEI/teiHeader").evaluate(metadata, XPathConstants.NODE);
            updatePublicationType(metadata);
            updateKeywords(metadata);
            NodeList affs = (NodeList) xPath.compile("/TEI/teiHeader/fileDesc/sourceDesc/biblStruct/analytic/author/affiliation").evaluate(metadata, XPathConstants.NODESET);
            parseAffiliationString(metadata, affs);
            teiHeader = (Element) newTEIcorpus.importNode(teiHeader, true);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return teiHeader;
    }

    private void updatePublicationType(Document metadata) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Element tei = (Element) xPath.compile("/TEI").evaluate(metadata, XPathConstants.NODE);

            Element textClass = (Element) xPath.compile("/TEI/teiHeader/profileDesc/textClass").evaluate(metadata, XPathConstants.NODE);
            Element classCode = metadata.createElement("classCode");
            classCode.setAttribute("scheme", "typology");
            classCode.setTextContent(tei.getAttribute("type"));
            if (textClass == null) {
                Element profileDesc = (Element) xPath.compile("/TEI/teiHeader/profileDesc").evaluate(metadata, XPathConstants.NODE);
                textClass = metadata.createElement("textClass");
                textClass.appendChild(classCode);
                profileDesc.appendChild(textClass);
            } else {
                textClass.appendChild(classCode);
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    private void updateKeywords(Document metadata) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Element existingKeywords = (Element) xPath.compile("/TEI/teiHeader/profileDesc/textClass/keywords").evaluate(metadata, XPathConstants.NODE);
            NodeList items = (NodeList) xPath.compile("/TEI/teiHeader/profileDesc/textClass/keywords/list/item/term").evaluate(metadata, XPathConstants.NODESET);
            Element textClass = (Element) xPath.compile("/TEI/teiHeader/profileDesc/textClass").evaluate(metadata, XPathConstants.NODE);
            Element keywords = metadata.createElement("keywords");
            //we suppose those are authors
            keywords.setAttribute("scheme", "author");
            for (int y = 0; y < items.getLength(); y++) {
                if (items.item(y).getNodeType() == Node.ELEMENT_NODE) {
                    keywords.appendChild(items.item(y));
                }
            }
            textClass.appendChild(keywords);
            if (existingKeywords != null) {
                existingKeywords.getParentNode().removeChild(existingKeywords);
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    private void parseAffiliationString(Document doc, NodeList affs) {
        Node aff = null;
        GrobidService gs = new GrobidService();
        for (int i = affs.getLength() - 1; i >= 0; i--) {
            aff = affs.item(i);
            if (aff.getNodeType() == Node.ELEMENT_NODE) {
                Element affElt = (Element) affs.item(i);
                String affiliationString = aff.getTextContent();
                affElt.setTextContent("");
                String grobidResponse = null;

                if (!affiliationString.isEmpty()) {
                    grobidResponse = gs.processAffiliation(affiliationString);
                    try {
                        // (HACK)Grobid may split affiliation string into two affiliation elements, which is considered not well-formed.
                        grobidResponse = "<wrap>" + grobidResponse + "</wrap>";
                        Element node = DocumentBuilderFactory
                                .newInstance()
                                .newDocumentBuilder()
                                .parse(new ByteArrayInputStream(grobidResponse.getBytes()))
                                .getDocumentElement();
                        if (node.getElementsByTagName("affiliation").getLength() > 0) {
                            for (int a = node.getElementsByTagName("affiliation").getLength() - 1; a >= 0; a--) {
                                if (node.getElementsByTagName("affiliation").item(a).getNodeType() == Node.ELEMENT_NODE) {
                                    Element affiliation = (Element) node.getElementsByTagName("affiliation").item(a);
                                    NodeList orgNames = affiliation.getElementsByTagName("orgName");
                                    NodeList addr = affiliation.getElementsByTagName("address");

                                    if (orgNames.getLength() > 1) {
                                        for (int k = orgNames.getLength() - 1; k >= 0; k--) {
                                            if (orgNames.item(k).getNodeType() == Node.ELEMENT_NODE) {
                                                Element orgNameElt = (Element) orgNames.item(k);
                                                Element org = doc.createElement("org");
                                                org.setAttribute("type", orgNameElt.getAttribute("type"));
                                                org.appendChild((Element) doc.importNode(orgNameElt, true));
                                                if (addr.getLength() > 0) {
                                                    Element desc = doc.createElement("desc");
                                                    Node newNode = addr.item(0).cloneNode(true);
                                                    desc.appendChild((Element) doc.importNode(newNode, true));
                                                    org.appendChild(desc);
                                                }
                                                affElt.appendChild(org);
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    } catch (ParserConfigurationException ex) {
                        Logger.getLogger(HalTEIConverter.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SAXException ex) {
                        Logger.getLogger(HalTEIConverter.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(HalTEIConverter.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }

        }
    }

}
