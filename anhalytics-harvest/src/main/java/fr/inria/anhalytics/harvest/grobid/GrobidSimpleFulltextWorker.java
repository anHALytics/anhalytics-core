package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.exceptions.GrobidTimeoutException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Patrice
 */
class GrobidSimpleFulltextWorker extends GrobidWorker {

    private static final Logger logger = LoggerFactory.getLogger(GrobidSimpleFulltextWorker.class);
    
    public GrobidSimpleFulltextWorker(InputStream content, String id, String date) throws UnknownHostException {
        super(content, id, date);
    }

    @Override
    protected void processCommand() {
        try {
            GrobidService grobidService = new GrobidService(2, -1, true, date); 
            // configured for HAL, first page is added to the document

            String filepath = Utilities.storeTmpFile(content);
            File file = new File(filepath);
            double mb = file.length() / (1024 * 1024);

            // for now we extract just files with less size (avoid thesis..which may take long time)
            if (mb <= 15) {
                logger.info("\t\t TEI extraction for : " + id + " sizing :" + mb + "mb");
                String tei = grobidService.runFullTextGrobid(filepath).trim();
                mm.insertGrobidTei(tei, id, date);

                logger.debug("\t\t " + id + " processed.");
            } else {
                logger.info("\t\t can't extract TEI for : " + id + "size too large : " + mb + "mb");
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
            logger.error(e.getMessage(), e.getCause());
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex.getCause());
        }
    }
}
