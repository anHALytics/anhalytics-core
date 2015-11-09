package fr.inria.anhalytics.entities;

/**
 *
 * @author azhar
 */
public class Journal {
    
    private Long journalID;
    
    private String title = "";
    
    public Journal(){}
    
    public Journal(Long journalID, String title){
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
    
}
