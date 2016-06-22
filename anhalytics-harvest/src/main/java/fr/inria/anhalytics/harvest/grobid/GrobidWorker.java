package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.exceptions.GrobidTimeoutException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
    protected InputStream content;
    protected MongoFileManager mm;
    protected String date;
    protected String repositoryDocId;
    protected String anhalyticsId;
    protected int start = 2;
    protected int end = -1;
    
    private static final String DOI_PATH = "teiHeader/fileDesc/sourceDesc/biblStruct/idno[@type=\"DOI\"]";

    protected DocumentBuilder docBuilder;

    protected XPath xPath = XPathFactory.newInstance().newXPath();

    public GrobidWorker(InputStream content, String currentRepositoryDocId, String currentAnhalyticsId, String date, int start, int end) throws UnknownHostException {
        this.content = content;
        this.mm = MongoFileManager.getInstance(false);
        this.date = date;
        this.repositoryDocId = currentRepositoryDocId;
        this.anhalyticsId = currentAnhalyticsId;
        this.start = start;
        this.end = end;

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();
        logger.info(Thread.currentThread().getName() + " Start. Processing = " + repositoryDocId);
        processCommand();
        long endTime = System.nanoTime();
        logger.info(Thread.currentThread().getName() + " End. :" + (endTime - startTime) / 1000000 + " ms");
    }

    protected void processCommand() {
        try {
            GrobidService grobidService = new GrobidService(this.start, this.end, true, date);//configured for HAL, first page is added to the document

            String filepath = Utilities.storeTmpFile(content);
            File file = new File(filepath);
            double mb = file.length() / (1024 * 1024);

            if (mb <= 15) { // for now we extract just files with less size (avoid thesis..which may take long time)
                logger.info("\t\t Tei extraction for : " + repositoryDocId + " sizing :" + mb + "mb");
                String resultPath = grobidService.runFullTextAssetGrobid(filepath);
                saveExtractions(resultPath);

                FileUtils.deleteDirectory(new File(resultPath));
                logger.debug("\t\t " + repositoryDocId + " processed.");
            } else {
                logger.info("\t\t can't extract tei for : " + repositoryDocId + "size too large : " + mb + "mb");
            }
            file.delete();
        } catch (GrobidTimeoutException e) {
            mm.save(repositoryDocId, "processGrobid", "timed out", date);
            logger.warn("Processing of " + repositoryDocId + " timed out");
        } catch (RuntimeException e) {
            logger.error("\t\t error occurred while processing " + repositoryDocId);
            if (e.getMessage().contains("timed out")) {
                mm.save(repositoryDocId, "processGrobid", "timed out", date);
            } else if (e.getMessage().contains("failed")) {
                mm.save(repositoryDocId, "processGrobid", "failed", date);
            }
            logger.error(e.getMessage(), e.getCause());
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex.getCause());
        }
    }

    protected String generateIdsTeiDoc(String tei) {

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

    protected void saveDocumentDOI(String tei) {
        if (mm.isWithoutDoi(anhalyticsId)) {
            try {
                InputStream grobidStream = new ByteArrayInputStream(tei.getBytes());
                Document grobid;
                grobid = docBuilder.parse(grobidStream);
                Element grobidRootElement = grobid.getDocumentElement();
                Node doiNode = (Node) xPath.compile(DOI_PATH)
                        .evaluate(grobidRootElement, XPathConstants.NODE);
                if (doiNode != null) {
                    mm.updateDoi(anhalyticsId, doiNode.getTextContent());
                    logger.debug("\t\t DOI of " + repositoryDocId + " saved.");
                }
                grobidStream.close();
            } catch (SAXException ex) {
                logger.error("\t\t error occurred while parsing document to find DOI " + repositoryDocId);
            } catch (XPathExpressionException ex) {
                logger.error("\t\t error occurred while parsing document to find DOI " + repositoryDocId);
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex.getCause());
            }
        }
    }

    protected void saveExtractions(String resultPath) {
    }

    ;

    @Override
    public String toString() {
        return this.repositoryDocId;
    }
}
