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
/*import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;*/

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author azhar
 */
public class QuantitiesService extends AnnotateService {

    private static final Logger logger = LoggerFactory.getLogger(QuantitiesService.class);

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
