package fr.inria.anhalytics.kb.entities;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author azhar
 */
public class Person {

    private Long personId;
    private String title = "";
    private String url = "";
    private String email = "";
    private String photo = "";
    private String phone = "";
    private List<Person_Identifier> person_identifiers = null;
    private List<Person_Name> person_names = null;

    public Person() {
    }

    public Person(Long personId, String title, String photo, String url, String email, String phone, List<Person_Identifier> person_identifiers, List<Person_Name> person_names) {
        this.personId = personId;
        this.person_names = person_names;
        this.photo = photo;
        this.title = title;
        this.url = url;
        this.email = email;
        this.person_identifiers = person_identifiers;
        this.phone = phone;
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
        if(title.length() > 45)
            title = title.substring(0, 44);
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
        if(photo.length() > 45)
            photo = photo.substring(0, 44);
        this.photo = photo;
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
        if(url.length() > 150)
            url = url.substring(0, 149);
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
        if(email.length() > 150)
            email = email.substring(0, 149);
        this.email = email;
    }
    
    

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        if(phone.length() > 45)
            phone = phone.substring(0, 44);
        this.phone = phone;
    }

    public Map<String, Object> getPersonDocument() {
        Map<String, Object> personDocument = new HashMap<String, Object>();
        Map<String, Object> personIdentifierDocument = new HashMap<String, Object>();
        personDocument.put("personId", this.getPersonId());
        personDocument.put("email", this.getEmail());
        personDocument.put("title", this.getTitle());
        personDocument.put("photo", this.getPhoto());
        personDocument.put("phone", this.getPhone());
        personDocument.put("url", this.getUrl());
        for (Person_Identifier pi : this.getPerson_identifiers()) {
            personIdentifierDocument.put(pi.getType(), pi.getId());
        }
        personDocument.put("identifers", personIdentifierDocument);
        return personDocument;
    }

    /**
     * @return the person_names
     */
    public List<Person_Name> getPerson_names() {
        return person_names;
    }

    /**
     * @param person_names the person_names to set
     */
    public void setPerson_names(List<Person_Name> person_names) {
        this.person_names = person_names;
    }
}
