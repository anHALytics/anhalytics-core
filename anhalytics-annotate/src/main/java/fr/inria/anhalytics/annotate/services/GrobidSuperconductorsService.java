package fr.inria.anhalytics.annotate.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.inria.anhalytics.commons.properties.AnnotateProperties;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


public class GrobidSuperconductorsService extends AnnotateService {

    private static final Logger logger = LoggerFactory.getLogger(GrobidSuperconductorsService.class);

    static private String REQUEST_URL = "/process/text";

    public GrobidSuperconductorsService(InputStream input) {
        super(input);
    }

    public String process() {
        StringBuilder output = new StringBuilder();
        HttpURLConnection conn = null;
        try {
            URL url = new URL(AnnotateProperties.getSuperconductorsUrl() + REQUEST_URL);
            conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf8");

            String inputString = IOUtils.toString(input, StandardCharsets.UTF_8);
            HttpEntity entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.STRICT)
                    .addTextBody("text", inputString)
                    .build();

            conn.setRequestProperty("Content-Type", entity.getContentType().getValue());

            try (OutputStream os = conn.getOutputStream()) {
                entity.writeTo(os);
                os.flush();
                logger.info("Response " + conn.getResponseCode());
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    logger.error("Failed annotating text segment: HTTP error code : " + conn.getResponseCode());
                    return null;
                }
                String outputString = IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8);
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = mapper.readValue(outputString, new TypeReference<Map<String, Object>>() {
                });
                List<Object> paragraphs = (List) map.get("paragraphs");

                long spans = paragraphs.stream().filter(par -> ((Map<String, Object>) par).containsKey("spans")).count();

                if (spans == 0) {
                    return null;
                }
                paragraphs.stream().forEach(par -> ((Map<String, Object>) par).remove("tokens"));

                StringWriter stringWriter = new StringWriter();
                mapper.writeValue(stringWriter, map);
                output.append(stringWriter.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return output.toString().trim();
    }

}
