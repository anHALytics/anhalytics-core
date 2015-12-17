package fr.inria.anhalytics.commons.data;

/**
 * Represents publication attached file.
 */
public class PublicationFile {
    //the link of the resource
    private String url;
    //date the file is available
    private String embargoDate;
    //is it an annex or the main file
    private boolean isAnnexFile;

    public PublicationFile(String url, String embargoDate, boolean isAnnexFile) {
        this.url = url;
        this.embargoDate = embargoDate;
        this.isAnnexFile = isAnnexFile;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the embargoDate
     */
    public String getEmbargoDate() {
        return embargoDate;
    }

    /**
     * @param embargoDate the embargoDate to set
     */
    public void setEmbargoDate(String embargoDate) {
        this.embargoDate = embargoDate;
    }

    /**
     * @return the type
     */
    public boolean isAnnexFile() {
        return isAnnexFile;
    }

    /**
     * @param type the type to set
     */
    public void setIsAnnexFile(boolean isAnnexFile) {
        this.isAnnexFile = isAnnexFile;
    }

}
