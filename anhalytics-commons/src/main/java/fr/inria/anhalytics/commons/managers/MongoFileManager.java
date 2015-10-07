package fr.inria.anhalytics.commons.managers;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.*;
import com.mongodb.util.JSON;
import fr.inria.anhalytics.commons.exceptions.FileNotFoundException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
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
public class MongoFileManager extends MongoManager implements MongoCollectionsInterface{

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoFileManager.class);

    
    /**
     * A static {@link MongoFileManager} object containing MongoFileManager instance
     * that can be used from different locations..
     */
    private static MongoFileManager mongoManager = null;
    
    private GridFS gfs = null;
    
    // for files
    private List<GridFSDBFile> files = null;
    private int indexFile = 0;

    private String currentFilename = null;
    private String currentHalID = null;

    // for annotations
    private DBCursor cursor = null;
    private DBCollection collection = null;


    public MongoFileManager(boolean isTest) {
        super(isTest);
        initDatabase(mongodbDb);
        
    }

    
       /**
     * Returns a static {@link MongoManager} object. If no one is set, then it
     * creates one. {@inheritDoc #MongoFilesManager()}
     *
     * @return
     */
    public static MongoFileManager getInstance(boolean isTest) throws UnknownHostException {
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
    protected static synchronized MongoFileManager getNewInstance(boolean isTest) throws UnknownHostException {
        mongoManager = new MongoFileManager(isTest);
        return mongoManager;
    }
    
    public boolean initTeiFiles(String date) {
        // open the GridFS
        try {
            // init the loop
            BasicDBObject bdbo = new BasicDBObject();
            if (date != null) {
                bdbo.append("uploadDate", Utilities.parseStringDate(date));
            }
            files = gfs.find(bdbo);
            if(files.size() > 0) LOGGER.debug(files.size()+ " found and will be processed.");
            indexFile = 0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean initBinaries(String date) {
        // open the GridFS
        try {
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
        
    public boolean initAnnexes(String date) {
        // open the GridFS
        try {            
            // filter on extensions... accept only pdf
            
            
            // init the loop
            BasicDBObject bdbo = new BasicDBObject();
            if (date != null) {
                bdbo.append("uploadDate", Utilities.parseStringDate(date));
                bdbo.append("filename", java.util.regex.Pattern.compile("^.pdf"));
            }
            files = gfs.find(bdbo);
            indexFile = 0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public boolean initAnnotations() throws MongoException {
        collection = getCollection(ANNOTATIONS);

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
        currentFilename = (String) obj.get("filename");
        currentHalID = (String) obj.get("halID");

        if (!cursor.hasNext()) {
            cursor.close();
        }
        indexFile++;
        return json;
    }

    public String getCurrentHalID() {
        return currentHalID;
    }

    public String getCurrentFilename() {
        return currentFilename;
    }

    public String nextDocument() {
        String tei = null;
        try {
            if (indexFile < files.size()) {
                GridFSDBFile teifile = files.get(indexFile);
                currentFilename = teifile.getFilename();
                currentHalID = (String)teifile.get("halId");
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
            GridFSDBFile binaryfile = files.get(indexFile);
            currentFilename = binaryfile.getFilename();
            currentHalID = (String)binaryfile.get("halId");
            input = binaryfile.getInputStream();
            indexFile++;
        }
        return input;
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
        DBCollection c = null;
        c = db.getCollection("annotations");
        c.ensureIndex(new BasicDBObject("filename", 1));
        c.ensureIndex(new BasicDBObject("xml:id", 1));
        
        DBObject dbObject = (DBObject) JSON.parse(json);
        WriteResult result = c.insert(dbObject);
        CommandResult res = result.getCachedLastError();
        if ((res != null) && (res.ok())) {
            return true;
        } else {
            return false;
        }
    }

    public String getAnnotation(String filename, String id) {
        DBCollection c = null;
        c = db.getCollection("annotations");
        c.ensureIndex(new BasicDBObject("filename", 1));
        c.ensureIndex(new BasicDBObject("xml:id", 1));
        
        String result = null;
        BasicDBObject query = new BasicDBObject("filename", filename);
        DBCursor curs = null;
        try {
            curs = c.find(query);
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
        DBCollection c = null;
        c = db.getCollection("annotations");
        c.ensureIndex(new BasicDBObject("filename", 1));
        c.ensureIndex(new BasicDBObject("xml:id", 1));
        
        String result = null;
        BasicDBObject query = new BasicDBObject("filename", filename);
        DBCursor curs = null;
        try {
            curs = c.find(query);
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
     * Check if the given pdf has already been harvested.
     */
    /*public boolean isCollectedDate(String filename) {
        GridFSDBFile f = (new GridFS(db, ADDITIONAL_TEIS)).findOne(filename);
        boolean result = false;
        if (f != null) {
            result = true;
        }
        return result;
    }*/
    /**
     * Check if the given pdf has already been grobidified.
     */
     public boolean isGrobidified() {
        String filename = getCurrentFilename();
        GridFSDBFile f = (new GridFS(db, GROBID_TEIS)).findOne(filename);
        boolean result = false;
        if (f != null) {
            result = true;
        }
        return result;
     }

    /**
     * Check if the current document has already been annotated.
     */
    public boolean isAnnotated() {
        DBCollection c = null;
        c = db.getCollection("annotations");
        c.ensureIndex(new BasicDBObject("filename", 1));
        c.ensureIndex(new BasicDBObject("xml:id", 1));
        
        boolean result = false;
        String filename = getCurrentFilename();
        BasicDBObject query = new BasicDBObject("filename", filename);
        
        DBCursor cursor = null;
        try {
            cursor = c.find(query);
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

    /**
     * Returns the asset files using the halID+filename indexes.
     */
    public InputStream getFile(String halId, String filename, String collection) throws FileNotFoundException {
        InputStream file = null;
        try{
            GridFS gfs = new GridFS(db, collection);
            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("halId", halId);
            whereQuery.put("filename", filename);
            GridFSDBFile cursor = gfs.findOne(whereQuery);
            file = cursor.getInputStream();
        }catch(Exception exp){
            throw new FileNotFoundException("Not found asset.");
        }
        return file;
    }

    public InputStream streamFile(String filename, String collection) {
        GridFSDBFile file = null;
        GridFS gfs = new GridFS(db, collection);
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
    
    public void setGridFS(String collectioName) {
        collection = getCollection(collectioName);
        gfs = new GridFS(db, collectioName);
    }
}
