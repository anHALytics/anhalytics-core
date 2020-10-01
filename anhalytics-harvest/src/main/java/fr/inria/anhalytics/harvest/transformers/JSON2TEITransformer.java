package fr.inria.anhalytics.harvest.transformers;

import net.sf.saxon.s9api.*;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class JSON2TEITransformer {

    public final static String xsltPath = "/xslt/Json2TEI.xsl";

    private static XsltTransformer transformer;
    private final Processor processor;

    public JSON2TEITransformer() {
        processor = new Processor(false);
        XsltCompiler compiler = processor.newXsltCompiler();
        XsltExecutable executable = null;
        try {
            executable = compiler.compile(new StreamSource(JSON2TEITransformer.class.getResourceAsStream(xsltPath)));
            transformer = executable.load();
        } catch (SaxonApiException e) {
            e.printStackTrace();
        }
    }

    public String transform(String input) throws SaxonApiException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Serializer serializer = processor.newSerializer();
        serializer.setOutputStream(outputStream);
        transformer.setDestination(serializer);
        transformer.setInitialTemplate(new QName("init")); //<-- SET INITIAL TEMPLATE
        transformer.setParameter(new QName("text"), new XdmAtomicValue(input)); //<-- PASS JSON IN AS PARAM
        transformer.transform();

        return outputStream.toString(StandardCharsets.UTF_8);
    }

    public String stripNamespaces(String input) {
        return input.replaceAll("xmlns:(pdm|xsl|ext|exch)=\"[^\"]+\"", "");
    }
}
