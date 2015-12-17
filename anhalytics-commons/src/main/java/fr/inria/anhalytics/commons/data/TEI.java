package fr.inria.anhalytics.commons.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe for storing publication TEI metadata extracted from OAI-PMH records.
 * 
 * @author Achraf
 */
public class TEI {
    
    public TEI(){}
    public TEI(String id, PublicationFile pdfdocument, List<PublicationFile> annexes, String doi, String documentType, String tei, String ref){
        this.id = id;
        this.tei = tei;
        this.documentType = documentType;
        this.pdfdocument = pdfdocument;
        this.annexes = annexes;
        this.doi = doi;
        this.ref = ref;
    }
    
    private String id;
    private String doi;
    private String tei;
    private String documentType;
    private PublicationFile pdfdocument;
    private List<PublicationFile> annexes;
    private String ref;
    private List<String> subjects;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

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
     * @return the ref
     */
    public String getRef() {
        return ref;
    }

    /**
     * @param ref the ref to set
     */
    public void setRef(String ref) {
        this.ref = ref;
    }
    
    public void addSubject(String subject){
        if(subjects == null)
            subjects = new ArrayList<String>();
        subjects.add(subject);
    }

    /**
     * @return the file
     */
    public PublicationFile getPdfdocument() {
        return pdfdocument;
    }

    /**
     * @param file the file to set
     */
    public void setPdfdocument(PublicationFile pdfdocument) {
        this.pdfdocument = pdfdocument;
    }

    /**
     * @return the annexes
     */
    public List<PublicationFile> getAnnexes() {
        return annexes;
    }

    /**
     * @param annexes the annexes to set
     */
    public void setAnnexes(List<PublicationFile> annexes) {
        this.annexes = annexes;
    }
}
