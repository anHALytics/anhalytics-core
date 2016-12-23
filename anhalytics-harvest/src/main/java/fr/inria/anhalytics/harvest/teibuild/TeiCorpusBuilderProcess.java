package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.data.TEIFile;
import fr.inria.anhalytics.commons.exceptions.DataException;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import fr.inria.anhalytics.harvest.exceptions.UnreachableGrobidServiceException;
import fr.inria.anhalytics.harvest.grobid.GrobidService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Appends available harvested/extracted data to the teiCorpus.
 *
 * @author Achraf
 */
public class TeiCorpusBuilderProcess {

    private static final Logger logger = LoggerFactory.getLogger(TeiCorpusBuilderProcess.class);

    private MongoFileManager mm;

    private TeiBuilder tb;

    public TeiCorpusBuilderProcess() {
        this.mm = MongoFileManager.getInstance(false);
        this.tb = new TeiBuilder();
    }

    /**
     * Clean up metadatas , add grobid TEI and build a corpus tei.
     */
    public void addFulltextToTEICorpus() {
        try {
            if (GrobidService.isGrobidOk()) {
                for (String date : Utilities.getDates()) {
                    if (!HarvestProperties.isProcessByDate()) {
                        date = null;
                    }
                    if (mm.initMetadataTeis(date)) {
                        while (mm.hasMore()) {
                            TEIFile tei = mm.nextTeiDocument();
                            Document generatedTEIcorpus = null;
                            if (tei.getAnhalyticsId() == null || tei.getAnhalyticsId().isEmpty()) {
                                logger.info("skipping " + tei.getRepositoryDocId() + " No anHALytics id provided");
                                continue;
                            }
                            logger.info("\t Building TEI for: " + tei.getRepositoryDocId());
                           tei.setTei(Utilities.trimEncodedCharaters(tei.getTei()));
                            try {
                                String grobidTei = getGrobidTei(tei.getAnhalyticsId());
                                generatedTEIcorpus = tb.addGrobidTEIToTEICorpus(tei.getTei(), grobidTei);
                            } catch (DataException de) {
                                logger.error("No corresponding fulltext TEI was found.");
                            }
                            tei.setTei(Utilities.toString(generatedTEIcorpus));
                            mm.insertTei(tei, date, MongoCollectionsInterface.METADATA_WITHFULLTEXT_TEIS);
                        }
                    }
                    if (!HarvestProperties.isProcessByDate()) {
                        break;
                    }
                }
            }
            logger.info("Done");
        } catch (UnreachableGrobidServiceException ugse) {
            logger.error(ugse.getMessage());
        }
    }



    /*
     Get correponding Metadata to process..anhalyticsId is necessary for identification
     */
    private String getGrobidTei(String anhalyticsId) {
        return mm.findGrobidTeiById(anhalyticsId);
    }
}
