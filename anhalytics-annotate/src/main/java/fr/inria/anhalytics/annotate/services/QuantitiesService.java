package fr.inria.anhalytics.annotate.services;

import fr.inria.anhalytics.commons.properties.AnnotateProperties;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author azhar
 */
public class QuantitiesService extends AnnotateService {

    private static final Logger logger = LoggerFactory.getLogger(QuantitiesService.class);

    static private String REQUEST_PDF_QUANTITIES = "annotateQuantityPDF";

    static private String REQUEST_TEXT_QUANTITIES = "processQuantityText";

    public QuantitiesService(String input) {
        super(input);
    }

    /**
     * Call the Quantities full text annotation service on server.
     *
     * @return the resulting annotation in JSON
     */
    public String processPDFQuantities() {
        StringBuffer output = new StringBuffer();
        try {
            URL url = new URL("http://" + AnnotateProperties.getQuantitiesHost()
                    + (AnnotateProperties.getQuantitiesPort().isEmpty() ? "" : ":" + AnnotateProperties.getQuantitiesPort()) + "/" + REQUEST_PDF_QUANTITIES);
            logger.info("http://" + AnnotateProperties.getQuantitiesHost()
                    + (AnnotateProperties.getQuantitiesPort().isEmpty() ? "" : ":" + AnnotateProperties.getQuantitiesPort()) + "/" + REQUEST_PDF_QUANTITIES);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            logger.info("Processing : "+input);
            FileBody fileBody = new FileBody(new File(input));
            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);
            multipartEntity.addPart("input", fileBody);

            conn.setRequestProperty("Content-Type", multipartEntity.getContentType().getValue());
            OutputStream out = conn.getOutputStream();
            try {
                multipartEntity.writeTo(out);
            } finally {
                out.close();
            }
            if (conn.getResponseCode() == HttpURLConnection.HTTP_UNAVAILABLE) {
                throw new HttpRetryException("Failed : HTTP error code : "
                        + conn.getResponseCode(), conn.getResponseCode());
            }

            //int status = connection.getResponseCode();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode() + " " + IOUtils.toString(conn.getErrorStream(), "UTF-8"));
            }
            logger.info("Response "+conn.getResponseCode());
            InputStream in = conn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader((in)));
            String line = null;
            while ((line = br.readLine()) != null) {
                output.append(line);
                output.append(" ");
            }

            IOUtils.closeQuietly(in);
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(output.toString().trim());
        return output.toString().trim();
    }

    /**
     * Call the Quantities text annotation service on server.
     *
     * @return the resulting annotation in JSON
     */
    public String processTextQuantities() {
        StringBuffer output = new StringBuffer();
        try {
            URL url = new URL("http://" + AnnotateProperties.getQuantitiesHost()
                    + (AnnotateProperties.getQuantitiesPort().isEmpty() ? "" : ":" + AnnotateProperties.getQuantitiesPort()) + "/" + REQUEST_TEXT_QUANTITIES);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf8");

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("text", input);
            byte[] postDataBytes = node.toString().getBytes("UTF-8");

            OutputStream os = conn.getOutputStream();
            os.write(postDataBytes);
            os.flush();
            logger.info("Response "+conn.getResponseCode());
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                logger.error("Failed annotating text segment: HTTP error code : "
                        + conn.getResponseCode());
                return null;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String line = null;
            while ((line = br.readLine()) != null) {
                output.append(line);
                output.append(" ");
            }
            os.close();
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(output.toString().trim());
        return output.toString().trim();
    }

}
