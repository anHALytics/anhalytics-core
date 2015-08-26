package fr.inria.anhalytics.annotate;

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

    private String nerd_host = null;
    private String nerd_port = null;
    private String input = null;

    static private String RESOURCEPATH = "processNERDQueryScience";

    public NerdService(String input, String nerdHost, String nerdPort) {
        this.input = input;
        this.nerd_host = nerdHost;
        this.nerd_port = nerdPort;
    }

    /**
     * Call the NERD full text annotation service on server.
     *
     * @return the resulting annotation in JSON
     */
    public String runNerd() {
        StringBuffer output = new StringBuffer();
        try {
            URL url = new URL("http://" + nerd_host + ":" + nerd_port + "/" + RESOURCEPATH);
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

}
