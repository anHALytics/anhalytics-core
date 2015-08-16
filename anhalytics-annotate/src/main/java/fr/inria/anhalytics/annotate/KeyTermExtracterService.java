package fr.inria.anhalytics.annotate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.anhalytics.commons.utilities.Utilities;

import java.io.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpRetryException;
import java.net.ConnectException;

import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.IOUtils;

/**
 *	Perform a key term extraction for a document and disambiguate the resulting terms. We use the tool 
 *  via its REST web services, similarly as for the NERD. The resulting extraction can be used to 
 *  annotate a document as a whole, so without stand-off position to particular chunks of texts. 
 *
 *  @author Patrice Lopez
 */
public class KeyTermExtracterService {
	
    private static final Logger logger = LoggerFactory.getLogger(KeyTermExtracterService.class);

    private String keyterm_host = null;
    private String keyterm_port = null;
    private String input = null;

    static private String RESOURCEPATH = "processKeyTermArticleTEI";

    public KeyTermExtracterService(String input, String keytermHost, String keytermPort) {
        this.input = input;
        this.keyterm_host = keytermHost;
        this.keyterm_port = keytermPort;
    }
	
    /**
     * Call the Keyterm extraction service on server for TEI documents.
     *
	 * @param inBinary input stream of the PDF to be processed	
     * @return the resulting extractions are in JSON 
     */
    public String runKeytermExtraction(InputStream inBinary) {
        StringBuffer output = new StringBuffer();
        try {
            URL url = new URL("http://" + keyterm_host + ":" + keyterm_port + "/" + RESOURCEPATH);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            String filepath = Utilities.storeTmpFile(inBinary);
            FileBody fileBody = new FileBody(new File(filepath));
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

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
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
        } 
		catch (ConnectException e) {
            e.printStackTrace();
            try {
                Thread.sleep(20000);
                runKeytermExtraction(inBinary);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } 
		catch (HttpRetryException e) {
            e.printStackTrace();
            try {
                Thread.sleep(20000);
                runKeytermExtraction(inBinary);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } 
		catch (MalformedURLException e) {
            e.printStackTrace();
        } 
		catch (IOException e) {
            e.printStackTrace();
        }
        return output.toString().trim();    
    }
	
}