package fr.inria.anhalytics.commons.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe for storing publication TEI metadata extracted from OAI-PMH records.
 * 
 * @author Achraf
 */
public class TEIFile extends File {
    
    public TEIFile(){}
    public TEIFile(String source, String repositoryDocId, BinaryFile pdfdocument, List<BinaryFile> annexes, String doi, String documentType, String teiString, String repositoryDocVersion, String anhalyticsId){
        super(source, repositoryDocId, repositoryDocId, repositoryDocVersion, anhalyticsId, documentType, "text/xml", repositoryDocId+".tei.xml", doi);
        this.tei = teiString;
        this.pdfdocument = pdfdocument;
        this.annexes = annexes;
    }

    private String tei;
    private BinaryFile pdfdocument;
    private List<BinaryFile> annexes;
    private List<String> subjects;


    /**
     * @return the tei
     */
    public String getTei() {
        return tei;
    }

    /**
     * @param tei the tei to set
     */
    public void setTei(String tei) {
        this.tei = tei;
    }

    /**
     * @return the subjects
     */
    public List<String> getSubjects() {
        return subjects;
    }

    /**
     * @param subjects the subjects to set
     */
    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }
    
    public void addSubject(String subject){
        if(subjects == null)
            subjects = new ArrayList<String>();
        subjects.add(subject);
    }

    /**
     * @return the file
     */
    public BinaryFile getPdfdocument() {
        return pdfdocument;
    }

    /**
     * @param file the file to set
     */
    public void setPdfdocument(BinaryFile pdfdocument) {
        this.pdfdocument = pdfdocument;
    }

    /**
     * @return the annexes
     */
    public List<BinaryFile> getAnnexes() {
        return annexes;
    }

    /**
     * @param annexes the annexes to set
     */
    public void setAnnexes(List<BinaryFile> annexes) {
        this.annexes = annexes;
    }
}
