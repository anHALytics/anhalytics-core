package fr.inria.anhalytics.harvest.converters;

import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.grobid.GrobidService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Function for converting metadata from Hal format to Standard Tei format close
 * to the one used for Grobid. https://github.com/kermitt2/Pub2TEIPrivate we
 * base our approach on the XSD here
 * https://raw.githubusercontent.com/CCSDForge/HAL/master/schema/tei.xsd
 *
 * @author achraf
 */
public class HalTEIConverter implements MetadataConverter {

    /**
     * Converts metadata to standard TEI format. 
     * Reorganizes some nodes especially authors/editors that are misplaced, puts them under biblFull node, 
     * parses the address using Grobid, 
     * updates the affiliation from the back 
     * Updates all Hal specific attribute(halTypology => typology)
     * Checks if there is the publication date and take other alternatives, conference date, submission date..
     * and returns the result( some data is redundant).
     */
    @Override
    public Element convertMetadataToTEIHeader(Document metadata, Document newTEICorpus, BiblioObject biblio) {

        XPath xPath = XPathFactory.newInstance().newXPath();
        /////////////// Hal specific : To be done as a harvesting post process before storing tei ////////////////////
        // remove ugly end-of-line in starting and ending text as it is
        // a problem for stand-off annotations
        //read an xml node using xpath
        Element teiHeader = null;
        try {

            metadata = transformMetadata(metadata);

            NodeList editors = (NodeList) xPath.compile("/TEI/text/body/listBibl/fileDesc/sourceDesc/biblStruct/analytic/editor").evaluate(metadata, XPathConstants.NODESET);
            NodeList authors = (NodeList) xPath.compile("/TEI/text/body/listBibl/fileDesc/sourceDesc/biblStruct/analytic/author").evaluate(metadata, XPathConstants.NODESET);
            NodeList orgs = (NodeList) xPath.compile("/TEI/text/back/listOrg/org").evaluate(metadata, XPathConstants.NODESET);
            parseOrgsAddress(metadata, orgs);
            updateAffiliations(authors, orgs, metadata);
            updateAffiliations(editors, orgs, metadata);
            Utilities.trimEOL(metadata.getDocumentElement(), metadata);
            NodeList stuffToTake = (NodeList) xPath.compile("/TEI/text/body/listBibl").evaluate(metadata, XPathConstants.NODESET);
            updatePublicationType(metadata);
            updateDomainsType(metadata);
            updatePublicationDate(metadata);
            updateAbstract(metadata);
            teiHeader = createMetadataTEIHeader(stuffToTake, newTEICorpus);

        } catch (XPathExpressionException e) {
            e.printStackTrace();

        }
        return teiHeader;
    }

    /**
    * For generalisation purpose we use typology scheme instead of halTypology 
    * and we concatenate the abbreviation with the full typology (must be handled from frontend..).
    */
    private void updatePublicationType(Document newTEICorpus) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Element pubType = (Element) xPath.compile("/TEI/text/body/listBibl/profileDesc/textClass/classCode[@scheme=\"halTypology\"]").evaluate(newTEICorpus, XPathConstants.NODE);
            Element textClass = (Element) xPath.compile("/TEI/text/body/listBibl/profileDesc/textClass").evaluate(newTEICorpus, XPathConstants.NODE);
            if (pubType != null) {
                pubType.setAttribute("scheme", "typology");
                pubType.setTextContent(pubType.getAttribute("n") + "_" + pubType.getTextContent());//for generality reasons, the abbreviations checked and would be parsed from the demo.
                //classCode.removeAttribute("n");
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    /**
    * For generalisation purpose we use typology scheme instead of halDomain
    * and we concatenate the abbreviation with the full typology (must be handled from frontend..).
    */
    private void updateDomainsType(Document newTEICorpus) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList pubDomains = (NodeList) xPath.compile("/TEI/text/body/listBibl/profileDesc/textClass/classCode[@scheme=\"halDomain\"]").evaluate(newTEICorpus, XPathConstants.NODESET);
            if (pubDomains != null) {
                for(int i = 0; i <= pubDomains.getLength() - 1; i++) {
                    if(pubDomains.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element classCode = (Element)pubDomains.item(i);
                        classCode.setAttribute("scheme", "domain");
                        classCode.setTextContent(classCode.getAttribute("n") + "_" + classCode.getTextContent());
                        //classCode.removeAttribute("n");
                    }
                }
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    /**
    * Checks the publication date and consider other ones if there is none.
    */
    private void updatePublicationDate(Document newTEICorpus) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Element dateElt = (Element) xPath.compile("/TEI/text/body/listBibl/fileDesc/sourceDesc/biblStruct/monogr/imprint/date[@type=\"datePub\"]").evaluate(newTEICorpus, XPathConstants.NODE);

            Element submissionDateElt = (Element) xPath.compile("/TEI/text/body/listBibl/fileDesc/editionStmt/edition[@type=\"current\"]/date[@type=\"whenSubmitted\"]").evaluate(newTEICorpus, XPathConstants.NODE);

            Element defendingElt = (Element) xPath.compile("/TEI/text/body/listBibl/fileDesc/sourceDesc/biblStruct/monogr/imprint/date[@type=\"dateDefended\"]").evaluate(newTEICorpus, XPathConstants.NODE);

            Element startDateConferenceElt = (Element) xPath.compile("/TEI/text/body/listBibl/fileDesc/sourceDesc/biblStruct/monogr/meeting/date[@type=\"start\"]").evaluate(newTEICorpus, XPathConstants.NODE);

            String pubDate = "";
            if (dateElt != null) {
                pubDate = Utilities.completeDate(dateElt.getTextContent());
            }
            //If date is empty we check and pick first submission date, start conference date, defending date.
            if (pubDate.isEmpty()) {
                String date = "";

                if (submissionDateElt != null) {
                    date = submissionDateElt.getTextContent().split(" ")[0];
                }

                if (startDateConferenceElt != null) {
                    String confDate = Utilities.completeDate(startDateConferenceElt.getTextContent());
                    if (!confDate.isEmpty()) {
                        date = confDate;
                    }
                }

                if (defendingElt != null) {
                    String defendedDate = Utilities.completeDate(defendingElt.getTextContent());
                    if (!defendedDate.isEmpty()) {
                        date = defendedDate;
                    }
                }
                pubDate = date;
            }

            if (dateElt == null) {
                Element newPubDate = newTEICorpus.createElement("date");
                newPubDate.setAttribute("when", pubDate);
                newPubDate.setAttribute("type", "published");
                newPubDate.setTextContent(pubDate);

                Element eltMonogr = (Element) xPath.compile("/TEI/text/body/listBibl/fileDesc/sourceDesc/biblStruct/monogr").evaluate(newTEICorpus, XPathConstants.NODE);
                Element eltImprint = (Element) eltMonogr.getElementsByTagName("imprint").item(0);
                eltImprint = (eltImprint == null) ? newTEICorpus.createElement("imprint") : eltImprint;
                eltImprint.appendChild(newPubDate);
                eltMonogr.appendChild(eltImprint);
            } else {
                dateElt.setAttribute("when", pubDate);
                dateElt.setAttribute("type", "published");
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    /**
    * Puts all elements found under biblFull into a teiHeader.
    */
    private Element createMetadataTEIHeader(NodeList stuffToTake, Document doc) {
        Element teiHeader = doc.createElement("teiHeader");
        if (stuffToTake.getLength() == 0) {
            return teiHeader;
        }
        Node biblFullRoot = stuffToTake.item(0);
        for (int i = 0; i < biblFullRoot.getChildNodes().getLength(); i++) {
            Node localNode = doc.importNode(biblFullRoot.getChildNodes().item(i), true);
            teiHeader.appendChild(localNode);

        }
        return teiHeader;
    }

    /**
    * parses the organisation address using Grobid.
    */
    private void parseOrgsAddress(Document doc, NodeList orgs) {
        Node org = null;
        GrobidService gs = new GrobidService();
        for (int i = orgs.getLength() - 1; i >= 0; i--) {
            org = orgs.item(i);
            if (org.getNodeType() == Node.ELEMENT_NODE) {
                Element orgElt = (Element) orgs.item(i);
                NodeList addressNodes = orgElt.getElementsByTagName("addrLine");

                NodeList orgNameNodes = orgElt.getElementsByTagName("orgName");
                String orgNameStr = "";
                Node orgNameNode = null;
                for (int y = orgNameNodes.getLength() - 1; y >= 0; y--) {
                    orgNameNode = orgNameNodes.item(y);
                    if (orgNameNode.getNodeType() == Node.ELEMENT_NODE) {
                        orgNameStr += !orgNameStr.isEmpty() ? " "+orgNameNode.getTextContent():orgNameNode.getTextContent();
                    }
                }

                String grobidResponse = null;
                if (addressNodes != null) {
                    Node addrLine = addressNodes.item(0);
                    if (addrLine != null && addrLine.getTextContent().trim().length() > 0) {
                        grobidResponse = gs.processAffiliation(orgNameStr+" "+addrLine.getTextContent());
                        try {
                            Element node = DocumentBuilderFactory
                                    .newInstance()
                                    .newDocumentBuilder()
                                    .parse(new ByteArrayInputStream(grobidResponse.getBytes()))
                                    .getDocumentElement();

                            String addrLineString = "";
                            // not needed since HAL provides already fine orgNames
//                            NodeList line1 = node.getElementsByTagName("orgName");
//                            for (int z = line1.getLength() - 1; z >= 0; z--) {
//                                if (line1.item(z).getNodeType() == Node.ELEMENT_NODE) {
//                                    Node localNode = (doc.importNode(line1.item(z), true));
//                                    orgElt.appendChild(localNode);
//                                }
//                            }
                            NodeList line2 = node.getElementsByTagName("addrLine");
                            for (int y = line2.getLength() - 1; y >= 0; y--) {
                                addrLineString += !addrLineString.isEmpty() ? " "+line2.item(y).getTextContent():line2.item(y).getTextContent();
                            }
                            addrLine.setTextContent(addrLineString);

                            NodeList address = node.getElementsByTagName("address");
                            for (int n = address.getLength() - 1; n >= 0; n--) {
                                NodeList addressChilds = address.item(n).getChildNodes();
                                for (int j = addressChilds.getLength() - 1; j >= 0; j--) {

                                    if (addressChilds.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                        Element e = (Element) (addressChilds.item(j));
                                        if (!e.getTagName().equals("addrLine")) {
                                            Node localNode = (doc.importNode(e, true));
                                            orgElt.getElementsByTagName("address").item(0).appendChild(localNode);
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

    /**
     * Remove duplicated and uneeded parts and update.
     */
    private Document transformMetadata(Document metadata) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        Node title = (Node) xPath.compile("/TEI/text/body/listBibl/biblFull/sourceDesc/biblStruct/analytic/title").evaluate(metadata, XPathConstants.NODE);
        if (title != null) {
            title.getParentNode().removeChild(title);
        }

        NodeList authorsDuplicate = (NodeList) xPath.compile("/TEI/text/body/listBibl/biblFull/titleStmt/author").evaluate(metadata, XPathConstants.NODESET);
        for (int j = authorsDuplicate.getLength() - 1; j >= 0; j--) {
            authorsDuplicate.item(j).getParentNode().removeChild(authorsDuplicate.item(j));
        }
        NodeList editorsDuplicate = (NodeList) xPath.compile("/TEI/text/body/listBibl/biblFull/titleStmt/editor").evaluate(metadata, XPathConstants.NODESET);
        for (int j = editorsDuplicate.getLength() - 1; j >= 0; j--) {
            editorsDuplicate.item(j).getParentNode().removeChild(editorsDuplicate.item(j));
        }
        Node publicationStmt = (Node) xPath.compile("/TEI/text/body/listBibl/biblFull/publicationStmt").evaluate(metadata, XPathConstants.NODE);
        publicationStmt.getParentNode().removeChild(publicationStmt);
        //Node seriesStmt = (Node) xPath.compile("/TEI/text/body/listBibl/biblFull/seriesStmt").evaluate(docAdditionalTei, XPathConstants.NODE);
        //seriesStmt.getParentNode().removeChild(seriesStmt);
        Node notesStmt = (Node) xPath.compile("/TEI/text/body/listBibl/biblFull/notesStmt").evaluate(metadata, XPathConstants.NODE);
        notesStmt.getParentNode().removeChild(notesStmt);

        Node profileDesc = (Node) xPath.compile("/TEI/text/body/listBibl/biblFull/profileDesc").evaluate(metadata, XPathConstants.NODE);
        profileDesc.getParentNode().removeChild(profileDesc);
        Node listBibl = (Node) xPath.compile("/TEI/text/body/listBibl").evaluate(metadata, XPathConstants.NODE);
        listBibl.appendChild(profileDesc);
        Node biblFull = (Node) xPath.compile("/TEI/text/body/listBibl/biblFull").evaluate(metadata, XPathConstants.NODE);
        metadata.renameNode(biblFull, biblFull.getNamespaceURI(), "fileDesc");
        return metadata;
    }

    /**
    * Moves all relations under author element.
     */
    private void moveOrgToAffiliation(Element aff, Element org, NodeList orgs) {
        NodeList relations = org.getElementsByTagName("relation");
        for (int j = relations.getLength() - 1; j >= 0; j--) {
            Node relationNode = relations.item(j);
            if (relationNode.getNodeType() == Node.ELEMENT_NODE) {
                Element relation = (Element) relationNode;
                if (relation.getAttribute("type").equals("direct")) {
                    String id = relation.getAttribute("active");
                    id = id.replace("#", "");
                    Node rel = Utilities.findNode("xml:id", id, orgs);
                    if (rel != null) {
                        Node newRel = rel.cloneNode(true);
                        moveOrgToAffiliation(aff, (Element) newRel, orgs);
                        aff.appendChild(newRel);
                    }
                }
            }
        }
    }

    /**
    * Selects affiliation from back of the TEI and attach it to the author along with associated relations.
     */
    private void updateAffiliations(NodeList persons, NodeList orgs, Document docAdditionalTei) {
        Element person = null;
        NodeList theNodes = null;
        for (int i = 0; i < persons.getLength(); i++) {
            if (persons.item(i).getNodeType() == Node.ELEMENT_NODE) {
                person = (Element) persons.item(i);
                theNodes = person.getChildNodes();
                for (int y = 0; y < theNodes.getLength(); y++) {
                    if (theNodes.item(y).getNodeType() == Node.ELEMENT_NODE) {
                        Element e = (Element) (theNodes.item(y));
                        if (e.getTagName().equals("affiliation")) {
                            String name = e.getAttribute("ref").replace("#", "");
                            Node aff = Utilities.findNode("xml:id", name, orgs);
                            if (aff != null) {
                                //person.removeChild(theNodes.item(y));
                                aff = aff.cloneNode(true);
                                Node localNode = (docAdditionalTei.importNode(aff, true));
                                // we need to rename this attribute because we cannot multiply the id attribute
                                // with the same value (XML doc becomes not well-formed)
                                Element orgElement = (Element) localNode;
                                moveOrgToAffiliation(e, orgElement, orgs);
                                e.removeAttribute("ref");
                                e.appendChild(localNode);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * In HAL, Abstract text is wrapped into abstract element while it should be under organised into paragraphs.
     */
    private void updateAbstract(Document newTEICorpus) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Element abstractElt = (Element) xPath.compile("/TEI/text/body/listBibl/profileDesc/abstract").evaluate(newTEICorpus, XPathConstants.NODE);
            if (abstractElt != null) {
                Element p = newTEICorpus.createElement("p");
                p.setTextContent(abstractElt.getTextContent());
                abstractElt.setTextContent("");
                abstractElt.appendChild(p);
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }
}
