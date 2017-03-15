package fr.inria.anhalytics.index;

import fr.inria.anhalytics.commons.data.Annotation;
import fr.inria.anhalytics.commons.data.TEIFile;
import fr.inria.anhalytics.commons.exceptions.SystemException;
import fr.inria.anhalytics.index.exceptions.IndexNotCreatedException;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import java.util.*;

import org.elasticsearch.action.bulk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JsonTapasML;
import org.json.JSONObject;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.commons.properties.IndexProperties;
import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.io.*;

/*import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;*/

import org.elasticsearch.action.search.SearchResponse;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

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
    public int indexTeiMetadataCollection() {
        int nb = 0;
        int bulkSize = 100;
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.setRefresh(true);
        for (String date : Utilities.getDates()) {

            if (!IndexProperties.isProcessByDate()) {
                date = null;
            }
            if (mm.initTeis(date, MongoCollectionsInterface.METADATAS_TEIS)) {
                while (mm.hasMore()) {
                    TEIFile tei = mm.nextTeiDocument();
                    if (tei.getAnhalyticsId() == null || tei.getAnhalyticsId().isEmpty()) {
                        logger.info("skipping " + tei.getRepositoryDocId() + " No anHALytics id provided");
                        continue;
                    }
                    String jsonStr = null;
                    try {
                        // convert the TEI document into JSON via JsonML
                        //System.out.println(halID);
                        JSONObject json = JsonTapasML.toJSONObject(tei.getTei());
                        jsonStr = json.toString();

                        jsonStr = indexingPreprocess.process(jsonStr, tei.getRepositoryDocId(), tei.getAnhalyticsId());

                        if (jsonStr == null) {
                            continue;
                        }

                        // index the json in ElasticSearch
                        // beware the document type bellow and corresponding mapping!
                        bulkRequest.add(client.prepareIndex(IndexProperties.getMetadataTeisIndexName(), IndexProperties.getFulltextTeisTypeName(), tei.getAnhalyticsId()).setSource(jsonStr));

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
                if (mm.initTeis(date, MongoCollectionsInterface.METADATA_WITHFULLTEXT_TEIS)) {

                    while (mm.hasMore()) {
                        TEIFile tei = mm.nextTeiDocument();
                        if (tei.getAnhalyticsId() == null || tei.getAnhalyticsId().isEmpty()) {
                            logger.info("skipping " + tei.getRepositoryDocId() + " No anHALytics id provided");
                            continue;
                        }
                        String jsonStr = null;
                        try {
                            // convert the TEI document into JSON via JsonML
                            //System.out.println(halID);
                            JSONObject json = JsonTapasML.toJSONObject(tei.getTei());
                            jsonStr = json.toString();

                            jsonStr = indexingPreprocess.process(jsonStr, tei.getRepositoryDocId(), tei.getAnhalyticsId());
                            if (jsonStr == null) {
                                continue;
                            }

                            // index the json in ElasticSearch
                            // beware the document type bellow and corresponding mapping!
                            bulkRequest.add(client.prepareIndex(IndexProperties.getFulltextTeisIndexName(), IndexProperties.getFulltextTeisTypeName(), tei.getAnhalyticsId()).setSource(jsonStr));
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
                while (mm.hasMore()) {
                    Annotation annotation = mm.nextAnnotation();
                    annotation.setJson(annotation.getJson().replaceAll("_id", "id"));
                    if (annotation.getAnhalyticsId() == null || annotation.getAnhalyticsId().isEmpty()) {
                        logger.info("skipping " + annotation.getRepositoryDocId() + " No anHALytics id provided");
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
                while (mm.hasMore()) {
                    Annotation annotation = mm.nextAnnotation();
                    if (annotation.getAnhalyticsId() == null || annotation.getAnhalyticsId().isEmpty()) {
                        logger.info("skipping " + annotation.getRepositoryDocId() + " No anHALytics id provided");
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

    /**
     * Indexing of the grobid-quantities annotations in ElasticSearch
     */
    public int indexQuantitiesAnnotations() {
        int nb = 0;
        int bulkSize = 200;
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.setRefresh(true);
        if (mm.initQuantitiesAnnotations()) {
            while (mm.hasMore()) {
                Annotation annotation = mm.nextQuantitiesAnnotation();
                if (annotation.getAnhalyticsId() == null || annotation.getAnhalyticsId().isEmpty()) {
                    logger.info("skipping " + annotation.getRepositoryDocId() + " No anHALytics id provided");
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
    }

    private List<String> validDocIDs(String anhalyticsId, ObjectMapper mapper) {
        List<String> results = new ArrayList<String>();
        logger.info("validDocIDs: " + anhalyticsId);
        String request[] = toBeIndexed.toArray(new String[0]);
        String query = "{\"query\": { \"filtered\": { \"query\": { \"term\": {\"_id\": \"" + anhalyticsId + "\"}}}}}";

        SearchResponse searchResponse = client.prepareSearch(IndexProperties.getFulltextTeisIndexName()).setQuery(query).addFields(request).execute().actionGet();

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
