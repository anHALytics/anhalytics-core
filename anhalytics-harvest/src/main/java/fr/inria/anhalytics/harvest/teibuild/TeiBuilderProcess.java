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
     * Clean up metadatas and build a corpus tei.
     */
    public void buildTei() {
        for (String date : Utilities.getDates()) {
            if (mm.initMetadataTeis(date)) {
                while (mm.hasMoreTeis()) {
                    String metadataTeiString = mm.nextTeiDocument();
                    String uri = mm.getCurrentRepositoryDocId();
                    String type = mm.getCurrentDocType();
                    String anhalyticsId = mm.getCurrentAnhalyticsId();
                    Document generatedTeiDoc = null;
                    try {
                        logger.info("\t Building tei for: " + uri);
                        InputStream metadataTeiStream = new ByteArrayInputStream(metadataTeiString.getBytes());
                        generatedTeiDoc = TeiBuilder.createTEICorpus(metadataTeiStream);
                        metadataTeiStream.close();
                        mm.insertTei(Utilities.toString(generatedTeiDoc), uri, type, anhalyticsId, date);
                    } catch (Exception xpe) {
                        xpe.printStackTrace();
                    }
                }
            }
        }
        logger.info("Done");
    }

    /**
     * Adds data TEI extracted with grobid.
     */
    public void appendGrobidFulltext() {
        for (String date : Utilities.getDates()) {
            if (mm.initGrobidTeis(date)) {
                while (mm.hasMoreTeis()) {
                    String finalTei = null;
                    String grobidTeiString = mm.nextTeiDocument();
                    String id = mm.getCurrentRepositoryDocId();
                    grobidTeiString = Utilities.trimEncodedCharaters(grobidTeiString);

                    if (!mm.isWithFulltext(id)) {
                        finalTei = mm.findFinalTeiById(id);
                        if (finalTei != null) {
                            Document generatedTeiDoc = null;
                            logger.info("\t Building tei for: " + id);
                            generatedTeiDoc = TeiBuilder.addGrobidTeiToTei(finalTei, grobidTeiString);
                            if (generatedTeiDoc != null) {
                                String generatedTeiString = Utilities.toString(generatedTeiDoc);
                                mm.updateTei(generatedTeiString, id, true);
                            }
                        }
                    }
                }
            }
        }
    }
}
