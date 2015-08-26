package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.utilities.Utilities;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Hal specific tei extraction and update.
 *
 * @author achraf
 */
public class halTeiExtractor {
    
    private static XPath xPath = XPathFactory.newInstance().newXPath();
    
    /**
     * Reorganizes some nodes especially authors/editors that are 'missplaced' and returns the biblFull to avoid redundancy. 
     * @param docAdditionalTei
     * @return 
     */
    // TO BE RENAMED.
    public static NodeList getTeiHeader(Document docAdditionalTei) throws XPathExpressionException {
        /////////////// Hal specific : To be done as a harvesting post process before storing tei ////////////////////
        // remove ugly end-of-line in starting and ending text as it is
        // a problem for stand-off annotations
        //read an xml node using xpath
        NodeList editors = (NodeList) xPath.compile("/TEI/text/body/listBibl/biblFull/titleStmt/editor").evaluate(docAdditionalTei, XPathConstants.NODESET);
        NodeList authors = (NodeList) xPath.compile("/TEI/text/body/listBibl/biblFull/titleStmt/author").evaluate(docAdditionalTei, XPathConstants.NODESET);
        NodeList orgs = (NodeList) xPath.compile("/TEI/text/back/listOrg/org").evaluate(docAdditionalTei, XPathConstants.NODESET);
       
        removeConsolidatedData(docAdditionalTei, authors);
        updateAffiliations(authors, orgs, docAdditionalTei);        
        removeConsolidatedData(docAdditionalTei, editors);
        updateAffiliations(editors, orgs, docAdditionalTei);
        docAdditionalTei = removeUnneededParts(docAdditionalTei); 
        Utilities.trimEOL(docAdditionalTei.getDocumentElement(), docAdditionalTei);
        return (NodeList) xPath.compile("/TEI/text/body/listBibl/biblFull").evaluate(docAdditionalTei, XPathConstants.NODESET);
    }
    
    /**
     * WIP...
     * @param docAdditionalTei
     * @param entities 
     */
    private static void removeConsolidatedData(Document docAdditionalTei, NodeList entities) throws XPathExpressionException{
        Node person = null;
        for (int i = entities.getLength() - 1; i >= 0; i--) {
            person = entities.item(i);
            /*
            theNodes = person.getChildNodes();
            for (int y = 0; y < theNodes.getLength(); y++) {
                Node node = theNodes.item(y);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if(!node.getNodeName().equals("idno"))
                        person.removeChild(node);
                }
            }
                    */
            person.getParentNode().removeChild(person);

            NodeList biblStruct = (NodeList) xPath.compile("/TEI/text/body/listBibl/biblFull/sourceDesc/biblStruct").evaluate(docAdditionalTei, XPathConstants.NODESET);
            biblStruct.item(0).insertBefore(person, biblStruct.item(0).getFirstChild());
        }
    
    }
    
    private static Document removeUnneededParts(Document docAdditionalTei) throws XPathExpressionException{
        Node analytic = (Node) xPath.compile("/TEI/text/body/listBibl/biblFull/sourceDesc/biblStruct/analytic").evaluate(docAdditionalTei, XPathConstants.NODE);
        analytic.getParentNode().removeChild(analytic);
        Node editionStmt = (Node) xPath.compile("/TEI/text/body/listBibl/biblFull/editionStmt").evaluate(docAdditionalTei, XPathConstants.NODE);
        editionStmt.getParentNode().removeChild(editionStmt);
        Node publicationStmt = (Node) xPath.compile("/TEI/text/body/listBibl/biblFull/publicationStmt").evaluate(docAdditionalTei, XPathConstants.NODE);
        publicationStmt.getParentNode().removeChild(publicationStmt);
        Node seriesStmt = (Node) xPath.compile("/TEI/text/body/listBibl/biblFull/seriesStmt").evaluate(docAdditionalTei, XPathConstants.NODE);
        seriesStmt.getParentNode().removeChild(seriesStmt);
        Node notesStmt = (Node) xPath.compile("/TEI/text/body/listBibl/biblFull/notesStmt").evaluate(docAdditionalTei, XPathConstants.NODE);
        notesStmt.getParentNode().removeChild(notesStmt);
        return docAdditionalTei;
    }

    private static void updateAffiliations(NodeList persons, NodeList orgs, Document docAdditionalTei) {
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
                            Node localNode = docAdditionalTei.importNode(aff, true);
                            // we need to rename this attribute because we cannot multiply the id attribute
                            // with the same value (XML doc becomes not well-formed)
                            Element orgElement = (Element) localNode;
                            orgElement.removeAttribute("xml:id");
                            orgElement.setAttribute("ref", "#" + name);
                            e.removeAttribute("ref");
                            e.appendChild(localNode);
                        }
                    }
                }
            }
        }
    }
}
