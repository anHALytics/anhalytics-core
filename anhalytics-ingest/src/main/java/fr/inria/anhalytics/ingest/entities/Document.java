package fr.inria.anhalytics.ingest.entities;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author azhar
 */
public class Document {

    private Long docID;
    private String version = "";
    private String tei_metadata = "";
    private String uri = "";

    public Document(Long docID, String version, String tei_metadata, String uri) {
        this.docID = docID;
        this.version = version;
        this.tei_metadata = tei_metadata;
        this.uri = uri;
    }

    public Document() {
    }

    /**
     * @return the docID
     */
    public Long getDocID() {
        return docID;
    }

    /**
     * @param docID the docID to set
     */
    public void setDocID(Long docID) {
        this.docID = docID;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        if(version.length() > 45)
            version = version.substring(0, 44);
        this.version = version;
    }

    /**
     * @return the tei_metadata
     */
    public String getTei_metadata() {
        return tei_metadata;
    }

    /**
     * @param tei_metadata the tei_metadata to set
     */
    public void setTei_metadata(String tei_metadata) {
        this.tei_metadata = tei_metadata;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        if(uri.length() > 45)
            uri = uri.substring(0, 44);
        this.uri = uri;
    }

    /**
     * The user ID is unique for each User. So this should compare User by ID
     * only.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        return (other instanceof Document) && (docID != null)
                ? docID.equals(((Document) other).docID)
                : (other == this);
    }

    /**
     * The user ID is unique for each User. So User with same ID should return
     * same hashcode.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (docID != null)
                ? (this.getClass().hashCode() + docID.hashCode())
                : super.hashCode();
    }

    /**
     * Returns the String representation of this User. Not required, it just
     * pleases reading logs.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("User[docID=%d,version=%s,uri=%s,tei_metadata=%s]",
                docID, version, uri, tei_metadata);
    }

    public Map<String, Object> getDocumentDocument() {
        Map<String, Object> documentDocument = new HashMap<String, Object>();
        documentDocument.put("docID", this.getDocID());
        documentDocument.put("version", this.getVersion());
        //documentDocument.put("tei_metadata", this.getTei_metadata());
        documentDocument.put("uri", this.getUri());
        return documentDocument;
    }
}
