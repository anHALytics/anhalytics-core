package fr.inria.anhalytics.annotate.services;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.inria.anhalytics.annotate.exceptions.ClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class NerdClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NerdClient.class);

    private static String PATH_DISAMBIGUATE = "/disambiguate";
    private static String PATH_CONCEPT = "/kb/concept";
    private static String PATH_LANGUAGE_RECOGNITION = "/language";
    private static String PATH_SEGMENTATION = "/segmentation";

    private static String DEFAULT_HOST = "http://nerd.huma-num.fr/nerd/service";

    private static int MAX_TEXT_LENGTH = 500;
    private static int SENTENCES_PER_GROUP = 10;

    private static final ObjectMapper mapper = new ObjectMapper();

    private String host;
    private int port = -1;

    public NerdClient() {
        this.host = DEFAULT_HOST;
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    public NerdClient(String host) {
        this();
        this.host = host;
    }

    public NerdClient(String host, int port) {
        this(host);
        this.port = port;
    }

    public ObjectNode getConcept(String id) {
        String urlNerd = this.host + PATH_CONCEPT + "/" + id;
        if (isBlank(id) || (!startsWith(id, "Q") && !startsWith(id, "P"))) {
            throw new ClientException("The id " + id + " must be not blank and start with P or Q or as number");
        }
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(urlNerd);

        try {
            return sendRequest(client.execute(request), PATH_CONCEPT);
        } catch (IOException e) {
            throw new ClientException("General error when calling the service. ", e);
        }
    }


    public ObjectNode getLanguage(String text) {
        return prepareAndSendSimplePost(text, PATH_LANGUAGE_RECOGNITION);
    }

    private ObjectNode prepareAndSendSimplePost(String text, String pathLanguageRecognition) {
        final URI uri = getUri(pathLanguageRecognition);

        HttpPost httpPost = new HttpPost(uri);
        CloseableHttpClient client = HttpClients.createDefault();

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("text", text);

        httpPost.setEntity(builder.build());

        try {
            return sendRequest(client.execute(httpPost), pathLanguageRecognition);
        } catch (IOException e) {
            throw new ClientException("General error when calling the service. ", e);
        }
    }


    public ObjectNode segment(String text) {
        return prepareAndSendSimplePost(text, PATH_SEGMENTATION);
    }

    private URI getUri(String path) {
        final URI uri;
        try {
            uri = new URI(this.host + path);
        } catch (URISyntaxException e) {
            throw new ClientException("Error while setting up the url. ", e);
        }
        return uri;
    }

    private List<ArrayNode> groupSentence(int totalNumberOfSentence, int groupLength) {
        List<ArrayNode> sentenceGroups = new ArrayList<>();
        ArrayNode currentSentenceGroup = mapper.createArrayNode();

        for (int i = 0; i < totalNumberOfSentence; i++) {
            if (i % groupLength == 0) {
                if (currentSentenceGroup.size() > 0) {
                    sentenceGroups.add(currentSentenceGroup);
                }

                currentSentenceGroup = mapper.createArrayNode();
                currentSentenceGroup.add(i);
            } else {
                currentSentenceGroup.add(i);
            }
        }

        if (currentSentenceGroup.size() > 0) {
            sentenceGroups.add(currentSentenceGroup);
        }

        return sentenceGroups;
    }

    protected ObjectNode processQuery(ObjectNode query) {
        return processQuery(query, false);
    }

    protected ObjectNode processQuery(ObjectNode query, boolean prepared) {

        if (prepared) {
            //POST
            final URI uri = getUri(PATH_DISAMBIGUATE);

            HttpPost httpPost = new HttpPost(uri);
            CloseableHttpClient httpResponse = HttpClients.createDefault();

            httpPost.setHeader("Content-Type", APPLICATION_JSON.toString());
            String jsonInString;
            try {
                jsonInString = mapper.writeValueAsString(query);
                httpPost.setEntity(new StringEntity(jsonInString));
            } catch (JsonProcessingException | UnsupportedEncodingException e) {
                throw new ClientException("Cannot serialise query. ", e);
            }

            try {
                return sendRequest(httpResponse.execute(httpPost), PATH_DISAMBIGUATE);
            } catch (IOException e) {
                throw new ClientException("Generic exception when sending POST. ", e);
            }

        }

        String text = query.get("text").asText();

        //prepare single sentence
        ArrayNode sentencesCoordinatesArray = mapper.createArrayNode();
        final ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("offsetStart", 0);
        objectNode.put("offsetEnd", StringUtils.length(text));
        sentencesCoordinatesArray.add(objectNode);

        int totalNumberOfSentences = sentencesCoordinatesArray.size();
        List<ArrayNode> sentenceGroup = new ArrayList<>();

        if (StringUtils.length(text) > MAX_TEXT_LENGTH) {
            // we need to cut the text in more sentences

            final ObjectNode sentences = segment(text);

            sentencesCoordinatesArray = (ArrayNode) sentences.get("sentences");
            totalNumberOfSentences = sentencesCoordinatesArray.size();

            sentenceGroup = groupSentence(totalNumberOfSentences, SENTENCES_PER_GROUP);

        } else {
            query.put("sentence", true);
        }

        if (totalNumberOfSentences > 1) {
            query.set("sentences", sentencesCoordinatesArray);
        }

        if (sentenceGroup.size() > 0) {
            System.out.println("Splitting the query in " + sentenceGroup.size() + " requests.");
            for (ArrayNode group : sentenceGroup) {
                query.set("processSentence", group);
                final ObjectNode jsonNodes = processQuery(query, true);
                query.set("entities", jsonNodes.get("entities"));
                query.set("language", jsonNodes.get("language"));
            }
        } else {
            final ObjectNode jsonNodes = processQuery(query, true);
            query.set("entities", jsonNodes.get("entities"));
            query.set("language", jsonNodes.get("language"));

        }
        return query;
    }

    public ObjectNode disambiguateQuery(String text, String language) {
        ObjectNode query = mapper.createObjectNode();
        query.put("shortText", text);
        query.put("customisation", "generic");

        if (isNotBlank(language)) {
            final ObjectNode lang = mapper.createObjectNode().put("lang", language);
            query.set("language", lang);
        }

        final URI uri = getUri(PATH_DISAMBIGUATE);

        HttpPost httpPost = new HttpPost(uri);
        CloseableHttpClient httpResponse = HttpClients.createDefault();

        httpPost.setHeader("Content-Type", APPLICATION_JSON.toString());
        String jsonInString;
        try {
            jsonInString = mapper.writeValueAsString(query);
            httpPost.setEntity(new StringEntity(jsonInString));
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            throw new ClientException("Cannot serialise query. ", e);
        }

        try {
            return sendRequest(httpResponse.execute(httpPost), PATH_DISAMBIGUATE);
        } catch (IOException e) {
            throw new ClientException("Generic exception when sending POST. ", e);
        }
    }

    public ObjectNode disambiguateText(String text, String language) {
        ObjectNode query = mapper.createObjectNode();
        query.put("text", text);
        if (isNotBlank(language)) {
            final ObjectNode lang = mapper.createObjectNode().put("lang", language);
            query.set("language", lang);
        }

//        if (CollectionUtils.isNotEmpty(entities)) {
//            query.setEntities(entities);
//        }

        return processQuery(query);
    }

    public ObjectNode disambiguatePDF(File pdf, String language) {
        try {
            return disambiguatePDF(new FileInputStream(pdf), language);
        } catch (FileNotFoundException e) {
            throw new ClientException("File not found", e);
        }
    }

    public ObjectNode disambiguatePDF(InputStream pdf, String language) {
        ObjectNode query = mapper.createObjectNode();
        query.put("customisation", "generic");
        if (isNotBlank(language)) {
            final ObjectNode lang = mapper.createObjectNode().put("lang", language);
            query.set("language", lang);
        }

        final URI uri = getUri(PATH_DISAMBIGUATE);

        HttpPost httpPost = new HttpPost(uri);
        CloseableHttpClient httpclient = HttpClients.createDefault();

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", pdf);
        builder.addTextBody("query", query.toString());

        httpPost.setEntity(builder.build());

        try {
            return sendRequest(httpclient.execute(httpPost), PATH_DISAMBIGUATE);
        } catch (IOException e) {
            throw new ClientException("Generic exception when sending POST. ", e);
        }

    }

    public ObjectNode disambiguateTerm(Map<String, Double> terms, String language) {
        final URI uri = getUri(PATH_DISAMBIGUATE);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        ArrayNode termsNode = mapper.createArrayNode();

        for (Map.Entry<String, Double> list : terms.entrySet()) {
            ObjectNode termNode = mapper.createObjectNode();
            termNode.put("term", list.getKey());
            termNode.put("score", list.getValue());
            termsNode.add(termNode);
        }

        node.set("termVector", termsNode);

        if (isNotBlank(language)) {
            ObjectNode dataNode = mapper.createObjectNode();
            dataNode.put("lang", language);
            node.set("language", dataNode);
        }
        HttpPost httpPost = new HttpPost(uri);
        CloseableHttpClient httpResponse = HttpClients.createDefault();

        httpPost.setHeader("Content-Type", APPLICATION_JSON.toString());
        try {
            httpPost.setEntity(new StringEntity(node.toString()));
            return sendRequest(httpResponse.execute(httpPost), PATH_DISAMBIGUATE);
        } catch (UnsupportedEncodingException e) {
            throw new ClientException("Cannot set body. ", e);
        } catch (IOException e) {
            throw new ClientException("Generic exception when sending POST. ", e);
        }
    }


    private ObjectNode sendRequest(CloseableHttpResponse execute, String request) {
        int retry = 0;
        int retries = 4;
        int status;

        CloseableHttpResponse closeableHttpResponse;

        do {
            try {
                closeableHttpResponse = execute;
                status = closeableHttpResponse.getStatusLine().getStatusCode();
                if (status == HttpStatus.SC_OK) {
                    JsonNode actualObj = mapper.readTree(closeableHttpResponse.getEntity().getContent());
                    return actualObj.deepCopy();
                } else if (status == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                    try {
                        LOGGER.warn("Got 503. Sleeping and re-trying. ");
                        Thread.sleep(900000);
                        retry++;
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (JsonParseException | JsonMappingException e) {
                throw new ClientException("Cannot parse query.", e);
            } catch (IOException e) {
                throw new ClientException("Error when sending the request.", e);
            }
        } while (retry < retries && status == HttpStatus.SC_GATEWAY_TIMEOUT);

        throw new ClientException("Cannot call the service " + request + ". Tried already several time without success. " +
                "Status code: " + status + ".");
    }
}
