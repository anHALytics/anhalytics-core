package fr.inria.anhalytics.index.main;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.index.Indexer;
import fr.inria.anhalytics.index.MetadataIndexer;
import fr.inria.anhalytics.index.properties.IndexProperties;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class that implements commands indexing TEIs and associated annotations
 * (appends a standoff for each entry)
 *
 * @author achraf
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static List<String> availableCommands = new ArrayList<String>() {
        {
            add("index");
            add("indexDaily");
            add("indexMtds");
        }
    };

    public static void main(String[] args) throws UnknownHostException {

        if (processArgs(args)) {
            //process name is needed to set properties.
            try {
                IndexProperties.init("index.properties");
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            if (IndexProperties.getFromDate() != null || IndexProperties.getUntilDate() != null) {
                Utilities.updateDates(IndexProperties.getUntilDate(), IndexProperties.getFromDate());
            }
            Main main = new Main();
            main.processCommand();
        } else {
            System.err.println(getHelp());
            return;
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
            } else if (currArg.equals("-exe")) {
                String command = args[i + 1];
                if (availableCommands.contains(command)) {
                    IndexProperties.setProcessName(command);
                    i++;
                    continue;
                } else {
                    System.err.println("-exe value should be one value from this list: " + availableCommands);
                    result = false;
                    break;
                }
            } else if (currArg.equals("-dFromDate")) {
                String stringDate = args[i + 1];
                if (!stringDate.isEmpty()) {
                    if (Utilities.isValidDate(stringDate)) {
                        IndexProperties.setFromDate(args[i + 1]);
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
                        IndexProperties.setUntilDate(stringDate);
                    } else {
                        System.err.println("The date given is not correct, make sure it follows the pattern : yyyy-MM-dd");
                        result = false;
                    }
                }
                i++;
                continue;
            } else {
                result = false;
            }
            i++;
            continue;
        }
        return result;
    }

    private void processCommand() throws UnknownHostException {
        Scanner sc = new Scanner(System.in);
        char reponse = ' ';
        String process = IndexProperties.getProcessName();
        Indexer esm = new Indexer();

        MetadataIndexer mi = new MetadataIndexer();
        if (process.equals("index")) {
            esm.setUpIndex(IndexProperties.getTeisIndexName());
            esm.setUpIndex(IndexProperties.getAnnotsIndexName());

            esm.indexTeiCollection();
            esm.indexAnnotations();
            esm.close();
        } else if (process.equals("indexDaily")) {
            if (esm.isIndexExists()) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1);
                String todayDate = dateFormat.format(cal.getTime());
                Utilities.updateDates(todayDate, todayDate);
            } else {
                System.err.println("Make sure both Teis index or annotations index are configured, you can choose re-init option to configure indexes.");
                esm.close();
                return;
            }

            esm.indexTeiCollection();
            esm.indexAnnotations();
            esm.close();
        } else if (process.equals("indexMtds")) {
            mi.setUpIndex(IndexProperties.getMetadataIndexName());
            mi.indexAuthors();
            mi.indexPublications();
            mi.indexOrganisations();
            mi.close();
        }
        return;
    }

    protected static String getHelp() {
        final StringBuffer help = new StringBuffer();
        help.append("HELP ANHALYTICS_INDEX \n");
        help.append("-h: displays help\n");
        help.append("-exe: followed by either :\n");
        help.append("\t" + availableCommands + "\n");
        return help.toString();
    }
}
