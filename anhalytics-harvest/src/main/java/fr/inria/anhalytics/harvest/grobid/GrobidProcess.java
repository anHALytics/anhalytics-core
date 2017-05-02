package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.data.BinaryFile;
import fr.inria.anhalytics.commons.data.Processings;
import fr.inria.anhalytics.harvest.exceptions.UnreachableGrobidServiceException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes the PDFs using Grobid.
 * @author Achraf
 */
public class GrobidProcess {

    private static final Logger logger = LoggerFactory.getLogger(GrobidProcess.class);

    private MongoFileManager mm;

    public GrobidProcess() {
        this.mm = MongoFileManager.getInstance(false);
    }

    static final public List<String> toBeGrobidified
            = Arrays.asList("ART", "COMM", "OUV", "POSTER", "DOUV", "PATENT", "REPORT", "COUV", "OTHER", "UNDEFINED");

    /**
    * Extracts the TEI using the available PDF.
    */
    public void processFulltexts() {
        BinaryFile bf = null;
        try {
            if (GrobidService.isGrobidOk()) {
                ExecutorService executor = Executors.newFixedThreadPool(HarvestProperties.getNbThreads());
                int start = -1;
                int end = -1;
                if (mm.initObjects(null)) {
                    while (mm.hasMore()) {
                        BiblioObject biblioObject = mm.nextBiblioObject();
//                        if (toBeGrobidified.contains(biblioObject.getPublicationType().split("_")[0])) {

                            if (!biblioObject.getIsWithFulltext()) {
                                logger.info("\t\t No fulltext available for : "+biblioObject.getRepositoryDocId()+", Skipping...");
                                continue;
                            }
                            if (!HarvestProperties.isReset() && mm.isProcessed(Processings.GROBID)) {
                                logger.info("\t\t Already grobidified, Skipping...");
                                continue;
                            }

                            try {
                                bf = new BinaryFile();
                                
                                if (biblioObject.getSource().equalsIgnoreCase("hal")) {
                                    start = 2;
                                }
                                
                                bf.setStream(mm.getFulltext(biblioObject));
                                biblioObject.setPdf(bf);
                                Runnable worker = new GrobidSimpleFulltextWorker(biblioObject, start, end);
                                executor.execute(worker);
                            } catch (ParserConfigurationException exp) {
                                logger.error("An error occured while processing the file " + bf.getRepositoryDocId()
                                        + ". Continuing the process for the other files" + exp.getMessage());
                            }
//                        }
                    }
                }

                executor.shutdown();
                while (!executor.isTerminated()) {
                }
            }
            logger.info("Finished all threads");
        } catch (UnreachableGrobidServiceException ugse) {
            logger.error(ugse.getMessage());
        }
    }
}
