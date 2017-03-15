package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.annotate.services.PDFQuantitiesService;
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
        // get all the elements having an attribute id and annotate their text content
        mm.insertAnnotation(annotateDocument(), annotationsCollection);
        logger.info("\t\t " + Thread.currentThread().getName() + ": " + 
            file.getRepositoryDocId() + " annotated by the QUANTITIES service.");
           
    }

    @Override
    protected String annotateDocument() {
        StringBuffer json = new StringBuffer();
        try {
            /*String filepath = Utilities.storeTmpFile(((IstexFile)file).getStream());
            try {
                ((IstexFile)file).getStream().close();
            } catch (IOException ex) {
                throw new DataException("File stream can't be closed.", ex);
            }*/

            json.append("{ \"repositoryDocId\" : \"" + file.getRepositoryDocId()
 //                   + "\", \"category\" :\"" + ((IstexFile)file).getCategory()
                    + "\", \"measurements\" : ");
            String jsonText = null;
            //QuantitiesService quantitiesService = new QuantitiesService(filepath);
            if (((TEIFile)file).getPdfdocument() == null) {
                logger.info("\t\t " + Thread.currentThread().getName() + " PDF not found for " + file.getRepositoryDocId());
                return null;
            }

            PDFQuantitiesService quantitiesService = new PDFQuantitiesService(((TEIFile)file).getPdfdocument().getStream());
            jsonText = quantitiesService.processPDFQuantities();
            if (jsonText != null) {
                json.append(jsonText).append("}");
            } else {
                json.append("{} }");
            }
        } catch (Exception ex) {
            logger.error("\t\t " + Thread.currentThread().getName() + ": TEI could not be processed by the keyterm extractor: ");
            ex.printStackTrace();
        }
        return json.toString();
    }

}
