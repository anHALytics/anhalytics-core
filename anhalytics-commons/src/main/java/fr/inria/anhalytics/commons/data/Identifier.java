package fr.inria.anhalytics.commons.data;

/**
 *
 * @author azhar
 */
public class Identifier {

    private String doi;
    private String repositoryDocId;
    private String anhalyticsId;

    public Identifier(String doi, String repositoryDocId, String anhalyticsId) {
        this.doi = doi;
        this.repositoryDocId = repositoryDocId;
        this.anhalyticsId = anhalyticsId;
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
}
