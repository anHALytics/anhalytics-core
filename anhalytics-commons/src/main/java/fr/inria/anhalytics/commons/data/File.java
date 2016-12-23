package fr.inria.anhalytics.commons.data;

/**
 *
 * @author azhar
 */
public class File {
    private String source;
    //the link of the resource
    private String url;
    private String repositoryDocId;
    private String repositoryDocVersion;
    private String anhalyticsId;
    private String documentType;
    private String fileType;
    private String fileName;
    private String doi;
    
    
    
    public File(){}
    public File(String source, String url, String repositoryDocId, String repositoryDocVersion, String anhalyticsId, String documentType, String fileType, String fileName, String doi){
        this.source = source;
        this.url = url;
        this.repositoryDocId = repositoryDocId;
        this.repositoryDocVersion = repositoryDocVersion;
        this.anhalyticsId = anhalyticsId;
        this.documentType = documentType;
        this.fileType = fileType;
        this.fileName = fileName;
        this.doi = doi;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the repositoryDocId
     */
    public String getRepositoryDocId() {
        return repositoryDocId;
    }

    /**
     * @param repositoryDocId the repositoryDocId to set
     */
    public void setRepositoryDocId(String repositoryDocId) {
        this.repositoryDocId = repositoryDocId;
    }

    /**
     * @return the repositoryDocVersion
     */
    public String getRepositoryDocVersion() {
        return repositoryDocVersion;
    }

    /**
     * @param repositoryDocVersion the repositoryDocVersion to set
     */
    public void setRepositoryDocVersion(String repositoryDocVersion) {
        this.repositoryDocVersion = repositoryDocVersion;
    }

    /**
     * @return the anhalyticsId
     */
    public String getAnhalyticsId() {
        return anhalyticsId;
    }

    /**
     * @param anhalyticsId the anhalyticsId to set
     */
    public void setAnhalyticsId(String anhalyticsId) {
        this.anhalyticsId = anhalyticsId;
    }

    /**
     * @return the documentType
     */
    public String getDocumentType() {
        return documentType;
    }

    /**
     * @param documentType the documentType to set
     */
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    /**
     * @return the fileType
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * @param fileType the fileType to set
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

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


    
}
