package fr.inria.anhalytics.kb.main;

import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.kb.datamine.KnowledgeBaseFeeder;
import fr.inria.anhalytics.commons.properties.KbProperties;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Achraf
 */
public class Main {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    
    private static List<String> availableCommands = new ArrayList<String>() {
        {
            add("initKnowledgeBase");
            add("initCitationKnowledgeBase");
            add("deduplicate");
        }
    };
    
    public static void main(String[] args) throws UnknownHostException, SQLException {
        try {
            KbProperties.init("anhalytics.properties");
        } catch (Exception exp) {
            LOGGER.error(exp.getMessage());
            return;
        }
        
        if (processArgs(args)) {
            Main main = new Main();
            main.processCommand();
        } else {
            System.err.println(getHelp());
            return;
        }
    }
    
    private void processCommand() throws UnknownHostException, SQLException {
        Scanner sc = new Scanner(System.in);
        char reponse = ' ';
        String process = KbProperties.getProcessName();
        try {
            KnowledgeBaseFeeder kbf = new KnowledgeBaseFeeder();
            if (process.equals("initKnowledgeBase")) {
                //Initiates HAL knowledge base and creates working corpus TEI.
                kbf.initKnowledgeBase();
            } else if (process.equals("initCitationKnowledgeBase")) {
                kbf.processCitations();
            }
        } catch (ServiceException se) {
            LOGGER.error("Error: ", se);
        }
        return;
    }
    
    protected static boolean processArgs(final String[] pArgs) {
        boolean result = true;
        if (pArgs.length == 0) {
            result = false;
        } else {
            String currArg;
            for (int i = 0; i < pArgs.length; i++) {
                currArg = pArgs[i];
                if (currArg.equals("-h")) {
                    result = false;
                    break;
                } else if (currArg.equals("-exe")) {
                    String command = pArgs[i + 1];
                    if (availableCommands.contains(command)) {
                        KbProperties.setProcessName(command);
                        i++;
                        continue;
                    } else {
                        System.err.println("-exe value should be one value from this list: " + availableCommands);
                        result = false;
                        break;
                    }
                } else if (currArg.equals("--reset")) {
                    KbProperties.setReset(true);
                    i++;
                    continue;
                } else {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
    
    protected static String getHelp() {
        final StringBuffer help = new StringBuffer();
        help.append("HELP ANHALYTICS_KNOWLEDGE_BASE\n");
        help.append("-h: displays help\n");
        help.append("-dFromDate: filter start date for the process, make sure it follows the pattern : yyyy-MM-dd\n");
        help.append("-dUntilDate: filter until date for the process, make sure it follows the pattern : yyyy-MM-dd\n");
        help.append("-nodates: fetches entries from database with no date filtering.\n");
        help.append("-exe: gives the command to execute. The value should be one of these : \n");
        help.append("--reset: updates all the documents (beware about versions/updates) : \n");
        help.append("\t" + availableCommands + "\n");
        return help.toString();
    }
}
