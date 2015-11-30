package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.datamine.HALMiner;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

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
        Document doc_result;
        mm.setGridFS(MongoCollectionsInterface.GROBID_TEIS);
        for (String date : Utilities.getDates()) {
            if (mm.initTeiFiles(date)) {
                logger.debug("Merging documents.. for: " + date);
                while (mm.hasMoreDocuments()) {
                    String tei_doc = mm.nextDocument();
                    String filename = mm.getCurrentFilename();
                    String hal_uri = mm.getCurrentHalURI();
                    logger.debug("\t\t Merging documents.. for: " + filename);
                    tei_doc = Utilities.trimEncodedCharaters(tei_doc);
                    grobid_tei = new ByteArrayInputStream(tei_doc.getBytes());
                    additional_tei = mm.streamFile(filename, MongoCollectionsInterface.ADDITIONAL_TEIS);
                    String docId = null;
                    InputStream final_tei = null;
                    doc_result = TeiBuilder.generateTeiCorpus(additional_tei, grobid_tei);
                    try {
                        //Extract teis Header metadata
                        doc_result = HALMiner.mine(doc_result, hal_uri);
                        docId = HALMiner.getDocId();
                        
                        //System.out.println(docId);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    result = Utilities.toString(doc_result);
                        final_tei = new ByteArrayInputStream(result.getBytes());
                    mm.addDocument(final_tei, filename, docId, MongoCollectionsInterface.FINAL_TEIS, date);
                    grobid_tei.close();
                    additional_tei.close();
                }
            }
        }
    }
}
