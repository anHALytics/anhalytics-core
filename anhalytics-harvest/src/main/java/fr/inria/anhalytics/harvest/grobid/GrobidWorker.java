package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.data.BinaryFile;
import fr.inria.anhalytics.harvest.exceptions.GrobidTimeoutException;
import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
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
    protected String date;
    protected BinaryFile bf;
    protected int start = 2;
    protected int end = -1;

    private static final String DOI_PATH = "teiHeader/fileDesc/sourceDesc/biblStruct/idno[@type=\"DOI\"]";

    protected DocumentBuilder docBuilder;

    protected XPath xPath = XPathFactory.newInstance().newXPath();

    public GrobidWorker(BinaryFile bf, String date, int start, int end) throws ParserConfigurationException {
        this.mm = MongoFileManager.getInstance(false);
        this.date = date;
        this.start = start;
        this.end = end;
        this.bf = bf;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);
        docBuilder = docFactory.newDocumentBuilder();
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();
        logger.info(Thread.currentThread().getName() + " Start. Processing = " + bf.getRepositoryDocId());
        processCommand();
        long endTime = System.nanoTime();
        logger.info(Thread.currentThread().getName() + " End. :" + (endTime - startTime) / 1000000 + " ms");
    }

    protected void processCommand() {
        try {
            GrobidService grobidService = new GrobidService(this.start, this.end, true, date);//configured for HAL, first page is added to the document

            String filepath = Utilities.storeTmpFile(bf.getStream());
            File file = new File(filepath);
            double mb = file.length() / (1024 * 1024);

            if (mb <= 15) { // for now we extract just files with less size (avoid thesis..which may take long time)
                logger.info("\t\t Tei extraction for : " + bf.getRepositoryDocId() + " sizing :" + mb + "mb");
                String resultPath = grobidService.runFullTextAssetGrobid(filepath);
                saveExtractions(resultPath);

                FileUtils.deleteDirectory(new File(resultPath));
                logger.info("\t\t " + bf.getRepositoryDocId()+bf.getRepositoryDocVersion() + " processed.");
            } else {
                logger.info("\t\t can't extract tei for : " + bf.getRepositoryDocId() + "size too large : " + mb + "mb");
            }
            file.delete();
        } catch (GrobidTimeoutException e) {
            mm.save(bf.getRepositoryDocId(), "processGrobid", "timed out", date);
            logger.warn("Processing of " + bf.getRepositoryDocId() + " timed out");
        } catch (RuntimeException e) {
            logger.error("\t\t error occurred while processing " + bf.getRepositoryDocId());
            if (e.getMessage().contains("timed out")) {
                mm.save(bf.getRepositoryDocId(), "processGrobid", "timed out", date);
            } else if (e.getMessage().contains("failed")) {
                mm.save(bf.getRepositoryDocId(), "processGrobid", "failed", date);
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
        if (mm.isWithoutDoi(bf.getAnhalyticsId())) {
            try {
                InputStream grobidStream = new ByteArrayInputStream(tei.getBytes());
                Document grobid;
                grobid = docBuilder.parse(grobidStream);
                Element grobidRootElement = grobid.getDocumentElement();
                Node doiNode = (Node) xPath.compile(DOI_PATH)
                        .evaluate(grobidRootElement, XPathConstants.NODE);
                if (doiNode != null) {
                    mm.updateDoi(bf.getAnhalyticsId(), doiNode.getTextContent());
                    logger.info("\t\t DOI of " + bf.getRepositoryDocId() + " saved.");
                }
                grobidStream.close();
            } catch (SAXException ex) {
                logger.error("\t\t error occurred while parsing document to find DOI " + bf.getRepositoryDocId());
            } catch (XPathExpressionException ex) {
                logger.error("\t\t error occurred while parsing document to find DOI " + bf.getRepositoryDocId());
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
        return this.bf.getRepositoryDocId();
    }
}
