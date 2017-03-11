package fr.inria.anhalytics.harvest.auxiliaries;

import fr.inria.anhalytics.commons.managers.MongoCollectionsInterface;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Harvests metadata&pdf from istex and stores it.
 *
 * @author achraf
 */
public class IstexHarvester {

    protected static final Logger logger = LoggerFactory.getLogger(IstexHarvester.class);
    private static IstexHarvester harvester = null;
    private static final String istexApiUrl = "https://api.istex.fr/document";
    private MongoFileManager mm = null;

    //20 docs from each category
    private static final int sampleSize = 20;

    static final public List<String> categories
            = Arrays.asList("scienceMetrix.pharmacology & pharmacy", "scienceMetrix.chemistry", "scienceMetrix.analytical chemistry",
                    "scienceMetrix.food science", "scienceMetrix.toxicology", "scienceMetrix.virology", "scienceMetrix.physics & astronomy", "wos.chemistry, medicinal");

    public IstexHarvester() {
        this.mm = MongoFileManager.getInstance(false);
    }

    public void sample() {
        HttpURLConnection urlConn = null;
        String request, params = null;
        int count = 0;
        String json = null;
        ArrayList<Integer> rands = null;
        Map<String, List<String>> entries = new HashMap<String, List<String>>();
        List<String> ids = null;
        for (String category : categories) {
            System.out.println(category);
            String[] cat = category.split("\\.");
            logger.info("Sampling " +sampleSize+" documents from category : "+cat[1]);
            count = getClassCount(cat);
            rands = new ArrayList<>();
            ids = new ArrayList<String>();
            for (int i = 0; i < sampleSize; i++) {
                rands.add((int) (Math.random() * ((count) - 1)));
            }
            try {
                for (int rnd : rands) {
                    params = "?q=genre:research%20article%20AND%20categories."+cat[0]+":" + URLEncoder.encode(cat[1], "UTF-8") + "%20AND%20qualityIndicators.score:[8%20TO%20*]&from=" + rnd + "&size=1";

                    request = istexApiUrl + "/" + params;
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
                        ids.add((String) ((JSONObject) hits.get(0)).get("id"));
                    }

                }
                logger.info("Saving IDs for download :" +ids);
                entries.put(cat[0]+"." + cat[1], ids);
                //Download / store docs
                processRecords(entries);

            } catch (UnsupportedEncodingException ex) {
                java.util.logging.Logger.getLogger(IstexHarvester.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int getClassCount(String[] cat) {
        HttpURLConnection urlConn = null;
        String request = null;
        String json = null;
        try {
            request = istexApiUrl + "/" + "?q=genre:research%20article%20AND%20categories."+cat[0]+":" + URLEncoder.encode(cat[1], "UTF-8") + "%20AND%20qualityIndicators.score:[8%20TO%20*]"+ "&size=0";
        } catch (UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(IstexHarvester.class.getName()).log(Level.SEVERE, null, ex);
        }
        Long total = null;
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
                total = (Long) jsonObject.get("total");

                logger.info(String.format("Found %d entries for %s class.", total, cat[0]+"."+cat[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total.intValue();
    }

    private void processRecords(Map<String, List<String>> entries) throws MalformedURLException, IOException {
        HttpURLConnection urlConn = null;
        Iterator it = entries.entrySet().iterator();
        String request = istexApiUrl;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String category = (String) pair.getKey();
            List<String> ids = (List<String>) pair.getValue();
            for (String id : ids) {
                request = istexApiUrl + "/" + id + "/fulltext/pdf";
                URL url = new URL(request);
                urlConn = (HttpURLConnection) url.openConnection();
                if (urlConn != null) {
                    urlConn.setDoInput(true);
                    urlConn.setRequestMethod("GET");
                    InputStream in = urlConn.getInputStream();
                    logger.info("Downloading PDF document :"+id);
                    //Get and store pdfs along with theirs class.
                    mm.insertPDFDocument(in, id, category, MongoCollectionsInterface.ISTEX_PDFS);
                    logger.info("saved");
                    in.close();
                }
                request = istexApiUrl + "/" + id + "/fulltext/tei";
                url = new URL(request);
                urlConn = (HttpURLConnection) url.openConnection();
                if (urlConn != null) {
                    urlConn.setDoInput(true);
                    urlConn.setRequestMethod("GET");
                    InputStream in = urlConn.getInputStream();
                    logger.info("Downloading fulltext TEI document :"+id);
                    //Get and store pdfs along with theirs class.
                    mm.insertTEIDocument(in, id, category, MongoCollectionsInterface.ISTEX_TEIS);
                    logger.info("saved");
                    in.close();
                }
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
}
