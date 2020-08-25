package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.harvest.exceptions.GrobidTimeoutException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Achraf
 */
abstract class GrobidWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GrobidWorker.class);
    protected MongoFileManager mm;
    protected BiblioObject biblioObject;
    File file = null;
    protected int start = 2;
    protected int end = -1;

    private static final String DOI_PATH = "teiHeader/fileDesc/sourceDesc/biblStruct/idno[@type=\"DOI\"]";

    public GrobidWorker(BiblioObject biblioObject, int start, int end) throws ParserConfigurationException {
        this.mm = MongoFileManager.getInstance(false);
        this.start = start;
        this.end = end;
        this.biblioObject = biblioObject;
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();
        logger.info(Thread.currentThread().getName() + " Start. Processing = " + biblioObject.getRepositoryDocId());
        processCommand();
        long endTime = System.nanoTime();
        logger.info(Thread.currentThread().getName() + " End. :" + (endTime - startTime) / 1000000 + " ms");
    }

    protected void processCommand() {
        try {
            GrobidService grobidService = new GrobidService(this.start, this.end, true);//configured for HAL, first page is added to the document

            String filepath = Utilities.storeTmpFile(biblioObject.getPdf().getStream());
            File file = new File(filepath);
            double mb = file.length() / (1024 * 1024);

            if (mb <= 15) { // for now we extract just files with less size (avoid thesis..which may take long time)
                logger.info("\t\t Tei extraction for : " + biblioObject.getRepositoryDocId() + " sizing :" + mb + "mb");
                String resultPath = grobidService.runFullTextAssetGrobid(filepath);
                saveExtractions(resultPath);

                FileUtils.deleteDirectory(new File(resultPath));
                logger.info("\t\t " + biblioObject.getRepositoryDocId()+biblioObject.getRepositoryDocVersion() + " processed.");
            } else {
                logger.info("\t\t can't extract tei for : " + biblioObject.getRepositoryDocId() + "size too large : " + mb + "mb");
            }
            file.delete();
        } catch (GrobidTimeoutException e) {
            mm.save(biblioObject.getRepositoryDocId(), "processGrobid", "timed out");
            logger.warn("Processing of " + biblioObject.getRepositoryDocId() + " timed out");
        } catch (RuntimeException e) {
            logger.error("\t\t error occurred while processing " + biblioObject.getRepositoryDocId());
            if (e.getMessage().contains("timed out")) {
                mm.save(biblioObject.getRepositoryDocId(), "processGrobid", "timed out");
            } else if (e.getMessage().contains("failed")) {
                mm.save(biblioObject.getRepositoryDocId(), "processGrobid", "failed");
            }
            logger.error(e.getMessage(), e.getCause());
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex.getCause());
        }
    }

    protected String generateIdsGrobidTeiDoc(String tei) {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        Document teiDoc = null;
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            teiDoc = docBuilder.parse(new ByteArrayInputStream(tei.getBytes()));

        } catch (Exception e) {
            e.printStackTrace();
        }
        Utilities.generateIDs(teiDoc);
        tei = Utilities.toString(teiDoc);
        return tei;
    }

    protected void saveExtractedDOI(String tei) {
        if (biblioObject.getDoi() == null) {
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                docFactory.setValidating(false);
                //docFactory.setNamespaceAware(true);
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                XPath xPath = XPathFactory.newInstance().newXPath();

                InputStream grobidStream = new ByteArrayInputStream(tei.getBytes());
                Document grobid = docBuilder.parse(grobidStream);
                Element grobidRootElement = grobid.getDocumentElement();
                Node doiNode = (Node) xPath.compile(DOI_PATH)
                        .evaluate(grobidRootElement, XPathConstants.NODE);
                if (doiNode != null) {
                    biblioObject.setDoi(doiNode.getTextContent());
                    logger.info("\t\t DOI of " + biblioObject.getRepositoryDocId() + " saved.");
                }
                grobidStream.close();
            } catch (SAXException | XPathExpressionException ex) {
                logger.error("\t\t error occurred while parsing document to find DOI " + biblioObject.getRepositoryDocId());
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex.getCause());
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        boolean success = false;
        if(file.exists()) {
            success = file.delete();
            if (!success) {
                logger.error(
                        "Deletion of temporary image files failed for file '" + file.getAbsolutePath() + "'");
            }else
                logger.info("\t\t "+Thread.currentThread().getName() +" :"+ file.getAbsolutePath()  +" deleted.");
        }
    }

    protected void saveExtractions(String resultPath) {
    }

    ;

    @Override
    public String toString() {
        return this.biblioObject.getRepositoryDocId();
    }
}
