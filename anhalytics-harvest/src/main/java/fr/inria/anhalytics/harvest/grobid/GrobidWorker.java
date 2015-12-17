package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.exceptions.GrobidTimeoutException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Achraf
 */
abstract class GrobidWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GrobidWorker.class);
    private InputStream content;
    protected MongoFileManager mm;
    protected String date;
    protected String id;

    public GrobidWorker(InputStream content, String id, String date) throws UnknownHostException {
        this.content = content;
        this.mm = MongoFileManager.getInstance(false);
        this.date = date;
        this.id = mm.getCurrentRepositoryDocId();
    }

    @Override
    public void run() {
        try {
            long startTime = System.nanoTime();
            logger.info(Thread.currentThread().getName() + " Start. Processing = " + id);
            processCommand();
            long endTime = System.nanoTime();
            logger.info(Thread.currentThread().getName() + " End. :" + (endTime - startTime) / 1000000 + " ms");
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(GrobidFulltextWorker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(GrobidFulltextWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processCommand() throws IOException, ParseException {
        try {
            GrobidService grobidService = new GrobidService(2, -1, true, date);//configured for HAL, first page is added to the document

            String filepath = Utilities.storeTmpFile(content);
            File file = new File(filepath);
            double mb = file.length() / (1024 * 1024);

            if (mb <= 15) { // for now we extract just files with less size (avoid thesis..which may take long time)
                logger.info("\t\t Tei extraction for : " + id + " sizing :" + mb +"mb");
                String resultPath = grobidService.runFullTextGrobid(filepath);
                saveExtractions(resultPath);

                FileUtils.deleteDirectory(new File(resultPath));
                logger.debug("\t\t " + id + " processed.");
            } else {
                logger.info("\t\t can't extract tei for : " + id + "size too large : " + mb +"mb");
            }
            file.delete();
        } catch (GrobidTimeoutException e) {
            mm.save(id, "processGrobid", "timed out", date);
            logger.warn("Processing of " + id + " timed out");
        } catch (RuntimeException e) {
            logger.error("\t\t error occurred while processing " + id);
            if (e.getMessage().contains("timed out")) {
                mm.save(id, "processGrobid", "timed out", date);
            } else if (e.getMessage().contains("failed")) {
                mm.save(id, "processGrobid", "failed", date);
            }
            e.printStackTrace();
        }
    }

    protected void saveExtractions(String resultPath) {
    }

    ;

    @Override
    public String toString() {
        return this.id;
    }
}
