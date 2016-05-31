package fr.inria.anhalytics.commons.managers;

import java.net.UnknownHostException;

/**
 *
 * @author achraf
 */
public class MongoDataManager extends MongoManager {
    
    /**
     * A static {@link MongoManager} object containing MongoManager instance
     * that can be used from different locations..
     */
    private static MongoDataManager mongoManager = null;

    public MongoDataManager(boolean isTest) throws UnknownHostException {
        super(isTest);
        initDatabase();
    }
    
    /**
     * Returns a static {@link MongoManager} object. If no one is set, then it
     * creates one. {@inheritDoc #MongoFilesManager()}
     *
     * @return
     */
    public static MongoDataManager getInstance(boolean isTest) throws UnknownHostException {
        if (mongoManager == null) {
            return getNewInstance(isTest);
        } else {
            return mongoManager;
        }
    }

    /**
     * Creates a new {@link MongoFilesManager} object, initializes it and
     * returns it. {@inheritDoc #MongoFilesManager()}
     *
     * @return MongoFilesManager
     */
    protected static synchronized MongoDataManager getNewInstance(boolean isTest) throws UnknownHostException {
        mongoManager = new MongoDataManager(isTest);
        return mongoManager;
    }
    
}
