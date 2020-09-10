package fr.inria.anhalytics.harvest.harvesters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.data.BinaryFile;
import fr.inria.anhalytics.commons.data.arxiv.ArxivMetadata;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import org.apache.commons.lang3.NotImplementedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class ArxivHarvester extends Harvester {

    @Override
    public void fetchAllDocuments() {
        if (isBlank(HarvestProperties.getInputDirectory()) || isBlank(HarvestProperties.getMetadataFile())) {
            throw new RuntimeException("Input directory (" + HarvestProperties.getInputDirectory()
                    + ") or metadata (" + HarvestProperties.getMetadataFile() + ") not specified");
        }

        if (!HarvestProperties.getLocal()) {
            throw new NotImplementedException("Not yet implemented. ");
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);


        try {

//            List<File> refFiles = refFiles = Files.walk(Paths.get(HarvestProperties.getInputDirectory()), Integer.MAX_VALUE)
//                    .filter(path -> Files.isRegularFile(path)
//                            && (StringUtils.endsWithIgnoreCase(path.getFileName().toString(), ".pdf")))
//                    .map(Path::toFile)
//                    .collect(Collectors.toList());

            try (Stream<String> stream = Files.lines(Paths.get(HarvestProperties.getMetadataFile()))) {

                stream.forEach(l -> {
                    try {
                        ArxivMetadata metadata = mapper.readValue(l, ArxivMetadata.class);

                        String id = metadata.getId();
                        String lastVersion = Iterables.getLast(metadata.getVersions()).getVersion();


                        if(id.contains("/")) {
                            id = id.split("/")[1];
                        }

                        String prefix = id.split("\\.")[0];
                        prefix = prefix.substring(0, 4);

//                        System.out.println(prefix);
                        String pdfFileName = id.replaceAll("\\.", "") + lastVersion + ".pdf";

                        Path pathPdf = Paths.get(HarvestProperties.getInputDirectory(), prefix, pdfFileName);
                        if (Files.exists(pathPdf) && Files.isRegularFile(pathPdf)) {
                            BiblioObject biblioObject = new BiblioObject("", Source.ARXIV.getName(), metadata.getId(), l);
                            biblioObject.setDomains(Arrays.asList(metadata.getCategories()));
                            biblioObject.setDoi(metadata.getDoi());
                            biblioObject.setIsProcessedByPub2TEI(Boolean.FALSE);
                            biblioObject.setPublicationType("ART_Journal articles");
                            biblioObject.setMetadata(metadata.toTei());
                            biblioObject.setSource(Source.ARXIV.getName());

                            BinaryFile binaryFile = new BinaryFile();
                            byte[] bytes = Files.readAllBytes(pathPdf);
                            binaryFile.setStream(new ByteArrayInputStream(bytes));
                            binaryFile.setAnhalyticsId("");
                            binaryFile.setFileName(pdfFileName);
                            biblioObject.setPdf(binaryFile);
                            grabbedObjects = Collections.singletonList(biblioObject);
                            saveObjects();

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });


//
//
//                LOGGER.info(refFiles.size() + " files to be processed.");
//
//                if (isEmpty(refFiles)) {
//                    return;
//                }
//
//                LOGGER.info(refFiles.size() + " files");
//                String name;
//
//                for (int n = 0; n < refFiles.size(); n++) {
//                    File theFile = refFiles.get(n);
//                    name = theFile.getName();
//                    LOGGER.info(name);

//                    BiblioObject object = new BiblioObject();
//                    object.setAnhalyticsId("");
//                    object.setIsWithFulltext(true);
//                    object.setMetadata("");
//                    object.setPdf(binaryFile);
//                    grabbedObjects = Collections.singletonList(object);
//                    saveObjects(true);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fetchListDocuments() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sample() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
