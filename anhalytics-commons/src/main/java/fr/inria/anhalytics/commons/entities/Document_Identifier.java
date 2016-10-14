package fr.inria.anhalytics.commons.entities;

/**
 *
 * @author azhar
 */
public class Document_Identifier {

    private String id = "";
    private String type = "";

    public Document_Identifier(String id, String type) {
        this.id = id;
        this.type = type;
    }
    
    public Document_Identifier(){}

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
        if(id.length() > 150)
            id = id.substring(0, 149);
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
        if(type.length() > 55)
            type = type.substring(0, 54);
        this.type = type;
    }

    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
