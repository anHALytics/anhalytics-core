package fr.inria.anhalytics.harvest;

import fr.inria.anhalytics.commons.data.PubFile;
import fr.inria.anhalytics.commons.data.TEI;
import fr.inria.anhalytics.commons.managers.MongoManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import javax.xml.parsers.*;

/**
 * HAL OAI-PMH implementation.
 */
public class OAIHarvester extends Harvester {

    private static String OAI_FORMAT = "xml-tei";
    private ArrayList<String> fields = null;
    private ArrayList<String> affiliations = null;

    private final OAIPMHDom oaiDom;

    private String oai_url = null;

    public OAIHarvester(MongoManager mm, String oai_url) {
        super(mm);
        this.oai_url = oai_url;
        oaiDom = new OAIPMHDom();

        fields = new ArrayList<String>();
        affiliations = new ArrayList<String>();

        affiliations.add("INRIA");

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
            System.out.println(request);
            logger.debug("Sending: " + request);

            InputStream in = Utilities.request(request, true);
            logger.debug("\t Extracting teis.... for " + date);
            List<TEI> teis = oaiDom.getTeis(in);

            processTeis(teis, date);

            // token if any:
            tokenn = oaiDom.getToken();
            if (tokenn == null) {
                stop = true;
            }
        }
        processEmbargoFiles(date);
    }

    @Override
    public void fetchAllDocuments() throws ParserConfigurationException, IOException {
        for (String date : Utilities.getDates()) {
            fetchDocumentsByDate(date);
        }
    }

    public void processEmbargoFiles(String date) {
        Map<String, String> files = mm.getEmbargoFiles(date);
        Iterator it = files.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            try {
                downloadFile(new PubFile((String) pair.getValue(), date, "file"), (String) pair.getKey(), date);
                System.out.println("done");
            } catch (Exception e) {
                e.printStackTrace();
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
}
