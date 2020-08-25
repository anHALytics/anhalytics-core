package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.annotate.services.PDFQuantitiesService;
import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.data.Processings;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Annotates the PDF with the quantities along with the boudingBoxes.
 */
public class PDFQuantitiesAnnotatorWorker extends AnnotatorWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(PDFQuantitiesAnnotatorWorker.class);

    public PDFQuantitiesAnnotatorWorker(MongoFileManager mongoManager,
            BiblioObject biblioObject) {
        super(mongoManager, biblioObject, MongoCollectionsInterface.PDF_QUANTITIES_ANNOTATIONS);
    }

    @Override
    protected void processCommand() {
        // get all the elements having an attribute id and annotate their text content
        boolean inserted = mm.insertAnnotation(annotateDocument(), annotationsCollection);
        if (inserted) {
            mm.updateBiblioObjectStatus(biblioObject, Processings.PDFQUANTITIES, false);
            LOGGER.info("\t\t " + Thread.currentThread().getName() + ": "
                    + biblioObject.getRepositoryDocId() + " annotated by the QUANTITIES service.");
        } else {
            LOGGER.info("\t\t " + Thread.currentThread().getName() + ": "
                    + biblioObject.getRepositoryDocId() + " error occured trying to annotate with QUANTITIES.");
        }

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

            json.append("{ \"repositoryDocId\" : \"" + biblioObject.getRepositoryDocId()
                    + "\",\"anhalyticsId\" : \"" + biblioObject.getAnhalyticsId()
                    //                    + "\", \"date\" :\"" + date
                    + "\",\"isIndexed\" : \"" + false
                    + "\", \"annotation\" : ");
            String jsonText = null;

            PDFQuantitiesService quantitiesService = new PDFQuantitiesService(biblioObject.getPdf().getStream());
            jsonText = quantitiesService.processPDFQuantities();
            if (jsonText != null) {
                json.append(jsonText).append("}");
            } else {
                json.append("{} }");
            }
            biblioObject.getPdf().getStream().close();
        } catch (Exception ex) {
            LOGGER.error("\t\t " + Thread.currentThread().getName() + ": PDF could not be processed by the quantities extractor: ");
            LOGGER.error("Error: ", ex);
            return null;
        }
        return json.toString();
    }

}
