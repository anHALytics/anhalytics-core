/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.anhalytics.commons.entities;

import java.util.Date;

/**
 *
 * @author achraf
 */
public class PART_OF {
    private Organisation organisation_mother;
    private Date fromDate;
    private Date untilDate;
    
    public PART_OF(){}
    public PART_OF(Organisation organisation_mother, Date fromDate, Date untilDate){
        this.organisation_mother = organisation_mother;
        this.fromDate = fromDate;
        this.untilDate = untilDate;
    }

    /**
     * @return the organisation_motherId
     */
    public Organisation getOrganisation_mother() {
        return organisation_mother;
    }

    /**
     * @param organisation_motherId the organisation_motherId to set
     */
    public void setOrganisation_mother(Organisation organisation_mother) {
        this.organisation_mother = organisation_mother;
    }

    /**
     * @return the fromDate
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * @param fromDate the fromDate to set
     */
    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * @return the untilDate
     */
    public Date getUntilDate() {
        return untilDate;
    }

    /**
     * @param untilDate the untilDate to set
     */
    public void setUntilDate(Date untilDate) {
        this.untilDate = untilDate;
    }
}
