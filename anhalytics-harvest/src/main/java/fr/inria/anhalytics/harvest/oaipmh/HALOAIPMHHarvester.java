package fr.inria.anhalytics.harvest.oaipmh;

import fr.inria.anhalytics.commons.data.TEIFile;
import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;

/**
 * HAL OAI-PMH implementation.
 *
 * @author Achraf
 */
public class HALOAIPMHHarvester extends OAIPMHHarvester {

    private static String OAI_FORMAT = "xml-tei";

    private HALOAIPMHDomParser oaiDom;

    public HALOAIPMHHarvester() {
        super();
        this.oaiDom = new HALOAIPMHDomParser();
    }

    @Override
    protected void fetchDocumentsByDate(String date) throws MalformedURLException {
        boolean stop = false;
        String tokenn = null;
        while (!stop) {
            String request = String.format("%s/?verb=ListRecords&metadataPrefix=%s&from=%s&until=%s",
                    this.oai_url, OAI_FORMAT, date, date);
            if (HarvestProperties.getCollection() != null) {
                request += String.format("&set=collection:%s", HarvestProperties.getCollection());
            }

            if (tokenn != null) {
                request = String.format("%s/?verb=ListRecords&resumptionToken=%s", this.oai_url, tokenn);
            }
            logger.info("\t Sending: " + request);

            InputStream in = Utilities.request(request);
            List<TEIFile> teis = this.oaiDom.getTeis(in);
            processTeis(teis, date, true);

            // token if any:
            tokenn = oaiDom.getToken();
            if (tokenn == null) {
                stop = true;
            }
            try {
                in.close();
            } catch (IOException ioex) {
                throw new ServiceException("Couldn't close opened harvesting stream source.", ioex);
            }
        }
    }

    @Override
    public void fetchAllDocuments() {
        String currentDate = "";
        try {
            for (String date : Utilities.getDates()) {
                logger.info("Extracting publications TEIs for : " + date);
                currentDate = date;
                fetchDocumentsByDate(date);
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (ServiceException se) {
            mm.save(currentDate, "blockedHarvestProcess", se.getMessage(), currentDate);
        }
    }
//
//    public void fetchEmbargoPublications() {
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        Calendar cal = Calendar.getInstance();
//        String today_date = dateFormat.format(cal.getTime());
//        Utilities.updateDates(null, today_date);
//        for (String date : Utilities.getDates()) {
//            List<TEIFile> files = mm.findEmbargoRecordsByDate(today_date);
//            System.out.println(files);
//            logger.info("\t [Embargo publications] Extracting publications TEIs for : " + date);
//            for (TEIFile file : files) {
//                PublicationFile pf = file.getPdfdocument();
//                String id = file.getRepositoryDocId();
//                String type = file.getDocumentType();
//
//                System.out.println(id + "   " + pf.isAnnexFile());
//                try {
//                    logger.info("\t\t Processing TEI for " + id);
//                    boolean donwloaded = requestFile(pf, id, type, date);
//                    if (donwloaded) {
//                        mm.removeEmbargoRecord(id, pf.getUrl());
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    mm.log(id, pf.getUrl(), type, pf.isAnnexFile(), "unableToDownload", date);
//                    logger.error("\t\t  Error occured while processing TEI for " + id);
//                }
//            }
//
//        }
//    }

    /**
     * @param oaiDom the oaiDom to set
     */
    public void setOaiDom(HALOAIPMHDomParser oaiDom) {
        this.oaiDom = oaiDom;
    }
}
