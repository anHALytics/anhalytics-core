package fr.inria.anhalytics.harvest.auxiliaries;

import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.harvest.Harvester;
import fr.inria.anhalytics.harvest.oaipmh.HALOAIPMHDomParser; // to be modified
import java.text.ParseException;

import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Harvesting based on a list of source-specific identifiers. The list of IDs
 * can be given in a file, and based on the source, urls are built for download,
 * normalize and store the available information.
 *
 */
public class IdListHarvester extends Harvester {
    
    protected static final Logger logger = LoggerFactory.getLogger(IdListHarvester.class);
    
    private static String halUrl = "https://hal.archives-ouvertes.fr/";
    private HALOAIPMHDomParser oaiDom;
    
    public IdListHarvester() {
        super();
        this.oaiDom = new HALOAIPMHDomParser();
    }

    /**
     * Get a list of HAL documents based on a list of HAL ID given in a file.
     * The ID file path is available in the HarvestProperties object.
     */
    /* note: add a source parameter to identify the target repository and
	 * the id type */
    public void fetchAllDocuments() throws IOException, SAXException, ParserConfigurationException, ParseException {
        Source source = Source.HAL;
        List<BiblioObject> biblioObjects = new ArrayList<BiblioObject>();
        BufferedReader br = null;
        try {
            // read the file with one hal id per line
            String docID = null;
            br = new BufferedReader(new FileReader(HarvestProperties.getListFile()));
            while ((docID = br.readLine()) != null) {
                if (docID.trim().length() == 0) {
                    continue;
                }
                if (!HarvestProperties.isReset() && mm.isSavedObject(docID)) {
                    logger.info("\t\t Already grabbed, Skipping...");
                    continue;
                }
                String teiUrl = halUrl + docID + "/tei";

                // get TEI file 
                String teiString = IOUtils.toString(new URL(teiUrl), "UTF-8");
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                docFactory.setValidating(false);
                Document teiDoc = null;
                try {
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    teiDoc = docBuilder.parse(new ByteArrayInputStream(teiString.getBytes()));
                } catch (SAXException | ParserConfigurationException | IOException e) {
                    e.printStackTrace();
                }
                Element rootElement = teiDoc.getDocumentElement();
                BiblioObject biblioObject = this.oaiDom.processRecord((Element) rootElement);
                if (biblioObject != null) {
                    biblioObjects.add(biblioObject);
                }
            }
            saveObjects(biblioObjects);
        } catch (MalformedURLException mue) {
            logger.error(mue.getMessage(), mue);
        } catch (ServiceException se) {
            logger.error(se.getMessage(), se);
            mm.save("", "blockedHarvestProcess", se.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        
    }
}
