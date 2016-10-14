package fr.inria.anhalytics.commons.entities;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author achraf
 */
public class Document_Organisation {
    private Document doc = null;
    private String type = "";
    private List<Organisation> orgs = null;
    
    public Document_Organisation(Document doc, String type, List<Organisation> orgs) {
        this.doc = doc;
        this.type = type;
        this.orgs = orgs;
    }
    
    public Document_Organisation(){}

    /**
     * @return the doc
     */
    public Document getDoc() {
        return doc;
    }

    /**
     * @param doc the doc to set
     */
    public void setDoc(Document doc) {
        this.doc = doc;
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
        if(type.length() > 55)
            type = type.substring(0, 54);
        this.type = type;
    }

    /**
     * @return the orgs
     */
    public List<Organisation> getOrgs() {
        if(this.orgs == null)
            this.orgs = new ArrayList<Organisation>();
        return orgs;
    }

    /**
     * @param orgs the doc_orgs to set
     */
    public void addOrg(Organisation doc_org) {
        if(this.orgs == null)
            this.orgs = new ArrayList<Organisation>();
        this.orgs.add(doc_org);
    }

}
