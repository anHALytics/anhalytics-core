package fr.inria.anhalytics.entities;

import java.util.Date;

/**
 *
 * @author azhar
 */
public class Location {
    private Long locationId;
    private Organisation organisation;
    private Address address;
    private Date begin_date;
    private Date end_date;
    public Location(){}
    public Location(Long locationId, Organisation organisation, Address address, Date begin_date, Date end_date){
        this.locationId = locationId;
        this.organisation = organisation;
        this.address = address;
        this.begin_date = begin_date;
        this.end_date = end_date;
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
