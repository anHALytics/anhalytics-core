package fr.inria.anhalytics.commons.utilities;

import fr.inria.anhalytics.commons.exceptions.BinaryNotAvailableException;
import fr.inria.anhalytics.commons.exceptions.DirectoryNotFoundException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

/**
 * All utilities are grouped here for managing dates, id generations, files DOM nodes..
 * 
 * @author Achraf
 */
public class Utilities {

    private static final Logger logger = LoggerFactory.getLogger(Utilities.class);

    private static Set<String> dates = new LinkedHashSet<String>();
    private static String tmpPath;

    public static void setTmpPath(String tmp_path) {
        tmpPath = tmp_path;
    }

    public static String getTmpPath() {
        return tmpPath;
    }

    static {
        Calendar toDay = Calendar.getInstance();
        int todayYear = toDay.get(Calendar.YEAR);
        int todayMonth = toDay.get(Calendar.MONTH) + 1;
        for (int year = 1960; year <= todayYear ; year++) {
            int monthYear = (year == todayYear) ? todayMonth : 12;
            for (int month = 1; month <= monthYear; month++) {
                for (int day = 1; day <=daysInMonth(year, month); day++) {
                    StringBuilder date = new StringBuilder();
                    date.append(String.format("%04d", year));
                    date.append("-");
                    date.append(String.format("%02d", month));
                    date.append("-");
                    date.append(String.format("%02d", day));
                    getDates().add(date.toString());
                }
            }
        }
    }

    public static void updateDates(String fromDate, String untilDate) {
        boolean isOkDate = true;
        if (untilDate != null) {
            isOkDate = false;
        }
        String[] dates1 = new String[dates.size()];
        dates.toArray(dates1);
        for (String date : dates1) {
            if (date.equals(untilDate)) {
                isOkDate = true;
            }
            if (!isOkDate) {
                dates.remove(date);
            }
            if (fromDate != null) {
                if (date.equals(fromDate)) {
                    isOkDate = false;
                }
            }
        }
    }

    private static int daysInMonth(int year, int month) {
        int daysInMonth;
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                daysInMonth = 31;
                break;
            case 2:
                if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
                    daysInMonth = 29;
                } else {
                    daysInMonth = 28;
                }
                break;
            default:
                // returns 30 even for nonexistant months 
                daysInMonth = 30;
        }
        return daysInMonth;
    }

    public static boolean isValidDate(String dateString) {
        return dates.contains(dateString);
    }

    public static String toString(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }

    public static String innerXmlToString(Node node) {
        DOMImplementationLS lsImpl
                = (DOMImplementationLS) node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
        LSSerializer lsSerializer = lsImpl.createLSSerializer();
        lsSerializer.getDomConfig().setParameter("xml-declaration", false);
        NodeList childNodes = node.getChildNodes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < childNodes.getLength(); i++) {
            sb.append(lsSerializer.writeToString(childNodes.item(i)));
        }
        return sb.toString();
    }

    public static Node findNode(String id, NodeList orgs) {
        Node org = null;
        for (int i = 0; i < orgs.getLength(); i++) {
            NamedNodeMap attr = orgs.item(i).getAttributes();
            if (attr.getNamedItem("xml:id") == null) {
                continue;
            }
            if (attr.getNamedItem("xml:id").getNodeValue().equals(id)) {
                org = orgs.item(i);
                break;
            }
        }
        return org;
    }

    /**
     * Add random xml ids on the textual nodes of the document
     */
    public static void generateIDs(Document doc) {
        NodeList titles = doc.getElementsByTagName("title");
        NodeList abstracts = doc.getElementsByTagName("abstract");
        NodeList terms = doc.getElementsByTagName("term");
        NodeList funders = doc.getElementsByTagName("funder");
        NodeList codes = doc.getElementsByTagName("classCode");
        NodeList ps = doc.getElementsByTagName("p");
        NodeList heads = doc.getElementsByTagName("head");
        NodeList figdescs = doc.getElementsByTagName("figDesc");
        NodeList items = doc.getElementsByTagName("item");
        generateID(titles);
        generateID(abstracts);
        generateID(terms);
        generateID(funders);
        generateID(codes);
        generateID(ps);
        generateID(heads);
        generateID(figdescs);
        generateID(items);
    }

    private static void generateID(NodeList theNodes) {
        for (int i = 0; i < theNodes.getLength(); i++) {
            Element theElement = (Element) theNodes.item(i);
            String divID = KeyGen.getKey().substring(0, 7);
            theElement.setAttribute("xml:id", "_" + divID);
        }
    }

    /**
     * Remove starting and ending end-of-line in XML element text content
     * recursively
     */
    public static void trimEOL(Node node, Document doc) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            String text = node.getNodeValue();
            if (text.replaceAll("[ \\t\\r\\n]+", "").length() != 0) {
                while (text.startsWith("\n") && text.length() > 0) {
                    text = text.substring(1, text.length());
                }
                while (text.endsWith("\n") && text.length() > 0) {
                    text = text.substring(0, text.length() - 1);
                }
                node.setNodeValue(text);
            }
        }
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            //if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
            trimEOL(currentNode, doc);
            //}
        }
    }

    public static void clearTmpDirectory() {
        try {
            File tmpDirectory = new File(tmpPath);
            FileUtils.cleanDirectory(tmpDirectory);
            logger.debug("Temporary directory is cleaned.");
        } catch (Exception exp) {
            logger.error("Error while deleting the temporary directory: " + exp);
        }
    }

    public static String storeTmpFile(InputStream inBinary) throws IOException {
        File f = File.createTempFile("tmp", ".pdf", new File(tmpPath));
        // deletes file when the virtual machine terminate
        f.deleteOnExit();
        String filePath = f.getAbsolutePath();
        if (inBinary == null) {
            System.out.println("null");
        }
        getBinaryURLContent(f, inBinary);
        return filePath;
    }

    public static String storeToTmpXmlFile(InputStream inBinary) throws IOException {
        File f = File.createTempFile("tmp", ".xml", new File(tmpPath));
        // deletes file when the virtual machine terminate
        f.deleteOnExit();
        String filePath = f.getAbsolutePath();
        getBinaryURLContent(f, inBinary);
        return filePath;
    }

    /**
     * Download binaries from a given URL
     */
    public static void getBinaryURLContent(File file, InputStream in) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        DataOutputStream writer = new DataOutputStream(fos);
        try {
            byte[] buf = new byte[4 * 1024]; // 4K buffer
            int bytesRead;
            while ((bytesRead = in.read(buf)) != -1) {
                writer.write(buf, 0, bytesRead);
            }
        } //exception null inputstream
        finally {
            in.close();
        }
    }

    public static String formatDate(Date date) {
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
        return dt1.format(date);
    }

    public static Date parseStringDate(String dateString) throws ParseException {
        Date date = null;
        if (dateString != null) {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            date = format.parse(dateString);
        }
        return date;
    }

    public static String getHalIDFromHalDocID(String halDocID) {
        int ind = halDocID.indexOf("v");
        String halID = "";
        if (ind > -1) {
            halID = halDocID.substring(0, ind);
        }
        return halID;
    }

    public static String getVersionFromURI(String uri) {
        int ind = uri.lastIndexOf("v");
        String version = "";
        if (ind > -1) {
            version = uri.substring(ind, uri.length());
        }
        return version;
    }

    public static String getHalURIFromFilename(String filename) {
        int ind = filename.indexOf(".");
        String halURI = filename.substring(0, ind);
        return halURI;
    }

    /**
     * @return the dates
     */
    public static Set<String> getDates() {
        return dates;
    }

    public static String trimEncodedCharaters(String string) {
        return string.replaceAll("&amp\\s+;", "&amp;").
                replaceAll("&quot[^;]|&amp;quot\\s*;", "&quot;").
                replaceAll("&lt[^;]|&amp;lt\\s*;", "&lt;").
                replaceAll("&gt[^;]|&amp;gt\\s*;", "&gt;").
                replaceAll("&apos[^;]|&amp;apos\\s*;", "&apos;");
    }

    public static void unzipIt(String file, String outPath) {
        try {
            ZipInputStream zis
                    = new ZipInputStream(new FileInputStream(new File(file)));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            byte[] buffer = new byte[1024];
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outPath + "/" + fileName);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
        } catch (Exception e) {

        }
    }

    public static String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    public static InputStream request(String request, boolean retry) {
        InputStream in = null;
        try {
            URL url = new URL(request);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("accept-charset", "UTF-8");
            in = conn.getInputStream();
            return in;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            if (retry) {
                try {
                    Thread.sleep(900000); //take a nap.
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                in = request(request, true);
            } else {
                throw new BinaryNotAvailableException("No stream found.");
            }
        } catch (IOException e) {
            throw new BinaryNotAvailableException("No stream found.");
        }
        return in;
    }

    public static String formatXMLString(String xmlString) {
        String formatedXml = null;
        try {
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new ByteArrayInputStream(xmlString.getBytes("utf-8"))));

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']",
                    document,
                    XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);

            transformer.transform(new DOMSource(document), streamResult);

            formatedXml = stringWriter.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formatedXml;
    }

    public static void checkPath(String path) {
        File f = new File(path);
        // exception if prop file does not exist
        if (!f.exists() || !f.isDirectory()) {
            throw new DirectoryNotFoundException("The file '" + path + "' does not exist.");
        }
    }

    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
    static public String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } finally {
            is.close();
        }
        return sb.toString();
    }
}
