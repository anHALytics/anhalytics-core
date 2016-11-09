package fr.inria.anhalytics.commons.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author azhar
 */
public class Affiliation {
    private Long affiliationId;
    private List<Organisation> organisations;
    private Person person;
    private Date from_date;
    private Date until_date;
    
    public Affiliation(){}
    public Affiliation(Long affiliationId ,List<Organisation> organisations ,Person person ,Date from_date ,Date until_date){ 
        this.affiliationId = affiliationId;
        this.organisations = organisations;
        this.person = person;
        this.from_date = from_date;
        this.until_date = until_date;
    }

    /**
     * @return the affiliationId
     */
    public Long getAffiliationId() {
        return affiliationId;
    }

    /**
     * @param affiliationId the affiliationId to set
     */
    public void setAffiliationId(Long affiliationId) {
        this.affiliationId = affiliationId;
    }

    /**
     * @return the organisation
     */
    public List<Organisation> getOrganisations() {
        return organisations;
    }

    /**
     * @param organisation the organisation to set
     */
    public void setOrganisation(List<Organisation> organisations) {
        this.organisations = organisations;
    }
    
    /**
     * @param organisation the organisation to set
     */
    public void addOrganisation(Organisation organisation) {
        if(this.organisations == null)
            this.organisations = new ArrayList<Organisation>();
        this.organisations.add(organisation);
    }

    /**
     * @return the person
     */
    public Person getPerson() {
        return person;
    }

    /**
     * @param person the person to set
     */
    public void setPerson(Person person) {
        this.person = person;
    }

    /**
     * @return the from_date
     */
    public Date getFrom_date() {
        return from_date;
    }

    /**
     * @param from_date the from_date to set
     */
    public void setFrom_date(Date from_date) {
        this.from_date = from_date;
    }

    /**
     * @return the until_date
     */
    public Date getUntil_date() {
        return until_date;
    }

    /**
     * @param until_date the until_date to set
     */
    public void setUntil_date(Date until_date) {
        this.until_date = until_date;
    }
}
