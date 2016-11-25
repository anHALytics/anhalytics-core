package fr.inria.anhalytics.commons.entities;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author achraf
 */
public class Document_Organisation {

    private Document doc = null;
    private List<Organisation> orgs = null;

    public Document_Organisation(Document doc, List<Organisation> orgs) {
        this.doc = doc;
        this.orgs = orgs;
    }

    public Document_Organisation() {
    }

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
     * @return the orgs
     */
    public List<Organisation> getOrgs() {
        if (this.orgs == null) {
            this.orgs = new ArrayList<Organisation>();
        }
        return orgs;
    }

    /**
     * @param orgs the doc_orgs to set
     */
    public void addOrg(Organisation doc_org) {
        if (this.orgs == null) {
            this.orgs = new ArrayList<Organisation>();
        }
        if (!this.orgs.contains(doc_org)) {
            this.orgs.add(doc_org);
        }
    }

}
