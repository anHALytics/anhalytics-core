package fr.inria.anhalytics.harvest.teibuild;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.harvest.properties.HarvestProperties;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author Achraf
 */
public class TestTeiBuild extends XMLTestCase {

    @Test
    public void testTEIMerging() throws Exception {
        try {
            HarvestProperties.init("harvest.properties");
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file of harvest properties harvest.properties", exp);
        }
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new MyNamespaceContext());
        File mtdTeiFile = new File(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                + "/hal-01110586v1.tei.xml");
        if (!mtdTeiFile.exists()) {
            throw new Exception("Cannot start test, because test resource folder is not correctly set.");
        }
        File fullTextFile = new File(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                + "/hal-01110586v1.fulltext.tei.xml");
        if (!fullTextFile.exists()) {
            throw new Exception("Cannot start test, because test resource folder is not correctly set.");
        }
        Document corpusTei = TeiBuilder.createTEICorpus(new FileInputStream(mtdTeiFile));
        String corpusTeiString = Utilities.toString(corpusTei);
        String result = Utilities.toString(TeiBuilder.addGrobidTeiToTei(corpusTeiString, FileUtils.readFileToString(fullTextFile)));
        
        // some test here...
        String expected = FileUtils.readFileToString(new File(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                + "/hal-01110586v1.final.tei.xml"), "UTF-8");
        String titleHalTei = (String) xPath.evaluate("/teiHeader/titleStmt/title/text()", new InputSource(new FileInputStream(mtdTeiFile)), XPathConstants.STRING);
        String titleResult = (String) xPath.evaluate("/teiCorpus/teiHeader/titleStmt/title/text()", new InputSource(new StringReader(result)), XPathConstants.STRING);
        assertEquals(titleResult.trim(), titleHalTei.trim());

        mtdTeiFile = new File(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                + "/hal-01110668v1.tei.xml");
        if (!mtdTeiFile.exists()) {
            throw new Exception("Cannot start test, because test resource folder is not correctly set.");
        }
        fullTextFile = new File(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                + "/hal-01110668v1.fulltext.tei.xml");
        if (!fullTextFile.exists()) {
            throw new Exception("Cannot start test, because test resource folder is not correctly set.");
        }
        corpusTei = TeiBuilder.createTEICorpus(new FileInputStream(mtdTeiFile));
        String corpusTeiStream1 = (Utilities.toString(corpusTei));
        result = Utilities.toString(TeiBuilder.addGrobidTeiToTei(corpusTeiStream1, FileUtils.readFileToString(fullTextFile)));

        expected = FileUtils.readFileToString(new File(this.getResourceDir("src/test/resources/").getAbsoluteFile()
                + "/hal-01110668v1.final.tei.xml"), "UTF-8");
		//assertXpathExists("/TEI[1]/teiHeader[1]", expected);

        titleResult = (String) xPath.evaluate("/teiHeader/titleStmt/title/text()", new InputSource(new FileInputStream(mtdTeiFile)), XPathConstants.STRING);
        titleHalTei = (String) xPath.evaluate("/teiCorpus/teiHeader/titleStmt/title/text()", new InputSource(new StringReader(result)), XPathConstants.STRING);
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
