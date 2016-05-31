package fr.inria.anhalytics.commons.managers;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.*;
import com.mongodb.util.JSON;
import fr.inria.anhalytics.commons.data.PublicationFile;
import fr.inria.anhalytics.commons.data.TEI;
import fr.inria.anhalytics.commons.exceptions.FileNotFoundException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for retrieving, inserting, updating and organizing metadata TEIs, PDFs
 * and extracted TEI files from MongoDB GridFS.
 *
 * WARNING: to keep in mind - this is not thread safe !
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

    private String currentDocSource = null;
    private String currentRepositoryDocId = null;
    private String currentAnhalyticsId = null;
    private String currentDocType = null;
    private String currentFileType = null;
    private String currentFileName = null;
    private boolean currentIsWithFulltext = false;

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
            cursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
            indexFile = 0;
        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getCause());
        }
        if (cursor.size() > 0) {
            logger.info(cursor.size() + " documents found for : " + date);
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
            logger.error(e.getMessage(), e.getCause());
        }
        if (cursor.size() > 0) {
            logger.info(cursor.size() + " documents found for : " + date);
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
            setGridFSCollection(MongoCollectionsInterface.METADATAS_TEIS);
            
            
            BasicDBObject bdbo = new BasicDBObject();
            if (date != null) {
                bdbo.append("uploadDate", Utilities.parseStringDate(date));
            }
            cursor = gfs.getFileList(bdbo);
            cursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
            indexFile = 0;
        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getCause());
        }
        if (cursor.size() > 0) {
            logger.info(cursor.size() + " documents found for : " + date);
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
            cursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
            indexFile = 0;
        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getCause());
        }
        if (cursor.size() > 0) {
            logger.info(cursor.size() + " documents found for : " + date);
            return true;
        } else {
            return false;
        }
    }

    /**
     * This initializes cursor for tei generated collection.
     */
    public boolean initTeis(String date, boolean withFulltext) throws MongoException {
        try {
            setGridFSCollection(MongoCollectionsInterface.FINAL_TEIS);
            BasicDBObject bdbo = new BasicDBObject();
            if (date != null) {
                bdbo.append("uploadDate", Utilities.parseStringDate(date));
            }
            bdbo.append("isWithFulltext", withFulltext);
            cursor = gfs.getFileList(bdbo);
            cursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
            indexFile = 0;
        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getCause());
        }
        if (cursor.size() > 0) {
            logger.info(cursor.size() + " documents found for : " + date);
            return true;
        } else {
            return false;
        }
    }

    /**
     * This initializes cursor for grobid assets files.
     */
    public boolean initAssets(String date) throws MongoException {
        try {
            setGridFSCollection(MongoCollectionsInterface.GROBID_ASSETS);
            BasicDBObject bdbo = new BasicDBObject();
            if (date != null) {
                bdbo.append("uploadDate", Utilities.parseStringDate(date));
            }
            cursor = gfs.getFileList(bdbo);
            indexFile = 0;
        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getCause());
        }
        if (cursor.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This initializes cursor for annotations collection.
     *
     * @param annotationCollection the annotation collection to initialize, e.g.
     * MongoCollectionsInterface.NERD_ANNOTATIONS,
     * MongoCollectionsInterface.KEYTERM_ANNOTATIONS, etc.
     */
    public boolean initAnnotations(String date, String annotationsCollection) throws MongoException {
        collection = getCollection(annotationsCollection);
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

    public boolean hasMoreAssets() {
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
        currentAnhalyticsId = (String) obj.get("anhalyticsId");
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
        currentDocType = (String) obj.get("documentType");
        currentAnhalyticsId = (String) obj.get("anhalyticsId");
        currentDocSource = (String) obj.get("source");
        currentIsWithFulltext = obj.get("isWithFulltext") != null ? (Boolean) obj.get("isWithFulltext") : false;
        GridFSDBFile binaryfile = gfs.findOne(currentRepositoryDocId + ".tei.xml");
        indexFile++;
        teiStream = binaryfile.getInputStream();
        try {
            tei = IOUtils.toString(teiStream);
            teiStream.close();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex.getCause());
        }
        return tei;
    }

    public InputStream nextBinaryDocument() {
        InputStream input = null;
        DBObject obj = cursor.next();
        currentRepositoryDocId = (String) obj.get("repositoryDocId");
        currentAnhalyticsId = (String) obj.get("anhalyticsId");
        currentDocSource = (String) obj.get("source");
        GridFSDBFile binaryfile = gfs.findOne(currentRepositoryDocId + ".pdf");
        currentDocType = (String) binaryfile.get("documentType");
        currentFileType = (String) binaryfile.getContentType();
        input = binaryfile.getInputStream();
        indexFile++;
        return input;
    }

    public String nextAsset() {
        InputStream input = null;
        DBObject obj = cursor.next();
        currentRepositoryDocId = (String) obj.get("repositoryDocId");
        currentFileType = (String) obj.get("contentType");
        currentFileName = (String) obj.get("filename");
        currentDocSource = (String) obj.get("source");
        currentAnhalyticsId = (String) obj.get("anhalyticsId");
        indexFile++;
        return currentFileName;
    }

    /**
     * Inserts json entry representing annotation result.
     */
    public boolean insertAnnotation(String json, String annotationsCollection) {
        boolean done = false;
        try {
            DBCollection c = null;
            c = db.getCollection(annotationsCollection);
            BasicDBObject index = new BasicDBObject();
            index.put("repositoryDocId", 1);
            index.put("xml:id", 1);
            c.ensureIndex(index, "index", true);
            DBObject dbObject = (DBObject) JSON.parse(json);
            WriteResult result = c.insert(dbObject);
            CommandResult res = result.getCachedLastError();
            if ((res != null) && (res.ok())) {
                done = true;
            } else {
                done = false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
        }
        return done;
    }

    /**
     * Inserts generated tei using GridFS.
     */
    public void insertTei(String teiString, String repositoryDocId, String anhalyticsId, boolean isWithFulltext, String date) {
        try {
            GridFS gfs = new GridFS(db, MongoCollectionsInterface.FINAL_TEIS);
            gfs.remove(repositoryDocId + ".tei.xml");
            GridFSInputFile gfsFile = gfs.createFile(new ByteArrayInputStream(teiString.getBytes()), true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(date));
            gfsFile.setFilename(repositoryDocId + ".tei.xml");
            gfsFile.put("repositoryDocId", repositoryDocId);
            gfsFile.put("anhalyticsId", anhalyticsId);
            gfsFile.put("source", currentDocSource);
            gfsFile.put("isWithFulltext", isWithFulltext);
            gfsFile.save();
        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    /**
     * Inserts grobid tei using GridFS.
     */
    public void insertGrobidTei(String teiString, String repositoryDocId, String anhalyticsId, String date) {
        try {
            GridFS gfs = new GridFS(db, MongoCollectionsInterface.GROBID_TEIS);
            gfs.remove(repositoryDocId + ".tei.xml");
            GridFSInputFile gfsFile = gfs.createFile(new ByteArrayInputStream(teiString.getBytes()), true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(date));
            gfsFile.setFilename(repositoryDocId + ".tei.xml");
            gfsFile.put("repositoryDocId", repositoryDocId);
            gfsFile.put("anhalyticsId", anhalyticsId);
            gfsFile.put("source", currentDocSource);
            gfsFile.save();
        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    /**
     * Inserts TEI metadata document in the GridFS.
     */
    public void insertMetadataTei(String tei, String source, String repositoryDocId, String type, String date) {
        try {
            GridFS gfs = new GridFS(db, MongoCollectionsInterface.METADATAS_TEIS);
            gfs.remove(repositoryDocId + ".tei.xml");
            GridFSInputFile gfsFile = gfs.createFile(new ByteArrayInputStream(tei.getBytes()), true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(date));
            gfsFile.setFilename(repositoryDocId + ".tei.xml");
            gfsFile.put("repositoryDocId", repositoryDocId);
            gfsFile.put("anhalyticsId", generateAnhalyticsId(repositoryDocId));
            gfsFile.put("source", source);
            gfsFile.put("documentType", type);
            gfsFile.save();
        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    private String generateAnhalyticsId(String repositoryDocId) {
        DBCollection collection = db.getCollection(MongoCollectionsInterface.IDENTIFIERS);
        BasicDBObject document = new BasicDBObject();
        document.put("repositoryDocId", repositoryDocId);
        collection.insert(document);
        currentAnhalyticsId = document.getObjectId("_id").toString();
        return currentAnhalyticsId;
    }

    /**
     * Inserts PDF binary document in the GridFS.
     */
    public void insertBinaryDocument(InputStream file, String source, String repositoryDocId, String type, String date) {
        try {
            GridFS gfs = new GridFS(db, MongoCollectionsInterface.BINARIES);
            gfs.remove(repositoryDocId + ".pdf");
            GridFSInputFile gfsFile = gfs.createFile(file, true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(date));
            gfsFile.setFilename(repositoryDocId + ".pdf");
            gfsFile.put("repositoryDocId", repositoryDocId);
            gfsFile.put("anhalyticsId", currentAnhalyticsId);
            gfsFile.put("source", source);
            gfsFile.put("documentType", type);
            gfsFile.setContentType("application/pdf");
            gfsFile.save();
        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getCause());
        }

    }

    /**
     * Updates already existing tei with new (more enriched one, fulltext..).
     */
    public void updateTei(String newTei, String repositoryDocId, boolean isFulltextAdded) {
        try {
            GridFS gfs = new GridFS(db, MongoCollectionsInterface.FINAL_TEIS);
            GridFSDBFile gdf = gfs.findOne(repositoryDocId + ".tei.xml");
            GridFSInputFile gfsNew = gfs.createFile(new ByteArrayInputStream(newTei.getBytes()), true);
            gfsNew.put("uploadDate", gdf.getUploadDate());
            gfsNew.setFilename(gdf.get("repositoryDocId") + ".tei.xml");
            gfsNew.put("repositoryDocId", gdf.get("repositoryDocId"));
            gfsNew.put("documentType", gdf.get("documentType"));
            gfsNew.put("anhalyticsId", gdf.get("anhalyticsId"));
            gfsNew.put("source", gdf.get("source"));
            Object o = gdf.get("isFulltextAdded");

            if (o == null) {
                gfsNew.put("isFulltextAdded", isFulltextAdded);
            } else {
                gfsNew.put("isFulltextAdded", gdf.get("isFulltextAdded"));
            }

            gfsNew.save();
            gfs.remove(gdf);
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
        }
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
            logger.error(e.getMessage(), e.getCause());
        }

    }

    /**
     * Inserts document assets with for repositoryDocId in the GridFS.
     */
    public void insertGrobidAssetDocument(InputStream file, String repositoryDocId, String anhalyticsId, String fileName, String dateString) throws ParseException {
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
            gfsFile.put("anhalyticsId", anhalyticsId);
            gfsFile.put("source", currentDocSource);
            gfsFile.setContentType("image/png");
            gfsFile.save();
        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    /**
     * Inserts publication annex document.
     */
    public void insertAnnexDocument(InputStream file, String source, String repositoryDocId, String fileName, String dateString) throws ParseException {
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
            gfsFile.put("anhalyticsId", currentAnhalyticsId);
            gfsFile.save();
        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    public void removeDocument(String filename) {
        try {
            GridFS gfs = new GridFS(db, FINAL_TEIS);
            gfs.remove(filename);
        } catch (MongoException e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    /**
     * Returns the annotation given a repositoryDocId and id (xml:id).
     */
    public String getAnnotation(String repositoryDocId, String id, String annotationsCollection) {
        DBCollection c = null;
        c = db.getCollection(annotationsCollection);
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

    public List<TEI> findEmbargoRecordsByDate(String date) {
        List<TEI> files = new ArrayList<TEI>();
        if (db.collectionExists(TO_REQUEST_LATER)) {
            collection = db.getCollection(TO_REQUEST_LATER);
            BasicDBObject query = new BasicDBObject("date", date);
            DBCursor curs = collection.find(query);
            try {
                if (curs.hasNext()) {
                    DBObject entry = curs.next();
                    String url = (String) entry.get("url");
                    String id = (String) entry.get("repositoryDocId");
                    String type = (String) entry.get("documentType");
                    boolean isAnnex = (Boolean) entry.get("isAnnex");
                    PublicationFile pf = new PublicationFile(url, date, isAnnex);
                    TEI metadata = new TEI(id, pf, null, id, type, null, null);
                    files.add(metadata);
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
    public String getAnnotations(String anhalyticsId, String annotationsCollection) {
        DBCollection c = db.getCollection(annotationsCollection);
        c.ensureIndex(new BasicDBObject("repositoryDocId", 1));
        c.ensureIndex(new BasicDBObject("anhalyticsId", 1));

        String result = null;
        BasicDBObject query = new BasicDBObject("anhalyticsId", anhalyticsId);
        DBCursor curs = null;
        try {
            curs = c.find(query);
            if (curs.hasNext()) {
                // we get now the sub-document corresponding to the annotations
                DBObject annotations = curs.next();
                //int ind = annotationsCollection.indexOf("_"); // TBR: this is dirty !
                //BasicDBList annot = (BasicDBList) annotations.get(annotationsCollection.substring(0, ind));
                if (annotations != null) {
                    result = annotations.toString();
                }
            } else {
                logger.error("The annotations for doc " + anhalyticsId + " was not found in " + annotationsCollection);
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
    public boolean isWithFulltext(String anhalyticsId) {

        GridFS gfs = new GridFS(db, MongoCollectionsInterface.FINAL_TEIS);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("anhalyticsId", anhalyticsId);

        Boolean result = false;
        GridFSDBFile fs = gfs.findOne(whereQuery);
        if (fs == null) {
            return false;
        }
        Object o = fs.get("isWithFulltext");

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
    public boolean isAnnotated(String annotationsCollection) {
        DBCollection c = null;
        c = db.getCollection(annotationsCollection);
        c.ensureIndex(new BasicDBObject("anhalyticsId", 1));
        c.ensureIndex(new BasicDBObject("xml:id", 1));
        boolean result = false;
        BasicDBObject query = new BasicDBObject("anhalyticsId", currentAnhalyticsId);

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
    public InputStream findAssetFile(String repositoryDocId, String filename) throws FileNotFoundException {
        InputStream file = null;
        try {
            GridFSDBFile cursor = findAssetBasicDBFile(repositoryDocId, filename);
            file = cursor.getInputStream();
        } catch (Exception exp) {
            throw new FileNotFoundException("Not found asset.");
        }
        return file;
    }

    private GridFSDBFile findAssetBasicDBFile(String repositoryDocId, String filename) {
        GridFS gfs = new GridFS(db, MongoCollectionsInterface.GROBID_ASSETS);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("repositoryDocId", repositoryDocId);
        whereQuery.put("filename", filename);
        GridFSDBFile cursor = gfs.findOne(whereQuery);
        return cursor;
    }

    public String findFinalTeiById(String anhalyticsId) {
        return findTeiById(anhalyticsId, MongoCollectionsInterface.FINAL_TEIS);
    }

    public String findGrobidTeiById(String anhalyticsId) {
        return findTeiById(anhalyticsId, MongoCollectionsInterface.GROBID_TEIS);
    }

    public String findMetadataTeiById(String anhalyticsId) {
        return findTeiById(anhalyticsId, MongoCollectionsInterface.METADATAS_TEIS);
    }

    private String findTeiById(String anhalyticsId, String collection) {
        String tei = null;
        try {
            GridFSDBFile file = findGridFSDBfileTeiById(anhalyticsId, collection);
            InputStream teiStream = file.getInputStream();
            tei = IOUtils.toString(teiStream);
            teiStream.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
        }
        return tei;
    }

    private GridFSDBFile findGridFSDBfileTeiById(String anhalyticsId, String collection) {
        GridFS gfs = new GridFS(db, collection);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("anhalyticsId", anhalyticsId);
        GridFSDBFile file = gfs.findOne(whereQuery);
        return file;
    }

    /**
     * Saves the issue occurring while processing.
     */
    public void save(String repositoryDocId, String process, String desc, String date) {
        try {
            DBCollection collection = db.getCollection(HARVEST_DIAGNOSTIC);

            BasicDBObject document = new BasicDBObject();
            document.put("repositoryDocId", repositoryDocId);
            document.put("process", process);

            collection.findAndRemove(document);

            BasicDBObject index = new BasicDBObject();
            index.put("repositoryDocId", 1);
            index.put("process", 1);
            collection.ensureIndex(index, "index", true);
            document.put("desc", desc);
            if (date == null) {
                date = Utilities.formatDate(new Date());
            }
            document.put("date", date);
            collection.insert(document);
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    /**
     * Saves the resource to be requested later (embargo).
     */
    public void log(String repositoryDocId, String url, String type, boolean isAnnex, String desc, String date) {
        try {
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
            document.put("documentType", type);
            document.put("anhalyticsId", currentAnhalyticsId);
            if (date == null) {
                date = Utilities.formatDate(new Date());
            }
            document.put("date", date);
            collection.insert(document);
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    public void removeEmbargoRecord(String repositoryDocId, String url) {
        try {
            DBCollection collection = db.getCollection(TO_REQUEST_LATER);
            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("repositoryDocId", repositoryDocId);
            whereQuery.put("url", url);
            collection.remove(whereQuery);
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
        }
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
    public String getCurrentAnhalyticsId() {
        return currentAnhalyticsId;
    }

    /**
     * @return the currentDocType
     */
    public String getCurrentDocType() {
        return currentDocType;
    }

    /**
     * @param currentDocType the currentDocType to set
     */
    public void setCurrentDocType(String currentDocType) {
        this.currentDocType = currentDocType;
    }

    /**
     * @return the currentFileName
     */
    public String getCurrentFileName() {
        return currentFileName;
    }

    /**
     * @param currentFileName the currentFileName to set
     */
    public void setCurrentFileName(String currentFileName) {
        this.currentFileName = currentFileName;
    }

    public void addLegendToAsset(String legend) {
        GridFSDBFile file = findAssetBasicDBFile(currentRepositoryDocId, currentFileName);
        file.put("legend", legend);
        file.save();
    }

    /**
     * @return the currentIsWithFulltext
     */
    public boolean isCurrentIsWithFulltext() {
        return currentIsWithFulltext;
    }

    /**
     * @param currentIsWithFulltext the currentIsWithFulltext to set
     */
    public void setCurrentIsWithFulltext(boolean currentIsWithFulltext) {
        this.currentIsWithFulltext = currentIsWithFulltext;
    }

    /**
     * @return the currentDocSource
     */
    public String getCurrentDocSource() {
        return currentDocSource;
    }

    /**
     * @param currentDocSource the currentDocSource to set
     */
    public void setCurrentDocSource(String currentDocSource) {
        this.currentDocSource = currentDocSource;
    }
}
