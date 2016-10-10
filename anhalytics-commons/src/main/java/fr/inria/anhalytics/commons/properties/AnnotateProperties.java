package fr.inria.anhalytics.commons.properties;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Represents the properties used for the annotation process.
 *
 * @author Achraf, Patrice
 */
public class AnnotateProperties {
    
    private static String processName;

    private static String fromDate;

    private static String untilDate;

    private static boolean processByDate = true;

    private static String nerd_host = null;

    private static String nerd_port = null;

    private static String keyterm_host = null;

    private static String keyterm_port = null;

    private static boolean isMultiThread;

    private static boolean reset;

    private static int nerd_nbThreads = 1;

    private static int keyterm_nbThreads = 1;

    /**
     * Loads and initializes properties from the file given the filename.
     */
    public static void init(String properties_filename) {
        Properties props = new Properties();
        try {
            File file = new File(System.getProperty("user.dir"));
            props.load(new FileInputStream(file.getParent()+File.separator+"config"+File.separator+properties_filename));
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file " + properties_filename, exp);
        }

        setNerdHost(props.getProperty("annotate.nerd_host"));
        setNerdPort(props.getProperty("annotate.nerd_port"));
        String threads = props.getProperty("annotate.nerd.nbThreads");
        try {
            setNerdNbThreads(Integer.parseInt(threads));
        } catch (java.lang.NumberFormatException e) {
            e.printStackTrace();
        }

        setKeytermHost(props.getProperty("annotate.keyterm_host"));
        setKeytermPort(props.getProperty("annotate.keyterm_port"));
        threads = props.getProperty("annotate.keyterm.nbThreads");
        try {
            setKeytermNbThreads(Integer.parseInt(threads));
        } catch (java.lang.NumberFormatException e) {
            e.printStackTrace();
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
     * @return the reset
     */
    public static boolean isReset() {
        return reset;
    }

    /**
     * @param isreset the reset to set
     */
    public static void setReset(boolean isreset) {
        reset = isreset;
    }

    /**
     * @return the nerd_host
     */
    public static String getNerdHost() {
        return nerd_host;
    }

    /**
     * @param aNerd_host the nerd_host to set
     */
    public static void setNerdHost(String aNerd_host) {
        nerd_host = aNerd_host;
    }

    /**
     * @return the nerd_port
     */
    public static String getNerdPort() {
        return nerd_port;
    }

    /**
     * @param aNerd_port the nerd_port to set
     */
    public static void setNerdPort(String aNerd_port) {
        nerd_port = aNerd_port;
    }

    /**
     * @return the host name of the keyterm extraction service
     */
    public static String getKeytermHost() {
        return keyterm_host;
    }

    /**
     * @param aNerd_host the host name for the keyterm extraction service
     */
    public static void setKeytermHost(String aKeyterm_host) {
        keyterm_host = aKeyterm_host;
    }

    /**
     * @return the port of the keyterm extraction service
     */
    public static String getKeytermPort() {
        return keyterm_port;
    }

    /**
     * @param aNerd_port the port for the keyterm extraction service
     */
    public static void setKeytermPort(String aKeyterm_port) {
        keyterm_port = aKeyterm_port;
    }

    /**
     * @return the isMultiThread
     */
    public static boolean isIsMultiThread() {
        return isMultiThread;
    }

    /**
     * @param aIsMultiThread the isMultiThread to set
     */
    public static void setIsMultiThread(boolean aIsMultiThread) {
        isMultiThread = aIsMultiThread;
    }

    /**
     * @return the nbThreads
     */
    public static int getNerdNbThreads() {
        return nerd_nbThreads;
    }

    /**
     * @param aNbThreads the nbThreads to set
     */
    public static void setNerdNbThreads(int aNbThreads) {
        nerd_nbThreads = aNbThreads;
    }

    /**
     * @return return the number of threads to be used for calling the keyterm extraction service
     */
    public static int getKeytermNbThreads() {
        return keyterm_nbThreads;
    }

    /**
     * @param aNbThreads the number of threads to be used for calling the keyterm extraction service
     */
    public static void setKeytermNbThreads(int aNbThreads) {
        keyterm_nbThreads = aNbThreads;
    }

    /**
     * @return the processName
     */
    public static String getProcessName() {
        return processName;
    }

    /**
     * @param aProcessName the processName to set
     */
    public static void setProcessName(String aProcessName) {
        processName = aProcessName;
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

}
