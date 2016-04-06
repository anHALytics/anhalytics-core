package fr.inria.anhalytics.harvest.main;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.commons.utilities.Utilities;
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
import java.util.Scanner;

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
            add("generateTei");
            add("generateTeiDaily");
            add("fetchEmbargoPublications");
            add("processGrobid");
            add("processGrobidDaily");
            add("appendGrobidFulltext");
            add("appendGrobidFulltextDaily");
            add("harvestIstex");
            add("assetLegend");
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
                Utilities.updateDates(HarvestProperties.getUntilDate(), HarvestProperties.getFromDate());
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
        Scanner sc = new Scanner(System.in);
        char reponse = ' ';
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String todayDate = dateFormat.format(cal.getTime());
        String process = HarvestProperties.getProcessName();
        GrobidProcess gp = new GrobidProcess();
        TeiBuilderProcess tb = new TeiBuilderProcess();
        HALOAIHarvester oai = new HALOAIHarvester();
        IstexHarvester ih = new IstexHarvester();
        if (process.equals("harvestAll")) {
            oai.fetchAllDocuments();
        } else if (process.equals("harvestDaily")) {
            Utilities.updateDates(todayDate, todayDate);
            oai.fetchAllDocuments();
        } else if (process.equals("generateTei")) {
             //xml_ids are updated, the annotation is time consuming process.      
            /*System.out.println("xml_ids used for the annotation purpose will be updated, the annotation is time consuming, continue ?(Y/N)");
            reponse = sc.nextLine().charAt(0);
            if (reponse != 'N') */
			{
                tb.buildTei();
            }
        } else if (process.equals("generateTeiDaily")) {
            Utilities.updateDates(todayDate, todayDate);
            tb.buildTei();
        }else if (process.equals("processGrobid")) {
            gp.processFulltexts();
        } else if (process.equals("processGrobidDaily")) {
            Utilities.updateDates(todayDate, todayDate);
            gp.processFulltexts();
        } else if (process.equals("appendGrobidFulltext")) {
            //xml_ids are updated, the annotation is time consuming process.      
            /*System.out.println("xml_ids used for the annotation purpose will be updated, the annotation is time consuming, continue ?(Y/N)");
            reponse = sc.nextLine().charAt(0);
            if (reponse != 'N') */
			{
                tb.appendGrobidFulltext();
            }
        } else if (process.equals("appendGrobidFulltextDaily")) {
            //xml_ids are updated, the annotation is time consuming process.
            Utilities.updateDates(todayDate, todayDate);
            tb.appendGrobidFulltext();
        } else if (process.equals("fetchEmbargoPublications")) {
            oai.fetchEmbargoPublications();
        } else if (process.equals("harvestIstex")) {
            ih.harvest();
        }else if (process.equals("assetLegend")) {
            gp.addAssetsLegend();
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
