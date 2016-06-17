package fr.inria.anhalytics.kb.entities;

import fr.inria.anhalytics.commons.utilities.Utilities;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author azhar
 */
public class Organisation_Name {

    private String name = "";
    private Date publication_date;

     public Organisation_Name(){};
    public Organisation_Name(String name, Date publication_date) {
        this.name = name;
        this.publication_date = publication_date;
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
     * @return the publication_date
     */
    public Date getPublication_date() {
        return publication_date;
    }

    /**
     * @param publication_date the publication_date to set
     */
    public void setPublication_date(Date publication_date) {
        this.publication_date = publication_date;
    }
    
    public Map<String, Object> getOrganisationNameDocument() {
        Map<String, Object> organisationNameDocument = new HashMap<String, Object>();
        organisationNameDocument.put("date", Utilities.formatDate(this.getPublication_date()));
        organisationNameDocument.put("name", this.getName());
        return organisationNameDocument;

    }
}
