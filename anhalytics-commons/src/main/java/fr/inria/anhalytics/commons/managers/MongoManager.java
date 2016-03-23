package fr.inria.anhalytics.commons.managers;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class to be sub-classed to use mongoDB service.
 *
 * @author Achraf
 */
abstract class MongoManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoManager.class);

    private String mongodbServer = null;
    private int mongodbPort;

    protected String mongodbDb = null;
    private String mongodbUser = null;
    private String mongodbPass = null;

    private MongoClient mongo = null;

    protected DB db = null;

    public MongoManager(boolean isTest) {
        try {
            if (!isTest) {
                Properties prop = new Properties();
                File file = new File(System.getProperty("user.dir"));
                prop.load(new FileInputStream(file.getParent() + File.separator + "config" + File.separator + "local" + File.separator + "commons.properties"));
                mongodbServer = prop.getProperty("commons.mongodb_host");
                mongodbPort = Integer.parseInt(prop.getProperty("commons.mongodb_port"));
                mongodbDb = prop.getProperty("commons.mongodb_db");
                mongodbUser = prop.getProperty("commons.mongodb_user");
                mongodbPass = prop.getProperty("commons.mongodb_pass");
                mongo = new MongoClient(mongodbServer, mongodbPort);
                if (!mongo.getDatabaseNames().contains(mongodbDb)) {
                    LOGGER.debug("MongoDB database " + mongodbDb + " does not exist and will be created");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes database if it exists and create it otherwise.
     *
     * @param dbName
     */
    protected void initDatabase(String dbName) {
        db = mongo.getDB(dbName);
        if (!mongo.getDatabaseNames().contains(dbName)) {
            BasicDBObject commandArguments = new BasicDBObject();
            commandArguments.put("user", mongodbUser);
            commandArguments.put("pwd", mongodbPass);
            String[] roles = {"readWrite"};
            commandArguments.put("roles", roles);
            BasicDBObject command = new BasicDBObject("createUser",
                    commandArguments);
            db.command(command);
        }
        boolean auth = db.authenticate(mongodbUser, mongodbPass.toCharArray());
    }

    public DBCollection getCollection(String collectionName) {
        boolean collectionFound = false;
        Set<String> collections = db.getCollectionNames();
        for (String collection : collections) {
            if (collection.equals(collectionName)) {
                collectionFound = true;
            }
        }
        if (!collectionFound) {
            LOGGER.debug("MongoDB collection " + collectionName + " does not exist and will be created");
        }
        return db.getCollection(collectionName);
    }

    public void close() {
        mongo.close();
    }

    public void setDB(DB currentDB) {
        this.db = currentDB;
    }

}
