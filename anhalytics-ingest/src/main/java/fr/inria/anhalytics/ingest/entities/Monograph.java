package fr.inria.anhalytics.ingest.entities;

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
        this.shortname = shortname;
    }
}
