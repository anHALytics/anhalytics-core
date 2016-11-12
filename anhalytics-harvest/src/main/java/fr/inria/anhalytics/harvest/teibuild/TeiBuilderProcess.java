package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.exceptions.DataException;
import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Retrieves existing grobid teis and adds fulltext to the final tei.
 *
 * @author Achraf
 */
public class TeiBuilderProcess {

    private static final Logger logger = LoggerFactory.getLogger(TeiBuilderProcess.class);

    private MongoFileManager mm;

    private TeiBuilder tb;

    public TeiBuilderProcess() throws ParserConfigurationException {
        try {
            this.mm = MongoFileManager.getInstance(false);
        } catch (ServiceException ex) {
            throw new ServiceException("MongoDB is not UP, the process will be halted.");
        }
        this.tb = new TeiBuilder();
    }

    /**
     * Clean up metadatas , add grobid TEI and build a corpus tei.
     */
    public void buildTEICorpus() {
        for (String date : Utilities.getDates()) {
            if (!HarvestProperties.isProcessByDate()) {
                date = null;
            }
            if (mm.initMetadataTeis(date)) {
                while (mm.hasMoreTeis()) {
                    String metadataString = mm.nextTeiDocument();
                    String currentRepositoryDocId = mm.getCurrentRepositoryDocId();
                    String currentAnhalyticsId = mm.getCurrentAnhalyticsId();
                    boolean fulltextAvailable = false;
                    Document generatedTEIcorpus = null;
                    if (currentAnhalyticsId == null || currentAnhalyticsId.isEmpty()) {
                        logger.info("skipping " + currentRepositoryDocId + " No anHALytics id provided");
                        continue;
                    }
                    logger.info("\t Building TEI for: " + currentRepositoryDocId);
                    metadataString = Utilities.trimEncodedCharaters(metadataString);
                    generatedTEIcorpus = createTEICorpus(metadataString);
                    try {
                        String grobidTei = getGrobidTei(currentAnhalyticsId);
                        generatedTEIcorpus = tb.addGrobidTEIToTEICorpus(Utilities.toString(generatedTEIcorpus), grobidTei);
                        fulltextAvailable = true;
                    } catch (DataException de) {
                        logger.error("No corresponding fulltext TEI was found.");
                    }
                    String teiCorpus = Utilities.toString(generatedTEIcorpus);
                    mm.insertTei(teiCorpus, currentRepositoryDocId, currentAnhalyticsId, fulltextAvailable, date);
                }
            }
            if (!HarvestProperties.isProcessByDate()) {
                break;
            }
        }
        logger.info("Done");
    }

    /**
     * Adds data TEI extracted with grobid.
     */
    private Document createTEICorpus(String metadata) {
        Document generatedTeiDoc = null;
        try {
            if (metadata != null) {
                InputStream metadataStream = new ByteArrayInputStream(metadata.getBytes());
                generatedTeiDoc = tb.createTEICorpus(metadataStream);
                metadataStream.close();
            }
        } catch (Exception xpe) {
            xpe.printStackTrace();
        }
        return generatedTeiDoc;
    }

    /*
     Get correponding Metadata to process..anhalyticsId is necessary for identification
     */
    private String getGrobidTei(String anhalyticsId) {
        return mm.findGrobidTeiById(anhalyticsId);
    }
}
