package fr.inria.anhalytics.harvest.main;

import fr.inria.anhalytics.commons.managers.MongoManager;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.OAIHarvester;
import fr.inria.anhalytics.harvest.grobid.GrobidProcess;
import fr.inria.anhalytics.harvest.properties.HarvestProperties;
import fr.inria.anhalytics.harvest.teibuild.TeiBuilderProcess;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author Achraf
 */
public class Main {

    private static List<String> availableCommands = new ArrayList<String>() {
        {
            add("harvestAll");
            add("harvestDaily");
            add("processGrobid");
            add("buildTei");
        }
    };
    private final MongoManager mm = new MongoManager(false);
    //private static int nullBinaries = 0;

    public static void main(String[] args) throws IOException, ParserConfigurationException {
        try {
            HarvestProperties.init("harvest.properties");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (processArgs(args)) {
            if (HarvestProperties.getFromDate() != null || HarvestProperties.getUntilDate() != null) {
                Utilities.updateDates(HarvestProperties.getFromDate(), HarvestProperties.getUntilDate());
            }
            Utilities.setTmpPath(HarvestProperties.getTmpPath());
            Main main = new Main();
            main.processCommand();
        }
    }

    private void processCommand() throws IOException, ParserConfigurationException {
        String process = HarvestProperties.getProcessName();
        GrobidProcess gp = new GrobidProcess(mm);
        TeiBuilderProcess tb = new TeiBuilderProcess(mm);
        OAIHarvester oai = new OAIHarvester(mm);//tb renamed (Process suffix)
        if (process.equals("harvestAll")) {
            oai.fetchAllDocuments();
            gp.processFulltext();
            return;
        } else if (process.equals("harvestDaily")) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String date = dateFormat.format(cal.getTime());
            Utilities.updateDates(date, date);
            oai.fetchDocumentsByDate(date);
            gp.processFulltext();
            return;
        } else if (process.equals("processGrobid")) {
            //clearTmpDirectory();           
            gp.processFulltext();
            return;
        } else if (process.equals("buildTei")) {
            tb.build();
            return;
        }
    }

    protected static boolean processArgs(final String[] pArgs) {
        boolean result = true;
        HarvestProperties.setOaiUrl("http://api.archives-ouvertes.fr/oai/hal");
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
                }
                if (currArg.equals("-gH")) {
                    HarvestProperties.setPath2grobidHome(pArgs[i + 1]);
                    if (pArgs[i + 1] != null) {
                        HarvestProperties.setPath2grobidProperty((pArgs[i + 1]) + File.separator + "config" + File.separator + "grobid.properties");
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-dOAI")) {
                    //check if url pattern && requestable
                    HarvestProperties.setOaiUrl(pArgs[i + 1]);
                    i++;
                    continue;
                }
                if (currArg.equals("-dFromDate")) {
                    String stringDate = pArgs[i + 1];
                    if (!stringDate.isEmpty()) {
                        if (Utilities.isValidDate(stringDate)) {
                            HarvestProperties.setFromDate(pArgs[i + 1]);
                        } else {
                            System.err.println("The date given is not correct, make sure it follows the pattern : yyyy-MM-dd");
                            result = false;
                        }
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-dUntilDate")) {
                    String stringDate = pArgs[i + 1];
                    if (!stringDate.isEmpty()) {
                        if (Utilities.isValidDate(stringDate)) {
                            HarvestProperties.setUntilDate(stringDate);
                        } else {
                            System.err.println("The date given is not correct, make sure it follows the pattern : yyyy-MM-dd");
                            result = false;
                        }
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-exe")) {
                    String command = pArgs[i + 1];
                    if (availableCommands.contains(command)) {
                        HarvestProperties.setProcessName(command);
                        i++;
                        continue;
                    } else {
                        System.err.println("-exe value should be one value from this list: " + availableCommands);
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

    protected static String getHelp() {
        final StringBuffer help = new StringBuffer();
        help.append("HELP HAL_OAI_HARVESTER\n");
        help.append("-h: displays help\n");
        help.append("-exe: gives the command to execute. The value should be one of these : \n");
        help.append("\t" + availableCommands + "\n");
        return help.toString();
    }
}
