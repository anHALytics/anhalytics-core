package fr.inria.anhalytics.index;

import fr.inria.anhalytics.index.exceptions.ElasticSearchConfigurationException;
import fr.inria.anhalytics.index.exceptions.IndexingServiceException;
import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.properties.IndexProperties;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.IndexNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author azhar
 */
abstract class Indexer {

    private static final Logger logger = LoggerFactory.getLogger(Indexer.class);

    protected MongoFileManager mm;

    protected TransportClient client;

    public Indexer() {
        this.mm = MongoFileManager.getInstance(false);
        try {
            Settings settings = Settings.settingsBuilder()
                    .put("cluster.name", IndexProperties.getElasticSearchClusterName()).build();
            this.client = TransportClient.builder().settings(settings).build()
                    .addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(IndexProperties.getElasticSearch_host(), Integer.parseInt(IndexProperties.getElasticSearch_port()))));
            int nodes = this.client.connectedNodes().size();
            if (nodes == 0) {
                throw new ServiceException("Cannot find elasticsearch cluster.");
            }
        } catch (ElasticsearchException e) {
            throw new ServiceException("Cannot find elasticsearch cluster", e);
        }
    }

    public void close() {
        this.client.close();
    }

    public boolean isIndexExists(String indexName) {
        boolean exists = this.client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
        return exists;
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
        } catch (Exception e) {
            logger.error("Sep-up of ElasticSearch failed for index " + indexName + ".", e);
            e.printStackTrace();
        }
    }

    /**
     * set-up ElasticSearch by loading the mapping and river json for the HAL
     * document database
     */
    public void setupQuantitiesIndex() {
        try {
            // delete previous index
            deleteIndex("quantities");
            // create new index and load the appropriate mapping
            createQuantitiesIndex();
        } catch (Exception e) {
            logger.error("Sep-up of ElasticSearch failed for index " + "quantities" + ".", e);
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public boolean createQuantitiesIndex() {
        boolean val = false;
        if (!client.admin().indices().prepareExists("quantities").execute().actionGet().isExists()) {
            // load custom analyzer
            String analyserStr = null;
            try {
                ClassLoader classLoader = DocumentIndexer.class.getClassLoader();
                analyserStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/analyzer.json"));
            } catch (Exception e) {
                throw new ElasticSearchConfigurationException("Cannot read analyzer for " + "quantities");
            }

            CreateIndexRequestBuilder createIndexRequestBuilder = this.client.admin().indices().prepareCreate("quantities").setSettings(analyserStr);

            createIndexRequestBuilder.addMapping("quantities", loadMapping("quantities", "quantities"));

            final CreateIndexResponse createResponse = createIndexRequestBuilder.execute().actionGet();
            if (!createResponse.isAcknowledged()) {
                throw new IndexingServiceException("Failed to create index <" + "quantities" + ">");
            }
            logger.info("Index {} created", "quantities");
        } else {
            logger.info("Index {} already exists", "quantities");
        }
        val = true;
        return val;
    }

    /**
     *
     */
    private boolean deleteIndex(String indexName) {
        boolean val = false;
        try {
            DeleteIndexResponse deleteResponse = this.client.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet();

            if (deleteResponse.isAcknowledged()) {
                logger.info("Index {} deleted", indexName);
                val = true;
            } else {
                logger.error("Could not delete index " + indexName);
            }
        } catch (IndexNotFoundException e) {
            logger.info("Index " + indexName + " not found.");

        }
        return val;
    }

    /**
     *
     */
    public boolean createIndex(String indexName) {
        boolean val = false;
        if (!client.admin().indices().prepareExists(indexName).execute().actionGet().isExists()) {
            // load custom analyzer
            String analyserStr = null;
            try {
                ClassLoader classLoader = DocumentIndexer.class.getClassLoader();
                analyserStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/analyzer.json"));
            } catch (Exception e) {
                throw new ElasticSearchConfigurationException("Cannot read analyzer for " + indexName);
            }

            CreateIndexRequestBuilder createIndexRequestBuilder = this.client.admin().indices().prepareCreate(indexName).setSettings(analyserStr);

            if (indexName.equals(IndexProperties.getNerdAnnotsIndexName())) {
                createIndexRequestBuilder.addMapping(IndexProperties.getNerdAnnotsTypeName(), loadMapping(indexName, IndexProperties.getNerdAnnotsTypeName()));
            } else if (indexName.equals(IndexProperties.getKeytermAnnotsIndexName())) {
                createIndexRequestBuilder.addMapping(IndexProperties.getKeytermAnnotsTypeName(), loadMapping(indexName, IndexProperties.getKeytermAnnotsTypeName()));
            } else if (indexName.equals(IndexProperties.getKbIndexName())) {
                createIndexRequestBuilder.addMapping(IndexProperties.getKbAuthorsTypeName(), loadMapping(indexName, IndexProperties.getKbAuthorsTypeName()));
                createIndexRequestBuilder.addMapping(IndexProperties.getKbOrganisationsTypeName(), loadMapping(indexName, IndexProperties.getKbOrganisationsTypeName()));
                createIndexRequestBuilder.addMapping(IndexProperties.getKbPublicationsTypeName(), loadMapping(indexName, IndexProperties.getKbPublicationsTypeName()));
            } else {
                createIndexRequestBuilder.addMapping(IndexProperties.getFulltextTeisTypeName(), loadMapping(indexName, IndexProperties.getFulltextTeisTypeName()));
            }

            final CreateIndexResponse createResponse = createIndexRequestBuilder.execute().actionGet();
            if (!createResponse.isAcknowledged()) {
                throw new IndexingServiceException("Failed to create index <" + indexName + ">");
            }
            logger.info("Index {} created", indexName);
        } else {
            logger.info("Index {} already exists", indexName);
        }
        val = true;
        return val;
    }

    private String loadMapping(String indexName, String type) throws ElasticSearchConfigurationException {
        String mappingStr = null;
        try {
            ClassLoader classLoader = DocumentIndexer.class.getClassLoader();
            if (indexName.contains(IndexProperties.getNerdAnnotsIndexName())) {
                mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/annotation_nerd.json"));
            } else if (indexName.contains(IndexProperties.getKeytermAnnotsIndexName())) {
                mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/annotation_keyterm.json"));
            } else if (indexName.equals(IndexProperties.getKbIndexName())) {
                if (type.equals(IndexProperties.getKbAuthorsTypeName())) {
                    mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/kbauthors.json"));
                } else if (type.equals(IndexProperties.getKbOrganisationsTypeName())) {
                    mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/kborganisations.json"));
                } else {
                    mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/kbpublications.json"));
                }
            } else if (indexName.contains("quantities")) {
                mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/annotation_quantities.json"));
            } else {
                mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/npl.json"));
            }
        } catch (Exception e) {
            throw new ElasticSearchConfigurationException("Cannot read mapping for " + indexName);
        }
        return mappingStr;
    }

}
