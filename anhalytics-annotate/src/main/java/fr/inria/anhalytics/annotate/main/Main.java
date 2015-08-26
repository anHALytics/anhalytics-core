package fr.inria.anhalytics.annotate.main;

import fr.inria.anhalytics.annotate.Annotator;
import fr.inria.anhalytics.annotate.properties.AnnotateProperties;
import fr.inria.anhalytics.commons.managers.MongoManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achraf
 */
public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    private final MongoManager mm = new MongoManager(false);
    
    public static void main(String[] args) throws IOException, ParserConfigurationException {
        try {
            AnnotateProperties.init("annotate.properties");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (processArgs(args)) {
            if (AnnotateProperties.getFromDate() != null || AnnotateProperties.getUntilDate() != null) {
                Utilities.updateDates(AnnotateProperties.getFromDate(), AnnotateProperties.getUntilDate());
            }
            Main main = new Main();
            main.processCommand();
        } else return;
    }
    
    private void processCommand() throws IOException, ParserConfigurationException {
        int nbAnnots;
        Annotator annotator = new Annotator(mm);
        // loading based on DocDB XML, with TEI conversion
        try {
            
            if (AnnotateProperties.isIsMultiThread()) {
                nbAnnots = annotator.annotateCollectionMultiThreaded();
            } else {
                nbAnnots = annotator.annotateCollection();
            }
                    
            logger.debug("Total: " + nbAnnots + " annotations produced.");
        } catch (Exception e) {
            System.err.println("Error when setting-up the annotator.");
            e.printStackTrace();
        }
    
    }
    protected static boolean processArgs(final String[] args) {
        String currArg;
        boolean result = true;
        for (int i = 0; i < args.length; i++) {
            currArg = args[i];
            if (currArg.equals("-h")) {
                System.out.println(getHelp());
                continue;
            }
            if (currArg.equals("-dFromDate")) {
                String stringDate = args[i + 1];
                if (!stringDate.isEmpty()) {
                    if (Utilities.isValidDate(stringDate)) {
                        AnnotateProperties.setFromDate(args[i + 1]);
                    } else {
                        System.err.println("The date given is not correct, make sure it follows the pattern : yyyy-MM-dd");
                        result = false;
                    }
                }
                i++;
                continue;
            }
            if (currArg.equals("-dUntilDate")) {
                String stringDate = args[i + 1];
                if (!stringDate.isEmpty()) {
                    if (Utilities.isValidDate(stringDate)) {
                        AnnotateProperties.setUntilDate(stringDate);
                    } else {
                        System.err.println("The date given is not correct, make sure it follows the pattern : yyyy-MM-dd");
                        result = false;
                    }
                }
                i++;
                continue;
            }
            if (currArg.equals("-multiThread")) {
                AnnotateProperties.setIsMultiThread(true);
                continue;
            }
        }
        return result;
    }
    
    protected static String getHelp() {
        final StringBuffer help = new StringBuffer();
        help.append("HELP ANNOTATE_HAL \n");
        help.append("-h: displays help\n");
        return help.toString();
    }
}
