package fr.inria.anhalytics.commons.data.arxiv;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.inria.anhalytics.commons.utilities.KeyGen;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;


/**
 * id: ArXiv ID (can be used to access the paper, see below)
 * submitter: Who submitted the paper
 * authors: Authors of the paper
 * title: Title of the paper
 * comments: Additional info, such as number of pages and figures
 * journal-ref: Information about the journal the paper was published in
 * doi: [https://www.doi.org](Digital Object Identifier)
 * abstract: The abstract of the paper
 * categories: Categories / tags in the ArXiv system
 * versions: A version history
 */

public class ArxivMetadata {
    private String id;
    private String submitter;
    private String authors;
    private String title;
    private String comments;
    @JsonProperty("journal-ref")
    private String journalRef;
    private String doi;

    @JsonProperty("report-no")
    private String reportNo;

    @JsonProperty("license")
    private String license;

    @JsonProperty("abstract")
    private String abstract_;
    private String categories;
    private List<ArxivVersion> versions;

    @JsonProperty("update_date")
    private String updateDate;

    @JsonProperty("authors_parsed")
    private List<List<String>> authorsParsed;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getJournalRef() {
        return journalRef;
    }

    public void setJournalRef(String journalRef) {
        this.journalRef = journalRef;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getAbstract_() {
        return abstract_;
    }

    public void setAbstract_(String abstract_) {
        this.abstract_ = abstract_;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }


    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getReportNo() {
        return reportNo;
    }

    public void setReportNo(String reportNo) {
        this.reportNo = reportNo;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public List<ArxivVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<ArxivVersion> versions) {
        this.versions = versions;
    }

    public List<List<String>> getAuthorsParsed() {
        return authorsParsed;
    }

    public void setAuthorsParsed(List<List<String>> authorsParsed) {
        this.authorsParsed = authorsParsed;
    }


    public String toTei() {
        StringBuilder tei = new StringBuilder();
        tei.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
//        tei.append("<!DOCTYPE TEI SYSTEM \"" + GrobidProperties.get_GROBID_HOME_PATH()
//                + "/schemas/dtd/Grobid.dtd" + "\">\n");
        tei.append("<TEI xml:space=\"preserve\" xmlns=\"http://www.tei-c.org/ns/1.0\" \n" +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "xsi:schemaLocation=\"http://www.tei-c.org/ns/1.0 " +
                "config/grobid/Grobid.xsd\"" +
                "\n xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");

        tei.append("\t<teiHeader xml:lang=\"en\">");
//        tei.append("\t<teiHeader>");

        tei.append("\n\t\t<fileDesc>\n\t\t\t<titleStmt>\n\t\t\t\t<title level=\"a\" type=\"main\"");
        String divID = KeyGen.getKey().substring(0, 7);
        tei.append(" xml:id=\"_" + divID + "\"");
        tei.append(">");

        if (getTitle() != null) {
            tei.append(StringEscapeUtils.escapeHtml4(getTitle()));
        }
        tei.append("</title>\n\t\t\t</titleStmt>\n");

        //No publisher

        tei.append("\t\t\t<sourceDesc>\n\t\t\t\t<biblStruct>\n\t\t\t\t\t<analytic>\n");

        // authors
        for (List<String> author : getAuthorsParsed()) {
            tei.append("<author><persName xmlns=\"http://www.tei-c.org/ns/1.0\">");
            if (isNotEmpty(author.get(0))) {
                tei.append("<forename type=\"first\">").append(author.get(0)).append("</forename>");
            }
            if (isNotEmpty(author.get(1))) {
                tei.append("<forename type=\"middle\">").append(author.get(1)).append("</forename>");
            }

            if (isNotEmpty(author.get(2))) {
                tei.append("<surname>").append(author.get(2)).append("</surname>");
            }
            tei.append("</persName></author>");
        }


        //getAuthors()
        tei.append("\t\t\t\t\t\t<title");
        tei.append(" level=\"a\" type=\"main\"");
        tei.append(" xml:id=\"_" + KeyGen.getKey().substring(0, 7) + "\"");
        tei.append(" xml:lang=\"en\">" + StringEscapeUtils.escapeHtml4(title) + "</title>\n");

        tei.append("\t\t\t\t\t</analytic>\n");

        if (!StringUtils.isEmpty(getDoi())) {
            String theDOI = StringEscapeUtils.escapeHtml4(getDoi());
            if (theDOI.endsWith(".xml")) {
                theDOI = theDOI.replace(".xml", "");
            }
            tei.append("\t\t\t\t\t<idno type=\"DOI\">" + theDOI + "</idno>\n");
        }

        if (!StringUtils.isEmpty(getId())) {
            tei.append("\t\t\t\t\t<idno type=\"arXiv\">" + StringEscapeUtils.escapeHtml4(getId()) + "</idno>\n");
        }
        tei.append("\t\t\t\t</biblStruct>\n");

        tei.append("\t\t\t</sourceDesc>\n");
        tei.append("\t\t</fileDesc>\n");

        // encodingDesc gives info about the producer of the file
        tei.append("\t\t<encodingDesc>\n");
        tei.append("\t\t\t<appInfo>\n");

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        df.setTimeZone(tz);
        String dateISOString = df.format(new java.util.Date());

        tei.append("\t\t\t\t<application version=\"0.0.1-SNAPSHOT\" ident=\"anhalytics\" when=\"" + dateISOString + "\">\n");
        tei.append("\t\t\t\t\t<desc>anhalytics</desc>\n");
        tei.append("\t\t\t\t\t<ref target=\"https://github.com/anhalytics/anhalytics-core\"/>\n");
        tei.append("\t\t\t\t</application>\n");
        tei.append("\t\t\t</appInfo>\n");
        tei.append("\t\t</encodingDesc>\n");

        tei.append("\t\t<profileDesc>\n");
        tei.append("\t\t\t<abstract xml:lang=\"").append("en").append("\">\n");
        tei.append("<div><p>").append(getAbstract_()).append("</p></div>");
        tei.append("\n\t\t\t</abstract>\n");
        tei.append("\t\t</profileDesc>\n");
        tei.append("\t</teiHeader>\n");
        tei.append("</TEI>");

        return tei.toString();
    }
}