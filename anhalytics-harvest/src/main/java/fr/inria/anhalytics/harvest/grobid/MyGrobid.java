package fr.inria.anhalytics.harvest.grobid;

import fr.inria.anhalytics.commons.properties.HarvestProperties;
import java.util.List;
import org.grobid.core.data.Affiliation;

import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;

/**
 *
 * @author achraf
 */
public class MyGrobid {

    private static Engine engine;

    public static String runGrobid(String address) {
        StringBuilder response = new StringBuilder();
        
        String retVal = null;
        try {
            MockContext.setInitialContext(HarvestProperties.getGrobidHome(), HarvestProperties.getGrobidProperties());
            GrobidProperties.getInstance();

            engine = GrobidFactory.getInstance().createEngine();
            List<Affiliation> affiliationList;
            address = address.replaceAll("\\t", " ");
            affiliationList = engine.processAffiliation(address);
            response.append("<response>");
            if (affiliationList != null) {
                for (Affiliation affi : affiliationList) {
                    
                    response.append(affi.toTEI());
                }
            }
            response.append("</response>");
            retVal = response.toString();
        } catch (Exception e) {
            // If an exception is generated, print a stack trace
            e.printStackTrace();
        } finally {
            try {
                MockContext.destroyInitialContext();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return retVal;
    }
}
