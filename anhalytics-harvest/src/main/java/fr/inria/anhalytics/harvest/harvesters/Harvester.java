package fr.inria.anhalytics.harvest.harvesters;

import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.data.BinaryFile;
import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.exceptions.BinaryNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Abstract class for all harvester of the system. A particular harvester will
 * implement a specific harvesting and/or loading protocol for the acquisition
 * of bibliographical objects (e.g. an article with all its associated resources
 * such as PDF, TEI, metadata header, annexes, suppplementary information),
 * possibly among several versions (because we are also addressing prepring
 * archives).
 *
 */
public abstract class Harvester {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Harvester.class);

    protected List<BiblioObject> grabbedObjects = new ArrayList<>();

    // Source of the harvesting
    public enum Source {
        HAL("hal"),
        ISTEX("istex"),
        ARXIV("arxiv"),
        CUSTOM("local");

        private String name;

        private Source(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
        
        public static boolean contains(String test) {
            for (Source c : Source.values()) {
                if (c.name().toLowerCase().equals(test)) {
                    return true;
                }
            }
            return false;
        }
    };

    protected Source source = null;
    protected MongoFileManager mm = null;

    public Harvester() {
        this.mm = MongoFileManager.getInstance(false);
    }

    abstract public void fetchAllDocuments();

    
    abstract public void fetchListDocuments() ;
    
    abstract public void sample();
    
    
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
    protected void saveObjects() {
        if (CollectionUtils.isEmpty(grabbedObjects)) {
            return;
        }
        for (BiblioObject object : grabbedObjects) {
            String metadataString = object.getMetadata();
            String pdfUrl = "";
            if (object.getPdf() != null) {
                pdfUrl = object.getPdf().getUrl();
            }
            String repositoryDocId = object.getRepositoryDocId();
            LOGGER.info("\t\t Processing metadata from " + object.getSource() + " document :" + repositoryDocId);
            if (metadataString.length() > 0) {
                if (!HarvestProperties.isReset() && mm.isSavedObject(repositoryDocId, object.getRepositoryDocVersion())) {
                    LOGGER.info("\t\t Already grabbed, Skipping...");
                    continue;
                }
                try {
                    if (object.getPdf() != null) {
                        if (object.getPdf().getStream() == null) {
                            LOGGER.info("\t\t\t\t downloading PDF file.");
                            requestFile(object.getPdf());
                            if (object.getPdf().getStream() == null)
                                object.setIsWithFulltext(Boolean.FALSE);
                        } else {
                            object.setIsWithFulltext(Boolean.TRUE);
                        }
                    } else {
                        object.setIsWithFulltext(Boolean.FALSE);
                        mm.save(object.getRepositoryDocId(), "harvestProcess", "no URL for binary");
                        LOGGER.info("\t\t\t\t PDF not found !");
                    }
                    if (object.getAnnexes() != null) {
                        for (BinaryFile file : object.getAnnexes()) {
                            requestFile(file);
                        }
                    }
                } catch (BinaryNotAvailableException bna) {
                    LOGGER.error(bna.getMessage());
                    mm.save(object.getRepositoryDocId(), "harvestProcess", "file not downloaded");
                } catch (ParseException | IOException e) {
                    LOGGER.error("\t\t Error occured while processing TEI for " + object.getRepositoryDocId(), e);
                    mm.save(object.getRepositoryDocId(), "harvestProcess", "harvest error");
                }

                LOGGER.info("\t\t\t\t Storing object " + repositoryDocId);
                mm.insertBiblioObject(object);
            } else {
                LOGGER.info("\t\t\t No TEI metadata !!!");
            }
        }
    }

    /**
     * Requests the given file if is not under embargo and register it either as
     * main file or as an annex.
     */
    protected void requestFile(BinaryFile bf) throws ParseException, IOException {
        Date embDate = Utilities.parseStringDate(bf.getEmbargoDate());
        Date today = new Date();
        if (embDate == null || embDate.before(today) || embDate.equals(today)) {
            LOGGER.info("\t\t\t Downloading: " + bf.getUrl());
            try {
                bf.setStream(Utilities.request(bf.getUrl()));
            } catch (MalformedURLException | ServiceException se) {
                LOGGER.error(se.getMessage());
                throw new BinaryNotAvailableException();
            }

            if (bf.getStream() == null) {
                mm.log(bf.getRepositoryDocId(), bf.getAnhalyticsId(), bf.getUrl(), bf.getDocumentType(), bf.isIsAnnexFile(), "nostream", "");
            } else {
                if (bf.isIsAnnexFile()) {
                    int n = bf.getUrl().lastIndexOf("/");
                    String filename = bf.getUrl().substring(n + 1);
                    bf.setFileName(filename);
                    LOGGER.info("\t\t\t\t Getting annex file " + filename + " for pub ID :" + bf.getRepositoryDocId());
                }
            }
        } else {
            mm.log(bf.getRepositoryDocId(), bf.getAnhalyticsId(), bf.getUrl(), bf.getDocumentType(), bf.isIsAnnexFile(), "embargo", bf.getEmbargoDate());
            LOGGER.info("\t\t\t file under embargo !");
        }
    }
}
