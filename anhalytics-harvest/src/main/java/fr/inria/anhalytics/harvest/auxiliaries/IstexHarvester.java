package fr.inria.anhalytics.harvest.auxiliaries;

import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Harvests metadata from istex and stores it.
 *
 * @author achraf
 */
public class IstexHarvester {

    protected static final Logger logger = LoggerFactory.getLogger(IstexHarvester.class);
    private static IstexHarvester harvester = null;
    private static final String istexApiUrl = "https://api.istex.fr/document";
    private MongoFileManager mm = null;
    private String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

    public IstexHarvester() {
        try {
            this.mm = MongoFileManager.getInstance(false);
        } catch (ServiceException ex) {
            throw new ServiceException("MongoDB is not UP, the process will be halted.");
        }
    }

    public List<String> harvest() {
        String size = "5000"; // max size we can get.
        String params = "?q=*&size=" + size + "from=0";
        List<String> records = null;
        HttpURLConnection urlConn = null;
        String json = null;
        boolean loop = true;
        int count = 0;
        String request = istexApiUrl + "/" + params;
        while (loop) {
            records = new ArrayList<String>();
            try {
                URL url = new URL(request);
                logger.info(request);
                urlConn = (HttpURLConnection) url.openConnection();
                if (urlConn != null) {
                    urlConn.setDoInput(true);
                    urlConn.setRequestMethod("GET");

                    InputStream in = urlConn.getInputStream();
                    json = Utilities.convertStreamToString(in);
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) jsonParser.parse(json);
                    JSONArray hits = (JSONArray) jsonObject.get("hits");
                    request = (String) jsonObject.get("nextPageURI");

                    Iterator i = hits.iterator();
                    while (i.hasNext()) {
                        JSONObject hit = (JSONObject) i.next();
                        records.add((String) hit.get("id"));
                    }
                    processRecords(records);
                    if (request == null) {
                        loop = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        logger.info(" count :" + count);
        return records;
    }

    public void processRecords(List<String> records) throws MalformedURLException, ProtocolException, IOException {
        HttpURLConnection urlConn = null;
        String request = istexApiUrl;
        System.out.println(records.size());
        for (String id : records) {
            request = istexApiUrl + "/" + id + "/fulltext/tei";
            URL url = new URL(request);
            urlConn = (HttpURLConnection) url.openConnection();
            System.out.println(" id :" + id);
            if (urlConn != null) {
                urlConn.setDoInput(true);
                urlConn.setRequestMethod("GET");
                InputStream in = urlConn.getInputStream();
                String xml = Utilities.convertStreamToString(in);
                mm.insertExternalTeiDocument(in, id, "istex", MongoCollectionsInterface.ISTEX_TEIS, date);
                in.close();
            }
        }
    }
}
