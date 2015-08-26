package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.managers.MongoManager;
import fr.inria.anhalytics.commons.utilities.KeyGen;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.properties.HarvestProperties;
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
 * Call of Grobid process via its REST web services.
 *
 * @author Patrice Lopez
 */
public class GrobidService {
    private int start = -1;
    private int end = -1;
    private boolean generateIDs = false;
    private String date;
    
    public GrobidService(int start, int end, boolean generateIDs, String date) {
        this.start = start;
        this.end = end;
        this.generateIDs = generateIDs;
        this.date = date;
        
    }

    /**
     * Call the Grobid full text extraction service on server.
     *
     * @param pdfPath path to the PDF file to be processed
     * @param start first page of the PDF to be processed, default -1 first page
     * @param last last page of the PDF to be processed, default -1 last page
     * @return the resulting TEI document as a String or null if the service
     * failed
     */
    public String runFullTextGrobid(InputStream inBinary) {
        String zipDirectoryPath = null;
        String tei = null;
        File zipFolder = null;
        try {
            URL url = new URL("http://" + HarvestProperties.getGrobidHost() + ":" + HarvestProperties.getGrobidPort() + "/processFulltextAssetDocument");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            String filepath = Utilities.storeTmpFile(inBinary);
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
                        + conn.getResponseCode()+ " " +IOUtils.toString(conn.getErrorStream(), "UTF-8"));
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
            
            
        } catch (ConnectException e) {
            e.printStackTrace();
            try {
                Thread.sleep(20000);
                runFullTextGrobid(inBinary);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } catch (HttpRetryException e) {
            e.printStackTrace();
            try {
                Thread.sleep(20000);
                runFullTextGrobid(inBinary);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return zipDirectoryPath;        
    }
    
}
