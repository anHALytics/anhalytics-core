package fr.inria.anhalytics.index;

import fr.inria.anhalytics.commons.data.Annotation;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.index.exceptions.IndexNotCreatedException;
import java.util.*;

import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JsonTapasML;
import org.json.JSONObject;
import fr.inria.anhalytics.commons.properties.IndexProperties;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import fr.inria.anhalytics.commons.data.BiblioObject;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.*;

/**
 * Method for management of the ElasticSearch cluster.
 *
 * @author Patrice Lopez
 */
public class DocumentIndexer extends Indexer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentIndexer.class);

    IndexingPreprocess indexingPreprocess;
    // only annotations under these paths will be indexed for the moment
    static final public List<String> toBeIndexed
            = Arrays.asList("$teiCorpus.$teiHeader.$fileDesc.$titleStmt.xml:id",
                    "$teiCorpus.$teiHeader.$profileDesc.$abstract.xml:id",
                    "$teiCorpus.$teiHeader.$profileDesc.$textClass.$keywords.$type_author.xml:id");

    public DocumentIndexer() {
        super();
        this.indexingPreprocess = new IndexingPreprocess(this.mm);
    }

    /**
     * Indexing of the fulltext document collection in ElasticSearch.
     */
    public int indexTeiCorpus() {
        int nb = 0;
        if (isIndexExists(IndexProperties.getTeisIndexName())) {
            int bulkSize = 100;
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            //bulkRequest.setRefresh(true);
            bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);

            boolean initResult;
            if (IndexProperties.isReset()) {
                initResult = mm.initObjects(null, MongoFileManager.ONLY_TRANSFORMED_METADATA);
            } else {
                initResult = mm.initObjects(null, MongoFileManager.ONLY_TRANSFORMED_METADATA_NOT_INDEXED);
            }

            if (initResult) {

                while (mm.hasMore()) {
                    BiblioObject biblioObject = mm.nextBiblioObject();
//                    if (!biblioObject.getIsWithFulltext()) {
//                        LOGGER.info("\t\t No fulltext available, Skipping...");
//                        continue;
//                    }
                    if (!IndexProperties.isReset() && biblioObject.getIsIndexed()) {
                        LOGGER.info("\t\t Already indexed, Skipping...");
                        continue;
                    }
                    String jsonStr = null;
                    try {
                        // convert the TEI document into JSON via JsonML
                        String tei = mm.getTEICorpus(biblioObject);
                        JSONObject json = JsonTapasML.toJSONObject(tei);
                        jsonStr = json.toString();

                        jsonStr = indexingPreprocess.process(jsonStr, biblioObject.getRepositoryDocId(), biblioObject.getAnhalyticsId());
                        if (jsonStr == null) {
                            continue;
                        }

                        // index the json in ElasticSearch
                        // beware the document type bellow and corresponding mapping!
                        bulkRequest.add(client.prepareIndex(IndexProperties.getTeisIndexName(), IndexProperties.getTeisTypeName(), biblioObject.getAnhalyticsId()).setSource(jsonStr));
                        nb++;
                        if (nb % bulkSize == 0) {
                            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                            if (bulkResponse.hasFailures()) {
                                // process failures by iterating through each bulk response item
                                LOGGER.error(bulkResponse.buildFailureMessage());
                            }
                            bulkRequest = client.prepareBulk();
                            //bulkRequest.setRefresh(true);
                            bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
                            LOGGER.info("\n Bulk number : " + nb / bulkSize);
                        }
                        biblioObject.setIsIndexed(Boolean.TRUE);
                        mm.updateBiblioObjectStatus(biblioObject, null, false);
                    } catch (Exception e) {
                        LOGGER.error("Error: ", e);
                    }
                }
            }

            // last bulk
            if (nb % bulkSize != 0) {
                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                LOGGER.info("\n One Last Bulk.");
                if (bulkResponse.hasFailures()) {
                    // process failures by iterating through each bulk response item
                    LOGGER.error(bulkResponse.buildFailureMessage());
                }
            }
        } else {
            throw new IndexNotCreatedException();
        }
        return nb;
    }

    /**
     * Indexing of the keyterm annotations in ElasticSearch.
     */
    public int indexKeytermAnnotations() {
        int nb = 0;

        int bulkSize = 200;
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        //bulkRequest.setRefresh(true);
        bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
        if (mm.initObjects(null, MongoFileManager.ONLY_KEYTERM_ANNOTATED)) {
            while (mm.hasMore()) {
                BiblioObject biblioObject = mm.nextBiblioObject();
                Annotation annotation = mm.getKeytermAnnotations(biblioObject.getAnhalyticsId());
                if(annotation == null)
                    continue;
                annotation.setJson(annotation.getJson().replaceAll("_id", "id"));
                if (!IndexProperties.isReset() && annotation.isIsIndexed()) {
                    LOGGER.info("\t\t Already indexed annotations for " + biblioObject.getAnhalyticsId() + ", Skipping...");
                    continue;
                }
                try {
                    // index the json in ElasticSearch
                    // beware the document type bellow and corresponding mapping!
                    bulkRequest.add(client.prepareIndex(
                            IndexProperties.getKeytermAnnotsIndexName(), IndexProperties.getKeytermAnnotsTypeName(),
                            annotation.getAnhalyticsId()).setSource(annotation.getJson()));

                    nb++;
                    if (nb % bulkSize == 0) {
                        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                        if (bulkResponse.hasFailures()) {
                            // process failures by iterating through each bulk response item
                            LOGGER.error(bulkResponse.buildFailureMessage());
                        }
                        bulkRequest = client.prepareBulk();
                        //bulkRequest.setRefresh(true);
                        bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
                        LOGGER.info("\n Bulk number : " + nb / bulkSize);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error: ", e);
                }
            }
        }

        if (nb % bulkSize != 0) {
            // last bulk
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            LOGGER.info("\n One Last Bulk.");
            if (bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item	
                LOGGER.error(bulkResponse.buildFailureMessage());
            }
        }
        return nb;
    }

    /**
     * Indexing of the NERD annotations in ElasticSearch.
     */
    public int indexNerdAnnotations() {
        int nb = 0;
        int bulkSize = 200;
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        //bulkRequest.setRefresh(true);
        bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
        ObjectMapper mapper = new ObjectMapper();
        if (mm.initObjects(null, MongoFileManager.ONLY_NERD_ANNOTATED)) {
            while (mm.hasMore()) {
                BiblioObject biblioObject = mm.nextBiblioObject();
                Annotation annotation = mm.getNerdAnnotations(biblioObject.getAnhalyticsId());
                if(annotation == null)
                    continue;

                annotation.setJson(annotation.getJson().replaceAll("_id", "id"));
                if (!IndexProperties.isReset() && annotation.isIsIndexed()) {
                    LOGGER.info("\t\t Already indexed annotations for " + biblioObject.getAnhalyticsId() + ", Skipping...");
                    continue;
                }

                try {
                    // get the xml:id of the elements we want to index from the document
                    // we only index title, abstract and keyphrase annotations !
                    List<String> validIDs = validDocIDs(annotation.getAnhalyticsId(), mapper);

                    JsonNode jsonAnnotation = mapper.readTree(annotation.getJson());
                    JsonNode jn = jsonAnnotation.findPath("nerd");

                    JsonNode newNode = mapper.createObjectNode();
                    Iterator<JsonNode> ite = jn.elements();
                    while (ite.hasNext()) {
                        JsonNode temp = ite.next();
                        JsonNode idNode = temp.findValue("xml:id");
                        String xmlID = idNode.textValue();

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
                                LOGGER.error(bulkResponse.buildFailureMessage());
                            }
                            bulkRequest = client.prepareBulk();
                            //bulkRequest.setRefresh(true);
                            bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
                            LOGGER.info("\n Bulk number : " + nb / bulkSize);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error: ", e);
                }
            }
        }

        // last bulk
        if (nb % bulkSize != 0) {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            LOGGER.info("\n One Last Bulk.");
            if (bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item	
                LOGGER.error(bulkResponse.buildFailureMessage());
            }
        }
        return nb;
    }

    /**
     * Indexing of the grobid-quantities annotations as independent documents in
     * ElasticSearch.
     */
    public int indexQuantitiesAnnotations() {
        int nb = 0;
        int bulkSize = 200;
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        //bulkRequest.setRefresh(true);
        bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
        //if (mm.initQuantitiesAnnotations()) {
        if (mm.initObjects(null, MongoFileManager.ONLY_QUANTITIES_ANNOTATED)) {
            while (mm.hasMore()) {
                BiblioObject biblioObject = mm.nextBiblioObject();
                Annotation annotation = mm.getQuantitiesAnnotations(biblioObject.getAnhalyticsId());
                if(annotation == null)
                    continue;
                
                annotation.setJson(annotation.getJson().replaceAll("_id", "id"));
                if (!IndexProperties.isReset() && annotation.isIsIndexed()) {
                    LOGGER.info("\t\t Already indexed annotations for " + biblioObject.getAnhalyticsId() + ", Skipping...");
                    continue;
                }
                try {
                    // index the json in ElasticSearch
                    // beware the document type bellow and corresponding mapping!
                    bulkRequest.add(client.prepareIndex(
                            IndexProperties.getQuantitiesAnnotsIndexName(), IndexProperties.getQuantitiesAnnotsTypeName(),
                            annotation.getAnhalyticsId()).setSource(annotation.getJson()));
                    nb++;
                    if (nb % bulkSize == 0) {
                        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                        if (bulkResponse.hasFailures()) {
                            // process failures by iterating through each bulk response item    
                            LOGGER.error(bulkResponse.buildFailureMessage());
                        }
                        bulkRequest = client.prepareBulk();
                        //bulkRequest.setRefresh(true);
                        bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
                        LOGGER.info("\n Bulk number : " + nb / bulkSize);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error: ", e);
                }
            }
        }

        // last bulk
        if (nb % bulkSize != 0) {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            LOGGER.info("\n One Last Bulk.");
            if (bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item    
                LOGGER.error(bulkResponse.buildFailureMessage());
            }
        }

        return nb;
    }

    /**
     * Returns a list of valid xml:ids found in the document.(to check if
     * annotation is to be considered).
     */
    private List<String> validDocIDs(String anhalyticsId, ObjectMapper mapper) {
        List<String> results = new ArrayList<String>();
        LOGGER.info("validDocIDs: " + anhalyticsId);
        //String request[] = toBeIndexed.toArray(new String[0]);
        //String query = "{\"query\": { \"bool\": { \"must\": { \"term\": {\"_id\": \"" + anhalyticsId + "\"}}}}}";

        BoolQueryBuilder builder = QueryBuilders.boolQuery().must(new TermQueryBuilder("_id", anhalyticsId));
        SearchRequestBuilder srb = client.prepareSearch(IndexProperties.getTeisIndexName()).setQuery(builder);
        for (String field : toBeIndexed) {
            srb.addStoredField(field);
        }
        SearchResponse searchResponse = srb.execute().actionGet();
        try {
            JsonNode resJsonStruct = mapper.readTree(searchResponse.toString());
            JsonNode hits = resJsonStruct.findPath("hits").findPath("hits");
            if (hits.isArray()) {
                JsonNode hit0 = hits.get(0);
                if (hit0 != null) {
                    JsonNode fields = hit0.findPath("fields");
                    Iterator<JsonNode> ite = fields.elements();
                    while (ite.hasNext()) {
                        JsonNode idNodes = (JsonNode) ite.next();
                        if (idNodes.isArray()) {
                            Iterator<JsonNode> ite2 = idNodes.elements();
                            while (ite2.hasNext()) {
                                JsonNode node = (JsonNode) ite2.next();
                                results.add(node.textValue());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error: ", e);
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
