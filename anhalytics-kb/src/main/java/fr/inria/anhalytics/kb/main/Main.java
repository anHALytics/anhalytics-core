package fr.inria.anhalytics.kb.main;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.kb.datamine.GrobidMiner;
import fr.inria.anhalytics.kb.datamine.HALMiner;
import fr.inria.anhalytics.kb.properties.KbProperties;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Achraf
 */
public class Main {

    private static List<String> availableCommands = new ArrayList<String>() {
        {
            
            add("initKnowledgeBase");
            add("initKnowledgeBaseDaily");
            add("initCitationKnowledgeBase");
            add("deduplicate");
        }
    };

    public static void main(String[] args) throws UnknownHostException, SQLException {
        try {
            KbProperties.init("kb.properties");
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file of harvest properties ingest.properties", exp);
        }

        if (processArgs(args)) {
            if (KbProperties.getFromDate() != null || KbProperties.getUntilDate() != null) {
                Utilities.updateDates(KbProperties.getUntilDate(), KbProperties.getFromDate());
            }
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
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String todayDate = dateFormat.format(cal.getTime());
        String process = KbProperties.getProcessName();
        GrobidMiner gm = new GrobidMiner();
        HALMiner hm = new HALMiner();
        if (process.equals("initKnowledgeBase")) {
            //Initiates HAL knowledge base and creates working corpus TEI.
            hm.initKnowledgeBase();
        } else if (process.equals("initKnowledgeBaseDaily")) {
            //Initiates HAL knowledge base and creates working corpus TEI.
            Utilities.updateDates(todayDate, todayDate);
            hm.initKnowledgeBase();
        }  else if (process.equals("initCitationKnowledgeBase")) {
            gm.processCitations();
        }
        return;
    }

    protected static boolean processArgs(final String[] pArgs) {
        boolean result = true;
        if (pArgs.length == 0) {
            System.out.println(getHelp());
        } else {
            String currArg;
            for (int i = 0; i < pArgs.length; i++) {
                currArg = pArgs[i];
                if (currArg.equals("-h")) {
                    System.out.println(getHelp());
                    result = false;
                    break;
                } else if (currArg.equals("-nodates")) {
                    KbProperties.setProcessByDate(false);
                    i++;
                    continue;
                } else if (currArg.equals("-dFromDate")) {
                    String stringDate = pArgs[i + 1];
                    if (!stringDate.isEmpty()) {
                        if (Utilities.isValidDate(stringDate)) {
                            KbProperties.setFromDate(pArgs[i + 1]);
                        } else {
                            System.err.println("The date given is not correct, make sure it follows the pattern : yyyy-MM-dd");
                            result = false;
                        }
                    }
                    i++;
                    continue;
                } else if (currArg.equals("-dUntilDate")) {
                    String stringDate = pArgs[i + 1];
                    if (!stringDate.isEmpty()) {
                        if (Utilities.isValidDate(stringDate)) {
                            KbProperties.setUntilDate(stringDate);
                        } else {
                            System.err.println("The date given is not correct, make sure it follows the pattern : yyyy-MM-dd");
                            result = false;
                        }
                    }
                    i++;
                    continue;
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
        help.append("HELP ANHALYTICS_HARVEST\n");
        help.append("-h: displays help\n");
        help.append("-dOAI: url of the OAI-PMH service\n");
        help.append("-dFromDate: filter start date for the process, make sure it follows the pattern : yyyy-MM-dd\n");
        help.append("-dUntilDate: filter until date for the process, make sure it follows the pattern : yyyy-MM-dd\n");
        help.append("-exe: gives the command to execute. The value should be one of these : \n");
        help.append("\t" + availableCommands + "\n");
        return help.toString();
    }
}
