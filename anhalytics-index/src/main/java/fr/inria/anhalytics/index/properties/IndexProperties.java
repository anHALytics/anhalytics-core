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
    private static String annotsIndexName;

    private static String teisIndexName;
    private static String metadataIndexName;
    
     private static String fromDate;

    private static String untilDate;

    public static void init(String properties_filename) {
        Properties props = new Properties();
        try {
            File file = new File(System.getProperty("user.dir"));
            props.load(new FileInputStream(file.getParent()+File.separator+"config"+File.separator+"local"+File.separator+properties_filename));
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file " + properties_filename, exp);
        }
        setElasticSearch_host(props.getProperty("index.elasticSearch_host"));
        setElasticSearch_port(props.getProperty("index.elasticSearch_port"));
        setElasticSearchClusterName(props.getProperty("index.elasticSearch_cluster"));
        setAnnotsIndexName(props.getProperty("index.elasticSearch_annotsIndexName"));
        setTeisIndexName(props.getProperty("index.elasticSearch_teisIndexName"));
        setAnnotsIndexName(props.getProperty("index.elasticSearch_annotsIndexName"));
        setMetadataIndexName(props.getProperty("index.elasticSearch_mtdsIndexName"));
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
    public static String getTeisIndexName() {
        return teisIndexName;
    }

    /**
     * @param aWorkingIndexName the workingIndexName to set
     */
    public static void setTeisIndexName(String ateisIndexName) {
        teisIndexName = ateisIndexName;
    }

    /**
     * @return the metadataIndexName
     */
    public static String getMetadataIndexName() {
        return metadataIndexName;
    }

    /**
     * @param aMetadataIndexName the metadataIndexName to set
     */
    public static void setMetadataIndexName(String aMetadataIndexName) {
        metadataIndexName = aMetadataIndexName;
    }

    /**
     * @return the fromDate
     */
    public static String getFromDate() {
        return fromDate;
    }

    /**
     * @param aFromDate the fromDate to set
     */
    public static void setFromDate(String aFromDate) {
        fromDate = aFromDate;
    }

    /**
     * @return the untilDate
     */
    public static String getUntilDate() {
        return untilDate;
    }

    /**
     * @param aUntilDate the untilDate to set
     */
    public static void setUntilDate(String aUntilDate) {
        untilDate = aUntilDate;
    }
    

}
