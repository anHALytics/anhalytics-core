package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.properties.HarvestProperties;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
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

    public TeiBuilderProcess() throws UnknownHostException {
        this.mm = MongoFileManager.getInstance(false);
    }

    /**
     * Clean up metadatas , add grobid TEI and build a corpus tei.
     */
    public void buildTei() {
        for (String date : Utilities.getDates()) {
            if (!HarvestProperties.isProcessByDate()) {
                date = null;
            }
            if (mm.initMetadataTeis(date)) {
                while (mm.hasMoreTeis()) {
                    String metadataTeiString = mm.nextTeiDocument();
                    String uri = mm.getCurrentRepositoryDocId();
                    String anhalyticsId = mm.getCurrentAnhalyticsId();
                    boolean fulltextAvailable = false;
                    Document generatedTeiDoc = null;
                    if (anhalyticsId == null || anhalyticsId.isEmpty()) {
                        logger.info("skipping " + uri + " No anHALytics id provided");
                        continue;
                    }
                    logger.info("\t Building tei for: " + uri);
                    metadataTeiString = Utilities.trimEncodedCharaters(metadataTeiString);
                    generatedTeiDoc = createTEI(metadataTeiString);

                    String grobidTei = getGrobidTei(anhalyticsId);
                    if (grobidTei != null) {
                        generatedTeiDoc = TeiBuilder.addGrobidTeiToTei(Utilities.toString(generatedTeiDoc), grobidTei);
                        fulltextAvailable = true;
                    }
                    String tei = Utilities.toString(generatedTeiDoc);
                    mm.insertTei(tei, uri, anhalyticsId, fulltextAvailable, date);
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
    private Document createTEI(String metadataTei) {
        Document generatedTeiDoc = null;
        try {
            if (metadataTei != null) {
                InputStream metadataTeiStream = new ByteArrayInputStream(metadataTei.getBytes());
                generatedTeiDoc = TeiBuilder.createTEICorpus(metadataTeiStream);
                metadataTeiStream.close();
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
