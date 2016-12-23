package fr.inria.anhalytics.commons.data;

import java.io.InputStream;

/**
 *
 * @author azhar
 * Represents the file attached to the tei.
 */
public class BinaryFile extends File {
    
    //date the file is available
    private String embargoDate;
    //is it an annex or the main file
    private boolean isAnnexFile;
    private InputStream stream = null;
    
    public BinaryFile(){};
    public BinaryFile(String source, String url, String repositoryDocId, String doi, String documentType, String fileType, String fileName, String repositoryDocVersion, String anhalyticsId, String embargoDate){
        super(source, url, repositoryDocId, repositoryDocVersion, anhalyticsId, documentType, fileType, fileName, doi);
        this.embargoDate = embargoDate;
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
     * @return the isAnnexFile
     */
    public boolean isIsAnnexFile() {
        return isAnnexFile;
    }

    /**
     * @param isAnnexFile the isAnnexFile to set
     */
    public void setIsAnnexFile(boolean isAnnexFile) {
        this.isAnnexFile = isAnnexFile;
    }

    /**
     * @return the stream
     */
    public InputStream getStream() {
        return stream;
    }

    /**
     * @param stream the stream to set
     */
    public void setStream(InputStream stream) {
        this.stream = stream;
    }
}
