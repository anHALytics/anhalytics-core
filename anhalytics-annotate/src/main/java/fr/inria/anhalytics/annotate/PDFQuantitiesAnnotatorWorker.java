package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.annotate.services.PDFQuantitiesService;
import fr.inria.anhalytics.commons.data.IstexFile;
import fr.inria.anhalytics.commons.data.TEIFile;
import fr.inria.anhalytics.commons.exceptions.DataException;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.commons.data.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author azhar
 */
public class PDFQuantitiesAnnotatorWorker extends AnnotatorWorker {

    private static final Logger logger = LoggerFactory.getLogger(PDFQuantitiesAnnotatorWorker.class);

    public PDFQuantitiesAnnotatorWorker(MongoFileManager mongoManager,
            File tei,
            String date) {
        super(mongoManager, tei, null, MongoCollectionsInterface.QUANTITIES_ANNOTATIONS);
    }

    @Override
    protected void processCommand() {
        try {
            /*String filepath = Utilities.storeTmpFile(((IstexFile)file).getStream());
            try {
                ((IstexFile)file).getStream().close();
            } catch (IOException ex) {
                throw new DataException("File stream can't be closed.", ex);
            }*/

            StringBuffer json = new StringBuffer();
            json.append("{ \"repositoryDocId\" : \"" + file.getRepositoryDocId()
 //                   + "\", \"category\" :\"" + ((IstexFile)file).getCategory()
                    + "\", \"quantities\" : ");
            String jsonText = null;
            //QuantitiesService quantitiesService = new QuantitiesService(filepath);
            if (((TEIFile)file).getPdfdocument() == null) {
                logger.info("\t\t " + Thread.currentThread().getName() + " PDF not found for " + file.getRepositoryDocId());
                return;
            }

            PDFQuantitiesService quantitiesService = new PDFQuantitiesService(((TEIFile)file).getPdfdocument().getStream());
            jsonText = quantitiesService.processPDFQuantities();
            if (jsonText != null) {
                json.append(jsonText).append("}");
            } else {
                json.append("{} }");
            }
            mm.insertQuantitiesAnnotation(json.toString(), annotationsCollection);
            logger.info("\t\t " + Thread.currentThread().getName() + " annotated by the Quantities extraction.");
        } catch (Exception ex) {
            logger.error("\t\t " + Thread.currentThread().getName() + ": TEI could not be processed by the keyterm extractor: ");
            ex.printStackTrace();
        }
    }

    @Override
    protected String annotateDocument() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
