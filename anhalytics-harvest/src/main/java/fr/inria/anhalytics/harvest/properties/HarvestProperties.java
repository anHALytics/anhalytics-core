package fr.inria.anhalytics.harvest.properties;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author achraf
 */
public class HarvestProperties {

    private static String processName;

    private static String fromDate;

    private static String untilDate;

    private static String path2grobidHome;

    private static String path2grobidProperty;

    private static String oaiUrl;

    private static String grobidHost;
    private static String grobidPort;

    private static String tmpPath;

    private static boolean reset;
    
    private static int nbThreads = 1;

    public static void init(String path) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(path));
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file of harvest properties" + path, exp);
        }
        setGrobidHost(props.getProperty("harvest.grobid_host"));
        setGrobidPort(props.getProperty("harvest.grobid_port"));
        //check path
        setTmpPath(props.getProperty("harvest.tmpPath"));
            // As grobid process may take a long time we can continue on previous works
        setReset(Boolean.valueOf(props.getProperty("harvest.reset")));
        setTmpPath(props.getProperty("harvest.tmpPath"));
        setPath2grobidHome(props.getProperty("harvest.pGrobidHome"));
        setPath2grobidProperty(props.getProperty("harvest.pGrobidProperties"));
        String threads = props.getProperty("harvest.nbThreads");
        try {
            setNbThreads(Integer.parseInt(threads));
        } catch (java.lang.NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public static void checkPath(String path) {
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
     * @return the fromDate
     */
    public static String getFromDate() {
        return fromDate;
    }

    /**
     * @param fromDate the fromDate to set
     */
    public static void setFromDate(String fromdate) {
        fromDate = fromdate;
    }

    /**
     * @return the untilDate
     */
    public static String getUntilDate() {
        return untilDate;
    }

    /**
     * @param untilDate the untilDate to set
     */
    public static void setUntilDate(String untildate) {
        untilDate = untildate;
    }

    /**
     * @return the path2grobidHome
     */
    public static String getPath2grobidHome() {
        return path2grobidHome;
    }

    /**
     * @param path2grobidHome the path2grobidHome to set
     */
    public static void setPath2grobidHome(String path2grobidhome) {
        path2grobidHome = path2grobidhome;
    }

    /**
     * @return the path2grobidProperty
     */
    public static String getPath2grobidProperty() {
        return path2grobidProperty;
    }

    /**
     * @param path2grobidProperty the path2grobidProperty to set
     */
    public static void setPath2grobidProperty(String path2grobidproperty) {
        path2grobidProperty = path2grobidproperty;
    }

    /**
     * @return the oaiUrl
     */
    public static String getOaiUrl() {
        return oaiUrl;
    }

    /**
     * @param oaiUrl the oaiUrl to set
     */
    public static void setOaiUrl(String oaiurl) {
        oaiUrl = oaiurl;
    }

    /**
     * @return the grobid_host
     */
    public static String getGrobidHost() {
        return grobidHost;
    }

    /**
     * @param grobid_host the grobid_host to set
     */
    public static void setGrobidHost(String grobid_host) {
        grobidHost = grobid_host;
    }

    /**
     * @return the grobid_port
     */
    public static String getGrobidPort() {
        return grobidPort;
    }

    /**
     * @param grobid_port the grobid_port to set
     */
    public static void setGrobidPort(String grobid_port) {
        grobidPort = grobid_port;
    }

    /**
     * @return the tmpPath
     */
    public static String getTmpPath() {
        return tmpPath;
    }

    /**
     * @param tmpPath the tmpPath to set
     */
    public static void setTmpPath(String tmppath) {
        tmpPath = tmppath;
    }

    /**
     * @return the reset
     */
    public static boolean isReset() {
        return reset;
    }

    /**
     * @param reset the reset to set
     */
    public static void setReset(boolean reset) {
        reset = reset;
    }

    /**
     * @return the nbThreads
     */
    public static int getNbThreads() {
        return nbThreads;
    }

    /**
     * @param aNbThreads the nbThreads to set
     */
    public static void setNbThreads(int aNbThreads) {
        nbThreads = aNbThreads;
    }

}
