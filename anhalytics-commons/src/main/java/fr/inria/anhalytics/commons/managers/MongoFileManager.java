package fr.inria.anhalytics.commons.managers;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.*;
import com.mongodb.util.JSON;
import fr.inria.anhalytics.commons.data.PublicationFile;
import fr.inria.anhalytics.commons.exceptions.FileNotFoundException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for retrieving, inserting, updating and organizing metadata TEIs, PDFs
 * and extracted TEI files from MongoDB GridFS.
 *
 * @author Achraf
 */
public class MongoFileManager extends MongoManager implements MongoCollectionsInterface {

    private static final Logger logger = LoggerFactory.getLogger(MongoFileManager.class);

    /**
     * A static {@link MongoFileManager} object containing MongoFileManager
     * instance that can be used from different locations..
     */
    private static MongoFileManager mongoManager = null;

    private GridFS gfs = null;

    // index to iterate through files.
    private int indexFile = 0;

    private String currentRepositoryDocId = null;
    private String currentDocId = null;
    private String currentFileType = null;

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

    /**
     * This initializes cursor for binaries collection.
     */
    public boolean initBinaries(String date) {
        setGridFSCollection(MongoCollectionsInterface.BINARIES);
        try {
            BasicDBObject bdbo = new BasicDBObject();
            if (date != null) {
                bdbo.append("uploadDate", Utilities.parseStringDate(date));
            }
            cursor = gfs.getFileList(bdbo);
            indexFile = 0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (cursor.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This initializes cursor for annexes collection.
     */
    public boolean initAnnexes(String date) {
        try {
            BasicDBObject bdbo = new BasicDBObject();
            if (date != null) {
                bdbo.append("uploadDate", Utilities.parseStringDate(date));
                bdbo.append("filename", java.util.regex.Pattern.compile("^.pdf"));
            }
            cursor = gfs.getFileList(bdbo);
            indexFile = 0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (cursor.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This initializes cursor for metadata tei collection.
     */
    public boolean initMetadataTeis(String date) throws MongoException {
        try {
            setGridFSCollection(MongoCollectionsInterface.ADDITIONAL_TEIS);
            BasicDBObject bdbo = new BasicDBObject();
            if (date != null) {
                bdbo.append("uploadDate", Utilities.parseStringDate(date));
            }
            cursor = gfs.getFileList(bdbo);
            indexFile = 0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (cursor.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This initializes cursor for grobid tei collection.
     */
    public boolean initGrobidTeis(String date) throws MongoException {
        try {
            setGridFSCollection(MongoCollectionsInterface.GROBID_TEIS);
            BasicDBObject bdbo = new BasicDBObject();
            if (date != null) {
                bdbo.append("uploadDate", Utilities.parseStringDate(date));
            }
            cursor = gfs.getFileList(bdbo);
            indexFile = 0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (cursor.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This initializes cursor for tei generated collection.
     */
    public boolean initTeis(String date) throws MongoException {
        try {
            setGridFSCollection(MongoCollectionsInterface.FINAL_TEIS);
            BasicDBObject bdbo = new BasicDBObject();
            if (date != null) {
                bdbo.append("uploadDate", Utilities.parseStringDate(date));
            }
            cursor = gfs.getFileList(bdbo);
            indexFile = 0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (cursor.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This initializes cursor for annotations collection.
     */
    public boolean initAnnotations(String date) throws MongoException {
        collection = getCollection(ANNOTATIONS);
        // index on filename and xml:id
        BasicDBObject index = new BasicDBObject();
        index.put("repositoryDocId", 1);
        index.put("xml:id", 1);
        collection.ensureIndex(index, "index", true);
        BasicDBObject bdbo = new BasicDBObject();
        if (date != null) {
            bdbo.append("date", date);
        }

        cursor = collection.find(bdbo);
        indexFile = 0;
        if (cursor.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasMoreTeis() {
        if (indexFile < cursor.size()) {
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

    public boolean hasMoreBinaryDocuments() {
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
        currentRepositoryDocId = (String) obj.get("repositoryDocId");
        currentDocId = (String) obj.get("docId");
        if (!cursor.hasNext()) {
            cursor.close();
        }
        indexFile++;
        return json;
    }

    public String nextTeiDocument() {
        InputStream teiStream = null;
        String tei = null;
        DBObject obj = cursor.next();
        currentRepositoryDocId = (String) obj.get("repositoryDocId");
        if (obj.containsField("docId")) {
            currentDocId = (String) obj.get("docId");
        }
        GridFSDBFile binaryfile = gfs.findOne(currentRepositoryDocId + ".tei.xml");
        indexFile++;
        teiStream = binaryfile.getInputStream();
        try {
            tei = IOUtils.toString(teiStream);
            teiStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return tei;
    }

    public InputStream nextBinaryDocument() {
        InputStream input = null;
        DBObject obj = cursor.next();
        currentRepositoryDocId = (String) obj.get("repositoryDocId");
        GridFSDBFile binaryfile = gfs.findOne(currentRepositoryDocId + ".pdf");
        currentRepositoryDocId = (String) binaryfile.get("repositoryDocId");
        currentFileType = (String) binaryfile.getContentType();
        input = binaryfile.getInputStream();
        indexFile++;
        return input;
    }

    /**
     * Inserts json entry representing annotation result.
     */
    public boolean insertAnnotation(String json) {
        DBCollection c = null;
        c = db.getCollection("annotations");
        BasicDBObject index = new BasicDBObject();
        index.put("repositoryDocId", 1);
        index.put("xml:id", 1);
        c.ensureIndex(index, "index", true);
        DBObject dbObject = (DBObject) JSON.parse(json);
        WriteResult result = c.insert(dbObject);
        CommandResult res = result.getCachedLastError();
        if ((res != null) && (res.ok())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Inserts generated tei using GridFS.
     */
    public void insertTei(String teiString, String repositoryDocId, String docId, String date) {
        try {
            GridFS gfs = new GridFS(db, MongoCollectionsInterface.FINAL_TEIS);
            gfs.remove(repositoryDocId + ".tei.xml");
            GridFSInputFile gfsFile = gfs.createFile(new ByteArrayInputStream(teiString.getBytes()), true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(date));
            gfsFile.setFilename(repositoryDocId + ".tei.xml");
            gfsFile.put("repositoryDocId", repositoryDocId);
            gfsFile.put("docId", docId);
            gfsFile.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts grobid tei using GridFS.
     */
    public void insertGrobidTei(String teiString, String repositoryDocId, String date) {
        try {
            GridFS gfs = new GridFS(db, MongoCollectionsInterface.GROBID_TEIS);
            gfs.remove(repositoryDocId + ".tei.xml");
            GridFSInputFile gfsFile = gfs.createFile(new ByteArrayInputStream(teiString.getBytes()), true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(date));
            gfsFile.setFilename(repositoryDocId + ".tei.xml");
            gfsFile.put("repositoryDocId", repositoryDocId);
            gfsFile.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts TEI metadata document in the GridFS.
     */
    public void insertMetadataTei(String tei, String repositoryDocId, String date) {
        try {
            GridFS gfs = new GridFS(db, MongoCollectionsInterface.ADDITIONAL_TEIS);
            gfs.remove(repositoryDocId + ".tei.xml");
            GridFSInputFile gfsFile = gfs.createFile(new ByteArrayInputStream(tei.getBytes()), true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(date));
            gfsFile.setFilename(repositoryDocId + ".tei.xml");
            gfsFile.put("repositoryDocId", repositoryDocId);
            gfsFile.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts PDF binary document in the GridFS.
     */
    public void insertBinaryDocument(InputStream file, String repositoryDocId, String date) {
        try {
            GridFS gfs = new GridFS(db, MongoCollectionsInterface.BINARIES);
            gfs.remove(repositoryDocId + ".pdf");
            GridFSInputFile gfsFile = gfs.createFile(file, true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(date));
            gfsFile.setFilename(repositoryDocId + ".pdf");
            gfsFile.put("repositoryDocId", repositoryDocId);
            gfsFile.setContentType("application/pdf");
            gfsFile.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    /**
     * Updates already existing tei with new (more enriched one, fulltext..).
     */
    public void updateTei(String newTei, String repositoryDocId, String docID, boolean isFulltextAdded) {
        GridFS gfs = new GridFS(db, MongoCollectionsInterface.FINAL_TEIS);
        GridFSDBFile gdf = gfs.findOne(repositoryDocId + ".tei.xml");
        GridFSInputFile gfsNew = gfs.createFile(new ByteArrayInputStream(newTei.getBytes()), true);
        gfsNew.put("uploadDate", gdf.getUploadDate());
        gfsNew.setFilename(gdf.get("repositoryDocId") + ".tei.xml");
        gfsNew.put("repositoryDocId", gdf.get("repositoryDocId"));
        if (docID == null) {
            gfsNew.put("docId", gdf.get("docId"));
        }
        else
            gfsNew.put("docId", docID);
        gfsNew.put("isFulltextAdded", isFulltextAdded);
        gfsNew.save();
        gfs.remove(gdf);
    }

    /**
     * inserts a Arxiv/istex TEI document in the GridFS.
     */
    public void insertExternalTeiDocument(InputStream file, String identifier, String repository, String namespace, String dateString) {
        try {
            GridFS gfs = new GridFS(db, namespace);
            GridFSInputFile gfsFile = gfs.createFile(file, true);
            gfs.remove(identifier + ".pdf");
            gfsFile.put("uploadDate", Utilities.parseStringDate(dateString));
            gfsFile.setFilename(identifier + ".tei.xml");
            gfsFile.put("identifier", identifier);
            gfsFile.put("repository", repository);
            gfsFile.setContentType("application/tei+xml");
            gfsFile.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    /**
     * Inserts document assets with for repositoryDocId in the GridFS.
     */
    public void insertGrobidAssetDocument(InputStream file, String repositoryDocId, String fileName, String dateString) throws ParseException {
        try {
            GridFS gfs = new GridFS(db, MongoCollectionsInterface.GROBID_ASSETS);
            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("repositoryDocId", repositoryDocId);
            whereQuery.put("fileName", fileName);
            gfs.remove(whereQuery);
            //version ?
            GridFSInputFile gfsFile = gfs.createFile(file, true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(dateString));
            gfsFile.setFilename(fileName);
            gfsFile.put("repositoryDocId", repositoryDocId);
            gfsFile.setContentType("image/png");
            gfsFile.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts publication annex document.
     */
    public void insertAnnexDocument(InputStream file, String repositoryDocId, String fileName, String dateString) throws ParseException {
        try {
            GridFS gfs = new GridFS(db, MongoCollectionsInterface.PUB_ANNEXES);
            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("repositoryDocId", repositoryDocId);
            whereQuery.put("filename", fileName);
            gfs.remove(whereQuery);
            //version ?
            GridFSInputFile gfsFile = gfs.createFile(file, true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(dateString));
            gfsFile.setFilename(fileName);
            gfsFile.put("repositoryDocId", repositoryDocId);
            gfsFile.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void removeDocument(String filename) {
        try {
            GridFS gfs = new GridFS(db, FINAL_TEIS);
            gfs.remove(filename);
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the annotation given a repositoryDocId and id (xml:id).
     */
    public String getAnnotation(String repositoryDocId, String id) {
        DBCollection c = null;
        c = db.getCollection("annotations");
        c.ensureIndex(new BasicDBObject("repositoryDocId", 1));
        c.ensureIndex(new BasicDBObject("xml:id", 1));

        String result = null;
        BasicDBObject query = new BasicDBObject("repositoryDocId", repositoryDocId);
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

    public Map<String, PublicationFile> findEmbargoRecordsByDate(String date) {
        Map<String, PublicationFile> files = new HashMap<String, PublicationFile>();
        if (db.collectionExists(TO_REQUEST_LATER)) {
            collection = db.getCollection(TO_REQUEST_LATER);
            BasicDBObject query = new BasicDBObject("date", date);
            DBCursor curs = collection.find(query);
            try {
                if (curs.hasNext()) {
                    DBObject entry = curs.next();
                    String url = (String) entry.get("url");
                    String id = (String) entry.get("repositoryDocId");
                    boolean isAnnex = (Boolean) entry.get("isAnnex");
                    PublicationFile pf = new PublicationFile(url, date, isAnnex);
                    files.put(id, pf);
                }
            } finally {
                if (curs != null) {
                    curs.close();
                }
            }
        }
        return files;
    }

    /**
     * Returns the annotation for a given repositoryDocId.
     */
    public String getAnnotations(String docId) {
        DBCollection c = null;
        c = db.getCollection("annotations");
        c.ensureIndex(new BasicDBObject("repositoryDocId", 1));
        c.ensureIndex(new BasicDBObject("xml:id", 1));

        String result = null;
        BasicDBObject query = new BasicDBObject("docId", docId);
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
     * Check if the document has been already grobidified.
     */
    public boolean isGrobidified(String repositoryDocId) {
        GridFS gfs = new GridFS(db, MongoCollectionsInterface.GROBID_TEIS);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("repositoryDocId", repositoryDocId);
        List<GridFSDBFile> fs = null;
        boolean result = false;
        fs = gfs.find(whereQuery);
        if (fs.size() > 0) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Check if the document fulltext has been already added.
     */
    public boolean isWithFulltext(String repositoryDocId) {

        GridFS gfs = new GridFS(db, MongoCollectionsInterface.FINAL_TEIS);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("repositoryDocId", repositoryDocId);

        Boolean result = false;
        GridFSDBFile fs = gfs.findOne(whereQuery);
        Object o = fs.get("isFulltextAdded");

        if (o == null) {
            return false;
        } else {
            result = (Boolean) o;
        }
        return result;
    }

    /**
     * Checks if the tei corpus document was already data mined.
     */
    public boolean isFinalTeiCreated(String repositoryDocId) {
        GridFS gfs = new GridFS(db, MongoCollectionsInterface.FINAL_TEIS);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("repositoryDocId", repositoryDocId);
        List<GridFSDBFile> fs = null;
        boolean result = false;
        fs = gfs.find(whereQuery);
        if (fs.size() > 0) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Check if the current document has already been annotated.
     */
    public boolean isAnnotated() {
        DBCollection c = null;
        c = db.getCollection("annotations");
        c.ensureIndex(new BasicDBObject("repositoryDocId", 1));
        c.ensureIndex(new BasicDBObject("xml:id", 1));
        boolean result = false;
        BasicDBObject query = new BasicDBObject("repositoryDocId", currentRepositoryDocId);

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
     * Returns the asset files using the halID+filename indexes.
     */
    public InputStream findAssetFile(String repositoryDocId, String filename, String collection) throws FileNotFoundException {
        InputStream file = null;
        try {
            GridFS gfs = new GridFS(db, collection);
            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("repositoryDocId", repositoryDocId);
            whereQuery.put("filename", filename);
            GridFSDBFile cursor = gfs.findOne(whereQuery);
            file = cursor.getInputStream();
        } catch (Exception exp) {
            throw new FileNotFoundException("Not found asset.");
        }
        return file;
    }

    public String findTeiById(String repositoryDocId) {
        String tei = null;
        try {
            GridFS gfs = new GridFS(db, FINAL_TEIS);
            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("repositoryDocId", currentRepositoryDocId);
            whereQuery.put("filename", currentRepositoryDocId + ".tei.xml");
            GridFSDBFile file = gfs.findOne(whereQuery);
            InputStream teiStream = file.getInputStream();
            tei = IOUtils.toString(teiStream);
            teiStream.close();
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return tei;
    }

    /**
     * Saves the issue occurring while processing.
     */
    public void save(String repositoryDocId, String process, String desc, String date) {
        DBCollection collection = db.getCollection(HARVEST_DIAGNOSTIC);
        BasicDBObject index = new BasicDBObject();
        index.put("repositoryDocId", 1);
        index.put("process", 1);
        collection.ensureIndex(index, "index", true);
        BasicDBObject document = new BasicDBObject();
        document.put("repositoryDocId", repositoryDocId);
        document.put("process", process);

        collection.findAndRemove(document);
        document.put("desc", desc);
        if (date == null) {
            date = Utilities.formatDate(new Date());
        }
        document.put("date", date);
        collection.insert(document);
    }

    /**
     * Saves the resource to be requested later (embargo).
     */
    public void saveForLater(String repositoryDocId, String url, boolean isAnnex, String desc, String date) {
        DBCollection collection = db.getCollection(MongoCollectionsInterface.TO_REQUEST_LATER);
        BasicDBObject index = new BasicDBObject();
        index.put("repositoryDocId", 1);
        index.put("url", 1);
        collection.ensureIndex(index, "index", true);
        BasicDBObject document = new BasicDBObject();
        document.put("repositoryDocId", repositoryDocId);
        document.put("url", url);
        collection.findAndRemove(document);
        document.put("isAnnex", true);
        document.put("desc", desc);
        if (date == null) {
            date = Utilities.formatDate(new Date());
        }
        document.put("date", date);
        collection.insert(document);
    }

    public void removeEmbargoRecord(String repositoryDocId, String url) {
        DBCollection collection = db.getCollection(TO_REQUEST_LATER);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("repositoryDocId", repositoryDocId);
        whereQuery.put("url", url);
        collection.remove(whereQuery);
    }

    public void setGridFSCollection(String collectionName) {
        if (gfs == null || !gfs.getBucketName().equals(collectionName)) {
            gfs = new GridFS(db, collectionName);
        }
    }

    public String getCurrentRepositoryDocId() {
        return currentRepositoryDocId;
    }

    /**
     * @return the currentFileType
     */
    public String getCurrentFileType() {
        return currentFileType;
    }

    /**
     * @return the currentDocId
     */
    public String getCurrentDocId() {
        return currentDocId;
    }
}
