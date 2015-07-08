package fr.inria.anhalytics.harvest.service;


import fr.inria.anhalytics.commons.managers.MongoManager;
import java.io.InputStream;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Achraf
 */
@Path("/assetHal")
public class AssetHalService {

    private MongoManager mm = new MongoManager(false);

    private static String KEY = "8uk797Nw74" ; // :) 
    
    public AssetHalService() {
    }

    @Path("{halid}/{filename}/{key}")
    @GET
    @Produces("image/png")
    public Response getFile(@PathParam("halid") String id, @PathParam("filename") String filename, @PathParam("key") String key) {
        Response response = null;
        if(key.equals(KEY)){
        try {
            InputStream is = mm.getFile(id, filename, MongoManager.GROBID_ASSETS);
            if (is == null) {
                response = Response.status(Status.NOT_FOUND).build();
            } else {
                //response = Response.status(Status.OK).type("image/png").build();
                response = Response
                        .ok()
                        .type("image/png")
                        .entity(IOUtils.toByteArray(is))
                        .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                        .build();
                is.close();
            }
        } catch (Exception exp) {
            response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
        }else
            response = Response.status(Status.UNAUTHORIZED).build();
        return response;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String sayHtmlHello() {
        return "<html> " + "<title>" + "Hello HAL" + "</title>"
                + "<body><h1>" + "Hello HAL" + "</body></h1>" + "</html> ";
    }
}
