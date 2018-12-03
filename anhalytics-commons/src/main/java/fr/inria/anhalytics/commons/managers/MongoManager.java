package fr.inria.anhalytics.commons.managers;

import com.mongodb.*;

import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.properties.CommonsProperties;

import java.io.IOException;

import java.net.ConnectException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Abstract class to be sub-classed to use mongoDB service.
 *
 * @author Achraf
 */
abstract class MongoManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoManager.class);

    private MongoClient mongo = null;

    protected DB db = null;


    public MongoClientOptions.Builder builder = new MongoClientOptions.Builder();

    public MongoManager(boolean isTest) {
        try {
            CommonsProperties.init("anhalytics.properties", isTest);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        try {
            builder.socketKeepAlive(true);
            MongoClientOptions options = builder.build();


            if (isNotBlank(CommonsProperties.getMongodbUser())) {
                final MongoCredential credential = MongoCredential.createCredential(CommonsProperties.getMongodbUser(), CommonsProperties.getMongodbDb(), CommonsProperties.getMongodbPass().toCharArray());
                mongo = new MongoClient(
                        new ServerAddress(
                                CommonsProperties.getMongodbServer(),
                                CommonsProperties.getMongodbPort()),
                        credential,
                        options);
            } else {
                mongo = new MongoClient(
                        new ServerAddress(
                                CommonsProperties.getMongodbServer(),
                                CommonsProperties.getMongodbPort()),
                        options);
            }


            LOGGER.info("Mongodb is running on server : " + CommonsProperties.getMongodbServer() + " port : " + CommonsProperties.getMongodbPort());
            if (!mongo.getDatabaseNames().contains(CommonsProperties.getMongodbDb())) {
                LOGGER.info("MongoDB database " + CommonsProperties.getMongodbDb() + " does not exist and will be created");
            }
        } catch (MongoException ex) {
            throw new ServiceException("MongoDB is not UP, the process will be halted.");
        }
    }

    /**
     * Initializes database if it exists and create it otherwise.
     */
    protected void initDatabase() {
        db = mongo.getDB(CommonsProperties.getMongodbDb());
        LOGGER.info("Mongodb is connecting to : " + CommonsProperties.getMongodbDb() + ".");
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
