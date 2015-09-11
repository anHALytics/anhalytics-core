package fr.inria.anhalytics.test.test;

import com.github.tlrx.elasticsearch.test.EsSetup;
import static com.github.tlrx.elasticsearch.test.EsSetup.createIndex;
import static com.github.tlrx.elasticsearch.test.EsSetup.delete;
import static com.github.tlrx.elasticsearch.test.EsSetup.deleteAll;
import static com.github.tlrx.elasticsearch.test.EsSetup.index;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import fr.inria.anhalytics.commons.exceptions.UnreachableGrobidServiceException;
import fr.inria.anhalytics.commons.managers.MongoManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.OAIHarvester;
import fr.inria.anhalytics.harvest.grobid.GrobidProcess;
import fr.inria.anhalytics.harvest.properties.HarvestProperties;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Achraf
 */
public class ProcessTest {
    
    static EsSetup esSetup;
        
    private static final String DATABASE_NAME = "anhalytics";

    private static final MongodStarter starter = MongodStarter.getDefaultInstance();
    
    private static MongodExecutable _mongodExe;
    private static MongodProcess _mongod;

    private static MongoManager mm ;
    private static DB db;
    
    private static final String GROBID_HOST = "locahost";
    private static final String GROBID_PORT = "8080";
    
    @BeforeClass
    public static void setESandMongodb() throws IOException {
        // Instantiates a local node & client
        esSetup = new EsSetup();
        // Clean all, and creates some indices
        esSetup.execute(
                deleteAll(),
                createIndex("my_index_1")
                        //.withMapping("annotation", fromClassPath("/home/azhar/NetBeansProjects/anHALytics-core/anhalytics-index/src/main/resources/elasticSearch/annotation.json"))
                ,
                createIndex("my_index_2")
                        //.withMapping("npl", fromClassPath("/home/azhar/NetBeansProjects/anHALytics-core/anhalytics-index/src/main/resources/elasticSearch/npl.json"))
                        //.withData(fromClassPath("path/to/bulk.json"))
        );
        
        
        _mongodExe = starter.prepare(new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(12345, Network.localhostIsIPv6()))
                .build());
        _mongod = _mongodExe.start();

        MongoClient _mongo = new MongoClient("localhost", 12345);
        db = _mongo.getDB(DATABASE_NAME);
        (mm = new MongoManager(true)).setDB(db);
        System.out.println("ElasticSearch and Mongodb are ready to use...");
    }
 
    @Test public void testESisOk(){
        // check if the index exists
        assertTrue(esSetup.exists("my_index_2"));
        // Index a new document
        esSetup.execute(index("my_index_2", "type1", "1").withSource("{ \"field1\" : \"value1\" }"));
        // Count the number of documents
        Long nb = esSetup.countAll();
        assertTrue(nb>0);
        // Delete a document
        esSetup.execute(delete("my_index_2", "type1", "1"));
        // Clean all indices
        esSetup.execute(deleteAll());
        nb = esSetup.countAll();
        assertTrue(nb==0);
    }
    
    @Test public void testFetchDocumentsByDate() throws ParserConfigurationException, IOException, InterruptedException {
        System.out.println("Test harvesting..");

        HarvestProperties.setOaiUrl("http://api.archives-ouvertes.fr/oai/hal");
        OAIHarvester oaih = new OAIHarvester(mm);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        //cal.add(Calendar.DATE, -1);
        String date = dateFormat.format(cal.getTime());
        Utilities.updateDates(date, date);

        Runnable fooRunner = new MyRunnable(oaih, date);

        Thread fooThread = new Thread(fooRunner);
        fooThread.start();

        Thread.sleep(60000);

        fooThread.interrupt();

        List<GridFSDBFile> f = (new GridFS(db, MongoManager.ADDITIONAL_TEIS)).find((DBObject)null);
        assertTrue(f.size() > 1);
    }
            
    @Test public void testTeiExtract() throws IOException {
        System.out.println("Test tei extract..");
        GrobidProcess gp = new GrobidProcess(mm);
        try{
            HarvestProperties.setGrobidHost(GROBID_HOST);
            HarvestProperties.setGrobidPort(GROBID_PORT);
            gp.processFulltext();
            List<GridFSDBFile> f = (new GridFS(db, MongoManager.GROBID_TEIS)).find((DBObject) null);
            assertTrue(f.size() > 0);
        } catch(UnreachableGrobidServiceException e){
            System.out.println(e.getMessage());
            // tests should be skipped !
        }
    }
    
    @AfterClass
    public static void tearDownESandMongodb() {
        // This will stop and clean the local node
        System.out.println("Shutting down ElasticSearch and Mongodb...");
        esSetup.terminate();
        
        _mongod.stop();
        _mongodExe.stop();
        System.out.println("ElasticSearch and Mongodb are closed.");
    }
}
