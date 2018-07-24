package fr.inria.anhalytics.harvest.grobid;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

/**
 * @author achraf
 */
public class AssetLegendExtracter {

    static String extractLegendFromTei(String filename, InputStream teiStream) {
        String legend = null;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);
        Document tei = null;
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();

            tei = docBuilder.parse(teiStream);

            NodeList nodeList = tei.getElementsByTagName("graphic");
            if (nodeList != null && nodeList.getLength() > 0) {
                System.out.println("nodeList.getLength() " + nodeList.getLength());
                for (int j = 0; j < nodeList.getLength(); j++) {
                    Element el = (Element) nodeList.item(j);
                    System.out.println(el.getAttribute("url"));
                    if (el.getAttribute("url").equals(filename)) {
                        Element figure = (Element) el.getParentNode();
                        NodeList figDescChild = figure.getElementsByTagName("figDesc");
                        if (figDescChild != null && figDescChild.getLength() > 0) {
                            Element figDesc = (Element) figDescChild.item(0);
                            legend = figDesc.getTextContent();

                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return legend;
    }

}
