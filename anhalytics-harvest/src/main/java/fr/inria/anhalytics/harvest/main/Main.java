package fr.inria.anhalytics.harvest.main;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.Harvester;
import fr.inria.anhalytics.harvest.auxiliaries.IstexHarvester;
import fr.inria.anhalytics.harvest.auxiliaries.IdListHarvester;
import fr.inria.anhalytics.harvest.crossref.CrossRef;
import fr.inria.anhalytics.harvest.crossref.OpenUrl;
import fr.inria.anhalytics.harvest.grobid.GrobidProcess;
import fr.inria.anhalytics.harvest.oaipmh.HALOAIPMHHarvester;
import fr.inria.anhalytics.harvest.teibuild.TeiCorpusBuilderProcess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.io.IOException;
import org.xml.sax.SAXException;

/**
 * Main class that implements commands for harvesting, extracting, inserting in
 * KB and generating TEI.
 *
 * @author Achraf
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static List<String> availableCommands = new ArrayList<String>() {
        {
            add("harvestAll");
            add("harvestDaily");
            add("harvestHalList");
            add("appendFulltextTei");
            //add("fetchEmbargoPublications");
            add("processGrobid");
            add("istexHarvest");
            //add("assetLegend");
            add("harvestDOI");
            add("openUrl");
            add("istexQuantities");
            add("transformMetadata");
        }
    };

    public static void main(String[] args) throws Exception {
        try {
            HarvestProperties.init("anhalytics.properties");
        } catch (PropertyException exp) {
            logger.error(exp.getMessage());
            return;
        }

        if (processArgs(args)) {
            if (HarvestProperties.isProcessByDate()) {
                if (HarvestProperties.getFromDate() != null || HarvestProperties.getUntilDate() != null) {
                    Utilities.updateDates(HarvestProperties.getUntilDate(), HarvestProperties.getFromDate());
                }
            }
            Utilities.setTmpPath(HarvestProperties.getTmpPath());
            Main main = new Main();
            main.processCommand();
        } else {
            System.err.println(getHelp());
            return;
        }
    }

    private void processCommand() throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String todayDate = dateFormat.format(cal.getTime());
        String process = HarvestProperties.getProcessName();
        GrobidProcess gp = new GrobidProcess();
        TeiCorpusBuilderProcess tcb = new TeiCorpusBuilderProcess();
        //HALOAIPMHHarvester oai = new HALOAIPMHHarvester();
        IstexHarvester ih = new IstexHarvester();
        CrossRef cr = new CrossRef();
        OpenUrl ou = new OpenUrl();

        Harvester harvester = null;

        if (process.equals("harvestAll")) {
            //HAL uses OAI PMH providing updated/new documents on daily basis.
            harvester = new HALOAIPMHHarvester();
            harvester.fetchAllDocuments();
        } else if (process.equals("harvestDaily")) {
            harvester = new HALOAIPMHHarvester();
            Utilities.updateDates(todayDate, todayDate);
            harvester.fetchAllDocuments();
        } else if (process.equals("transformMetadata")) {
            tcb.transformMetadata();
        } else if (process.equals("processGrobid")) {
            gp.processFulltexts();
        } else if (process.equals("appendFulltextTei")) {
            tcb.addGrobidFulltextToTEICorpus();
        } else if (process.equals("harvestDOI")) {
            cr.findDois();
        } else if (process.equals("openUrl")) {
            ou.getIstexUrl();
        } else if (process.equals("istexHarvest")) {
            ih.sample();
        } else if (process.equals("istexQuantities")) {

        } else if (process.equals("harvestHalList")) {
            harvester = new IdListHarvester();
            harvester.fetchAllDocuments();
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
                } else if (currArg.equals("-nodates")) {
                    HarvestProperties.setProcessByDate(false);
                    i++;
                    continue;
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
                } else if (currArg.equals("-set")) {
                    String command = pArgs[i + 1];

                    //check collection exists
                    HarvestProperties.setCollection(command);
                    i++;
                    continue;
                } else if (currArg.equals("-list")) {
                    String command = pArgs[i + 1];

                    //check collection exists
                    HarvestProperties.setListFile(command);
                    i++;
                    continue;
                } else if (currArg.equals("--reset")) {
                    HarvestProperties.setReset(true);
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
        help.append("HELP ANHALYTICS_HARVEST\n");
        help.append("-h: displays help\n");
        help.append("-dOAI: url of the OAI-PMH service\n");
        help.append("-set: select a specific set (INRIA, CNRS..), the available sets are for instance : https://api.archives-ouvertes.fr/oai/hal/?verb=ListSets \n");
        help.append("-list: give a file with a list of HAL id to harvest, one HAL ID per line \n");
        help.append("-dFromDate: filter start date for the process, make sure it follows the pattern : yyyy-MM-dd\n");
        help.append("-dFromDate: filter start date for the process, make sure it follows the pattern : yyyy-MM-dd\n");
        help.append("-dUntilDate: filter until date for the process, make sure it follows the pattern : yyyy-MM-dd\n");
        help.append("-exe: gives the command to execute. The value should be one of these : \n");
        help.append("--reset: updates all the documents (beware about versions/updates) : \n");
        help.append("\t" + availableCommands + "\n");
        return help.toString();
    }
}
