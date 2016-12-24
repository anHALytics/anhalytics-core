package fr.inria.anhalytics.harvest.converters;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.grobid.GrobidService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
 * Function for converting metadata from Hal stored to Standard Tei format close
 * to the one used for Grobid. https://github.com/kermitt2/Pub2TEIPrivate we
 * base our approach on the XSD here
 * https://raw.githubusercontent.com/CCSDForge/HAL/master/schema/tei.xsd
 *
 * @author achraf
 */
public class HalTEIConverter implements MetadataConverter {

    private static XPath xPath = XPathFactory.newInstance().newXPath();

    /**
     * Converts metadata to standard TEI format. Reorganizes some nodes
     * especially authors/editors that are misplaced, puts them under biblFull
     * node and returns the result( some data is redundant).
     *
     * @param docAdditionalTei
     * @return
     */
    public Element convertMetadataToTEIHeader(Document metadata, Document newTEICorpus) {
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
            teiHeader = createMetadataTEIHeader(stuffToTake, newTEICorpus);
        } catch (XPathExpressionException e) {
            e.printStackTrace();

        }
        return teiHeader;
    }

    public void updatePublicationDate(Element teiHeader, Document newTEICorpus) {
        try {
            Element dateElt = (Element) xPath.compile("/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/imprint/date[@type=\"datePub\"]").evaluate(newTEICorpus, XPathConstants.NODE);

            Element submissionDateElt = (Element) xPath.compile("/teiCorpus/teiHeader/fileDesc/editionStmt/edition[@type=\"current\"]/date[@type=\"whenSubmitted\"]").evaluate(newTEICorpus, XPathConstants.NODE);
            
            Element defendingElt = (Element) xPath.compile("/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/imprint/date[@type=\"dateDefended\"]").evaluate(newTEICorpus, XPathConstants.NODE);

            Element startDateConferenceElt = (Element) xPath.compile("/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/meeting/date[@type=\"start\"]").evaluate(newTEICorpus, XPathConstants.NODE);

            String pubDate = "";
            if (dateElt != null) {
                pubDate = Utilities.completeDate(dateElt.getTextContent());
            }
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
                newPubDate.setAttribute("type", "datePub");
                newPubDate.setTextContent(pubDate);
                
                Element eltMonogr = (Element) xPath.compile("/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr").evaluate(newTEICorpus, XPathConstants.NODE);
                Element eltImprint = (Element) eltMonogr.getElementsByTagName("imprint").item(0);
                eltImprint = (eltImprint == null) ? newTEICorpus.createElement("imprint") : eltImprint;
                eltImprint.appendChild(newPubDate);
                eltMonogr.appendChild(eltImprint);
            } else {
                dateElt.setAttribute("when", pubDate);
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

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

    private void setPublicationDate(Document doc) {

    }

    private void parseOrgsAddress(Document doc, NodeList orgs) {
        Node org = null;
        GrobidService gs = new GrobidService();
        for (int i = orgs.getLength() - 1; i >= 0; i--) {
            org = orgs.item(i);
            if (org.getNodeType() == Node.ELEMENT_NODE) {
                Element orgElt = (Element) orgs.item(i);
                NodeList addressNodes = orgElt.getElementsByTagName("addrLine");
                String grobidResponse = null;
                if (addressNodes != null) {
                    Node addrLine = addressNodes.item(0);
                    if (addrLine != null && addrLine.getTextContent().trim().length() > 0) {
                        grobidResponse = gs.processAffiliation(addrLine.getTextContent());
                        try {
                            Element node = DocumentBuilderFactory
                                    .newInstance()
                                    .newDocumentBuilder()
                                    .parse(new ByteArrayInputStream(grobidResponse.getBytes()))
                                    .getDocumentElement();
                            NodeList line1 = node.getElementsByTagName("orgName");
                            String addrLineString = "";
                            for (int z = line1.getLength() - 1; z >= 0; z--) {
                                addrLineString += line1.item(z).getTextContent();
                            }
                            NodeList line2 = node.getElementsByTagName("addrLine");
                            for (int y = line2.getLength() - 1; y >= 0; y--) {
                                addrLineString += line2.item(y).getTextContent();
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
     * Moves metadata under biblFull.
     *
     * @param docAdditionalTei
     * @param entities
     */
    private void correctDataLocation(Document docAdditionalTei, NodeList entities) throws XPathExpressionException {
        Node person = null;
        for (int i = entities.getLength() - 1; i >= 0; i--) {
            person = entities.item(i);

            person.getParentNode().removeChild(person);

            NodeList biblStruct = (NodeList) xPath.compile("/TEI/text/body/listBibl/biblFull/sourceDesc/biblStruct").evaluate(docAdditionalTei, XPathConstants.NODESET);
            biblStruct.item(0).insertBefore(person, biblStruct.item(0).getFirstChild());
        }

    }

    /**
     * Remove duplicated parts and update.
     */
    private Document transformMetadata(Document metadata) throws XPathExpressionException {
        Node title = (Node) xPath.compile("/TEI/text/body/listBibl/biblFull/sourceDesc/biblStruct/analytic/title").evaluate(metadata, XPathConstants.NODE);
        title.getParentNode().removeChild(title);

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

    private void moveOrgToAffiliation(Element aff, Element org, NodeList orgs) {

        NodeList relations = org.getElementsByTagName("relation");
        for (int j = relations.getLength() - 1; j >= 0; j--) {
            Node relationNode = relations.item(j);
            if (relationNode.getNodeType() == Node.ELEMENT_NODE) {
                Element relation = (Element) relationNode;
                if (relation.getAttribute("type").equals("direct")) {
                    String id = relation.getAttribute("active");
                    id = id.replace("#", "");
                    Node rel = Utilities.findNode("xml:id",id, orgs);
                    Node newRel = rel.cloneNode(true);
                    moveOrgToAffiliation(aff, (Element) newRel, orgs);
                    aff.appendChild(newRel);
                }
            }
        }
    }

    private void updateAffiliations(NodeList persons, NodeList orgs, Document docAdditionalTei) {
        Node person = null;
        NodeList theNodes = null;
        for (int i = 0; i < persons.getLength(); i++) {
            person = persons.item(i);
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

    public void fillAbstract(Document doc, Document teiCorpusDoc) throws XPathExpressionException {
        Element profileDescElt = (Element) xPath.compile("/teiCorpus/teiHeader/profileDesc").evaluate(teiCorpusDoc, XPathConstants.NODE);
        NodeList profileDescChilds = profileDescElt.getElementsByTagName("abstract");
        Element existingAbstractElt = null;
        if (profileDescChilds.getLength() > 0) {
            existingAbstractElt = (Element) profileDescChilds.item(0);
        }
        if (existingAbstractElt == null || existingAbstractElt.getTextContent().isEmpty()) {
            Element abstractElt = (Element) xPath.compile("/TEI/teiHeader/profileDesc/abstract/p").evaluate(doc, XPathConstants.NODE);
            if (abstractElt != null && !abstractElt.getTextContent().isEmpty()) {
                if (existingAbstractElt == null) {
                    existingAbstractElt = teiCorpusDoc.createElement("abstract");
                    profileDescElt.appendChild(existingAbstractElt);
                }
                existingAbstractElt.setTextContent(abstractElt.getTextContent());
            }
        }
    }

    public void fillKeywords(Document doc, Document teiCorpusDoc) throws XPathExpressionException {
        Element profileDescElt = (Element) xPath.compile("/teiCorpus/teiHeader/profileDesc").evaluate(teiCorpusDoc, XPathConstants.NODE);
        NodeList existingKeywordsNode = profileDescElt.getElementsByTagName("term");
        if (existingKeywordsNode.getLength() == 0) {
            Element textClassElt = (Element) xPath.compile("/TEI/teiHeader/profileDesc/textClass").evaluate(doc, XPathConstants.NODE);
            if (textClassElt != null) {
                NodeList termsNodes = textClassElt.getElementsByTagName("term");
                if (termsNodes.getLength() > 0) {
                    Element keywordsElt = (Element) textClassElt.getElementsByTagName("keywords").item(0);
                    keywordsElt = (Element) keywordsElt.cloneNode(true);
                    keywordsElt.setAttribute("scheme", "author");
                    Node keywordsLocalNode = (teiCorpusDoc.importNode(keywordsElt, true));

                    // weird minOccurs in the xsd for textClass is 0
                    if (profileDescElt.getElementsByTagName("textClass").getLength() > 0) {
                        Element existingTextClassElt = (Element) profileDescElt.getElementsByTagName("textClass").item(0);
                        existingTextClassElt.appendChild(keywordsLocalNode);
                    } else {
                        Element newTextClassElt = teiCorpusDoc.createElement("textClass");
                        newTextClassElt.appendChild(keywordsLocalNode);
                        profileDescElt.appendChild(newTextClassElt);
                    }
                }
            }
        }
    }

    /*
    We consider that grobid is accurate.
     */
    public void fillPubDate(Document doc, Document teiCorpusDoc) throws XPathExpressionException {
        try {
            Element dateElt = (Element) xPath.compile("/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/imprint/date[@type=\"datePub\"]").evaluate(teiCorpusDoc, XPathConstants.NODE);
            Element grobidDateElt = (Element) xPath.compile("/TEI/teiHeader/fileDesc/publicationStmt/date[@type=\"published\"]").evaluate(doc, XPathConstants.NODE);

            String dateRaw = "";
            String dateFormatted = dateElt.getAttribute("when");

            //and check again if the content is not ok (create a new function and reuse it)
            if (dateFormatted.isEmpty()) {
                if (grobidDateElt != null) {
                    dateRaw = grobidDateElt.getAttribute("when");
                    if (!dateRaw.isEmpty()) {
                        dateFormatted = Utilities.completeDate(dateRaw);
                    }
                }

                dateElt.setTextContent(dateFormatted);
                dateElt.setAttribute("when", dateFormatted);
            }

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    public void fillAuthors(Document grobidDoc, Document teiCorpusDoc) throws XPathExpressionException {

        // email & ptr to be done
        Element teiHeader = (Element) xPath.compile("/teiCorpus/teiHeader").evaluate(teiCorpusDoc, XPathConstants.NODE);
        NodeList authorsFromfulltextTeiHeader = (NodeList) xPath.compile("/TEI/teiHeader/fileDesc/sourceDesc/biblStruct/analytic/author").evaluate(grobidDoc, XPathConstants.NODESET);
        NodeList authors = teiHeader.getElementsByTagName("author");
        Element person = null;
        for (int i = authors.getLength() - 1; i >= 0; i--) {
            if (authors.item(i).getNodeType() == Node.ELEMENT_NODE) {
                person = (Element) authors.item(i);
                if (person.getElementsByTagName("affiliation").getLength() == 0) {
                    String fullname = "";
                    Element persName = (Element) person.getElementsByTagName("persName").item(0);
                    if (persName != null) {
                        NodeList nodes = persName.getChildNodes();
                        String forename = "", surname = "";
                        for (int z = nodes.getLength() - 1; z >= 0; z--) {
                            if (nodes.item(z).getNodeName().equals("forename")) {
                                forename = nodes.item(z).getTextContent();
                            } else if (nodes.item(z).getNodeName().equals("surname")) {
                                surname = nodes.item(z).getTextContent();
                            }
                        }
                        fullname = forename;
                        if (!surname.isEmpty()) {
                            fullname += " " + surname;
                        }
                    }
                    NodeList affs = null;
                    Element authorElt = Utilities.matchAuthor(fullname, authorsFromfulltextTeiHeader);
                    if (authorElt != null) {
                        affs = authorElt.getElementsByTagName("affiliation");
                        if (affs != null) {
                            Element affiliation = null;
                            Element address = null;
                            Element desc = null;
                            NodeList orgnames = null;
                            for (int y = affs.getLength() - 1; y >= 0; y--) {
                                if (affs.item(y).getNodeType() == Node.ELEMENT_NODE) {
                                    affiliation = (Element) affs.item(y);
                                    address = (Element) authorElt.getElementsByTagName("address").item(0);
                                    if (address != null) {
                                        address = (Element) (teiCorpusDoc.importNode(address.cloneNode(true), true));
                                        desc = teiCorpusDoc.createElement("desc");
                                        desc.appendChild(address);
                                    }
                                    orgnames = authorElt.getElementsByTagName("orgName");
                                    List<Element> labOrgnames = new ArrayList<Element>();
                                    List<Element> deptOrgnames = new ArrayList<Element>();
                                    List<Element> instOrgnames = new ArrayList<Element>();
                                    for (int k = orgnames.getLength() - 1; k >= 0; k--) {
                                        if (orgnames.item(k).getNodeType() == Node.ELEMENT_NODE) {
                                            if (((Element) orgnames.item(k)).getAttribute("type").equals("laboratory")) {
                                                labOrgnames.add((Element) orgnames.item(k).cloneNode(true));
                                            } else if (((Element) orgnames.item(k)).getAttribute("type").equals("department")) {
                                                deptOrgnames.add((Element) orgnames.item(k).cloneNode(true));
                                            } else if (((Element) orgnames.item(k)).getAttribute("type").equals("institution")) {
                                                instOrgnames.add((Element) orgnames.item(k).cloneNode(true));
                                            }
                                        }
                                    }

//                                    List<Element> labOrgs = new ArrayList<Element>();
//                                    List<Element> deptOrgs = new ArrayList<Element>();
//                                    List<Element> instOrgs = new ArrayList<Element>();
                                    if (labOrgnames.size() > 0) {
                                        Element aff = null;
                                        Element org = null;
                                        for (Element labOrgname : labOrgnames) {
                                            aff = teiCorpusDoc.createElement("affiliation");
                                            aff.setAttribute("source", "#grobid");
                                            org = teiCorpusDoc.createElement("org");
                                            org.setAttribute("type", "laboratory");
                                            org.appendChild(teiCorpusDoc.importNode(labOrgname, true));
                                            if (desc != null) {
                                                org.appendChild((Element) desc.cloneNode(true));
                                            }
                                            aff.appendChild(org);

                                            person.appendChild(aff);
//                                            labOrgs.add(aff);
                                        }
                                    }
                                    if (deptOrgnames.size() > 0) {
                                        Element aff = null;
                                        Element org = null;
                                        for (Element deptOrgname : deptOrgnames) {
                                            aff = teiCorpusDoc.createElement("affiliation");
                                            aff.setAttribute("source", "#grobid");
                                            org = teiCorpusDoc.createElement("org");
                                            org.setAttribute("type", "department");
                                            org.appendChild(teiCorpusDoc.importNode(deptOrgname, true));
                                            if (desc != null) {
                                                org.appendChild((Element) desc.cloneNode(true));
                                            }
                                            aff.appendChild(org);
                                            person.appendChild(aff);
//                                            deptOrgs.add(aff);
                                        }
                                    }
                                    if (instOrgnames.size() > 0) {
                                        Element aff = null;
                                        Element org = null;
                                        for (Element instOrgname : instOrgnames) {
                                            aff = teiCorpusDoc.createElement("affiliation");
                                            aff.setAttribute("source", "#grobid");
                                            org = teiCorpusDoc.createElement("org");
                                            org.setAttribute("type", "institution");
                                            org.appendChild(teiCorpusDoc.importNode(instOrgname, true));
                                            if (desc != null) {
                                                org.appendChild((Element) desc.cloneNode(true));
                                            }
                                            aff.appendChild(org);
                                            person.appendChild(aff);
//                                            instOrgs.add(aff);
                                        }
                                    }

                                }
                            }
                        }
                    }
                }

            }

        }
    }
}
