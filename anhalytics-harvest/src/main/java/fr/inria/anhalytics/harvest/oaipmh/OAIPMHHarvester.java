package fr.inria.anhalytics.harvest.oaipmh;

import fr.inria.anhalytics.commons.data.PublicationFile;
import fr.inria.anhalytics.commons.data.TEI;
import fr.inria.anhalytics.commons.exceptions.ServiceException;
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
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Abstract class to be sub-classes to process a list
 * of TEI objects extracted from OAI-PMH services.
 *
 * @author Achraf
 */
abstract class OAIPMHHarvester implements HarvesterItf {

    protected static final Logger logger = LoggerFactory.getLogger(OAIPMHHarvester.class);

    protected String oai_url = null;

    public OAIPMHHarvester() {
        try {
            this.mm = MongoFileManager.getInstance(false);
        } catch (ServiceException ex) {
            throw new ServiceException("MongoDB is not UP, the process will be halted.");
        }
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
    protected void processTeis(List<TEI> teis, String date, boolean withAnnexes) {
        for (TEI tei : teis) {
            try {
                String teiString = tei.getTei();
                String doi = tei.getDoi();
                String pdfUrl = "";
                if (tei.getPdfdocument() != null)
                    pdfUrl = tei.getPdfdocument().getUrl();
                String repositoryDocId = tei.getRepositoryDocId();
                logger.info("\t\t Processing TEI for " + repositoryDocId);
                if (teiString.length() > 0) {
                    logger.info("\t\t\t\t Storing TEI " + repositoryDocId);
                    mm.insertMetadataTei(teiString, doi, pdfUrl, HarvestProperties.getSource(), repositoryDocId, tei.getDocumentType(), date);

                    if (tei.getPdfdocument() != null) {
                        logger.info("\t\t\t\t downloading PDF file.");
                        requestFile(tei.getPdfdocument(), repositoryDocId, tei.getDocumentType(), date);
                    } else {
                        mm.save(tei.getRepositoryDocId(), "harvestProcess", "no URL for binary", date);
                        logger.info("\t\t\t\t PDF not found !");
                    }
                    if (withAnnexes) {
                        downloadAnnexes(tei.getAnnexes(), tei.getRepositoryDocId(), date);
                    }
                } else {
                    logger.info("\t\t\t No TEI metadata !!!");
                }
            } catch (BinaryNotAvailableException bna) {
                logger.error(bna.getMessage());
                mm.save(tei.getRepositoryDocId(), "harvestProcess", "file not downloaded", date);
            } catch (ParseException | IOException e) {
                mm.save(tei.getRepositoryDocId(), "harvestProcess", "harvest error", date);
                logger.error("\t\t Error occured while processing TEI for " + tei.getRepositoryDocId(), e);
            }
        }
    }

    /**
     * Downloads publication annexes and stores them.
     */
    protected void downloadAnnexes(List<PublicationFile> annexes, String repositoryDocId, String date) throws ParseException, IOException {
        //annexes
        for (PublicationFile file : annexes) {
            requestFile(file, repositoryDocId, "annex", date);
            // diagnose annexes (not found)?
        }
    }

    /**
     * Requests the given file if is not under embargo and register it either as
     * main file or as an annex.
     */
    protected boolean requestFile(PublicationFile file, String repositoryDocId, String type, String date) throws ParseException, IOException {
        InputStream in = null;
        Date embDate = Utilities.parseStringDate(file.getEmbargoDate());
        Date today = new Date();
        if (embDate.before(today) || embDate.equals(today)) {
            logger.info("\t\t\t Downloading: " + file.getUrl());
            in = Utilities.request(file.getUrl(), false);
            if (in == null) {
                mm.log(repositoryDocId, file.getUrl(), type, file.isAnnexFile(), "nostream", date);
            } else {
                if (!file.isAnnexFile()) {
                    mm.insertBinaryDocument(in, HarvestProperties.getSource(), repositoryDocId, type, date);
                } else {
                    int n = file.getUrl().lastIndexOf("/");
                    String filename = file.getUrl().substring(n + 1);
                    logger.info("\t\t\t\t Getting annex file " + filename + " for pub ID :" + repositoryDocId);
                    mm.insertAnnexDocument(in, HarvestProperties.getSource(), repositoryDocId, filename, date);
                }
                in.close();
            }

            return true;
        } else {
            mm.log(repositoryDocId, file.getUrl(), type, file.isAnnexFile(), "embargo", file.getEmbargoDate());
            logger.info("\t\t\t file under embargo !");
            return false;
        }
    }
}
