package fr.inria.anhalytics.commons.entities;

import fr.inria.anhalytics.commons.utilities.Utilities;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author azhar
 */
public class Organisation_Name {

    private Long organisation_nameid;
    private String name = "";
    private Date lastupdate_date;

     public Organisation_Name(){};
    public Organisation_Name(Long organisation_nameid, String name, Date lastupdate_date) {
        this.organisation_nameid = organisation_nameid;
        this.name = name;
        this.lastupdate_date = lastupdate_date;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
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
    public void setLastupdate_date(Date publication_date) {
        this.lastupdate_date = publication_date;
    }
    
    public Map<String, Object> getOrganisationNameDocument() {
        Map<String, Object> organisationNameDocument = new HashMap<String, Object>();
        organisationNameDocument.put("date", Utilities.formatDate(this.getLastupdate_date()));
        organisationNameDocument.put("name", this.getName());
        return organisationNameDocument;

    }
    
    @Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;

        if (object != null && object instanceof Organisation_Name)
        {
            sameSame = this.name.equals(((Organisation_Name) object).name);
        }
        return sameSame;
    }

    /**
     * @return the organisation_nameid
     */
    public Long getOrganisation_nameid() {
        return organisation_nameid;
    }

    /**
     * @param organisation_nameid the organisation_nameid to set
     */
    public void setOrganisation_nameid(Long organisation_nameid) {
        this.organisation_nameid = organisation_nameid;
    }
}
