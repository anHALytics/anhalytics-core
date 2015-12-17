package fr.inria.anhalytics.index.main;

import fr.inria.anhalytics.index.Indexer;
import fr.inria.anhalytics.index.properties.IndexProperties;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class that implements commands indexing TEIs and associated annotations (appends a standoff for each entry)
 * @author achraf
 */
public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static List<String> availableCommands = new ArrayList<String>() {
        {
            add("tei");
            add("annotation");
        }
    };

    public static void main(String[] args) throws UnknownHostException {
        
        if (processArgs(args)) {
            //process name is needed to set properties.
            try {
                IndexProperties.init("index.properties");
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            Main main = new Main();
            main.processCommand();
        }
    }

    protected static boolean processArgs(final String[] args) {
        String currArg;
        boolean result = true;
        for (int i = 0; i < args.length; i++) {
            currArg = args[i];
            if (currArg.equals("-index")) {
                String command = args[i + 1];
                if (availableCommands.contains(command)) {
                    IndexProperties.setProcessName(command);
                    continue;
                } else {
                    System.err.println("ProcessName value should be one value from this list: " + availableCommands);
                    result = false;
                }
            }
            i++;
            continue;
        }
        return result;
    }

    private void processCommand() throws UnknownHostException  {
        String process = IndexProperties.getProcessName();
        Indexer esm = new Indexer();
        try {
            if (process.equals("tei")) {
                esm.setUpElasticSearch(process);
                // loading based on DocDB XML, with TEI conversion
                int nbDoc = esm.indexCollection();
                logger.info("Total: " + nbDoc + " documents indexed.");
            } else if (process.equals("annotation")) {
                esm.setUpElasticSearch(process);

                int nbAnnotsIndexed = esm.indexAnnotations();
                logger.info("Total: " + nbAnnotsIndexed + " annotations indexed.");
            }
        } catch (Exception e) {
            logger.error("Error when setting-up ElasticSeach cluster");
            e.printStackTrace();
        }
    }
}
