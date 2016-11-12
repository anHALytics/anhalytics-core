package fr.inria.anhalytics.harvest.crossref;

import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author azhar
 */
public class OpenUrl {

    private static final Logger logger = LoggerFactory.getLogger(OpenUrl.class);
    private MongoFileManager mm;

    private static final String IstexURL
            = "http://api.istex.fr/document/openurl?url_ver=rft_id=info:doi/%s";

    public OpenUrl() {
        try {
            this.mm = MongoFileManager.getInstance(false);
        } catch (ServiceException ex) {
            throw new ServiceException("MongoDB is not UP, the process will be halted.");
        }
    }

    public void getIstexUrl() {
        if (mm.initIdentifiersWithoutPdfUrl()) {
            while (mm.hasMoreIdentifiers()) {
                try {
                    String doi = mm.nextIdentifier();
                    String currentAnhalyticsId = mm.getCurrentAnhalyticsId();
                    logger.info("################################" + currentAnhalyticsId+"####################");
                    URL url = new URL(String.format(IstexURL, doi));
                    logger.info("Sending: " + url.toString());
                    HttpURLConnection urlConn = null;
                    try {
                        urlConn = (HttpURLConnection) url.openConnection();
                    } catch (Exception e) {
                        try {
                            urlConn = (HttpURLConnection) url.openConnection();
                        } catch (Exception e2) {
                            urlConn = null;
                            throw new Exception("An exception occured while running Grobid.", e2);
                        }
                    }
                    if (urlConn != null) {
                        try {
                            urlConn.setDoOutput(true);
                            urlConn.setDoInput(true);
                            urlConn.setRequestMethod("GET");

                            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            if (urlConn.getResponseCode() == 200) {
                                String foundurl = urlConn.getURL().toString();
                                logger.info("URL found : " + foundurl);
                                //mm.updateIdentifier(doi);
                                //mm.insertBinary();
                            }
                            urlConn.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            logger.info("Done.");
        }
    }
}
