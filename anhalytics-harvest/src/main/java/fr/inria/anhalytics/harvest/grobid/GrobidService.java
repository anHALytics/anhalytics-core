package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.properties.HarvestProperties;
import fr.inria.anhalytics.commons.utilities.KeyGen;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.exceptions.GrobidTimeoutException;
import fr.inria.anhalytics.harvest.exceptions.UnreachableGrobidServiceException;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Call of Grobid process via its REST web services.
 *
 * @author Patrice
 */
public class GrobidService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrobidService.class);

    private int start = -1;
    private int end = -1;
    private boolean generateIDs = false;

    int TIMEOUT_VALUE = 30000;

    public GrobidService(int start, int end, boolean generateIDs) {
        this.start = start;
        this.end = end;
        this.generateIDs = generateIDs;

    }

    public GrobidService() {
    }

    /**
     * Call the Grobid full text extraction service on server.
     *
     * @param filepath InputStream of the PDF file to be processed
     * @return the resulting TEI document as a String or null if the service
     * failed
     */
    public String runFullTextGrobid(String filepath) {
        String tei = null;
        try {
            URL url = new URL(HarvestProperties.getGrobidHost() + "/processFulltextDocument");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //conn.setConnectTimeout(TIMEOUT_VALUE);
            //conn.setReadTimeout(TIMEOUT_VALUE);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            FileBody fileBody = new FileBody(new File(filepath));
            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);
            multipartEntity.addPart("input", fileBody);

            if (start != -1) {
                StringBody contentString = new StringBody("" + start);
                multipartEntity.addPart("start", contentString);
            }
            if (end != -1) {
                StringBody contentString = new StringBody("" + end);
                multipartEntity.addPart("end", contentString);
            }
            /*if (generateIDs) {
                StringBody contentString = new StringBody("1");
                multipartEntity.addPart("generateIDs", contentString);
            }*/

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

            InputStream in = conn.getInputStream();
            tei = IOUtils.toString(in, StandardCharsets.UTF_8);

            try {
                in.close();
            } catch (IOException e) {
                LOGGER.warn("Cannot close InputStream ", e);
            }


            conn.disconnect();

        } catch (ConnectException | HttpRetryException e) {
            LOGGER.error(e.getMessage(), e.getCause());
            try {
                Thread.sleep(20000);
                runFullTextGrobid(filepath);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } catch (SocketTimeoutException e) {
            throw new GrobidTimeoutException("Grobid processing timed out.");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e.getCause());
        }
        return tei;
    }

    public String runFullTextAssetGrobid(String filepath) {
        String zipDirectoryPath = null;
        String tei = null;
        File zipFolder = null;
        try {
            URL url = new URL(HarvestProperties.getGrobidHost() + "/processFulltextAssetDocument");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //conn.setConnectTimeout(TIMEOUT_VALUE);
            //conn.setReadTimeout(TIMEOUT_VALUE);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            FileBody fileBody = new FileBody(new File(filepath));
            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);
            multipartEntity.addPart("input", fileBody);

            if (start != -1) {
                StringBody contentString = new StringBody("" + start);
                multipartEntity.addPart("start", contentString);
            }
            if (end != -1) {
                StringBody contentString = new StringBody("" + end);
                multipartEntity.addPart("end", contentString);
            }
            /*if (generateIDs) {
                StringBody contentString = new StringBody("1");
                multipartEntity.addPart("generateIDs", contentString);
            }*/

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

            InputStream in = conn.getInputStream();
            zipDirectoryPath = HarvestProperties.getTmpPath() + "/" + KeyGen.getKey();
            zipFolder = new File(zipDirectoryPath);
            if (!zipFolder.exists()) {
                zipFolder.mkdir();
            }
            FileOutputStream zipStream = new FileOutputStream(zipDirectoryPath + "/" + "out.zip");
            IOUtils.copy(in, zipStream);
            zipStream.close();
            in.close();

            Utilities.unzipIt(zipDirectoryPath + "/" + "out.zip", zipDirectoryPath);

            conn.disconnect();

        } catch (ConnectException | HttpRetryException e) {
            LOGGER.error(e.getMessage(), e.getCause());
            try {
                Thread.sleep(20000);
                runFullTextGrobid(filepath);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } catch (SocketTimeoutException e) {
            throw new GrobidTimeoutException("Grobid processing timed out.");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e.getCause());
        }
        return zipDirectoryPath;
    }

    public String processAffiliation(String affiliations) {

        String retVal = null;
        try {
            URL url = new URL(HarvestProperties.getGrobidHost() + "/processAffiliations");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //conn.setConnectTimeout(TIMEOUT_VALUE);
            //conn.setReadTimeout(TIMEOUT_VALUE);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            OutputStream out = conn.getOutputStream();
            try {
                DataOutputStream wr = new DataOutputStream(
                        conn.getOutputStream());
                wr.writeBytes("affiliations=" + URLEncoder.encode(affiliations, "UTF-8"));
                wr.flush();
                wr.close();
            } finally {
                out.close();
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_UNAVAILABLE) {
                throw new HttpRetryException("Failed : HTTP error code : "
                        + conn.getResponseCode(), conn.getResponseCode());
            }

            //int status = connection.getResponseCode();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK && conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode() + " " + IOUtils.toString(conn.getErrorStream(), "UTF-8"));
            }

            //Get Response	
            InputStream is = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            retVal = response.toString();
        } catch (ConnectException e) {
            LOGGER.error(e.getMessage(), e.getCause());
            try {
                Thread.sleep(20000);
                processAffiliation(affiliations);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } catch (HttpRetryException e) {
            LOGGER.error(e.getMessage(), e.getCause());
            try {
                Thread.sleep(20000);
                processAffiliation(affiliations);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } catch (SocketTimeoutException e) {
            throw new GrobidTimeoutException("Grobid processing timed out.");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return retVal;

    }

    /**
     * Checks if Grobid service is responding and local tmp directory is
     * available.
     *
     * @return boolean
     */
    public static boolean isGrobidOk() throws UnreachableGrobidServiceException {
        LOGGER.info("Cheking Grobid service...");

        int responseCode = 0;
        try {
            URL url = new URL(HarvestProperties.getGrobidHost() + "/isalive");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            responseCode = conn.getResponseCode();
            conn.disconnect();
        } catch (IOException e) {
            throw new UnreachableGrobidServiceException("Grobid service is not alive.", e);
        }
        if (responseCode != 200) {
            throw new UnreachableGrobidServiceException(responseCode);
        }
        LOGGER.info("Grobid service is ok and can be used.");
        return true;
    }
}
