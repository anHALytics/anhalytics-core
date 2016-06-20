package fr.inria.anhalytics.kb.entities;

import fr.inria.anhalytics.commons.utilities.Utilities;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author azhar
 */
public class Publication {

    private Long publicationID;
    private Document document;
    private Monograph monograph;
    private Publisher publisher;
    private String type = "";
    private String doc_title = "";
    private Date date_printed;
    private String date_eletronic = "";
    private String start_page = "";
    private String end_page = "";
    private String language = "";

    public Publication(){}
    public Publication(Long publicationID, Document document, Monograph monograph, Publisher publisher, String type, String doc_title,
            Date date_printed, String date_eletronic, String start_page, String end_page, String language) {
        this.publicationID = publicationID;
        this.document = document;
        this.monograph = monograph;
        this.publisher = publisher;
        this.type = type;
        this.doc_title = doc_title;
        this.date_printed = date_printed;
        this.date_eletronic = date_eletronic;
        this.start_page = start_page;
        this.end_page = end_page;
        this.language = language;
    }

    /**
     * @return the publicationID
     */
    public Long getPublicationID() {
        return publicationID;
    }

    /**
     * @param publicationID the publicationID to set
     */
    public void setPublicationID(Long publicationID) {
        this.publicationID = publicationID;
    }

    /**
     * @return the document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * @param document the document to set
     */
    public void setDocument(Document document) {
        this.document = document;
    }

    /**
     * @return the monograph
     */
    public Monograph getMonograph() {
        return monograph;
    }

    /**
     * @param monograph the monograph to set
     */
    public void setMonograph(Monograph monograph) {
        this.monograph = monograph;
    }

    /**
     * @return the publisher
     */
    public Publisher getPublisher() {
        if(publisher == null)
            publisher = new Publisher();
        return publisher;
    }

    /**
     * @param publisher the publisher to set
     */
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
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
        if(type.length() > 45)
            type = type.substring(0, 44);
        this.type = type;
    }

    /**
     * @return the doc_title
     */
    public String getDoc_title() {
        return doc_title;
    }

    /**
     * @param doc_title the doc_title to set
     */
    public void setDoc_title(String doc_title) {
        this.doc_title = doc_title;
    }

    /**
     * @return the date_printed
     */
    public Date getDate_printed() {
        return date_printed;
    }

    /**
     * @param date_printed the date_printed to set
     */
    public void setDate_printed(Date date_printed) {
        this.date_printed = date_printed;
    }

    /**
     * @return the date_eletronic
     */
    public String getDate_eletronic() {
        return date_eletronic;
    }

    /**
     * @param date_eletronic the date_eletronic to set
     */
    public void setDate_eletronic(String date_eletronic) {
        if(date_eletronic.length() > 45)
            date_eletronic = date_eletronic.substring(0, 44);
        this.date_eletronic = date_eletronic;
    }

    /**
     * @return the start_page
     */
    public String getStart_page() {
        return start_page;
    }

    /**
     * @param start_page the start_page to set
     */
    public void setStart_page(String start_page) {
        if(start_page.length() > 45)
            start_page = start_page.substring(0, 44);
        this.start_page = start_page;
    }

    /**
     * @return the end_page
     */
    public String getEnd_page() {
        return end_page;
    }

    /**
     * @param end_page the end_page to set
     */
    public void setEnd_page(String end_page) {
        if(end_page.length() > 45)
            end_page = end_page.substring(0, 44);
        this.end_page = end_page;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language) {
        if(language.length() > 45)
            language = language.substring(0, 44);
        this.language = language;
    }
    
    public Map<String, Object> getPublicationDocument() {
        Map<String, Object> publicationDocument = new HashMap<String, Object>();
        publicationDocument.put("publicationID", this.getPublicationID());
        publicationDocument.put("type", this.getType());
        publicationDocument.put("doc_title", this.getDoc_title());
        publicationDocument.put("date_printed", Utilities.formatDate(this.getDate_printed()));
        publicationDocument.put("date_electronic", this.getDate_eletronic());
        publicationDocument.put("start_page", this.getStart_page());
        publicationDocument.put("end_page", this.getEnd_page());
        publicationDocument.put("publisher", this.getPublisher().getPublisherDocument());
        publicationDocument.put("monograph", this.getMonograph().getMonographDocument());
        return publicationDocument;
    }
}
