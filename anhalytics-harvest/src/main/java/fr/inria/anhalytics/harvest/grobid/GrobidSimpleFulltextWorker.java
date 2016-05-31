package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.exceptions.GrobidTimeoutException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Patrice
 */
class GrobidSimpleFulltextWorker extends GrobidWorker {

    private static final Logger logger = LoggerFactory.getLogger(GrobidSimpleFulltextWorker.class);
    
    public GrobidSimpleFulltextWorker(InputStream content, String currentRepositoryDocId, String currentAnhalyticsId, String date, int start, int end) throws UnknownHostException {
        super(content, currentRepositoryDocId, currentAnhalyticsId, date, start, end);
    }

    @Override
    protected void processCommand() {
        try {
            GrobidService grobidService = new GrobidService(this.start, this.end, true, date); 
            // configured for HAL, first page is added to the document

            String filepath = Utilities.storeTmpFile(content);
            File file = new File(filepath);
            double mb = file.length() / (1024 * 1024);

            // for now we extract just files with less size (avoid thesis..which may take long time)
            if (mb <= 15) {
                logger.info("\t\t TEI extraction for : " + repositoryDocId + " sizing :" + mb + "mb");
                String tei = grobidService.runFullTextGrobid(filepath).trim();
                tei = generateIdsTeiDoc(tei);
                mm.insertGrobidTei(tei, repositoryDocId, anhalyticsId, date);

                logger.debug("\t\t " + repositoryDocId + " processed.");
            } else {
                logger.info("\t\t can't extract TEI for : " + repositoryDocId + "size too large : " + mb + "mb");
            }
            file.delete();
        } catch (GrobidTimeoutException e) {
            mm.save(repositoryDocId, "processGrobid", "timed out", date);
            logger.warn("Processing of " + repositoryDocId + " timed out");
        } catch (RuntimeException e) {
            logger.error("\t\t error occurred while processing " + repositoryDocId);
                mm.save(repositoryDocId, "processGrobid", e.getMessage(), date);
            logger.error(e.getMessage(), e.getCause());
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex.getCause());
        }
    }
}
