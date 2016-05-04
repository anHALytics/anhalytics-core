package fr.inria.anhalytics.harvest;

import java.io.IOException;
import java.text.ParseException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author Achraf
 */
public interface HarvesterItf {
 
    
    /**
     * Harvests all the repository.
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.text.ParseException
     */
    public void fetchAllDocuments() throws IOException, SAXException, ParserConfigurationException, ParseException ;
}
