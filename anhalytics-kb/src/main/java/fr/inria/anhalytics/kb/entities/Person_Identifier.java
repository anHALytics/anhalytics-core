package fr.inria.anhalytics.kb.entities;

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
        if (id.length() > 150) {
            id = id.substring(0, 149);
        }
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
        if (type.length() > 45) {
            type = type.substring(0, 44);
        }
        this.type = type;
    }
}
