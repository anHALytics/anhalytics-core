package fr.inria.anhalytics.harvest.service;


import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.commons.managers.MongoManager;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.IOUtils;

/**
 * Grobid assets provider.
 * @author Achraf
 */
@Path("/")
public class AnhalyticsAssetService {

    private MongoManager mm = new MongoManager(false);

    private static String KEY = null ; // :) 
    
    public AnhalyticsAssetService() {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("harvest.properties"));
        } catch (Exception exp) {
            throw new PropertyException("Cannot open file of harvest properties " + "harvest.properties", exp);
        }
        KEY = props.getProperty("harvest.service_key");
    }

    @Path("asset")
    @GET
    public Response getImage(@QueryParam("halid") String id, @QueryParam("filename") String filename, @QueryParam("key") String key) {
        Response response = null;
        if(key.equals(KEY)){
            try {
                InputStream is = mm.getFile(id, filename, MongoManager.GROBID_ASSETS);
                if (is == null) {
                    response = Response.status(Status.NOT_FOUND).build();
                } else {
                    response = Response
                            .ok()
                            .type("image/png")
                            .entity(IOUtils.toByteArray(is))
                            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                            .build();
                }
                is.close();
            } catch (Exception exp) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).type("text/plain").entity(exp.getMessage()).build();
            }
        }else
            response = Response.status(Status.UNAUTHORIZED).type("text/plain").build();
        return response;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String sayHtmlHello() {
        return "<html> " + "<title>" + "Hello Anhalytics" + "</title>"
                + "<body><h1>" + "Hello Anhalytics" + "</body></h1>" + "</html> ";
    }
}
