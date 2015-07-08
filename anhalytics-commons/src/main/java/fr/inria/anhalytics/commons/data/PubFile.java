package fr.inria.anhalytics.commons.data;

/**
 * Represents a publication file (in order to keep important informations
 * (embargo date, type..))
 */
public class PubFile {

    private String url;
    private String embargoDate;
    private String type;

    public PubFile(String url, String embargoDate, String type) {
        this.url = url;
        this.embargoDate = embargoDate;
        this.type = type;
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
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

}
