package fr.inria.anhalytics.commons.managers;

import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.util.JSON;

import fr.inria.anhalytics.commons.data.Annotation;
import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.data.BinaryFile;
import fr.inria.anhalytics.commons.data.Processings;
import fr.inria.anhalytics.commons.exceptions.DataException;
import fr.inria.anhalytics.commons.utilities.Utilities;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static final DBObject ONLY_WITH_FULLTEXT_PROCESS = new BasicDBObjectBuilder()
            .add("isWithFulltext", true)
            .get();

    public static final DBObject ONLY_NOT_PROCESSED_FULLTEXT_APPEND_PROCESS = new BasicDBObjectBuilder()
            .add("isFulltextAppended", false)
            .add("isWithFulltext", true)
            .get();

    public static final DBObject ONLY_WITH_FULLTEXT_NOT_PROCESSED_GROBID_PROCESS = new BasicDBObjectBuilder()
            .add("isWithFulltext", true)
            .add("$or",
                    Arrays.asList(
                            new BasicDBObject(Processings.GROBID.getName(), new BasicDBObject("$exists", false)),
                            new BasicDBObject(Processings.GROBID.getName(), false)))
            .get();

    public static final DBObject ONLY_NOT_PROCESSED_TRANSFORM_METADATA_PROCESS = new BasicDBObjectBuilder()
            .add("isProcessedPub2TEI", false).get();

    public static final DBObject ONLY_TRANSFORMED_METADATA = new BasicDBObjectBuilder()
            .add("isProcessedPub2TEI", true)
            .get();

    public static final DBObject ONLY_NOT_MINED_INIT_KB_PROCESS = new BasicDBObjectBuilder()
            .add("isMined", false)
            .add("isProcessedPub2TEI", true)
            .get();

    public static final DBObject ONLY_NOT_NERD_ANNOTATED_TRANSFORMED_METADATA = new BasicDBObjectBuilder()
            .add("isProcessedPub2TEI", true)
            .add("$or",
                    Arrays.asList(
                            new BasicDBObject(Processings.NERD.getName(), new BasicDBObject("$exists", false)),
                            new BasicDBObject(Processings.NERD.getName(), false)))
            .get();

    public static final DBObject ONLY_NOT_KEYTERM_ANNOTATED_TRANSFORMED_METADATA = new BasicDBObjectBuilder()
            .add("isProcessedPub2TEI", true)
            .add("$or",
                    Arrays.asList(
                            new BasicDBObject(Processings.KEYTERM.getName(), new BasicDBObject("$exists", false)),
                            new BasicDBObject(Processings.KEYTERM.getName(), false)))
            .get();

    public static final DBObject ONLY_NOT_QUANTITIES_ANNOTATED_TRANSFORMED_METADATA = new BasicDBObjectBuilder()
            .add("isProcessedPub2TEI", true)
            .add("$or",
                    Arrays.asList(
                            new BasicDBObject(Processings.QUANTITIES.getName(), new BasicDBObject("$exists", false)),
                            new BasicDBObject(Processings.QUANTITIES.getName(), false)))
            .get();

    public static final DBObject ONLY_NOT_PDFQUANTITIES_ANNOTATED_WITH_FULLTEXT = new BasicDBObjectBuilder()
            .add("isWithFulltext", true)
            .add("$or",
                    Arrays.asList(
                            new BasicDBObject(Processings.PDFQUANTITIES.getName(), new BasicDBObject("$exists", false)),
                            new BasicDBObject(Processings.PDFQUANTITIES.getName(), false)))
            .get();

    public static final DBObject ONLY_TRANSFORMED_METADATA_NOT_INDEXED = new BasicDBObjectBuilder()
            .add("isProcessedPub2TEI", true)
            .add("isIndexed", false)
            .get();

    public static final DBObject ONLY_NERD_ANNOTATED = new BasicDBObjectBuilder()
            .add(Processings.NERD.getName(), true)
            .get();

    public static final DBObject ONLY_KEYTERM_ANNOTATED = new BasicDBObjectBuilder()
            .add(Processings.KEYTERM.getName(), true)
            .get();

    public static final DBObject ONLY_QUANTITIES_ANNOTATED = new BasicDBObjectBuilder()
            .add(Processings.QUANTITIES.getName(), true)
            .get();


    /**
     * A static {@link MongoFileManager} object containing MongoFileManager
     * instance that can be used from different locations..
     */
    private static MongoFileManager mongoManager = null;

    private GridFS gfs = null;

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

    public boolean hasMore() {
        return cursor.hasNext();
    }

    public boolean isSavedObject(String repositoryDocId, String repositoryDocVersion) {
        return findObject(repositoryDocId, repositoryDocVersion) != null;
    }

    private BasicDBObject findObject(String repositoryDocId, String repositoryDocVersion) {
        DBCollection collection = db.getCollection(MongoCollectionsInterface.BIBLIO_OBJECTS);
        BasicDBObject document = new BasicDBObject();
        /*BasicDBObject index = new BasicDBObject();
            index.put("repositoryDocId", 1);
            collection.ensureIndex(index, "index", true);
         */
        document.put("repositoryDocId", repositoryDocId);
        if (repositoryDocVersion != null)
            document.put("repositoryDocVersion", repositoryDocVersion);
        BasicDBObject temp = (BasicDBObject) collection.findOne(document);
        return temp;
    }

    /**
     * This initializes cursor for existing objects by specifying a query.
     */
    public boolean initObjects(String source, DBObject query) throws MongoException {

        collection = getCollection(MongoCollectionsInterface.BIBLIO_OBJECTS);
        BasicDBObject index = new BasicDBObject();
        index.put("repositoryDocId", 1);
        index.put("anhalyticsId", 1);
        collection.createIndex(index, "index", true);

        BasicDBObject ensureIndexQuery = new BasicDBObject();

        query.toMap().keySet().stream().forEach(
                k ->{
                    if(!k.equals("$or"))
                        ensureIndexQuery.append((String) k, 1);
                }
                );

        collection.createIndex(ensureIndexQuery, "index_" + StringUtils.join(ensureIndexQuery.keySet(), "_"));
        cursor = collection.find(query);
        cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
        logger.info(cursor.size() + " objects found.");
        if (cursor.hasNext()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This initializes cursor for existing objects.
     */
    public boolean initObjects(String source) throws MongoException {

        collection = getCollection(MongoCollectionsInterface.BIBLIO_OBJECTS);
        BasicDBObject index = new BasicDBObject();
        index.put("repositoryDocId", 1);
        index.put("anhalyticsId", 1);
        collection.createIndex(index, "index", true);
        BasicDBObject bdbo = new BasicDBObject();
        if (source != null) {
            bdbo.append("source", source);
        }
        cursor = collection.find(bdbo);
        cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
        logger.info(cursor.size() + " objects found.");
        if (cursor.hasNext()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Updates object status(Processings) and resets status if new TEICorpus was
     * created (resetStatus).
     *
     * @param biblioObject
     */
    public void updateBiblioObjectStatus(BiblioObject biblioObject, Processings processing, boolean resetStatus) {
        DBCollection collection = db.getCollection(MongoCollectionsInterface.BIBLIO_OBJECTS);
        BasicDBObject newDocument = new BasicDBObject();
        ObjectId objectId = new ObjectId(biblioObject.getAnhalyticsId());
        newDocument.put("_id", objectId);
        newDocument.put("anhalyticsId", objectId.toString());
        newDocument.put("repositoryDocId", biblioObject.getRepositoryDocId());
        newDocument.put("source", biblioObject.getSource());
        newDocument.put("metadataURL", biblioObject.getMetadataURL());
        newDocument.put("publicationType", biblioObject.getPublicationType());
        newDocument.put("repositoryDocVersion", biblioObject.getRepositoryDocVersion());
        newDocument.put("doi", biblioObject.getDoi());
        newDocument.put("domains", biblioObject.getDomains());
        newDocument.put("isWithFulltext", biblioObject.getIsWithFulltext());
        newDocument.put("isFulltextAppended", biblioObject.getIsFulltextAppended());
        newDocument.put("isProcessedPub2TEI", biblioObject.getIsProcessedByPub2TEI());
        newDocument.put("isMined", biblioObject.getIsMined());
        newDocument.put("isIndexed", biblioObject.getIsIndexed());

        for (Processings p : Processings.values()) {
            if (processing != null && p.equals(processing)) {
                newDocument.put(processing.getName(), true);
            } else if (temp.get(p.getName()) != null) {
                //here should be handled the workflow (when for instance when text xml:ids change (new grobid tei is generated or tei corpus))
//                if(resetStatus)
//                    newDocument.put(p.getName(), false);
//                else
                newDocument.put(p.getName(), (boolean)temp.get(p.getName()));
            }
        }


        BasicDBObject searchQuery = new BasicDBObject().append("anhalyticsId", biblioObject.getAnhalyticsId());

        collection.update(searchQuery, newDocument);
    }

    /**
     * Saves the resource to be requested later (embargo).
     */
    public void insertBiblioObject(BiblioObject biblioObject) {
        DBCollection collection = db.getCollection(MongoCollectionsInterface.BIBLIO_OBJECTS);

        BasicDBObject index = new BasicDBObject();
        index.put("repositoryDocId", 1);
        index.put("anhalyticsId", 1);
        collection.createIndex(index, "index", true);

        BasicDBObject document = new BasicDBObject();
        /*BasicDBObject index = new BasicDBObject();
            index.put("repositoryDocId", 1);
            collection.ensureIndex(index, "index", true);
         */
        document.put("repositoryDocId", biblioObject.getRepositoryDocId());
        BasicDBObject temp = (BasicDBObject) collection.findOne(document);
        ObjectId objectId = new ObjectId();
        if (temp != null) {
            objectId = temp.getObjectId("_id");
            collection.remove(temp);
        }
        document.put("_id", objectId);
        document.put("anhalyticsId", objectId.toString());
        document.put("source", biblioObject.getSource());
        document.put("metadataURL", biblioObject.getMetadataURL());
        document.put("publicationType", biblioObject.getPublicationType());
        document.put("repositoryDocVersion", biblioObject.getRepositoryDocVersion());
        document.put("doi", biblioObject.getDoi());
        document.put("domains", biblioObject.getDomains());
        document.put("isWithFulltext", biblioObject.getIsWithFulltext());
        document.put("isFulltextAppended", biblioObject.getIsFulltextAppended());
        document.put("isProcessedPub2TEI", biblioObject.getIsProcessedByPub2TEI());
        document.put("isMined", biblioObject.getIsMined());
        document.put("isIndexed", biblioObject.getIsIndexed());

        try {
            insertMetadataDocument(biblioObject.getMetadata(), objectId.toString());
            if (biblioObject.getIsWithFulltext() && biblioObject.getPdf().getStream() != null) {
                insertPDFDocument(biblioObject.getPdf().getStream(), objectId.toString());
            }
            if (biblioObject.getAnnexes() != null) {
                for (BinaryFile bf : biblioObject.getAnnexes()) {
                    if (bf.getStream() != null) {
                        insertAnnexDocument(bf, objectId.toString());
                    }
                }
            }
        } catch (MongoException me) {
            me.printStackTrace();
            //rollback
        } catch (IOException ioe) {
            ioe.printStackTrace();
            //rollback
        }
        collection.insert(document);

    }

    public InputStream getFulltext(BiblioObject biblioObject) {
        InputStream fulltext = null;
        try {
            fulltext = getFulltextByAnhalyticsId(biblioObject.getAnhalyticsId());
        } catch (DataException de) {
            logger.error("No PDF document was found for : " + biblioObject.getAnhalyticsId(), de);
        }
        return fulltext;
    }

    public InputStream getFulltextByAnhalyticsId(String anhalyticsId) throws DataException {
        try {
            GridFS gfs = new GridFS(db, BINARIES);
            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("anhalyticsId", anhalyticsId);
            GridFSDBFile file = gfs.findOne(whereQuery);
            if (file == null) {
                throw new DataException("No fulltext available for document " + anhalyticsId);
            }
            return file.getInputStream();
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    public String getMetadata(BiblioObject biblioObject) {
        String metadata = null;
        try {
            metadata = this.getTei(biblioObject.getAnhalyticsId(), MongoCollectionsInterface.METADATAS_TEIS);
        } catch (DataException de) {
            logger.error("No metadata was found for " + biblioObject, de);
        }
        return metadata;
    }

    public String getTEICorpus(BiblioObject biblioObject) {
        String teiCorpus = null;
        try {
            teiCorpus = this.getTei(biblioObject.getAnhalyticsId(), MongoCollectionsInterface.TEI_CORPUS);
        } catch (DataException de) {
            logger.error("No TEI corpus was found for " + biblioObject, de);
        }
        return teiCorpus;
    }

    public String getGrobidTei(BiblioObject biblioObject) {
        String grobidTei = null;
        try {
            grobidTei = this.getTei(biblioObject.getAnhalyticsId(), MongoCollectionsInterface.GROBID_TEIS);
        } catch (DataException de) {
            logger.error("No corresponding fulltext TEI was found for " + biblioObject);
        }
        return grobidTei;
    }

    private String getTei(String anhalyticsId, String collection) throws DataException {
        String tei = null;
        try {
            GridFS gfs = new GridFS(db, collection);
            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("anhalyticsId", anhalyticsId);
            GridFSDBFile file = gfs.findOne(whereQuery);
            InputStream teiStream = file.getInputStream();
            tei = IOUtils.toString(teiStream, "UTF-8");
            teiStream.close();
        } catch (Exception e) {
            throw new DataException(e);
        }
        return tei;
    }

    public BiblioObject nextBiblioObject() {
        BiblioObject biblioObject = new BiblioObject();
        temp = cursor.next();
        biblioObject.setRepositoryDocId((String) temp.get("repositoryDocId"));
        biblioObject.setAnhalyticsId((String) temp.get("anhalyticsId"));
        biblioObject.setDomains((List<String>) temp.get("domains"));
        biblioObject.setDoi((String) temp.get("doi"));
        biblioObject.setIsFulltextAppended((boolean) temp.get("isFulltextAppended"));
        biblioObject.setIsWithFulltext((boolean) temp.get("isWithFulltext"));
        biblioObject.setIsProcessedByPub2TEI((boolean) temp.get("isProcessedPub2TEI"));
        biblioObject.setIsMined((boolean) temp.get("isMined"));
        biblioObject.setIsIndexed((boolean) temp.get("isIndexed"));
        biblioObject.setPublicationType((String) temp.get("publicationType"));
        biblioObject.setSource((String) temp.get("source"));
        biblioObject.setRepositoryDocVersion((String) temp.get("repositoryDocVersion"));
        biblioObject.setMetadataURL((String) temp.get("metadataURL"));
        return biblioObject;
    }

    /**
     * Inserts json entry representing annotation result.
     */
    public boolean insertAnnotation(String json, String annotationsCollection) {
        boolean done = false;
        if ((json == null) || (json.length() == 0)) {
            // there is nothing to insert, so we can assume that we are effectively doing
            // nothing
            return false;
        }
        try {
            DBObject dbObject = (DBObject) JSON.parse(json);
            DBCollection c = null;
            c = db.getCollection(annotationsCollection);
            BasicDBObject index = new BasicDBObject();
            index.put("anhalyticsId", 1);
//        index.put("xml:id", 1);
            c.createIndex(index, "index", true);

            BasicDBObject document = new BasicDBObject();
            document.put("anhalyticsId", dbObject.get("anhalyticsId"));
            c.findAndRemove(document);

            WriteResult result = c.insert(dbObject, WriteConcern.ACKNOWLEDGED);
            if ((result != null) && (result.wasAcknowledged())) {
                done = true;
            } else {
                done = false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
        }
        return done;
    }

    public boolean insertCrossRefMetadata(String currentAnhalyticsId, String currentRepositoryDocId, String crossRefMetadata) {
        boolean done = false;
        try {
            DBCollection c = null;
            c = db.getCollection(MongoCollectionsInterface.CROSSREF_METADATAS);
            BasicDBObject index = new BasicDBObject();
            index.put("repositoryDocId", 1);
            index.put("anhalyticsId", 1);
            c.createIndex(index, "index", true);
            DBObject dbObject = (DBObject) JSON.parse(crossRefMetadata);
            WriteResult result = c.insert(dbObject, WriteConcern.ACKNOWLEDGED);
            if ((result != null) && (result.wasAcknowledged())) {
                done = true;
            } else {
                done = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return done;
    }

    /**
     * inserts a Arxiv/istex TEI document in the GridFS.
     */
    public boolean insertPDFDocument(InputStream file, String anhalyticsId) throws MongoException, IOException {
//        try {
        GridFS gfs = new GridFS(db, MongoCollectionsInterface.BINARIES);
        GridFSInputFile gfsFile = gfs.createFile(file, true);
        gfs.remove(anhalyticsId + ".pdf");
        gfsFile.setFilename(anhalyticsId + ".pdf");
        gfsFile.put("anhalyticsId", anhalyticsId);
        gfsFile.save();
        file.close();
//        } catch (ParseException e) {
//            logger.error(e.getMessage(), e.getCause());
//        }
        return true;
    }

    /**
     * inserts a Arxiv/istex TEI document in the GridFS.
     */
    public boolean insertTEIcorpus(String TEIcorpus, String anhalyticsId) throws MongoException {
//        try {
        GridFS gfs = new GridFS(db, MongoCollectionsInterface.TEI_CORPUS);
        GridFSInputFile gfsFile = gfs.createFile(new ByteArrayInputStream(TEIcorpus.getBytes()), true);
        gfs.remove(anhalyticsId + ".tei.xml");
        gfsFile.setFilename(anhalyticsId + ".tei.xml");
        gfsFile.put("anhalyticsId", anhalyticsId);
        gfsFile.save();
//        } catch (ParseException e) {
//            logger.error(e.getMessage(), e.getCause());
//        }
        return true;
    }

    /**
     * inserts a Arxiv/istex TEI document in the GridFS.
     */
    public boolean insertGrobidTei(String grobidTei, String anhalyticsId) throws MongoException {
//        try {
        GridFS gfs = new GridFS(db, MongoCollectionsInterface.GROBID_TEIS);
        GridFSInputFile gfsFile = gfs.createFile(new ByteArrayInputStream(grobidTei.getBytes()), true);
        gfs.remove(anhalyticsId + ".tei.xml");
        gfsFile.setFilename(anhalyticsId + ".tei.xml");
        gfsFile.put("anhalyticsId", anhalyticsId);
        gfsFile.save();
//        } catch (ParseException e) {
//            logger.error(e.getMessage(), e.getCause());
//        }
        return true;
    }

    /**
     * inserts a Arxiv/istex TEI document in the GridFS.
     */
    public boolean insertMetadataDocument(String metadata, String anhalyticsId) throws MongoException {
//        try {
        GridFS gfs = new GridFS(db, MongoCollectionsInterface.METADATAS_TEIS);
        GridFSInputFile gfsFile = gfs.createFile(new ByteArrayInputStream(metadata.getBytes()), true);
        gfs.remove(anhalyticsId + ".tei.xml");
        gfsFile.setFilename(anhalyticsId + ".tei.xml");
        gfsFile.put("anhalyticsId", anhalyticsId);
        gfsFile.save();
//        } catch (ParseException e) {
//            logger.error(e.getMessage(), e.getCause());
//        }

        return true;
    }

    /**
     * Inserts publication annex document.
     */
    public boolean insertAnnexDocument(BinaryFile bf, String anhalyticsId) throws MongoException, IOException {
        GridFS gfs = new GridFS(db, MongoCollectionsInterface.PUB_ANNEXES);
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("anhalyticsId", anhalyticsId);
        whereQuery.put("filename", bf.getFileName());
        gfs.remove(whereQuery);
        //version ?
        GridFSInputFile gfsFile = gfs.createFile(bf.getStream(), true);
        gfsFile.setFilename(bf.getFileName());
        gfsFile.put("anhalyticsId", bf.getAnhalyticsId());
        gfsFile.save();
        bf.getStream().close();
        return true;
    }

    public Annotation getNerdAnnotations(String anhalyticsId) {
        return getAnnotations(anhalyticsId, MongoCollectionsInterface.NERD_ANNOTATIONS);
    }

    public Annotation getKeytermAnnotations(String anhalyticsId) {
        return getAnnotations(anhalyticsId, MongoCollectionsInterface.KEYTERM_ANNOTATIONS);
    }

    public Annotation getQuantitiesAnnotations(String anhalyticsId) {
        return getAnnotations(anhalyticsId, MongoCollectionsInterface.PDF_QUANTITIES_ANNOTATIONS);
    }

    /**
     * Returns the annotation for a given repositoryDocId.
     */
    private Annotation getAnnotations(String anhalyticsId, String annotationsCollection) {
        Annotation annotation = null;
        DBCollection c = db.getCollection(annotationsCollection);

//        c.ensureIndex(new BasicDBObject("repositoryDocId", 1));
//        c.ensureIndex(new BasicDBObject("anhalyticsId", 1));
        DBObject result = null;
        BasicDBObject query = new BasicDBObject("anhalyticsId", anhalyticsId);
        DBCursor curs = null;
        try {
            curs = c.find(query);
            if (curs.hasNext()) {
                // we get now the sub-document corresponding to the annotations
                result = curs.next();
                annotation = new Annotation(result.toString(), (String) result.get("repositoryDocId"), (String) result.get("anhalyticsId"), Boolean.parseBoolean((String) result.get("isIndexed")));
                //int ind = annotationsCollection.indexOf("_"); // TBR: this is dirty !
                //BasicDBList annot = (BasicDBList) annotations.get(annotationsCollection.substring(0, ind));
//                if (annotations != null) {
//                    result = annotations.toString();
//                }
            } else {
                logger.warn("The annotations for doc " + anhalyticsId + " was not found in " + annotationsCollection);
            }
        } finally {
            if (curs != null) {
                curs.close();
            }
        }
        return annotation;
    }

    /**
     * Saves the issue occurring while processing.
     */
    public void save(String repositoryDocId, String process, String desc) {
        try {
            DBCollection collection = db.getCollection(HARVEST_DIAGNOSTIC);

            BasicDBObject document = new BasicDBObject();
            document.put("repositoryDocId", repositoryDocId);
            document.put("process", process);

            collection.findAndRemove(document);

            BasicDBObject index = new BasicDBObject();
            index.put("repositoryDocId", 1);
            index.put("process", 1);
            collection.createIndex(index, "index", true);
            document.put("desc", desc);
//            if (date == null) {
//                date = Utilities.formatDate(new Date());
//            }
//            document.put("date", date);
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
            collection.createIndex(index, "index", true);
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
            document.put("embragoDate", date);
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

    //    public void setGridFSCollection(String collectionName) {
//        if (gfs == null || !gfs.getBucketName().equals(collectionName)) {
//            gfs = new GridFS(db, collectionName);
//
//            gfs.getDB().getCollection(collectionName + ".files")
//                    .createIndex(new BasicDBObject("uploadDate", 1).append("isWithFulltext", 1));
//
//            gfs.getDB().getCollection(collectionName + ".files")
//                    .createIndex(new BasicDBObject("uploadDate", 1));
//
//        }
//    }
    /*

     */
    public boolean isProcessed(Processings processing) {
        Object isProcessed = temp.get(processing.getName());
        if (isProcessed == null) {
            return false;
        } else {
            return (boolean) isProcessed;
        }
    }
}
