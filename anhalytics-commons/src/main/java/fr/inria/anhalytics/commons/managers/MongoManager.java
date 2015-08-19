package fr.inria.anhalytics.commons.managers;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.*;
import com.mongodb.util.JSON;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for retrieving TEI files to be indexed from MongoDB GridFS
 *
 */
public class MongoManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoManager.class);

    public static final String HARVEST_DIAGNOSTIC = "diagnostic";
    public static final String ADDITIONAL_TEIS = "additional_teis";
    public static final String BINARIES = "binaries";
    public static final String PUB_ANNEXES = "pub_annexes";
    public static final String FINAL_TEIS = "final_teis";
    public static final String GROBID_TEIS = "grobid_teis";
    public static final String GROBID_ASSETS = "grobid_assets";

    public static final String ANNOTATIONS = "annotations";

    private String mongodbServer = null;
    private int mongodbPort;

    private DB db = null;
    private GridFS gfs = null;

    private List<GridFSDBFile> files = null;
    private int indexFile = 0;

    private String currentAnnotationFilename = null;
    private String currentAnnotationHalID = null;

    // for annotations
    private DBCursor cursor = null;
    private DBCollection collection = null;

    private MongoClient mongo = null;

    public MongoManager(boolean test) {
        try {
            Properties prop = new Properties();
            File file = new File(System.getProperty("user.dir"));
            prop.load(new FileInputStream(file.getParent() + "/anhalytics-commons/" + "commons.properties"));
            mongodbServer = prop.getProperty("commons.mongodb_host");
            mongodbPort = Integer.parseInt(prop.getProperty("commons.mongodb_port"));
            String mongodbDb = prop.getProperty("commons.mongodb_db") + (test ? "_test" : "");
            String mongodbUser = prop.getProperty("commons.mongodb_user");
            String mongodbPass = prop.getProperty("commons.mongodb_pass");
            mongo = new MongoClient(mongodbServer, mongodbPort);
            db = mongo.getDB(mongodbDb);
            boolean auth = db.authenticate(mongodbUser, mongodbPass.toCharArray());

            //initAnnotations();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        mongo.close();
    }

    public boolean init(String collection, String date) {
        // open the GridFS
        try {
            gfs = new GridFS(db, collection);

            // init the loop
            BasicDBObject bdbo = new BasicDBObject();
            if (date != null) {
                bdbo.append("uploadDate", Utilities.parseStringDate(date));
            }
            files = gfs.find(bdbo);
            indexFile = 0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean initAnnotations() throws MongoException {
        // open the collection
        boolean collectionFound = false;
        Set<String> collections = db.getCollectionNames();
        for (String collection : collections) {
            if (collection.equals(ANNOTATIONS)) {
                collectionFound = true;
            }
        }
        if (!collectionFound) {
            LOGGER.debug("MongoDB collection annotations does not exist and will be created");
        }
        collection = db.getCollection(ANNOTATIONS);

        // index on filename and xml:id
        collection.ensureIndex(new BasicDBObject("filename", 1));
        collection.ensureIndex(new BasicDBObject("xml:id", 1));
        cursor = collection.find();
        indexFile = 0;
        return true;
    }

    public boolean hasMoreDocuments() {
        if (indexFile < files.size()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasMoreAnnotations() {
        if (indexFile < cursor.size()) {
            return true;
        } else {
            return false;
        }
    }

    public String nextAnnotation() {
        String json = null;

        DBObject obj = cursor.next();
        json = obj.toString();
        currentAnnotationFilename = (String) obj.get("filename");
        currentAnnotationHalID = (String) obj.get("halID");

        if (!cursor.hasNext()) {
            cursor.close();
        }
        indexFile++;
        return json;
    }

    public String getCurrentHalID() {
        String halID = null;
        if (indexFile < files.size()) {
            GridFSDBFile teifile = files.get(indexFile);
            String filename = teifile.getFilename();
            int ind = filename.indexOf(".");
            halID = filename.substring(0, ind);
            // we still have possibly the version information
            ind = halID.indexOf("v");
            halID = halID.substring(0, ind);
        }
        return halID;
    }

    public String getCurrentFilename() {
        String filename = null;
        if (indexFile < files.size()) {
            GridFSDBFile teifile = files.get(indexFile);
            filename = teifile.getFilename();
        }
        return filename;
    }

    public String nextDocument() {
        String tei = null;
        try {
            if (indexFile < files.size()) {
                GridFSDBFile teifile = files.get(indexFile);
                InputStream input = teifile.getInputStream();
                tei = IOUtils.toString(input, "UTF-8");
                indexFile++;
                input.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tei;
    }

    public InputStream nextBinaryDocument() {
        InputStream input = null;
        if (indexFile < files.size()) {
            GridFSDBFile teifile = files.get(indexFile);
            input = teifile.getInputStream();
            indexFile++;
        }
        return input;
    }

    public String getCurrentAnnotationFilename() {
        return currentAnnotationFilename;
    }

    public String getCurrentAnnotationHalID() {
        return currentAnnotationHalID;
    }

    public void removeDocument(String filename) {
        try {
            GridFS gfs = new GridFS(db, FINAL_TEIS);
            gfs.remove(filename);
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public boolean insertAnnotation(String json) {
        if (collection == null) {
            collection = db.getCollection("annotations");
            collection.ensureIndex(new BasicDBObject("filename", 1));
            collection.ensureIndex(new BasicDBObject("xml:id", 1));
        }
        DBObject dbObject = (DBObject) JSON.parse(json);
        WriteResult result = collection.insert(dbObject);
        CommandResult res = result.getCachedLastError();
        if ((res != null) && (res.ok())) {
            return true;
        } else {
            return false;
        }
    }

    public String getAnnotation(String filename, String id) {
        if (collection == null) {
            collection = db.getCollection("annotations");
            collection.ensureIndex(new BasicDBObject("filename", 1));
            collection.ensureIndex(new BasicDBObject("xml:id", 1));
        }
        String result = null;
        BasicDBObject query = new BasicDBObject("filename", filename);
        DBCursor curs = null;
        try {
            curs = collection.find(query);
            if (curs.hasNext()) {
                // we get now the sub-document corresponding to the given id
                DBObject annotations = curs.next();
                BasicDBList nerd = (BasicDBList) annotations.get("nerd");
                for (int i = 0; i < nerd.size(); i++) {
                    BasicDBObject annotation = (BasicDBObject) nerd.get(i);
                    String theId = annotation.getString("xml:id");
                    if ((theId != null) && (theId.equals(id))) {
                        result = annotation.toString();
                        break;
                    }
                }
            }
        } finally {
            if (curs != null) {
                curs.close();
            }
        }
        return result;
    }

    public Map<String, String> getEmbargoFiles(String date) {
        Map<String, String> files = new HashMap<String, String>();
        if (db.collectionExists(HARVEST_DIAGNOSTIC)) {
            collection = db.getCollection(HARVEST_DIAGNOSTIC);
            BasicDBObject query = new BasicDBObject("date", date);
            DBCursor curs = collection.find(query);
            try {
                if (curs.hasNext()) {
                    DBObject entry = curs.next();
                    String url = (String) entry.get("desc");
                    String id = (String) entry.get("halID");
                    files.put(id, url);
                }
            } finally {
                if (curs != null) {
                    curs.close();
                }
            }
        }
        return files;
    }

    public String getAnnotation(String filename) {
        if (collection == null) {
            collection = db.getCollection("annotations");
            collection.ensureIndex(new BasicDBObject("filename", 1));
            collection.ensureIndex(new BasicDBObject("xml:id", 1));
        }
        String result = null;
        BasicDBObject query = new BasicDBObject("filename", filename);
        DBCursor curs = null;
        try {
            curs = collection.find(query);
            if (curs.hasNext()) {
                // we get now the sub-document corresponding to the given id
                DBObject annotations = curs.next();
                BasicDBList nerd = (BasicDBList) annotations.get("nerd");
                result = nerd.toString();
            }
        } finally {
            if (curs != null) {
                curs.close();
            }
        }
        return result;
    }

    /**
     * Check if the current document has already been annotated.
     */
    public boolean isAnnotated() {
        if (collection == null) {
            collection = db.getCollection("annotations");
            collection.ensureIndex(new BasicDBObject("filename", 1));
            collection.ensureIndex(new BasicDBObject("xml:id", 1));
        }
        boolean result = false;
        String filename = getCurrentFilename();
        BasicDBObject query = new BasicDBObject("filename", filename);
        DBCursor cursor = null;
        try {
            cursor = collection.find(query);
            if (cursor.hasNext()) {
                result = true;
            } else {
                result = false;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * Check if the given pdf has already been harvested.
     */
    public boolean isCollected(String filename) {
        GridFS gfs = new GridFS(db, BINARIES);
        GridFSDBFile f = gfs.findOne(filename);
        boolean result = false;
        if (f != null) {
            result = true;
        }
        return result;
    }

    /**
     * Add a TEI/PDF document in the GridFS
     */
    public void addDocument(InputStream file, String fileName, String namespace, String dateString) {
        try {
            GridFS gfs = new GridFS(db, namespace);
            gfs.remove(fileName);
            GridFSInputFile gfsFile = gfs.createFile(file, true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(dateString));
            gfsFile.setFilename(fileName);
            gfsFile.put("halId", Utilities.getHalIDFromFilename(fileName));
            gfsFile.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    /**
     * Add a document assets with corresponding desc in the GridFS (otherwise we
     * could do it with ES ?)
     */
    public void addAssetDocument(InputStream file, String id, String fileName, String namespace, String dateString) throws ParseException {

        try {
            GridFS gfs = new GridFS(db, namespace);
            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("halId", id);
            whereQuery.put("fileName", fileName);
            gfs.remove(whereQuery);
            //version ?
            GridFSInputFile gfsFile = gfs.createFile(file, true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(dateString));
            gfsFile.setFilename(fileName);
            gfsFile.put("halId", id);
            gfsFile.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     */
    public void addAnnexDocument(InputStream file, String type, String id, String fileName, String namespace, String dateString) throws ParseException {

        try {
            GridFS gfs = new GridFS(db, namespace);
            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("halId", id);
            whereQuery.put("filename", fileName);
            gfs.remove(whereQuery);
            //version ?
            GridFSInputFile gfsFile = gfs.createFile(file, true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(dateString));
            gfsFile.setFilename(fileName);
            gfsFile.put("halId", id);
            gfsFile.setContentType(type);
            gfsFile.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /*
     Returns the asset files using the halID+filename indexes.
     */
    public InputStream getFile(String halId, String filename, String collection) {
        InputStream file = null;
        GridFS gfs = new GridFS(db, collection);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("halId", halId);
        whereQuery.put("filename", filename);
        GridFSDBFile cursor = gfs.findOne(whereQuery);
        file = cursor.getInputStream();
        return file;
    }

    public InputStream streamFile(String filename, String collection) {
        GridFSDBFile file = null;
        GridFS gfs = new GridFS(db, collection);
        file = gfs.findOne(filename);
        return file.getInputStream();
    }

    public InputStream streamFile(String filename) {
        GridFSDBFile file = null;
        GridFS gfs = new GridFS(db, BINARIES);
        file = gfs.findOne(filename);
        return file.getInputStream();
    }

    public void save(String haldID, String process, String desc, String date) {
        DBCollection collection = db.getCollection(HARVEST_DIAGNOSTIC);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("halId", haldID);
        whereQuery.put("process", process);
        collection.remove(whereQuery);
        BasicDBObject document = new BasicDBObject();
        document.put("haldID", haldID);
        document.put("process", process);
        document.put("desc", desc);
        if (date == null) {
            date = Utilities.formatDate(new Date());
        }
        document.put("date", date);
        collection.insert(document);
    }
}
