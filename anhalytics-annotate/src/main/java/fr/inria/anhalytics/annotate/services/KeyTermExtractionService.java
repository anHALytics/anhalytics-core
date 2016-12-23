package fr.inria.anhalytics.annotate.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.anhalytics.commons.properties.AnnotateProperties;

import java.io.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.net.HttpRetryException;
import java.net.ConnectException;

import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Perform a key term extraction for a document and disambiguate the resulting
 * terms. We use the tool via its REST web services, similarly as for the NERD.
 * The resulting extraction can be used to annotate a document as a whole, so
 * without stand-off position to particular chunks of texts.
 *
 * @author Achraf, Patrice
 */
public class KeyTermExtractionService extends AnnotateService {

    private static final Logger logger = LoggerFactory.getLogger(KeyTermExtractionService.class);

    static private String RESOURCEPATH = "processKeyTermArticleTEI";

    public KeyTermExtractionService(String tei) {
        super(tei);
    }

    /**
     * Call the Keyterm extraction service on server for a TEI document.
     *
     * @return the resulting extracted disambiguated terms in JSON
     */
    public String runKeyTermExtraction() {
        StringBuffer output = new StringBuffer();
        try {
            URL url = new URL("http://" + AnnotateProperties.getKeytermHost() + 
                    (AnnotateProperties.getKeytermPort().isEmpty() ? "" : ":" + AnnotateProperties.getKeytermPort()) + "/" + RESOURCEPATH);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            StringBody contentBody = new StringBody(input, Charset.forName("UTF-8"));
            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);
            multipartEntity.addPart("file", contentBody);

            conn.setRequestProperty("Content-Type", multipartEntity.getContentType().getValue());
            OutputStream out = conn.getOutputStream();
            try {
                multipartEntity.writeTo(out);
            } finally {
                out.close();
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_UNAVAILABLE) {
                throw new HttpRetryException("Failed: service not available - HTTP error code : "
                        + conn.getResponseCode(), conn.getResponseCode());
            }

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed: HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String line = null;
            while ((line = br.readLine()) != null) {
                output.append(line);
                output.append(" ");
            }
            br.close();
            conn.disconnect();
        } catch (ConnectException e) {
            e.printStackTrace();
            try {
                Thread.sleep(20000);
                runKeyTermExtraction();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } catch (HttpRetryException e) {
            e.printStackTrace();
            try {
                Thread.sleep(20000);
                runKeyTermExtraction();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toString().trim();
    }

}
