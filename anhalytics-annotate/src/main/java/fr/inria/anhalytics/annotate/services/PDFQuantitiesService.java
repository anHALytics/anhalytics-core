package fr.inria.anhalytics.annotate.services;

import fr.inria.anhalytics.commons.properties.AnnotateProperties;
import fr.inria.anhalytics.commons.utilities.KeyGen;

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
import org.apache.commons.io.FileUtils;
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
 * Call the quantity annotation service for a PDF input. Annotations will be enriched with
 * coordinates of annotations in the original PDF document.
 */
public class PDFQuantitiesService extends AnnotateService {

    private static final Logger logger = LoggerFactory.getLogger(QuantitiesService.class);

    static private String REQUEST_PDF_QUANTITIES = "annotateQuantityPDF";

    public PDFQuantitiesService(InputStream inputPDF) {
        super(inputPDF);
    }

    /**
     * Call the Quantities PDF annotation service on server.
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

            // note: how to pass directly the stream in the multipartEntity? - we could if we know the length of the stream
            File file = new File(AnnotateProperties.getTmp(), KeyGen.getKey());
            FileUtils.copyInputStreamToFile(input,file);
            FileBody fileBody = new FileBody(file);
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

}
