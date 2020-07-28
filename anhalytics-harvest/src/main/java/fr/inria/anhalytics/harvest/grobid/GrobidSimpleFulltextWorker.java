package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.data.Processings;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(GrobidSimpleFulltextWorker.class);

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
            file = new File(filepath);
            double mb = file.length() / (1024 * 1024);

            // for now we extract just files with less size (avoid thesis..which may take long time)
            if (mb <= 15) {
                LOGGER.info("\t\t "+Thread.currentThread().getName() +": TEI extraction for : " + biblioObject.getRepositoryDocId() + " sizing :" + mb + "mb");
                String tei = grobidService.runFullTextGrobid(filepath).trim();
                tei = generateIdsGrobidTeiDoc(tei);

                boolean inserted = mm.insertGrobidTei(tei, biblioObject.getAnhalyticsId());
                if (inserted) {
                    this.saveExtractedDOI(tei);
                    mm.updateBiblioObjectStatus(biblioObject, Processings.GROBID, false);
                    LOGGER.info("\t\t "+Thread.currentThread().getName() +": " + biblioObject.getRepositoryDocId() + " processed.");
                } else
                    LOGGER.error("\t\t "+Thread.currentThread().getName() +": Problem occured while saving " + biblioObject.getRepositoryDocId() + " grobid TEI.");
            } else {
                LOGGER.info("\t\t "+Thread.currentThread().getName() +": can't extract TEI for : " + biblioObject.getRepositoryDocId() + "size too large : " + mb + "mb");
            }

        } catch (GrobidTimeoutException e) {
            mm.save(biblioObject.getRepositoryDocId(), "processGrobid", "timed out");
            LOGGER.warn(Thread.currentThread().getName() +"Processing of " + biblioObject.getRepositoryDocId() + " timed out");
        } catch (RuntimeException e) {
            LOGGER.error("Error: ", e);
            LOGGER.error("\t\t "+Thread.currentThread().getName() +": error occurred while processing " + biblioObject.getRepositoryDocId());
            mm.save(biblioObject.getRepositoryDocId(), "processGrobid", e.getMessage());
            LOGGER.error(e.getMessage(), e.getCause());
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex.getCause());
        }
        boolean success = false;
        if(file.exists()) {
            success = file.delete();
            if (!success) {
                LOGGER.error(
                        Thread.currentThread().getName() +": Deletion of temporary image files failed for file '" + file.getAbsolutePath() + "'");
            }else
                LOGGER.info("\t\t "+Thread.currentThread().getName() +" :"+ file.getAbsolutePath()  +" deleted.");
        }
    }
}
