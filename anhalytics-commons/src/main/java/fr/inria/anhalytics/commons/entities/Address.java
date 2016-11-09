package fr.inria.anhalytics.commons.entities;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author azhar
 */
public class Address {

    private Long addressId;
    private String addrLine = "";
    private String postBox = "";
    private String postCode = "";
    private String settlement = "";
    private String region = "";
    private Country country;

    public Address() {
    }

    public Address(Long addressId, String addrLine, String postBox, String postCode, String settlement, String region, Country country) {
        this.addressId = addressId;
        this.addrLine = addrLine;
        this.postBox = postBox;
        this.postCode = postCode;
        this.settlement = settlement;
        this.region = region;
        this.country = country;
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
        if (addrLine.length() > 150) {
            addrLine = addrLine.substring(0, 149);
        }
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
        if (postBox.length() > 45) {
            postBox = postBox.substring(0, 44);
        }
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
        if (postCode.length() > 45) {
            postCode = postCode.substring(0, 44);
        }
        this.postCode = postCode;
    }

    /**
     * @return the Settlement
     */
    public String getSettlement() {
        return settlement;
    }

    /**
     * @param Settlement the Settlement to set
     */
    public void setSettlement(String settlement) {
        if (settlement.length() > 45) {
            settlement = settlement.substring(0, 44);
        }
        this.settlement = settlement;
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
        if (region.length() > 45) {
            region = region.substring(0, 44);
        }
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

    public Map<String, Object> getAddressDocument() {
        Map<String, Object> addressDocument = new HashMap<String, Object>();
        addressDocument.put("addressId", this.getAddressId());
        addressDocument.put("addrLine", this.getAddrLine());
        if (this.getCountry() != null) {
            addressDocument.put("country", this.getCountry().getIso());
        } else {
            addressDocument.put("country", "");
        }
        addressDocument.put("postBox", this.getPostBox());
        addressDocument.put("postCode", this.getPostCode());
        addressDocument.put("region", this.getRegion());
        addressDocument.put("settlement", this.getSettlement());
        return addressDocument;

    }
}
