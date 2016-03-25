package fr.inria.anhalytics.annotate;

import fr.inria.anhalytics.annotate.properties.AnnotateProperties;
import fr.inria.anhalytics.commons.exceptions.UnreachableNerdServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.codehaus.jackson.node.*;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Call of Nerd process via its REST web services.
 *
 * @author Patrice Lopez
 */
public class NerdService {

    private static final Logger logger = LoggerFactory.getLogger(NerdService.class);

    private String input = null;

    static private String REQUEST = "processERDQuery";

    public NerdService(String input) {
        this.input = input;
    }

    /**
     * Call the NERD full text annotation service on server.
     *
     * @return the resulting annotation in JSON
     */
    public String runNerd() {
        StringBuffer output = new StringBuffer();
        try {
            URL url = new URL("http://" + AnnotateProperties.getNerdHost() + ":" + AnnotateProperties.getNerdPort() + "/nerd/service/" + REQUEST);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf8");

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("text", input);
            ArrayNode dataTable = mapper.createArrayNode();
            dataTable.add("fr");
            dataTable.add("de");
            dataTable.add("en");
            node.put("resultLanguages", dataTable);
            byte[] postDataBytes = node.toString().getBytes("UTF-8");

            OutputStream os = conn.getOutputStream();
            os.write(postDataBytes);
            os.flush();
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
        return output.toString().trim();
    }

    /**
     * Checks if Nerd service is responding and available.
     * @return boolean
     */
    public static boolean isNerdReady() throws UnreachableNerdServiceException {
        logger.info("Checking NERD service...");
        int responseCode = 0;
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://" + AnnotateProperties.getNerdHost() + ":" + 
				AnnotateProperties.getNerdPort() + "/nerd/service/isalive");
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            responseCode = conn.getResponseCode();
        } catch (Exception e) {
            throw new UnreachableNerdServiceException("NERD service is not reachable, check host and port parameters.");
        }
        if (responseCode != 200) {
            logger.error("NERD service is not alive.");
            throw new UnreachableNerdServiceException("NERD service is not alive.");
        }
        conn.disconnect();
        logger.info("NERD service is ok and can be used.");
        return true;
    }

}
