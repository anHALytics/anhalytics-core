package fr.inria.anhalytics.index.properties;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author achraf
 */
public class IndexProperties {

    private static String processName;

    private static String elasticSearch_host;
    private static String elasticSearch_port;
    
    private static String elasticSearchClusterName;
    private static String teiIndexName;
    private static String annotsIndexName;
    
    private static String workingIndexName;

    public static void init(String path) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(path));
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file of index properties" + path, exp);
        }
        setElasticSearch_host(props.getProperty("index.elasticSearch_host"));
        setElasticSearch_port(props.getProperty("index.elasticSearch_port"));
        setElasticSearchClusterName(props.getProperty("index.elasticSearch_cluster"));            
        setTeiIndexName(props.getProperty("index.elasticSearch_indexName"));
        setAnnotsIndexName(props.getProperty("index.elasticSearch_annotsIndexName"));
        if (processName.equals("tei")) {
            setWorkingIndexName(props.getProperty("index.elasticSearch_indexName"));
        } else if (processName.equals("annotation")) {
            setWorkingIndexName(props.getProperty("index.elasticSearch_annotsIndexName"));
        }
    }

    private static void checkPath(String path) {
        File propertyFile = new File(path);

        // exception if prop file does not exist
        if (!propertyFile.exists()) {
            throw new PropertyException("Could not read harvest.properties, the file '" + path + "' does not exist.");
        }
    }

    /**
     * @return the processName
     */
    public static String getProcessName() {
        return processName;
    }

    /**
     * @param processName the processName to set
     */
    public static void setProcessName(String processname) {
        processName = processname;
    }

    /**
     * @return the elasticSearch_host
     */
    public static String getElasticSearch_host() {
        return elasticSearch_host;
    }

    /**
     * @param aElasticSearch_host the elasticSearch_host to set
     */
    public static void setElasticSearch_host(String aElasticSearch_host) {
        elasticSearch_host = aElasticSearch_host;
    }

    /**
     * @return the elasticSearch_port
     */
    public static String getElasticSearch_port() {
        return elasticSearch_port;
    }

    /**
     * @param aElasticSearch_port the elasticSearch_port to set
     */
    public static void setElasticSearch_port(String aElasticSearch_port) {
        elasticSearch_port = aElasticSearch_port;
    }

    /**
     * @return the elasticSearchClusterName
     */
    public static String getElasticSearchClusterName() {
        return elasticSearchClusterName;
    }

    /**
     * @param aElasticSearchClusterName the elasticSearchClusterName to set
     */
    public static void setElasticSearchClusterName(String aElasticSearchClusterName) {
        elasticSearchClusterName = aElasticSearchClusterName;
    }

    /**
     * @return the teiIndexName
     */
    public static String getTeiIndexName() {
        return teiIndexName;
    }

    /**
     * @param aTeiIndexName the teiIndexName to set
     */
    public static void setTeiIndexName(String aTeiIndexName) {
        teiIndexName = aTeiIndexName;
    }

    /**
     * @return the annotsIndexName
     */
    public static String getAnnotsIndexName() {
        return annotsIndexName;
    }

    /**
     * @param aAnnotsIndexName the annotsIndexName to set
     */
    public static void setAnnotsIndexName(String aAnnotsIndexName) {
        annotsIndexName = aAnnotsIndexName;
    }

    /**
     * @return the workingIndexName
     */
    public static String getWorkingIndexName() {
        return workingIndexName;
    }

    /**
     * @param aWorkingIndexName the workingIndexName to set
     */
    public static void setWorkingIndexName(String aWorkingIndexName) {
        workingIndexName = aWorkingIndexName;
    }

}
