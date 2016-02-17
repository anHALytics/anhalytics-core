package fr.inria.anhalytics.ingest.entities;

/**
 *
 * @author azhar
 */
public class Publisher {

    private Long publisherID;
    private String name = "";

    public Publisher() {
    }

    public Publisher(Long publisherID, String name) {
        this.publisherID = publisherID;
        this.name = name;
    }

    /**
     * @return the publisherID
     */
    public Long getPublisherID() {
        return publisherID;
    }

    /**
     * @param publisherID the publisherID to set
     */
    public void setPublisherID(Long publisherID) {
        this.publisherID = publisherID;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
