package fr.inria.anhalytics.harvest;

import fr.inria.anhalytics.commons.data.PublicationFile;
import fr.inria.anhalytics.commons.data.TEI;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.properties.HarvestProperties;
import java.io.*;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * HAL OAI-PMH implementation.
 *
 * @author Achraf
 */
public class HALOAIHarvester extends Harvester {

    private static String OAI_FORMAT = "xml-tei";

    private final HALOAIPMHDomParser oaiDom;

    private String oai_url = null;

    public HALOAIHarvester() throws UnknownHostException {
        super();
        this.oai_url = HarvestProperties.getOaiUrl();
        oaiDom = new HALOAIPMHDomParser();
    }

    @Override
    protected void fetchDocumentsByDate(String date) {
        boolean stop = false;
        String tokenn = null;
        while (!stop) {
            String request = oai_url + "/?verb=ListRecords&metadataPrefix=" + OAI_FORMAT + "&from=" + date + "&until=" + date;

            if (tokenn != null) {
                request = oai_url + "/?verb=ListRecords&resumptionToken=" + tokenn;
            }
            logger.info("\t Sending: " + request);

            InputStream in = Utilities.request(request, true);
            List<TEI> teis = oaiDom.getTeis(in);
            processTeis(teis, date, true);

            // token if any:
            tokenn = oaiDom.getToken();
            if (tokenn == null) {
                stop = true;
            }
            try {
                in.close();
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
    }

    @Override
    public void fetchAllDocuments() {
        for (String date : Utilities.getDates()) {
            logger.info("Extracting publications teis for : " + date);
            fetchDocumentsByDate(date);
        }
    }

    public void fetchEmbargoPublications() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String today_date = dateFormat.format(cal.getTime());
        Utilities.updateDates(null, today_date);
        for (String date : Utilities.getDates()) {
            List<TEI> files = mm.findEmbargoRecordsByDate(today_date);
            System.out.println(files);
            logger.info("\t [Embargo publications] Extracting publications teis for : " + date);
            for (TEI file : files) {
                PublicationFile pf = file.getPdfdocument();
                String id = file.getId();
                String type = file.getDocumentType();
                
                System.out.println(id + "   " + pf.isAnnexFile());
                try {
                    logger.info("\t\t Processing tei for " + id);
                    boolean donwloaded = requestFile(pf, id, type, date);
                    if (donwloaded) {
                        mm.removeEmbargoRecord(id, pf.getUrl());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mm.saveForLater(id, pf.getUrl(), type, pf.isAnnexFile(), "unableToDownload", date);
                    logger.error("\t\t  Error occured while processing tei for " + id);
                }
            }

        }
    }
}
