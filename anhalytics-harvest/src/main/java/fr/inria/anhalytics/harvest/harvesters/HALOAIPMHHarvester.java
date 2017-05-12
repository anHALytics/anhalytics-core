package fr.inria.anhalytics.harvest.harvesters;

import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.Harvester;
import fr.inria.anhalytics.harvest.parsers.HALOAIPMHDomParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * HAL OAI-PMH harvester implementation.
 *
 * @author Achraf
 */
public class HALOAIPMHHarvester extends Harvester {

    private static String OAI_FORMAT = "xml-tei";

    // the api url
    protected String oai_url = "http://api.archives-ouvertes.fr/oai/hal";
    
    private HALOAIPMHDomParser oaiDom;

    public HALOAIPMHHarvester() {
        super();
        this.oaiDom = new HALOAIPMHDomParser();
    }

    /**
    * Gets results given a date as suggested by OAI-PMH.
    */
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
            grabbedObjects = this.oaiDom.getGrabbedObjects(in, grabbedObjects);
            saveObjects();

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
            logger.error(mue.getMessage(), mue);
        } catch (ServiceException se) {
            logger.error(se.getMessage(), se);
            mm.save(currentDate, "blockedHarvestProcess", se.getMessage());
        }
    }

}
