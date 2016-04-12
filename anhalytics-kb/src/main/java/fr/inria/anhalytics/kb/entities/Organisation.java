package fr.inria.anhalytics.ingest.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<PART_OF> rels = null;

    public Organisation() {
    }

    public Organisation(Long organisationId, String type, String url, String structure, List<String> names, List<PART_OF> rels) {
        this.organisationId = organisationId;
        this.type = type;
        this.url = url;
        this.structure = structure;
        this.names = names;
        this.rels = rels;
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
        if(type.length() > 45)
            type = type.substring(0, 44);
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
        if(url.length() > 255)
            url = url.substring(0, 254);
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
        if(structure.length() > 45)
            structure = structure.substring(0, 44);
        this.structure = structure;
    }

    /**
     * @return the name
     */
    public List<String> getNames() {
        if (this.names == null) {
            this.names = new ArrayList<String>();
        }
        return names;
    }

    /**
     * @param name the name to set
     */
    public void addName(String name) {
        if (this.names == null) {
            this.names = new ArrayList<String>();
        }
        if(name.length() > 150)
            name = name.substring(0, 149);
        this.names.add(name);
    }

    /**
     * @return the rels
     */
    public List<PART_OF> getRels() {
        if (this.rels == null) {
            this.rels = new ArrayList<PART_OF>();
        }
        return rels;
    }

    /**
     * @param rels the orgs to set
     */
    public void addRel(PART_OF rel) {
        if (this.rels == null) {
            this.rels = new ArrayList<PART_OF>();
        }
        this.rels.add(rel);
    }

    public Map<String, Object> getOrganisationDocument() {
        Map<String, Object> organisationDocument = new HashMap<String, Object>();
        Map<String, Object> organisationNamesDocument = new HashMap<String, Object>();
        organisationDocument.put("organisationId", this.getOrganisationId());
        organisationDocument.put("names", this.getNames());
        organisationDocument.put("type", this.getType());
        organisationDocument.put("structId", this.getStructure());
        organisationDocument.put("url", this.getUrl());
        //organisationDocument.put("orgs", this.getRels());
        return organisationDocument;

    }

}
