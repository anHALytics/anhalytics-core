package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.File;
import java.io.InputStream;
import java.net.UnknownHostException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worker to extract tei/assets from a publication binary.
 * 
 * @author Achraf
 */
public class GrobidFulltextWorker extends GrobidWorker {

    private static final Logger logger = LoggerFactory.getLogger(GrobidFulltextWorker.class);
    
    public GrobidFulltextWorker(InputStream content, String id, String anhalyticsId, String date) throws UnknownHostException {
        super(content, id, anhalyticsId, date);
    }
    
    @Override
    protected void saveExtractions(String resultDirectoryPath) {
        String tei = null;
        try {
            File directoryPath = new File(resultDirectoryPath);
            if (directoryPath.exists()) {
                File[] files = directoryPath.listFiles();
                if (files != null) {
                    for (final File currFile : files) {
                        if (currFile.getName().toLowerCase().endsWith(".png")) {
                            InputStream targetStream = FileUtils.openInputStream(currFile);
                            mm.insertGrobidAssetDocument(targetStream, repositoryDocId, anhalyticsId,currFile.getName(), date);
                            targetStream.close();
                        } else if (currFile.getName().toLowerCase().endsWith(".xml")) {
                            tei = Utilities.readFile(currFile.getAbsolutePath());
                            tei = Utilities.trimEncodedCharaters(tei);
                            tei = generateIdsTeiDoc(tei);
                            System.out.println(repositoryDocId);
                            mm.insertGrobidTei(tei, repositoryDocId, anhalyticsId, date);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex.getCause());
        }
    }
}
