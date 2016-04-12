/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.anhalytics.ingest.entities;

import java.util.Date;

/**
 *
 * @author achraf
 */
public class PART_OF {
    private Organisation organisation_mother;
    private Date beginDate;
    private Date endDate;
    
    public PART_OF(){}
    public PART_OF(Organisation organisation_mother, Date beginDate, Date endDate){
        this.organisation_mother = organisation_mother;
        this.beginDate = beginDate;
        this.endDate = endDate;
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
     * @return the beginDate
     */
    public Date getBeginDate() {
        return beginDate;
    }

    /**
     * @param beginDate the beginDate to set
     */
    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
