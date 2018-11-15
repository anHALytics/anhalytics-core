package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import fr.inria.anhalytics.harvest.exceptions.UnreachableGrobidServiceException;
import fr.inria.anhalytics.harvest.grobid.GrobidService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

import static org.apache.commons.lang3.StringUtils.lowerCase;

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

        boolean initResult = false;
        if (HarvestProperties.isReset()) {
            initResult = mm.initObjects(lowerCase(HarvestProperties.getSource()));
        } else {
            initResult = mm.initObjects(lowerCase(HarvestProperties.getSource()), MongoFileManager.ONLY_NOT_PROCESSED_TRANSFORM_METADATA_PROCESS);
        }

        if (initResult) {
            while (mm.hasMore()) {
                BiblioObject biblioObject = mm.nextBiblioObject();
                if (!HarvestProperties.isReset() && biblioObject.getIsProcessedByPub2TEI()) {
                    logger.info("\t\t Already transformed, Skipping...  " + biblioObject.getRepositoryDocId());
                    continue;
                }
                biblioObject.setMetadata(mm.getMetadata(biblioObject));
                Runnable worker = new TeiBuilderWorker(biblioObject, Steps.TRANSFORM);
                executor.execute(worker);
            }
        }

        executor.shutdown();
        logger.info("Jobs done, shutting down thread pool. The executor will wait 1 minutes before forcing off.  ");
        try {
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
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
            if (!GrobidService.isGrobidOk()) {
                return;
            }
            boolean initResult = false;

            if (HarvestProperties.isReset()) {
                initResult = mm.initObjects(null);
            } else {
                initResult = mm.initObjects(null, MongoFileManager.ONLY_NOT_PROCESSED_FULLTEXT_APPEND_PROCESS);
            }

            if (initResult) {
                while (mm.hasMore()) {
                    BiblioObject biblioObject = mm.nextBiblioObject();
                    if (!HarvestProperties.isReset() && biblioObject.getIsFulltextAppended()) {
                        logger.info("\t\t Fulltext already appended, Skipping... " + biblioObject.getRepositoryDocId());
                        continue;
                    }
                    //grobid tei and tei corpus with metadata initialisation should be available.
                    if (!biblioObject.getIsProcessedByPub2TEI()) {
                        logger.info("\t\t Metadata TEI not found, first consider creating TEI from metadata, Skipping... " + biblioObject.getRepositoryDocId());
                        continue;
                    }
                    Runnable worker = new TeiBuilderWorker(biblioObject, Steps.APPEND_FULLTEXT);
                    executor.execute(worker);
                }
            }


            executor.shutdown();
            logger.info("Jobs done, shutting down thread pool. ");
            try {
                if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
            logger.info("Finished all threads");
        } catch (UnreachableGrobidServiceException ugse) {
            logger.error(ugse.getMessage());
        }
    }
}
