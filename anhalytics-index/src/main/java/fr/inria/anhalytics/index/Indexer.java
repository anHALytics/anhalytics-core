package fr.inria.anhalytics.index;



import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.commons.managers.MongoManager;
import java.io.*;
import java.util.*;

import java.net.*;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.common.settings.*;
import org.elasticsearch.client.*;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.index.*;
import org.elasticsearch.common.transport.*;
import org.elasticsearch.client.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JsonTapasML;
import org.json.JSONObject;
import fr.inria.anhalytics.commons.utilities.IndexingPreprocess;
import fr.inria.anhalytics.index.properties.IndexProperties;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Method for management of the ElasticSearch cluster.
 *
 * @author Patrice Lopez
 */
public class Indexer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Indexer.class);

    private Client client = null;

    private final MongoManager mm;

    // only annotations under these paths will be indexed for the moment
    static final public List<String> toBeIndexed
            = Arrays.asList("$teiCorpus.$teiHeader.$titleStmt.xml:id",
                    "$teiCorpus.$teiHeader.$profileDesc.xml:id",
                    "$teiCorpus.$teiHeader.$profileDesc.$textClass.$keywords.$type_author.xml:id");

    public Indexer(MongoManager mm) {
        this.mm = mm;
    }

    /**
     * set-up ElasticSearch by loading the mapping and river json for the HAL
     * document database
     */
    public void setUpElasticSearch(String process) throws Exception {
        try {
            // delete previous index
            deleteIndex();

            // create new index and load the appropriate mapping
            createIndex();
            loadMapping(process);
        } catch (Exception e) {
            throw new Exception("Sep-up of ElasticSearch failed for HAL index.", e);
        }
    }

    /**
     *
     */
    private boolean deleteIndex() throws Exception {
        boolean val = false;
        try {
            String urlStr = "http://" + IndexProperties.getElasticSearch_host() + ":" + IndexProperties.getElasticSearch_port() + "/" + IndexProperties.getWorkingIndexName();
            URL url = new URL(urlStr);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestProperty(
                    "Content-Type", "application/x-www-form-urlencoded");
            httpCon.setRequestMethod("DELETE");
            httpCon.connect();
            System.out.println("ElasticSearch Index " + IndexProperties.getWorkingIndexName() + " deleted: status is "
                    + httpCon.getResponseCode());
            if (httpCon.getResponseCode() == 200) {
                val = true;
            }
            httpCon.disconnect();
        } catch (Exception e) {
            throw new Exception("Cannot delete index for " + IndexProperties.getWorkingIndexName());
        }
        return val;
    }

    /**
     *
     */
    private boolean createIndex() throws Exception {
        boolean val = false;

        // create index
        String urlStr = "http://" + IndexProperties.getElasticSearch_host() + ":" + IndexProperties.getElasticSearch_port() + "/" + IndexProperties.getWorkingIndexName();
        URL url = new URL(urlStr);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestProperty(
                "Content-Type", "application/x-www-form-urlencoded");
        httpCon.setRequestMethod("PUT");

        /*System.out.println("ElasticSearch Index " + indexName + " creation: status is " + 
         httpCon.getResponseCode());
         if (httpCon.getResponseCode() == 200) {
         val = true;
         }*/
        // load custom analyzer
        String analyserStr = null;
        try {
            File file = new File("src/main/resources/elasticSearch/analyzer.json");
            analyserStr = FileUtils.readFileToString(file, "UTF-8");
        } catch (Exception e) {
            throw new Exception("Cannot read analyzer for " + IndexProperties.getWorkingIndexName());
        }

        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("PUT");
        httpCon.addRequestProperty("Content-Type", "text/json");
        OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
        out.write(analyserStr);
        out.close();

        System.out.println("ElasticSearch analyzer for " + IndexProperties.getWorkingIndexName() + " : status is "
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
    private boolean loadMapping(String process) throws Exception {
        boolean val = false;

        String urlStr = "http://" + IndexProperties.getElasticSearch_host() + ":" + IndexProperties.getElasticSearch_port() + "/" + IndexProperties.getWorkingIndexName();
        if (process.equals("annotation")) {
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
            File file;
            if (process.equals("annotation")) {
                file = new File("src/main/resources/elasticSearch/annotation.json");
            } else {
                file = new File("src/main/resources/elasticSearch/npl.json");
            }
            mappingStr = FileUtils.readFileToString(file, "UTF-8");
        } catch (Exception e) {
            throw new Exception("Cannot read mapping for " + IndexProperties.getWorkingIndexName());
        }
        System.out.println(urlStr);

        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("PUT");
        httpCon.addRequestProperty("Content-Type", "text/json");
        OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
        out.write(mappingStr);
        out.close();

        System.out.println("ElasticSearch mapping for " + IndexProperties.getWorkingIndexName() + " : status is "
                + httpCon.getResponseCode());
        if (httpCon.getResponseCode() == 200) {
            val = true;
        }
        return val;
    }

    /**
     * Launch the indexing of the HAL collection in ElasticSearch
     */
    public int indexCollection() throws Exception {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", IndexProperties.getElasticSearchClusterName()).build();
        Client client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(IndexProperties.getElasticSearch_host(), 9300));

        IndexingPreprocess indexingPreprocess = new IndexingPreprocess(mm);

        int nb = 0;
        mm.setGridFS(MongoManager.FINAL_TEIS);
        if (mm.initTeiFiles(null)) {
            int i = 0;
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            bulkRequest.setRefresh(true);
            while (mm.hasMoreDocuments()) {
                String tei = mm.nextDocument();
                String filename = mm.getCurrentFilename();
                String halID = mm.getCurrentHalID();

                // convert the TEI document into JSON via JsonML
                //System.out.println(halID);
                JSONObject json = JsonTapasML.toJSONObject(tei);
                String jsonStr = json.toString();
                try {

                    jsonStr = indexingPreprocess.process(jsonStr, filename);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //System.out.println(jsonStr);

                if (jsonStr == null) {
                    continue;
                }

                // index the json in ElasticSearch
                try {
                    // beware the document type bellow and corresponding mapping!
                    bulkRequest.add(client.prepareIndex(IndexProperties.getTeiIndexName(), "npl", halID).setSource(jsonStr));

                    if (i >= 100) {
                        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                        if (bulkResponse.hasFailures()) {
                            // process failures by iterating through each bulk response item	
                            System.out.println(bulkResponse.buildFailureMessage());
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
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item	
                System.out.println(bulkResponse.buildFailureMessage());
            }
        }
        client.close();

        return nb;
    }

    public void index(String json, String halID) {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", IndexProperties.getElasticSearchClusterName()).build();
        Client client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(IndexProperties.getElasticSearch_host(), 9300));

        IndexResponse response = client.prepareIndex(IndexProperties.getTeiIndexName(), "npl", halID)
                .setSource(json)
                .execute()
                .actionGet();
        client.close();
    }

    /**
     * Launch the indexing of the HAL annotations in ElasticSearch
     */
    public int indexAnnotations() throws Exception {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", IndexProperties.getElasticSearchClusterName()).build();
        Client client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(IndexProperties.getElasticSearch_host(), 9300));
        int nb = 0;
        try {
            ObjectMapper mapper = new ObjectMapper();

            if (mm.initAnnotations()) {
                int i = 0;
                BulkRequestBuilder bulkRequest = client.prepareBulk();
                bulkRequest.setRefresh(true);
                while (mm.hasMoreAnnotations()) {
                    String json = mm.nextAnnotation();
                    String filename = mm.getCurrentFilename();
                    String halID = mm.getCurrentHalID();
                    
                    // get the xml:id of the elements we want to index from the document
                    // we only index title, abstract and keyphrase annotations !
                    List<String> validIDs = validDocIDs(halID, mapper);
                    
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
                        JsonNode nerdNode = temp.findPath("nerd");
                        JsonNode entitiesNode = null;
                        if ((nerdNode != null) && (!nerdNode.isMissingNode())) {
                            entitiesNode = nerdNode.findPath("entities");
                        }

                        if ((entitiesNode == null) || entitiesNode.isMissingNode()) {
                            //System.out.println("Skipping " + annotJson);
                            continue;
                        }

                        // index the json in ElasticSearch
                        try {
                            // beware the document type bellow and corresponding mapping!
                            bulkRequest.add(client.prepareIndex(IndexProperties.getAnnotsIndexName(), "annotation", xmlID).setSource(annotJson));

                            if (i >= 100) {
                                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                                if (bulkResponse.hasFailures()) {
                                    // process failures by iterating through each bulk response item	
                                    System.out.println(bulkResponse.buildFailureMessage());
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
                }
                // last bulk
                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                if (bulkResponse.hasFailures()) {
                    // process failures by iterating through each bulk response item	
                    System.out.println(bulkResponse.buildFailureMessage());
                }
                System.out.print("\n");
            }
        } finally {
            if (mm != null) {
                mm.close();
            }
        }
        return nb;
    }

    private List<String> validDocIDs(String halID, ObjectMapper mapper) {
        List<String> results = new ArrayList<String>();
        System.out.println("validDocIDs: " + halID);

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
        request += "], \"query\": { \"filtered\": { \"query\": { \"term\": {\"_id\": \"" + halID + "\"}}}}}";
        //System.out.println(request);

        String urlStr = "http://" + IndexProperties.getElasticSearch_host() + ":" + IndexProperties.getElasticSearch_port() + "/" + IndexProperties.getTeiIndexName() + "/_search";
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
                System.out.println("Failed, HTTP error code : "
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
}
