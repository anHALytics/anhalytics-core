package fr.inria.anhalytics.kb.entities;

/**
 *
 * @author azhar
 */
public class Serial_Identifier {
    private Long serial_IdentifierID;
    private String id;
    private String type;
    private Journal journal;
    private Collection collection;
    
    
    public Serial_Identifier(){}
    
    public Serial_Identifier(Long serial_IdentifierID, String id, String type, Journal journal, Collection collection){
        this.serial_IdentifierID = serial_IdentifierID;
        this.id = id;
    }

    /**
     * @return the serial_IdentifierID
     */
    public Long getSerial_IdentifierID() {
        return serial_IdentifierID;
    }

    /**
     * @param serial_IdentifierID the serial_IdentifierID to set
     */
    public void setSerial_IdentifierID(Long serial_IdentifierID) {
        this.serial_IdentifierID = serial_IdentifierID;
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
        if(id.length() > 45)
            this.id = id.substring(0, 44);
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
     * @return the journal
     */
    public Journal getJournal() {
        return journal;
    }

    /**
     * @param journal the journal to set
     */
    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    /**
     * @return the collection
     */
    public Collection getCollection() {
        return collection;
    }

    /**
     * @param collection the collection to set
     */
    public void setCollection(Collection collection) {
        this.collection = collection;
    }
}
