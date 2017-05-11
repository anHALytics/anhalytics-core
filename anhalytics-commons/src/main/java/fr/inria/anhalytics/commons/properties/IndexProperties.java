package fr.inria.anhalytics.commons.properties;

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
    private static String nerdAnnotsTypeName = "nerd";
    private static String keytermAnnotsIndexName = "annotations_keyterm";
    private static String keytermAnnotsTypeName = "keyterm";
    private static String quantitiesAnnotsIndexName = "annotations_quantities";
    private static String quantitiesAnnotsTypeName = "quantities";

    private static String teisIndexName  = "anhalytics_teis";
    private static String teisTypeName = "anhalytics_teis";
    
    private static String kbIndexName = "anhalytics_kb"; // to be rename, it's not metadata but KB
    private static String kbAuthorsTypeName = "authors"; // to be rename, it's not metadata but KB
    private static String kbPublicationsTypeName = "publications"; // to be rename, it's not metadata but KB
    private static String kbOrganisationsTypeName = "organisations"; // to be rename, it's not metadata but KB
    
    private static boolean reset;
    
    private static String fromDate;

    private static String untilDate;

    private static boolean processByDate = true;

    public static void init(String properties_filename) {
        Properties props = new Properties();
        try {
            File file = new File(System.getProperty("user.dir"));
            props.load(new FileInputStream(file.getParentFile()+File.separator+"config"+File.separator+properties_filename));
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file " + properties_filename, exp);
        }
        setElasticSearch_host(props.getProperty("index.elasticSearch_host"));
        setElasticSearch_port(props.getProperty("index.elasticSearch_port"));
        setElasticSearchClusterName(props.getProperty("index.elasticSearch_cluster"));
        setNerdAnnotsIndexName(props.getProperty("index.elasticSearch_nerdAnnotsIndexName"));
        setKeytermAnnotsIndexName(props.getProperty("index.elasticSearch_keytermAnnotsIndexName"));
        setQuantitiesAnnotsIndexName(props.getProperty("index.elasticSearch_quantitiesAnnotsIndexName"));
        setTeisIndexName(props.getProperty("index.elasticSearch_TeisIndexName"));
        setKbIndexName(props.getProperty("index.elasticSearch_kbIndexName"));
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
     * @return the grobid-quantities annotation index name
     */
    public static String getQuantitiesAnnotsIndexName() {
        return quantitiesAnnotsIndexName;
    }

    /**
     * @param aQuantitiesAnnotsIndexName the grobid-quantities annotation index name to set
     */
    public static void setQuantitiesAnnotsIndexName(String aQuantitiesAnnotsIndexName) {
        quantitiesAnnotsIndexName = aQuantitiesAnnotsIndexName;
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

    /**
     * @return the processByDate
     */
    public static boolean isProcessByDate() {
        return processByDate;
    }

    /**
     * @param aProcessByDate the processByDate to set
     */
    public static void setProcessByDate(boolean aProcessByDate) {
        processByDate = aProcessByDate;
    }

    /**
     * @return the teisIndexName
     */
    public static String getTeisIndexName() {
        return teisIndexName;
    }

    /**
     * @param aTeisIndexName the metadataTeisIndexName to set
     */
    public static void setTeisIndexName(String aTeisIndexName) {
        teisIndexName = aTeisIndexName;
    }

    /**
     * @return the kbIndexName
     */
    public static String getKbIndexName() {
        return kbIndexName;
    }

    /**
     * @param aKbIndexName the kbIndexName to set
     */
    public static void setKbIndexName(String aKbIndexName) {
        kbIndexName = aKbIndexName;
    }

    /**
     * @return the nerdAnnotsTypeName
     */
    public static String getNerdAnnotsTypeName() {
        return nerdAnnotsTypeName;
    }

    /**
     * @param aNerdAnnotsTypeName the nerdAnnotsTypeName to set
     */
    public static void setNerdAnnotsTypeName(String aNerdAnnotsTypeName) {
        nerdAnnotsTypeName = aNerdAnnotsTypeName;
    }

    /**
     * @return the keytermAnnotsTypeName
     */
    public static String getKeytermAnnotsTypeName() {
        return keytermAnnotsTypeName;
    }

    /**
     * @param aKeytermAnnotsTypeName the keytermAnnotsTypeName to set
     */
    public static void setKeytermAnnotsTypeName(String aKeytermAnnotsTypeName) {
        keytermAnnotsTypeName = aKeytermAnnotsTypeName;
    }

    /**
     * @return the quantitiesAnnotsTypeName
     */
    public static String getQuantitiesAnnotsTypeName() {
        return quantitiesAnnotsTypeName;
    }

    /**
     * @param aQuantitiesAnnotsTypeName the quantitiesAnnotsTypeName to set
     */
    public static void setQuantitiesAnnotsTypeName(String aQuantitiesAnnotsTypeName) {
        quantitiesAnnotsTypeName = aQuantitiesAnnotsTypeName;
    }

    /**
     * @return the teisTypeName
     */
    public static String getTeisTypeName() {
        return teisTypeName;
    }

    /**
     * @param aTeisTypeName the fulltextTeisTypeName to set
     */
    public static void setTeisTypeName(String aTeisTypeName) {
        teisTypeName = aTeisTypeName;
    }

    /**
     * @return the kbAuthorsTypeName
     */
    public static String getKbAuthorsTypeName() {
        return kbAuthorsTypeName;
    }

    /**
     * @param aKbAuthorsTypeName the kbAuthorsTypeName to set
     */
    public static void setKbAuthorsTypeName(String aKbAuthorsTypeName) {
        kbAuthorsTypeName = aKbAuthorsTypeName;
    }

    /**
     * @return the kbPublicationsTypeName
     */
    public static String getKbPublicationsTypeName() {
        return kbPublicationsTypeName;
    }

    /**
     * @param aKbPublicationsTypeName the kbPublicationsTypeName to set
     */
    public static void setKbPublicationsTypeName(String aKbPublicationsTypeName) {
        kbPublicationsTypeName = aKbPublicationsTypeName;
    }

    /**
     * @return the kbOrganisationsTypeName
     */
    public static String getKbOrganisationsTypeName() {
        return kbOrganisationsTypeName;
    }

    /**
     * @param aKbOrganisationsTypeName the kbOrganisationsTypeName to set
     */
    public static void setKbOrganisationsTypeName(String aKbOrganisationsTypeName) {
        kbOrganisationsTypeName = aKbOrganisationsTypeName;
    }

    /**
     * @return the reset
     */
    public static boolean isReset() {
        return reset;
    }

    /**
     * @param aReset the reset to set
     */
    public static void setReset(boolean aReset) {
        reset = aReset;
    }
    

}
