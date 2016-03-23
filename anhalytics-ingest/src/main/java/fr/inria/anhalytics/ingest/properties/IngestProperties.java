package fr.inria.anhalytics.ingest.properties;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author achraf
 */
public class IngestProperties {

    private static String processName;

    private static String fromDate;

    private static String untilDate;

    private static String mysql_url;
    private static String mysql_db;
    private static String mysql_bibliodb;
    private static String mysql_user = "";
    private static String mysql_pass = "";

    private static boolean reset;


    public static void init(String properties_filename) {
        Properties props = new Properties();
        try {
            File file = new File(System.getProperty("user.dir"));
            props.load(new FileInputStream(file.getParent()+File.separator+"config"+File.separator+"local"+File.separator+properties_filename));
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file " + properties_filename, exp);
        }
        setMysql_url(props.getProperty("ingest.mysql_url"));
        setMysql_db(props.getProperty("ingest.mysql_db"));
        setMysql_bibliodb(props.getProperty("ingest.mysql_bibliodb"));
        setMysql_user(props.getProperty("ingest.mysql_user"));
        setMysql_pass(props.getProperty("ingest.mysql_pass"));
        // As grobid process may take a long time we can continue on previous works
        setReset(Boolean.valueOf(props.getProperty("ingest.reset")));
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
     * @param reset the reset to set
     */
    public static void setReset(boolean reset) {
        reset = reset;
    }

    /**
     * @return the mysql_url
     */
    public static String getMysql_url() {
        return mysql_url;
    }

    /**
     * @param aMysql_url the mysql_url to set
     */
    public static void setMysql_url(String aMysql_url) {
        mysql_url = aMysql_url;
    }

    /**
     * @return the mysql_db
     */
    public static String getMysql_db() {
        return mysql_db;
    }

    /**
     * @param aMysql_db the mysql_db to set
     */
    public static void setMysql_db(String aMysql_db) {
        mysql_db = aMysql_db;
    }

    /**
     * @return the mysql_bibliodb
     */
    public static String getMysql_bibliodb() {
        return mysql_bibliodb;
    }

    /**
     * @param aMysql_bibliodb the mysql_bibliodb to set
     */
    public static void setMysql_bibliodb(String aMysql_bibliodb) {
        mysql_bibliodb = aMysql_bibliodb;
    }

    /**
     * @return the mysql_user
     */
    public static String getMysql_user() {
        return mysql_user;
    }

    /**
     * @param aMysql_user the mysql_user to set
     */
    public static void setMysql_user(String aMysql_user) {
        mysql_user = aMysql_user;
    }

    /**
     * @return the mysql_pass
     */
    public static String getMysql_pass() {
        return mysql_pass;
    }

    /**
     * @param aMysql_pass the mysql_pass to set
     */
    public static void setMysql_pass(String aMysql_pass) {
        mysql_pass = aMysql_pass;
    }
}
