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


    public static void init(String properties_filename, boolean isTest) {
        try {
            if (!isTest) {
                Properties prop = new Properties();
                File file = new File(System.getProperty("user.dir"));
                prop.load(new FileInputStream(file.getParent() + File.separator + "config" + File.separator + "local" + File.separator + properties_filename));
                setMongodbServer(prop.getProperty("commons.mongodb_host"));
                setMongodbPort(Integer.parseInt(prop.getProperty("commons.mongodb_port")));
                setMongodbDb(prop.getProperty("commons.mongodb_db"));
                setMongodbUser(prop.getProperty("commons.mongodb_user"));
                setMongodbPass(prop.getProperty("commons.mongodb_pass"));
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

    
}
