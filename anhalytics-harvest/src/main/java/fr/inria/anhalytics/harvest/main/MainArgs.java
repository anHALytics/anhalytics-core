package fr.inria.anhalytics.harvest.main;

/**
 *
 * @author Achraf
 */
public class MainArgs {
    private String processName;

    private String fromDate;

    private String untilDate;

    private String path2grobidHome;

    private String path2grobidProperty;

    private String oaiUrl;
    
    private String grobidHost;
    private String grobidPort;
    
    private String tmpPath;


    /**
     * @return the processName
     */
    public String getProcessName() {
        return processName;
    }

    /**
     * @param processName the processName to set
     */
    public void setProcessName(String processName) {
        this.processName = processName;
    }

    /**
     * @return the fromDate
     */
    public String getFromDate() {
        return fromDate;
    }

    /**
     * @param fromDate the fromDate to set
     */
    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * @return the untilDate
     */
    public String getUntilDate() {
        return untilDate;
    }

    /**
     * @param untilDate the untilDate to set
     */
    public void setUntilDate(String untilDate) {
        this.untilDate = untilDate;
    }

    /**
     * @return the path2grobidHome
     */
    public String getPath2grobidHome() {
        return path2grobidHome;
    }

    /**
     * @param path2grobidHome the path2grobidHome to set
     */
    public void setPath2grobidHome(String path2grobidHome) {
        this.path2grobidHome = path2grobidHome;
    }

    /**
     * @return the path2grobidProperty
     */
    public String getPath2grobidProperty() {
        return path2grobidProperty;
    }

    /**
     * @param path2grobidProperty the path2grobidProperty to set
     */
    public void setPath2grobidProperty(String path2grobidProperty) {
        this.path2grobidProperty = path2grobidProperty;
    }

    /**
     * @return the oaiUrl
     */
    public String getOaiUrl() {
        return oaiUrl;
    }

    /**
     * @param oaiUrl the oaiUrl to set
     */
    public void setOaiUrl(String oaiUrl) {
        this.oaiUrl = oaiUrl;
    }

    /**
     * @return the grobid_host
     */
    public String getGrobidHost() {
        return grobidHost;
    }

    /**
     * @param grobid_host the grobid_host to set
     */
    public void setGrobidHost(String grobid_host) {
        this.grobidHost = grobid_host;
    }

    /**
     * @return the grobid_port
     */
    public String getGrobidPort() {
        return grobidPort;
    }

    /**
     * @param grobid_port the grobid_port to set
     */
    public void setGrobidPort(String grobid_port) {
        this.grobidPort = grobid_port;
    }

    /**
     * @return the tmpPath
     */
    public String getTmpPath() {
        return tmpPath;
    }

    /**
     * @param tmpPath the tmpPath to set
     */
    public void setTmpPath(String tmpPath) {
        this.tmpPath = tmpPath;
    }

}
