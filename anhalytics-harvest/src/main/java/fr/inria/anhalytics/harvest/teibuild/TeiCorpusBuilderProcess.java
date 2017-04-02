package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.exceptions.DataException;
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
     * Initialize the teiCorpus.
     */
    public void transformMetadata() {
        if (mm.initObjects()) {
            while (mm.hasMore()) {
                BiblioObject biblioObject = mm.nextBiblioObject();
                if (!HarvestProperties.isReset() && biblioObject.getIsProcessedByPub2TEI()) {
                    logger.info("\t\t Already transformed, Skipping...");
                    continue;
                }
                Document generatedTEIcorpus = null;
                String metadata = mm.getMetadata(biblioObject);
                generatedTEIcorpus = tb.createTEICorpus(metadata);
                mm.insertTEIcorpus(Utilities.toString(generatedTEIcorpus), biblioObject.getAnhalyticsId());
                biblioObject.setIsProcessedByPub2TEI(Boolean.TRUE);
                //We re-initialize everything
                biblioObject.setIsIndexed(Boolean.FALSE);
                biblioObject.setIsProcessedByGrobid(Boolean.FALSE);
                biblioObject.setIsProcessedByNerd(Boolean.FALSE);
                biblioObject.setIsProcessedByKeyterm(Boolean.FALSE);
                biblioObject.setIsProcessedByTextQuantities(Boolean.FALSE);
                biblioObject.setIsProcessedByPDFQuantities(Boolean.FALSE);
                mm.updateBiblioObjectStatus(biblioObject);
            }
        }
    }

    /**
     * Clean up metadatas , add grobid TEI and build a corpus tei.
     */
    public void addGrobidFulltextToTEICorpus() {
        try {
            if (GrobidService.isGrobidOk()) {
                    if (mm.initObjects()) {
                        while (mm.hasMore()) {
                            BiblioObject biblioObject = mm.nextBiblioObject();
                            Document generatedTEIcorpus = null;
                            //grobid tei and teicorpus with metadata initialisation should be available.
                            if(!biblioObject.getIsProcessedByPub2TEI() || !biblioObject.getIsProcessedByGrobid()){
                                logger.info("\t\t Metadata TEI or Grobid TEI not found, first consider creating TEI from metadata and extracting TEI using Grobid, Skipping...");
                                continue;
                            }
                            logger.info("\t Building TEI for: " + biblioObject.getRepositoryDocId());
                            //tei.setTei(Utilities.trimEncodedCharaters(tei.getTei()));
                            try {
                                String TEICorpus = mm.getTEICorpus(biblioObject);
                                String grobidTei = mm.getGrobidTei(biblioObject);
                                generatedTEIcorpus = tb.addGrobidTEIToTEICorpus(TEICorpus, grobidTei);
                            } catch (DataException de) {
                                logger.error("No corresponding fulltext TEI was found.");
                            }
//                            tei.setTei(Utilities.toString(generatedTEIcorpus));
                            mm.insertTEIcorpus(Utilities.toString(generatedTEIcorpus), biblioObject.getAnhalyticsId());
                        }
                    }
                
            }
            logger.info("Done");
        } catch (UnreachableGrobidServiceException ugse) {
            logger.error(ugse.getMessage());
        }
    }
}
