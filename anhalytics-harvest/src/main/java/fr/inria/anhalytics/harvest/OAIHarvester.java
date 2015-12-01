package fr.inria.anhalytics.harvest;

import fr.inria.anhalytics.commons.data.PubFile;
import fr.inria.anhalytics.commons.data.TEI;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.properties.HarvestProperties;
import java.io.*;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.parsers.*;

/**
 * HAL OAI-PMH implementation.
 */
public class OAIHarvester extends Harvester {

    private static String OAI_FORMAT = "xml-tei";

    private final OAIPMHDomParser oaiDom;

    private String oai_url = null;

    public OAIHarvester() throws UnknownHostException {
        super();
        this.oai_url = HarvestProperties.getOaiUrl();
        oaiDom = new OAIPMHDomParser();
    }

    @Override
    public void fetchDocumentsByDate(String date) throws ParserConfigurationException, IOException {
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
            in.close();
        }
    }

    @Override
    public void fetchAllDocuments() throws ParserConfigurationException, IOException {
        for (String date : Utilities.getDates()) {
            logger.info("Extracting publications teis for : " + date);
            fetchDocumentsByDate(date);
        }
    }

    public void fetchEmbargoPublications() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String today_date = dateFormat.format(cal.getTime());
        Utilities.updateDates(null, today_date);
        for (String date : Utilities.getDates()) {
            Map<String, String> files = mm.getEmbargoRecords(date);
            Iterator it = files.entrySet().iterator();
            logger.info("\t [Embargo publications] Extracting publications teis for : " + date);
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String url = (String) pair.getValue();
                String id = (String) pair.getKey();
                try {
                    logger.info("\t\t Processing tei for " + id);
                    boolean donwloaded = downloadFile(new PubFile(url, date, "file"), id, date);
                    if (donwloaded) {
                        mm.removeEmbargoRecord(id);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mm.save(id, "harvestProcess", "harvest error", date);
                    logger.error("\t\t  Error occured while processing tei for " + id);
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
    }
}
