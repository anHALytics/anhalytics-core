package fr.inria.anhalytics.commons.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author achraf
 */
public class CommonsProperties {

    private static String mongodbServer;
    private static int mongodbPort;
    private static String mongodbDb;
    private static String mongodbUser;
    private static String mongodbPass;

    private static String mysql_url;
    private static String mysql_db;
    private static String mysql_bibliodb;
    private static String mysql_user = "";
    private static String mysql_pass = "";

    public static void init(String properties_filename, boolean isTest) {
        try {
            if (!isTest) {
                Properties prop = new Properties();
                File file = new File(System.getProperty("user.dir"));
                prop.load(new FileInputStream(file.getParent() + File.separator + "config" + File.separator + properties_filename));
                setMongodbServer(prop.getProperty("commons.mongodb_host"));
                setMongodbPort(Integer.parseInt(prop.getProperty("commons.mongodb_port")));
                setMongodbDb(prop.getProperty("commons.mongodb_db"));
                setMongodbUser(prop.getProperty("commons.mongodb_user"));
                setMongodbPass(prop.getProperty("commons.mongodb_pass"));

                setMysql_url(prop.getProperty("kb.mysql_url"));
                setMysql_db(prop.getProperty("kb.mysql_db"));
                setMysql_bibliodb(prop.getProperty("kb.mysql_bibliodb"));
                setMysql_user(prop.getProperty("kb.mysql_user"));
                setMysql_pass(prop.getProperty("kb.mysql_pass"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the mongodbServer
     */
    public static String getMongodbServer() {
        return mongodbServer;
    }

    /**
     * @param aMongodbServer the mongodbServer to set
     */
    public static void setMongodbServer(String aMongodbServer) {
        mongodbServer = aMongodbServer;
    }

    /**
     * @return the mongodbPort
     */
    public static int getMongodbPort() {
        return mongodbPort;
    }

    /**
     * @param aMongodbPort the mongodbPort to set
     */
    public static void setMongodbPort(int aMongodbPort) {
        mongodbPort = aMongodbPort;
    }

    /**
     * @return the mongodbDb
     */
    public static String getMongodbDb() {
        return mongodbDb;
    }

    /**
     * @param aMongodbDb the mongodbDb to set
     */
    public static void setMongodbDb(String aMongodbDb) {
        mongodbDb = aMongodbDb;
    }

    /**
     * @return the mongodbUser
     */
    public static String getMongodbUser() {
        return mongodbUser;
    }

    /**
     * @param aMongodbUser the mongodbUser to set
     */
    public static void setMongodbUser(String aMongodbUser) {
        mongodbUser = aMongodbUser;
    }

    /**
     * @return the mongodbPass
     */
    public static String getMongodbPass() {
        return mongodbPass;
    }

    /**
     * @param aMongodbPass the mongodbPass to set
     */
    public static void setMongodbPass(String aMongodbPass) {
        mongodbPass = aMongodbPass;
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
