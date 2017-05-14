package fr.inria.anhalytics.harvest.harvesters;

import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.data.BinaryFile;
import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Harvest implementation for ISTEX.
 *
 * @author achraf
 */
public class IstexHarvester extends Harvester {

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

    @Override
    public void fetchAllDocuments() throws IOException, SAXException, ParserConfigurationException, ParseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void fetchListDocuments() throws IOException, SAXException, ParserConfigurationException, ParseException {
        BufferedReader br = null;
        HttpURLConnection urlConn = null;
        String json = null;
        BiblioObject bo = new BiblioObject();

        try {
            // read the file with one hal id per line
            String docID = null;
            br = new BufferedReader(new FileReader(HarvestProperties.getListFile()));
            while ((docID = br.readLine()) != null) {
                if (docID.trim().length() == 0) {
                    continue;
                }
                if (!HarvestProperties.isReset() && mm.isSavedObject(docID)) {
                    logger.info("\t\t Already grabbed, Skipping...");
                    continue;
                }
                String request = "https://api.istex.fr/document/?q=id:" + docID + "&size=1&output=*";
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
                    bo = getBiblioObjectFromHit((JSONObject) hits.get(0));
                    if (bo != null) {
                        grabbedObjects.add(bo);
                    }
                }
            }
            saveObjects();
        } catch (MalformedURLException mue) {
            logger.error(mue.getMessage(), mue);
        } catch (ServiceException se) {
            logger.error(se.getMessage(), se);
            mm.save("", "blockedHarvestProcess", se.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Parses the hit entry, downloads the tei metadata and the fulltext pdf and returns a biblioObject.
     * @param hit
     * @return
     */
    private BiblioObject getBiblioObjectFromHit(JSONObject hit) throws MalformedURLException, IOException {
        BiblioObject bo = new BiblioObject();
        bo.setSource(Harvester.Source.ISTEX.getName());
        bo.setRepositoryDocId((String) (hit).get("id"));
        if ((JSONArray) (hit).get("doi") != null) {
            bo.setDoi((String) ((JSONArray) (hit).get("doi")).get(0));
        }
        if ((JSONArray) (hit).get("genre") != null) {
            bo.setPublicationType((String) ((JSONArray) (hit).get("genre")).get(0));
        }
        ArrayList<String> domains = new ArrayList<String>();
        if ((JSONArray) (hit).get("genre") != null) {
            JSONObject jo = (JSONObject) (hit).get("categories");
            JSONArray jsonArray = (JSONArray) jo.get("wos");
            if (jsonArray != null) {
                int len = jsonArray.size();
                for (int i = 0; i < len; i++) {
                    domains.add(jsonArray.get(i).toString());
                }
            }

            bo.setDomains(domains);
        }

        //get Metadata string from id
        String request = istexApiUrl + "/" + bo.getRepositoryDocId() + "/fulltext/tei";
        logger.info("Downloading fulltext TEI document :" + bo.getRepositoryDocId());
        bo.setMetadataURL(request);
        bo.setMetadata(IOUtils.toString(new URL(request), "UTF-8"));

        JSONArray fulltextArray = (JSONArray) (hit).get("fulltext");
        if (fulltextArray != null) {
            int len = fulltextArray.size();
            for (int i = 0; i < len; i++) {
                if (((JSONObject) fulltextArray.get(i)).get("extension").equals("pdf")) {
                    BinaryFile bf = new BinaryFile();
                    bf.setUrl(istexApiUrl + "/" + bo.getRepositoryDocId() + "/fulltext/pdf");
                    bo.setPdf(bf);
                    bo.setIsWithFulltext(Boolean.TRUE);
                }
            }
        }
        return bo;
    }

    /**
     * Samples a sampleSize from each category.
     */
    @Override
    public void sample() {
        HttpURLConnection urlConn = null;
        String request, params = null;
        int count = 0;
        String json = null;
        ArrayList<Integer> rands = null;
        BiblioObject bo = new BiblioObject();

        try {
            for (String category : categories) {
                String[] cat = category.split("\\.");
                logger.info("\t\t\t\t Sampling " + sampleSize + " documents from category : " + cat[1]);
                count = getCategoryDocCount(cat);
                rands = new ArrayList<>();
                //we pick some pages randomly because we have no idea how istex is building pages..
                for (int i = 0; i < sampleSize; i++) {
                    rands.add((int) (Math.random() * ((count) - 1)));
                }
                for (int rnd : rands) {
                    params = "?q=genre:research%20article%20AND%20categories." + cat[0] + ":" + URLEncoder.encode(cat[1], "UTF-8") + "%20AND%20qualityIndicators.score:[8%20TO%20*]&from=" + rnd + "&size=1&output=*";

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

                        //we get only the first element.
                        bo = getBiblioObjectFromHit((JSONObject) hits.get(0));
                        grabbedObjects.add(bo);
                    }

                }
//                logger.info("Saving IDs for download :" + ids);
                //Download / store docs
            }
            saveObjects();
        } catch (UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(IstexHarvester.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns docCount from each category.
     */
    private int getCategoryDocCount(String[] cat) {
        HttpURLConnection urlConn = null;
        String request = null;
        String json = null;
        try {
            request = istexApiUrl + "/" + "?q=genre:research%20article%20AND%20categories." + cat[0] + ":" + URLEncoder.encode(cat[1], "UTF-8") + "%20AND%20qualityIndicators.score:[8%20TO%20*]" + "&size=0";
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

                logger.info(String.format("Found %d entries for %s class.", total, cat[0] + "." + cat[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total.intValue();
    }

}
