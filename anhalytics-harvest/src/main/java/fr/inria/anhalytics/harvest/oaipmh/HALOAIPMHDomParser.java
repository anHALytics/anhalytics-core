package fr.inria.anhalytics.harvest.oaipmh;

import fr.inria.anhalytics.commons.data.PublicationFile;
import fr.inria.anhalytics.commons.data.TEI;
import fr.inria.anhalytics.commons.exceptions.DataException;
import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

/**
 * Extract and parse records from oai-pmh response.
 *
 * @author Achraf
 */
public class HALOAIPMHDomParser {

    protected static final Logger logger = LoggerFactory.getLogger(HALOAIPMHDomParser.class);

    private List<TEI> teis = new ArrayList<TEI>();
    private Document doc;
    private String token;
    private XPath xPath;

    public HALOAIPMHDomParser() {
        xPath = XPathFactory.newInstance().newXPath();
    }

    public List<TEI> getTeis(InputStream in) {
        teis = new ArrayList<TEI>();
        setDoc(parse(in));
        if (doc != null) {
            Element rootElement = doc.getDocumentElement();
            NodeList listRecords = getRecords(rootElement);

            //XPath xPath = XPathFactory.newInstance().newXPath();//play with it  
            //NodeList nodes = (NodeList)xPath.evaluate("/OAI-PMH/ListRecords/record/metadata", rootElement, XPathConstants.NODESET);
            setToken(rootElement);
            logger.info("\t \t " + listRecords.getLength() + " records found. processing...");

            if (listRecords.getLength() >= 1) {
                for (int i = listRecords.getLength() - 1; i >= 0; i--) {
                    if ((listRecords.item(i) instanceof Element)) {
                        Element record = (Element) listRecords.item(i);
                        String type = getDocumentType(record.getElementsByTagName(OAIPMHPathsItf.TypeElement));
                        if (isConsideredType(type)) {
                            String completeRepositoryDocId = getRepositoryDocId(record.getElementsByTagName(OAIPMHPathsItf.IdElement));
                            String currentVersion = getCurrentVersion(record);
                            String docVersion = Utilities.getVersionFromURI(completeRepositoryDocId);
                            if (docVersion.equals(currentVersion)) {
                                String tei = getTei(record.getElementsByTagName(OAIPMHPathsItf.TeiElement));
                                String doi = getDoi(record);

                                PublicationFile file = getFile(record);
                                List<PublicationFile> annexes = getAnnexes(record);

                                String ref = getRef(record);
                                teis.add(new TEI(completeRepositoryDocId, file, annexes, doi, type, tei, ref));
                                logger.info("\t \t \t tei of " + completeRepositoryDocId + " extracted.");
                            } else {
                                logger.info("\t \t \t skipping " + completeRepositoryDocId + " , it's not a current version.");
                            }
                        }
                    }
                }

            }
        } else {
            throw new ServiceException("No TEIs metadata found.");
        }
        return teis;
    }

    public String getCurrentVersion(Node record) {
        String currentVersion = null;
        try {
            Element node = (Element) xPath.compile(OAIPMHPathsItf.EditionElement).evaluate(record, XPathConstants.NODE);
            currentVersion = node.getAttribute("n");
        } catch (DataException | XPathExpressionException ex) {
            logger.info("\t \t \t \t No current edition found .");
        }
        return currentVersion;
    }

    public String getRef(Node ref) {
        String reference = null;
        Node node = null;
        try {
            node = (Node) xPath.compile(OAIPMHPathsItf.RefPATH).evaluate(ref, XPathConstants.NODE);
            if (node != null) {
                reference = node.getTextContent();
            } else {
                throw new DataException();
            }
        } catch (DataException | XPathExpressionException | DOMException ex) {
            logger.info("\t \t \t \t hal ref not found");
        }
        return reference;
    }

    public String getDoi(Node ref) {
        String doi = null;
        try {
            Node node = (Node) xPath.compile(OAIPMHPathsItf.DoiPATH).evaluate(ref, XPathConstants.NODE);
            if (node != null) {
                doi = node.getTextContent();
            } else {
                throw new DataException();
            }
        } catch (DataException | XPathExpressionException | DOMException ex) {
            logger.info("\t \t \t \t doi not found");
        }
        return doi;
    }

    public String getToken() {
        return this.token;
    }

    private void setToken(Element rootElement) {
        try {
            this.token = URLEncoder.encode(rootElement.getElementsByTagName(OAIPMHPathsItf.ResumptionToken).item(0).getTextContent());
        } catch (Exception ex) {
            this.token = null;
        }
    }

    private void setDoc(Document doc) {
        this.doc = doc;
    }

    private Document parse(InputStream in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(in);
        } catch (DataException | IOException | ParserConfigurationException | SAXException e) {//
            logger.error("Could not parse document because "
                    + e.getMessage());
        }
        return null;
    }

    public PublicationFile getFile(Node record) {
        PublicationFile file = null;
        try {
            Element node = (Element) xPath.compile(OAIPMHPathsItf.FileElement).evaluate(record, XPathConstants.NODE);
            if (node != null) {

                String url = node.getAttribute("target");
                Element dateNode = (Element) node.getChildNodes().item(1);
                String embargoDate = "";
                if (dateNode != null) {
                    embargoDate = dateNode.getAttribute("notBefore");
                }
                file = new PublicationFile(url, embargoDate, false);
            } else {
                throw new DataException();
            }
        } catch (DataException | XPathExpressionException ex) {
            logger.info("\t \t \t \t No file attached .");
        }
        return file;
    }

    public List<PublicationFile> getAnnexes(Node record) {
        List<PublicationFile> annexes = new ArrayList<PublicationFile>();
        NodeList nodes = null;
        try {
            nodes = (NodeList) xPath.compile(OAIPMHPathsItf.AnnexesUrlsElement).evaluate(record, XPathConstants.NODESET);
        } catch (XPathExpressionException ex) {
            logger.info("\t \t \t \t No annex files attached .");
        }
        String url = null;
        String embargoDate = null;
        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            Element node = (Element) nodes.item(i);
            url = node.getAttribute("target");
            embargoDate = ((Element) node.getChildNodes().item(1)).getAttribute("notBefore");
            annexes.add(new PublicationFile(url, embargoDate, true));
        }
        return annexes;
    }

    public String getTei(NodeList tei) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        NodeList teiNodes = tei.item(0).getChildNodes();
        for (int i = teiNodes.getLength() - 1; i >= 0; i--) {
            if (teiNodes.item(i) instanceof Element) {
                Element teiElement = (Element) teiNodes.item(i);
                teiElement.setAttribute("xmlns:tei", "http://www.tei-c.org/ns/1.0");//BOF
                renameNode(teiElement);
            }
        }
        String teiString = sb.append(innerXmlToString(tei.item(0))).toString();
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        Document teiDoc = null;
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            teiDoc = docBuilder.parse(new ByteArrayInputStream(teiString.getBytes()));
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
        Utilities.generateIDs(teiDoc);
        try {
            teiString = Utilities.toString(teiDoc);
        } catch (DataException de) {
            de.printStackTrace();
        }
        return teiString;
    }

    private void renameNode(Element teiElement) {
        doc.renameNode(teiElement, null, teiElement.getTagName().split(":")[1]);
        NodeList nodes = teiElement.getChildNodes();
        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            if (nodes.item(i) instanceof Element) {
                Element element = (Element) nodes.item(i);
                renameNode(element);
            }
        }
    }

    public String getRepositoryDocId(NodeList identifier) {
        return identifier.item(0).getTextContent().split(":")[2];
    }

    public String getDocumentType(NodeList sets) {
        String type = null;
        for (int i = sets.getLength() - 1; i >= 0; i--) {
            String content = sets.item(i).getTextContent();
            if (content.contains("type")) {
                String[] n = content.split(":");
                type = n.length > 1 ? n[1] : null;
                break;
            }
        }
        return type;
    }

    private NodeList getRecords(Element rootElement) {
        return rootElement.getElementsByTagName(OAIPMHPathsItf.RecordElement);
    }

    private static String innerXmlToString(Node node) {
        DOMImplementationLS lsImpl
                = (DOMImplementationLS) node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
        LSSerializer lsSerializer = lsImpl.createLSSerializer();
        lsSerializer.getDomConfig().setParameter("xml-declaration", false);
        NodeList childNodes = node.getChildNodes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < childNodes.getLength(); i++) {
            sb.append(lsSerializer.writeToString(childNodes.item(i)));
        }
        return sb.toString();
    }

    private boolean isConsideredType(String setSpec) {
        try {
            OAIPMHPathsItf.ConsideredTypes.valueOf(setSpec);
            return true;
        } catch (NullPointerException | IllegalArgumentException ex) {
            return false;
        }
    }
}
