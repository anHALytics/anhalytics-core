package fr.inria.anhalytics.ingest.entities;

/**
 *
 * @author azhar
 */
public class Country {
    private Long countryID;
    private String iso="";
    public Country(){}
    public Country(Long countryID, String iso){
        this.countryID = countryID;
        this.iso = iso;
    }

    /**
     * @return the countryID
     */
    public Long getCountryID() {
        return countryID;
    }

    /**
     * @param countryID the countryID to set
     */
    public void setCountryID(Long countryID) {
        this.countryID = countryID;
    }

    /**
     * @return the iso
     */
    public String getIso() {
        return iso;
    }

    /**
     * @param iso the iso to set
     */
    public void setIso(String iso) {
        this.iso = iso;
    }
}
