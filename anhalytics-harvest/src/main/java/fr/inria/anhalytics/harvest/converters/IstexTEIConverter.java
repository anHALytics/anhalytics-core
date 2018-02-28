package fr.inria.anhalytics.harvest.converters;

import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.harvest.grobid.GrobidService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
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
    public Element convertMetadataToTEIHeader(Document metadata, Document newTEIcorpus, BiblioObject biblio) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        Element teiHeader = null;
        try {
            teiHeader = (Element) xPath.compile("/TEI/teiHeader").evaluate(metadata, XPathConstants.NODE);
            updatePublicationType(metadata);
            updateKeywords(metadata);
            updatePublicationDate(metadata);
            NodeList affs = (NodeList) xPath.compile("/TEI/teiHeader/fileDesc/sourceDesc/biblStruct/analytic/author/affiliation").evaluate(metadata, XPathConstants.NODESET);
            parseAffiliationString(metadata, affs);

            addDomains(metadata, biblio);
            teiHeader = (Element) newTEIcorpus.importNode(teiHeader, true);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return teiHeader;
    }

    private void updatePublicationDate(Document metadata) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Element dateElt = (Element) xPath.compile("/TEI/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/imprint/date[@type=\"published\"]").evaluate(metadata, XPathConstants.NODE);

            String dateFormatted = dateElt.getAttribute("when");

            //and check again if the content is not ok (create a new function and reuse it)
            if (!dateFormatted.isEmpty()) {
                dateElt.setTextContent(dateFormatted);
                dateElt.setAttribute("when", dateFormatted);
            }

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
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

    /**
     * Takes the domains found from the api metadata and put them to the TEI.
     */
    private void addDomains(Document newTEICorpus, BiblioObject biblioObj) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            List<TreeMap<Integer, String>> domains_hierarchies = new ArrayList<TreeMap<Integer, String>>();
            TreeMap<Integer, String> domains_hierarchy = new TreeMap<Integer, String>();
            if(biblioObj.getDomains() != null) {
                for (int i = 0; i <= biblioObj.getDomains().size() - 1; i++) {
                    String domain = biblioObj.getDomains().get(i);

                    String[] domain_parts = domain.split(" - ");
                    int key = Integer.parseInt(domain_parts[0]);
                    String domainStr = domain_parts[1];

                    if (domains_hierarchy.containsKey(key)) {
                        //here we suppose beginning first key would be 1
                        if (key > 1) {
                            domains_hierarchies.add(domains_hierarchy);
                            domains_hierarchy = (TreeMap) domains_hierarchy.clone();
                            int j = domains_hierarchy.lastKey();
                            while (j >= key) {
                                domains_hierarchy.remove(j);
                                j--;
                            }
                            domains_hierarchy.put(key, domainStr);
                        } else if (key == 1) {
                            domains_hierarchies.add(domains_hierarchy);
                            domains_hierarchy = new TreeMap<Integer, String>();
                            domains_hierarchy.put(key, domainStr);
                        }
                    } else
                        domains_hierarchy.put(key, domainStr);

                    if (i == biblioObj.getDomains().size() - 1)
                        domains_hierarchies.add(domains_hierarchy);


                }

                if (domains_hierarchies.size() >= 1) {

                    Element profileDesc = (Element) xPath.compile("/TEI/teiHeader/profileDesc").evaluate(newTEICorpus, XPathConstants.NODE);
                    Element textClass = profileDesc.getElementsByTagName("textClass").item(0).getNodeType() == Node.ELEMENT_NODE ? (Element)profileDesc.getElementsByTagName("textClass").item(0):null;
                    if(textClass == null)
                        textClass = newTEICorpus.createElement("textClass");
                    for (TreeMap<Integer, String> domain : domains_hierarchies) {
                        String taxonomy = "";
                        Element classCode = newTEICorpus.createElement("classCode");
                        classCode.setAttribute("scheme", "domain");
                        for (int o = 1; o <= domain.size(); o++) {
                            if (o == 1)
                                taxonomy += domain.get(o);
                            else
                                taxonomy += "." + domain.get(o);
                        }
                        classCode.setAttribute("n", taxonomy);
                        classCode.setTextContent(taxonomy);
                        textClass.appendChild(classCode);
                    }
                    profileDesc.appendChild(textClass);
                }
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

}
