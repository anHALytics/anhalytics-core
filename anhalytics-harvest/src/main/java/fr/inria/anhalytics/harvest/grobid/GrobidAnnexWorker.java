package fr.inria.anhalytics.harvest.grobid;

import java.io.File;
import java.io.InputStream;
import java.net.UnknownHostException;

/**
 * Process publications annex with grobid.
 * 
 * @author Achraf
 */
public class GrobidAnnexWorker extends GrobidWorker {

    public GrobidAnnexWorker(InputStream content, String id, String anhalyticsId, String date) throws UnknownHostException {
        super(content, id, anhalyticsId, date);
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
            e.printStackTrace();
        }
    }
}
