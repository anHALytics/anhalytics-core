package fr.inria.anhalytics.kb.datamine;

import fr.inria.anhalytics.commons.exceptions.NumberOfCoAuthorsExceededException;
import fr.inria.anhalytics.commons.utilities.JaroWinkler;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.dao.AbstractDAOFactory;
import fr.inria.anhalytics.dao.AddressDAO;
import fr.inria.anhalytics.kb.dao.anhalytics.AffiliationDAO;
import fr.inria.anhalytics.dao.Conference_EventDAO;
import fr.inria.anhalytics.kb.dao.anhalytics.LocationDAO;
import fr.inria.anhalytics.dao.DocumentDAO;
import fr.inria.anhalytics.dao.Document_OrganisationDAO;
import fr.inria.anhalytics.kb.dao.anhalytics.Document_IdentifierDAO;
import fr.inria.anhalytics.dao.In_SerialDAO;
import fr.inria.anhalytics.dao.MonographDAO;
import fr.inria.anhalytics.kb.dao.anhalytics.OrganisationDAO;
import fr.inria.anhalytics.dao.PersonDAO;
import fr.inria.anhalytics.dao.PublicationDAO;
import fr.inria.anhalytics.dao.PublisherDAO;
import fr.inria.anhalytics.kb.dao.anhalytics.DAOFactory;
import fr.inria.anhalytics.kb.entities.Address;
import fr.inria.anhalytics.kb.entities.Affiliation;
import fr.inria.anhalytics.kb.entities.Author;
import fr.inria.anhalytics.kb.entities.Collection;
import fr.inria.anhalytics.kb.entities.Conference;
import fr.inria.anhalytics.kb.entities.Conference_Event;
import fr.inria.anhalytics.kb.entities.Country;
import fr.inria.anhalytics.kb.entities.Document_Organisation;
import fr.inria.anhalytics.kb.entities.Editor;
import fr.inria.anhalytics.kb.entities.In_Serial;
import fr.inria.anhalytics.kb.entities.Journal;
import fr.inria.anhalytics.kb.entities.Location;
import fr.inria.anhalytics.kb.entities.Monograph;
import fr.inria.anhalytics.kb.entities.Organisation;
import fr.inria.anhalytics.kb.entities.Organisation_Name;
import fr.inria.anhalytics.kb.entities.PART_OF;
import fr.inria.anhalytics.kb.entities.Person;
import fr.inria.anhalytics.kb.entities.Person_Identifier;
import fr.inria.anhalytics.kb.entities.Publication;
import fr.inria.anhalytics.kb.entities.Publisher;
import fr.inria.anhalytics.kb.entities.Serial_Identifier;
import fr.inria.anhalytics.kb.properties.KbProperties;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Format and Extract HAL metadata to seed the KB.
 *
 * @author Achraf
 */
public class HALMiner extends Miner {

    private static final Logger logger = LoggerFactory.getLogger(HALMiner.class);

    private static final AbstractDAOFactory adf = AbstractDAOFactory.getFactory(AbstractDAOFactory.DAO_FACTORY);

    private static XPath xPath = XPathFactory.newInstance().newXPath();

    public HALMiner() throws UnknownHostException {
        super();
    }

    /**
     * Initiates HAL knowledge base and creates working corpus TEI.
     */
    public void initKnowledgeBase() {
        DAOFactory.initConnection();
        PublicationDAO pd = (PublicationDAO) adf.getPublicationDAO();
        DocumentDAO dd = (DocumentDAO) adf.getDocumentDAO();

        for (String date : Utilities.getDates()) {

            if (!KbProperties.isProcessByDate()) {
                date = null;
            }
            if (mm.initTeis(date, true)) {
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
                            NodeList monogr = (NodeList) xPath.compile(TeiPaths.MonogrElement).evaluate(teiDoc, XPathConstants.NODESET);
                            NodeList ids = (NodeList) xPath.compile(TeiPaths.IdnoElement).evaluate(teiDoc, XPathConstants.NODESET);
                            logger.info("Extracting :" + repositoryDocId);
                            if (authors.getLength() > 20) {
                                throw new NumberOfCoAuthorsExceededException("Number of authors exceed 20 co-authors for this publication.");
                            }
                            fr.inria.anhalytics.kb.entities.Document doc = new fr.inria.anhalytics.kb.entities.Document(currentAnhalyticsId, Utilities.getVersionFromURI(repositoryDocId), repositoryDocId);

                            dd.create(doc);

                            pub.setDocument(doc);
                            // for some pub types we just keep the submission date.
                            pub.setDate_eletronic(submission_date.getTextContent());
                            pub.setDate_printed(Utilities.parseStringDate(submission_date.getTextContent()));
                            pub.setDoc_title(title.getTextContent());
                            pub.setType(type.getTextContent());
                            pub.setLanguage(language.getTextContent());

                            processMonogr(monogr, pub);

                            pd.create(pub);
                            processPersons(authors, "author", pub, teiDoc, authorsFromfulltextTeiHeader);
                            processPersons(editors, "editor", pub, teiDoc, authorsFromfulltextTeiHeader);
                            processIdentifiers(ids, doc);

                            logger.debug("#################################################################");
                        } catch (Exception xpe) {
                            adf.rollback();
                            teiDoc = null;
                            xpe.printStackTrace();
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

    private static void processIdentifiers(NodeList ids, fr.inria.anhalytics.kb.entities.Document doc) throws SQLException {
        String type = null;
        String id = null;
        Document_IdentifierDAO did = (Document_IdentifierDAO) adf.getDocument_IdentifierDAO();
        for (int i = ids.getLength() - 1; i >= 0; i--) {
            Node node = ids.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element identifierElt = (Element) node;
                type = identifierElt.getAttribute("type");
                id = node.getTextContent();
            }
            fr.inria.anhalytics.kb.entities.Document_Identifier di = new fr.inria.anhalytics.kb.entities.Document_Identifier(null, id, type, doc);
            did.create(di);
        }
        fr.inria.anhalytics.kb.entities.Document_Identifier dihal = new fr.inria.anhalytics.kb.entities.Document_Identifier(null, doc.getUri(), "hal", doc);
        did.create(dihal);
    }

    private static void processMonogr(NodeList monogr, Publication pub) throws SQLException {
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
        NodeList content = monogr.item(0).getChildNodes();
        for (int i = content.getLength() - 1; i >= 0; i--) {
            Node node = content.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element monogrChildElt = (Element) node;
                if (monogrChildElt.getNodeName().equals("idno")) {
                    serial_identifiers.add(new Serial_Identifier(null, monogrChildElt.getTextContent(), monogrChildElt.getAttribute("type"), journal, collection));
                } else if (monogrChildElt.getNodeName().equals("title")) {
                    String type = monogrChildElt.getAttribute("level");
                    mn.setTitle(monogrChildElt.getTextContent());
                    mn.setType(type);
                    if (type.equals("j")) {
                        journal.setTitle(monogrChildElt.getTextContent());
                    } else {
                        collection.setTitle(monogrChildElt.getTextContent());
                    }
                } else if (monogrChildElt.getNodeName().equals("imprint")) {
                    NodeList imprint = monogrChildElt.getChildNodes();

                    for (int j = imprint.getLength() - 1; j >= 0; j--) {
                        Node entry = imprint.item(j);
                        if (entry.getNodeType() == Node.ELEMENT_NODE) {
                            Element imprintChildElt = (Element) entry;
                            if (imprintChildElt.getNodeName().equals("publisher")) {
                                pls.setName(imprintChildElt.getTextContent());
                            } else if (imprintChildElt.getNodeName().equals("date")) {
                                String type = imprintChildElt.getAttribute("type");
                                String date = imprintChildElt.getTextContent();
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
                                    collection.setTitle(imprintChildElt.getTextContent());
                                    if (journal.getTitle().isEmpty()) {
                                        journal.setTitle(imprintChildElt.getTextContent());
                                    }
                                } else if (unit.equals("volume")) {
                                    is.setVolume(imprintChildElt.getTextContent());
                                } else if (unit.equals("issue")) {
                                    is.setIssue(imprintChildElt.getTextContent());
                                } else if (unit.equals("pp")) {
                                    String pp = imprintChildElt.getTextContent();
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
                                ce.getConference().setTitle(meetingChildElt.getTextContent());
                            } else if (meetingChildElt.getNodeName().equals("date")) {
                                String type = meetingChildElt.getAttribute("type");
                                if (type.equals("start")) {
                                    ce.setStart_date(meetingChildElt.getTextContent());
                                } else if (type.equals("end")) {
                                    ce.setEnd_date(meetingChildElt.getTextContent());
                                }
                            } else if (meetingChildElt.getNodeName().equals("settlement")) {
                                addr.setSettlement(meetingChildElt.getTextContent());
                            } else if (meetingChildElt.getNodeName().equals("country")) {
                                Country c = new Country();
                                addr.setCountryStr(meetingChildElt.getTextContent());
                                c.setIso(meetingChildElt.getAttribute("key"));
                                addr.setCountry(c);
                            } else if (meetingChildElt.getNodeName().equals("region")) {
                                addr.setRegion(meetingChildElt.getTextContent());
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

    private static Organisation parseOrg(Node orgNode, Organisation org, Document_Organisation document_organisation, Date pubDate) throws SQLException {
        LocationDAO ld = (LocationDAO) adf.getLocationDAO();
        AddressDAO ad = (AddressDAO) adf.getAddressDAO();
        OrganisationDAO od = (OrganisationDAO) adf.getOrganisationDAO();
        Organisation organisationParent = new Organisation();
        Location locationParent = null;
        Address addrParent = null;
        PART_OF part_of = new PART_OF();
        if (orgNode.getNodeType() == Node.ELEMENT_NODE) {
            Element orgElt = (Element) orgNode;

            organisationParent.setType(orgElt.getAttribute("type"));
            organisationParent.setStructure(orgElt.getAttribute("xml:id"));
            NodeList nlorg = orgElt.getChildNodes();
            for (int o = nlorg.getLength() - 1; o >= 0; o--) {
                Node ndorg = nlorg.item(o);
                if (ndorg.getNodeType() == Node.ELEMENT_NODE) {
                    Element orgChildElt = (Element) ndorg;
                    if (orgChildElt.getNodeName().equals("orgName")) {
                        organisationParent.addName(new Organisation_Name(orgChildElt.getTextContent(), pubDate));
                    } else if (orgChildElt.getNodeName().equals("desc")) {
                        NodeList descorgChilds = ndorg.getChildNodes();
                        for (int l = descorgChilds.getLength() - 1; l >= 0; l--) {
                            Node descChild = descorgChilds.item(l);
                            if (descChild.getNodeType() == Node.ELEMENT_NODE) {
                                Element descChildElt = (Element) descChild;
                                if (descChildElt.getNodeName().equals("address")) {
                                    NodeList addressChilds = descChildElt.getChildNodes();
                                    addrParent = new Address();
                                    locationParent = new Location();
                                    for (int x = addressChilds.getLength() - 1; x >= 0; x--) {
                                        Node addrorgnode = addressChilds.item(x);
                                        if (addrorgnode.getNodeType() == Node.ELEMENT_NODE) {
                                            Element addrChildElt = (Element) addrorgnode;
                                            if (addrChildElt.getNodeName().equals("addrLine")) {
                                                addrParent.setAddrLine(addrChildElt.getTextContent());
                                            } else if (addrChildElt.getNodeName().equals("country")) {
                                                addrParent.setCountry(new Country(null, addrChildElt.getAttribute("key")));
                                            } else if (addrChildElt.getNodeName().equals("settlement")) {
                                                addrParent.setSettlement(addrChildElt.getTextContent());
                                            } else if (addrChildElt.getNodeName().equals("postCode")) {
                                                addrParent.setPostCode(addrChildElt.getTextContent());
                                            } else if (addrChildElt.getNodeName().equals("region")) {
                                                addrParent.setRegion(addrChildElt.getTextContent());
                                            }
                                        }
                                    }
                                } else if (descChildElt.getNodeName().equals("ref")) {
                                    String type = descChildElt.getAttribute("type");
                                    if (type.equals("url")) {
                                        organisationParent.setUrl(descChildElt.getTextContent());
                                    }
                                }
                            }
                        }
                    } else if (orgChildElt.getNodeName().equals("org")) {
                        organisationParent = parseOrg(orgChildElt, organisationParent, document_organisation, pubDate);
                    }
                }
            }
            part_of.setBeginDate(pubDate);
            organisationParent.setPublication_date(pubDate);
            od.create(organisationParent);
            document_organisation.addOrg(organisationParent);
            part_of.setOrganisation_mother(organisationParent);
            org.addRel(part_of);

            if (addrParent != null) {
                ad.create(addrParent);
                locationParent.setAddress(addrParent);
                locationParent.setBegin_date(pubDate);
                locationParent.setOrganisation(organisationParent);
                ld.create(locationParent);
            }

        }
        return org;
    }

    private static void curateAffiliation(Affiliation affiliation, Publication pub, Element affiliationElt, boolean isGrobid) throws SQLException {
        Document_Organisation document_organisation = new Document_Organisation();
        Document_OrganisationDAO d_o = (Document_OrganisationDAO) adf.getDocument_OrganisationDAO();
        document_organisation.setDoc(pub.getDocument());
        List<Organisation> organisations = new ArrayList<Organisation>();
        Organisation organisation = new Organisation();
        OrganisationDAO od = (OrganisationDAO) adf.getOrganisationDAO();
        Location location = null;
        LocationDAO ld = (LocationDAO) adf.getLocationDAO();
        AddressDAO ad = (AddressDAO) adf.getAddressDAO();
        NodeList nodes = affiliationElt.getChildNodes();

        for (int o = nodes.getLength() - 1; o >= 0; o--) {

            Node ndorg = nodes.item(o);
            if (ndorg.getNodeType() == Node.ELEMENT_NODE) {
                Element childElt = (Element) ndorg;
                if (childElt.getNodeName().equals("org")) {
                    organisation = parseOrg(childElt, organisation, document_organisation, pub.getDate_printed());
                } else if (childElt.getNodeName().equals("address")) {

                    location = processAddress(childElt);
                } else if (childElt.getNodeName().equals("ref")) {
                    String descReftype = childElt.getAttribute("type");
                    if (descReftype.equals("url")) {
                        organisation.setUrl(childElt.getTextContent());

                    }
                } else if (childElt.getNodeName().equals("orgName")) {

                    if (isGrobid) {
                        Organisation organisation1 = new Organisation();
                        if (organisation.getType().isEmpty() && childElt.hasAttribute("type")) {
                            organisation1.setUrl(organisation.getUrl());
                            organisation1.setType(childElt.getAttribute("type"));
                            organisation1.addName(new Organisation_Name(childElt.getTextContent(), pub.getDate_printed()));
                            organisation1.setPublication_date(pub.getDate_printed());
                        }
                        organisations.add(organisation1);
                    } else {
                        organisation.addName(new Organisation_Name(childElt.getTextContent(), pub.getDate_printed()));
                        organisation.setPublication_date(pub.getDate_printed());
                    }
                }
            }
        }
        if (!isGrobid) {
            organisation.setType(affiliationElt.getAttribute("type"));
            organisation.setStructure(affiliationElt.getAttribute("xml:id"));
            organisations.add(organisation);
        }
        for (Organisation org : organisations) {
            od.create(org);

            document_organisation.addOrg(org);
            d_o.create(document_organisation);
            affiliation.addOrganisation(org);
            affiliation.setBegin_date(pub.getDate_printed());
            if (location != null && location.getAddress() != null) {
                if (location.getAddress().getAddressId() == null) {
                    ad.create(location.getAddress());
                }
                location.setBegin_date(pub.getDate_printed());
                location.setOrganisation(org);
                ld.create(location);
            }
        }
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
            prs = new Person();
            prs.setPublication_date(pubDate);
            affiliation = new Affiliation();
            List<Person_Identifier> pis = new ArrayList<Person_Identifier>();

            if (person.getNodeType() == Node.ELEMENT_NODE) {
                NodeList theNodes = person.getChildNodes();
                NodeList nodes = null;
                boolean isAffiliated = false;
                for (int y = theNodes.getLength() - 1; y >= 0; y--) {
                    Node node = theNodes.item(y);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element personChildElt = (Element) node;
                        if (personChildElt.getNodeName().equals("persName")) {
                            nodes = personChildElt.getChildNodes();
                            for (int z = nodes.getLength() - 1; z >= 0; z--) {
                                if (nodes.item(z).getNodeName().equals("forename")) {
                                    prs.setForename(nodes.item(z).getTextContent());
                                } else if (nodes.item(z).getNodeName().equals("surname")) {
                                    prs.setSurname(nodes.item(z).getTextContent());
                                }
                            }
                        } else if (personChildElt.getNodeName().equals("email")) {
                            prs.setEmail(personChildElt.getTextContent());
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
                            curateAffiliation(affiliation, pub, org, false);
                            isAffiliated = true;
                        } else if (personChildElt.getNodeName().equals("idno")) {
                            Person_Identifier pi = new Person_Identifier();
                            String id_type = personChildElt.getAttribute("type");
                            String id_value = personChildElt.getTextContent();
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
                String fullname = prs.getForename();
                if (!prs.getMiddlename().isEmpty()) {
                    fullname += " " + prs.getMiddlename();
                }
                if (!prs.getForename().isEmpty()) {
                    fullname += " " + prs.getSurname();
                }
                prs.setFullname(fullname);

                if (!isAffiliated && type.equals("author")) {
                    NodeList affs = null;
                    Element authorElt = matchAuthor(prs, authorsFromfulltextTeiHeader);
                    if (authorElt != null) {
                        affs = authorElt.getElementsByTagName("affiliation");
                        if (affs != null) {
                            for (int y = affs.getLength() - 1; y >= 0; y--) {
                                if (affs.item(y).getNodeType() == Node.ELEMENT_NODE) {
                                    curateAffiliation(affiliation, pub, (Element) affs.item(y), true);
                                }
                            }
                        }
                    }
                    //email
                    //ptr//
                }

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

    private static Element matchAuthor(Person person, NodeList authorsFromfulltextTeiHeader) {
        Element author = null;
        JaroWinkler jw = new JaroWinkler();
        String fullname = person.getFullname();
        NodeList nodes = null;
        for (int y = authorsFromfulltextTeiHeader.getLength() - 1; y >= 0; y--) {
            Node personChildElt = authorsFromfulltextTeiHeader.item(y);
            if (personChildElt.getNodeType() == Node.ELEMENT_NODE) {
                String forename = "", lastname = "";
                NodeList personChildEltNL = personChildElt.getChildNodes();
                for (int i = personChildEltNL.getLength() - 1; i >= 0; i--) {
                    Node personChildEltElt = personChildEltNL.item(i);
                    if (personChildEltElt.getNodeType() == Node.ELEMENT_NODE) {
                        if (personChildEltElt.getNodeName().equals("persName")) {
                            nodes = personChildEltElt.getChildNodes();
                            for (int z = nodes.getLength() - 1; z >= 0; z--) {
                                if (nodes.item(z).getNodeName().equals("forename")) {
                                    boolean first = forename.isEmpty();
                                    forename += (first ? "" : " ") + nodes.item(z).getTextContent();
                                } else if (nodes.item(z).getNodeName().equals("surname")) {
                                    boolean first = lastname.isEmpty();
                                    lastname += (first ? "" : " ") + nodes.item(z).getTextContent();
                                }
                            }
                        }
                    }
                }
                if (jw.similarity(fullname, forename + " " + lastname) > 0.7) {
                    author = (Element) personChildElt;
                    break;
                }
            }
        }
        return author;
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
                    addr.setAddrLine(addrChildElt.getTextContent());
                } else if (addrChildElt.getNodeName().equals("country")) {
                    addr.setCountry(new Country(null, addrChildElt.getAttribute("key")));
                } else if (addrChildElt.getNodeName().equals("settlement")) {
                    addr.setSettlement(addrChildElt.getTextContent());
                } else if (addrChildElt.getNodeName().equals("postCode")) {
                    addr.setPostCode(addrChildElt.getTextContent());
                } else if (addrChildElt.getNodeName().equals("region")) {
                    addr.setRegion(addrChildElt.getTextContent());
                }
            }
        }
        location.setAddress(addr);
        return location;
    }
}
