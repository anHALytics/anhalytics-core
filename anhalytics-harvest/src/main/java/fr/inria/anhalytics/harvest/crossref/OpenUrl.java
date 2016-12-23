package fr.inria.anhalytics.harvest.crossref;

import fr.inria.anhalytics.commons.data.Identifier;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import java.net.HttpURLConnection;
import java.net.URL;
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
        this.mm = MongoFileManager.getInstance(false);
    }

    public void getIstexUrl() {
        if (mm.initIdentifiersWithoutPdfUrl()) {
            while (mm.hasMore()) {
                try {
                    Identifier id = mm.nextIdentifier();
                    String currentAnhalyticsId = id.getAnhalyticsId();
                    logger.info("################################" + currentAnhalyticsId + "####################");
                    URL url = new URL(String.format(IstexURL, id.getDoi()));
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
