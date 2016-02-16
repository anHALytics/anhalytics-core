package fr.inria.anhalytics.datamine;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.dao.AbstractDAOFactory;
import fr.inria.anhalytics.dao.AddressDAO;
import fr.inria.anhalytics.dao.anhalytics.AffiliationDAO;
import fr.inria.anhalytics.dao.Conference_EventDAO;
import fr.inria.anhalytics.dao.anhalytics.LocationDAO;
import fr.inria.anhalytics.dao.DocumentDAO;
import fr.inria.anhalytics.dao.Document_OrganisationDAO;
import fr.inria.anhalytics.dao.anhalytics.Document_IdentifierDAO;
import fr.inria.anhalytics.dao.In_SerialDAO;
import fr.inria.anhalytics.dao.MonographDAO;
import fr.inria.anhalytics.dao.anhalytics.OrganisationDAO;
import fr.inria.anhalytics.dao.PersonDAO;
import fr.inria.anhalytics.dao.PublicationDAO;
import fr.inria.anhalytics.dao.anhalytics.DAOFactory;
import fr.inria.anhalytics.entities.Address;
import fr.inria.anhalytics.entities.Affiliation;
import fr.inria.anhalytics.entities.Author;
import fr.inria.anhalytics.entities.Collection;
import fr.inria.anhalytics.entities.Conference;
import fr.inria.anhalytics.entities.Conference_Event;
import fr.inria.anhalytics.entities.Country;
import fr.inria.anhalytics.entities.Document_Organisation;
import fr.inria.anhalytics.entities.Editor;
import fr.inria.anhalytics.entities.In_Serial;
import fr.inria.anhalytics.entities.Journal;
import fr.inria.anhalytics.entities.Location;
import fr.inria.anhalytics.entities.Monograph;
import fr.inria.anhalytics.entities.Organisation;
import fr.inria.anhalytics.entities.PART_OF;
import fr.inria.anhalytics.entities.Person;
import fr.inria.anhalytics.entities.Person_Identifier;
import fr.inria.anhalytics.entities.Publication;
import fr.inria.anhalytics.entities.Publisher;
import fr.inria.anhalytics.entities.Serial_Identifier;
import fr.inria.anhalytics.harvest.teibuild.TeiBuilder;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
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

    public static String docID = "";

    public HALMiner() throws UnknownHostException {
        super();
    }

    public static String getDocId() {
        return docID;
    }

    /**
     * Initiates HAL knowledge base and creates working corpus TEI.
     */
    public void initKnowledgeBase() {
        DAOFactory.initConnection();
        PublicationDAO pd = (PublicationDAO) adf.getPublicationDAO();

        DocumentDAO dd = (DocumentDAO) adf.getDocumentDAO();
        for (String date : Utilities.getDates()) {
            if (mm.initMetadataTeis(date)) {
                while (mm.hasMoreTeis()) {
                    String metadataTeiString = mm.nextTeiDocument();
                    String uri = mm.getCurrentRepositoryDocId();
                    if (!dd.isMined(uri)) {
                        logger.info("Extracting metadata from :" + uri);
                        adf.openTransaction();
                        Document generatedTeiDoc = null;
                        try {
                            InputStream metadataTeiStream = new ByteArrayInputStream(metadataTeiString.getBytes());
                            generatedTeiDoc = TeiBuilder.createTEICorpus(metadataTeiStream);
                            metadataTeiStream.close();

                            Publication pub = new Publication();
                            Node title = (Node) xPath.compile("/teiCorpus/teiHeader/titleStmt/title").evaluate(generatedTeiDoc, XPathConstants.NODE);
                            Node language = (Node) xPath.compile("/teiCorpus/teiHeader/profileDesc/langUsage/language").evaluate(generatedTeiDoc, XPathConstants.NODE);
                            Node type = (Node) xPath.compile("/teiCorpus/teiHeader/profileDesc/textClass/classCode[@scheme=\"halTypology\"]").evaluate(generatedTeiDoc, XPathConstants.NODE);
                            Node domain = (Node) xPath.compile("/teiCorpus/teiHeader/profileDesc/textClass/classCode[@scheme=\"halDomain\"]").evaluate(generatedTeiDoc, XPathConstants.NODE);
                            //more than one domain / article
                            NodeList editors = (NodeList) xPath.compile("/teiCorpus/teiHeader/sourceDesc/biblStruct/editor").evaluate(generatedTeiDoc, XPathConstants.NODESET);
                            NodeList authors = (NodeList) xPath.compile("/teiCorpus/teiHeader/sourceDesc/biblStruct/author").evaluate(generatedTeiDoc, XPathConstants.NODESET);
                            Node metadata = (Node) xPath.compile("/teiCorpus/teiHeader").evaluate(generatedTeiDoc, XPathConstants.NODE);
                            NodeList monogr = (NodeList) xPath.compile("/teiCorpus/teiHeader/sourceDesc/biblStruct/monogr").evaluate(generatedTeiDoc, XPathConstants.NODESET);
                            NodeList ids = (NodeList) xPath.compile("/teiCorpus/teiHeader/sourceDesc/biblStruct/idno").evaluate(generatedTeiDoc, XPathConstants.NODESET);

                            fr.inria.anhalytics.entities.Document doc = new fr.inria.anhalytics.entities.Document(null, Utilities.getVersionFromURI(uri), Utilities.innerXmlToString(metadata), uri);

                            dd.create(doc);
                            docID = Long.toString(doc.getDocID());

                            pub.setDocument(doc);

                            pub.setDoc_title(title.getTextContent());
                            pub.setType(type.getTextContent());
                            pub.setLanguage(language.getTextContent());
                            logger.info("Mining : " + uri + "");

                            processMonogr(monogr, pub);

                            pd.create(pub);
                            processPersons(authors, "author", pub, generatedTeiDoc);
                            processPersons(editors, "editor", pub, generatedTeiDoc);
                            processIdentifiers(ids, doc);
                        } catch (Exception xpe) {
                            xpe.printStackTrace();
                            adf.rollback();
                        }
                        adf.endTransaction();
                        logger.info("Done.");
                        mm.insertTei(Utilities.toString(generatedTeiDoc), uri, docID, date);
                    }
                }
            }

        }
        DAOFactory.closeConnection();
        logger.info("DONE.");
    }

    private static void processIdentifiers(NodeList ids, fr.inria.anhalytics.entities.Document doc) {
        String type = null;
        String id = null;
        Document_IdentifierDAO did = (Document_IdentifierDAO) adf.getDocument_IdentifierDAO();
        for (int i = ids.getLength() - 1; i >= 0; i--) {

            Node node = ids.item(i);

            NamedNodeMap nnm = node.getAttributes();
            for (int j = nnm.getLength() - 1; j >= 0; j--) {
                if (nnm.item(j).getNodeName().equals("type")) {
                    type = nnm.item(j).getTextContent();
                }
            }
            id = node.getTextContent();

            fr.inria.anhalytics.entities.Document_Identifier di = new fr.inria.anhalytics.entities.Document_Identifier(null, id, type, doc);
            did.create(di);
        }
        fr.inria.anhalytics.entities.Document_Identifier dihal = new fr.inria.anhalytics.entities.Document_Identifier(null, doc.getUri(), "hal", doc);
        did.create(dihal);
    }

    private static void processMonogr(NodeList monogr, Publication pub) {
        MonographDAO md = (MonographDAO) adf.getMonographDAO();
        Conference_EventDAO ced = (Conference_EventDAO) adf.getConference_EventDAO();
        In_SerialDAO isd = (In_SerialDAO) adf.getIn_SerialDAO();
        Monograph mn = new Monograph();
        Conference_Event ce = null;
        In_Serial is = new In_Serial();
        Journal journal = new Journal();
        Collection collection = new Collection();
        Serial_Identifier serial_identifier = new Serial_Identifier();

        NodeList content = monogr.item(0).getChildNodes();
        for (int i = content.getLength() - 1; i >= 0; i--) {
            Node node = content.item(i);
            if (node.getNodeName().equals("idno")) {
                NamedNodeMap nnm = node.getAttributes();
                for (int j = nnm.getLength() - 1; j >= 0; j--) {
                    if (nnm.item(j).getNodeName().equals("type")) {
                        if (nnm.item(j).getTextContent().equals("issn")) {
                            is.setNumber(node.getTextContent());
                        } else if (nnm.item(j).getTextContent().equals("isbn")) {
                            is.setNumber(node.getTextContent());
                        }
                    }
                }
            } else if (node.getNodeName().equals("title")) {
                NamedNodeMap nnm = node.getAttributes();
                mn.setTitle(node.getTextContent());
                for (int j = nnm.getLength() - 1; j >= 0; j--) {
                    if (nnm.item(j).getNodeName().equals("level")) {
                        mn.setType(nnm.item(j).getTextContent());
                        if (nnm.item(j).getTextContent().equals("j")) {
                            journal.setTitle(node.getTextContent());
                        } else {
                            collection.setTitle(node.getTextContent());
                        }
                    }
                }
            } else if (node.getNodeName().equals("imprint")) {
                NodeList imprint = node.getChildNodes();

                for (int j = imprint.getLength() - 1; j >= 0; j--) {
                    Node entry = imprint.item(j);
                    if (entry.getNodeName().equals("publisher")) {
                        Publisher pls = new Publisher(null, entry.getTextContent());
                        adf.getPublisherDAO().create(pls);
                        pub.setPublisher(pls);
                        //System.out.println(entry.getTextContent());
                    } else if (entry.getNodeName().equals("date")) {
                        String type = null;
                        String date = null;
                        NamedNodeMap nnm = entry.getAttributes();
                        for (int p = nnm.getLength() - 1; p >= 0; p--) {
                            if (nnm.item(p).getNodeName().equals("type")) {
                                type = nnm.item(p).getTextContent();
                            }
                        }
                        date = entry.getTextContent();
                        if (type.equals("datePub")) {
                            String[] n = date.split("-");
                            if (n.length == 1) {
                                date = date + "-01-01";
                            } else if (n.length == 2) {
                                date = date + "-01";
                            }
                            pub.setDate_eletronic(date);
                        }
                    } else if (entry.getNodeName().equals("biblScope")) {
                        NamedNodeMap nnm = entry.getAttributes();
                        //System.out.println(nnm.getLength());
                        for (int p = nnm.getLength() - 1; p >= 0; p--) {
                            if (nnm.item(p).getNodeName().equals("unit")) {
                                //System.out.println(nnm.item(p).getTextContent());
                                if (nnm.item(p).getTextContent().equals("serie")) {
                                    collection.setTitle(entry.getTextContent());
                                } else if (nnm.item(p).getTextContent().equals("volume")) {
                                    is.setVolume(entry.getTextContent());
                                } else if (nnm.item(p).getTextContent().equals("pp")) {
                                    String pp = entry.getTextContent();
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

                }
            } else if (node.getNodeName().equals("meeting")) {
                ce = new Conference_Event();
                ce.setConference(new Conference());
                Address addr = new Address();
                AddressDAO ad = (AddressDAO) adf.getAddressDAO();
                NodeList meeting = node.getChildNodes();
                for (int j = meeting.getLength() - 1; j >= 0; j--) {
                    Node entry = meeting.item(j);
                    if (entry.getNodeName().equals("title")) {
                        ce.getConference().setTitle(entry.getTextContent());
                    } else if (entry.getNodeName().equals("date")) {
                        NamedNodeMap nnm = entry.getAttributes();
                        for (int p = nnm.getLength() - 1; p >= 0; p--) {
                            if (nnm.item(p).getNodeName().equals("type")) {
                                if (nnm.item(p).getTextContent().equals("start")) {
                                    ce.setStart_date(entry.getTextContent());
                                } else if (nnm.item(p).getTextContent().equals("end")) {
                                    ce.setEnd_date(entry.getTextContent());
                                }
                            }
                        }
                    } else if (entry.getNodeName().equals("settlement")) {
                        addr.setSettlement(entry.getTextContent());
                    } else if (entry.getNodeName().equals("country")) {
                        Country c = new Country();
                        addr.setCountryStr(entry.getTextContent());
                        NamedNodeMap nnm = entry.getAttributes();
                        for (int p = nnm.getLength() - 1; p >= 0; p--) {
                            if (nnm.item(p).getNodeName().equals("key")) {
                                c.setIso(nnm.item(p).getTextContent());
                            }
                        }
                        addr.setCountry(c);
                    } else if (entry.getNodeName().equals("region")) {
                        addr.setRegion(entry.getTextContent());
                    }

                }
                ad.create(addr);
                ce.setAddress(addr);
            }
        }

        md.create(mn);
        if (ce != null) {
            ce.setMongoraph(mn);
            ced.create(ce);
        }
        pub.setMonograph(mn);
        is.setMg(mn);
        is.setJ(journal);
        is.setC(collection);
        isd.createSerial(is, serial_identifier);
    }

    private static Organisation parseOrg(Node orgNode, Organisation org, Document_Organisation document_organisation, Date pubDate) {
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
                if (ndorg.getNodeName().equals("orgName")) {
                    organisationParent.addName(ndorg.getTextContent());
                } else if (ndorg.getNodeName().equals("desc")) {
                    NodeList descorg = ndorg.getChildNodes();
                    for (int l = descorg.getLength() - 1; l >= 0; l--) {

                        if (descorg.item(l).getNodeName().equals("address")) {
                            NodeList addressorg = (descorg.item(l)).getChildNodes();
                            addrParent = new Address();
                            locationParent = new Location();
                            for (int x = addressorg.getLength() - 1; x >= 0; x--) {
                                Node addrorgnode = addressorg.item(x);
                                if (addrorgnode.getNodeName().equals("addrLine")) {
                                    addrParent.setAddrLine(addrorgnode.getTextContent());
                                } else if (addrorgnode.getNodeName().equals("country")) {
                                    Element countryElt = (Element) addrorgnode;
                                    addrParent.setCountry(new Country(null, countryElt.getAttribute("key")));
                                }
                            }

                        } else if (descorg.item(l).getNodeName().equals("ref")) {

                            NamedNodeMap nnmDescOrg = descorg.item(l).getAttributes();
                            for (int f = nnmDescOrg.getLength() - 1; f >= 0; f--) {
                                if (nnmDescOrg.item(f).getNodeName().equals("type")) {
                                    if (nnmDescOrg.item(f).getTextContent().equals("url")) {
                                        organisationParent.setUrl(descorg.item(l).getTextContent());
                                    }
                                }
                            }
                        }

                    }
                } else if (ndorg.getNodeName().equals("org")) {
                    organisationParent = parseOrg(ndorg, organisationParent, document_organisation, pubDate);
                }
            }
            part_of.setBeginDate(pubDate);
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

    private static void processPersons(NodeList persons, String type, Publication pub, Document doc) {
        Node person = null;
        PersonDAO pd = (PersonDAO) adf.getPersonDAO();
        Person prs = null;
        Affiliation affiliation = null;
        AffiliationDAO affd = (AffiliationDAO) adf.getAffiliationDAO();
        Organisation organisation = null;
        OrganisationDAO od = (OrganisationDAO) adf.getOrganisationDAO();
        Date pubDate = null;
        if (!pub.getDate_eletronic().isEmpty()) {
            try {
                pubDate = Utilities.parseStringDate(pub.getDate_eletronic());
            } catch (ParseException ex) {
                System.out.println(pub.getDate_eletronic());
                ex.printStackTrace();
                //location.setBegin_date(Utilities.parseStringDate(date));
            }
        }
        for (int i = persons.getLength() - 1; i >= 0; i--) {
            person = persons.item(i);
            prs = new Person();
            affiliation = new Affiliation();
            NodeList theNodes = person.getChildNodes();
            NodeList nodes = null;
            List<Person_Identifier> pis = new ArrayList<Person_Identifier>();
            for (int y = theNodes.getLength() - 1; y >= 0; y--) {
                Node node = theNodes.item(y);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (node.getNodeName().equals("persName")) {
                        nodes = node.getChildNodes();
                        for (int z = nodes.getLength() - 1; z >= 0; z--) {
                            if (nodes.item(z).getNodeName().equals("forename")) {
                                prs.setForename(nodes.item(z).getTextContent());
                            } else if (nodes.item(z).getNodeName().equals("surname")) {
                                prs.setSurname(nodes.item(z).getTextContent());
                            }
                        }
                    } else if (node.getNodeName().equals("email")) {
                        prs.setEmail(node.getTextContent());
                    } else if (node.getNodeName().equals("ptr")) {
                        Element ptr = (Element) node;
                        if (ptr.getAttribute("type").equals("url")) {
                            prs.setUrl(ptr.getAttribute("target"));
                        }
                    } else if (node.getNodeName().equals("affiliation")) {
                        Document_Organisation document_organisation = new Document_Organisation();
                        Document_OrganisationDAO d_o = (Document_OrganisationDAO) adf.getDocument_OrganisationDAO();
                        document_organisation.setDoc(pub.getDocument());
                        organisation = new Organisation();
                        Location location = null;
                        LocationDAO ld = (LocationDAO) adf.getLocationDAO();
                        Address addr = null;
                        AddressDAO ad = (AddressDAO) adf.getAddressDAO();
                        Node org = node.getChildNodes().item(0);
                        if (org != null && org.getNodeType() == Node.ELEMENT_NODE) {
                            Element orgElt = (Element) org;

                            organisation.setType(orgElt.getAttribute("type"));
                            organisation.setStructure(orgElt.getAttribute("xml:id"));

                            NodeList nl = org.getChildNodes();
                            for (int n = nl.getLength() - 1; n >= 0; n--) {
                                Node nd = nl.item(n);
                                if (nd.getNodeName().equals("orgName")) {
                                    organisation.addName(nd.getTextContent());
                                } else if (nd.getNodeName().equals("org")) {
                                    organisation = parseOrg(nd, organisation, document_organisation, pubDate);
                                } else if (nd.getNodeName().equals("desc")) {
                                    NodeList desc = nd.getChildNodes();
                                    for (int d = desc.getLength() - 1; d >= 0; d--) {
                                        if (desc.item(d).getNodeName().equals("address")) {
                                            location = new Location();
                                            addr = new Address();
                                            NodeList address = (nd.getChildNodes().item(0)).getChildNodes();
                                            for (int x = address.getLength() - 1; x >= 0; x--) {
                                                Node addrnodes = address.item(x);
                                                if (addrnodes.getNodeName().equals("addrLine")) {
                                                    addr.setAddrLine(addrnodes.getTextContent());
                                                } else if (addrnodes.getNodeName().equals("country")) {
                                                    addr.setCountry(new Country(null, (addrnodes.getAttributes()).item(0).getTextContent()));
                                                }
                                            }
                                        } else if (desc.item(d).getNodeName().equals("ref")) {
                                            NamedNodeMap nnmDesc = desc.item(d).getAttributes();
                                            for (int p = nnmDesc.getLength() - 1; p >= 0; p--) {
                                                if (nnmDesc.item(p).getNodeName().equals("type")) {
                                                    if (nnmDesc.item(p).getTextContent().equals("url")) {
                                                        organisation.setUrl(desc.item(d).getTextContent());

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            od.create(organisation);

                            document_organisation.addOrg(organisation);
                            d_o.create(document_organisation);
                            affiliation.addOrganisation(organisation);
                            affiliation.setBegin_date(pubDate);
                            if (addr != null) {
                                ad.create(addr);
                                location.setAddress(addr);
                                location.setBegin_date(pubDate);
                                location.setOrganisation(organisation);
                                ld.create(location);
                            }
                        }
                    } else if (node.getNodeName().equals("idno")) {
                        Person_Identifier pi = new Person_Identifier();
                        NamedNodeMap nnm = node.getAttributes();
                        String id_type = null;
                        String id_value = node.getTextContent();
                        for (int p = nnm.getLength() - 1; p >= 0; p--) {
                            if (nnm.item(p).getNodeName().equals("type")) {
                                id_type = nnm.item(p).getTextContent();
                            }
                        }
                        if (id_type.equals("anhalyticsID")) {
                            person.removeChild(node);
                        } else {
                            pi.setId(id_value);
                            pi.setType(id_type);
                            pis.add(pi);
                        }
                    }
                    //person.removeChild(node);
                }
            }
            prs.setFullname(prs.getSurname() + " " + prs.getMiddlename() + " " + prs.getForename());
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
