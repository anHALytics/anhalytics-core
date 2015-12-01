package fr.inria.anhalytics.datamine;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.dao.AddressDAO;
import fr.inria.anhalytics.dao.AffiliationDAO;
import fr.inria.anhalytics.dao.AnhalyticsConnection;
import fr.inria.anhalytics.dao.Conference_EventDAO;
import fr.inria.anhalytics.dao.LocationDAO;
import fr.inria.anhalytics.dao.DocumentDAO;
import fr.inria.anhalytics.dao.Document_IdentifierDAO;
import fr.inria.anhalytics.dao.In_SerialDAO;
import fr.inria.anhalytics.dao.MonographDAO;
import fr.inria.anhalytics.dao.OrganisationDAO;
import fr.inria.anhalytics.dao.PersonDAO;
import fr.inria.anhalytics.dao.PublicationDAO;
import fr.inria.anhalytics.dao.PublisherDAO;
import fr.inria.anhalytics.entities.Address;
import fr.inria.anhalytics.entities.Affiliation;
import fr.inria.anhalytics.entities.Author;
import fr.inria.anhalytics.entities.Collection;
import fr.inria.anhalytics.entities.Conference;
import fr.inria.anhalytics.entities.Conference_Event;
import fr.inria.anhalytics.entities.Country;
import fr.inria.anhalytics.entities.Editor;
import fr.inria.anhalytics.entities.In_Serial;
import fr.inria.anhalytics.entities.Journal;
import fr.inria.anhalytics.entities.Location;
import fr.inria.anhalytics.entities.Monograph;
import fr.inria.anhalytics.entities.Organisation;
import fr.inria.anhalytics.entities.Person;
import fr.inria.anhalytics.entities.Person_Identifier;
import fr.inria.anhalytics.entities.Publication;
import fr.inria.anhalytics.entities.Publisher;
import fr.inria.anhalytics.entities.Serial_Identifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author achraf
 */
public class HALMiner {
    
    private static final Logger logger = LoggerFactory.getLogger(HALMiner.class);
    
    private static XPath xPath = XPathFactory.newInstance().newXPath();
    
    public static String docID = "";
    
    public static String getDocId() {
        return docID;
    }
    
    public static Document mine(Document docTeiCorpus, String uri) throws ParserConfigurationException, IOException, XPathExpressionException {
        PublicationDAO pd = new PublicationDAO(AnhalyticsConnection.getInstance());
        DocumentDAO dd = new DocumentDAO(AnhalyticsConnection.getInstance());
        
        Publication pub = new Publication();
        
        Node title = (Node) xPath.compile("/teiCorpus/teiHeader/titleStmt/title").evaluate(docTeiCorpus, XPathConstants.NODE);
        Node language = (Node) xPath.compile("/teiCorpus/teiHeader/profileDesc/langUsage/language").evaluate(docTeiCorpus, XPathConstants.NODE);
        NodeList editors = (NodeList) xPath.compile("/teiCorpus/teiHeader/sourceDesc/biblStruct/editor").evaluate(docTeiCorpus, XPathConstants.NODESET);
        NodeList authors = (NodeList) xPath.compile("/teiCorpus/teiHeader/sourceDesc/biblStruct/author").evaluate(docTeiCorpus, XPathConstants.NODESET);
        Node metadata = (Node) xPath.compile("/teiCorpus/teiHeader").evaluate(docTeiCorpus, XPathConstants.NODE);
        NodeList monogr = (NodeList) xPath.compile("/teiCorpus/teiHeader/sourceDesc/biblStruct/monogr").evaluate(docTeiCorpus, XPathConstants.NODESET);
        
        NodeList ids = (NodeList) xPath.compile("/teiCorpus/teiHeader/sourceDesc/biblStruct/idno").evaluate(docTeiCorpus, XPathConstants.NODESET);
        
        fr.inria.anhalytics.entities.Document doc = new fr.inria.anhalytics.entities.Document(null, Utilities.getVersionFromURI(uri), Utilities.innerXmlToString(metadata), uri);
        
        dd.create(doc);
        docID = Long.toString(doc.getDocID());
        
        pub.setDocument(doc);
        
        pub.setDoc_title(title.getTextContent());
        pub.setLanguage(language.getTextContent());
        logger.info("#######" + uri + "########");

        processMonogr(monogr, pub);
        
        pd.create(pub);
        processPersons(authors, "author", pub, docTeiCorpus);
        processPersons(editors, "editor", pub, docTeiCorpus);
        processIdentifiers(ids, doc);
        
        return docTeiCorpus;
        
    }
    
    private static void processIdentifiers(NodeList ids, fr.inria.anhalytics.entities.Document doc) {
        String type = null;
        String id = null;
        Document_IdentifierDAO did = new Document_IdentifierDAO(AnhalyticsConnection.getInstance());
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
        MonographDAO md = new MonographDAO(AnhalyticsConnection.getInstance());
        Conference_EventDAO ced = new Conference_EventDAO(AnhalyticsConnection.getInstance());
        In_SerialDAO isd = new In_SerialDAO(AnhalyticsConnection.getInstance());
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
                        pub.setType(nnm.item(j).getTextContent());
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
                        (new PublisherDAO(AnhalyticsConnection.getInstance())).create(pls);
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
                        if (type == "datePub") {
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
                                    if (pp.contains("-")) {
                                        String[] pages = pp.split("-");
                                        pub.setStart_page(pages[0]);
                                        pub.setEnd_page(pages[1]);
                                    } else {
                                        pub.setStart_page(pp);
                                    }
                                }
                            }
                        }
                    }
                    
                }
            } else if (node.getNodeName().equals("meeting")) {
                ce = new Conference_Event();
                Address addr = new Address();
                AddressDAO ad = new AddressDAO(AnhalyticsConnection.getInstance());
                NodeList meeting = node.getChildNodes();
                for (int j = meeting.getLength() - 1; j >= 0; j--) {
                    Node entry = meeting.item(j);
                    if (entry.getNodeName().equals("title")) {
                        ce.setConference((new Conference(null, entry.getTextContent())));
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
    
    private static void processPersons(NodeList persons, String type, Publication pub, Document doc) {
        Node person = null;
        PersonDAO pd = new PersonDAO(AnhalyticsConnection.getInstance());
        Person prs = new Person();
        Affiliation affiliation = null;
        AffiliationDAO affd = new AffiliationDAO(AnhalyticsConnection.getInstance());
        Organisation organisation = null;
        OrganisationDAO od = new OrganisationDAO(AnhalyticsConnection.getInstance());
        List<Person_Identifier> pis = new ArrayList<Person_Identifier>();
        logger.debug(" number of authors : " + persons.getLength());
        for (int i = persons.getLength() - 1; i >= 0; i--) {
            person = persons.item(i);
            prs = new Person();
            affiliation = new Affiliation();
            NodeList theNodes = person.getChildNodes();
            NodeList nodes = null;
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
                    } else if (node.getNodeName().equals("affiliation")) {
                        
                        organisation = new Organisation();
                        Location location = new Location();
                        LocationDAO ld = new LocationDAO(AnhalyticsConnection.getInstance());
                        Address addr = new Address();
                        AddressDAO ad = new AddressDAO(AnhalyticsConnection.getInstance());
                        Node org = node.getChildNodes().item(0);
                        
                        NamedNodeMap nnm = org.getAttributes();
                        for (int p = nnm.getLength() - 1; p >= 0; p--) {
                            if (nnm.item(p).getNodeName().equals("type")) {
                                organisation.setType(nnm.item(p).getTextContent());
                            } else if (nnm.item(p).getNodeName().equals("ref")) {
                                organisation.setStructure(nnm.item(p).getTextContent());
                            }
                        }
                        
                        NodeList nl = org.getChildNodes();
                        for (int n = 0; n < nl.getLength(); n++) {
                            Node nd = nl.item(n);
                            if (nd.getNodeName().equals("orgName")) {
                                organisation.addName(nd.getTextContent());
                            } else if (nd.getNodeName().equals("desc")) {
                                NodeList desc = nd.getChildNodes();
                                for (int d = 0; d < desc.getLength(); d++) {
                                    if (desc.item(d).getNodeName().equals("address")) {
                                        NodeList address = (nd.getChildNodes().item(0)).getChildNodes();
                                        for (int x = 0; x < address.getLength(); x++) {
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
                        ad.create(addr);
                        od.create(organisation);
                        affiliation.addOrganisation(organisation);
                        location.setAddress(addr);
                        location.setOrganisation(organisation);
                        ld.create(location);
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
                        pi.setId(id_value);
                        pi.setType(id_type);
                        pis.add(pi);
                    }
                    person.removeChild(node);
                }
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
            idno.setAttribute(type, "anhalyticsID");
            idno.setTextContent(Long.toString(prs.getPersonId()));
            person.appendChild(idno);
        }
        
    }
}
