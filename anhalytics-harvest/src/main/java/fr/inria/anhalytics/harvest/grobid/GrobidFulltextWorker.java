package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.managers.MongoManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;

/**
 * Worker to extract tei/assets from a publication binary.
 * @author Achraf
 */
public class GrobidFulltextWorker extends GrobidWorker {

    public GrobidFulltextWorker(InputStream content, MongoManager mongoManager, String grobidHost, String grobidPort, String date) {
        super(content, mongoManager, grobidHost, grobidPort, date);
    }
    
    @Override
    protected void storeToGridfs(String zipDirectoryPath) {
        String tei = null;
        try {
            File directoryPath = new File(zipDirectoryPath);
            if (directoryPath.exists()) {
                File[] files = directoryPath.listFiles();
                if (files != null) {
                    for (final File currFile : files) {
                        if (currFile.getName().toLowerCase().endsWith(".png")) {
                            InputStream targetStream = FileUtils.openInputStream(currFile);
                            mm.addAssetDocument(targetStream, Utilities.getHalIDFromFilename(filename), currFile.getName(), MongoManager.GROBID_ASSETS, date);
                            targetStream.close();
                        } else if (currFile.getName().toLowerCase().endsWith(".xml")) {
                            tei = Utilities.readFile(currFile.getAbsolutePath());
                            tei = Utilities.trimEncodedCharaters(tei);
                            System.out.println(filename);
                            mm.addDocument(new ByteArrayInputStream(tei.getBytes()), filename.substring(0, filename.indexOf("."))+".tei.xml", MongoManager.GROBID_TEIS, date);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
