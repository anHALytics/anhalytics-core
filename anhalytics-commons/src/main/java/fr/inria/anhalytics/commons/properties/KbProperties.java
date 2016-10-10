package fr.inria.anhalytics.commons.properties;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author achraf
 */
public class KbProperties {

    private static String processName;

    private static String fromDate;

    private static String untilDate;

    private static boolean processByDate = true;

    private static boolean reset;


    public static void init(String properties_filename) {
        Properties props = new Properties();
        try {
            File file = new File(System.getProperty("user.dir"));
            props.load(new FileInputStream(file.getParent()+File.separator+"config"+File.separator+properties_filename));
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file " + properties_filename, exp);
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
