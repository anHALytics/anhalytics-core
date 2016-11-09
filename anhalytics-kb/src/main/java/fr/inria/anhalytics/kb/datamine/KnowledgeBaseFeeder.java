package fr.inria.anhalytics.kb.datamine;

import fr.inria.anhalytics.commons.exceptions.NumberOfCoAuthorsExceededException;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.commons.dao.AbstractDAOFactory;
import fr.inria.anhalytics.commons.dao.AddressDAO;
import fr.inria.anhalytics.commons.dao.anhalytics.AffiliationDAO;
import fr.inria.anhalytics.commons.dao.Conference_EventDAO;
import fr.inria.anhalytics.commons.dao.anhalytics.LocationDAO;
import fr.inria.anhalytics.commons.dao.DocumentDAO;
import fr.inria.anhalytics.commons.dao.Document_OrganisationDAO;
import fr.inria.anhalytics.commons.dao.In_SerialDAO;
import fr.inria.anhalytics.commons.dao.MonographDAO;
import fr.inria.anhalytics.commons.dao.anhalytics.OrganisationDAO;
import fr.inria.anhalytics.commons.dao.PersonDAO;
import fr.inria.anhalytics.commons.dao.PublicationDAO;
import fr.inria.anhalytics.commons.dao.PublisherDAO;
import fr.inria.anhalytics.commons.dao.anhalytics.DAOFactory;
import fr.inria.anhalytics.commons.dao.biblio.AbstractBiblioDAOFactory;
import fr.inria.anhalytics.commons.dao.biblio.BiblioDAOFactory;
import fr.inria.anhalytics.commons.entities.Address;
import fr.inria.anhalytics.commons.entities.Affiliation;
import fr.inria.anhalytics.commons.entities.Author;
import fr.inria.anhalytics.commons.entities.Collection;
import fr.inria.anhalytics.commons.entities.Conference;
import fr.inria.anhalytics.commons.entities.Conference_Event;
import fr.inria.anhalytics.commons.entities.Country;
import fr.inria.anhalytics.commons.entities.Document_Identifier;
import fr.inria.anhalytics.commons.entities.Document_Organisation;
import fr.inria.anhalytics.commons.entities.Editor;
import fr.inria.anhalytics.commons.entities.In_Serial;
import fr.inria.anhalytics.commons.entities.Journal;
import fr.inria.anhalytics.commons.entities.Location;
import fr.inria.anhalytics.commons.entities.Monograph;
import fr.inria.anhalytics.commons.entities.Organisation;
import fr.inria.anhalytics.commons.entities.Organisation_Identifier;
import fr.inria.anhalytics.commons.entities.Organisation_Name;
import fr.inria.anhalytics.commons.entities.PART_OF;
import fr.inria.anhalytics.commons.entities.Person;
import fr.inria.anhalytics.commons.entities.Person_Identifier;
import fr.inria.anhalytics.commons.entities.Person_Name;
import fr.inria.anhalytics.commons.entities.Publication;
import fr.inria.anhalytics.commons.entities.Publisher;
import fr.inria.anhalytics.commons.entities.Serial_Identifier;
import fr.inria.anhalytics.commons.properties.KbProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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
 * Format and Extract HAL metadata to seed the KB.
 *
 * @author Achraf
 */
public class KnowledgeBaseFeeder {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseFeeder.class);

    private static final AbstractDAOFactory adf = AbstractDAOFactory.getFactory(AbstractDAOFactory.DAO_FACTORY);

    private static final AbstractBiblioDAOFactory abdf = AbstractBiblioDAOFactory.getFactory(AbstractBiblioDAOFactory.DAO_FACTORY);

    private static XPath xPath = XPathFactory.newInstance().newXPath();

    protected MongoFileManager mm = null;

    public KnowledgeBaseFeeder() throws UnknownHostException {
        this.mm = MongoFileManager.getInstance(false);
    }

    /**
     * Initiates HAL knowledge base and creates working corpus TEI.
     */
    public void initKnowledgeBase() throws SQLException {
        DAOFactory.initConnection();
        PublicationDAO pd = (PublicationDAO) adf.getPublicationDAO();
        DocumentDAO dd = (DocumentDAO) adf.getDocumentDAO();

        for (String date : Utilities.getDates()) {

            if (!KbProperties.isProcessByDate()) {
                date = null;
            }
            if (mm.initTeis(date, true, MongoCollectionsInterface.FINAL_TEIS)) {
                while (mm.hasMoreTeis()) {
                    String metadataTeiString = mm.nextTeiDocument();
                    String repositoryDocId = mm.getCurrentRepositoryDocId();
                    String currentAnhalyticsId = mm.getCurrentAnhalyticsId();
                    if (currentAnhalyticsId == null || currentAnhalyticsId.isEmpty()) {
                        logger.info("skipping " + repositoryDocId + " No anHALytics id provided");
                        continue;
                    }
                    if (!dd.isMined(currentAnhalyticsId)) {
                        adf.openTransaction();
                        Document teiDoc = null;
                        try {
                            InputStream teiStream = new ByteArrayInputStream(metadataTeiString.getBytes());
                            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                            docFactory.setValidating(false);
                            //docFactory.setNamespaceAware(true);
                            DocumentBuilder docBuilder = null;
                            try {
                                docBuilder = docFactory.newDocumentBuilder();
                                teiDoc = docBuilder.parse(teiStream);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            teiStream.close();

                            Publication pub = new Publication();

                            Element teiHeader = (Element) xPath.compile(TeiPaths.MetadataElement).evaluate(teiDoc, XPathConstants.NODE);
                            NodeList authorsFromfulltextTeiHeader = (NodeList) xPath.compile(TeiPaths.FulltextTeiHeaderAuthors).evaluate(teiDoc, XPathConstants.NODESET);

                            Element title = (Element) teiHeader.getElementsByTagName("title").item(0);
                            Node language = (Node) xPath.compile(TeiPaths.LanguageElement).evaluate(teiDoc, XPathConstants.NODE);
                            Node type = (Node) xPath.compile(TeiPaths.TypologyElement).evaluate(teiDoc, XPathConstants.NODE);
                            Node submission_date = (Node) xPath.compile(TeiPaths.SubmissionDateElement).evaluate(teiDoc, XPathConstants.NODE);
                            Node domain = (Node) xPath.compile(TeiPaths.DomainElement).evaluate(teiDoc, XPathConstants.NODE);
                            //more than one domain / article
                            NodeList editors = teiHeader.getElementsByTagName("editor");
                            NodeList authors = teiHeader.getElementsByTagName("author");
                            Element monogr = (Element) xPath.compile(TeiPaths.MonogrElement).evaluate(teiDoc, XPathConstants.NODE);
                            NodeList ids = (NodeList) xPath.compile(TeiPaths.IdnoElement).evaluate(teiDoc, XPathConstants.NODESET);
                            logger.info("Extracting :" + repositoryDocId);
                            if (authors.getLength() > 30) {
                                throw new NumberOfCoAuthorsExceededException("Number of authors exceed 30 co-authors for this publication.");
                            }

                            fr.inria.anhalytics.commons.entities.Document doc = new fr.inria.anhalytics.commons.entities.Document(currentAnhalyticsId, Utilities.getVersionFromURI(repositoryDocId), repositoryDocId, new ArrayList<Document_Identifier>());

                            processIdentifiers(ids, doc, repositoryDocId);
                            dd.create(doc);

                            pub.setDocument(doc);
                            // for some pub types we just keep the submission date.
                            pub.setDate_eletronic(submission_date.getTextContent());
                            pub.setDate_printed(Utilities.parseStringDate(submission_date.getTextContent()));
                            pub.setDoc_title(title.getTextContent().trim());
                            pub.setType(type.getTextContent());
                            pub.setLanguage(language.getTextContent());
                            processMonogr(monogr, pub);

                            pd.create(pub);
                            processPersons(authors, "author", pub, teiDoc, authorsFromfulltextTeiHeader);
                            processPersons(editors, "editor", pub, teiDoc, authorsFromfulltextTeiHeader);

                            logger.debug("#################################################################");
                        } catch (Exception xpe) {
                            xpe.printStackTrace();
                            adf.rollback();
                            teiDoc = null;
                        }
                        adf.endTransaction();
                        if (teiDoc != null) {
                            String generatedTeiString = Utilities.toString(teiDoc);
                            mm.updateTei(generatedTeiString, repositoryDocId, true);
                        }
                    }
                }
            }
            if (!KbProperties.isProcessByDate()) {
                break;
            }

        }
        DAOFactory.closeConnection();
        logger.info("DONE.");
    }

    private static void processIdentifiers(NodeList ids, fr.inria.anhalytics.commons.entities.Document doc, String halId) throws SQLException {
        String type = null;
        String id = null;
        List<Document_Identifier> dis = new ArrayList<Document_Identifier>();
        for (int i = ids.getLength() - 1; i >= 0; i--) {
            Node node = ids.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element identifierElt = (Element) node;
                type = identifierElt.getAttribute("type");
                id = node.getTextContent().trim();
            }
            Document_Identifier di = new Document_Identifier(id, type);
            dis.add(di);
        }
        Document_Identifier dihal = new Document_Identifier(halId, "hal");
        dis.add(dihal);
        doc.setDocument_Identifiers(dis);
    }

    private static void processMonogr(Element monogr, Publication pub) throws SQLException {
        MonographDAO md = (MonographDAO) adf.getMonographDAO();
        Conference_EventDAO ced = (Conference_EventDAO) adf.getConference_EventDAO();
        In_SerialDAO isd = (In_SerialDAO) adf.getIn_SerialDAO();
        PublisherDAO pd = (PublisherDAO) adf.getPublisherDAO();
        Monograph mn = new Monograph();
        Conference_Event ce = null;
        In_Serial is = new In_Serial();
        Journal journal = new Journal();
        Collection collection = new Collection();
        List<Serial_Identifier> serial_identifiers = new ArrayList<Serial_Identifier>();
        Publisher pls = new Publisher();
        NodeList content = monogr.getChildNodes();
        if (content.getLength() > 0) {
            for (int i = content.getLength() - 1; i >= 0; i--) {
                Node node = content.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element monogrChildElt = (Element) node;
                    if (monogrChildElt.getNodeName().equals("idno")) {
                        serial_identifiers.add(new Serial_Identifier(null, monogrChildElt.getTextContent().trim(), monogrChildElt.getAttribute("type"), journal, collection));
                    } else if (monogrChildElt.getNodeName().equals("title")) {
                        String type = monogrChildElt.getAttribute("level");
                        mn.setTitle(monogrChildElt.getTextContent().trim());
                        mn.setType(type);
                        if (type.equals("j")) {
                            journal.setTitle(monogrChildElt.getTextContent().trim());
                        } else {
                            collection.setTitle(monogrChildElt.getTextContent().trim());
                        }
                    } else if (monogrChildElt.getNodeName().equals("imprint")) {
                        NodeList imprint = monogrChildElt.getChildNodes();

                        for (int j = imprint.getLength() - 1; j >= 0; j--) {
                            Node entry = imprint.item(j);
                            if (entry.getNodeType() == Node.ELEMENT_NODE) {
                                Element imprintChildElt = (Element) entry;
                                if (imprintChildElt.getNodeName().equals("publisher")) {
                                    pls.setName(imprintChildElt.getTextContent().trim());
                                } else if (imprintChildElt.getNodeName().equals("date")) {
                                    String type = imprintChildElt.getAttribute("type");
                                    String date = imprintChildElt.getAttribute("when");
                                    if (type.equals("datePub") || type.equals("dateDefended")) {
                                        pub.setDate_eletronic(date);
                                        date = Utilities.completeDate(date);
                                        try {
                                            pub.setDate_printed(Utilities.parseStringDate(date));
                                        } catch (ParseException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                } else if (imprintChildElt.getNodeName().equals("biblScope")) {
                                    String unit = imprintChildElt.getAttribute("unit");
                                    if (unit.equals("serie")) {
                                        collection.setTitle(imprintChildElt.getTextContent().trim());
                                        if (journal.getTitle().isEmpty()) {
                                            journal.setTitle(imprintChildElt.getTextContent().trim());
                                        }
                                    } else if (unit.equals("volume")) {
                                        is.setVolume(imprintChildElt.getTextContent().trim());
                                    } else if (unit.equals("issue")) {
                                        is.setIssue(imprintChildElt.getTextContent().trim());
                                    } else if (unit.equals("pp")) {
                                        String pp = imprintChildElt.getTextContent().trim();
                                        if (pp.length() < 10) {
                                            if (pp.contains("-") && pp.length() > 3) {
                                                String[] pages = pp.split("-");
                                                pub.setStart_page(pages[0]);
                                                if (pages.length > 1) {
                                                    pub.setEnd_page(pages[1]);
                                                }
                                            } else {
                                                pub.setStart_page(pp);
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    } else if (monogrChildElt.getNodeName().equals("meeting")) {
                        ce = new Conference_Event();
                        ce.setConference(new Conference());
                        Address addr = new Address();
                        AddressDAO ad = (AddressDAO) adf.getAddressDAO();
                        NodeList meeting = node.getChildNodes();
                        for (int m = meeting.getLength() - 1; m >= 0; m--) {
                            Node meetingChild = meeting.item(m);
                            if (meeting.item(m).getNodeType() == Node.ELEMENT_NODE) {
                                Element meetingChildElt = (Element) meetingChild;
                                if (meetingChildElt.getNodeName().equals("title")) {
                                    ce.getConference().setTitle(meetingChildElt.getTextContent().trim());
                                } else if (meetingChildElt.getNodeName().equals("date")) {
                                    String type = meetingChildElt.getAttribute("type");
                                    if (type.equals("start")) {
                                        ce.setStart_date(meetingChildElt.getTextContent().trim());
                                    } else if (type.equals("end")) {
                                        ce.setEnd_date(meetingChildElt.getTextContent().trim());
                                    }
                                } else if (meetingChildElt.getNodeName().equals("settlement")) {
                                    addr.setSettlement(meetingChildElt.getTextContent().trim());
                                } else if (meetingChildElt.getNodeName().equals("country")) {
                                    Country c = new Country();
                                    c.setIso(meetingChildElt.getAttribute("key"));
                                    addr.setCountry(c);
                                } else if (meetingChildElt.getNodeName().equals("region")) {
                                    addr.setRegion(meetingChildElt.getTextContent().trim());
                                }
                            }
                        }
                        ad.create(addr);
                        ce.setAddress(addr);
                    }
                }
            }
            pd.create(pls);
            pub.setPublisher(pls);
            md.create(mn);
            if (ce != null) {
                ce.setMongoraph(mn);
                ced.create(ce);
            }
            pub.setMonograph(mn);
            is.setMg(mn);
            is.setJ(journal);
            is.setC(collection);

            isd.create(is);
            for (Serial_Identifier serial_identifier : serial_identifiers) {
                serial_identifier.setJournal(journal);
                serial_identifier.setCollection(collection);
                isd.createSerialIdentifier(serial_identifier);
            }
        }
    }

    private static Organisation parseOrg(Node orgNode, Organisation org, Document_Organisation document_organisation, Date pubDate, Document doc) throws SQLException {
        LocationDAO ld = (LocationDAO) adf.getLocationDAO();
        AddressDAO ad = (AddressDAO) adf.getAddressDAO();
        OrganisationDAO od = (OrganisationDAO) adf.getOrganisationDAO();
        Organisation organisationParent = new Organisation();
        List<Organisation_Identifier> ois = new ArrayList<Organisation_Identifier>();
        Location locationParent = null;
        PART_OF part_of = new PART_OF();
        if (orgNode.getNodeType() == Node.ELEMENT_NODE) {
            Element orgElt = (Element) orgNode;
            organisationParent.setType(orgElt.getAttribute("type"));
            if (orgElt.hasAttribute("xml:id")) {
                ois.add(new Organisation_Identifier(orgElt.getAttribute("xml:id"), "halId"));
                org.setStructure(orgElt.getAttribute("xml:id"));
            }
            if (orgElt.hasAttribute("status")) {
                organisationParent.setStatus(orgElt.getAttribute("status"));
            }
            NodeList nlorg = orgElt.getChildNodes();
            for (int o = nlorg.getLength() - 1; o >= 0; o--) {
                Node ndorg = nlorg.item(o);
                if (ndorg.getNodeType() == Node.ELEMENT_NODE) {
                    Element orgChildElt = (Element) ndorg;
                    if (orgChildElt.getNodeName().equals("orgName")) {
                        organisationParent.addName(new Organisation_Name(null, orgChildElt.getTextContent().trim(), pubDate));
                    } else if (orgChildElt.getNodeName().equals("desc")) {
                        NodeList descorgChilds = ndorg.getChildNodes();
                        for (int l = descorgChilds.getLength() - 1; l >= 0; l--) {
                            Node descChild = descorgChilds.item(l);
                            if (descChild.getNodeType() == Node.ELEMENT_NODE) {
                                Element descChildElt = (Element) descChild;
                                if (descChildElt.getNodeName().equals("address")) {
                                    locationParent = processAddress(descChildElt);

                                } else if (descChildElt.getNodeName().equals("ref")) {
                                    String type = descChildElt.getAttribute("type");
                                    if (type.equals("url")) {
                                        organisationParent.setUrl(descChildElt.getTextContent().trim());
                                    }
                                }
                            }
                        }
                    } else if (orgChildElt.getNodeName().equals("org")) {
                        organisationParent = parseOrg(orgChildElt, organisationParent, document_organisation, pubDate, doc);
                    } else if (orgChildElt.getNodeName().equals("ref")) {
                        String descReftype = orgChildElt.getAttribute("type");
                        if (descReftype.equals("url")) {
                            organisationParent.setUrl(orgChildElt.getTextContent().trim());
                        }
                    } else if (orgChildElt.getNodeName().equals("idno")) {
                        String id_type = orgChildElt.getAttribute("type");
                        String id_value = orgChildElt.getTextContent().trim();
                        if (id_type.equals("anhalyticsID")) {
                            orgNode.removeChild(orgChildElt);
                        } else {
                            organisationParent.getOrganisation_identifiers().add(new Organisation_Identifier(id_value, id_type));
                        }
                    }
                }
            }
            organisationParent.setOrganisation_identifiers(ois);
            part_of.setBeginDate(pubDate);
            od.create(organisationParent);
            document_organisation.addOrg(organisationParent);
            part_of.setOrganisation_mother(organisationParent);
            org.addRel(part_of);

            if (locationParent != null && locationParent.getAddress() != null) {
                if (locationParent.getAddress().getAddressId() == null) {
                    
                    ad.create(locationParent.getAddress());
                }
                locationParent.setBegin_date(pubDate);
                locationParent.setEnd_date(pubDate);
                locationParent.setOrganisation(organisationParent);
                ld.create(locationParent);
            }
            // how to handle it for grobid case ?
            Element idno = doc.createElement("idno");
            idno.setAttribute("type", "anhalyticsID");
            idno.setTextContent(Long.toString(organisationParent.getOrganisationId()));
            orgNode.appendChild(idno);
        }
        return org;
    }

    private static void parseAffiliationOrg(Affiliation affiliation, Publication pub, Element orgElt, Document doc) throws SQLException {
        Document_Organisation document_organisation = new Document_Organisation();
        Document_OrganisationDAO d_o = (Document_OrganisationDAO) adf.getDocument_OrganisationDAO();
        document_organisation.setDoc(pub.getDocument());
        Organisation organisation = new Organisation();
        OrganisationDAO od = (OrganisationDAO) adf.getOrganisationDAO();
        Location location = null;
        LocationDAO ld = (LocationDAO) adf.getLocationDAO();
        AddressDAO ad = (AddressDAO) adf.getAddressDAO();
        NodeList nodes = orgElt.getChildNodes();

        for (int o = nodes.getLength() - 1; o >= 0; o--) {

            Node ndorg = nodes.item(o);
            if (ndorg.getNodeType() == Node.ELEMENT_NODE) {
                Element childElt = (Element) ndorg;
                if (childElt.getNodeName().equals("orgName")) {
                    organisation.addName(new Organisation_Name(null, childElt.getTextContent().trim(), pub.getDate_printed()));
                } else if (childElt.getNodeName().equals("desc")) {
                    NodeList descorgChilds = ndorg.getChildNodes();
                    for (int l = descorgChilds.getLength() - 1; l >= 0; l--) {
                        Node descChild = descorgChilds.item(l);
                        if (descChild.getNodeType() == Node.ELEMENT_NODE) {
                            Element descChildElt = (Element) descChild;
                            if (descChildElt.getNodeName().equals("address")) {
                                location = processAddress(descChildElt);
                            } else if (descChildElt.getNodeName().equals("ref")) {
                                String type = descChildElt.getAttribute("type");
                                if (type.equals("url")) {
                                    organisation.setUrl(descChildElt.getTextContent().trim());
                                }
                            }
                        }
                    }
                } else if (childElt.getNodeName().equals("org")) {
                    organisation = parseOrg(childElt, organisation, document_organisation, pub.getDate_printed(), doc);
                } else if (childElt.getNodeName().equals("ref")) {
                    String descReftype = childElt.getAttribute("type");
                    if (descReftype.equals("url")) {
                        organisation.setUrl(childElt.getTextContent().trim());
                    }
                } else if (childElt.getNodeName().equals("idno")) {
                    String id_type = childElt.getAttribute("type");
                    String id_value = childElt.getTextContent().trim();
                    if (id_type.equals("anhalyticsID")) {
                        orgElt.removeChild(childElt);
                    } else {
                        organisation.getOrganisation_identifiers().add(new Organisation_Identifier(id_value, id_type));
                    }
                }
            }
        }
        organisation.setType(orgElt.getAttribute("type"));
        if (orgElt.hasAttribute("xml:id")) {
            organisation.getOrganisation_identifiers().add(new Organisation_Identifier(orgElt.getAttribute("xml:id"), "halId"));
        }
        if (orgElt.hasAttribute("status")) {
            organisation.setStatus(orgElt.getAttribute("status"));
        }
        od.create(organisation);
        document_organisation.addOrg(organisation);
        d_o.create(document_organisation);
        affiliation.addOrganisation(organisation);
        affiliation.setBegin_date(pub.getDate_printed());
        affiliation.setEnd_date(pub.getDate_printed());
        if (location != null && location.getAddress() != null) {
            if (location.getAddress().getAddressId() == null) {
                ad.create(location.getAddress());
            }
            location.setBegin_date(pub.getDate_printed());
            location.setEnd_date(pub.getDate_printed());
            location.setOrganisation(organisation);
            ld.create(location);
        }

        // how to handle it for grobid case ?
        Element idno = doc.createElement("idno");
        idno.setAttribute("type", "anhalyticsID");
        idno.setTextContent(Long.toString(organisation.getOrganisationId()));
        orgElt.appendChild(idno);
    }

    private static void processPersons(NodeList persons, String type, Publication pub, Document doc, NodeList authorsFromfulltextTeiHeader) throws SQLException {
        Node person = null;
        PersonDAO pd = (PersonDAO) adf.getPersonDAO();
        Person prs = null;
        Affiliation affiliation = null;
        AffiliationDAO affd = (AffiliationDAO) adf.getAffiliationDAO();
        Date pubDate = pub.getDate_printed();
        for (int i = persons.getLength() - 1; i >= 0; i--) {
            person = persons.item(i);

            if (person.getNodeType() == Node.ELEMENT_NODE) {
                Element personElt = (Element) person;
                if (personElt.getElementsByTagName("persName").getLength() > 0) {
                    NodeList theNodes = person.getChildNodes();
                    if (theNodes.getLength() > 0) {
                        prs = new Person();
                        affiliation = new Affiliation();
                        List<Person_Name> prs_names = new ArrayList<Person_Name>();
                        List<Person_Identifier> pis = new ArrayList<Person_Identifier>();
                        NodeList nodes = null;
                        for (int y = theNodes.getLength() - 1; y >= 0; y--) {
                            Node node = theNodes.item(y);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                Element personChildElt = (Element) node;
                                if (personChildElt.getNodeName().equals("persName")) {
                                    Person_Name prs_name = new Person_Name();
                                    nodes = personChildElt.getChildNodes();
                                    for (int z = nodes.getLength() - 1; z >= 0; z--) {
                                        if (nodes.item(z).getNodeName().equals("forename")) {
                                            prs_name.setForename(nodes.item(z).getTextContent().trim());
                                        } else if (nodes.item(z).getNodeName().equals("surname")) {
                                            prs_name.setSurname(nodes.item(z).getTextContent().trim());
                                        }
                                    }
                                    String fullname = prs_name.getForename();
                                    if (!prs_name.getMiddlename().isEmpty()) {
                                        fullname += " " + prs_name.getMiddlename();
                                    }
                                    if (!prs_name.getSurname().isEmpty()) {
                                        fullname += " " + prs_name.getSurname();
                                    }
                                    prs_name.setFullname(fullname);
                                    prs_name.setPublication_date(pubDate);
                                    prs_names.add(prs_name);
                                } else if (personChildElt.getNodeName().equals("email")) {
                                    prs.setEmail(personChildElt.getTextContent().trim());
                                } else if (personChildElt.getNodeName().equals("ptr")) {
                                    if (personChildElt.getAttribute("type").equals("url")) {
                                        prs.setUrl(personChildElt.getAttribute("target"));
                                    }
                                } else if (personChildElt.getNodeName().equals("affiliation")) {
                                    NodeList nl = personChildElt.getChildNodes();
                                    Element org = null;
                                    Node n = null;
                                    for (int z = nl.getLength() - 1; z >= 0; z--) {
                                        n = nl.item(z);
                                        if (n.getNodeType() == Node.ELEMENT_NODE) {
                                            if (n.getNodeName().equals("org")) {
                                                org = (Element) n;
                                                break;
                                            }
                                        }
                                    }
                                    parseAffiliationOrg(affiliation, pub, org, doc);
                                } else if (personChildElt.getNodeName().equals("idno")) {
                                    Person_Identifier pi = new Person_Identifier();
                                    String id_type = personChildElt.getAttribute("type");
                                    String id_value = personChildElt.getTextContent().trim();
                                    if (id_type.equals("anhalyticsID")) {
                                        person.removeChild(personChildElt);
                                    } else {
                                        pi.setId(id_value);
                                        pi.setType(id_type);
                                        pis.add(pi);
                                    }
                                }
                                //person.removeChild(node);
                            }
                        }
                        prs.setPerson_names(prs_names);

                        prs.setPerson_identifiers(pis);

                        if (type.equals("author")) {
                            Author author = new Author(pub.getDocument(), prs, 0, 0);
                            pd.createAuthor(author);
                            if (affiliation.getOrganisations() != null) {
                                affiliation.setPerson(prs);
                                affd.create(affiliation);
                            }
                        } else if (type.equals("editor")) {
                            Editor editor = new Editor(0, prs, pub);
                            pd.createEditor(editor);
                        }

                        Element idno = doc.createElement("idno");
                        idno.setAttribute("type", "anhalyticsID");
                        idno.setTextContent(Long.toString(prs.getPersonId()));
                        person.appendChild(idno);
                    }
                }
            }
        }

    }

    private static Location processAddress(Element addressElt) {

        Location location = new Location();
        Address addr = new Address();
        NodeList address = addressElt.getChildNodes();
        for (int x = address.getLength() - 1; x >= 0; x--) {

            Node addrChild = address.item(x);

            if (addrChild.getNodeType() == Node.ELEMENT_NODE) {
                Element addrChildElt = (Element) addrChild;
                if (addrChildElt.getNodeName().equals("addrLine")) {
                    addr.setAddrLine(addrChildElt.getTextContent().trim());
                } else if (addrChildElt.getNodeName().equals("country")) {
                    addr.setCountry(new Country(null, addrChildElt.getAttribute("key")));
                } else if (addrChildElt.getNodeName().equals("settlement")) {
                    addr.setSettlement(addrChildElt.getTextContent().trim());
                } else if (addrChildElt.getNodeName().equals("postCode")) {
                    addr.setPostCode(addrChildElt.getTextContent().trim());
                } else if (addrChildElt.getNodeName().equals("region")) {
                    addr.setRegion(addrChildElt.getTextContent().trim());
                }
            }
        }
        location.setAddress(addr);
        return location;
    }

    /**
     *
     */
    public void processCitations() throws SQLException {
        BiblioDAOFactory.initConnection();
        DocumentDAO dd = (DocumentDAO) abdf.getDocumentDAO();

        for (String date : Utilities.getDates()) {
            if (!KbProperties.isProcessByDate()) {
                date = null;
            }
            if (mm.initTeis(date, true, MongoCollectionsInterface.FINAL_TEIS)) {
                while (mm.hasMoreTeis()) {
                    String teiString = mm.nextTeiDocument();
                    String repositoryDocId = mm.getCurrentRepositoryDocId();
                    String anhalyticsId = mm.getCurrentAnhalyticsId();
                    if (anhalyticsId == null || anhalyticsId.isEmpty()) {
                        logger.info("skipping " + repositoryDocId + " No anHALytics id provided");
                        continue;
                    }
                    if (!dd.isCitationsMined(anhalyticsId)) {
                        logger.info("Extracting :" + repositoryDocId);
                        abdf.openTransaction();
                        try {
                            InputStream teiStream = new ByteArrayInputStream(teiString.getBytes());
                            Document teiDoc = getDocument(teiStream);
                            teiStream.close();
                            Node citations = (Node) xPath.compile("/teiCorpus/TEI/text/back/div[@type='references']/listBibl").evaluate(teiDoc, XPathConstants.NODE);
                            if (citations != null) {
                                NodeList references = citations.getChildNodes();
                                fr.inria.anhalytics.commons.entities.Document doc = new fr.inria.anhalytics.commons.entities.Document(anhalyticsId, Utilities.getVersionFromURI(repositoryDocId), repositoryDocId, new ArrayList<Document_Identifier>());
                                dd.create(doc);

                                for (int j = 0; j < references.getLength() - 1; j++) {
                                    Node reference = references.item(j);
                                    if (reference.getNodeType() == Node.ELEMENT_NODE) {
                                        processBiblStruct((Element) reference, doc);
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
            if (!KbProperties.isProcessByDate()) {
                break;
            }
        }
        BiblioDAOFactory.closeConnection();
    }

    private void processBiblStruct(Element reference, fr.inria.anhalytics.commons.entities.Document doc) throws SQLException {
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
                        pub.setDoc_title(analyticChildElt.getTextContent().trim());
                        pub.setType(analyticChildElt.getAttribute("level"));
                    } else if (analyticChildElt.getNodeName().equals("author")) {
                        if (analyticChildElt.getElementsByTagName("persName").getLength() > 0) {
                            Person prs = new Person();
                            List<Person_Name> prs_names = new ArrayList<Person_Name>();
                            List<Person_Identifier> pis = new ArrayList<Person_Identifier>();
                            NodeList authorChilds = analyticChilds.item(z).getChildNodes();
                            for (int y = authorChilds.getLength() - 1; y >= 0; y--) {
                                Node authorChild = authorChilds.item(y);
                                if (authorChild.getNodeType() == Node.ELEMENT_NODE) {
                                    Element authorChildElt = (Element) authorChild;
                                    if (authorChildElt.getNodeName().equals("persName")) {
                                        Person_Name prs_name = new Person_Name();
                                        NodeList persNameChilds = authorChildElt.getChildNodes();
                                        for (int o = persNameChilds.getLength() - 1; o >= 0; o--) {
                                            Node persNameChild = persNameChilds.item(o);
                                            if (persNameChild.getNodeType() == Node.ELEMENT_NODE) {
                                                Element persNameChildElt = (Element) persNameChild;
                                                if (persNameChildElt.getNodeName().equals("forename")) {
                                                    prs_name.setForename(persNameChildElt.getTextContent().trim());
                                                } else if (persNameChildElt.getNodeName().equals("surname")) {
                                                    prs_name.setSurname(persNameChildElt.getTextContent().trim());
                                                }
                                            }
                                        }
                                        String fullname = prs_name.getForename();
                                        if (!prs_name.getMiddlename().isEmpty()) {
                                            fullname += " " + prs_name.getMiddlename();
                                        }
                                        if (!prs_name.getSurname().isEmpty()) {
                                            fullname += " " + prs_name.getSurname();
                                        }
                                        prs_name.setFullname(fullname);
                                        //prs_name.setPublication_date(pubDate);
                                        prs_names.add(prs_name);
                                    } else if (authorChildElt.getNodeName().equals("email")) {
                                        prs.setEmail(authorChildElt.getTextContent().trim());
                                    } else if (authorChildElt.getNodeName().equals("ptr")) {
                                        if (authorChildElt.getAttribute("type").equals("url")) {
                                            prs.setUrl(authorChildElt.getAttribute("type"));
                                        }
                                    } else if (authorChildElt.getNodeName().equals("idno")) {
                                        Person_Identifier pi = new Person_Identifier();
                                        String id_type = authorChildElt.getAttribute("type");
                                        String id_value = authorChildElt.getTextContent().trim();
                                        pi.setId(id_value);
                                        pi.setType(id_type);
                                        pis.add(pi);
                                    }
                                }
                            }
                            prs.setPerson_names(prs_names);
                            prs.setPerson_identifiers(pis);
                            prss.add(prs);
                        }
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
                        mn.setTitle(monogrChildElt.getTextContent().trim());
                        if (analytic == null) {
                            pub.setDoc_title(monogrChildElt.getTextContent().trim());
                        }
                        mn.setType(monogrChildElt.getAttribute("level"));
                        if (analytic == null) {
                            pub.setType(monogrChildElt.getAttribute("level"));
                        }
                        if (monogrChildElt.getTextContent().trim() != null) {
                            if (monogrChildElt.getAttribute("level").equals("j")) {
                                journal = new Journal(null, monogrChildElt.getTextContent().trim());
                            } else {
                                collection = new Collection(null, monogrChildElt.getTextContent().trim());
                            }
                        }
                    } else if (monogrChildElt.getNodeName().equals("idno")) {
                        serial_identifiers.add(new Serial_Identifier(null, monogrChildElt.getTextContent().trim(), monogrChildElt.getAttribute("type"), journal, collection));
                    } else if (monogrChildElt.getNodeName().equals("imprint")) {
                        NodeList imprintChilds = monogrChildElt.getChildNodes();
                        is = new In_Serial();
                        for (int j = imprintChilds.getLength() - 1; j >= 0; j--) {
                            Node imprintChild = imprintChilds.item(j);
                            if (imprintChild.getNodeType() == Node.ELEMENT_NODE) {
                                Element imprintChildElt = (Element) imprintChild;
                                if (imprintChildElt.getNodeName().equals("publisher")) {
                                    Publisher pls = new Publisher(null, imprintChildElt.getTextContent().trim());
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
                                        collection.setTitle(imprintChildElt.getTextContent().trim());
                                    } else if (unit.equals("volume")) {
                                        is.setVolume(imprintChildElt.getTextContent().trim());
                                    } else if (unit.equals("issue")) {
                                        is.setIssue(imprintChildElt.getTextContent().trim());
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
                                    ce.getConference().setTitle(meetingChildElt.getTextContent().trim());
                                } else if (meetingChildElt.getNodeName().equals("date")) {
                                    String type = meetingChildElt.getAttribute("type");
                                    String date = meetingChildElt.getAttribute("when");
                                    if (type.equals("start")) {
                                        ce.setStart_date(date);
                                    } else if (type.equals("end")) {
                                        ce.setEnd_date(date);
                                    }
                                } else if (meetingChildElt.getNodeName().equals("settlement")) {
                                    addr.setSettlement(meetingChildElt.getTextContent().trim());
                                } else if (meetingChildElt.getNodeName().equals("country")) {
                                    Country country = new Country();
                                    country.setIso(meetingChildElt.getAttribute("key"));
                                    addr.setCountry(country);
                                } else if (meetingChildElt.getNodeName().equals("region")) {
                                    addr.setRegion(meetingChildElt.getTextContent().trim());
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
            for (Person_Name pn : p.getPerson_names()) {
                pn.setPublication_date(pub.getDate_printed());
            }
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
