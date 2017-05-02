package fr.inria.anhalytics.harvest.crossref;

import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.exceptions.SystemException;
import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import com.fasterxml.jackson.databind.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import java.io.*;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class for managing the extraction of bibliographical information from pdf
 * documents.
 *
 * @author Achraf Azhar
 */
public class CrossRef {

    private static final Logger logger = LoggerFactory.getLogger(CrossRef.class);

    /**
     * Lookup by DOI - 3 parameters are id, password, doi.
     */
    private static final String DOI_BASE_QUERY
            = "openurl?url_ver=Z39.88-2004&pid=%s:%s&rft_id=info:doi/%s&noredirect=true&format=unixref";

    /**
     * Lookup by journal title, volume and first page - 6 parameters are id,
     * password, journal title, author, volume, firstPage.
     */
    private static final String JOURNAL_AUTHOR_BASE_QUERY
            = //"query?usr=%s&pwd=%s&type=a&format=unixref&qdata=|%s||%s||%s|||KEY|";
            "query?usr=%s&pwd=%s&type=a&format=unixref&qdata=|%s|%s|%s||%s|||KEY|";

    // ISSN|TITLE/ABBREV|FIRST AUTHOR|VOLUME|ISSUE|START PAGE|YEAR|RESOURCE TYPE|KEY|DOI
    /**
     * Lookup by journal title, volume and first page - 6 parameters are id,
     * password, journal title, volume, firstPage.
     */
    private static final String JOURNAL_BASE_QUERY
            = //"query?usr=%s&pwd=%s&type=a&format=unixref&qdata=|%s||%s||%s|||KEY|";
            "query?usr=%s&pwd=%s&type=a&format=unixref&qdata=|%s||%s||%s|||KEY|";

    /**
     * Lookup first author surname and article title - 4 parameters are id,
     * password, title, author.
     */
    private static final String TITLE_BASE_QUERY
            = "query?usr=%s&pwd=%s&type=a&format=unixref&qdata=%s|%s||key|";

    private MongoFileManager mm;

    private DocumentBuilder docBuilder;

    private XPath xPath = XPathFactory.newInstance().newXPath();

    public CrossRef() {
        this.mm = MongoFileManager.getInstance(false);

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new SystemException("Cannot instantiate CrossRef parser", e);
        }
    }

    /**
     * Try to consolidate some uncertain bibliographical data with crossref web
     * service based on core metadata
     */
    public void findDois() {
//        String doi = "";
//        String aut = "";
//        String title = "";
//        String journalTitle = "";
//        String volume = "";
//        String firstPage = "";
//        String pageRange = "";
//        int beginPage;
//        String subpath = "";
//        int i = 0;
//        for (String date : Utilities.getDates()) {
//            if (!HarvestProperties.isProcessByDate()) {
//                date = null;
//            }
//            if (mm.initTeis(date, MongoCollectionsInterface.METADATAS_TEIS)) {
//                while (mm.hasMore()) {
//
//                    TEIFile tei = mm.nextTeiDocument();
//                    String metadataString = tei.getTei();
//                    String currentRepositoryDocId = tei.getRepositoryDocId();
//                    String currentAnhalyticsId = tei.getAnhalyticsId();
//
//                    InputStream metadataStream = new ByteArrayInputStream(metadataString.getBytes());
//
//                    Document metadata = null;
//
//                    try {
//                        logger.info("###################" + currentRepositoryDocId + "#######################");
//                        doi = mm.getDocumentDoi(currentAnhalyticsId);
//                        if (doi == null || doi.isEmpty()) {
//                            metadata = docBuilder.parse(metadataStream);
//                            metadataStream.close();
//                            Element rootElement = metadata.getDocumentElement();
//                            Node node = (Node) xPath.compile("text/body/listBibl/biblFull/titleStmt/title")
//                                    .evaluate(rootElement, XPathConstants.NODE);
//                            if (node != null) {
//                                title = node.getTextContent();
//                            }
//                            node = (Element) xPath.compile("text/body/listBibl/biblFull/titleStmt/author/persName/surname").evaluate(rootElement, XPathConstants.NODE);
//                            if (node != null) {
//                                aut = node.getTextContent();
//                            }
//                            if (aut != null) {
//                                aut = Utilities.removeAccents(aut);
//                            }
//                            if (title != null) {
//                                title = Utilities.removeAccents(title);
//                            }
//
//                            if (StringUtils.isNotBlank(title)
//                                    && StringUtils.isNotBlank(aut)) {
//                                logger.info("test retrieval per title, author");
//                                logger.info(String.format("persName=%s, title=%s", aut, title));
//                                subpath = String.format(TITLE_BASE_QUERY,
//                                        HarvestProperties.getCrossrefId(),
//                                        HarvestProperties.getCrossrefPwd(),
//                                        URLEncoder.encode(title, "UTF-8"),
//                                        URLEncoder.encode(aut, "UTF-8"));
//                                doi = queryCrossref(subpath);
//                            }
//                            if (doi.isEmpty()) {
//                                node = (Element) xPath.compile("text/body/listBibl/biblFull/sourceDesc/biblStruct/monogr/title")
//                                        .evaluate(rootElement, XPathConstants.NODE);
//                                if (node != null) {
//                                    journalTitle = node.getTextContent();
//                                    if (journalTitle != null) {
//                                        journalTitle = Utilities.removeAccents(journalTitle);
//                                    }
//                                }
//                                node = (Element) xPath.compile("text/body/listBibl/biblFull/sourceDesc/biblStruct/monogr/imprint/biblScope[@unit='pp']")
//                                        .evaluate(rootElement, XPathConstants.NODE);
//                                if (node != null) {
//                                    pageRange = node.getTextContent();
//                                }
//                                if (pageRange != null) {
//                                    pageRange = pageRange.replaceAll("[A-Za-z.,\\s+]", "");
//                                    StringTokenizer st = new StringTokenizer(pageRange, "-");
//                                    if (st.countTokens() == 2) {
//                                        firstPage = st.nextToken();
//                                    } else if (st.countTokens() == 1) {
//                                        firstPage = pageRange;
//                                    }
//                                }
//                                node = (Element) xPath.compile("text/body/listBibl/biblFull/sourceDesc/biblStruct/monogr/imprint/biblScope[@unit='volume']")
//                                        .evaluate(rootElement, XPathConstants.NODE);
//                                if (node != null) {
//                                    volume = node.getTextContent();
//                                }
//                            }
//
//                            if (doi.isEmpty() && StringUtils.isNotBlank(journalTitle)
//                                    && StringUtils.isNotBlank(volume)
//                                    //&& StringUtils.isNotBlank(aut)
//                                    && StringUtils.isNotBlank(firstPage)) {
//                                // retrieval per journal title, author, volume, first page
//                                logger.info("test retrieval per journal title, author, volume, first page");
//                                logger.info(String.format("aut=%s, firstPage=%s, journalTitle=%s, volume=%s",
//                                        aut, firstPage, journalTitle, volume));
//                                if (StringUtils.isNotBlank(aut)) {
//                                    subpath = String.format(JOURNAL_AUTHOR_BASE_QUERY,
//                                            HarvestProperties.getCrossrefId(),
//                                            HarvestProperties.getCrossrefPwd(),
//                                            URLEncoder.encode(journalTitle, "UTF-8"),
//                                            URLEncoder.encode(aut, "UTF-8"),
//                                            URLEncoder.encode(volume, "UTF-8"),
//                                            firstPage);
//                                } else {
//                                    subpath = String.format(JOURNAL_BASE_QUERY,
//                                            HarvestProperties.getCrossrefId(),
//                                            HarvestProperties.getCrossrefPwd(),
//                                            URLEncoder.encode(journalTitle, "UTF-8"),
//                                            URLEncoder.encode(volume, "UTF-8"),
//                                            firstPage);
//                                }
//                                doi = queryCrossref(subpath);
//                            }
//                            if (!doi.isEmpty()) {
//                                i++;
//                                mm.updateDoi(currentAnhalyticsId, doi);
//                            }
//                        }
//
//                        if (!doi.isEmpty()) {
//                            String crossRefMetadata = getMetadataByDoi(doi);
//                            System.out.println(crossRefMetadata);
//                            if (!crossRefMetadata.isEmpty()) {
//                                crossRefMetadata = "{ \"repositoryDocId\" : \"" + currentRepositoryDocId
//                                        + "\",\"anhalyticsId\" : \"" + currentAnhalyticsId + "\""
//                                        + "," + crossRefMetadata + "}";
//                                mm.insertCrossRefMetadata(currentAnhalyticsId, currentRepositoryDocId, crossRefMetadata);
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                if (!HarvestProperties.isProcessByDate()) {
//                    break;
//                }
//            }
//            logger.info("Done");
//        }
//        logger.info("nb of found doi : " + i);

    }

    /*
    here we are using the new crossRef api as it's faster than the old one, but in beta version.
     */
    private String getMetadataByDoi(String doi) throws Exception {
        String metadata = "";
        ObjectMapper mapper = new ObjectMapper();
        URL url = new URL("http://api.crossref.org/works/" + doi);
        logger.info("Fetching for metadata: " + url.toString());
        logger.info("Sending: " + url.toString());
        HttpURLConnection urlConn = null;
        urlConn = openConnection(url);
        if (urlConn != null) {
            try {
                urlConn.setDoOutput(true);
                urlConn.setDoInput(true);
                urlConn.setRequestMethod("GET");

                urlConn.setRequestProperty("Content-Type", "application/json");

                InputStream in = urlConn.getInputStream();

                BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null) {
                    responseStrBuilder.append(inputStr);
                }
                JsonNode jsonAnnotation = mapper.readTree(responseStrBuilder.toString());
                JsonNode metadataNode = jsonAnnotation.findPath("message");
                if (!metadataNode.isNull()) {
                    metadata = mapper.writeValueAsString(metadataNode);
                    metadata = " \"metadata\": " + metadata;
                }
                in.close();
                logger.info("DOI : " + doi);
                urlConn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return metadata;
    }

    private HttpURLConnection openConnection(URL url) {
        HttpURLConnection  urlConn;
        try {
            urlConn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            try {
                urlConn = (HttpURLConnection) url.openConnection();
            } catch (Exception e2) {
                throw new ServiceException("An exception occured while running calling CrossREF.", e2);
            }
        }
        return urlConn;
    }

    /**
     * Try to consolidate some uncertain bibliographical data with crossref web
     * service based on title and first author.
     *
     */
    private String queryCrossref(String query) throws Exception {

        String doi = "";
        // we check if the entry is not already in the DB

        URL url = new URL("http://" + HarvestProperties.getCrossrefHost() + "/" + query);

        logger.info("Sending: " + url.toString());
        HttpURLConnection urlConn = openConnection(url);
        if (urlConn != null) {
            try {
                urlConn.setDoOutput(true);
                urlConn.setDoInput(true);
                urlConn.setRequestMethod("GET");

                urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                InputStream in = urlConn.getInputStream();

                Document response = docBuilder.parse(in);
                Element root = response.getDocumentElement();
                //Element fulltext_metadata = Utilities.getElementByAttribute("publication_type", "full_text", root);
                NodeList nl = root.getElementsByTagName("doi");
                if (nl != null && nl.getLength() > 0) {
                    doi = nl.item(0).getTextContent();
                }
                in.close();
                logger.info("DOI : " + doi);
                urlConn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return doi;
    }
}
