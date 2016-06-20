package fr.inria.anhalytics.kb.entities;

import java.util.HashMap;
import java.util.Map;

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
        if(name.length() > 150)
            name = name.substring(0, 149);
        this.name = name;
    }
    
    public Map<String, Object> getPublisherDocument() {
    Map<String, Object> publisherDocument = new HashMap<String, Object>();
    publisherDocument.put("publisherID", this.getPublisherID());
    publisherDocument.put("name", this.getName());
    return publisherDocument;
    
    }
    
}
