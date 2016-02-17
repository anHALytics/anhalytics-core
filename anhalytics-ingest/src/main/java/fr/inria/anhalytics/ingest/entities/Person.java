package fr.inria.anhalytics.ingest.entities;

import java.util.List;

/**
 *
 * @author azhar
 */
public class Person {
    private Long personId;
    private String title = "";
    private String photo= "";
    private String fullname = "";
    private String forename = "";
    private String middlename = "";
    private String surname = "";
    private String url = "";
    private String email = "";
    private List<Person_Identifier> person_identifiers = null;
    
    public Person(){}
    public Person(Long personId, String title, String photo, String fullname, String forename, String middlename, String surname, String url, String email, List<Person_Identifier> person_identifiers){
        this.personId = personId;
        this.title = title;
        this.photo = photo;
        this.fullname = fullname;
        this.forename = forename;
        this.middlename = middlename;
        this.surname = surname;
        this.url = url;
        this.email = email;
        this.person_identifiers = person_identifiers;
    }

    /**
     * @return the personId
     */
    public Long getPersonId() {
        return personId;
    }

    /**
     * @param personId the personId to set
     */
    public void setPersonId(Long personId) {
        this.personId = personId;
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
     * @return the photo
     */
    public String getPhoto() {
        return photo;
    }

    /**
     * @param photo the photo to set
     */
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    /**
     * @return the fullname
     */
    public String getFullname() {
        return fullname;
    }

    /**
     * @param fullname the fullname to set
     */
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    /**
     * @return the forename
     */
    public String getForename() {
        return forename;
    }

    /**
     * @param forename the forename to set
     */
    public void setForename(String forename) {
        this.forename = forename;
    }

    /**
     * @return the middlename
     */
    public String getMiddlename() {
        return middlename;
    }

    /**
     * @param middlename the middlename to set
     */
    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    /**
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
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
     * @return the person_identifiers
     */
    public List<Person_Identifier> getPerson_identifiers() {
        return person_identifiers;
    }

    /**
     * @param person_identifiers the person_identifiers to set
     */
    public void setPerson_identifiers(List<Person_Identifier> person_identifiers) {
        this.person_identifiers = person_identifiers;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
