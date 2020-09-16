package fr.inria.anhalytics.annotate.main;

import fr.inria.anhalytics.annotate.Annotator;
import fr.inria.anhalytics.commons.data.AnnotatorType;
import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.properties.AnnotateProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private static final List<String> availableCommands = Arrays.asList(
            "annotateAll",
            "annotateNerd",
            "annotateKeyTerm",
            "annotateQuantities",
            "annotateQuantitiesFromPDF",
            "annotateSuperconductors",
            "annotateSuperconductorsFromPDF");

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

    private void processCommand() {
        String process = AnnotateProperties.getProcessName();

        try {
            Annotator annotator = new Annotator();
            if (process.equals("annotateNerd")) {
                annotator.annotate(AnnotatorType.NERD);
            } else if (process.equals("annotateKeyTerm")) {
                annotator.annotate(AnnotatorType.KEYTERM);
            } else if (process.equals("annotateAll")) {
                annotator.annotate(AnnotatorType.NERD);
                annotator.annotate(AnnotatorType.KEYTERM);
            } else if (process.equals("annotateQuantities")) {
                annotator.annotate(AnnotatorType.QUANTITIES);
            } else if (process.equals("annotateQuantitiesFromPDF")) {
                annotator.annotate(AnnotatorType.PDFQUANTITIES);
            } else if (process.equals("annotateSuperconductors")) {
                annotator.annotate(AnnotatorType.SUPERCONDUCTORS);
            } else if (process.equals("annotateSuperconductorsFromPDF")) {
                annotator.annotate(AnnotatorType.SUPERCONDUCTORS_PDF);
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
        help.append("--reset: updates all the documents (beware about versions/updates) : \n");
        help.append("-exe: gives the command to execute. The value should be one of these : \n");
        help.append("\t" + availableCommands + "\n");
        return help.toString();
    }
}
