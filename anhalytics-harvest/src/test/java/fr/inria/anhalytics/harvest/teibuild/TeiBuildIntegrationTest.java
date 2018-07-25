package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.data.BiblioObject;
import fr.inria.anhalytics.commons.properties.HarvestProperties;
import fr.inria.anhalytics.commons.utilities.Utilities;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * @author Achraf
 */
public class TeiBuildIntegrationTest {
    DocumentBuilder docBuilder;
    XPath xPath;
    TeiBuilderWorker teiBuilder;
    BiblioObject biblioObject;

    @Before
    public void setUp() throws Exception {
        HarvestProperties.init("anhalytics.test.properties");
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);
        docBuilder = docFactory.newDocumentBuilder();

        xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new MyNamespaceContext());

        biblioObject = new BiblioObject();
    }

    @Test
    public void testTransformBuildCorpusTeiHal() throws Exception {

        InputStream metadataTeiStream = null, baseCorpusTeiStream = null, grobidTeiStream = null;
        String metadataTeiStr, grobidTeiStr;

        Document corpusTeiDoc, expectedCorpusTeiDoc = null;

        //HAL example
        HarvestProperties.setSource("hal");
        metadataTeiStream = new FileInputStream(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                + "/5a7c4c00b64afc92264bd313.hal.tei.xml");
        metadataTeiStr = IOUtils.toString(metadataTeiStream, "UTF-8");
        metadataTeiStream.close();

        baseCorpusTeiStream = new FileInputStream(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                + "/5a7c4c00b64afc92264bd313.hal.corpus.tei.xml");
        expectedCorpusTeiDoc = docBuilder.parse(baseCorpusTeiStream);
        baseCorpusTeiStream.close();

        biblioObject.setMetadata(metadataTeiStr);


        teiBuilder = new TeiBuilderWorker(biblioObject, Steps.APPEND_FULLTEXT);
        corpusTeiDoc = teiBuilder.createTEICorpus();

        String titleHalTei = (String) xPath.compile("/teiCorpus/teiHeader[@id=\"hal\"]/fileDesc/titleStmt/title").evaluate(corpusTeiDoc, XPathConstants.STRING);
        String titleResult = (String) xPath.compile("/teiCorpus/teiHeader[@id=\"hal\"]/fileDesc/titleStmt/title").evaluate(expectedCorpusTeiDoc, XPathConstants.STRING);
        assertEquals(titleResult.trim(), titleHalTei.trim());

        //other asserts..

        grobidTeiStream = new FileInputStream(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                + "/5a7c4c00b64afc92264bd313.hal.grobid.tei.xml");
        grobidTeiStr = IOUtils.toString(grobidTeiStream, "UTF-8");
        grobidTeiStream.close();

        corpusTeiDoc = teiBuilder.addGrobidTEIToTEICorpus(Utilities.toString(corpusTeiDoc), grobidTeiStr);
        String titleGrobidCorpusTei = (String) xPath.compile("/TEI[@id=\"grobid\"]/teiHeader/fileDesc/titleStmt/title").evaluate(corpusTeiDoc, XPathConstants.STRING);
        String expectedGrobidTitleResult = (String) xPath.compile("/TEI[@id=\"grobid\"]/teiHeader/fileDesc/titleStmt/title").evaluate(expectedCorpusTeiDoc, XPathConstants.STRING);
        assertEquals(titleGrobidCorpusTei.trim(), expectedGrobidTitleResult.trim());


    }

    @Test
    public void testTransformBuildCorpusTeiIstex() throws Exception {

        InputStream metadataTeiStream = null, baseCorpusTeiStream = null, grobidTeiStream = null;
        String metadataTeiStr, grobidTeiStr;

        Document corpusTeiDoc, expectedCorpusTeiDoc = null;

        //ISTEX example
        HarvestProperties.setSource("istex");
        metadataTeiStream = new FileInputStream(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                + "/5a87113db64a8b35ae8e6916.istex.tei.xml");
        metadataTeiStr = IOUtils.toString(metadataTeiStream, "UTF-8");
        metadataTeiStream.close();

        baseCorpusTeiStream = new FileInputStream(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                + "/5a87113db64a8b35ae8e6916.istex.corpus.tei.xml");
        expectedCorpusTeiDoc = docBuilder.parse(baseCorpusTeiStream);
        baseCorpusTeiStream.close();

        biblioObject.setDomains(Arrays.asList("1 - science", "2 - pharmacology & pharmacy"));
        biblioObject.setMetadata(metadataTeiStr);

        teiBuilder = new TeiBuilderWorker(biblioObject, Steps.APPEND_FULLTEXT);
        corpusTeiDoc = teiBuilder.createTEICorpus();

        String titleCorpusTei = (String) xPath.compile("/teiCorpus/teiHeader[@id=\"istex\"]/fileDesc/titleStmt/title").evaluate(corpusTeiDoc, XPathConstants.STRING);
        String expectedTitleResult = (String) xPath.compile("/teiCorpus/teiHeader[@id=\"istex\"]/fileDesc/titleStmt/title").evaluate(expectedCorpusTeiDoc, XPathConstants.STRING);
        assertEquals(titleCorpusTei.trim(), expectedTitleResult.trim());

        String expectedSubjectDomains = (String) xPath.compile("/teiCorpus/teiHeader[@id=\"istex\"]/profileDesc/textClass/classCode[@scheme=\"domain\"]").evaluate(expectedCorpusTeiDoc, XPathConstants.STRING);
        assertEquals("science.pharmacology & pharmacy", expectedSubjectDomains);//this should improved


        grobidTeiStream = new FileInputStream(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                + "/5a87113db64a8b35ae8e6916.istex.grobid.tei.xml");
        grobidTeiStr = IOUtils.toString(grobidTeiStream, "UTF-8");
        grobidTeiStream.close();

        corpusTeiDoc = teiBuilder.addGrobidTEIToTEICorpus(Utilities.toString(corpusTeiDoc), grobidTeiStr);
        String titleGrobidCorpusTei = (String) xPath.compile("/TEI[@id=\"grobid\"]/teiHeader/fileDesc/titleStmt/title").evaluate(corpusTeiDoc, XPathConstants.STRING);
        String expectedGrobidTitleResult = (String) xPath.compile("/TEI[@id=\"grobid\"]/teiHeader/fileDesc/titleStmt/title").evaluate(expectedCorpusTeiDoc, XPathConstants.STRING);
        assertEquals(titleGrobidCorpusTei.trim(), expectedGrobidTitleResult.trim());
        // some test here...
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
