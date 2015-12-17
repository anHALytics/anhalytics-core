package fr.inria.anhalytics.annotate.main;

import fr.inria.anhalytics.annotate.Annotator;
import fr.inria.anhalytics.annotate.properties.AnnotateProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class that implements command for annotating TEIs and saving the result
 * to Mongodb.
 *
 * @author Achraf
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws UnknownHostException {
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
        } else {
            System.out.println(getHelp());
            return;
        }
    }

    private void processCommand() throws UnknownHostException {
        Annotator annotator = new Annotator();
        // loading based on DocDB XML, with TEI conversion
        try {

            if (AnnotateProperties.isIsMultiThread()) {

                annotator.annotateTeiCollectionMultiThreaded();
            } else {
                annotator.annotateTeiCollection();
            }
        } catch (Exception e) {
            logger.error("Error when setting-up the annotator.");
            e.printStackTrace();
        }
        return;
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
            else if (currArg.equals("-dFromDate")) {
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
            else if (currArg.equals("-dUntilDate")) {
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
            else if (currArg.equals("-multiThread")) {
                AnnotateProperties.setIsMultiThread(true);
                continue;
            } else {
                result = false;
            }
        }
        return result;
    }

    protected static String getHelp() {
        final StringBuffer help = new StringBuffer();
        help.append("HELP ANHALYTICS-ANNOTATE \n");
        help.append("-h: displays help\n");
        help.append("-multiThread: enables using multiple threads to annotate\n");
        help.append("-dFromDate: filter start date for the process, make sure it follows the pattern : yyyy-MM-dd\n");
        help.append("-dUntilDate: filter until date for the process, make sure it follows the pattern : yyyy-MM-dd\n");
        return help.toString();
    }
}
