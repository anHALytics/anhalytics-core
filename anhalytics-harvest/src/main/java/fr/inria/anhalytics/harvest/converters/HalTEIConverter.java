package fr.inria.anhalytics.harvest.converters;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.grobid.MyGrobid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
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
        for (int i = orgs.getLength() - 1; i >= 0; i--) {
            org = orgs.item(i);
            if (org.getNodeType() == Node.ELEMENT_NODE) {
                Element orgElt = (Element) orgs.item(i);
                NodeList addressNodes = orgElt.getElementsByTagName("addrLine");
                String grobidResponse = null;
                if (addressNodes != null) {
                    Node addrLine = addressNodes.item(0);
                    if (addrLine != null) {
                        grobidResponse = MyGrobid.runGrobid(addrLine.getTextContent());
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

    private void updateOrgType(Element org, NodeList orgs) {

        NodeList relations = org.getElementsByTagName("relation");
        for (int j = relations.getLength() - 1; j >= 0; j--) {
            Node relationNode = relations.item(j);
            if (relationNode.getNodeType() == Node.ELEMENT_NODE) {
                Element relation = (Element) relationNode;
                if (relation.getAttribute("type").equals("direct")) {
                    String id = relation.getAttribute("active");
                    id = id.replace("#", "");
                    Node rel = Utilities.findNode(id, orgs);
                    Node newRel = rel.cloneNode(true);
                    updateOrgType((Element) newRel, orgs);

                    org.appendChild(newRel);
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
                        Node aff = Utilities.findNode(name, orgs);
                        if (aff != null) {
                            //person.removeChild(theNodes.item(y));
                            aff = aff.cloneNode(true);
                            Node localNode = (docAdditionalTei.importNode(aff, true));
                            // we need to rename this attribute because we cannot multiply the id attribute
                            // with the same value (XML doc becomes not well-formed)
                            Element orgElement = (Element) localNode;
                            updateOrgType(orgElement, orgs);
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

    public void fillPubDate(Document doc, Document teiCorpusDoc) throws XPathExpressionException {
        try {
            Element dateElt = (Element) xPath.compile("/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/imprint/date[@type=\"datePub\"]").evaluate(teiCorpusDoc, XPathConstants.NODE);
            if (dateElt == null) {
                dateElt = (Element) xPath.compile("/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/imprint/date[@type=\"dateDefended\"]").evaluate(teiCorpusDoc, XPathConstants.NODE);
            }
            if (dateElt != null) {
                dateElt.setTextContent(Utilities.completeDate(dateElt.getTextContent()));
            }
            if (dateElt == null || dateElt.getTextContent().contains("0000")) {
                String date = "";
                Element grobidDateElt = (Element) xPath.compile("/TEI/teiHeader/fileDesc/publicationStmt/date[@type=\"published\"]").evaluate(doc, XPathConstants.NODE);
                if (grobidDateElt != null) {
                    String grobidDate = grobidDateElt.getTextContent();
                    if (!grobidDate.contains("0000")) {
                        grobidDate = Utilities.completeDate(grobidDate);
                        date = grobidDate;
                    }
                } 
                if(date.isEmpty()) {
                    Node submitDate = (Node) xPath.compile("/teiCorpus/teiHeader/fileDesc/editionStmt/edition[@type=\"current\"]/date[@type=\"whenSubmitted\"]").evaluate(teiCorpusDoc, XPathConstants.NODE);
                    date = submitDate.getTextContent().split(" ")[0];
                }
                Element newPubDate = teiCorpusDoc.createElement("date");
                newPubDate.setAttribute("type", "datePub");
                newPubDate.setTextContent(date);
                Element eltMonogr = (Element) xPath.compile("/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr").evaluate(teiCorpusDoc, XPathConstants.NODE);
                Element eltImprint = (Element) eltMonogr.getElementsByTagName("imprint").item(0);
                eltImprint = (eltImprint == null) ? doc.createElement("imprint") : eltImprint;
                eltImprint.appendChild(newPubDate);
                eltMonogr.appendChild(eltImprint);
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }
}
