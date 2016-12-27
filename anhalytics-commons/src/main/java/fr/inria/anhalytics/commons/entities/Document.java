package fr.inria.anhalytics.commons.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author azhar
 */
public class Document {

    private String docID;
    private String version = "";
    private List<Document_Identifier> document_Identifiers = null;

    public Document(String docID, String version, List<Document_Identifier> document_Identifiers) {
        this.docID = docID;
        this.version = version;
        this.document_Identifiers = document_Identifiers;
    }

    public Document() {
    }

    /**
     * @return the docID
     */
    public String getDocID() {
        return docID;
    }

    /**
     * @param docID the docID to set
     */
    public void setDocID(String docID) {
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
        return String.format("User[docID=%d,version=%s]",
                docID, version);
    }

    public Map<String, Object> getDocumentDocument() {
        Map<String, Object> documentDocument = new HashMap<String, Object>();
        documentDocument.put("docID", this.getDocID());
        documentDocument.put("version", this.getVersion());
        
        List<Map<String, Object>> identifiers = new ArrayList<Map<String, Object>>();
        for (Document_Identifier di : this.getDocument_Identifiers()) {
            Map<String, Object> identifier = new HashMap<String, Object>();
            identifier.put("id", di.getId());
            identifier.put("type", di.getType());
            identifiers.add(identifier);
        }
        documentDocument.put("identifiers", identifiers);
        
        return documentDocument;
    }
    
    
    /**
     * @return the document_Identifiers
     */
    public void addDocument_Identifier(Document_Identifier di) {
        if (this.document_Identifiers == null) {
            this.document_Identifiers = new ArrayList<Document_Identifier>();
        }
        document_Identifiers.add(di);
    }

    /**
     * @return the document_Identifiers
     */
    public List<Document_Identifier> getDocument_Identifiers() {
        if (this.document_Identifiers == null) {
            this.document_Identifiers = new ArrayList<Document_Identifier>();
        }
        return document_Identifiers;
    }

    /**
     * @param document_Identifiers the document_Identifiers to set
     */
    public void setDocument_Identifiers(List<Document_Identifier> document_Identifiers) {
        this.document_Identifiers = document_Identifiers;
    }
}
