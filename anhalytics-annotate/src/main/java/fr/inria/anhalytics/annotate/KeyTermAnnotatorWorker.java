package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.annotate.services.KeyTermExtractionService;
import fr.inria.anhalytics.commons.data.TEIFile;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runnable that uses the NERD REST service for annotating HAL TEI
 * documents.Resulting JSON annotations are then stored in MongoDB as persistent
 * storage.
 *
 * The content of every TEI elements having an attribute @xml:id randomly
 * generated will be annotated. The annotations follow a stand-off
 * representation that is using the @xml:id as base and offsets to identified
 * the annotated chunk of text.
 *
 * @author Achraf, Patrice
 */
public class KeyTermAnnotatorWorker extends AnnotatorWorker {

    private static final Logger logger = LoggerFactory.getLogger(KeyTermAnnotatorWorker.class);

    public KeyTermAnnotatorWorker(MongoFileManager mongoManager,
            TEIFile tei,
            String date) {
        super(mongoManager, tei, date, MongoCollectionsInterface.KEYTERM_ANNOTATIONS);
    }

    @Override
    protected void processCommand() {
        try {
            mm.insertAnnotation(annotateDocument(), annotationsCollection);
            logger.info("\t\t " + Thread.currentThread().getName() + ": "+ tei.getRepositoryDocId() + " annotated by the KeyTerm extraction and disambiguation service.");
        } catch (Exception ex) {
            logger.error("\t\t " + Thread.currentThread().getName() + ": TEI could not be processed by the keyterm extractor: " + tei.getRepositoryDocId());
            ex.printStackTrace();
        }
    }

    /**
     * Annotation of a complete document with extracted disambiguated key terms
     */
    @Override
    protected String annotateDocument() {
        // NOTE: the part bellow should be used in the future for improving the keyterm disambiguation 
        // by setting a custom domain context which helps the disambiguation (so don't remove it ;)

        /*List<String> halDomainTexts = new ArrayList<String>();
        List<String> halDomains = new ArrayList<String>();
        List<String> meSHDescriptors = new ArrayList<String>();

        // get the HAL domain 
        NodeList classes = docTei.getElementsByTagName("classCode");
        for (int p = 0; p < classes.getLength(); p++) {
            Node node = classes.item(p);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) (node);
                // filter on attribute @scheme="halDomain"
                String scheme = e.getAttribute("scheme");
                if ((scheme != null) && scheme.equals("halDomain")) {
                    halDomainTexts.add(e.getTextContent());
                    String n_att = e.getAttribute("n");
                    halDomains.add(n_att);
                } else if ((scheme != null) && scheme.equals("mesh")) {
                    meSHDescriptors.add(e.getTextContent());
                }
            }
        }*/
        StringBuffer json = new StringBuffer();
        json.append("{ \"repositoryDocId\" : \"" + tei.getRepositoryDocId()
                + "\",\"anhalyticsId\" : \"" + tei.getAnhalyticsId()
                + "\", \"date\" :\"" + date
                + "\", \"keyterm\" : ");
        String jsonText = null;
        KeyTermExtractionService keyTermService = new KeyTermExtractionService(tei.getTei());
        jsonText = keyTermService.runKeyTermExtraction();
        if (jsonText != null) {
            json.append(jsonText).append("}");
        } else {
            json.append("{} }");
        }
        return json.toString();
    }
}
