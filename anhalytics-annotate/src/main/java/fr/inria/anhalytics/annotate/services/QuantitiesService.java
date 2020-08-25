package fr.inria.anhalytics.annotate.services;

import fr.inria.anhalytics.commons.properties.AnnotateProperties;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 *
 * @author azhar
 */
public class QuantitiesService extends AnnotateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuantitiesService.class);

    static private String REQUEST_TEXT_QUANTITIES = "processQuantityText";

    public QuantitiesService(InputStream input) {
        super(input);
    }

    /**
     * Call the Quantities text annotation service on server.
     *
     * @return the resulting annotation in JSON
     */
    public String processTextQuantities() {
        StringBuffer output = new StringBuffer();
        try {
            URL url = new URL(AnnotateProperties.getQuantitiesHost()
                    + (AnnotateProperties.getQuantitiesPort().isEmpty() ? "" : ":" + AnnotateProperties.getQuantitiesPort()) + "/" + REQUEST_TEXT_QUANTITIES);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf8");

            /*ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("text", input);
            byte[] postDataBytes = node.toString().getBytes("UTF-8");*/

            String inputString = IOUtils.toString(input, "UTF-8");
            String piece = "text="+inputString;
            byte[] postDataBytes = piece.getBytes("UTF-8");

            OutputStream os = conn.getOutputStream();
            os.write(postDataBytes);
            os.flush();
            LOGGER.info("Response "+conn.getResponseCode());
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                LOGGER.error("Failed annotating text segment: HTTP error code : "
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
            LOGGER.error("Error: ", e);
        } catch (IOException e) {
            LOGGER.error("Error: ", e);
        }
        //System.out.println(output.toString().trim());
        return output.toString().trim();
    }

}
