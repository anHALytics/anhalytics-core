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

    private static String nerdAnnotsIndexName = "annotations_nerd";
	private static String keytermAnnotsIndexName = "annotations_keyterm";
    private static String teisIndexName  = "anhalytics_teis";
    private static String metadataIndexName = "anhalytics_metadata"; // to be rename, it's not metadata but KB

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
        /*setTeisIndexName(props.getProperty("index.elasticSearch_teisIndexName"));
        setNerdAnnotsIndexName(props.getProperty("index.elasticSearch_nerdAnnotsIndexName"));
		setKeytermAnnotsIndexName(props.getProperty("index.elasticSearch_keytermAnnotsIndexName"));
        setMetadataIndexName(props.getProperty("index.elasticSearch_mtdsIndexName"));*/
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
     * @return the ElasticSearch cluster name
     */
    public static String getElasticSearchClusterName() {
        return elasticSearchClusterName;
    }

    /**
     * @param aElasticSearchClusterName the ElasticSearch cluster name to set
     */
    public static void setElasticSearchClusterName(String aElasticSearchClusterName) {
        elasticSearchClusterName = aElasticSearchClusterName;
    }

    /**
     * @return the NERD annotation index name
     */
    public static String getNerdAnnotsIndexName() {
        return nerdAnnotsIndexName;
    }

    /**
     * @param aNerdAnnotsIndexName the NERD annotation index name to set
     */
    public static void setNerdAnnotsIndexName(String aNerdAnnotsIndexName) {
        nerdAnnotsIndexName = aNerdAnnotsIndexName;
    }
	
    /**
     * @return the keyterm annotation index name
     */
    public static String getKeytermAnnotsIndexName() {
        return keytermAnnotsIndexName;
    }

    /**
     * @param aKeytermAnnotsIndexName the keyterm annotation index name to set
     */
    public static void setKeytermAnnotsIndexName(String aKeytermAnnotsIndexName) {
        keytermAnnotsIndexName = aKeytermAnnotsIndexName;
    }

    /**
     * @return the index name for TEI
     */
    public static String getTeisIndexName() {
        return teisIndexName;
    }

    /**
     * @param aTeisIndexName the index name for TEI to set
     */
    public static void setTeisIndexName(String aTeisIndexName) {
        teisIndexName = aTeisIndexName;
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
    

}
