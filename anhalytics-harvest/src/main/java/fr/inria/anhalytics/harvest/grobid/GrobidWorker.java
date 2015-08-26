package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.managers.MongoManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Achraf
 */
abstract class GrobidWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GrobidWorker.class);
    private InputStream content;
    protected MongoManager mm;
    private String grobid_host;
    private String grobid_port;
    protected String date;
    protected String filename;

    public GrobidWorker(InputStream content, MongoManager mongoManager, String grobidHost, String grobidPort, String date) {
        this.content = content;
        this.mm = mongoManager;
        this.grobid_host = grobidHost;
        this.grobid_port = grobidPort;
        this.date = date;
        this.filename = mm.getCurrentFilename();
    }

    @Override
    public void run() {
        try {
            long startTime = System.nanoTime();
            System.out.println(Thread.currentThread().getName() + " Start. Processing = " + filename);
            processCommand();
            long endTime = System.nanoTime();
            System.out.println(Thread.currentThread().getName() + " End. :" + (endTime - startTime) / 1000000 + " ms");
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(GrobidFulltextWorker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(GrobidFulltextWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processCommand() throws IOException, ParseException {
        try {
            System.out.println(filename);
            GrobidService grobidService = new GrobidService(grobid_host, grobid_port, 2, -1, true, date);
            String zipPath = grobidService.runFullTextGrobid(content);
            storeToGridfs(zipPath);
            (new File(zipPath)).delete();
            logger.debug("\t\t "+filename+" for "+date+" processed.");
        } catch (RuntimeException e) {
            if(e.getMessage().contains("timed out")){
                 mm.save(Utilities.getHalIDFromFilename(filename), "processGrobid", "timed out", date);
            } else if(e.getMessage().contains("failed")){
                 mm.save(Utilities.getHalIDFromFilename(filename), "processGrobid", "failed", date);
            }
            e.printStackTrace();
        }
    }

    protected void storeToGridfs(String zipDirectoryPath) {};

    @Override
    public String toString() {
        return this.filename;
    }
}
