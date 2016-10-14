package fr.inria.anhalytics.commons.entities;

/**
 *
 * @author azhar
 */
public class Organisation_Identifier {

    private String id = "";
    private String type = "";

    public Organisation_Identifier(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public Organisation_Identifier() {
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
