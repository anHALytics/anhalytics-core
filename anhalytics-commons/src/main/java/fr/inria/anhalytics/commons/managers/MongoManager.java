package fr.inria.anhalytics.commons.managers;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import fr.inria.anhalytics.commons.properties.CommonsProperties;
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

    private MongoClient mongo = null;

    protected DB db = null;

    public MongoManager(boolean isTest) {
        try {
            try {
                CommonsProperties.init("commons.properties", isTest);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
            mongo = new MongoClient(CommonsProperties.getMongodbServer(), CommonsProperties.getMongodbPort());
            if (!mongo.getDatabaseNames().contains(CommonsProperties.getMongodbDb())) {
                LOGGER.debug("MongoDB database " + CommonsProperties.getMongodbDb() + " does not exist and will be created");
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
    protected void initDatabase() {
        db = mongo.getDB(CommonsProperties.getMongodbDb());
        if (!mongo.getDatabaseNames().contains(CommonsProperties.getMongodbDb())) {
            BasicDBObject commandArguments = new BasicDBObject();
            commandArguments.put("user", CommonsProperties.getMongodbUser());
            commandArguments.put("pwd", CommonsProperties.getMongodbPass());
            String[] roles = {"readWrite"};
            commandArguments.put("roles", roles);
            BasicDBObject command = new BasicDBObject("createUser",
                    commandArguments);
            db.command(command);
        }
        boolean auth = db.authenticate(CommonsProperties.getMongodbUser(), CommonsProperties.getMongodbPass().toCharArray());
    }

    public DBCollection getCollection(String collectionName) {
        /* can t check with gridfs collection !
        boolean collectionFound = db.collectionExists(collectionName);
        
        if (!collectionFound) {
            LOGGER.debug("MongoDB collection " + collectionName + " does not exist and will be created");
        }
         */
        return db.getCollection(collectionName);
    }

    public void close() {
        mongo.close();
    }

    public void setDB(DB currentDB) {
        this.db = currentDB;
    }

}
