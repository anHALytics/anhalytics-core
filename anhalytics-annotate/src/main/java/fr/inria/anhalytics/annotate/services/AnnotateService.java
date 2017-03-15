package fr.inria.anhalytics.annotate.services;

import com.sun.org.apache.xerces.internal.util.URI;
import fr.inria.anhalytics.annotate.Annotator;
import fr.inria.anhalytics.commons.properties.AnnotateProperties;
import fr.inria.anhalytics.annotate.exceptions.UnreachableAnnotateServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;

/**
 * Call of annotate services via its REST web services. Data to be sent to the service 
 * is given as a stream, which could be textual, xml, PDF or whatever.
 *
 */
public abstract class AnnotateService {

    private static final Logger logger = LoggerFactory.getLogger(AnnotateService.class);

    //protected String input = null;
    protected InputStream input = null;

    public AnnotateService(InputStream input) {
        this.input = input;
    }

    /**
     * Checks if Annotating service is responding and available.
     *
     * @return boolean
     */
    public static boolean isAnnotateServiceReady(Annotator.Annotator_Type annotator_type) throws UnreachableAnnotateServiceException {
        logger.info("Checking " + annotator_type + " service...");
        int responseCode = 0;
        HttpURLConnection conn = null;
        try {
            String urlString = "";
            if (annotator_type == Annotator.Annotator_Type.NERD) {
                urlString = "http://" + AnnotateProperties.getNerdHost()
                        + (AnnotateProperties.getNerdPort().isEmpty() ? "" : ":" + AnnotateProperties.getNerdPort()) + "/service/isalive";
            } else if (annotator_type == Annotator.Annotator_Type.QUANTITIES) {
                urlString = "http://" + AnnotateProperties.getQuantitiesHost()
                        + (AnnotateProperties.getQuantitiesPort().isEmpty() ? "" : ":" + AnnotateProperties.getQuantitiesPort()) + "/isalive";
            } else {
                // keyterm isalive checking not implemented yet.
                logger.info(annotator_type + "  service is ok and can be used.");
                return true;
            }
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            responseCode = conn.getResponseCode();
        } catch (IOException e) {
            throw new UnreachableAnnotateServiceException(responseCode, annotator_type.toString());
        }
        if (responseCode != 200) {
            logger.error(annotator_type + "  service is not alive.");
            throw new UnreachableAnnotateServiceException(responseCode, annotator_type.toString());
        }
        conn.disconnect();
        logger.info(annotator_type + "  service is ok and can be used.");
        return true;
    }
}
