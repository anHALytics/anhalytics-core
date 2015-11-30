package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
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

    public GrobidFulltextWorker(InputStream content, MongoFileManager mongoManager,String date) {
        super(content, mongoManager, date);
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
                            mm.addAssetDocument(targetStream, Utilities.getHalIDFromFilename(filename), currFile.getName(), MongoCollectionsInterface.GROBID_ASSETS, date);
                            targetStream.close();
                        } else if (currFile.getName().toLowerCase().endsWith(".xml")) {
                            tei = Utilities.readFile(currFile.getAbsolutePath());
                            tei = Utilities.trimEncodedCharaters(tei);
                            System.out.println(filename);
                            mm.addDocument(new ByteArrayInputStream(tei.getBytes()), "", filename.substring(0, filename.indexOf("."))+".tei.xml", MongoCollectionsInterface.GROBID_TEIS, date);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
