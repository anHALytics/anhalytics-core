package fr.inria.anhalytics.annotate.properties;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import java.io.File;
import java.util.Properties;

/**
 * Represents the properties used for the annotation process.
 *
 * @author Achraf
 */
public class AnnotateProperties {

    private static String fromDate;

    private static String untilDate;

    private static String nerd_host = null;

    private static String nerd_port = null;

    private static boolean isMultiThread;

    private static boolean reset;

    private static int nbThreads = 1;

    /**
     * Loads and initializes properties from the file given the filename.
     */
    public static void init(String properties_filename) {
        Properties props = new Properties();
        try {
            ClassLoader classLoader = AnnotateProperties.class.getClassLoader();
            props.load(classLoader.getResourceAsStream(properties_filename));
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file " + properties_filename, exp);
        }
        setNerd_host(props.getProperty("annotate.nerd_host"));
        setNerd_port(props.getProperty("annotate.nerd_port"));
        String threads = props.getProperty("annotate.nbThreads");
        try {
            setNbThreads(Integer.parseInt(threads));
        } catch (java.lang.NumberFormatException e) {
            e.printStackTrace();
        }
        setReset(Boolean.valueOf(props.getProperty("annotate.reset")));
        //exceptions if a necessary propertry is not set..
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
     * @param reset the reset to set
     */
    public static void setReset(boolean reSet) {
        reset = reSet;
    }

    /**
     * @return the nerd_host
     */
    public static String getNerd_host() {
        return nerd_host;
    }

    /**
     * @param aNerd_host the nerd_host to set
     */
    public static void setNerd_host(String aNerd_host) {
        nerd_host = aNerd_host;
    }

    /**
     * @return the nerd_port
     */
    public static String getNerd_port() {
        return nerd_port;
    }

    /**
     * @param aNerd_port the nerd_port to set
     */
    public static void setNerd_port(String aNerd_port) {
        nerd_port = aNerd_port;
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
