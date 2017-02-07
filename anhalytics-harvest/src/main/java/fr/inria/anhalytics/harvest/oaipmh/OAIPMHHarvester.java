package fr.inria.anhalytics.harvest.oaipmh;

import fr.inria.anhalytics.commons.data.BinaryFile;
import fr.inria.anhalytics.commons.data.TEIFile;
import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.HarvesterItf;
import fr.inria.anhalytics.harvest.exceptions.BinaryNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Abstract class to be sub-classes to process a list of TEI objects extracted
 * from OAI-PMH services.
 *
 * @author Achraf
 */
abstract class OAIPMHHarvester implements HarvesterItf {

    protected static final Logger logger = LoggerFactory.getLogger(OAIPMHHarvester.class);

    protected String oai_url = null;

    public OAIPMHHarvester() {
        this.mm = MongoFileManager.getInstance(false);
        this.oai_url = HarvestProperties.getApiUrl();
//        if(this.oai_url.isEmpty() || this.oai_url == null)
//            throw new PropertyException("No API URL is found, check the properties file.");
    }

    protected MongoFileManager mm;

    /**
     * Harvests the documents submitted on the given date.
     */
    protected void fetchDocumentsByDate(String date) throws IOException, SAXException, ParserConfigurationException, ParseException {
    }

    /**
     * Stores the given teis and downloads attachements(main file(s), annexes
     * ..) .
     */
    protected void processTeis(List<TEIFile> teis, String date, boolean withAnnexes) {
        for (TEIFile tei : teis) {
            String teiString = tei.getTei();
            String pdfUrl = "";
            if (tei.getPdfdocument() != null) {
                pdfUrl = tei.getPdfdocument().getUrl();
            }
            String repositoryDocId = tei.getRepositoryDocId();
            logger.info("\t\t Processing TEI for " + repositoryDocId);
            if (teiString.length() > 0) {
                logger.info("\t\t\t\t Storing TEI " + repositoryDocId);
                mm.insertTei(tei, date, MongoCollectionsInterface.METADATAS_TEIS);
                try {
                    if (tei.getPdfdocument() != null) {
                        logger.info("\t\t\t\t downloading PDF file.");
                        requestFile(tei.getPdfdocument(), date);
                    } else {
                        mm.save(tei.getRepositoryDocId(), "harvestProcess", "no URL for binary", date);
                        logger.info("\t\t\t\t PDF not found !");
                    }
                    if (withAnnexes) {
                        downloadAnnexes(tei.getAnnexes(), date);
                    }
                } catch (BinaryNotAvailableException bna) {
                    logger.error(bna.getMessage());
                    mm.save(tei.getRepositoryDocId(), "harvestProcess", "file not downloaded", date);
                } catch (ParseException | IOException e) {
                    logger.error("\t\t Error occured while processing TEI for " + tei.getRepositoryDocId(), e);
                    mm.save(tei.getRepositoryDocId(), "harvestProcess", "harvest error", date);
                }
            } else {
                logger.info("\t\t\t No TEI metadata !!!");
            }
        }
    }

    /**
     * Downloads publication annexes and stores them.
     */
    protected void downloadAnnexes(List<BinaryFile> annexes, String date) throws ParseException, IOException {
        //annexes
        for (BinaryFile file : annexes) {
            requestFile(file, date);
            // diagnose annexes (not found)?
        }
    }

    /**
     * Requests the given file if is not under embargo and register it either as
     * main file or as an annex.
     */
    protected boolean requestFile(BinaryFile bf, String date) throws ParseException, IOException {
        Date embDate = Utilities.parseStringDate(bf.getEmbargoDate());
        Date today = new Date();
        if (embDate.before(today) || embDate.equals(today)) {
            logger.info("\t\t\t Downloading: " + bf.getUrl());
            try {
                bf.setStream(Utilities.request(bf.getUrl()));
            } catch (MalformedURLException | ServiceException se) {
                logger.error(se.getMessage());
                throw new BinaryNotAvailableException();
            }

            if (bf.getStream() == null) {
                mm.log(bf.getRepositoryDocId(), bf.getAnhalyticsId(), bf.getUrl(), bf.getDocumentType(), bf.isIsAnnexFile(), "nostream", date);
            } else {
                if (!bf.isIsAnnexFile()) {
                    mm.insertBinaryDocument(bf, date);
                } else {
                    int n = bf.getUrl().lastIndexOf("/");
                    String filename = bf.getUrl().substring(n + 1);
                    bf.setFileName(filename);
                    logger.info("\t\t\t\t Getting annex file " + filename + " for pub ID :" + bf.getRepositoryDocId());
                    mm.insertAnnexDocument(bf, date);
                }
                bf.getStream().close();
            }

            return true;
        } else {
            mm.log(bf.getRepositoryDocId(), bf.getAnhalyticsId(), bf.getUrl(), bf.getDocumentType(), bf.isIsAnnexFile(), "embargo", bf.getEmbargoDate());
            logger.info("\t\t\t file under embargo !");
            return false;
        }
    }
}
