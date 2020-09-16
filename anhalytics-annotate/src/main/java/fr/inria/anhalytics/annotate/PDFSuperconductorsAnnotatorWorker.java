package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.annotate.services.PDFQuantitiesService;
import fr.inria.anhalytics.annotate.services.PDFSuperconductorsService;
import fr.inria.anhalytics.commons.data.AnnotatorType;
import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Annotates the PDF with the quantities along with the boudingBoxes.
 */
public class PDFSuperconductorsAnnotatorWorker extends AnnotatorWorker {

    private static final Logger logger = LoggerFactory.getLogger(PDFSuperconductorsAnnotatorWorker.class);

    public PDFSuperconductorsAnnotatorWorker(MongoFileManager mongoManager,
                                             BiblioObject biblioObject) {
        super(mongoManager, biblioObject, MongoCollectionsInterface.SUPERCONDUCTORS_PDF_ANNOTATIONS);
    }

    @Override
    protected void processCommand() {
        // get all the elements having an attribute id and annotate their text content
        boolean inserted = mm.insertAnnotation(annotateDocument(), annotationsCollection);
        if (inserted) {
            mm.updateBiblioObjectStatus(biblioObject, AnnotatorType.SUPERCONDUCTORS_PDF, false);
            logger.info("\t\t " + Thread.currentThread().getName() + ": "
                    + biblioObject.getRepositoryDocId() + " annotated by the SUPERCONDUCTORS service.");
        } else {
            logger.info("\t\t " + Thread.currentThread().getName() + ": "
                    + biblioObject.getRepositoryDocId() + " error occurred trying to annotate with QUANTITIES.");
        }

    }

    @Override
    protected String annotateDocument() {
        StringBuffer json = new StringBuffer();
        try {

            json.append("{ \"repositoryDocId\" : \"" + biblioObject.getRepositoryDocId()
                    + "\",\"anhalyticsId\" : \"" + biblioObject.getAnhalyticsId()
                    //                    + "\", \"date\" :\"" + date
                    + "\",\"isIndexed\" : \"" + false
                    + "\", \"annotation\" : ");
            String jsonText = null;

            PDFSuperconductorsService superconductorsService = new PDFSuperconductorsService(biblioObject.getPdf().getStream());
            jsonText = superconductorsService.processPDFQuantities();
            if (jsonText != null) {
                json.append(jsonText).append("}");
            } else {
                json.append("{} }");
            }
            biblioObject.getPdf().getStream().close();
        } catch (Exception ex) {
            logger.error("\t\t " + Thread.currentThread().getName() + ": PDF could not be processed by the superconductors extractor: ", ex);
            return null;
        }
        return json.toString();
    }

}
