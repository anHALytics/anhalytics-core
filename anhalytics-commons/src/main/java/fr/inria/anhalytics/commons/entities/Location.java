package fr.inria.anhalytics.commons.entities;

import java.util.Date;

/**
 *
 * @author azhar
 */
public class Location {
    private Long locationId;
    private Organisation organisation;
    private Address address;
    private Date from_date;
    private Date until_date;
    public Location(){}
    public Location(Long locationId, Organisation organisation, Address address, Date from_date, Date until_date){
        this.locationId = locationId;
        this.organisation = organisation;
        this.address = address;
        this.from_date = from_date;
        this.until_date = until_date;
    }

    /**
     * @return the locationId
     */
    public Long getLocationId() {
        return locationId;
    }

    /**
     * @param locationId the locationId to set
     */
    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    /**
     * @return the organisation
     */
    public Organisation getOrganisation() {
        return organisation;
    }

    /**
     * @param organisation the organisation to set
     */
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    /**
     * @return the address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(Address address) {
        this.address = address;
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
