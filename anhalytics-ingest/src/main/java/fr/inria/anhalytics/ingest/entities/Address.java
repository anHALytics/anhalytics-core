package fr.inria.anhalytics.ingest.entities;

/**
 *
 * @author azhar
 */
public class Address {

    private Long addressId;
    private String addrLine = "";
    private String postBox = "";
    private String postCode = "";
    private String Settlement = "";
    private String region = "";
    private Country country;
    
    private String countryStr="";

    public Address() {
    }

    public Address(Long addressId, String addrLine, String postBox, String postCode, String Settlement, String region, String countryStr, Country country) {
        this.addressId = addressId;
        this.addrLine = addrLine;
        this.postBox = postBox;
        this.postCode = postCode;
        this.Settlement = Settlement;
        this.region = region;
        this.country = country;
        this.countryStr = countryStr;
    }

    /**
     * @return the addressId
     */
    public Long getAddressId() {
        return addressId;
    }

    /**
     * @param addressId the addressId to set
     */
    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    /**
     * @return the addrLine
     */
    public String getAddrLine() {
        return addrLine;
    }

    /**
     * @param addrLine the addrLine to set
     */
    public void setAddrLine(String addrLine) {
        this.addrLine = addrLine;
    }

    /**
     * @return the postBox
     */
    public String getPostBox() {
        return postBox;
    }

    /**
     * @param postBox the postBox to set
     */
    public void setPostBox(String postBox) {
        this.postBox = postBox;
    }

    /**
     * @return the postCode
     */
    public String getPostCode() {
        return postCode;
    }

    /**
     * @param postCode the postCode to set
     */
    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    /**
     * @return the Settlement
     */
    public String getSettlement() {
        return Settlement;
    }

    /**
     * @param Settlement the Settlement to set
     */
    public void setSettlement(String Settlement) {
        this.Settlement = Settlement;
    }

    /**
     * @return the region
     */
    public String getRegion() {
        return region;
    }

    /**
     * @param region the region to set
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * @return the country
     */
    public Country getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(Country country) {
        this.country = country;
    }

    /**
     * @return the countryStr
     */
    public String getCountryStr() {
        return countryStr;
    }

    /**
     * @param countryStr the countryStr to set
     */
    public void setCountryStr(String countryStr) {
        this.countryStr = countryStr;
    }
}
