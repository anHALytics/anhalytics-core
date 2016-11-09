package fr.inria.anhalytics.commons.entities;

import fr.inria.anhalytics.commons.utilities.Utilities;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
    private String status = "";
    private List<Organisation_Name> names = null;
    private List<PART_OF> rels = null;
    private List<Organisation_Identifier> organisation_identifiers = null;

    public Organisation() {
    }

    public Organisation(Long organisationId, String type, String status, String url, String structure, List<Organisation_Name> names, List<PART_OF> rels, List<Organisation_Identifier> organisation_identifiers) {
        this.organisationId = organisationId;
        this.type = type;
        this.url = url;
        this.structure = structure;
        this.names = names;
        this.status = status;
        this.rels = rels;
        this.organisation_identifiers = organisation_identifiers;
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
        if (type.length() > 45) {
            type = type.substring(0, 44);
        }
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
        if (url.length() > 255) {
            url = url.substring(0, 254);
        }
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
        if (structure.length() > 45) {
            structure = structure.substring(0, 44);
        }
        this.structure = structure;
    }

    /**
     * @return the name
     */
    public List<Organisation_Name> getNames() {
        if (this.names == null) {
            this.names = new ArrayList<Organisation_Name>();
        }
        return names;
    }

    /**
     * @param name the name to set
     */
    public void addName(Organisation_Name name) {
        if (this.names == null) {
            this.names = new ArrayList<Organisation_Name>();
        }
        if (name.getName().length() > 150) {
            name.setName(name.getName().substring(0, 149));
        }
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
        List<Map<String, Object>> organisationNamesDocument = new ArrayList<Map<String, Object>>();
        organisationDocument.put("organisationId", this.getOrganisationId());
        List<Map<String, Object>> organisationIdentifiersDocument = new ArrayList<Map<String, Object>>();
        for (Organisation_Name name : getNames()) {
            organisationNamesDocument.add(name.getOrganisationNameDocument());
        }
        organisationDocument.put("names", organisationNamesDocument);
        organisationDocument.put("type", this.getType());
        organisationDocument.put("status", this.getStatus());
        organisationDocument.put("structId", this.getStructure());
        organisationDocument.put("url", this.getUrl());
        for (Organisation_Identifier oi : this.getOrganisation_identifiers()) {
            Map<String, Object> id = new HashMap<String, Object>();
            id.put("type", oi.getType());
            id.put("id", oi.getId());
            organisationIdentifiersDocument.add(id);
        }
        organisationDocument.put("identifers", organisationIdentifiersDocument);
        //organisationDocument.put("orgs", this.getRels());
        return organisationDocument;

    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the organisation_identifiers
     */
    public List<Organisation_Identifier> getOrganisation_identifiers() {
        if (this.organisation_identifiers == null) {
            this.organisation_identifiers = new ArrayList<Organisation_Identifier>();
        }
        return organisation_identifiers;
    }

    /**
     * @param organisation_identifiers the organisation_identifiers to set
     */
    public void setOrganisation_identifiers(List<Organisation_Identifier> organisation_identifiers) {
        this.organisation_identifiers = organisation_identifiers;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (object != null && object instanceof Organisation) {
            isEqual = (this.organisationId.equals(((Organisation) object).organisationId));
        }

        return isEqual;
    }

}
