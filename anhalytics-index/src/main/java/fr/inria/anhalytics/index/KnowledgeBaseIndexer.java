package fr.inria.anhalytics.index;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.commons.dao.AbstractDAOFactory;
import fr.inria.anhalytics.commons.dao.AddressDAO;
import fr.inria.anhalytics.commons.dao.Conference_EventDAO;
import fr.inria.anhalytics.commons.dao.DocumentDAO;
import fr.inria.anhalytics.commons.dao.In_SerialDAO;
import fr.inria.anhalytics.commons.dao.MonographDAO;
import fr.inria.anhalytics.commons.dao.PersonDAO;
import fr.inria.anhalytics.commons.dao.PublicationDAO;
import fr.inria.anhalytics.commons.properties.IndexProperties;
import fr.inria.anhalytics.commons.dao.anhalytics.DAOFactory;
import fr.inria.anhalytics.commons.dao.anhalytics.OrganisationDAO;
import fr.inria.anhalytics.commons.dao.biblio.AbstractBiblioDAOFactory;
import fr.inria.anhalytics.commons.dao.biblio.BiblioDAOFactory;
import fr.inria.anhalytics.commons.entities.Address;
import fr.inria.anhalytics.commons.entities.Affiliation;
import fr.inria.anhalytics.commons.entities.Conference_Event;
import fr.inria.anhalytics.commons.entities.Document;
import fr.inria.anhalytics.commons.entities.Document_Organisation;
import fr.inria.anhalytics.commons.entities.In_Serial;
import fr.inria.anhalytics.commons.entities.Organisation;
import fr.inria.anhalytics.commons.entities.PART_OF;
import fr.inria.anhalytics.commons.entities.Person;
import fr.inria.anhalytics.commons.entities.Publication;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achraf
 */
public class KnowledgeBaseIndexer extends Indexer {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseIndexer.class);

    private static final AbstractDAOFactory adf = AbstractDAOFactory.getFactory(AbstractDAOFactory.DAO_FACTORY);
    private static final AbstractBiblioDAOFactory biblioadf = AbstractBiblioDAOFactory.getFactory(AbstractBiblioDAOFactory.DAO_FACTORY);

    public KnowledgeBaseIndexer() {
        super();
        DAOFactory.initConnection();
        BiblioDAOFactory.initConnection();
    }

    private static void getAffiliations(List<Map<String, Object>> organisations, Organisation org, Date begin_date, Date end_date) throws SQLException {

        if (org != null) {
            AddressDAO adao = (AddressDAO) adf.getAddressDAO();
            Map<String, Object> orgDocument = org.getOrganisationDocument();

            orgDocument.put("begin_date", Utilities.formatDate(begin_date));
            orgDocument.put("end_date", Utilities.formatDate(end_date));
            Address addr = (adao.getOrganisationAddress(org.getOrganisationId()));
            Map<String, Object> orgAddress = null;
            if (addr != null) {
                orgAddress = addr.getAddressDocument();
            }
            orgDocument.put("address", orgAddress);
            organisations.add(orgDocument);
            for (PART_OF part_of : org.getRels()) {
                Organisation org1 = part_of.getOrganisation_mother();
                getAffiliations(organisations, org1, begin_date, end_date);

            }
        }

    }

    public int indexAuthors() throws SQLException, UnknownHostException {
        IndexingPreprocess indexingPreprocess = new IndexingPreprocess(MongoFileManager.getInstance(false));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode standoffNode = null;
        PersonDAO pdao = (PersonDAO) adf.getPersonDAO();
        OrganisationDAO odao = (OrganisationDAO) adf.getOrganisationDAO();
        DocumentDAO ddao = (DocumentDAO) adf.getDocumentDAO();
        PublicationDAO pubdao = (PublicationDAO) adf.getPublicationDAO();
        Map<Long, Person> persons = pdao.findAllAuthors();
        int nb = 0;
        int bulkSize = 100;
        Iterator it = persons.entrySet().iterator();
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.setRefresh(true);
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Long personId = (Long) pair.getKey();
            Person pers = (Person) pair.getValue();
            Map<String, Object> jsonDocument = pers.getPersonDocument();
            List<Affiliation> affs = odao.getAffiliationByPersonID(pers);
            List<Map<String, Object>> organisations = new ArrayList<Map<String, Object>>();
            for (Affiliation aff : affs) {
                getAffiliations(organisations, aff.getOrganisations().get(0), aff.getFrom_date(), aff.getUntil_date());
            }
            jsonDocument.put("affiliations", organisations);

            List<Map<String, Object>> publications = new ArrayList<Map<String, Object>>();
            Map<String, Object> documentDoc = null;
            Publication publication = null;
            Address addr = null;
            Document_Organisation dorg = null;
            Map<String, Object> orgDoc = null;
            List<Map<String, Object>> document_organisations = null;
            List<Map<String, Object>> coauthors_documents = new ArrayList<Map<String, Object>>();
            Map<Long, Person> coauthors = null;
            for (Document doc : ddao.getDocumentsByAuthorId(personId)) {
                document_organisations = new ArrayList<Map<String, Object>>();
                documentDoc = doc.getDocumentDocument();
                publication = pubdao.findByDocId(doc.getDocID()).get(0);
                documentDoc.put("publication", publication.getPublicationDocument());
//                dorg = odao.getOrganisationByDocumentID(doc.getDocID());
//                for (Organisation org : dorg.getOrgs()) {
//                    orgDoc = org.getOrganisationDocument();
//                    addr = adao.getOrganisationAddress(org.getOrganisationId());
//                    if (addr != null) {
//                        orgDoc.put("address", addr.getAddressDocument());
//                    }
//                    document_organisations.add(orgDoc);
//                }
//                documentDoc.put("organisations", document_organisations);
                //authors/publication ?
                Map<String, Object> result = new HashMap<String, Object>();
                try {
                    standoffNode = indexingPreprocess.getStandoffNerd(mapper, doc.getDocID());
                    standoffNode = indexingPreprocess.getStandoffKeyTerm(mapper, doc.getDocID(), standoffNode);
                    if (standoffNode != null) {
                        result = mapper.convertValue(standoffNode, Map.class);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                documentDoc.put("annotations", result);
                publications.add(documentDoc);
//                coauthors = pdao.getAuthorsByDocId(doc.getDocID());
//
//                coauthors.remove(personId);
//
//                Iterator it2 = coauthors.entrySet().iterator();
//                while (it2.hasNext()) {
//                    Map.Entry pair1 = (Map.Entry) it2.next();
//                    Map<String, Object> personDoc = ((Person) pair1.getValue()).getPersonDocument();
//                    personDoc.put("date_coauthorship", publication.getDate_printed());
//                    coauthors_documents.add(personDoc);
//                }
                //Get authors ids(except the actual one) + get pub date + with fullname
            }
            //jsonDocument.put("coauthors", coauthors_documents);
            jsonDocument.put("publications", publications);
            // index the json in ElasticSearch
            // beware the document type bellow and corresponding mapping!
            bulkRequest.add(client.prepareIndex(IndexProperties.getKbIndexName(), "authors", "" + personId).setSource(jsonDocument));
            nb++;
            if (nb % bulkSize == 0) {
                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                if (bulkResponse.hasFailures()) {
                    // process failures by iterating through each bulk response item	
                    logger.error(bulkResponse.buildFailureMessage());
                }
                bulkRequest = client.prepareBulk();
                bulkRequest.setRefresh(true);
                logger.debug("\n Bulk number : " + nb / bulkSize);
            }
        }
        // last bulk
        if (nb % bulkSize != 0) {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            logger.debug("\n One Last Bulk.");
            if (bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item	
                logger.error(bulkResponse.buildFailureMessage());
            }
        }
        return nb;
    }

    public int indexPublications() throws SQLException, UnknownHostException {
        int nb = 0;
        int bulkSize = 100;

        IndexingPreprocess indexingPreprocess = new IndexingPreprocess(MongoFileManager.getInstance(false));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode standoffNode = null;
        PublicationDAO pubdao = (PublicationDAO) adf.getPublicationDAO();
        OrganisationDAO odao = (OrganisationDAO) adf.getOrganisationDAO();
        AddressDAO adao = (AddressDAO) adf.getAddressDAO();
        PublicationDAO bibliopubdao = (PublicationDAO) biblioadf.getPublicationDAO();
        DocumentDAO ddao = (DocumentDAO) adf.getDocumentDAO();
        DocumentDAO biblioddao = (DocumentDAO) biblioadf.getDocumentDAO();
        In_SerialDAO bibliinsddao = (In_SerialDAO) biblioadf.getIn_SerialDAO();
        PersonDAO pdao = (PersonDAO) adf.getPersonDAO();
        PersonDAO bibliopdao = (PersonDAO) biblioadf.getPersonDAO();
        MonographDAO mdao = (MonographDAO) adf.getMonographDAO();
        Conference_EventDAO ced = (Conference_EventDAO) adf.getConference_EventDAO();
        List<Document> documents = ddao.findAllDocuments();
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.setRefresh(true);
        for (Document doc : documents) {
            Map<String, Object> documentDocument = doc.getDocumentDocument();

            Map<Long, Person> authors = pdao.getAuthorsByDocId(doc.getDocID());
            List<Map<String, Object>> authorsDocument = new ArrayList<Map<String, Object>>();
            Iterator it = authors.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Long personId = (Long) pair.getKey();
                Person pers = (Person) pair.getValue();
                Map<String, Object> jsonDocument = pers.getPersonDocument();
                authorsDocument.add(jsonDocument);
            }
            //see how to reference other objects...would be better.
            documentDocument.put("authors", authorsDocument);
            List<Publication> pubs = pubdao.findByDocId(doc.getDocID());
            Map<String, Object> publicationDocument = pubs.get(0).getPublicationDocument();
            Map<String, Object> monographDocument = (HashMap<String, Object>) publicationDocument.get("monograph");
            Conference_Event conf = ced.findByMonograph(pubs.get(0).getMonograph().getMonographID());
            if (conf != null) {
                monographDocument.put("conference", conf.getConference_EventDocument());
            }

            documentDocument.put("publication", publicationDocument);

            List<Map<String, Object>> document_organisations = new ArrayList<Map<String, Object>>();
            Document_Organisation dorg = odao.getOrganisationByDocumentID(doc.getDocID());
            for (Organisation org : dorg.getOrgs()) {
                Map<String, Object> orgDocument = org.getOrganisationDocument();
                Address addr = (adao.getOrganisationAddress(org.getOrganisationId()));
                Map<String, Object> orgAddress = null;
                if (addr != null) {
                    orgAddress = addr.getAddressDocument();
                }
                orgDocument.put("address", orgAddress);
                document_organisations.add(orgDocument);
            }
            documentDocument.put("organisations", document_organisations);

            Map<Long, Person> editors = pdao.getEditorsByPubId(pubs.get(0).getPublicationID());//suppose one-one relation..
            List<Map<String, Object>> editorsDocument = new ArrayList<Map<String, Object>>();
            Iterator it1 = editors.entrySet().iterator();
            while (it1.hasNext()) {
                Map.Entry pair = (Map.Entry) it1.next();
                Long personId = (Long) pair.getKey();
                Person pers = (Person) pair.getValue();
                Map<String, Object> jsonDocument = pers.getPersonDocument();
                editorsDocument.add(jsonDocument);
            }
            documentDocument.put("editors", editorsDocument);

            //document_organisation
            //biblioadf  references
            Document docRef = biblioddao.find(doc.getDocID());

            List<Map<String, Object>> referencesPubDocument = new ArrayList<Map<String, Object>>();
            if (docRef != null) {
                List<Publication> referencesPub = bibliopubdao.findByDocId(docRef.getDocID());

                for (Publication referencePub : referencesPub) {
                    In_Serial in = bibliinsddao.find(referencePub.getMonograph().getMonographID());
                    Map<String, Object> referencePubDocument = referencePub.getPublicationDocument();
                    referencePubDocument.put("journal", in.getJ().getJournalDocument());
                    referencePubDocument.put("collection", in.getC().getCollectionDocument());
                    referencePubDocument.put("issue", in.getIssue());
                    referencePubDocument.put("volume", in.getVolume());
                    Map<Long, Person> referenceAuthors = bibliopdao.getEditorsByPubId(referencePub.getPublicationID());

                    List<Map<String, Object>> referenceAuthorsDocument = new ArrayList<Map<String, Object>>();
                    Iterator it2 = referenceAuthors.entrySet().iterator();
                    while (it2.hasNext()) {
                        Map.Entry pair = (Map.Entry) it2.next();
                        Long personId = (Long) pair.getKey();
                        Person pers = (Person) pair.getValue();
                        Map<String, Object> jsonDocument = pers.getPersonDocument();
                        referenceAuthorsDocument.add(jsonDocument);
                    }

                    referencePubDocument.put("authors", referenceAuthorsDocument);
                    referencesPubDocument.add(referencePubDocument);
                }
            }
            documentDocument.put("references", referencesPubDocument);

            Map<String, Object> result = new HashMap<String, Object>();
            try {
                standoffNode = indexingPreprocess.getStandoffNerd(mapper, doc.getDocID());
                standoffNode = indexingPreprocess.getStandoffKeyTerm(mapper, doc.getDocID(), standoffNode);
                if (standoffNode != null) {
                    result = mapper.convertValue(standoffNode, Map.class);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            documentDocument.put("annotations", result);
            //HAL domains

            // index the json in ElasticSearch
            // beware the document type bellow and corresponding mapping!
            bulkRequest.add(client.prepareIndex(IndexProperties.getKbIndexName(), "publications", "" + doc.getDocID()).setSource(documentDocument));

            nb++;
            if (nb % bulkSize == 0) {
                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                if (bulkResponse.hasFailures()) {
                    // process failures by iterating through each bulk response item	
                    logger.error(bulkResponse.buildFailureMessage());
                }
                bulkRequest = client.prepareBulk();
                bulkRequest.setRefresh(true);
                logger.debug("\n Bulk number : " + nb / bulkSize);
            }
        }
        // last bulk
        if (nb % bulkSize != 0) {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            logger.debug("\n One Last Bulk.");
            if (bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item	
                logger.error(bulkResponse.buildFailureMessage());
            }
        }
        return nb;
    }

    public int indexOrganisations() throws SQLException, UnknownHostException {
        int nb = 0;
        int bulkSize = 100;
        IndexingPreprocess indexingPreprocess = new IndexingPreprocess(MongoFileManager.getInstance(false));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode standoffNode = null;

        OrganisationDAO odao = (OrganisationDAO) adf.getOrganisationDAO();
        PublicationDAO pubdao = (PublicationDAO) adf.getPublicationDAO();
        List<Organisation> organisations = odao.findAllOrganisations();
        DocumentDAO ddao = (DocumentDAO) adf.getDocumentDAO();
        AddressDAO adao = (AddressDAO) adf.getAddressDAO();
        PersonDAO pdao = (PersonDAO) adf.getPersonDAO();
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.setRefresh(true);
        for (Organisation org : organisations) {
            Map<String, Object> organisationDocument = org.getOrganisationDocument();
            Address addr = (adao.getOrganisationAddress(org.getOrganisationId()));
            Map<String, Object> addressDocument = null;
            if (addr != null) {
                addressDocument = addr.getAddressDocument();
            }
            organisationDocument.put("address", addressDocument);
            List<Map<String, Object>> orgRelationsDocument = new ArrayList<Map<String, Object>>();
            for (PART_OF partOf : org.getRels()) {
                Map<String, Object> motherOrganisationDocument = partOf.getOrganisation_mother().getOrganisationDocument();
                Address motheraddr = (adao.getOrganisationAddress(partOf.getOrganisation_mother().getOrganisationId()));
                Map<String, Object> motheraddressDocument = null;
                if (motheraddr != null) {
                    motheraddressDocument = motheraddr.getAddressDocument();
                }
                motherOrganisationDocument.put("address", motheraddressDocument);
                orgRelationsDocument.add(motherOrganisationDocument);
            }

            organisationDocument.put("relations", orgRelationsDocument);
            List<Document> docs = ddao.getDocumentsByOrgId(org.getOrganisationId());
            List<Map<String, Object>> orgDocumentsDocument = new ArrayList<Map<String, Object>>();
            Map<String, Object> documentDocument = null;
            for (Document doc : docs) {
                List<Publication> pubs = pubdao.findByDocId(doc.getDocID());
                Map<String, Object> publicationDocument = pubs.get(0).getPublicationDocument();
                documentDocument = doc.getDocumentDocument();
                documentDocument.put("publication", publicationDocument);
                orgDocumentsDocument.add(documentDocument);

                Map<String, Object> result = new HashMap<String, Object>();
                try {
                    standoffNode = indexingPreprocess.getStandoffNerd(mapper, doc.getDocID());
                    standoffNode = indexingPreprocess.getStandoffKeyTerm(mapper, doc.getDocID(), standoffNode);
                    if (standoffNode != null) {
                        result = mapper.convertValue(standoffNode, Map.class);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                documentDocument.put("annotations", result);

            }
            organisationDocument.put("docCount", docs.size());
            organisationDocument.put("documents", orgDocumentsDocument);

            Map<Long, Person> authors = pdao.getPersonsByOrgID(org.getOrganisationId());
            List<Map<String, Object>> authorsDocument = new ArrayList<Map<String, Object>>();

            Iterator it2 = authors.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry pair = (Map.Entry) it2.next();
                Long personId = (Long) pair.getKey();
                Person pers = (Person) pair.getValue();
                Map<String, Object> jsonDocument = pers.getPersonDocument();
                authorsDocument.add(jsonDocument);
            }
            organisationDocument.put("authors", authorsDocument);
            // index the json in ElasticSearch
            // beware the document type bellow and corresponding mapping!
            bulkRequest.add(client.prepareIndex(IndexProperties.getKbIndexName(), "organisations", "" + org.getOrganisationId()).setSource(organisationDocument));

            nb++;
            if (nb % bulkSize == 0) {
                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                if (bulkResponse.hasFailures()) {
                    // process failures by iterating through each bulk response item	
                    logger.error(bulkResponse.buildFailureMessage());
                }
                bulkRequest = client.prepareBulk();
                bulkRequest.setRefresh(true);
                logger.debug("\n Bulk number : " + nb / bulkSize);
            }
        }
        // last bulk
        if (nb % bulkSize != 0) {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            logger.debug("\n One Last Bulk.");
            if (bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item	
                logger.error(bulkResponse.buildFailureMessage());
            }
        }
        return nb;
    }

    @Override
    public void close() {
        super.close();
        BiblioDAOFactory.closeConnection();
        DAOFactory.closeConnection();
    }

}
