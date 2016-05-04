package fr.inria.anhalytics.index;

import fr.inria.anhalytics.commons.exceptions.ElasticSearchConfigurationException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import java.io.*;
import java.util.*;

import java.net.*;
import org.elasticsearch.common.settings.*;
import org.elasticsearch.client.*;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.common.transport.*;
import org.elasticsearch.client.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JsonTapasML;
import org.json.JSONObject;
import fr.inria.anhalytics.index.IndexingPreprocess;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.index.properties.IndexProperties;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Method for management of the ElasticSearch cluster.
 *
 * @author Patrice Lopez
 */
public class Indexer {

    private static final Logger logger = LoggerFactory.getLogger(Indexer.class);

    private final MongoFileManager mm;
    private Client client;

    IndexingPreprocess indexingPreprocess;
    // only annotations under these paths will be indexed for the moment
    static final public List<String> toBeIndexed
            = Arrays.asList("$teiCorpus.$teiHeader.$titleStmt.xml:id",
                    "$teiCorpus.$teiHeader.$profileDesc.xml:id",
                    "$teiCorpus.$teiHeader.$profileDesc.$textClass.$keywords.$type_author.xml:id");

    public Indexer() throws UnknownHostException {
        this.mm = MongoFileManager.getInstance(false);
        this.indexingPreprocess = new IndexingPreprocess(this.mm);
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", IndexProperties.getElasticSearchClusterName()).build();
        this.client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(IndexProperties.getElasticSearch_host(), 9300));
    }

    public boolean isIndexExists() {
        boolean fulltextTeisIndexExists = this.client.admin().indices().prepareExists(IndexProperties.getFulltextTeisIndexName()).execute().actionGet().isExists();
        boolean annotsNerdIndexExists = this.client.admin().indices().prepareExists(IndexProperties.getNerdAnnotsIndexName()).execute().actionGet().isExists();
        boolean annotsKeytermIndexExists = this.client.admin().indices().prepareExists(IndexProperties.getKeytermAnnotsIndexName()).execute().actionGet().isExists();
        return (fulltextTeisIndexExists && annotsNerdIndexExists && annotsKeytermIndexExists);
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
            String urlStr = "http://" + IndexProperties.getElasticSearch_host() + ":"
                    + IndexProperties.getElasticSearch_port() + "/" + indexName;
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
        if (indexName.equals(IndexProperties.getNerdAnnotsIndexName())) {
            urlStr += "/annotation_nerd/_mapping";
        } else if (indexName.equals(IndexProperties.getKeytermAnnotsIndexName())) {
            urlStr += "/annotation_keyterm/_mapping";
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
            if (indexName.contains(IndexProperties.getNerdAnnotsIndexName())) {
                mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/annotation_nerd.json"));
            } else if (indexName.contains(IndexProperties.getKeytermAnnotsIndexName())) {
                mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/annotation_keyterm.json"));
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

    /**
     * Indexing of the document collection in ElasticSearch
     */
    public int indexTeiMetadataCollection() {
        int nb = 0;

        for (String date : Utilities.getDates()) {

            if (!IndexProperties.isProcessByDate()) {
                date = null;
            }
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            bulkRequest.setRefresh(true);
            if (mm.initTeis(date, false)) {
                int i = 0;

                while (mm.hasMoreTeis()) {
                    String tei = mm.nextTeiDocument();
                    String id = mm.getCurrentRepositoryDocId();
                    String anhalyticsId = mm.getCurrentAnhalyticsId();
                    boolean isWithFulltext = mm.isCurrentIsWithFulltext();
                    if (!isWithFulltext) {
                        logger.info("skipping " + id + " No fulltext found");
                        continue;
                    }
                    if (anhalyticsId == null || anhalyticsId.isEmpty()) {
                        logger.info("skipping " + id + " No anHALytics id provided");
                        continue;
                    }
                    String jsonStr = null;
                    try {
                        // convert the TEI document into JSON via JsonML
                        //System.out.println(halID);
                        JSONObject json = JsonTapasML.toJSONObject(tei);
                        jsonStr = json.toString();

                        jsonStr = indexingPreprocess.process(jsonStr, id, anhalyticsId);

                        if (jsonStr == null) {
                            continue;
                        }

                        // index the json in ElasticSearch
                        // beware the document type bellow and corresponding mapping!
                        bulkRequest.add(client.prepareIndex(IndexProperties.getMetadataTeisIndexName(), "npl", anhalyticsId).setSource(jsonStr));

                        if (i >= 100) {
                            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                            if (bulkResponse.hasFailures()) {
                                // process failures by iterating through each bulk response item	
                                logger.error(bulkResponse.buildFailureMessage());
                            }
                            bulkRequest = client.prepareBulk();
                            bulkRequest.setRefresh(true);
                            i = 0;
                            System.out.print(".");
                            System.out.flush();
                        }

                        i++;
                        nb++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // last bulk
                if (i != 0) {
                    BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                    System.out.print(".");
                    if (bulkResponse.hasFailures()) {
                        // process failures by iterating through each bulk response item	
                        logger.error(bulkResponse.buildFailureMessage());
                    }
                }
            }

            if (!IndexProperties.isProcessByDate()) {
                break;
            }
        }
        return nb;
    }

    /**
     * Indexing of the document collection in ElasticSearch
     */
    public int indexTeiFulltextCollection() {
        int nb = 0;

        for (String date : Utilities.getDates()) {

            if (!IndexProperties.isProcessByDate()) {
                date = null;
            }
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            bulkRequest.setRefresh(true);
            if (mm.initTeis(date, true)) {
                int i = 0;

                while (mm.hasMoreTeis()) {
                    String tei = mm.nextTeiDocument();
                    String id = mm.getCurrentRepositoryDocId();
                    String anhalyticsId = mm.getCurrentAnhalyticsId();
                    boolean isWithFulltext = mm.isCurrentIsWithFulltext();
                    if (!isWithFulltext) {
                        logger.info("skipping " + id + " No fulltext found");
                        continue;
                    }
                    if (anhalyticsId == null || anhalyticsId.isEmpty()) {
                        logger.info("skipping " + id + " No anHALytics id provided");
                        continue;
                    }
                    String jsonStr = null;
                    try {
                        // convert the TEI document into JSON via JsonML
                        //System.out.println(halID);
                        JSONObject json = JsonTapasML.toJSONObject(tei);
                        jsonStr = json.toString();

                        jsonStr = indexingPreprocess.process(jsonStr, id, anhalyticsId);
                        if (jsonStr == null) {
                            continue;
                        }

                        // index the json in ElasticSearch
                        // beware the document type bellow and corresponding mapping!
                        bulkRequest.add(client.prepareIndex(IndexProperties.getFulltextTeisIndexName(), "npl", anhalyticsId).setSource(jsonStr));

                        if (i >= 100) {
                            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                            if (bulkResponse.hasFailures()) {
                                // process failures by iterating through each bulk response item
                                logger.error(bulkResponse.buildFailureMessage());
                            }
                            bulkRequest = client.prepareBulk();
                            bulkRequest.setRefresh(true);
                            i = 0;
                            System.out.print(".");
                            System.out.flush();
                        }

                        i++;
                        nb++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // last bulk
                if (i != 0) {
                    BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                    System.out.print(".");
                    if (bulkResponse.hasFailures()) {
                        // process failures by iterating through each bulk response item
                        logger.error(bulkResponse.buildFailureMessage());
                    }
                }
            }

            if (!IndexProperties.isProcessByDate()) {
                break;
            }
        }
        return nb;
    }

    /**
     * Indexing of the keyterm annotations in ElasticSearch
     */
    public int indexKeytermAnnotations() {
        int nb = 0;
        for (String date : Utilities.getDates()) {
            if (mm.initAnnotations(date, MongoCollectionsInterface.KEYTERM_ANNOTATIONS)) {
                int i = 0;
                BulkRequestBuilder bulkRequest = client.prepareBulk();
                bulkRequest.setRefresh(true);
                while (mm.hasMoreAnnotations()) {
                    String json = mm.nextAnnotation();
                    String id = mm.getCurrentRepositoryDocId();
                    String anhalyticsId = mm.getCurrentAnhalyticsId();
                    if (anhalyticsId == null || anhalyticsId.isEmpty()) {
                        logger.info("skipping " + id + " No anHALytics id provided");
                        continue;
                    }
                    try {
                        // index the json in ElasticSearch
                        // beware the document type bellow and corresponding mapping!
                        bulkRequest.add(client.prepareIndex(
                                IndexProperties.getKeytermAnnotsIndexName(), "annotation_keyterm",
                                anhalyticsId).setSource(json));

                        if (i >= 200) {
                            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                            if (bulkResponse.hasFailures()) {
                                // process failures by iterating through each bulk response item	
                                logger.error(bulkResponse.buildFailureMessage());
                            }
                            bulkRequest = client.prepareBulk();
                            bulkRequest.setRefresh(true);
                            i = 0;
                            System.out.print(".");
                            System.out.flush();
                        }
                        i++;
                        nb++;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (i != 0) {
                    // last bulk
                    BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                    if (bulkResponse.hasFailures()) {
                        // process failures by iterating through each bulk response item	
                        logger.error(bulkResponse.buildFailureMessage());
                    }
                }
            }
        }
        return nb;
    }

    /**
     * Indexing of the NERD annotations in ElasticSearch
     */
    public int indexNerdAnnotations() {
        int nb = 0;
        ObjectMapper mapper = new ObjectMapper();
        for (String date : Utilities.getDates()) {

            if (!IndexProperties.isProcessByDate()) {
                date = null;
            }
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            bulkRequest.setRefresh(true);
            if (mm.initAnnotations(date, MongoCollectionsInterface.NERD_ANNOTATIONS)) {
                int i = 0;
                while (mm.hasMoreAnnotations()) {
                    String json = mm.nextAnnotation();
                    String id = mm.getCurrentRepositoryDocId();
                    String anhalyticsId = mm.getCurrentAnhalyticsId();
                    if (anhalyticsId == null || anhalyticsId.isEmpty()) {
                        logger.info("skipping " + id + " No anHALytics id provided");
                        continue;
                    }

                    try {
                        // get the xml:id of the elements we want to index from the document
                        // we only index title, abstract and keyphrase annotations !
                        List<String> validIDs = validDocIDs(anhalyticsId, mapper);

                        JsonNode jsonAnnotation = mapper.readTree(json);
                        JsonNode jn = jsonAnnotation.findPath("nerd");

                        JsonNode newNode = mapper.createObjectNode();
                        Iterator<JsonNode> ite = jn.getElements();
                        while (ite.hasNext()) {
                            JsonNode temp = ite.next();
                            JsonNode idNode = temp.findValue("xml:id");
                            String xmlID = idNode.getTextValue();

                            if (!validIDs.contains(xmlID)) {
                                continue;
                            }
                            ((ObjectNode) newNode).put("annotation", temp);
                            String annotJson = newNode.toString();

                            // we do not index the empty annotation results! 
                            // the nerd subdoc has no entites field
                            JsonNode annotNode = temp.findPath("nerd");
                            JsonNode entitiesNode = null;
                            if ((annotNode != null) && (!annotNode.isMissingNode())) {
                                entitiesNode = annotNode.findPath("entities");
                            }

                            if ((entitiesNode == null) || entitiesNode.isMissingNode()) {
                                //System.out.println("Skipping " + annotJson);
                                continue;
                            }

                            // index the json in ElasticSearch
                            // beware the document type bellow and corresponding mapping!
                            bulkRequest.add(client.prepareIndex(
                                    IndexProperties.getNerdAnnotsIndexName(), "annotation_nerd", xmlID).setSource(annotJson));

                            if (i >= 200) {
                                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                                if (bulkResponse.hasFailures()) {
                                    // process failures by iterating through each bulk response item	
                                    logger.error(bulkResponse.buildFailureMessage());
                                }
                                bulkRequest = client.prepareBulk();
                                bulkRequest.setRefresh(true);
                                i = 0;
                                System.out.print(".");
                                System.out.flush();
                            }

                            i++;
                            nb++;

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // last bulk
                if (i != 0) {
                    BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                    System.out.print(".");
                    if (bulkResponse.hasFailures()) {
                        // process failures by iterating through each bulk response item	
                        logger.error(bulkResponse.buildFailureMessage());
                    }
                }
            }

            if (!IndexProperties.isProcessByDate()) {
                break;
            }
        }

        return nb;
    }

    private List<String> validDocIDs(String anhalyticsId, ObjectMapper mapper) {
        List<String> results = new ArrayList<String>();
        logger.debug("validDocIDs: " + anhalyticsId);

        String request = "{\"fields\": [ ";
        boolean first = true;
        for (String path : toBeIndexed) {
            if (first) {
                first = false;
            } else {
                request += ", ";
            }
            request += "\"" + path + "\"";
        }
        request += "], \"query\": { \"filtered\": { \"query\": { \"term\": {\"_id\": \"" + anhalyticsId + "\"}}}}}";
        //System.out.println(request);

        String urlStr = "http://" + IndexProperties.getElasticSearch_host() + ":" + IndexProperties.getElasticSearch_port() + "/" + IndexProperties.getFulltextTeisIndexName() + "/_search";
        StringBuffer json = new StringBuffer();
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf8");

            byte[] postDataBytes = request.getBytes("UTF-8");

            OutputStream os = conn.getOutputStream();
            os.write(postDataBytes);
            os.flush();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                logger.error("Failed, HTTP error code : "
                        + conn.getResponseCode());
                return null;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String line = null;
            while ((line = br.readLine()) != null) {
                json.append(line);
                json.append(" ");
            }
            os.close();
            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JsonNode resJsonStruct = mapper.readTree(json.toString());
            JsonNode hits = resJsonStruct.findPath("hits").findPath("hits");
            if (hits.isArray()) {
                JsonNode hit0 = hits.get(0);
                if (hit0 != null) {
                    JsonNode fields = hit0.findPath("fields");
                    Iterator<JsonNode> ite = fields.getElements();
                    while (ite.hasNext()) {
                        JsonNode idNodes = (JsonNode) ite.next();
                        if (idNodes.isArray()) {
                            Iterator<JsonNode> ite2 = idNodes.getElements();
                            while (ite2.hasNext()) {
                                JsonNode node = (JsonNode) ite2.next();
                                results.add(node.getTextValue());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public void close() {
        this.client.close();
        if (mm != null) {
            mm.close();
        }
    }
}
