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
    private Date begin_date;
    private Date end_date;
    
    public Affiliation(){}
    public Affiliation(Long affiliationId ,List<Organisation> organisations ,Person person ,Date begin_date ,Date end_date){ 
        this.affiliationId = affiliationId;
        this.organisations = organisations;
        this.person = person;
        this.begin_date = begin_date;
        this.end_date = end_date;
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
     * @return the begin_date
     */
    public Date getBegin_date() {
        return begin_date;
    }

    /**
     * @param begin_date the begin_date to set
     */
    public void setBegin_date(Date begin_date) {
        this.begin_date = begin_date;
    }

    /**
     * @return the end_date
     */
    public Date getEnd_date() {
        return end_date;
    }

    /**
     * @param end_date the end_date to set
     */
    public void setEnd_date(Date end_date) {
        this.end_date = end_date;
    }
}
