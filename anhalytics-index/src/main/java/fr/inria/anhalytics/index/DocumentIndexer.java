package fr.inria.anhalytics.index;

import fr.inria.anhalytics.commons.data.Annotation;
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

/*import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;*/
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.*;

/**
 * Method for management of the ElasticSearch cluster.
 *
 * @author Patrice Lopez
 */
public class DocumentIndexer extends Indexer {

    private static final Logger logger = LoggerFactory.getLogger(DocumentIndexer.class);

    IndexingPreprocess indexingPreprocess;
    // only annotations under these paths will be indexed for the moment
    static final public List<String> toBeIndexed
            = Arrays.asList("$teiCorpus.$teiHeader.$fileDesc.$titleStmt.xml:id",
                    "$teiCorpus.$teiHeader.$profileDesc.xml:id",
                    "$teiCorpus.$teiHeader.$profileDesc.$textClass.$keywords.$type_author.xml:id");

    public DocumentIndexer() {
        super();
        this.indexingPreprocess = new IndexingPreprocess(this.mm);
    }

    /*public int indexIstexQuantites() throws XPathExpressionException, JSONException {
        int nb = 0;

        int bulkSize = 100;
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.setRefresh(true);
        if (mm.initQuantitiesAnnotations()) {
            while (mm.hasMore()) {
                Annotation annotation = mm.nextQuantitiesAnnotation();
                JSONObject jsonObj = new JSONObject(annotation.getJson());
                String tei = mm.findGridFSDBfileIstexTeiById(annotation.getAnhalyticsId());
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                docFactory.setValidating(false);
                //docFactory.setNamespaceAware(true);
                DocumentBuilder docBuilder;
                try {
                    docBuilder = docFactory.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    throw new SystemException("Cannot instantiate TeiBuilder", e);
                }
                Document teiDoc = null;
                try {
                    teiDoc = docBuilder.parse(new InputSource(new ByteArrayInputStream(tei.getBytes("utf-8"))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                XPath xPath = XPathFactory.newInstance().newXPath();
                Node text = (Node) xPath.compile("/TEI/text").evaluate(teiDoc, XPathConstants.NODE);
                if (text != null) {
                    text.getParentNode().removeChild(text);
                }
                JSONObject json = JsonTapasML.toJSONObject(Utilities.toString(teiDoc));
                json.put("quantities", jsonObj);
                try {
                    // index the json in ElasticSearch
                    // beware the document type bellow and corresponding mapping!
                    bulkRequest.add(client.prepareIndex(
                            "quantities", "quantities",
                            annotation.getAnhalyticsId()).setSource(json.toString()));

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
        return nb;
    }*/
    /**
     * Indexing of the document collection in ElasticSearch
     */
    /* PL: not clear what is it for? metadata are already in the TEI full text normally */
    public int indexTeiMetadataCollection() {
        int nb = 0;
        int bulkSize = 100;
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        //bulkRequest.setRefresh(true);
        bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);

        if (mm.initObjects(null)) {
            while (mm.hasMore()) {
                BiblioObject biblioObject = mm.nextBiblioObject();
                if (!IndexProperties.isReset() && biblioObject.getIsIndexed()) {
                    logger.info("\t\t Already indexed, Skipping...");
                    continue;
                }
                String jsonStr = null;
                try {
                    // convert the TEI document into JSON via JsonML
                    String tei = mm.getTEICorpus(biblioObject);
                    JSONObject json = JsonTapasML.toJSONObject(tei);
                    jsonStr = json.toString();

                    // PL: this will add all the annotations in the "TeiMetadata" collection, do we want/need this? 
                    jsonStr = indexingPreprocess.process(jsonStr, biblioObject.getRepositoryDocId(), biblioObject.getAnhalyticsId());
                    if (jsonStr == null) {
                        continue;
                    }

                    // index the json in ElasticSearch
                    // beware the document type bellow and corresponding mapping!
                    bulkRequest.add(client.prepareIndex(IndexProperties.getMetadataTeisIndexName(),
                            IndexProperties.getFulltextTeisTypeName(),
                            biblioObject.getAnhalyticsId()).setSource(jsonStr));

                    nb++;
                    if (nb % bulkSize == 0) {
                        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                        if (bulkResponse.hasFailures()) {
                            // process failures by iterating through each bulk response item	
                            logger.error(bulkResponse.buildFailureMessage());
                        }
                        bulkRequest = client.prepareBulk();
                        //bulkRequest.setRefresh(true);
                        bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
                        logger.info("\n Bulk number : " + nb / bulkSize);
                    }
                    biblioObject.setIsIndexed(Boolean.TRUE);
                    mm.updateBiblioObjectStatus(biblioObject, null, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            //bulkRequest.setRefresh(true);
            bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);

            if (mm.initObjects(null)) {

                while (mm.hasMore()) {
                    BiblioObject biblioObject = mm.nextBiblioObject();
                    if (!biblioObject.getIsWithFulltext()) {
                        logger.info("\t\t No fulltext available, Skipping...");
                        continue;
                    }
                    if (!IndexProperties.isReset() && biblioObject.getIsIndexed()) {
                        logger.info("\t\t Already indexed, Skipping...");
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
                        bulkRequest.add(client.prepareIndex(IndexProperties.getFulltextTeisIndexName(), IndexProperties.getFulltextTeisTypeName(), biblioObject.getAnhalyticsId()).setSource(jsonStr));
                        nb++;
                        if (nb % bulkSize == 0) {
                            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                            if (bulkResponse.hasFailures()) {
                                // process failures by iterating through each bulk response item
                                logger.error(bulkResponse.buildFailureMessage());
                            }
                            bulkRequest = client.prepareBulk();
                            //bulkRequest.setRefresh(true);
                            bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
                            logger.info("\n Bulk number : " + nb / bulkSize);
                        }
                        biblioObject.setIsIndexed(Boolean.TRUE);
                        mm.updateBiblioObjectStatus(biblioObject, null, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        //bulkRequest.setRefresh(true);
        bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
        if (mm.initObjects(null)) {
            while (mm.hasMore()) {
                BiblioObject biblioObject = mm.nextBiblioObject();
                Annotation annotation = mm.getNerdAnnotations(biblioObject.getAnhalyticsId());
                annotation.setJson(annotation.getJson().replaceAll("_id", "id"));
                if (!IndexProperties.isReset() && annotation.isIsIndexed()) {
                    logger.info("\t\t Already indexed annotations for " + biblioObject.getAnhalyticsId() + ", Skipping...");
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
                            logger.error(bulkResponse.buildFailureMessage());
                        }
                        bulkRequest = client.prepareBulk();
                        //bulkRequest.setRefresh(true);
                        bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
                        logger.info("\n Bulk number : " + nb / bulkSize);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        //bulkRequest.setRefresh(true);
        bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
        ObjectMapper mapper = new ObjectMapper();
        if (mm.initObjects(null)) {
            while (mm.hasMore()) {
                BiblioObject biblioObject = mm.nextBiblioObject();
                Annotation annotation = mm.getNerdAnnotations(biblioObject.getAnhalyticsId());
                annotation.setJson(annotation.getJson().replaceAll("_id", "id"));
                if (!IndexProperties.isReset() && annotation.isIsIndexed()) {
                    logger.info("\t\t Already indexed annotations for " + biblioObject.getAnhalyticsId() + ", Skipping...");
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
                                logger.error(bulkResponse.buildFailureMessage());
                            }
                            bulkRequest = client.prepareBulk();
                            //bulkRequest.setRefresh(true);
                            bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
                            logger.info("\n Bulk number : " + nb / bulkSize);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
     * Indexing of the grobid-quantities annotations as independent documents in
     * ElasticSearch
     */
    public int indexQuantitiesAnnotations() {
        int nb = 0;
        int bulkSize = 200;
        ObjectMapper mapper = new ObjectMapper();
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        //bulkRequest.setRefresh(true);
        bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
        //if (mm.initQuantitiesAnnotations()) {
        if (mm.initObjects(null)) {
            while (mm.hasMore()) {
                BiblioObject biblioObject = mm.nextBiblioObject();
                Annotation annotation = mm.getNerdAnnotations(biblioObject.getAnhalyticsId());
                annotation.setJson(annotation.getJson().replaceAll("_id", "id"));
                if (!IndexProperties.isReset() && annotation.isIsIndexed()) {
                    logger.info("\t\t Already indexed annotations for " + biblioObject.getAnhalyticsId() + ", Skipping...");
                    continue;
                }

                try {
                    JsonNode jsonAnnotation = mapper.readTree(annotation.getJson());
                    JsonNode jn = jsonAnnotation.findPath("annotation");
                    Iterator<JsonNode> ite = jn.elements();
                    while (ite.hasNext()) {
                        JsonNode temp = ite.next();
                        JsonNode idNode = temp.findValue("xml:id");
                        if ((idNode == null) || (idNode.isMissingNode())) {
                            continue;
                        }
                        String xmlID = idNode.textValue();

                        // we do not index the empty annotation results! ls
                        // the nerd subdoc has no entites field
                        JsonNode annotNode = temp.findPath("quantities");
                        if ((annotNode == null) || (annotNode.isMissingNode())) {
                            continue;
                        }
                        JsonNode annotNode2 = annotNode.findPath("measurements");
                        if ((annotNode2 == null) || (annotNode2.isMissingNode())) {
                            continue;
                        }
                        Iterator<JsonNode> ite2 = annotNode2.elements();
                        int rank = 0;
                        while (ite2.hasNext()) {
                            JsonNode temp2 = ite2.next();
                            ((ObjectNode) temp2).put("xml:id", xmlID);
                            ((ObjectNode) temp2).put("anHALyticsID", annotation.getAnhalyticsId());
                            JsonNode newNode = mapper.createObjectNode();

                            // we try to enrich with a range or atomic value
                            JsonNode typeNode = temp2.findPath("type");
                            if ((typeNode != null) && (!typeNode.isMissingNode())) {
                                String type = typeNode.textValue();

                                if (type.equals("value")) {
                                    //JsonNode newNode = mapper.createArrayNode();
                                    JsonNode quantity = temp2.findPath("quantity");
                                    if ((quantity != null) && (!quantity.isMissingNode())) {
                                        JsonNode normalizedQuantity = quantity.findPath("normalizedQuantity");
                                        if ((normalizedQuantity != null) && (!normalizedQuantity.isMissingNode())) {
                                            Double val = normalizedQuantity.doubleValue();
                                            ((ObjectNode) temp2).put("atomic", val);
                                        }
                                    }
                                } else if (type.equals("interval")) {
                                    JsonNode quantityMost = temp2.findPath("quantityMost");
                                    JsonNode quantityLeast = temp2.findPath("quantityLeast");

                                    if ((quantityMost != null) && (!quantityMost.isMissingNode())
                                            && (quantityLeast != null) && (!quantityLeast.isMissingNode())) {

                                        JsonNode normalizedQuantityLeast = quantityLeast.findPath("normalizedQuantity");
                                        if ((normalizedQuantityLeast != null) && (!normalizedQuantityLeast.isMissingNode())) {
                                            Double valLeast = normalizedQuantityLeast.doubleValue();

                                            JsonNode normalizedQuantityMost = quantityMost.findPath("normalizedQuantity");
                                            if ((normalizedQuantityMost != null) && (!normalizedQuantityMost.isMissingNode())) {
                                                Double valMost = normalizedQuantityMost.doubleValue();
                                                JsonNode range = mapper.createObjectNode();
                                                ((ObjectNode) range).put("lte", valMost);
                                                ((ObjectNode) range).put("gte", valLeast);
                                                ((ObjectNode) temp2).put("range", range);
                                            }
                                        }
                                    }
                                } else if (type.equals("listc")) {
                                    JsonNode quantitiesList = temp2.findPath("quantities");
                                    if ((quantitiesList == null) || (quantitiesList.isMissingNode())) {
                                        continue;
                                    }
                                    // quantitiesList here is a list with a list of quantity values
                                    // to be done...
                                }
                            }

                            ((ObjectNode) newNode).put("measurement", temp2);
                            String annotJson = newNode.toString();

                            // index the json in ElasticSearch
                            // beware the document type bellow and corresponding mapping!
                            bulkRequest.add(client.prepareIndex(
                                    IndexProperties.getQuantitiesAnnotsIndexName(), IndexProperties.getQuantitiesAnnotsTypeName(),
                                    xmlID + "_" + rank).setSource(annotJson));
                            nb++;
                            rank++;
                            if (nb % bulkSize == 0) {
                                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                                if (bulkResponse.hasFailures()) {
                                    // process failures by iterating through each bulk response item    
                                    logger.error(bulkResponse.buildFailureMessage());
                                }
                                bulkRequest = client.prepareBulk();
                                //bulkRequest.setRefresh(true);
                                bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
                                logger.info("\n Bulk number : " + nb / bulkSize);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        //String request[] = toBeIndexed.toArray(new String[0]);
        //String query = "{\"query\": { \"bool\": { \"must\": { \"term\": {\"_id\": \"" + anhalyticsId + "\"}}}}}";

        BoolQueryBuilder builder = QueryBuilders.boolQuery().must(new TermQueryBuilder("_id", anhalyticsId));
        SearchRequestBuilder srb = client.prepareSearch(IndexProperties.getFulltextTeisIndexName()).setQuery(builder);
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
