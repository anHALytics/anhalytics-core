package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.data.BiblioObject;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worker to extract tei/assets from a publication binary.
 * 
 * @author Achraf
 */
public class GrobidFulltextWorker extends GrobidWorker {

    private static final Logger logger = LoggerFactory.getLogger(GrobidFulltextWorker.class);
    
    public GrobidFulltextWorker(BiblioObject biblioObject, String date, int start, int end) throws ParserConfigurationException {
        super(biblioObject, start, end);
    }
    
//    @Override
//    protected void saveExtractions(String resultDirectoryPath) {
//        String tei = null;
//        try {
//            File directoryPath = new File(resultDirectoryPath);
//            if (directoryPath.exists()) {
//                File[] files = directoryPath.listFiles();
//                if (files != null) {
//                    for (final File currFile : files) {
//                        if (currFile.getName().toLowerCase().endsWith(".png")) {
//                            InputStream targetStream = FileUtils.openInputStream(currFile);
//                            mm.insertGrobidAssetDocument(targetStream, repositoryDocId, anhalyticsId,currFile.getName(), date);
//                            targetStream.close();
//                        } else if (currFile.getName().toLowerCase().endsWith(".xml")) {
//                            tei = Utilities.readFile(currFile.getAbsolutePath());
//                            tei = Utilities.trimEncodedCharaters(tei);
//                            tei = generateIdsTeiDoc(tei);
//                            System.out.println(repositoryDocId);
//                            mm.insertGrobidTei(tei, repositoryDocId, anhalyticsId, date);
//                        }
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            logger.error(ex.getMessage(), ex.getCause());
//        }
//    }
}
