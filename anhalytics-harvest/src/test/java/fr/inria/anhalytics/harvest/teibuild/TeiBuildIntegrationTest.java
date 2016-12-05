package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.properties.HarvestProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * @author Achraf
 */
public class TeiBuildIntegrationTest {

    @Test
    public void testMetadataAppend() throws Exception {
        try {
            HarvestProperties.init("anhalytics.test.properties");
        } catch (Exception exp) {
            System.err.println(exp.getMessage());
            return;
        }


        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new MyNamespaceContext());

        File metadata = null, fullTextFile = null;
        try {
            metadata = new File(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                    + "/hal-00576900.tei.xml");
            /*if (!metadata.exists()) {
            throw new Exception("Cannot start test, because test resource folder is not correctly set.");
        }*/
            fullTextFile = new File(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                    + "/hal-00576900.fulltext.tei.xml");
            /*if (!fullTextFile.exists()) {
            throw new Exception("Cannot start test, because test resource folder is not correctly set.");
        }
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
        TeiBuilder teiBuilder = new TeiBuilder();
        Document corpusTei = teiBuilder.createTEICorpus(new FileInputStream(metadata));
        String corpusTeiString = Utilities.toString(corpusTei);

        String result = Utilities.toString(teiBuilder.addGrobidTEIToTEICorpus(corpusTeiString, FileUtils.readFileToString(fullTextFile)));
        // some test here...

        try {
            String expected = FileUtils.readFileToString(new File(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                    + "/hal-00576900.corpus.tei.xml"), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);
        Document metadataDoc = null;
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            metadataDoc = docBuilder.parse(new FileInputStream(metadata));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Document corpusDoc = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            corpusDoc = docBuilder.parse(new ByteArrayInputStream(result.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String titleHalTei = (String) xPath.compile("/TEI/text/body/listBibl/biblFull/titleStmt/title").evaluate(metadataDoc, XPathConstants.STRING);
        String titleResult = (String) xPath.compile("/teiCorpus/teiHeader/fileDesc/titleStmt/title").evaluate(corpusDoc, XPathConstants.STRING);
        assertEquals(titleResult.trim(), titleHalTei.trim());

        try {
            metadata = new File(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                    + "/inria-00510267.tei.xml");
            fullTextFile = new File(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                    + "/inria-00510267.fulltext.tei.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
        corpusTei = teiBuilder.createTEICorpus(new FileInputStream(metadata));
        String corpusTeiStream1 = (Utilities.toString(corpusTei));
        result = Utilities.toString(teiBuilder.addGrobidTEIToTEICorpus(corpusTeiStream1, FileUtils.readFileToString(fullTextFile)));

        try {
            docBuilder = docFactory.newDocumentBuilder();
            metadataDoc = docBuilder.parse(new FileInputStream(metadata));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            docBuilder = docFactory.newDocumentBuilder();
            corpusDoc = docBuilder.parse(new ByteArrayInputStream(result.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String expected;
        try {
            expected = FileUtils.readFileToString(new File(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                    + "/inria-00510267.corpus.tei.xml"), "UTF-8");
            //assertXpathExists("/TEI[1]/teiHeader[1]", expected);
        } catch (Exception e) {
            e.printStackTrace();
        }

        titleResult = (String) xPath.compile("/TEI/text/body/listBibl/biblFull/titleStmt/title").evaluate(metadataDoc, XPathConstants.STRING);
        titleHalTei = (String) xPath.compile("/teiCorpus/teiHeader/fileDesc/titleStmt/title").evaluate(corpusDoc, XPathConstants.STRING);
        assertEquals(titleResult.trim(), titleHalTei.trim());
    }

    /*
     @Test
     public void testTEIMergingBrutal() throws Exception {
        
     }*/
    private static class MyNamespaceContext implements NamespaceContext {

        public String getNamespaceURI(String prefix) {
            if ("tei".equals(prefix)) {
                return "http://www.tei-c.org/ns/1.0";
            }
            return null;
        }

        public String getPrefix(String namespaceURI) {
            return null;
        }

        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }

    }

    public File getResourceDir(String resourceDir) throws Exception {
        File file = new File(resourceDir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new Exception("Cannot start test, because test resource folder is not correctly set.");
            }
        }
        return (file);
    }
}
