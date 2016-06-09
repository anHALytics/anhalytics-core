package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.properties.HarvestProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Achraf
 */
public class GrobidProcess {

    private static final Logger logger = LoggerFactory.getLogger(GrobidProcess.class);

    private MongoFileManager mm;

    public GrobidProcess() throws UnknownHostException {
        this.mm = MongoFileManager.getInstance(false);
    }

    static final public List<String> toBeGrobidified
            = Arrays.asList("ART", "COMM", "OUV", "POSTER", "DOUV", "PATENT", "REPORT", "COUV", "OTHER", "UNDEFINED");

    public void processFulltexts() {
        try {
            if (GrobidService.isGrobidOk()) {
                ExecutorService executor = Executors.newFixedThreadPool(HarvestProperties.getNbThreads());
                int start = -1;
                int end = -1;
                for (String date : Utilities.getDates()) {

                    if (!HarvestProperties.isProcessByDate()) {
                        date = null;
                    }
                    if (mm.initBinaries(date)) {
                        while (mm.hasMoreBinaryDocuments()) {
                            InputStream content = mm.nextBinaryDocument();
                            String currentRepositoryDocId = mm.getCurrentRepositoryDocId();
                            String type = mm.getCurrentDocType();
                            String currentAnhalyticsId = mm.getCurrentAnhalyticsId();
                            String source = mm.getCurrentDocSource();
                            if (toBeGrobidified.contains(type)) {
                                if (currentAnhalyticsId == null || currentAnhalyticsId.isEmpty()) {
                                    logger.info("skipping " + currentRepositoryDocId + " No anHALytics id provided");
                                    continue;
                                }
                                if (!HarvestProperties.isReset()) {
                                    if (mm.isGrobidified(currentRepositoryDocId)) {
                                        logger.info("skipping " + currentRepositoryDocId + " Already grobidified");
                                        continue;
                                    }
                                }

                                try {
                                    if (source.equalsIgnoreCase("hal")) {
                                        start = 2;
                                    }
                                    Runnable worker = new GrobidSimpleFulltextWorker(content, currentRepositoryDocId, currentAnhalyticsId, date, start, end);
                                    executor.execute(worker);
                                } catch (final Exception exp) {
                                    logger.error("An error occured while processing the file " + currentRepositoryDocId
                                            + ". Continuing the process for the other files" + exp.getMessage());
                                }
                                content.close();
                            }
                        }
                    }
                    if (!HarvestProperties.isProcessByDate()) {
                        break;
                    }
                }
                executor.shutdown();
                while (!executor.isTerminated()) {
                }
            }
        } catch (IOException ioex) {
            logger.error(ioex.getMessage(), ioex.getCause());
        }
        logger.info("Finished all threads");
    }

    public void processAnnexes() throws IOException {
        if (GrobidService.isGrobidOk()) {
            ExecutorService executor = Executors.newFixedThreadPool(HarvestProperties.getNbThreads());
            mm.setGridFSCollection(MongoCollectionsInterface.PUB_ANNEXES);
            for (String date : Utilities.getDates()) {
                if (mm.initAnnexes(date)) {
                    while (mm.hasMoreBinaryDocuments()) {
                        InputStream content = mm.nextBinaryDocument();
                        String anhalyticsId = mm.getCurrentAnhalyticsId();
                        if (mm.getCurrentFileType().contains(".pdf")) {
                            String id = mm.getCurrentRepositoryDocId();
                            try {
                                Runnable worker = new GrobidAnnexWorker(content, id, anhalyticsId, date, -1, -1);
                                executor.execute(worker);
                            } catch (final Exception exp) {
                                logger.error("An error occured while processing the file " + id
                                        + ". Continuing the process for the other files" + exp.getMessage());
                            }
                            content.close();
                        }
                    }
                }
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            logger.info("Finished all threads");
        }
    }

    public void addAssetsLegend() {
        try {
            for (String date : Utilities.getDates()) {
                if (mm.initAssets(date)) {
                    while (mm.hasMoreBinaryDocuments()) {
                        String filename = mm.nextAsset();
                        String currentRepositoryId = mm.getCurrentRepositoryDocId();
                        String currentAnhalyticsId = mm.getCurrentAnhalyticsId();
                        System.out.println(currentRepositoryId);
                        String tei = mm.findGrobidTeiById(currentRepositoryId);
                        InputStream teiStream = new ByteArrayInputStream(tei.getBytes());
                        String legend = AssetLegendExtracter.extractLegendFromTei(filename, teiStream);
                        teiStream.close();
                        if (legend != null) {
                            System.out.println(legend);
                            //mm.addLegendToAsset(legend);
                        }
                    }
                }
            }

        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }
}
