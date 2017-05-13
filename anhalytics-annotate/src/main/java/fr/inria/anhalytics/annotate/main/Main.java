package fr.inria.anhalytics.annotate.main;

import fr.inria.anhalytics.annotate.Annotator;
import fr.inria.anhalytics.commons.data.Processings;
import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.properties.AnnotateProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class that implements command for annotating TEIs and saving the result
 * to Mongodb.
 *
 * @author Achraf, Patrice
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static List<String> availableCommands = new ArrayList<String>() {
        {
            add("annotateAll");
            add("annotateNerd");
            add("annotateKeyTerm");
            add("annotateQuantities");
            add("annotateQuantitiesFromPDF");
        }
    };

    public static void main(String[] args) throws UnknownHostException {
        try {
            AnnotateProperties.init("anhalytics.properties");
        } catch (PropertyException e) {
            logger.error(e.getMessage());
            return;
        }

        if (processArgs(args)) {
            Utilities.setTmpPath(AnnotateProperties.getTmp());
            Main main = new Main();
            main.processCommand();
        } else {
            System.out.println(getHelp());
            return;
        }
    }

    private void processCommand() throws UnknownHostException {
        String process = AnnotateProperties.getProcessName();

        try {
            Annotator annotator = new Annotator();
            if (process.equals("annotateNerd")) {
                annotator.annotate(Processings.NERD);
            } else if (process.equals("annotateKeyTerm")) {
                annotator.annotate(Processings.KEYTERM);
            } else if (process.equals("annotateAll")) {
                annotator.annotate(Processings.NERD);
                annotator.annotate(Processings.KEYTERM);
            } else if (process.equals("annotateQuantities")) {
                annotator.annotate(Processings.QUANTITIES);
            } else if (process.equals("annotateQuantitiesFromPDF")) {
                annotator.annotate(Processings.PDFQUANTITIES);
            }
        } catch (ServiceException se) {
            logger.error(se.getMessage());
        }
    }

    protected static boolean processArgs(final String[] args) {
        String currArg;
        boolean result = true;

        if (args.length == 0) {
            result = false;
        } else {
            for (int i = 0; i < args.length; i++) {
                currArg = args[i];
                if (currArg.equals("-h")) {
                    result = false;
                    break;
                } else if (currArg.equals("-multiThread")) {
                    AnnotateProperties.setIsMultiThread(true);
                    continue;
                } else if (currArg.equals("-exe")) {
                    String command = args[i + 1];
                    if (availableCommands.contains(command)) {
                        AnnotateProperties.setProcessName(command);
                        i++;
                        continue;
                    } else {
                        System.err.println("-exe value should be one value from this list: " + availableCommands);
                        result = false;
                        break;
                    }
                } else if (currArg.equals("--reset")) {
                    AnnotateProperties.setReset(true);
                    i++;
                    continue;
                } else {
                    result = false;
                }
            }
        }
        return result;
    }

    protected static String getHelp() {
        final StringBuffer help = new StringBuffer();
        help.append("HELP ANHALYTICS-ANNOTATE \n");
        help.append("-h: displays help\n");
        help.append("-multiThread: enables using multiple threads to annotate\n");
        help.append("-nodates: fetches entries from database with no date filtering.\n");
        help.append("-exe: gives the command to execute. The value should be one of these : \n");
        help.append("--reset: updates all the documents (beware about versions/updates) : \n");
        help.append("\t" + availableCommands + "\n");
        return help.toString();
    }
}
