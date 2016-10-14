package fr.inria.anhalytics.commons.entities;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author azhar
 */
public class Collection {

    private Long collectionID;
    private String title = "";

    public Collection() {
    }

    public Collection(Long collectionID, String title) {
        this.collectionID = collectionID;
        this.title = title;
    }

    /**
     * @return the collectionID
     */
    public Long getCollectionID() {
        return collectionID;
    }

    /**
     * @param collectionID the collectionID to set
     */
    public void setCollectionID(Long collectionID) {
        this.collectionID = collectionID;
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

    public Map<String, Object> getCollectionDocument() {
        Map<String, Object> collectionDocument = new HashMap<String, Object>();
        collectionDocument.put("collectionID", this.getCollectionID());
        collectionDocument.put("title", this.getTitle());
        return collectionDocument;
    }
}
