package fr.inria.anhalytics.test;

import fr.inria.anhalytics.annotate.Annotator;
import fr.inria.anhalytics.commons.managers.MongoManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.OAIHarvester;
import fr.inria.anhalytics.harvest.grobid.GrobidProcess;
import fr.inria.anhalytics.index.ElasticSearchManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author Achraf
 */
public class TestProcess {
    
    private static String grobid_host = null;
    private static String grobid_port = null;
    private static String tmp_path = null;
    
    private static MongoManager mm = new MongoManager(true);
    
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, ParseException, Exception{
    
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("test.properties"));
            grobid_host = prop.getProperty("test.grobid_host");
            grobid_port = prop.getProperty("test.grobid_port");
            tmp_path = prop.getProperty("test.tmpPath");
        } catch (Exception e) {
            e.printStackTrace();
        }
        OAIHarvester oai = new OAIHarvester(mm, "http://api.archives-ouvertes.fr/oai/hal");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String date = dateFormat.format(cal.getTime());
        Utilities.updateDates(date, date);
        System.out.println("==================================");
        System.out.println("=======Harvesting documents=======");
        System.out.println("==================================");
        oai.fetchDocumentsByDate(date);
        System.out.println("===============Done===============");
        GrobidProcess gp = new GrobidProcess(grobid_host, grobid_port, mm);
        System.out.println("==================================");
        System.out.println("=========TEI extractions==========");
        System.out.println("==================================");
        gp.processFulltext();
        System.out.println("===============Done===============");
        System.out.println("==================================");
        System.out.println("=======Annotate collection========");
        System.out.println("==================================");
        Annotator annotator = new Annotator(mm);
        annotator.annotateCollectionMultiThreaded();
        System.out.println("===============Done===============");
        System.out.println("==================================");
        System.out.println("=========Index collection=========");
        System.out.println("==================================");
        ElasticSearchManager es = new ElasticSearchManager(mm);
        es.indexCollection();
        System.out.println("===============Done===============");
    }
}
