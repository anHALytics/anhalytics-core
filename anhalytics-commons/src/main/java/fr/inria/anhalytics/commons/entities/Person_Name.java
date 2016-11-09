package fr.inria.anhalytics.commons.entities;

import java.util.Date;

/**
 *
 * @author azhar
 */
public class Person_Name {

    private Long personNameId;
    private Long personId;
    private String title = "";
    private String fullname = "";
    private String forename = "";
    private String middlename = "";
    private String surname = "";
    private Date lastupdate_date;

    
    public Person_Name() {
    }
    public Person_Name(Long personNameId, Long personId, String fullname, String forename, String middlename, String surname, String title, Date lastupdate_date) {
        this.personNameId = personNameId;
        this.personId = personId;
        this.title = title;
        this.fullname = fullname;
        this.forename = forename;
        this.middlename = middlename;
        this.surname = surname;
        this.lastupdate_date = lastupdate_date;
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
     * @return the fullname
     */
    public String getFullname() {
        return fullname;
    }

    /**
     * @param fullname the fullname to set
     */
    public void setFullname(String fullname) {
        if(fullname.length() > 150)
            fullname = fullname.substring(0, 149);
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
        if(forename.length() > 150)
            forename = forename.substring(0, 149);
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
        if(middlename.length() > 45)
            middlename = middlename.substring(0, 44);
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
        if(surname.length() > 150)
            surname = surname.substring(0, 149);
        this.surname = surname;
    }

    /**
     * @return the personNameId
     */
    public Long getPersonNameId() {
        return personNameId;
    }

    /**
     * @param personNameId the personNameId to set
     */
    public void setPersonNameId(Long personNameId) {
        this.personNameId = personNameId;
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
     * @return the lastupdate_date
     */
    public Date getLastupdate_date() {
        return lastupdate_date;
    }

    /**
     * @param lastupdate_date the lastupdate_date to set
     */
    public void setLastupdate_date(Date lastupdate_date) {
        this.lastupdate_date = lastupdate_date;
    }
    
    @Override
public boolean equals(Object object)
{
    boolean isEqual= false;

    if (object != null && object instanceof Person_Name)
    {
        isEqual = (this.fullname.equals(((Person_Name) object).fullname)) ;
    }

    return isEqual;
}

}
