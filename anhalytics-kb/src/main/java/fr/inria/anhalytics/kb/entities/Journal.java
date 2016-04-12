package fr.inria.anhalytics.ingest.entities;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author azhar
 */
public class Journal {

    private Long journalID;

    private String title = "";

    public Journal() {
    }

    public Journal(Long journalID, String title) {
        this.journalID = journalID;
        this.title = title;
    }

    /**
     * @return the journalID
     */
    public Long getJournalID() {
        return journalID;
    }

    /**
     * @param journalID the journalID to set
     */
    public void setJournalID(Long journalID) {
        this.journalID = journalID;
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

    public Map<String, Object> getJournalDocument() {
        Map<String, Object> journalDocument = new HashMap<String, Object>();
        journalDocument.put("journalID", this.getJournalID());
        journalDocument.put("title", this.getTitle());
        return journalDocument;
    }

}
