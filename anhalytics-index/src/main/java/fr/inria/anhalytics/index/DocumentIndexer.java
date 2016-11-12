package fr.inria.anhalytics.index;

import fr.inria.anhalytics.index.exceptions.IndexNotCreatedException;
import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import java.io.*;
import java.util.*;

import java.net.*;
import org.elasticsearch.action.bulk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JsonTapasML;
import org.json.JSONObject;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.commons.properties.IndexProperties;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Method for management of the ElasticSearch cluster.
 *
 * @author Patrice Lopez
 */
public class DocumentIndexer extends Indexer {

    private static final Logger logger = LoggerFactory.getLogger(DocumentIndexer.class);

    private final MongoFileManager mm;

    IndexingPreprocess indexingPreprocess;
    // only annotations under these paths will be indexed for the moment
    static final public List<String> toBeIndexed
            = Arrays.asList("$teiCorpus.$teiHeader.$fileDesc.$titleStmt.xml:id",
                    "$teiCorpus.$teiHeader.$profileDesc.xml:id",
                    "$teiCorpus.$teiHeader.$profileDesc.$textClass.$keywords.$type_author.xml:id");

    public DocumentIndexer() {
        super();
        try {
            this.mm = MongoFileManager.getInstance(false);
        } catch (ServiceException ex) {
            throw new ServiceException("MongoDB is not UP, the process will be halted.");
        }
        this.indexingPreprocess = new IndexingPreprocess(this.mm);
    }

    /**
     * Indexing of the document collection in ElasticSearch
     */
    public int indexTeiMetadataCollection() {
        int nb = 0;
        int bulkSize = 100;
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.setRefresh(true);
        for (String date : Utilities.getDates()) {

            if (!IndexProperties.isProcessByDate()) {
                date = null;
            }
            if (mm.initTeis(date, false, MongoCollectionsInterface.FINAL_TEIS)) {
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
                        bulkRequest.add(client.prepareIndex(IndexProperties.getMetadataTeisIndexName(), IndexProperties.getFulltextTeisTypeName(), anhalyticsId).setSource(jsonStr));

                        nb++;
                        if (nb % bulkSize == 0) {
                            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                            if (bulkResponse.hasFailures()) {
                                // process failures by iterating through each bulk response item	
                                logger.error(bulkResponse.buildFailureMessage());
                            }
                            bulkRequest = client.prepareBulk();
                            bulkRequest.setRefresh(true);
                            logger.info("\n Bulk number : " + nb / bulkSize);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (!IndexProperties.isProcessByDate()) {
                break;
            }
        }
        // last bulk
        if (nb % bulkSize != 0) {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            logger.info("\n One Last Bulk.");
            if (bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item	
                logger.error(bulkResponse.buildFailureMessage());
            }
        }
        return nb;
    }

    /**
     * Indexing of the document collection in ElasticSearch
     */
    public int indexTeiFulltextCollection() {
        int nb = 0;
        if (isIndexExists(IndexProperties.getFulltextTeisIndexName())) {
            int bulkSize = 100;
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            bulkRequest.setRefresh(true);
            for (String date : Utilities.getDates()) {

                if (!IndexProperties.isProcessByDate()) {
                    date = null;
                }
                if (mm.initTeis(date, true, MongoCollectionsInterface.FINAL_TEIS)) {

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
                            bulkRequest.add(client.prepareIndex(IndexProperties.getFulltextTeisIndexName(), IndexProperties.getFulltextTeisTypeName(), anhalyticsId).setSource(jsonStr));
                            nb++;
                            if (nb % bulkSize == 0) {
                                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                                if (bulkResponse.hasFailures()) {
                                    // process failures by iterating through each bulk response item
                                    logger.error(bulkResponse.buildFailureMessage());
                                }
                                bulkRequest = client.prepareBulk();
                                bulkRequest.setRefresh(true);
                                logger.info("\n Bulk number : " + nb / bulkSize);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!IndexProperties.isProcessByDate()) {
                    break;
                }
            }
            // last bulk
            if (nb % bulkSize != 0) {
                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                logger.info("\n One Last Bulk.");
                if (bulkResponse.hasFailures()) {
                    // process failures by iterating through each bulk response item
                    logger.error(bulkResponse.buildFailureMessage());
                }
            }
        } else {
            throw new IndexNotCreatedException();
        }
        return nb;
    }

    /**
     * Indexing of the keyterm annotations in ElasticSearch
     */
    public int indexKeytermAnnotations() {
        int nb = 0;

        int bulkSize = 200;
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.setRefresh(true);
        for (String date : Utilities.getDates()) {
            if (!IndexProperties.isProcessByDate()) {
                date = null;
            }
            if (mm.initAnnotations(date, MongoCollectionsInterface.KEYTERM_ANNOTATIONS)) {
                while (mm.hasMoreAnnotations()) {
                    String json = mm.nextAnnotation();
                    json = json.replaceAll("_id", "id");
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
                                IndexProperties.getKeytermAnnotsIndexName(), IndexProperties.getKeytermAnnotsTypeName(),
                                anhalyticsId).setSource(json));

                        nb++;
                        if (nb % bulkSize == 0) {
                            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                            if (bulkResponse.hasFailures()) {
                                // process failures by iterating through each bulk response item	
                                logger.error(bulkResponse.buildFailureMessage());
                            }
                            bulkRequest = client.prepareBulk();
                            bulkRequest.setRefresh(true);
                            logger.info("\n Bulk number : " + nb / bulkSize);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!IndexProperties.isProcessByDate()) {
                break;
            }
        }
        if (nb % bulkSize != 0) {
            // last bulk
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            logger.info("\n One Last Bulk.");
            if (bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item	
                logger.error(bulkResponse.buildFailureMessage());
            }
        }
        return nb;
    }

    /**
     * Indexing of the NERD annotations in ElasticSearch
     */
    public int indexNerdAnnotations() {
        int nb = 0;
        int bulkSize = 200;
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.setRefresh(true);
        ObjectMapper mapper = new ObjectMapper();
        for (String date : Utilities.getDates()) {
            if (!IndexProperties.isProcessByDate()) {
                date = null;
            }
            if (mm.initAnnotations(date, MongoCollectionsInterface.NERD_ANNOTATIONS)) {
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
                                    IndexProperties.getNerdAnnotsIndexName(), IndexProperties.getNerdAnnotsTypeName(), xmlID).setSource(annotJson));

                            nb++;
                            if (nb % bulkSize == 0) {
                                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                                if (bulkResponse.hasFailures()) {
                                    // process failures by iterating through each bulk response item	
                                    logger.error(bulkResponse.buildFailureMessage());
                                }
                                bulkRequest = client.prepareBulk();
                                bulkRequest.setRefresh(true);
                                logger.info("\n Bulk number : " + nb / bulkSize);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (!IndexProperties.isProcessByDate()) {
                break;
            }
        }

        // last bulk
        if (nb % bulkSize != 0) {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            logger.info("\n One Last Bulk.");
            if (bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item	
                logger.error(bulkResponse.buildFailureMessage());
            }
        }
        return nb;
    }

    private List<String> validDocIDs(String anhalyticsId, ObjectMapper mapper) {
        List<String> results = new ArrayList<String>();
        logger.info("validDocIDs: " + anhalyticsId);
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

    @Override
    public void close() {
        super.close();
        if (mm != null) {
            mm.close();
        }
    }
}
