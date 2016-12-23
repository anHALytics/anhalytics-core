package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.data.BinaryFile;
import fr.inria.anhalytics.commons.data.TEIFile;
import fr.inria.anhalytics.commons.exceptions.DataException;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.harvest.exceptions.GrobidTimeoutException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Patrice
 */
class GrobidSimpleFulltextWorker extends GrobidWorker {

    private static final Logger logger = LoggerFactory.getLogger(GrobidSimpleFulltextWorker.class);

    public GrobidSimpleFulltextWorker(BinaryFile bf, String date, int start, int end) throws ParserConfigurationException {
        super(bf, date, start, end);
    }

    @Override
    protected void processCommand() {
        try {
            GrobidService grobidService = new GrobidService(this.start, this.end, true, date);
            // configured for HAL, first page is added to the document

            String filepath = Utilities.storeTmpFile(bf.getStream());

            try {
                bf.getStream().close();
            } catch (IOException ex) {
                throw new DataException("File stream can't be closed.", ex);
            }
            File file = new File(filepath);
            double mb = file.length() / (1024 * 1024);

            // for now we extract just files with less size (avoid thesis..which may take long time)
            if (mb <= 15) {
                logger.info("\t\t TEI extraction for : " + bf.getRepositoryDocId() + " sizing :" + mb + "mb");
                String tei = grobidService.runFullTextGrobid(filepath).trim();
                tei = generateIdsTeiDoc(tei);
                TEIFile teifile = new TEIFile(bf.getSource(), bf.getRepositoryDocId(), null, null, "", bf.getDocumentType(), tei, bf.getRepositoryDocVersion(), bf.getAnhalyticsId());
                mm.insertTei(teifile, date, MongoCollectionsInterface.GROBID_TEIS);
                this.saveDocumentDOI(tei);
                logger.info("\t\t " + bf.getRepositoryDocId() + " processed.");
            } else {
                logger.info("\t\t can't extract TEI for : " + bf.getRepositoryDocId() + "size too large : " + mb + "mb");
            }
            file.delete();
        } catch (GrobidTimeoutException e) {
            mm.save(bf.getRepositoryDocId(), "processGrobid", "timed out", date);
            logger.warn("Processing of " + bf.getRepositoryDocId() + " timed out");
        } catch (RuntimeException e) {
            e.printStackTrace();
            logger.error("\t\t error occurred while processing " + bf.getRepositoryDocId());
            mm.save(bf.getRepositoryDocId(), "processGrobid", e.getMessage(), date);
            logger.error(e.getMessage(), e.getCause());
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex.getCause());
        }
    }
}
