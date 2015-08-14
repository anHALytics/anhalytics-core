package fr.inria.anhalytics.annotate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *	Perform a key term extraction for a document and disambiguate the resulting terms. We use the tool 
 *  via its REST web services, similarly as for the NERD. The resulting extraction can be used to 
 *  annotate a document as a whole, so without stand-off position to particular chunks of texts. 
 *
 *  @author Patrice Lopez
 */
public class KeyTermExtracter {
	
    private static final Logger logger = LoggerFactory.getLogger(KeyTermExtracter.class);

    private String keyterm_host = null;
    private String keyterm_port = null;
    private String input = null;

    static private String RESOURCEPATH = "processKeyTermArticleTEI";

    public NerdService(String input, String keytermHost, String keytermPort) {
        this.input = input;
        this.keyterm_host = keytermHost;
        this.keyterm_port = keytermPort;
    }
	
    /**
     * Call the Keyterm extraction service on server for TEI documents.
     *
     * @return the resulting extractions are in JSON 
     */
    public String runKeytermExtraction() {
        StringBuffer output = new StringBuffer();
        try {
            URL url = new URL("http://" + keyterm_host + ":" + keyterm_port + "/" + RESOURCEPATH);
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