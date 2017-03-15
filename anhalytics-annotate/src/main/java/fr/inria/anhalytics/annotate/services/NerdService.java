package fr.inria.anhalytics.annotate.services;

import fr.inria.anhalytics.annotate.Annotator;
import fr.inria.anhalytics.commons.properties.AnnotateProperties;
import fr.inria.anhalytics.annotate.exceptions.UnreachableAnnotateServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/*import org.codehaus.jackson.node.*;
import org.codehaus.jackson.map.ObjectMapper;*/

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.io.*;

import org.apache.commons.io.IOUtils;

/**
 * Call of Nerd process via its REST web services.
 *
 * @author Patrice Lopez
 */
public class NerdService extends AnnotateService {

    private static final Logger logger = LoggerFactory.getLogger(NerdService.class);

	private String language = null;

    static private String REQUEST = "processERDQuery";

    public NerdService(InputStream input, String language) {
        super(input);
        this.language = language;
    }

    /**
     * Call the NERD full text annotation service on server.
     *
     * @return the resulting annotation in JSON
     */
    public String runNerd() {
        StringBuffer output = new StringBuffer();
        try {
            URL url = new URL("http://" + AnnotateProperties.getNerdHost() + 
                    (AnnotateProperties.getNerdPort().isEmpty() ? "":":" + AnnotateProperties.getNerdPort()) + "/service/" + REQUEST);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf8");

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            String inputString = IOUtils.toString(input, "UTF-8");
            node.put("text", inputString);
            ArrayNode dataTable = mapper.createArrayNode();
            dataTable.add("fr");
            dataTable.add("de");
            dataTable.add("en");
            node.put("resultLanguages", dataTable);
			if (language != null) {
	            ObjectNode dataNode = mapper.createObjectNode();
	            dataNode.put("lang", language);
				node.put("language", dataNode);
			}
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
