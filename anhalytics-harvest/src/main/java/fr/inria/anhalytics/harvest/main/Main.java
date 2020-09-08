package fr.inria.anhalytics.harvest.main;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.crossref.CrossRef;
import fr.inria.anhalytics.harvest.crossref.OpenUrl;
import fr.inria.anhalytics.harvest.grobid.GrobidProcess;
import fr.inria.anhalytics.harvest.harvesters.ArxivHarvester;
import fr.inria.anhalytics.harvest.harvesters.HALOAIPMHHarvester;
import fr.inria.anhalytics.harvest.harvesters.Harvester;
import fr.inria.anhalytics.harvest.harvesters.IstexHarvester;
import fr.inria.anhalytics.harvest.teibuild.TeiCorpusBuilderProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;

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
            add("harvestFromFilesystem");
            add("harvestAll");
            add("transformMetadata");
            add("processGrobid");
            add("appendFulltextTei");
            add("harvestList");
            add("sample");
//            add("harvestDOI");
//            add("openUrl");
        }
    };

    public static final Map<String, Harvester> harvestersMap = new HashMap<String, Harvester>() {
        {
            put(Harvester.Source.HAL.getName(), new HALOAIPMHHarvester());
            put(Harvester.Source.ISTEX.getName(), new IstexHarvester());
            put(Harvester.Source.ARXIV.getName(), new ArxivHarvester());
        }
    };


    public static void main(String[] args) throws Exception {
        try {
            HarvestProperties.init("anhalytics.properties");
        } catch (PropertyException exp) {
            logger.error("Something wrong when opening anhalytics.properties", exp);
            return;
        }

        if (processArgs(args)) {
            if (HarvestProperties.isProcessByDate()) {
                if (HarvestProperties.getFromDate() != null || HarvestProperties.getUntilDate() != null) {
                    Utilities.updateDates(HarvestProperties.getUntilDate(), HarvestProperties.getFromDate());
                }
            }

            if (isBlank(HarvestProperties.getSource())) {
                throw new RuntimeException("Source not specified, please select one of the following: " + Arrays.toString(Harvester.Source.values()));
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
        String process = HarvestProperties.getProcessName();
        GrobidProcess gp = new GrobidProcess();
        TeiCorpusBuilderProcess tcb = new TeiCorpusBuilderProcess();
        CrossRef cr = new CrossRef();
        OpenUrl ou = new OpenUrl();


        if (process.equals("harvestAll")) {
            Harvester harvester = harvestersMap.get(HarvestProperties.getSource());
            harvester.fetchAllDocuments();
        } else if (process.equals("harvestList")) {
            Harvester harvester = harvestersMap.get(HarvestProperties.getSource());
            harvester.fetchListDocuments();
        } else if (process.equals("sample")) {
            Harvester harvester = harvestersMap.get(HarvestProperties.getSource());
            harvester.sample();
        } else if (process.equals("transformMetadata")) {
            tcb.transformMetadata();
        } else if (process.equals("processGrobid")) {
            gp.processFulltexts();
        } else if (process.equals("appendFulltextTei")) {
            tcb.addGrobidFulltextToTEICorpus();
        }

//        else if (process.equals("harvestDOI")) {
//            cr.findDois();
//        } else if (process.equals("openUrl")) {
//            ou.getIstexUrl();
//        }
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
                } else if (currArg.equals("-source")) {
                    String command = pArgs[i + 1];

                    //check source exists
                    if (!Harvester.Source.contains(command.toLowerCase())) {
                        System.out.println(command);
                        System.err.println("source should be one value from this list: " + Arrays.toString(Harvester.Source.values()));
                        System.err.println("Refer to the documentation to add new harvesters ");
                        result = false;
                    }

                    HarvestProperties.setSource(command);
                    i++;
                    continue;
                } else if (currArg.equals("-input")) {
                    String input = pArgs[i + 1];
                    HarvestProperties.setInputDirectory(input);
                    HarvestProperties.setLocal(true);
                    i++;
                } else if (currArg.equals("-metadata")) {
                    String metadata = pArgs[i + 1];
                    HarvestProperties.setMetadataDirectory(metadata);
                    HarvestProperties.setLocal(true);
                    i++;
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
        help.append("-source: the sources available are : \n");
        help.append("\t" + Arrays.toString(Harvester.Source.values()) + "\n");
        help.append("-dFromDate: filter start date for the process, make sure it follows the pattern : yyyy-MM-dd\n");
        help.append("-dUntilDate: filter until date for the process, make sure it follows the pattern : yyyy-MM-dd\n");
        help.append("-input: input directory (only valid when -exe is 'harvestFromFilesystem') \n");
        help.append("--reset: updates all the documents (beware about versions/updates) : \n");
        help.append("-exe: gives the command to execute. The value should be one of these : \n");
        help.append("\t" + availableCommands + "\n");
        return help.toString();
    }
}
