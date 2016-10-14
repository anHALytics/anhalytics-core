package fr.inria.anhalytics.commons.entities;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author azhar
 */
public class Monograph {

    private Long monographID;
    private String type = "";
    private String title = "";
    private String shortname = "";

    public Monograph() {
    }

    public Monograph(Long monographID, String type, String title, String shortname) {
        this.monographID = monographID;
        this.type = type;
        this.title = title;
        this.shortname = shortname;
    }

    /**
     * @return the monographID
     */
    public Long getMonographID() {
        return monographID;
    }

    /**
     * @param monographID the monographID to set
     */
    public void setMonographID(Long monographID) {
        this.monographID = monographID;
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
        if(type.length() > 45)
            type = type.substring(0, 44);
        this.type = type;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the shortname
     */
    public String getShortname() {
        return shortname;
    }

    /**
     * @param shortname the shortname to set
     */
    public void setShortname(String shortname) {
        if(shortname.length() > 45)
            shortname = shortname.substring(0, 44);
        this.shortname = shortname;
    }

    public Map<String, Object> getMonographDocument() {
        Map<String, Object> monographDocument = new HashMap<String, Object>();
        monographDocument.put("monographID", this.getMonographID());
        monographDocument.put("title", this.getTitle());
        monographDocument.put("type", this.getType());
        monographDocument.put("shortname", this.getShortname());
        return monographDocument;

    }
}
