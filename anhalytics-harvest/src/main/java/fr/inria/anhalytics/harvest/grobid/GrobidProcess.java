package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.exceptions.UnreachableGrobidServiceException;
import fr.inria.anhalytics.commons.managers.MongoManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.properties.HarvestProperties;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Achraf
 */
public class GrobidProcess {

    private static final Logger logger = LoggerFactory.getLogger(GrobidProcess.class);

    private MongoManager mm;

    public GrobidProcess(MongoManager mm) {
        this.mm = mm;
    }

    /**
     * Checks if Grobid service is 'requestabte' and local tmp directory is
     * available.
     *
     * @return
     */
    private boolean isAllOk() throws MalformedURLException, IOException {
        URL url = new URL("http://" + HarvestProperties.getGrobidHost() + ":" + HarvestProperties.getGrobidPort() + "/isalive");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("GET");
        int responseCode = 0;
        try {
            responseCode = conn.getResponseCode();
        } catch (UnknownHostException e) {
        }
        if (responseCode != 200) {
            throw new UnreachableGrobidServiceException("Grobid service is not alive.");
        }
        conn.disconnect();

        Utilities.checkPath(HarvestProperties.getTmpPath());
        logger.debug("Grobid service is ok and ready to be used...");
        return true;
    }

    public void processFulltext() throws IOException {
        if (isAllOk()) {
            ExecutorService executor = Executors.newFixedThreadPool(HarvestProperties.getNbThreads());
            mm.setGridFS(MongoManager.BINARIES);
            for (String date : Utilities.getDates()) {
                if (mm.initBinaries(date)) {
                    while (mm.hasMoreDocuments()) {
                        InputStream content = mm.nextBinaryDocument();
                        if (HarvestProperties.isReset() | !mm.isGrobidified()) { // condition TBT
                            String filename = mm.getCurrentFilename();
                            try {
                                Runnable worker = new GrobidFulltextWorker(content, mm, date);
                                executor.execute(worker);
                            } catch (final Exception exp) {
                                logger.error("An error occured while processing the file " + filename
                                        + ". Continuing the process for the other files" + exp.getMessage());
                            }
                        }
                        content.close();
                    }
                }
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            System.out.println("Finished all threads");
        }
    }

    public void processAnnexes() throws IOException {
        if (isAllOk()) {
            ExecutorService executor = Executors.newFixedThreadPool(HarvestProperties.getNbThreads());
            for (String date : Utilities.getDates()) {
                if (mm.initAnnexes(date)) {
                    while (mm.hasMoreDocuments()) {
                        String filename = mm.getCurrentFilename();
                        if (filename.endsWith(".pdf")) {
                            InputStream content = mm.nextBinaryDocument();
                            try {
                                Runnable worker = new GrobidAnnexWorker(content, mm, date);
                                executor.execute(worker);
                            } catch (final Exception exp) {
                                logger.error("An error occured while processing the file " + filename
                                        + ". Continuing the process for the other files" + exp.getMessage());
                            }
                            content.close();

                        }
                    }
                }
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            System.out.println("Finished all threads");
        }
    }
}
