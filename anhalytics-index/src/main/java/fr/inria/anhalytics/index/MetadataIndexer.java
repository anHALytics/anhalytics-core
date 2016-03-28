package fr.inria.anhalytics.index;

import fr.inria.anhalytics.commons.exceptions.ElasticSearchConfigurationException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.dao.AbstractDAOFactory;
import fr.inria.anhalytics.dao.AddressDAO;
import fr.inria.anhalytics.dao.DocumentDAO;
import fr.inria.anhalytics.dao.In_SerialDAO;
import fr.inria.anhalytics.dao.MonographDAO;
import fr.inria.anhalytics.dao.PersonDAO;
import fr.inria.anhalytics.dao.PublicationDAO;
import fr.inria.anhalytics.index.properties.IndexProperties;
import fr.inria.anhalytics.ingest.dao.anhalytics.DAOFactory;
import fr.inria.anhalytics.ingest.dao.anhalytics.OrganisationDAO;
import fr.inria.anhalytics.ingest.dao.biblio.AbstractBiblioDAOFactory;
import fr.inria.anhalytics.ingest.dao.biblio.BiblioDAOFactory;
import fr.inria.anhalytics.ingest.entities.Address;
import fr.inria.anhalytics.ingest.entities.Affiliation;
import fr.inria.anhalytics.ingest.entities.Document;
import fr.inria.anhalytics.ingest.entities.In_Serial;
import fr.inria.anhalytics.ingest.entities.Organisation;
import fr.inria.anhalytics.ingest.entities.PART_OF;
import fr.inria.anhalytics.ingest.entities.Person;
import fr.inria.anhalytics.ingest.entities.Publication;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achraf
 */
public class MetadataIndexer {

    private static final Logger logger = LoggerFactory.getLogger(MetadataIndexer.class);

    private static final AbstractDAOFactory adf = AbstractDAOFactory.getFactory(AbstractDAOFactory.DAO_FACTORY);
    private static final AbstractBiblioDAOFactory biblioadf = AbstractBiblioDAOFactory.getFactory(AbstractBiblioDAOFactory.DAO_FACTORY);

    private Client client;

    public MetadataIndexer() {

        DAOFactory.initConnection();
        BiblioDAOFactory.initConnection();
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", IndexProperties.getElasticSearchClusterName()).build();
        this.client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(IndexProperties.getElasticSearch_host(), 9300));
    }

    public void indexAuthors() {
        PersonDAO pdao = (PersonDAO) adf.getPersonDAO();
        OrganisationDAO odao = (OrganisationDAO) adf.getOrganisationDAO();
        AddressDAO adao = (AddressDAO) adf.getAddressDAO();
        DocumentDAO ddao = (DocumentDAO) adf.getDocumentDAO();
        List<Person> persons = pdao.findAllAuthors();
        for (Person person : persons) {
            Map<String, Object> jsonDocument = person.getPersonDocument();
            List<Affiliation> affs = odao.getAffiliationByPersonID(person);
            List<Map<String, Object>> organisations = new ArrayList<Map<String, Object>>();
            for (Affiliation aff : affs) {

                Map<String, Object> orgDocument = aff.getOrganisations().get(0).getOrganisationDocument();

                orgDocument.put("begin_date", Utilities.formatDate(aff.getBegin_date()));
                orgDocument.put("end_date", Utilities.formatDate(aff.getBegin_date()));
                Address addr = (adao.getOrganisationAddress(aff.getOrganisations().get(0).getOrganisationId()));
                Map<String, Object> orgAddress = null;
                if (addr != null) {
                    orgAddress = addr.getAddressDocument();
                }
                orgDocument.put("address", orgAddress);
                organisations.add(orgDocument);
            }
            jsonDocument.put("publications", ddao.getDocumentsByAuthorId(person.getPersonId()));
            jsonDocument.put("affiliations", organisations);
            client.prepareIndex(IndexProperties.getMetadataIndexName(), "authors", "" + person.getPersonId())
                    .setSource(jsonDocument).execute().actionGet();
        }
    }

    public void indexPublications() {
        DAOFactory.initConnection();
        PublicationDAO pubdao = (PublicationDAO) adf.getPublicationDAO();
        PublicationDAO bibliopubdao = (PublicationDAO) biblioadf.getPublicationDAO();
        DocumentDAO ddao = (DocumentDAO) adf.getDocumentDAO();
        DocumentDAO biblioddao = (DocumentDAO) biblioadf.getDocumentDAO();
        In_SerialDAO bibliinsddao = (In_SerialDAO) biblioadf.getIn_SerialDAO();
        PersonDAO pdao = (PersonDAO) adf.getPersonDAO();
        MonographDAO mdao = (MonographDAO) adf.getMonographDAO();
        List<Document> documents = ddao.findAllDocuments();
        for (Document doc : documents) {
            Map<String, Object> documentDocument = doc.getDocumentDocument();

            List<Person> authors = pdao.getAuthorsByDocId(doc.getDocID());
            List<Map<String, Object>> authorsDocument = new ArrayList<Map<String, Object>>();
            for (Person author : authors) {
                authorsDocument.add(author.getPersonDocument());
            }
            documentDocument.put("authors", authorsDocument);
            List<Publication> pubs = pubdao.findByDocId(doc.getDocID());
            documentDocument.put("publication", pubs.get(0).getPublicationDocument());

            List<Person> editors = pdao.getEditorsByPubId(pubs.get(0).getPublicationID());//suppose one-one relation..
            List<Map<String, Object>> editorsDocument = new ArrayList<Map<String, Object>>();
            for (Person editor : editors) {
                editorsDocument.add(editor.getPersonDocument());
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
                    referencesPubDocument.add(referencePubDocument);
                }
            }
            documentDocument.put("references", referencesPubDocument);
            client.prepareIndex(IndexProperties.getMetadataIndexName(), "publications", "" + doc.getDocID())
                    .setSource(documentDocument).execute().actionGet();
        }
    }

    public void indexOrganisations() {
        OrganisationDAO odao = (OrganisationDAO) adf.getOrganisationDAO();
        List<Organisation> organisations = odao.findAllOrganisations();
        DocumentDAO ddao = (DocumentDAO) adf.getDocumentDAO();
        AddressDAO adao = (AddressDAO) adf.getAddressDAO();
        PersonDAO pdao = (PersonDAO) adf.getPersonDAO();
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
            for(Document doc:docs){
                orgDocumentsDocument.add(doc.getDocumentDocument());
            }
            organisationDocument.put("publications", orgDocumentsDocument);
            
            
            List<Person> authors = pdao.getPersonsByOrgID(org.getOrganisationId());
            List<Map<String, Object>> authorsDocument = new ArrayList<Map<String, Object>>();
            for (Person author : authors) {
                authorsDocument.add(author.getPersonDocument());
            }
            organisationDocument.put("authors", authorsDocument);
            client.prepareIndex(IndexProperties.getMetadataIndexName(), "organisations", "" + org.getOrganisationId())
                    .setSource(organisationDocument).execute().actionGet();
        }

    }

    /**
     * set-up ElasticSearch by loading the mapping and river json for the HAL
     * document database
     */
    public void setUpIndex(String indexName) {
        try {
            // delete previous index
            deleteIndex(indexName);

            // create new index and load the appropriate mapping
            createIndex(indexName);
            loadMapping(indexName);
        } catch (Exception e) {
            logger.error("Sep-up of ElasticSearch failed for HAL index.", e);
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private boolean deleteIndex(String indexName) throws Exception {
        boolean val = false;
        try {
            String urlStr = "http://" + IndexProperties.getElasticSearch_host() + ":" + IndexProperties.getElasticSearch_port() + "/" + indexName;
            URL url = new URL(urlStr);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestProperty(
                    "Content-Type", "application/x-www-form-urlencoded");
            httpCon.setRequestMethod("DELETE");
            httpCon.connect();
            logger.info("ElasticSearch Index " + indexName + " deleted: status is "
                    + httpCon.getResponseCode());
            if (httpCon.getResponseCode() == 200) {
                val = true;
            }
            httpCon.disconnect();
        } catch (Exception e) {
            throw new Exception("Cannot delete index for " + indexName);
        }
        return val;
    }

    /**
     *
     */
    private boolean createIndex(String indexName) throws IOException {
        boolean val = false;

        // create index
        String urlStr = "http://" + IndexProperties.getElasticSearch_host() + ":" + IndexProperties.getElasticSearch_port() + "/" + indexName;
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException ex) {
            java.util.logging.Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestProperty(
                "Content-Type", "application/x-www-form-urlencoded");
        try {
            httpCon.setRequestMethod("PUT");
        } catch (ProtocolException ex) {
            java.util.logging.Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*System.out.println("ElasticSearch Index " + indexName + " creation: status is " + 
         httpCon.getResponseCode());
         if (httpCon.getResponseCode() == 200) {
         val = true;
         }*/
        // load custom analyzer
        String analyserStr = null;
        try {
            ClassLoader classLoader = Indexer.class.getClassLoader();
            analyserStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/analyzer.json"));
        } catch (Exception e) {
            throw new ElasticSearchConfigurationException("Cannot read analyzer for " + indexName);
        }

        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("PUT");
        httpCon.addRequestProperty("Content-Type", "text/json");
        OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
        out.write(analyserStr);
        out.close();

        logger.info("ElasticSearch analyzer for " + indexName + " : status is "
                + httpCon.getResponseCode());
        if (httpCon.getResponseCode() == 200) {
            val = true;
        }

        httpCon.disconnect();
        return val;
    }

    /**
     *
     */
    private boolean loadMapping(String indexName) throws Exception {
        boolean val = false;

        String urlStr = "http://" + IndexProperties.getElasticSearch_host() + ":" + IndexProperties.getElasticSearch_port() + "/" + indexName;
        if (indexName.contains("annotation")) {
            urlStr += "/annotation/_mapping";
        } else {
            urlStr += "/npl/_mapping";
        }

        URL url = new URL(urlStr);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestProperty(
                "Content-Type", "application/x-www-form-urlencoded");
        httpCon.setRequestMethod("PUT");
        String mappingStr = null;
        try {
            ClassLoader classLoader = Indexer.class.getClassLoader();
            if (indexName.contains("annotation")) {
                mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/annotation.json"));
            } else {
                mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/npl.json"));
            }
        } catch (Exception e) {
            throw new ElasticSearchConfigurationException("Cannot read mapping for " + indexName);
        }
        logger.info(urlStr);

        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("PUT");
        httpCon.addRequestProperty("Content-Type", "text/json");
        OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
        out.write(mappingStr);
        out.close();

        logger.info("ElasticSearch mapping for " + indexName + " : status is "
                + httpCon.getResponseCode());
        if (httpCon.getResponseCode() == 200) {
            val = true;
        }
        return val;
    }

    public void close() {
        
        BiblioDAOFactory.closeConnection();
        DAOFactory.closeConnection();
        this.client.close();
    }

}
