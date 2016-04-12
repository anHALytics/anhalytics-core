package fr.inria.anhalytics.ingest.entities;

/**
 *
 * @author azhar
 */
public class Conference {

    private Long confID;
    private String title = "";

    public Conference() {
    }

    public Conference(Long confID, String title) {
        this.confID = confID;
        this.title = title;
    }

    /**
     * @return the confID
     */
    public Long getConfID() {
        return confID;
    }

    /**
     * @param confID the confID to set
     */
    public void setConfID(Long confID) {
        this.confID = confID;
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

}
