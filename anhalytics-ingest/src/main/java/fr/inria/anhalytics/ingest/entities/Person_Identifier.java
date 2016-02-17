package fr.inria.anhalytics.ingest.entities;

/**
 *
 * @author azhar
 */
public class Person_Identifier {
    
    private String id = "";
    private String type = "";

    public Person_Identifier() {
    }

    public Person_Identifier(String id, String type) {
        this.id = id;
        this.type = type;
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
}
