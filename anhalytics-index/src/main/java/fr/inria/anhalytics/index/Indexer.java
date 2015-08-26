package fr.inria.anhalytics.index;

import fr.inria.anhalytics.commons.managers.MongoManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Achraf
 */
public class Indexer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Indexer.class);

    private static List<String> availableCommands = new ArrayList<String>() {
        {
            add("tei");
            add("annotation");
        }
    };

    /**
     * Set-up ElasticSearch.
     */
    public static void main(String[] args)
            throws IOException, ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        String currArg;
        MongoManager mm = new MongoManager(false);
        ElasticSearchManager esm = new ElasticSearchManager(mm);
        for (int i = 0; i < args.length; i++) {

            currArg = args[i];

            if (currArg.equals("-index")) {
                String process = args[i + 1];
                if (!process.isEmpty()) {
                    try {
                        if (process.equals("tei")) {
                            esm.setUpElasticSearch(process);
                            // loading based on DocDB XML, with TEI conversion
                            int nbDoc = esm.indexCollection();
                            System.out.println("Total: " + nbDoc + " documents indexed.");
                        } else if (process.equals("annotation")) {
                            esm.setUpElasticSearch(process);

                            int nbAnnotsIndexed = esm.indexAnnotations();
                            LOGGER.debug("Total: " + nbAnnotsIndexed + " annotations indexed.");
                        } else {
                            System.err.println("The date given is not correct, make sure it follows the pattern : yyyy-MM-dd");
                            return;
                        }
                    } catch (Exception e) {
                        System.err.println("Error when setting-up ElasticSeach cluster");
                        e.printStackTrace();
                    }
                }
                i++;
                continue;
            }
        }

    }

}
