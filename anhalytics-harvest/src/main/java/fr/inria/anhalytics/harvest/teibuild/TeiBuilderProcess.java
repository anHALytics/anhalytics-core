package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Achraf
 */
public class TeiBuilderProcess {

    private static final Logger logger = LoggerFactory.getLogger(TeiBuilderProcess.class);

    private MongoFileManager mm;

    public TeiBuilderProcess() throws UnknownHostException {
        this.mm = MongoFileManager.getInstance(false);
    }

    public void build() throws ParserConfigurationException, IOException {
        InputStream grobid_tei = null;
        InputStream additional_tei = null;
        String result;
        mm.setGridFS(MongoCollectionsInterface.GROBID_TEIS);
        for (String date : Utilities.getDates()) {
            if (mm.initTeiFiles(date)) {
                logger.debug("Merging documents.. for: " + date);
                while (mm.hasMoreDocuments()) {
                    String tei_doc = mm.nextDocument();
                    String filename = mm.getCurrentFilename();
                    logger.debug("\t\t Merging documents.. for: " + filename);
                    tei_doc = Utilities.trimEncodedCharaters(tei_doc);
                    grobid_tei = new ByteArrayInputStream(tei_doc.getBytes());
                    additional_tei = mm.streamFile(filename, MongoCollectionsInterface.ADDITIONAL_TEIS);
                    
                    try {
                        result = TeiBuilder.generateTeiCorpus(additional_tei, grobid_tei, true);
                        InputStream tei = new ByteArrayInputStream(result.getBytes());
                        mm.addDocument(tei, filename, MongoCollectionsInterface.FINAL_TEIS, date);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    grobid_tei.close();
                    additional_tei.close();
                }
            }
        }
    }
}
