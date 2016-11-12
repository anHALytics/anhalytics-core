package fr.inria.anhalytics.annotate.main;

import fr.inria.anhalytics.annotate.Annotator;
import fr.inria.anhalytics.annotate.Annotator.Annotator_Type;
import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.properties.AnnotateProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
            add("annotateAllNerd");
            add("annotateAllKeyTerm");
            add("annotateNerdDaily");
            add("annotateKeyTermDaily");
            add("annotateDaily");
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
            if (AnnotateProperties.getFromDate() != null || AnnotateProperties.getUntilDate() != null) {
                Utilities.updateDates(AnnotateProperties.getUntilDate(), AnnotateProperties.getFromDate());
            }
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
            if (process.equals("annotateNerdDaily")) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1);
                String todayDate = dateFormat.format(cal.getTime());
                Utilities.updateDates(todayDate, todayDate);
                annotator.annotate(Annotator_Type.NERD);
            } else if (process.equals("annotateKeyTermDaily")) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1);
                String todayDate = dateFormat.format(cal.getTime());
                Utilities.updateDates(todayDate, todayDate);
                annotator.annotate(Annotator_Type.KEYTERM);
            } else if (process.equals("annotateDaily")) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1);
                String todayDate = dateFormat.format(cal.getTime());
                Utilities.updateDates(todayDate, todayDate);
                annotator.annotate(Annotator_Type.NERD);
                annotator.annotate(Annotator_Type.KEYTERM);
            } else if (process.equals("annotateAllNerd")) {
                annotator.annotate(Annotator_Type.NERD);
            } else if (process.equals("annotateAllKeyTerm")) {
                annotator.annotate(Annotator_Type.KEYTERM);
            } else if (process.equals("annotateAll")) {
                annotator.annotate(Annotator_Type.NERD);
                annotator.annotate(Annotator_Type.KEYTERM);
            }
        } catch (ServiceException se) {
            logger.error(se.getMessage());
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
            } else if (currArg.equals("-nodates")) {
                AnnotateProperties.setProcessByDate(false);
                i++;
                continue;
            } else if (currArg.equals("-dFromDate")) {
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
            } else if (currArg.equals("-dUntilDate")) {
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
        return result;
    }

    protected static String getHelp() {
        final StringBuffer help = new StringBuffer();
        help.append("HELP ANHALYTICS-ANNOTATE \n");
        help.append("-h: displays help\n");
        help.append("-multiThread: enables using multiple threads to annotate\n");
        help.append("-dFromDate: filter start date for the process, make sure it follows the pattern : yyyy-MM-dd\n");
        help.append("-dUntilDate: filter until date for the process, make sure it follows the pattern : yyyy-MM-dd\n");
        help.append("-exe: gives the command to execute. The value should be one of these : \n");
        help.append("--reset: updates all the documents (beware about versions/updates) : \n");
        help.append("\t" + availableCommands + "\n");
        return help.toString();
    }
}
