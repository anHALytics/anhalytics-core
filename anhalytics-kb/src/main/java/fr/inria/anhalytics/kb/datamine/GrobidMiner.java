package fr.inria.anhalytics.kb.datamine;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.dao.AddressDAO;
import fr.inria.anhalytics.dao.Conference_EventDAO;
import fr.inria.anhalytics.dao.DocumentDAO;
import fr.inria.anhalytics.dao.In_SerialDAO;
import fr.inria.anhalytics.dao.MonographDAO;
import fr.inria.anhalytics.dao.PersonDAO;
import fr.inria.anhalytics.dao.PublicationDAO;
import fr.inria.anhalytics.kb.dao.biblio.AbstractBiblioDAOFactory;
import fr.inria.anhalytics.kb.dao.biblio.BiblioDAOFactory;
import fr.inria.anhalytics.kb.entities.Address;
import fr.inria.anhalytics.kb.entities.Collection;
import fr.inria.anhalytics.kb.entities.Conference;
import fr.inria.anhalytics.kb.entities.Conference_Event;
import fr.inria.anhalytics.kb.entities.Country;
import fr.inria.anhalytics.kb.entities.Editor;
import fr.inria.anhalytics.kb.entities.In_Serial;
import fr.inria.anhalytics.kb.entities.Journal;
import fr.inria.anhalytics.kb.entities.Monograph;
import fr.inria.anhalytics.kb.entities.Person;
import fr.inria.anhalytics.kb.entities.Person_Identifier;
import fr.inria.anhalytics.kb.entities.Publication;
import fr.inria.anhalytics.kb.entities.Publisher;
import fr.inria.anhalytics.kb.entities.Serial_Identifier;
import fr.inria.anhalytics.kb.properties.IngestProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author azhar
 */
public class GrobidMiner extends Miner {

    private static final Logger logger = LoggerFactory.getLogger(GrobidMiner.class);

    private static final AbstractBiblioDAOFactory abdf = AbstractBiblioDAOFactory.getFactory(AbstractBiblioDAOFactory.DAO_FACTORY);

    private static XPath xPath = XPathFactory.newInstance().newXPath();

    public GrobidMiner() throws UnknownHostException {
        super();
    }

    /**
     *
     */
    public void processCitations() {
        BiblioDAOFactory.initConnection();
        DocumentDAO dd = (DocumentDAO) abdf.getDocumentDAO();

        for (String date : Utilities.getDates()) {
            if (!IngestProperties.isProcessByDate()) {
                date = null;
            }
            if (mm.initTeis(date)) {
                while (mm.hasMoreTeis()) {
                    String teiString = mm.nextTeiDocument();
                    String uri = mm.getCurrentRepositoryDocId();
                    String anhalyticsId = mm.getCurrentAnhalyticsId();
                    if(anhalyticsId == null || anhalyticsId.isEmpty()){
                        logger.info("skipping "+uri+" No anHALytics id provided");
                        continue;
                    }
                    if (!dd.isCitationsMined(anhalyticsId)) {
                        logger.info("Extracting :" + uri);
                        abdf.openTransaction();
                        try {
                            InputStream teiStream = new ByteArrayInputStream(teiString.getBytes());
                            Document teiDoc = getDocument(teiStream);
                            teiStream.close();
                            Node citations = (Node) xPath.compile("/teiCorpus/TEI/text/back/div[@type='references']/listBibl").evaluate(teiDoc, XPathConstants.NODE);
                            if (citations != null) {
                                NodeList references = citations.getChildNodes();
                                fr.inria.anhalytics.kb.entities.Document doc = new fr.inria.anhalytics.kb.entities.Document(anhalyticsId, Utilities.getVersionFromURI(uri), Utilities.innerXmlToString(citations), uri);
                                dd.create(doc);

                                for (int j = 0; j < references.getLength() - 1; j++) {
                                    Node reference = references.item(j);
                                    if (reference.getNodeType() == Node.ELEMENT_NODE) {
                                        processBiblStruct((Element)reference , doc);
                                    }
                                }
                            }
                        } catch (Exception xpe) {
                            xpe.printStackTrace();
                            abdf.rollback();
                        }
                        abdf.endTransaction();
                    }
                }
            }
            if (!IngestProperties.isProcessByDate()) {
                break;
            }
        }
        BiblioDAOFactory.closeConnection();
    }

    private void processBiblStruct(Element reference, fr.inria.anhalytics.kb.entities.Document doc) throws SQLException {
        PublicationDAO pd = (PublicationDAO) abdf.getPublicationDAO();
        MonographDAO md = (MonographDAO) abdf.getMonographDAO();
        In_SerialDAO isd = (In_SerialDAO) abdf.getIn_SerialDAO();
        PersonDAO persd = (PersonDAO) abdf.getPersonDAO();
        Conference_EventDAO ced = (Conference_EventDAO) abdf.getConference_EventDAO();

        Conference_Event ce = null;
        In_Serial is = null;
        Publication pub = new Publication();
        Monograph mn = new Monograph();

        pub.setDocument(doc);
        List<Person> prss = new ArrayList<Person>();
        Node analytic = (reference.getElementsByTagName("analytic")).item(0);

        Node monogr = (reference.getElementsByTagName("monogr")).item(0);
        if (analytic != null && analytic.getNodeType() == Node.ELEMENT_NODE) {
            NodeList analyticChilds = analytic.getChildNodes();
            for (int z = analyticChilds.getLength() - 1; z >= 0; z--) {
                Node analyticChild = analyticChilds.item(z);

                if (analyticChild.getNodeType() == Node.ELEMENT_NODE) {
                    Element analyticChildElt = (Element) analyticChild;
                    if (analyticChildElt.getNodeName().equals("title")) {
                        pub.setDoc_title(analyticChildElt.getTextContent());
                        pub.setType(analyticChildElt.getAttribute("level"));
                    } else if (analyticChildElt.getNodeName().equals("author")) {
                        Person prs = new Person();
                        List<Person_Identifier> pis = new ArrayList<Person_Identifier>();
                        NodeList authorChilds = analyticChilds.item(z).getChildNodes();
                        for (int y = authorChilds.getLength() - 1; y >= 0; y--) {
                            Node authorChild = authorChilds.item(y);
                            if (authorChild.getNodeType() == Node.ELEMENT_NODE) {
                                Element authorChildElt = (Element) authorChild;
                                if (authorChildElt.getNodeName().equals("persName")) {
                                    NodeList persNameChilds = authorChildElt.getChildNodes();
                                    for (int o = persNameChilds.getLength() - 1; o >= 0; o--) {
                                        Node persNameChild = persNameChilds.item(o);
                                        if (persNameChild.getNodeType() == Node.ELEMENT_NODE) {
                                            Element persNameChildElt = (Element)persNameChild;
                                            if (persNameChildElt.getNodeName().equals("forename")) {
                                                prs.setForename(persNameChildElt.getTextContent());
                                            } else if (persNameChildElt.getNodeName().equals("surname")) {
                                                prs.setSurname(persNameChildElt.getTextContent());
                                            }
                                        }
                                    }
                                } else if (authorChildElt.getNodeName().equals("email")) {
                                    prs.setEmail(authorChildElt.getTextContent());
                                } else if (authorChildElt.getNodeName().equals("ptr")) {
                                    if (authorChildElt.getAttribute("type").equals("url")) {
                                        prs.setUrl(authorChildElt.getAttribute("type"));
                                    }
                                } else if (authorChildElt.getNodeName().equals("idno")) {
                                    Person_Identifier pi = new Person_Identifier();
                                    String id_type = authorChildElt.getAttribute("type");
                                    String id_value = authorChildElt.getTextContent();
                                    pi.setId(id_value);
                                    pi.setType(id_type);
                                    pis.add(pi);
                                }
                            }
                        }
                        prs.setFullname(prs.getSurname() + " " + prs.getMiddlename() + " " + prs.getForename());
                        prs.setPerson_identifiers(pis);
                        prss.add(prs);
                    }
                }
            }
        }
        if (monogr != null && monogr.getNodeType() == Node.ELEMENT_NODE) {
            NodeList monogrChilds = monogr.getChildNodes();

            Journal journal = null;
            Collection collection = null;
            List<Serial_Identifier> serial_identifiers = new ArrayList<Serial_Identifier>();
            for (int c = monogrChilds.getLength() - 1; c >= 0; c--) {
                Node monogrChild = monogrChilds.item(c);
                if (monogrChild.getNodeType() == Node.ELEMENT_NODE) {
                    Element monogrChildElt = (Element) monogrChild;
                    if (monogrChildElt.getNodeName().equals("title")) {
                        mn.setTitle(monogrChildElt.getTextContent());
                        if (analytic == null) {
                            pub.setDoc_title(monogrChildElt.getTextContent());
                        }
                        mn.setType(monogrChildElt.getAttribute("level"));
                        if (analytic == null) {
                            pub.setType(monogrChildElt.getAttribute("level"));
                        }
                        if (monogrChildElt.getTextContent() != null) {
                            if (monogrChildElt.getAttribute("level").equals("j")) {
                                journal = new Journal(null, monogrChildElt.getTextContent());
                            } else {
                                collection = new Collection(null, monogrChildElt.getTextContent());
                            }
                        }
                    } else if (monogrChildElt.getNodeName().equals("idno")) {
                        serial_identifiers.add(new Serial_Identifier(null, monogrChildElt.getTextContent(), monogrChildElt.getAttribute("type"), journal, collection));
                    } else if (monogrChildElt.getNodeName().equals("imprint")) {
                        NodeList imprintChilds = monogrChildElt.getChildNodes();
                        is = new In_Serial();
                        for (int j = imprintChilds.getLength() - 1; j >= 0; j--) {
                            Node imprintChild = imprintChilds.item(j);
                            if (imprintChild.getNodeType() == Node.ELEMENT_NODE) {
                                Element imprintChildElt = (Element) imprintChild;
                                if (imprintChildElt.getNodeName().equals("publisher")) {
                                    Publisher pls = new Publisher(null, imprintChildElt.getTextContent());
                                    abdf.getPublisherDAO().create(pls);
                                    pub.setPublisher(pls);
                                } else if (imprintChildElt.getNodeName().equals("date")) {
                                    String type = imprintChildElt.getAttribute("type");
                                    String date = imprintChildElt.getAttribute("when");
                                    pub.setDate_eletronic(date);
                                    String[] n = date.split("-");
                                    if (n.length == 1) {
                                        date = date + "-01-01";
                                    } else if (n.length == 2) {
                                        date = date + "-01";
                                    }

                                    try {
                                        pub.setDate_printed(Utilities.parseStringDate(date));
                                    } catch (ParseException ex) {
                                        ex.printStackTrace();
                                    }
                                } else if (imprintChildElt.getNodeName().equals("biblScope")) {
                                    String unit = imprintChildElt.getAttribute("unit");
                                    if (unit.equals("serie")) {
                                        collection.setTitle(imprintChildElt.getTextContent());
                                    } else if (unit.equals("volume")) {
                                        is.setVolume(imprintChildElt.getTextContent());
                                    } else if (unit.equals("issue")) {
                                        is.setIssue(imprintChildElt.getTextContent());
                                    } else if (unit.equals("page")) {
                                        String start = imprintChildElt.getAttribute("from");
                                        String end = imprintChildElt.getAttribute("to");
                                        pub.setStart_page(start);
                                        pub.setEnd_page(end);
                                    }
                                }
                            }
                        }
                    } else if (monogrChildElt.getNodeName().equals("meeting")) {
                        ce = new Conference_Event();
                        ce.setConference(new Conference());
                        Address addr = new Address();
                        AddressDAO ad = (AddressDAO) abdf.getAddressDAO();
                        NodeList meetingChilds = monogrChildElt.getChildNodes();

                        for (int j = meetingChilds.getLength() - 1; j >= 0; j--) {
                            Node meetingChild = meetingChilds.item(j);
                            if (meetingChild.getNodeType() == Node.ELEMENT_NODE) {
                                Element meetingChildElt = (Element) meetingChild;
                                if (meetingChildElt.getNodeName().equals("title")) {
                                    ce.getConference().setTitle(meetingChildElt.getTextContent());
                                } else if (meetingChildElt.getNodeName().equals("date")) {
                                    String type = meetingChildElt.getAttribute("type");
                                    String date = meetingChildElt.getAttribute("when");
                                    if (type.equals("start")) {
                                        ce.setStart_date(date);
                                    } else if (type.equals("end")) {
                                        ce.setEnd_date(date);
                                    }
                                } else if (meetingChildElt.getNodeName().equals("settlement")) {
                                    addr.setSettlement(meetingChildElt.getTextContent());
                                } else if (meetingChildElt.getNodeName().equals("country")) {
                                    Country country = new Country();
                                    addr.setCountryStr(meetingChildElt.getTextContent());
                                    country.setIso(meetingChildElt.getAttribute("key"));
                                    addr.setCountry(country);
                                } else if (meetingChildElt.getNodeName().equals("region")) {
                                    addr.setRegion(meetingChildElt.getTextContent());
                                }
                            }
                        }
                        ad.create(addr);
                        ce.setAddress(addr);
                    }
                    if (collection != null) {
                        is.setC(collection);
                    }
                    if (journal != null) {
                        is.setJ(journal);
                    }
                }
            }
            md.create(mn);

            if (ce != null) {
                ce.setMongoraph(mn);
                ced.create(ce);
            }
            if (is
                    != null) {
                is.setMg(mn);
                isd.create(is);
            }

            for (Serial_Identifier serial_identifier : serial_identifiers) {
                serial_identifier.setJournal(journal);
                serial_identifier.setCollection(collection);
                isd.createSerialIdentifier(serial_identifier);
            }
            pub.setMonograph(mn);
        }

        pd.create(pub);
        for (Person p : prss) {
            persd.createEditor(new Editor(0, p, pub));
        }
    }

    private Document getDocument(InputStream in) throws IOException, ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);

        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = null;
        try {
            doc = docBuilder.parse(in);
        } catch (SAXException e) {
            e.printStackTrace();

        }
        return doc;
    }
}
