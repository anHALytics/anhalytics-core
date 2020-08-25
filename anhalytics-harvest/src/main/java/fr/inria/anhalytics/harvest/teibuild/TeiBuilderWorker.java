package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.exceptions.DataException;
import fr.inria.anhalytics.commons.exceptions.SystemException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.harvesters.Harvester;
import fr.inria.anhalytics.harvest.converters.HalTEIConverter;
import fr.inria.anhalytics.harvest.converters.IstexTEIConverter;
import fr.inria.anhalytics.harvest.converters.MetadataConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Functions that builds TEICorpus.
 *
 * 1. Creates Tei corpus (we use this format to append other extracted metadata
 * from annexes, annotation standoffs..) 2. Appends Extracted Grobid Tei to TEI
 * Corpus (completes missing abstract, keywords, affiliations, publication date)
 *
 * @author Achraf
 */
public class TeiBuilderWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TeiBuilderWorker.class);

    private Steps step;

    protected MongoFileManager mm;

    protected BiblioObject biblioObject;

    public TeiBuilderWorker(BiblioObject biblioObject, Steps step) {
        this.step = step;
        this.biblioObject = biblioObject;
    }

    @Override
    public void run() {
        this.mm = MongoFileManager.getInstance(false);
        long startTime = System.nanoTime();
        logger.info(Thread.currentThread().getName() + " Start. Processing = " + biblioObject.getRepositoryDocId());

        if(step == Steps.TRANSFORM) {
            try {
                logger.info("\t\t transforming :" + biblioObject.getRepositoryDocId());
                Document generatedTEIcorpus = createTEICorpus();
                if (generatedTEIcorpus != null) {
                    boolean inserted = mm.insertTEIcorpus(Utilities.toString(generatedTEIcorpus), biblioObject.getAnhalyticsId());
                    if (inserted) {
                        biblioObject.setIsProcessedByPub2TEI(Boolean.TRUE);
                        //We re-initialize everything for this new TEI (this is considered the starting point of a new coming entry from source)
                        biblioObject.setIsFulltextAppended(Boolean.FALSE);
                        biblioObject.setIsMined(Boolean.FALSE);
                        biblioObject.setIsIndexed(Boolean.FALSE);
                        mm.updateBiblioObjectStatus(biblioObject, null, true);
                        logger.info("\t\t " + biblioObject.getRepositoryDocId()+ " transformed.");
                    } else {
                        logger.error("\t\t Problem occured while saving " + biblioObject.getRepositoryDocId() + " corpus TEI.");
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else if(step == Steps.APPEND_FULLTEXT) {

            Document generatedTEIcorpus = null;
            logger.info("\t Building TEI for: " + biblioObject.getRepositoryDocId());
            //tei.setTei(Utilities.trimEncodedCharaters(tei.getTei()));
            try {
                String grobidTei = mm.getGrobidTei(biblioObject);
                String TEICorpus = mm.getTEICorpus(biblioObject);

                generatedTEIcorpus = addGrobidTEIToTEICorpus(TEICorpus, grobidTei);

                boolean inserted = mm.insertTEIcorpus(Utilities.toString(generatedTEIcorpus), biblioObject.getAnhalyticsId());
                if (inserted) {
                    biblioObject.setIsFulltextAppended(Boolean.TRUE);
                    mm.updateBiblioObjectStatus(biblioObject, null, true);
                    logger.info("\t\t " + biblioObject.getRepositoryDocId()+ " built.");
                } else {
                    logger.error("\t\t Problem occured while saving " + biblioObject.getRepositoryDocId() + " corpus TEI.");
                }

            } catch (DataException de) {
                logger.error("No corresponding fulltext TEI was found for " + biblioObject.getRepositoryDocId() + ".");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        long endTime = System.nanoTime();
        logger.info(Thread.currentThread().getName() + " End. :" + (endTime - startTime) / 1000000 + " ms");
    }

    /**
     * Creates a working TEICorpus from the harvested metadata in TEI header.
     */
    public Document createTEICorpus() throws IOException, SAXException {
        DocumentBuilder docBuilder = null;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new SystemException("Cannot instantiate TeiBuilder", e);
        }

        MetadataConverter mc;
        if (StringUtils.equals(HarvestProperties.getSource().toLowerCase(), Harvester.Source.HAL.getName())) {
            mc = new HalTEIConverter();
        } else if (StringUtils.equals(HarvestProperties.getSource().toLowerCase(), Harvester.Source.ISTEX.getName())){
            mc = new IstexTEIConverter();
        } else {
            throw new RuntimeException("Missing -source ");
        }
        Document newTEICorpus = null;
        Document metadataDoc = docBuilder.parse(new InputSource(new ByteArrayInputStream(biblioObject.getMetadata().getBytes("utf-8"))));

        newTEICorpus = docBuilder.newDocument();
        //biblioobject is used to fill the missing metadata, domains, publication type, doi...
        Element teiHeader = mc.convertMetadataToTEIHeader(metadataDoc, newTEICorpus, biblioObject);

        Element teiCorpus = newTEICorpus.createElement("teiCorpus");
        teiHeader.setAttribute("xml:id", HarvestProperties.getSource());
        teiCorpus.appendChild(teiHeader);
        teiCorpus.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0");
        newTEICorpus.appendChild(teiCorpus);
        Utilities.generateIDs(newTEICorpus);
        return newTEICorpus;
    }

    /**
     * appends fulltext grobid tei to existing TEICorpus. Fills missing metadata
     * parts , abstract, keywords, publication date and authors.
     */
    public Document addGrobidTEIToTEICorpus(String teiCorpus, String grobidTei) throws IOException, SAXException {
        DocumentBuilder docBuilder = null;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new SystemException("Cannot instantiate ", e);
        }

        Document resultTei = null;

        HalTEIConverter htc = new HalTEIConverter();
        Document teiCorpusDoc = docBuilder.parse(new InputSource(new ByteArrayInputStream(teiCorpus.getBytes("utf-8"))));
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
        Element grobidTeiElement = null;
        if (grobidTei != null) {
            doc = docBuilder.parse(new InputSource(new ByteArrayInputStream(grobidTei.getBytes("utf-8"))));
            grobidTeiElement = (Element) doc.getDocumentElement();
            Attr attr = grobidTeiElement.getAttributeNode("xmlns");
            grobidTeiElement.removeAttributeNode(attr);
            grobidTeiElement.setAttribute("type", "main");
            grobidTeiElement.setAttribute("xml:id", "grobid");
            try {
                fillAbstract(doc, teiCorpusDoc);
                fillKeywords(doc, teiCorpusDoc);
                fillPubDate(doc, teiCorpusDoc);
                fillAuthors(doc, teiCorpusDoc);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }
        resultTei = addNewElementToTEI(teiCorpusDoc, grobidTeiElement);
        // add random xml:id on textual elements

        return resultTei;
    }

    public void fillAbstract(Document doc, Document teiCorpusDoc) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
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

    public void fillKeywords(Document grobidDoc, Document teiCorpusDoc) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        Element profileDescElt = (Element) xPath.compile("/teiCorpus/teiHeader/profileDesc").evaluate(teiCorpusDoc, XPathConstants.NODE);
        NodeList existingKeywordsNode = profileDescElt.getElementsByTagName("term");
        if (existingKeywordsNode.getLength() == 0) {
            Element textClassElt = (Element) xPath.compile("/TEI/teiHeader/profileDesc/textClass").evaluate(grobidDoc, XPathConstants.NODE);
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

    /**
     * We consider that grobid is accurate.
     */
    public void fillPubDate(Document doc, Document teiCorpusDoc) throws XPathExpressionException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Element dateElt = (Element) xPath.compile("/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/imprint/date[@type=\"published\"]").evaluate(teiCorpusDoc, XPathConstants.NODE);
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

    /**
     * Completes the authors affiliation using grobid.
     */
    public void fillAuthors(Document grobidDoc, Document teiCorpusDoc) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
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
