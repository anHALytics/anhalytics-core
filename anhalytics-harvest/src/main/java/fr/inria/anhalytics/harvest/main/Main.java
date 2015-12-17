package fr.inria.anhalytics.harvest.main;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.datamine.GrobidMiner;
import fr.inria.anhalytics.datamine.HALMiner;
import fr.inria.anhalytics.harvest.HALOAIHarvester;
import fr.inria.anhalytics.harvest.auxiliaries.IstexHarvester;
import fr.inria.anhalytics.harvest.grobid.GrobidProcess;
import fr.inria.anhalytics.harvest.properties.HarvestProperties;
import fr.inria.anhalytics.harvest.teibuild.TeiBuilderProcess;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Main class that implements commands for harvesting, extracting, inserting in
 * KB and generating TEI.
 *
 * @author Achraf
 */
public class Main {

    private static List<String> availableCommands = new ArrayList<String>() {
        {
            add("harvestAll");
            add("harvestDaily");
            add("fetchEmbargoPublications");
            add("processGrobid");
            add("generateTei");
            add("harvestIstex");
            add("seedKnowledgeBase");
            add("deduplicate");
        }
    };

    public static void main(String[] args) throws UnknownHostException {
        try {
            HarvestProperties.init("harvest.properties");
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file of harvest properties harvest.properties", exp);
        }

        if (processArgs(args)) {
            if (HarvestProperties.getFromDate() != null || HarvestProperties.getUntilDate() != null) {
                Utilities.updateDates(HarvestProperties.getFromDate(), HarvestProperties.getUntilDate());
            }
            Utilities.setTmpPath(HarvestProperties.getTmpPath());
            Main main = new Main();
            main.processCommand();
        } else {
             System.err.println(getHelp());
             return;
        }
    }

    private void processCommand() throws UnknownHostException {
        String process = HarvestProperties.getProcessName();
        GrobidProcess gp = new GrobidProcess();
        TeiBuilderProcess tb = new TeiBuilderProcess();
        HALOAIHarvester oai = new HALOAIHarvester();
        GrobidMiner gm = new GrobidMiner();
        HALMiner hm = new HALMiner();
        IstexHarvester ih = new IstexHarvester();
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
        } else if (process.equals("fetchEmbargoPublications")) {
            oai.fetchEmbargoPublications();
            return;
        } else if (process.equals("processGrobid")) {
            gp.processFulltext();
            return;
        } else if (process.equals("seedKnowledgeBase")) {
            hm.mine();
            return;
        } else if (process.equals("generateTei")) {
            //warn about xml_id modifications
            tb.build();
            return;
        } else if (process.equals("harvestIstex")) {
            ih.harvest();
            return;
        }
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
                } else if (currArg.equals("-dFromDate")) {
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
                } else if (currArg.equals("-dUntilDate")) {
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
                } else if (currArg.equals("-exe")) {
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
