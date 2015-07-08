package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.managers.MongoManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.IOException;
import java.io.InputStream;
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

    private static final int NTHREDS = 3;

    private String grobid_host = null;
    private String grobid_port = null;
    private MongoManager mm;

    public GrobidProcess(String grobid_host, String grobid_port, MongoManager mm) {
        this.grobid_host = grobid_host;
        this.grobid_port = grobid_port;
        this.mm = mm;
    }

    public void processFulltext() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
        for (String date : Utilities.getDates()) {
            if (mm.init(MongoManager.BINARIES, date)) {
                while (mm.hasMoreDocuments()) {
                    String filename = mm.getCurrentFilename();
                    InputStream content = mm.nextBinaryDocument();
                    try {
                        Runnable worker = new GrobidFulltextWorker(content, mm, grobid_host, grobid_port, date);
                        executor.execute(worker);
                    } catch (final Exception exp) {
                        logger.error("An error occured while processing the file " + filename
                                + ". Continuing the process for the other files" + exp.getMessage());
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

    public void processAnnexes() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
        for (String date : Utilities.getDates()) {
            if (mm.init(MongoManager.BINARIES, date)) {
                while (mm.hasMoreDocuments()) {
                    String filename = mm.getCurrentFilename();
                    if (filename.endsWith(".pdf")) {
                        InputStream content = mm.nextBinaryDocument();
                        try {
                            Runnable worker = new GrobidAnnexWorker(content, mm, grobid_host, grobid_port, date);
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
