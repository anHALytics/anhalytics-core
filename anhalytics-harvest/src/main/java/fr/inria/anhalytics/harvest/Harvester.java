package fr.inria.anhalytics.harvest;

import fr.inria.anhalytics.commons.data.PublicationFile;
import fr.inria.anhalytics.commons.data.TEI;
import fr.inria.anhalytics.commons.exceptions.BinaryNotAvailableException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Abstract class to be sub-classes to process a list
 * of TEI objects extracted from OAI-PMH services.
 * 
 * @author Achraf
 */
abstract class Harvester {

    protected static final Logger logger = LoggerFactory.getLogger(Harvester.class);

    public Harvester() throws UnknownHostException {
        this.mm = MongoFileManager.getInstance(false);
    }

    protected MongoFileManager mm;

    /**
     * Harvests the documents submitted on the given date.
     */
    public void fetchDocumentsByDate(String date) throws IOException, SAXException, ParserConfigurationException, ParseException {
    }

    /**
     * Harvests all the repository.
     */
    public void fetchAllDocuments() throws IOException, SAXException, ParserConfigurationException, ParseException {
    }

    /**
     * Stores the given teis and downloads attachements(main file(s), annexes
     * ..) .
     */
    protected void processTeis(List<TEI> teis, String date, boolean withAnnexes) {
        for (TEI tei : teis) {
            try {
                logger.info("\t\t Processing tei for " + tei.getId());
                String teiString = tei.getTei();
                if (teiString.length() > 0) {
                    logger.info("\t\t\t\t Storing tei " + tei.getId());
                    mm.insertMetadataTei(teiString, tei.getId(), date);

                    if (tei.getPdfdocument() != null) {
                        logger.info("\t\t\t\t downloading PDF file.");
                        requestFile(tei.getPdfdocument(), tei.getId(), date);
                    } else {
                        mm.save(tei.getId(), "harvestProcess", "no url for binary", date);
                        logger.info("\t\t\t\t PDF not found !");
                    }
                    if (withAnnexes) {
                        downloadAnnexes(tei.getAnnexes(), tei.getId(), date);
                    }
                } else {
                    logger.info("\t\t\t No tei metadata !!!");
                }
            } catch (BinaryNotAvailableException bna) {
                mm.save(tei.getId(), "harvestProcess", "file not downloaded", date);
            } catch (Exception e) {
                e.printStackTrace();
                mm.save(tei.getId(), "harvestProcess", "harvest error", date);
                logger.error("\t\t Error occured while processing tei for " + tei.getId());
            }
        }
    }

    /**
     * Downloads publication annexes and stores them.
     */
    protected void downloadAnnexes(List<PublicationFile> annexes, String id, String date) throws ParseException, IOException {
        //annexes
        for (PublicationFile file : annexes) {
            requestFile(file, id, date);
            // diagnose annexes (not found)?
        }
    }

    /**
     * Requests the given file if is not under embargo and register it either as
     * main file or as an annex.
     */
    protected boolean requestFile(PublicationFile file, String id, String date) throws ParseException, IOException {
        InputStream inBinary = null;
        Date embDate = Utilities.parseStringDate(file.getEmbargoDate());
        Date today = new Date();
        if (embDate.before(today) || embDate.equals(today)) {
            logger.info("\t\t\t Downloading: " + file.getUrl());
            inBinary = Utilities.request(file.getUrl(), false);
            if (inBinary == null) {
                mm.saveForLater(id, file.getUrl(), file.isAnnexFile(), "nostream", date);
            } else {
                if (!file.isAnnexFile()) {
                    mm.insertBinaryDocument(inBinary, id, date);
                } else {
                    int n = file.getUrl().lastIndexOf("/");
                    String filename = file.getUrl().substring(n + 1);
                    logger.debug("\t\t\t\t Getting annex file " + filename + " for pub Id :" + id);
                    mm.insertAnnexDocument(inBinary, id, filename, date);
                }
            }
            inBinary.close();
            return true;
        } else {
            mm.saveForLater(id, file.getUrl(), file.isAnnexFile(), "embargo", file.getEmbargoDate());
            logger.info("\t\t\t file under embargo !");
            return false;
        }
    }
}
