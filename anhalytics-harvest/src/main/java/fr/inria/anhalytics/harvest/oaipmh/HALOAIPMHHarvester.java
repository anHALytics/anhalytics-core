package fr.inria.anhalytics.harvest.oaipmh;

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
public class HALOAIPMHHarvester extends OAIPMHHarvester {

    private static String OAI_FORMAT = "xml-tei";

    protected HALOAIPMHDomParser oaiDom;
    
    public HALOAIPMHHarvester() throws UnknownHostException {
        super();
        this.oaiDom = new HALOAIPMHDomParser();
    }

    @Override
    protected void fetchDocumentsByDate(String date) {
        boolean stop = false;
        String tokenn = null;
        while (!stop) {
            String request = String.format("%s/?verb=ListRecords&metadataPrefix=%s&from=%s&until=%s", 
						this.oai_url, OAI_FORMAT, date, date);
            if(HarvestProperties.getCollection() != null)
                request += String.format("&set=collection:%s", HarvestProperties.getCollection());
            
            if (tokenn != null) {
                request = String.format("%s/?verb=ListRecords&resumptionToken=%s", this.oai_url, tokenn);
            }
            logger.info("\t Sending: " + request);

            InputStream in = Utilities.request(request, true);
            List<TEI> teis = this.oaiDom.getTeis(in);
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
            logger.info("Extracting publications TEIs for : " + date);
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
            logger.info("\t [Embargo publications] Extracting publications TEIs for : " + date);
            for (TEI file : files) {
                PublicationFile pf = file.getPdfdocument();
                String id = file.getRepositoryDocId();
                String type = file.getDocumentType();
                
                System.out.println(id + "   " + pf.isAnnexFile());
                try {
                    logger.info("\t\t Processing TEI for " + id);
                    boolean donwloaded = requestFile(pf, id, type, date);
                    if (donwloaded) {
                        mm.removeEmbargoRecord(id, pf.getUrl());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mm.log(id, pf.getUrl(), type, pf.isAnnexFile(), "unableToDownload", date);
                    logger.error("\t\t  Error occured while processing TEI for " + id);
                }
            }

        }
    }
}
