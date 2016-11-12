package fr.inria.anhalytics.annotate.services;

import com.sun.org.apache.xerces.internal.util.URI;
import fr.inria.anhalytics.annotate.Annotator;
import fr.inria.anhalytics.commons.properties.AnnotateProperties;
import fr.inria.anhalytics.annotate.exceptions.UnreachableAnnotateServiceException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Call of annotate services via its REST web services.
 * @author Achraf
 */
public abstract class AnnotateService {
    private static final Logger logger = LoggerFactory.getLogger(AnnotateService.class);
    
    protected String input = null;
    
    public AnnotateService(String input) {
        this.input = input;
    }
    
    
    /**
     * Checks if Annotating service is responding and available.
     * @return boolean
     */
    public static boolean isAnnotateServiceReady(Annotator.Annotator_Type annotator_type) throws UnreachableAnnotateServiceException {
        logger.info("Checking "+annotator_type+" service...");
        int responseCode = 0;
        HttpURLConnection conn = null;
        try {
            String urlString = "";
            if (annotator_type == Annotator.Annotator_Type.NERD) {
                urlString = "http://" + AnnotateProperties.getNerdHost() +
                (AnnotateProperties.getNerdPort().isEmpty() ? "":":" + AnnotateProperties.getNerdPort()) + "/service/isalive";
            } else {
                // keyterm isalive checking not implemented yet.
                logger.info(annotator_type+"  service is ok and can be used.");
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
            logger.error(annotator_type+"  service is not alive.");
            throw new UnreachableAnnotateServiceException(responseCode, annotator_type.toString());
        }
        conn.disconnect();
        logger.info(annotator_type+"  service is ok and can be used.");
        return true;
    }
}
