package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.exceptions.DataException;
import fr.inria.anhalytics.harvest.exceptions.GrobidTimeoutException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Patrice
 */
class GrobidSimpleFulltextWorker extends GrobidWorker {

    private static final Logger logger = LoggerFactory.getLogger(GrobidSimpleFulltextWorker.class);

    public GrobidSimpleFulltextWorker(BiblioObject biblioObject, int start, int end) throws ParserConfigurationException {
        super(biblioObject, start, end);
    }

    @Override
    protected void processCommand() {
        try {
            GrobidService grobidService = new GrobidService(this.start, this.end, true);
            // configured for HAL, first page is added to the document

            String filepath = Utilities.storeTmpFile(biblioObject.getPdf().getStream());

            try {
                biblioObject.getPdf().getStream().close();
            } catch (IOException ex) {
                throw new DataException("File stream can't be closed.", ex);
            }
            File file = new File(filepath);
            double mb = file.length() / (1024 * 1024);

            // for now we extract just files with less size (avoid thesis..which may take long time)
            if (mb <= 15) {
                logger.info("\t\t TEI extraction for : " + biblioObject.getRepositoryDocId() + " sizing :" + mb + "mb");
                String tei = grobidService.runFullTextGrobid(filepath).trim();
                tei = generateIdsGrobidTeiDoc(tei);
                
                mm.insertGrobidTei(tei, biblioObject.getAnhalyticsId());
                this.saveExtractedDOI(tei);
                biblioObject.setIsProcessedByGrobid(Boolean.TRUE);
                mm.updateBiblioObjectStatus(biblioObject);
                logger.info("\t\t " + biblioObject.getRepositoryDocId() + " processed.");
            } else {
                logger.info("\t\t can't extract TEI for : " + biblioObject.getRepositoryDocId() + "size too large : " + mb + "mb");
            }
            file.delete();
        } catch (GrobidTimeoutException e) {
            mm.save(biblioObject.getRepositoryDocId(), "processGrobid", "timed out");
            logger.warn("Processing of " + biblioObject.getRepositoryDocId() + " timed out");
        } catch (RuntimeException e) {
            e.printStackTrace();
            logger.error("\t\t error occurred while processing " + biblioObject.getRepositoryDocId());
            mm.save(biblioObject.getRepositoryDocId(), "processGrobid", e.getMessage());
            logger.error(e.getMessage(), e.getCause());
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex.getCause());
        }
    }
}
