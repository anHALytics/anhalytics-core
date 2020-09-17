package fr.inria.anhalytics.annotate.services;

import com.sun.tools.javac.comp.Annotate;
import fr.inria.anhalytics.commons.properties.AnnotateProperties;
import fr.inria.anhalytics.commons.utilities.KeyGen;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PDFSuperconductorsService extends AnnotateService {

    private static final Logger logger = LoggerFactory.getLogger(QuantitiesService.class);

    private static final String REQUEST_URL = "/process/pdf";

    public PDFSuperconductorsService(InputStream inputPDF) {
        super(inputPDF);
    }

    public String processPDFQuantities() {
        StringBuffer output = new StringBuffer();
        try {
            String urlString = AnnotateProperties.getSuperconductorsUrl() + REQUEST_URL;
            URL url = new URL(urlString);
            logger.info(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            // note: how to pass directly the stream in the multipartEntity? - we could if we know the length of the stream
            File file = File.createTempFile("tmp_superconductors", AnnotateProperties.getTmp());
            FileUtils.copyInputStreamToFile(input, file);
            FileBody fileBody = new FileBody(file);
            HttpEntity multipartEntity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.STRICT)
                    .addPart("input", fileBody)
                    .build();

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
                        + conn.getResponseCode() + " " + IOUtils.toString(conn.getErrorStream(), StandardCharsets.UTF_8));
            }
            logger.info("Response " + conn.getResponseCode());
            InputStream in = conn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader((in)));
            String line = null;
            while ((line = br.readLine()) != null) {
                output.append(line);
                output.append(" ");
            }

            IOUtils.closeQuietly(in);
            conn.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(output.toString().trim());
        return output.toString().trim();
    }

}
