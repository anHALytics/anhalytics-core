package fr.inria.anhalytics.harvest;

import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.data.TEIFile;
import fr.inria.anhalytics.commons.data.BinaryFile;
import fr.inria.anhalytics.harvest.exceptions.BinaryNotAvailableException;
import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.utilities.Utilities;

import java.io.IOException;
import java.util.List;
import java.util.Date;
import java.text.ParseException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for all harvester of the system. A particular harvester will
 * implement a specific harvesting and/or loading protocol for the acquisition 
 * of bibliographical objects (e.g. an article with all its associated resources
 * such as PDF, TEI, metadata header, annexes, suppplementary information), 
 * possibly among several versions (because we are also addressing prepring archives).
 *
 */
public abstract class Harvester {

	protected static final Logger logger = LoggerFactory.getLogger(Harvester.class);

	// Source of the harvesting
	public enum Source {
		HAL		("hal"),
		ISTEX	("istex"),
		ARXIV	("arxiv");
		
		private String name;

		private Source(String name) {
          	this.name = name;
		}

		public String getName() {
			return name;
		}
	};

	protected Source source = null;
	protected MongoFileManager mm = null;

	public Harvester() {
		this.mm = MongoFileManager.getInstance(false);
	}

	abstract public void fetchAllDocuments() throws IOException, SAXException, ParserConfigurationException, ParseException;

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	/**
     * Stores the given teis and downloads attachements(main file(s), annexes
     * ..) .
     */
    protected void processTeis(List<TEIFile> teis, String date, boolean withAnnexes) {
    	if ( (teis == null) || (teis.size() == 0) )
    		return;
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
}