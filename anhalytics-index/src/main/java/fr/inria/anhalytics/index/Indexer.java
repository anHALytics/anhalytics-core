package fr.inria.anhalytics.index;

import fr.inria.anhalytics.commons.exceptions.ElasticSearchConfigurationException;
import fr.inria.anhalytics.commons.properties.IndexProperties;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author azhar
 */
abstract class Indexer {
    
    private static final Logger logger = LoggerFactory.getLogger(Indexer.class);
    
    protected Client client;
    
    public Indexer(){
    Settings settings = Settings.settingsBuilder()
                .put("cluster.name", IndexProperties.getElasticSearchClusterName()).build();
        this.client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(IndexProperties.getElasticSearch_host(), 9300)));
    }
    
    public void close() {
    this.client.close();
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
            if(!indexName.equals(IndexProperties.getKbIndexName()))
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
            java.util.logging.Logger.getLogger(DocumentIndexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestProperty(
                "Content-Type", "application/x-www-form-urlencoded");
        try {
            httpCon.setRequestMethod("PUT");
        } catch (ProtocolException ex) {
            java.util.logging.Logger.getLogger(DocumentIndexer.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*System.out.println("ElasticSearch Index " + indexName + " creation: status is " + 
         httpCon.getResponseCode());
         if (httpCon.getResponseCode() == 200) {
         val = true;
         }*/
        // load custom analyzer
        String analyserStr = null;
        try {
            ClassLoader classLoader = DocumentIndexer.class.getClassLoader();
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
            urlStr += "/"+IndexProperties.getNerdAnnotsTypeName()+"/_mapping";
        } else if (indexName.equals(IndexProperties.getKeytermAnnotsIndexName())) {
            urlStr += "/"+IndexProperties.getKeytermAnnotsTypeName()+"/_mapping";
        } else if(indexName.equals(IndexProperties.getKbIndexName())) {
            urlStr += "?pretty";
        } else {
            urlStr += "/"+IndexProperties.getFulltextTeisTypeName()+"/_mapping";
        }

        URL url = new URL(urlStr);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestProperty(
                "Content-Type", "application/x-www-form-urlencoded");
        httpCon.setRequestMethod("PUT");
        String mappingStr = null;
        try {
            ClassLoader classLoader = DocumentIndexer.class.getClassLoader();
            if (indexName.contains(IndexProperties.getNerdAnnotsIndexName())) {
                mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/annotation_nerd.json"));
            } else if (indexName.contains(IndexProperties.getKeytermAnnotsIndexName())) {
                mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/annotation_keyterm.json"));
            } else if(indexName.equals(IndexProperties.getKbIndexName())) {
            mappingStr = IOUtils.toString(classLoader.getResourceAsStream("elasticSearch/kb.json"));
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
                + httpCon.getResponseCode() + " "+ httpCon.getResponseMessage());
        if (httpCon.getResponseCode() == 200) {
            val = true;
        }
        return val;
    }

}
