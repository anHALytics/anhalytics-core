package fr.inria.anhalytics.kb.entities;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author azhar
 */
public class Conference_Event {

    private Long conf_eventID;
    private String start_date = "";
    private String end_date = "";
    private Monograph monograph;
    private Conference conference;
    private Address address;

    public Conference_Event() {
    }

    public Conference_Event(Long conf_eventID, String start_date, String end_date, Monograph monograph, Conference conference, Address address) {
        this.conf_eventID = conf_eventID;
        this.start_date = start_date;
        this.end_date = end_date;
        this.monograph = monograph;
        this.conference = conference;
        this.address = address;
    }

    /**
     * @return the conf_eventID
     */
    public Long getConf_eventID() {
        return conf_eventID;
    }

    /**
     * @param conf_eventID the conf_eventID to set
     */
    public void setConf_eventID(Long conf_eventID) {
        this.conf_eventID = conf_eventID;
    }

    /**
     * @return the start_date
     */
    public String getStart_date() {
        return start_date;
    }

    /**
     * @param start_date the start_date to set
     */
    public void setStart_date(String start_date) {
        if (start_date.length() > 45) {
            start_date = start_date.substring(0, 44);
        }
        this.start_date = start_date;
    }

    /**
     * @return the end_date
     */
    public String getEnd_date() {
        return end_date;
    }

    /**
     * @param end_date the end_date to set
     */
    public void setEnd_date(String end_date) {
        if (end_date.length() > 45) {
            end_date = end_date.substring(0, 44);
        }
        this.end_date = end_date;
    }

    /**
     * @return the mongoraph
     */
    public Monograph getMonograph() {
        return monograph;
    }

    /**
     * @param mongoraph the mongoraph to set
     */
    public void setMongoraph(Monograph monograph) {
        this.monograph = monograph;
    }

    /**
     * @return the conference
     */
    public Conference getConference() {
        return conference;
    }

    /**
     * @param conference the conference to set
     */
    public void setConference(Conference conference) {
        this.conference = conference;
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

    public Map<String, Object> getConference_EventDocument() {
        Map<String, Object> conference_EventDocument = new HashMap<String, Object>();
        conference_EventDocument.put("conf_eventID", this.getConf_eventID());
        conference_EventDocument.put("address", this.getAddress().getAddressDocument());
        conference_EventDocument.put("start_date", this.getStart_date());
        conference_EventDocument.put("end_date", this.getEnd_date());
        conference_EventDocument.put("title", this.getConference().getTitle());
        return conference_EventDocument;
    }
}
