package fr.inria.anhalytics.harvest.oaipmh;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Achraf
 */

/* to be reviewed and moved !!*/

public interface OAIPMHPathsItf {
    public final static String ListRecordsElement = "ListRecords";
    public final static String RecordElement = "record";
    public final static String TeiElement = "metadata";
    public final static String IdElement = "identifier";
    public final static String TypeElement = "setSpec";
    public final static String ResumptionToken = "resumptionToken";
    public final static String AnnexesUrlsElement = "metadata/TEI/text/body/listBibl/biblFull/editionStmt/edition[@type='current']/ref[@type='annex']";
    public final static String FileElement = "metadata/TEI/text/body/listBibl/biblFull/editionStmt/edition[@type='current']/ref[@type='file'][1]";
    public final static String EditionElement = "metadata/TEI/text/body/listBibl/biblFull/editionStmt/edition[@type='current']";
    public final static String RefPATH = "metadata/TEI/text/body/listBibl/biblFull/publicationStmt/idno[@type='halRef']";
    public final static String DoiPATH = "metadata/TEI/text/body/listBibl/biblFull/sourceDesc/biblStruct/idno[@type='doi']";

    /* note: these are HAL specific types */    
    enum ConsideredTypes {
        ART, COMM, OUV, POSTER, DOUV, PATENT, REPORT, THESE, HDR, LECTURE, COUV, OTHER, UNDEFINED  //IMG, VIDEO, AUDIOS, SON, MAP
    };
}
