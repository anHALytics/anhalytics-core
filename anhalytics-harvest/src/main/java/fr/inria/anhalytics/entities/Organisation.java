package fr.inria.anhalytics.entities;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author azhar
 */
public class Organisation {
    private Long organisationId;
    private String type = "";
    private String url = "";
    private String structure = "";
    private List<String> names = null;
    public Organisation(){}
    public Organisation(Long organisationId, String type, String url, String structure, List<String> names){
        this.organisationId = organisationId;
        this.type = type;
        this.url = url;
        this.structure = structure;
        this.names = names;
    }

    /**
     * @return the organisationId
     */
    public Long getOrganisationId() {
        return organisationId;
    }

    /**
     * @param organisationId the organisationId to set
     */
    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the structure
     */
    public String getStructure() {
        return structure;
    }

    /**
     * @param structure the structure to set
     */
    public void setStructure(String structure) {
        this.structure = structure;
    }

    /**
     * @return the name
     */
    public List<String> getNames() {
        if(this.names == null)
            this.names = new ArrayList<String>();
        return names;
    }

    /**
     * @param name the name to set
     */
    public void addName(String name) {
        if(this.names == null)
            this.names = new ArrayList<String>();
        this.names.add(name);
    }
}
