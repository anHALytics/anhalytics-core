package fr.inria.anhalytics.ingest.entities;

/**
 *
 * @author azhar
 */
public class Document_Identifier {

    private Long doc_identifierID;
    private String id = "";
    private String type = "";
    private Document doc = null;

    public Document_Identifier(Long doc_identifierID, String id, String type, Document doc) {
        this.doc_identifierID = doc_identifierID;
        this.id = id;
        this.type = type;
        this.doc = doc;
    }
    
    public Document_Identifier(){}

    /**
     * @return the doc_identifierID
     */
    public Long getDoc_identifierID() {
        return doc_identifierID;
    }

    /**
     * @param doc_identifierID the doc_identifierID to set
     */
    public void setDoc_identifierID(Long doc_identifierID) {
        this.doc_identifierID = doc_identifierID;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the doc
     */
    public Document getDoc() {
        return doc;
    }

    /**
     * @param doc the doc to set
     */
    public void setDoc(Document doc) {
        this.doc = doc;
    }

    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
