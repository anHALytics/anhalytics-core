package fr.inria.anhalytics.kb.entities;

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
    private Map<Date, String> names = null;
    private Date publication_date;
    private List<PART_OF> rels = null;

    public Organisation() {
    }

    public Organisation(Long organisationId, String type, String url, String structure, Map<Date, String> names, List<PART_OF> rels, Date publication_date) {
        this.organisationId = organisationId;
        this.type = type;
        this.url = url;
        this.structure = structure;
        this.names = names;
        this.rels = rels;
        this.publication_date = publication_date;
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
    public Map<Date, String> getNames() {
        if (this.names == null) {
            this.names = new HashMap<Date, String>();
        }
        return names;
    }

    /**
     * @param name the name to set
     */
    public void addName(Date date, String name) {
        if (this.names == null) {
            this.names = new HashMap<Date, String>();
        }
        if(name.length() > 150)
            name = name.substring(0, 149);
        this.names.put(date, name);
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
        Iterator it = this.getNames().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Date date = (Date) pair.getKey();
            String name = (String) pair.getValue();
        organisationNamesDocument.put(Utilities.formatDate(date), name);
        
        }
        organisationDocument.put("names", organisationNamesDocument);
        organisationDocument.put("type", this.getType());
        organisationDocument.put("structId", this.getStructure());
        organisationDocument.put("url", this.getUrl());
        //organisationDocument.put("orgs", this.getRels());
        return organisationDocument;

    }

    /**
     * @return the publication_date
     */
    public Date getPublication_date() {
        return publication_date;
    }

    /**
     * @param publication_date the publication_date to set
     */
    public void setPublication_date(Date publication_date) {
        this.publication_date = publication_date;
    }

}
