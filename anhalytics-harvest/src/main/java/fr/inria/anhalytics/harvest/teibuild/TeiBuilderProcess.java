package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
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
            if (mm.initGrobidTeis(date)) {
                while (mm.hasMoreTeis()) {
                    String grobidTeiString = mm.nextTeiDocument();
                    String uri = mm.getCurrentRepositoryDocId();
                    String anhalyticsId = mm.getCurrentAnhalyticsId();
                    Document generatedTeiDoc = null;
                    if (anhalyticsId.isEmpty()) {
                        logger.info("skipping "+uri+" No anHALytics id provided");
                        continue;
                    }
                    logger.info("\t Building tei for: " + uri);
                    grobidTeiString = Utilities.trimEncodedCharaters(grobidTeiString);
                    generatedTeiDoc = createTEI(anhalyticsId, grobidTeiString);
                    if (generatedTeiDoc != null) {
                        String tei = Utilities.toString(generatedTeiDoc);
                        mm.insertTei(tei, uri, anhalyticsId, date);
                    }

                }
            }
        }
        logger.info("Done");
    }

    /**
     * Adds data TEI extracted with grobid.
     */
    private Document createTEI(String anhalyticsId, String grobidTei) {
        Document generatedTeiDoc = null;
        try {
            if (!mm.isWithFulltext(anhalyticsId)) {
                String metadataTei = mm.findMetadataTeiById(anhalyticsId);
                if (metadataTei != null) {
                    logger.info("\t\t Metadata found, Building tei for: " + mm.getCurrentRepositoryDocId());
                    InputStream metadataTeiStream = new ByteArrayInputStream(metadataTei.getBytes());
                    generatedTeiDoc = TeiBuilder.createTEICorpus(metadataTeiStream);
                    metadataTeiStream.close();
                    generatedTeiDoc = TeiBuilder.addGrobidTeiToTei(Utilities.toString(generatedTeiDoc), grobidTei);
                }
            }
        } catch (Exception xpe) {
            xpe.printStackTrace();
        }
        return generatedTeiDoc;
    }
}
