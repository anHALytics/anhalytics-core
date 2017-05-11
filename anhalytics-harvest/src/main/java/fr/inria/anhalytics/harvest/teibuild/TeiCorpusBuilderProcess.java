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
     * Formats the metadata and initializes the TEICorpus Based on the Grobid
     * standard and pub2TEI. Pub2TEI equivalent.
     */
    public void transformMetadata() {
        if (mm.initObjects(HarvestProperties.getSource().toLowerCase())) {
            while (mm.hasMore()) {
                BiblioObject biblioObject = mm.nextBiblioObject();
                if (!HarvestProperties.isReset() && biblioObject.getIsProcessedByPub2TEI()) {
                    logger.info("\t\t Already transformed, Skipping...");
                    continue;
                }
                logger.info("\t\t transforming :" + biblioObject.getRepositoryDocId());
                String metadata = mm.getMetadata(biblioObject);
                Document generatedTEIcorpus = tb.createTEICorpus(metadata);
                if (generatedTEIcorpus != null) {
                    boolean inserted = mm.insertTEIcorpus(Utilities.toString(generatedTEIcorpus), biblioObject.getAnhalyticsId());
                    if (inserted) {
                        biblioObject.setIsProcessedByPub2TEI(Boolean.TRUE);
                        //We re-initialize everything for this new TEI (this is considered the starting point of a new coming entry from source)
                        biblioObject.setIsFulltextAppended(Boolean.FALSE);
                        biblioObject.setIsMined(Boolean.FALSE);
                        biblioObject.setIsIndexed(Boolean.FALSE);
                        mm.updateBiblioObjectStatus(biblioObject, null, true);
                    } else {
                        logger.error("\t\t Problem occured while saving " + biblioObject.getRepositoryDocId() + " corpus TEI.");
                    }
                }
            }
        }
    }

    /**
     * Appends Grobid TEI to preexisting TEICorpus. Completes the missing
     * metadata parts, abstract, keywords, publication date, and authors
     * affiliations.
     */
    public void addGrobidFulltextToTEICorpus() {
        try {
            if (GrobidService.isGrobidOk()) {
                if (mm.initObjects(null)) {
                    while (mm.hasMore()) {
                        BiblioObject biblioObject = mm.nextBiblioObject();
                        Document generatedTEIcorpus = null;
                        if (!HarvestProperties.isReset() && biblioObject.getIsFulltextAppended()) {
                            logger.info("\t\t Fulltext already appended, Skipping...");
                            continue;
                        }
                        //grobid tei and teicorpus with metadata initialisation should be available.
                        if (!biblioObject.getIsProcessedByPub2TEI()) {
                            logger.info("\t\t Metadata TEI not found, first consider creating TEI from metadata, Skipping...");
                            continue;
                        }
                        logger.info("\t Building TEI for: " + biblioObject.getRepositoryDocId());
                        //tei.setTei(Utilities.trimEncodedCharaters(tei.getTei()));
                        try {
                            String grobidTei = mm.getGrobidTei(biblioObject);
                            String TEICorpus = mm.getTEICorpus(biblioObject);

                            generatedTEIcorpus = tb.addGrobidTEIToTEICorpus(TEICorpus, grobidTei);
                        } catch (DataException de) {
                            logger.error("No corresponding fulltext TEI was found.");
                        }
                        boolean inserted = mm.insertTEIcorpus(Utilities.toString(generatedTEIcorpus), biblioObject.getAnhalyticsId());
                        if (inserted) {
                            biblioObject.setIsFulltextAppended(Boolean.TRUE);
                            mm.updateBiblioObjectStatus(biblioObject, null, true);
                        } else {
                            logger.error("\t\t Problem occured while saving " + biblioObject.getRepositoryDocId() + " corpus TEI.");
                        }
                    }
                }

            }
            logger.info("Done");
        } catch (UnreachableGrobidServiceException ugse) {
            logger.error(ugse.getMessage());
        }
    }
}
