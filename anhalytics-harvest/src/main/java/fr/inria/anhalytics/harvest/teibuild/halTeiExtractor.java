package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.utilities.Utilities;
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

    public static final String TITLE_PATH = "/TEI/text/body/listBibl/biblFull/titleStmt/title";
    public static final String ABSTRACT_PATH = "/TEI/text/body/listBibl/biblFull/profileDesc/abstract";
    public static final String AUTHORS_PATH = "/TEI/text/body/listBibl/biblFull/titleStmt/author";
    public static final String TEXTCLASS_PATH = "/TEI/text/body/listBibl/biblFull/profileDesc/textClass";
    public static final String PUBDATE_PATH = "/TEI/text/body/listBibl/biblFull/sourceDesc/biblStruct/monogr/imprint/date";
    public static final String PUBPRODDATE_PATH = "/TEI/text/body/listBibl/biblFull/editionStmt/edition[@type='current']/date[@type='whenProduced']";
    public static final String LANG_PATH = "/TEI/text/body/listBibl/biblFull/profileDesc/langUsage";
    
    public static NodeList getTeiHeader(Document docAdditionalTei) {
        /////////////// Hal specific : To be done as a harvesting post process before storing tei ////////////////////
        // remove ugly end-of-line in starting and ending text as it is
        // a problem for stand-off annotations
        Utilities.trimEOL(docAdditionalTei.getDocumentElement(), docAdditionalTei);
        docAdditionalTei = removeUnnecessaryParts(docAdditionalTei);
        NodeList orgs = docAdditionalTei.getElementsByTagName("org");
        NodeList authors = docAdditionalTei.getElementsByTagName("author");
        removeConsolidatedData(docAdditionalTei, authors);
        updateAffiliations(authors, orgs, docAdditionalTei);
        NodeList editors = docAdditionalTei.getElementsByTagName("editor");
        removeConsolidatedData(docAdditionalTei, editors);
        updateAffiliations(editors, orgs, docAdditionalTei);
        return docAdditionalTei.getElementsByTagName("biblFull");
    }
    
    private static void removeConsolidatedData(Document docAdditionalTei, NodeList entities){
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
            docAdditionalTei.getElementsByTagName("titleStmt").item(1).removeChild(person);
            NodeList biblStruct = docAdditionalTei.getElementsByTagName("biblStruct");
            biblStruct.item(0).insertBefore(person, biblStruct.item(0).getFirstChild());
        }
    
    }
    
    private static Document removeUnnecessaryParts(Document docAdditionalTei){
        docAdditionalTei = Utilities.removeElement(docAdditionalTei, "analytic");
        docAdditionalTei = Utilities.removeElement(docAdditionalTei, "editionStmt");
        docAdditionalTei = Utilities.removeElement(docAdditionalTei, "publicationStmt");
        docAdditionalTei = Utilities.removeElement(docAdditionalTei, "publicationStmt");
        docAdditionalTei = Utilities.removeElement(docAdditionalTei, "seriesStmt");
        docAdditionalTei = Utilities.removeElement(docAdditionalTei, "notesStmt");
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
