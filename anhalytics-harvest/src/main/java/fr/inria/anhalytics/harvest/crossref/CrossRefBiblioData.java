package fr.inria.anhalytics.harvest.crossref;

/**
 * Represents metadata needed to find DOI.
 *
 * @author azhar
 */
public class CrossRefBiblioData {

    private String doi;
    private String aut;
    private String title;
    private String journalTitle;
    private String volume;
    private String pageRange;
    private int beginPage;

    public CrossRefBiblioData() {
    }

    public CrossRefBiblioData(String doi, String aut, String title, String journalTitle, String volume, String pageRange, int beginPage) {
        this.doi = doi;
        this.aut = aut;
        this.title = title;
        this.journalTitle = journalTitle;
        this.volume = volume;
        this.pageRange = pageRange;
        this.beginPage = beginPage;
    }

    ;
    /**
     * @return the doi
     */
    public String getDoi() {
        return doi;
    }

    /**
     * @param doi the doi to set
     */
    public void setDoi(String doi) {
        this.doi = doi;
    }

    /**
     * @return the aut
     */
    public String getAut() {
        return aut;
    }

    /**
     * @param aut the aut to set
     */
    public void setAut(String aut) {
        this.aut = aut;
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
     * @return the journalTitle
     */
    public String getJournalTitle() {
        return journalTitle;
    }

    /**
     * @param journalTitle the journalTitle to set
     */
    public void setJournalTitle(String journalTitle) {
        this.journalTitle = journalTitle;
    }

    /**
     * @return the volume
     */
    public String getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(String volume) {
        this.volume = volume;
    }

    /**
     * @return the firstPage
     */
    public String getPageRange() {
        return pageRange;
    }

    /**
     * @param pageRange the firstPage to set
     */
    public void setPageRange(String pageRange) {
        this.pageRange = pageRange;
    }

    /**
     * @return the beginPage
     */
    public int getBeginPage() {
        return beginPage;
    }

    /**
     * @param beginPage the beginPage to set
     */
    public void setBeginPage(int beginPage) {
        this.beginPage = beginPage;
    }
}
