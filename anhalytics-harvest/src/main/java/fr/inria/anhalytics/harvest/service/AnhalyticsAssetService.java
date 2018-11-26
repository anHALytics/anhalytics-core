package fr.inria.anhalytics.harvest.service;

import fr.inria.anhalytics.commons.managers.MongoFileManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Grobid assets provider.
 *
 * @author Achraf
 */
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

@RestController
public class AnhalyticsAssetService {

    private MongoFileManager mm;

    private static String KEY = "key"; // :)

    public AnhalyticsAssetService() throws IOException {
        this.mm = MongoFileManager.getInstance(false);
//        Properties prop = new Properties();
//        try {
//            ClassLoader classLoader = AnhalyticsAssetService.class.getClassLoader();
//            prop.load(classLoader.getResourceAsStream("harvest.properties"));
//        } catch (Exception exp) {
//            throw new PropertyException("Cannot open file of harvest.properties", exp);
//        }
//        KEY = prop.getProperty("harvest.service_key");
    }

//    @Path("asset")
//    @GET
//    public Response getImage(@QueryParam("id") String id, @QueryParam("filename") String filename, @QueryParam("key") String key) {
//
//        Response response = null;
//        InputStream is = null;
//        if (StringUtils.equals(key, KEY)) {
//            try {
//                is = mm.getFulltextByAnhalyticsId(id);
//                if (is == null) {
//                    response = Response.status(Status.NOT_FOUND).build();
//                } else {
//                    response = Response
//                            .ok()
//                            .type("image/png")
//                            .entity(IOUtils.toByteArray(is))
//                            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
//                            .build();
//                }
//
//            } catch (Exception exp) {
//                response = Response.status(Status.INTERNAL_SERVER_ERROR).type("text/plain").entity(exp.getMessage()).build();
//            } finally {
//                IOUtils.closeQuietly(is);
//            }
//        } else {
//            response = Response.status(Status.UNAUTHORIZED).type("text/plain").build();
//        }
//        return response;
//    }

    @ResponseBody
    @RequestMapping(value = "/pdf", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public void getPDF(@RequestParam(value="id") String id, @RequestParam(value="key") String key, HttpServletResponse response) throws IOException {
        InputStream is = null;
        System.out.println(id);
        if (StringUtils.equals(key, KEY)) {
            try {
                is = mm.getFulltextByAnhalyticsId(id);
                if (is == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.addHeader("content-type", "application/pdf");
                    IOUtils.copy(is, response.getOutputStream());
                    response.addHeader("Content-Disposition", "filename=\"" + id + ".pdf\"");
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
                    response.addHeader("Access-Control-Allow-Headers", "Range");
                    response.addHeader("Access-Control-Expose-Headers", "Accept-Ranges, Content-Encoding, Content-Length, Content-Range");
                }

            } catch (Exception exp) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.addHeader("content-type", "text/plain");
                ServletOutputStream sout = response.getOutputStream();
                sout.print(exp.getMessage());
            } finally {
                IOUtils.closeQuietly(is);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.addHeader("content-type", "text/plain");
        }
    }

    @RequestMapping(value = "/hello", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String sayHtmlHello() {
        return "<html> " + "<title>" + "Hello Anhalytics" + "</title>"
                + "<body><h1>" + "Hello Anhalytics" + "</body></h1>" + "</html> ";
    }
}
