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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Appends available harvested/extracted data to the teiCorpus.
 *
 * @author Achraf
 */
public class TeiCorpusBuilderProcess {

    private static final Logger logger = LoggerFactory.getLogger(TeiCorpusBuilderProcess.class);

    private MongoFileManager mm;

    public TeiCorpusBuilderProcess() {
        this.mm = MongoFileManager.getInstance(false);
    }

    /**
     * Formats the metadata and initializes the TEICorpus Based on the Grobid
     * standard and pub2TEI. Pub2TEI equivalent.
     */
    public void transformMetadata() {

        ExecutorService executor = Executors.newFixedThreadPool(HarvestProperties.getNbThreads());

        if (mm.initObjects(HarvestProperties.getSource().toLowerCase())) {
            while (mm.hasMore()) {
                BiblioObject biblioObject = mm.nextBiblioObject();
                if (!HarvestProperties.isReset() && biblioObject.getIsProcessedByPub2TEI()) {
                    logger.info("\t\t Already transformed, Skipping...  "  + biblioObject.getRepositoryDocId());
                    continue;
                }
                biblioObject.setMetadata(mm.getMetadata(biblioObject));
                Runnable worker = new TeiBuilderWorker(biblioObject, Steps.TRANSFORM);
                executor.execute(worker);
            }
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        logger.info("Finished all threads");
    }

    /**
     * Appends Grobid TEI to preexisting TEICorpus. Completes the missing
     * metadata parts, abstract, keywords, publication date, and authors
     * affiliations.
     */
    public void addGrobidFulltextToTEICorpus() {
        ExecutorService executor = Executors.newFixedThreadPool(HarvestProperties.getNbThreads());
        try {
            if (GrobidService.isGrobidOk()) {
                if (mm.initObjects(null)) {
                    while (mm.hasMore()) {
                        BiblioObject biblioObject = mm.nextBiblioObject();
                        if (!HarvestProperties.isReset() && biblioObject.getIsFulltextAppended()) {
                            logger.info("\t\t Fulltext already appended, Skipping... " + biblioObject.getRepositoryDocId());
                            continue;
                        }
                        //grobid tei and teicorpus with metadata initialisation should be available.
                        if (!biblioObject.getIsProcessedByPub2TEI()) {
                            logger.info("\t\t Metadata TEI not found, first consider creating TEI from metadata, Skipping... " + biblioObject.getRepositoryDocId());
                            continue;
                        }
                        Runnable worker = new TeiBuilderWorker(biblioObject, Steps.APPEND_FULLTEXT);
                        executor.execute(worker);
                    }
                }

            }
            logger.info("Done");
        } catch (UnreachableGrobidServiceException ugse) {
            logger.error(ugse.getMessage());
        }
    }
}
