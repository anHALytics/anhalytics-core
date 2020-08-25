package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.data.BiblioObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Process publications annex with grobid.
 * 
 * @author Achraf
 */
public class GrobidAnnexWorker extends GrobidWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrobidAnnexWorker.class);

    public GrobidAnnexWorker(BiblioObject biblioObject, String date, int start, int end) throws ParserConfigurationException {
        super(biblioObject, start, end);
    }

    @Override
    protected void saveExtractions(String zipDirectoryPath) {
        String tei = null;
        try {
            File directoryPath = new File(zipDirectoryPath);
            if (directoryPath.exists()) {
                File[] files = directoryPath.listFiles();
                if (files != null) {
                    for (final File currFile : files) {

                        if (currFile.getName().toLowerCase().endsWith(".png")) {
                        } else if (currFile.getName().toLowerCase().endsWith(".xml")) {
                        }

                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error: ", e);
        }
    }
}
