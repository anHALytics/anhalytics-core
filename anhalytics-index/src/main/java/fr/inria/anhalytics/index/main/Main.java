package fr.inria.anhalytics.index.main;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.index.DocumentIndexer;
import fr.inria.anhalytics.index.KnowledgeBaseIndexer;
import fr.inria.anhalytics.commons.properties.IndexProperties;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Arrays;
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

    private static List<String> availableCommands
            = Arrays.asList("indexAll", "indexDaily", "indexMetadata", "indexFulltext", "indexAnnotations", "indexKB");

    public static void main(String[] args) throws UnknownHostException {

        if (processArgs(args)) {
            //process name is needed to set properties.
            try {
                IndexProperties.init("anhalytics.properties");
            } catch (Exception e) {
                logger.error(e.getMessage());
                return;
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
            } else if (currArg.equals("-nodates")) {
                IndexProperties.setProcessByDate(false);
                i++;
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
            } 
//            else if (currArg.equals("--reset")) {
//                IndexProperties.setReset(true);
//                i++;
//                continue;
//            } 
            else {
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
        DocumentIndexer esm = new DocumentIndexer();
        KnowledgeBaseIndexer mi = new KnowledgeBaseIndexer(); // this is the KB in fact !

        if (process.equals("indexAll")) {
            System.out.println("The existing indices will be deleted and reseted, continue ?(Y/N)");
            reponse = sc.nextLine().charAt(0);
            if (reponse != 'N') {
                esm.setUpIndex(IndexProperties.getFulltextTeisIndexName());
                esm.setUpIndex(IndexProperties.getNerdAnnotsIndexName());
                esm.setUpIndex(IndexProperties.getKeytermAnnotsIndexName());
                //mi.setUpIndex(IndexProperties.getMetadataIndexName());

                int nbDoc = esm.indexTeiFulltextCollection();
                logger.info("Total: " + nbDoc + " TEI documents indexed.");
                int nbNerdAnnot = esm.indexNerdAnnotations();
                logger.info("Total: " + nbNerdAnnot + " NERD annotations indexed.");
                int nbKeytermAnnot = esm.indexKeytermAnnotations();
                logger.info("Total: " + nbKeytermAnnot + " Keyterm annotations indexed.");
            }

            // TBD: counters would be nice
            //mi.indexAuthors();
            //mi.indexPublications();
            //mi.indexOrganisations();
        } else if (process.equals("indexDaily")) {
            if (esm.isIndexExists()) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1);
                String todayDate = dateFormat.format(cal.getTime());
                Utilities.updateDates(todayDate, todayDate);
            } else {
                System.err.println("Make sure both TEI index or annotations index are configured, you can choose re-init option to configure indexes.");
                esm.close();
                return;
            }
            int nbDoc = esm.indexTeiFulltextCollection();
            logger.info("Total: " + nbDoc + " fulltext documents indexed.");
            int nbDoc1 = esm.indexTeiMetadataCollection();
            logger.info("Total: " + nbDoc1 + " metadata documents indexed.");
            int nbNerdAnnot = esm.indexNerdAnnotations();
            logger.info("Total: " + nbNerdAnnot + " NERD annotations indexed.");
            int nbKeytermAnnot = esm.indexKeytermAnnotations();
            logger.info("Total: " + nbKeytermAnnot + " Keyterm annotations indexed.");

            // TBD: daily KB refresh and indexing ?
        } else if (process.equals("indexFulltext")) {
            System.out.println("The existing indices will be deleted and reseted, continue ?(Y/N)");
            reponse = sc.nextLine().charAt(0);
            if (reponse != 'N') {
                esm.setUpIndex(IndexProperties.getFulltextTeisIndexName());
                int nbDoc = esm.indexTeiFulltextCollection();
                logger.info("Total: " + nbDoc + " TEI documents indexed.");
            }
        } else if (process.equals("indexMetadata")) {
            System.out.println("The existing indices will be deleted and reseted, continue ?(Y/N)");
            reponse = sc.nextLine().charAt(0);
            if (reponse != 'N') {
                esm.setUpIndex(IndexProperties.getMetadataTeisIndexName());
                int nbDoc = esm.indexTeiMetadataCollection();
                logger.info("Total: " + nbDoc + " TEI documents indexed.");
            }
        } else if (process.equals("indexAnnotations")) {
            System.out.println("The existing indices will be deleted and reseted, continue ?(Y/N)");
            reponse = sc.nextLine().charAt(0);
            if (reponse != 'N') {
                esm.setUpIndex(IndexProperties.getNerdAnnotsIndexName());
                esm.setUpIndex(IndexProperties.getKeytermAnnotsIndexName());

                int nbNerdAnnot = esm.indexNerdAnnotations();
                logger.info("Total: " + nbNerdAnnot + " NERD annotations indexed.");
                int nbKeytermAnnot = esm.indexKeytermAnnotations();
                logger.info("Total: " + nbKeytermAnnot + " Keyterm annotations indexed.");
            }
        } else if (process.equals("indexKB")) {
            System.out.println("The existing indices will be deleted and reseted, continue ?(Y/N)");
            reponse = sc.nextLine().charAt(0);

            if (reponse != 'N') {
                mi.setUpIndex(IndexProperties.getKbIndexName());
            }
            try {
                int nbAuthtors = mi.indexAuthors();
                logger.info("Total: " + nbAuthtors + " authors indexed.");
                int nbPubs = mi.indexPublications();
                logger.info("Total: " + nbPubs + " publications indexed.");
                int nbOrgs = mi.indexOrganisations();
                logger.info("Total: " + nbOrgs + " organisations indexed.");
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        esm.close();
        mi.close();
        return;
    }

    protected static String getHelp() {
        final StringBuffer help = new StringBuffer();
        help.append("HELP ANHALYTICS_INDEX \n");
        help.append("-h: displays help\n");
        help.append("-exe: followed by either :\n");
        help.append("--reset: skip items that are already processed(beware about versions/updates) : \n");
        help.append("\t" + availableCommands + "\n");
        return help.toString();
    }
}
