package fr.inria.anhalytics.commons.managers;

import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.util.JSON;

import fr.inria.anhalytics.commons.data.Annotation;
import fr.inria.anhalytics.commons.data.BinaryFile;
import fr.inria.anhalytics.commons.data.Identifier;
import fr.inria.anhalytics.commons.data.IstexFile;
import fr.inria.anhalytics.commons.data.TEIFile;
import fr.inria.anhalytics.commons.exceptions.DataException;
import fr.inria.anhalytics.commons.exceptions.FileNotFoundException;
import fr.inria.anhalytics.commons.utilities.Utilities;

import org.apache.commons.io.IOUtils;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Class for retrieving, inserting, updating and organizing metadata TEIs, PDFs
 * and extracted TEI files from MongoDB GridFS.
 * <p>
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

    private DBObject temp = null;
    private DBCursor cursor = null;
    private DBCollection collection = null;

    public MongoFileManager(boolean isTest) {
        super(isTest);
        initDatabase();
    }

    public static MongoFileManager getInstance(boolean isTest) {
        if (mongoManager == null) {
            return getNewInstance(isTest);
        } else {
            return mongoManager;
        }
    }

    protected static synchronized MongoFileManager getNewInstance(boolean isTest) {
        mongoManager = new MongoFileManager(isTest);
        return mongoManager;
    }

    /**
     * This initializes cursor for binaries collection.
     */
    public boolean initBinaries(String date) {
        setGridFSCollection(MongoCollectionsInterface.BINARIES);
        BasicDBObject bdbo = new BasicDBObject();
        try {
            if (date != null) {
                bdbo.append("uploadDate", Utilities.parseStringDate(date));
            }
            cursor = gfs.getFileList(bdbo);
            cursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
            indexFile = 0;
        } catch (ParseException e) {
            logger.error("", e);
        }
        if (cursor.size() > 0) {
            logger.info(cursor.size() + " documents found for : " + date);
            return true;
        } else {
            return false;
        }
    }

    /**
     * This initializes cursor for binaries collection.
     */
    public boolean initIstexPdfs() {
        setGridFSCollection(MongoCollectionsInterface.ISTEX_PDFS);
        BasicDBObject bdbo = new BasicDBObject();
        cursor = gfs.getFileList(bdbo);
        cursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
        indexFile = 0;
        if (cursor.size() > 0) {
            logger.info(cursor.size() + " documents found");
            return true;
        } else {
            return false;
        }
    }
    
     /**
     * This initializes cursor for binaries collection.
     */
    public boolean initIstexTeis() {
        setGridFSCollection(MongoCollectionsInterface.ISTEX_TEIS);
        BasicDBObject bdbo = new BasicDBObject();
        cursor = gfs.getFileList(bdbo);
        cursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
        indexFile = 0;
        if (cursor.size() > 0) {
            logger.info(cursor.size() + " documents found");
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
    
    public boolean initQuantitiesAnnotations() throws MongoException {
    collection = getCollection(MongoCollectionsInterface.QUANTITIES_ANNOTATIONS);
        // index on filename and xml:id
        BasicDBObject index = new BasicDBObject();
        index.put("repositoryDocId", 1);
        index.put("category", 1);
        collection.ensureIndex(index, "index", true);
        BasicDBObject bdbo = new BasicDBObject();

        cursor = collection.find(bdbo);
        indexFile = 0;
        if (cursor.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This initializes cursor for tei generated collection.
     */
    public boolean initTeis(String date, String teiCollection) throws MongoException {
        try {
            // Create indexes before using the data in it.
            // Github issue: https://github.com/anHALytics/anHALytics-core/issues/60
            setGridFSCollection(teiCollection);

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
            logger.error("Cannot parse date: " + date, e);
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
     * @param date
     * @param annotationsCollection the annotation collection to initialize,
     * e.g. MongoCollectionsInterface.NERD_ANNOTATIONS,
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

    public boolean initIdentifiersWithoutPdfUrl() {
        collection = getCollection(MongoCollectionsInterface.IDENTIFIERS);

        BasicDBObject query = new BasicDBObject("doi", new BasicDBObject("$ne", ""));
        query.append("pdfUrl", "");
        cursor = collection.find(query);
        indexFile = 0;
        //181974 init
        logger.info("Found " + cursor.size() + " doi elements.");
        return true;
    }

    public boolean initIdentifiers() {
        collection = getCollection(MongoCollectionsInterface.IDENTIFIERS);
        cursor = collection.find();
        indexFile = 0;
        System.out.println(cursor.size());
        return true;
    }

    public boolean hasMore() {
        if (indexFile < cursor.size()) {
            return true;
        } else {
            return false;
        }
    }

    public Identifier nextIdentifier() {
        BasicDBObject obj = (BasicDBObject) cursor.next();
        Identifier id = new Identifier((String) obj.get("doi"), (String) obj.get("repositoryDocId"), (String) obj.getObjectId("_id").toString());
        if (!cursor.hasNext()) {
            cursor.close();
        }
        indexFile++;
        return id;
    }

    public Annotation nextAnnotation() {
        DBObject obj = cursor.next();
        Annotation annotation = new Annotation(obj.toString(), (String) obj.get("repositoryDocId"), (String) obj.get("anhalyticsId"));
        if (!cursor.hasNext()) {
            cursor.close();
        }
        indexFile++;
        return annotation;
    }
    
    public Annotation nextQuantitiesAnnotation() {
        DBObject obj = cursor.next();
        Annotation annotation = new Annotation(obj.toString(), (String) obj.get("repositoryDocId"), (String) obj.get("repositoryDocId"));
        if (!cursor.hasNext()) {
            cursor.close();
        }
        indexFile++;
        return annotation;
    }

    public TEIFile nextTeiDocument() {
        InputStream teiStream = null;
        TEIFile tei = new TEIFile();
        DBObject obj = cursor.next();
        tei.setRepositoryDocId((String) obj.get("repositoryDocId"));
        tei.setFileName(tei.getRepositoryDocId() + ".tei.xml");
        tei.setDocumentType((String) obj.get("documentType"));
        tei.setAnhalyticsId((String) obj.get("anhalyticsId"));
        tei.setSource((String) obj.get("source"));
        tei.setRepositoryDocVersion((String) obj.get("version"));
        GridFSDBFile binaryfile = gfs.findOne(tei.getFileName());

        tei.setFileType((String) binaryfile.getContentType());
        indexFile++;
        teiStream = binaryfile.getInputStream();
        try {
            tei.setTei(IOUtils.toString(teiStream));
            teiStream.close();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex.getCause());
        }
        return tei;
    }
    
    public BinaryFile nextBinaryDocument() {
        BinaryFile binaryFile = new BinaryFile();
        DBObject obj = cursor.next();
        binaryFile.setRepositoryDocId((String) obj.get("repositoryDocId"));
        binaryFile.setAnhalyticsId((String) obj.get("anhalyticsId"));
        binaryFile.setSource((String) obj.get("source"));
        binaryFile.setRepositoryDocVersion((String) obj.get("version"));
        GridFSDBFile binaryfile = gfs.findOne(binaryFile.getRepositoryDocId() + ".pdf");
        binaryFile.setDocumentType((String) binaryfile.get("documentType"));
        binaryFile.setFileType((String) binaryfile.getContentType());
        binaryFile.setStream(binaryfile.getInputStream());
        indexFile++;
        return binaryFile;
    }

    public IstexFile nextIstexBinaryDocument() {
        IstexFile istexBinaryFile = new IstexFile();
        DBObject obj = cursor.next();
        istexBinaryFile.setRepositoryDocId((String) obj.get("identifier"));
        istexBinaryFile.setCategory((String) obj.get("category"));
        GridFSDBFile binaryfile = gfs.findOne(istexBinaryFile.getRepositoryDocId() + ".pdf");
        istexBinaryFile.setStream(binaryfile.getInputStream());
        indexFile++;
        return istexBinaryFile;
    }
    
        public TEIFile nextIstexTeiDocument() {
            InputStream teiStream = null;
        TEIFile tei = new TEIFile();
        DBObject obj = cursor.next();
        tei.setRepositoryDocId((String) obj.get("identifier"));
        GridFSDBFile binaryfile = gfs.findOne(tei.getRepositoryDocId() + ".tei.xml");
        teiStream = binaryfile.getInputStream();
        try {
            tei.setTei(IOUtils.toString(teiStream));
            teiStream.close();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex.getCause());
        }
        indexFile++;
        return tei;
    }

//    public String nextAsset() {
//        InputStream input = null;
//        DBObject obj = cursor.next();
//        setCurrentRepositoryDocId((String) obj.get("repositoryDocId"));
//        setCurrentFileType((String) obj.get("contentType"));
//        currentFileName = (String) obj.get("filename");
//        setCurrentDocSource((String) obj.get("source"));
//        setCurrentAnhalyticsId((String) obj.get("anhalyticsId"));
//        indexFile++;
//        return currentFileName;
//    }
    /**
     * Inserts json entry representing annotation result.
     */
    public boolean insertAnnotation(String json, String annotationsCollection) {
        boolean done = false;
        if ( (json == null) || (json.length() == 0) ) {
            // there is nothing to insert, so we can assume that we are effectively doing
            // nothing
            return true;
        }
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
    
    /*public boolean insertQuantitiesAnnotation(String json, String annotationsCollection) {
        boolean done = false;
        try {
            DBCollection c = null;
            c = db.getCollection(annotationsCollection);
            BasicDBObject index = new BasicDBObject();
            index.put("repositoryDocId", 1);
            //index.put("category", 1);
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
    }*/

    /**
     * Inserts generated tei using GridFS.
     */
    public void insertTei(TEIFile tei, String date, String collection) {
        try {
            GridFS gfs = new GridFS(db, collection);
            gfs.remove(tei.getFileName());
            GridFSInputFile gfsFile = gfs.createFile(new ByteArrayInputStream(tei.getTei().getBytes()), true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(date));
            gfsFile.setFilename(tei.getFileName());
            gfsFile.put("repositoryDocId", tei.getRepositoryDocId());
            if (collection.equals(MongoCollectionsInterface.METADATAS_TEIS)) {
                String anhalyticsID = generateAnhalyticsId(tei.getRepositoryDocId(), tei.getDoi(), (tei.getPdfdocument() != null) ? tei.getPdfdocument().getUrl() : null);
                gfsFile.put("anhalyticsId", anhalyticsID);
                if (tei.getPdfdocument() != null) {
                    tei.getPdfdocument().setAnhalyticsId(anhalyticsID);
                }
                for (BinaryFile annex : tei.getAnnexes()) {
                    annex.setAnhalyticsId(anhalyticsID);
                }
            } else {
                gfsFile.put("anhalyticsId", tei.getAnhalyticsId());
            }
            gfsFile.put("source", tei.getSource());
            gfsFile.put("version", tei.getRepositoryDocVersion());
            gfsFile.put("documentType", tei.getDocumentType());
            gfsFile.setContentType(tei.getFileType());
            gfsFile.save();
        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    /**
     * Inserts grobid tei using GridFS.
     */
    public void insertGrobidTei(String teiString, String repositoryDocId, String anhalyticsId, String version, String source, String type, String date) {
        try {
            GridFS gfs = new GridFS(db, MongoCollectionsInterface.GROBID_TEIS);
            gfs.remove(repositoryDocId + ".tei.xml");
            GridFSInputFile gfsFile = gfs.createFile(new ByteArrayInputStream(teiString.getBytes()), true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(date));
            gfsFile.setFilename(repositoryDocId + ".tei.xml");
            gfsFile.put("repositoryDocId", repositoryDocId);
            gfsFile.put("anhalyticsId", anhalyticsId);
            gfsFile.put("source", source);
            gfsFile.put("version", version);
            gfsFile.put("documentType", type);
            gfsFile.save();
        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    /**
     * Inserts TEI metadata document in the GridFS.
     */
    public void insertMetadataTei(String tei, String doi, String pdfUrl, String source, String repositoryDocId, String version, String type, String date) {
        try {
            GridFS gfs = new GridFS(db, MongoCollectionsInterface.METADATAS_TEIS);
            gfs.remove(repositoryDocId + ".tei.xml");
            GridFSInputFile gfsFile = gfs.createFile(new ByteArrayInputStream(tei.getBytes()), true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(date));
            gfsFile.setFilename(repositoryDocId + ".tei.xml");
            gfsFile.put("repositoryDocId", repositoryDocId);
            gfsFile.put("anhalyticsId", generateAnhalyticsId(repositoryDocId, doi, pdfUrl));
            gfsFile.put("source", source);
            gfsFile.put("version", version);
            gfsFile.put("documentType", type);
            gfsFile.save();
        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    public boolean insertCrossRefMetadata(String currentAnhalyticsId, String currentRepositoryDocId, String crossRefMetadata) {
        boolean done = false;
        try {
            DBCollection c = null;
            c = db.getCollection(MongoCollectionsInterface.CROSSREF_METADATAS);
            BasicDBObject index = new BasicDBObject();
            index.put("repositoryDocId", 1);
            index.put("anhalyticsId", 1);
            c.ensureIndex(index, "index", true);
            DBObject dbObject = (DBObject) JSON.parse(crossRefMetadata);
            WriteResult result = c.insert(dbObject);
            CommandResult res = result.getCachedLastError();
            if ((res != null) && (res.ok())) {
                done = true;
            } else {
                done = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return done;
    }

    public boolean isWithoutDoi(String anhalyticsId) {
        String doi = this.getDocumentDoi(anhalyticsId);
        if (doi == null || doi.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public String getDocumentDoi(String anhalyticsId) {
        DBCollection collection = db.getCollection(MongoCollectionsInterface.IDENTIFIERS);
        DBObject query = new BasicDBObject("_id", new ObjectId(anhalyticsId));
        temp = collection.findOne(query);
        String doi = (String) temp.get("doi");
        return doi;
    }

    //synchronize
    public void updateDoi(String anhalyticsId, String doi) {
        DBCollection collection = db.getCollection(MongoCollectionsInterface.IDENTIFIERS);
        temp.put("doi", doi);
        collection.update(new BasicDBObject("_id", new ObjectId(anhalyticsId)), temp);
    }

    private String generateAnhalyticsId(String repositoryDocId, String doi, String pdfUrl) {
        DBCollection collection = db.getCollection(MongoCollectionsInterface.IDENTIFIERS);
        BasicDBObject document = new BasicDBObject();
        /*BasicDBObject index = new BasicDBObject();
            index.put("repositoryDocId", 1);
            collection.ensureIndex(index, "index", true);
         */
        document.put("repositoryDocId", repositoryDocId);
        BasicDBObject temp = (BasicDBObject) collection.findOne(document);
        if (temp != null) {
            return temp.getObjectId("_id").toString();
        } else {
            document.put("doi", doi);
            document.put("pdfUrl", pdfUrl);
            collection.insert(document);
            return document.getObjectId("_id").toString();
        }
    }

    /**
     * Inserts PDF binary document in the GridFS.
     */
    public void insertBinaryDocument(BinaryFile bf, String date) {
        try {
            GridFS gfs = new GridFS(db, MongoCollectionsInterface.BINARIES);
            gfs.remove(bf.getFileName());
            GridFSInputFile gfsFile = gfs.createFile(bf.getStream(), true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(date));
            gfsFile.setFilename(bf.getFileName());
            gfsFile.put("repositoryDocId", bf.getRepositoryDocId());
            gfsFile.put("anhalyticsId", bf.getAnhalyticsId());
            gfsFile.put("source", bf.getSource());
            gfsFile.put("version", bf.getRepositoryDocVersion());
            gfsFile.put("documentType", bf.getDocumentType());
            gfsFile.setContentType(bf.getFileType());
            gfsFile.save();
        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getCause());
        }

    }

    /**
     * Updates already existing tei with new (more enriched one, fulltext..).
     */
    public void updateTei(String newTei, String repositoryDocId, String collection) {
        try {
            GridFS gfs = new GridFS(db, collection);
            GridFSDBFile gdf = gfs.findOne(repositoryDocId + ".tei.xml");
            GridFSInputFile gfsNew = gfs.createFile(new ByteArrayInputStream(newTei.getBytes()), true);
            gfsNew.put("uploadDate", gdf.getUploadDate());
            gfsNew.setFilename(gdf.get("repositoryDocId") + ".tei.xml");
            gfsNew.put("repositoryDocId", gdf.get("repositoryDocId"));
            gfsNew.put("documentType", gdf.get("documentType"));
            gfsNew.put("anhalyticsId", gdf.get("anhalyticsId"));
            gfsNew.put("source", gdf.get("source"));

            gfsNew.save();
            gfs.remove(gdf);
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    /**
     * inserts a Arxiv/istex TEI document in the GridFS.
     */
    public void insertPDFDocument(InputStream file, String identifier, String category, String namespace) {
//        try {
        GridFS gfs = new GridFS(db, namespace);
        GridFSInputFile gfsFile = gfs.createFile(file, true);
        //gfs.remove(identifier + ".pdf");
        gfsFile.setFilename(identifier + ".pdf");
        gfsFile.put("identifier", identifier);
        gfsFile.put("anhalyticsId", identifier);
        gfsFile.put("category", category);
        gfsFile.save();
//        } catch (ParseException e) {
//            logger.error(e.getMessage(), e.getCause());
//        }

    }
    
    /**
     * inserts a Arxiv/istex TEI document in the GridFS.
     */
    public void insertTEIDocument(InputStream file, String identifier, String category, String namespace) {
//        try {
        GridFS gfs = new GridFS(db, namespace);
        GridFSInputFile gfsFile = gfs.createFile(file, true);
        //gfs.remove(identifier + ".pdf");
        gfsFile.setFilename(identifier + ".tei.xml");
        gfsFile.put("identifier", identifier);
        gfsFile.put("category", category);
        gfsFile.save();
//        } catch (ParseException e) {
//            logger.error(e.getMessage(), e.getCause());
//        }

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
//
//    /**
//     * Inserts document assets with for repositoryDocId in the GridFS.
//     */
//    public void insertGrobidAssetDocument(InputStream file, String repositoryDocId, String anhalyticsId, String fileName, String dateString) throws ParseException {
//        try {
//            GridFS gfs = new GridFS(db, MongoCollectionsInterface.GROBID_ASSETS);
//            BasicDBObject whereQuery = new BasicDBObject();
//            whereQuery.put("repositoryDocId", repositoryDocId);
//            whereQuery.put("fileName", fileName);
//            gfs.remove(whereQuery);
//            //version ?
//            GridFSInputFile gfsFile = gfs.createFile(file, true);
//            gfsFile.put("uploadDate", Utilities.parseStringDate(dateString));
//            gfsFile.setFilename(fileName);
//            gfsFile.put("repositoryDocId", repositoryDocId);
//            gfsFile.put("anhalyticsId", anhalyticsId);
//            gfsFile.put("source", getCurrentDocSource());
//            gfsFile.setContentType("image/png");
//            gfsFile.save();
//        } catch (ParseException e) {
//            logger.error(e.getMessage(), e.getCause());
//        }
//    }

    /**
     * Inserts publication annex document.
     */
    public void insertAnnexDocument(BinaryFile bf, String dateString) throws ParseException {
        try {
            GridFS gfs = new GridFS(db, MongoCollectionsInterface.PUB_ANNEXES);
            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("repositoryDocId", bf.getRepositoryDocId());
            whereQuery.put("filename", bf.getFileName());
            gfs.remove(whereQuery);
            //version ?
            GridFSInputFile gfsFile = gfs.createFile(bf.getStream(), true);
            gfsFile.put("uploadDate", Utilities.parseStringDate(dateString));
            gfsFile.setFilename(bf.getFileName());
            gfsFile.put("source", bf.getSource());
            gfsFile.put("version", bf.getRepositoryDocVersion());
            gfsFile.put("repositoryDocId", bf.getRepositoryDocId());
            gfsFile.put("anhalyticsId", bf.getAnhalyticsId());
            gfsFile.save();
        } catch (ParseException e) {
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
//
//    public List<TEIFile> findEmbargoRecordsByDate(String date) {
//        List<TEIFile> files = new ArrayList<TEIFile>();
//        if (db.collectionExists(TO_REQUEST_LATER)) {
//            collection = db.getCollection(TO_REQUEST_LATER);
//            BasicDBObject query = new BasicDBObject("date", date);
//            DBCursor curs = collection.find(query);
//            try {
//                if (curs.hasNext()) {
//                    DBObject entry = curs.next();
//                    String url = (String) entry.get("url");
//                    String id = (String) entry.get("repositoryDocId");
//                    String type = (String) entry.get("documentType");
//                    boolean isAnnex = (Boolean) entry.get("isAnnex");
//                    PublicationFile pf = new PublicationFile(url, date, isAnnex);
//                    TEIFile metadata = new TEIFile(id, pf, null, id, type, null, null, null);
//                    files.add(metadata);
//                }
//            } finally {
//                if (curs != null) {
//                    curs.close();
//                }
//            }
//        }
//        return files;
//    }

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
                logger.warn("The annotations for doc " + anhalyticsId + " was not found in " + annotationsCollection);
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
    public boolean isGrobidified(String repositoryDocId, String version) {
        GridFS gfs = new GridFS(db, MongoCollectionsInterface.GROBID_TEIS);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("repositoryDocId", repositoryDocId);
        whereQuery.put("version", version);
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
     * Checks if the tei corpus document was already data mined.
     */
    public boolean isFinalTeiCreated(String repositoryDocId) {
        GridFS gfs = new GridFS(db, MongoCollectionsInterface.METADATA_WITHFULLTEXT_TEIS);
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
    public boolean isAnnotated(String annotationsCollection, String anhalyticsId) {
        DBCollection c = null;
        c = db.getCollection(annotationsCollection);
        c.ensureIndex(new BasicDBObject("anhalyticsId", 1));
        c.ensureIndex(new BasicDBObject("xml:id", 1));
        boolean result = false;
        BasicDBObject query = new BasicDBObject("anhalyticsId", anhalyticsId);

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

    public String findFulltextTeiById(String anhalyticsId) {
        return findTeiById(anhalyticsId, MongoCollectionsInterface.METADATA_WITHFULLTEXT_TEIS);
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
            throw new DataException(e);
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
    
    public String findGridFSDBfileIstexTeiById(String id) {
        String tei = null;
        try {
        GridFS gfs = new GridFS(db, MongoCollectionsInterface.ISTEX_TEIS);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("identifier", id);
        GridFSDBFile file = gfs.findOne(whereQuery);
        InputStream teiStream = file.getInputStream();
            tei = IOUtils.toString(teiStream);
            teiStream.close();
        } catch (Exception e) {
            throw new DataException(e);
        }
        return tei;
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
    public void log(String repositoryDocId, String anhalyticsId, String url, String type, boolean isAnnex, String desc, String date) {
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
            document.put("anhalyticsId", anhalyticsId);
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
//
//    public void removeIdentifier() {
//        try {
//            BasicDBObject whereQuery = new BasicDBObject();
//            whereQuery.put("_id", new ObjectId(getCurrentAnhalyticsId()));
//            collection.remove(whereQuery);
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e.getCause());
//        }
//    }

    public void setGridFSCollection(String collectionName) {
        if (gfs == null || !gfs.getBucketName().equals(collectionName)) {
            gfs = new GridFS(db, collectionName);

            gfs.getDB().getCollection(collectionName + ".files")
                    .createIndex(new BasicDBObject("uploadDate", 1).append("isWithFulltext", 1));

            gfs.getDB().getCollection(collectionName + ".files")
                    .createIndex(new BasicDBObject("uploadDate", 1));

        }
    }

//    public void addLegendToAsset(String legend) {
//        GridFSDBFile file = findAssetBasicDBFile(getCurrentRepositoryDocId(), currentFileName);
//        file.put("legend", legend);
//        file.save();
//    }
}
