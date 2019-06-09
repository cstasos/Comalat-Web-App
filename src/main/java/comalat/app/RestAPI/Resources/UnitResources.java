package comalat.app.RestAPI.Resources;

import comalat.app.Domain.Folder;
import comalat.app.Constants;
import comalat.app.RestAPI.ResponseMessage.ResponseMessage;
import comalat.app.Domain.lesson.Unit;
import comalat.app.RestAPI.Exception.DataNotFoundException;
import comalat.app.RestAPI.Exception.ConflictException;
import comalat.app.RestAPI.Exception.InvalidInputException;

import java.io.File;
import java.io.InputStream;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 *
 * @author SyleSakis
 */
// */comalat/languages/{lang}/levels/{lvl}/courses/{coursesXY}/units
@Produces(MediaType.APPLICATION_JSON)
public class UnitResources {

    @GET
    public Response get() {
        ResponseMessage message = new ResponseMessage("Units", Status.OK.getStatusCode(), null);
        return Response.status(Status.OK).entity(message).build();
    }

    // get pdf file from a language/education_level/courses 
    // */comalat/languages/{lang}/levels/{lvl}/courses/{coursesXY}/units/{unit}
    @GET
    @Path("/{unit}")
    public Response getUnitsXY(
            @PathParam("lang") String lang,
            @PathParam("lvl") String lvl,
            @PathParam("coursesXY") String coursesXY,
            @PathParam("unit") String unit) {
        
        Unit file = new Unit(lang, lvl, coursesXY, unit);
        File pdfFile = file.getUnitFile();
        
        if (pdfFile == null || !pdfFile.exists()) {
            throw new DataNotFoundException("Can not find pdf file at folder {" + lang + "/" + lvl + "/" + coursesXY + "/" + unit + "}");
        }

        return Response.ok(pdfFile, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + pdfFile.getName() + "\"")
                .header("x-pdffilename", pdfFile.getName())
                .header("x-fileformat", Constants.PDF_FORMAT)
                .build();
    }

    // delete pdf file from language/education_level/courses
    // */comalat/languages/{lang}/levels/{lvl}/courses/{coursesXY}/units/{unit}
    // Response 200 OK || 404 NOT FOUND
    @DELETE
    @Path("/{unit}")
    public Response deletePDFFile(
            @PathParam("lang") String lang,
            @PathParam("lvl") String lvl,
            @PathParam("coursesXY") String coursesXY,
            @PathParam("unit") String unit) {

        Folder file = new Unit(lang, lvl, coursesXY, unit);

        if (!file.exists()) {
            ResponseMessage em = new ResponseMessage(unit + " does not exist at folder" + coursesXY, Status.NOT_FOUND.getStatusCode(), null);
            return Response.status(Status.NOT_FOUND).entity(em).build();
        }
        file.delete();
        ResponseMessage sm = new ResponseMessage(unit + " successfully deleted", Status.OK.getStatusCode(), null);
        return Response.status(Status.OK).entity(sm).build();
    }

    // upload pdf file to language/education_level/courses
    // */comalat/languages/{lang}/levels/{lvl}/courses/{coursesXY}/units/upload
    // Response 201 CREATE || 404 NOT FOUND || 409 CONFLICT
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @FormDataParam("uploadFile") InputStream in,
            @FormDataParam("uploadFile") FormDataContentDisposition info,
            @FormDataParam("name") String unit,
            @PathParam("lang") String lang,
            @PathParam("lvl") String lvl,
            @PathParam("coursesXY") String coursesXY) {
        
        Unit file = new Unit(lang, lvl, coursesXY, unit);

        if (in == null || !info.getFileName().endsWith(Constants.PDF_FORMAT)) {
            throw new InvalidInputException("Please select pdf format file");
        }

        if (unit == null || unit.replace(" ", "").isEmpty()) {
            // invalid filename input
            // if is null get name from info
            throw new InvalidInputException("Please input file name");
        }
        
        if(file.getUnitDirecoty() != null){
            File tmpFile = file.getUnitFile();
            if(tmpFile != null && tmpFile.getName().equalsIgnoreCase(info.getFileName()))
                throw new ConflictException("PDF file " + info.getFileName() + " at folder " + unit + " already exist!");
        }
        
        file.save(in, info.getFileName());
        
        ResponseMessage message = new ResponseMessage("Upload " + info.getFileName(), Status.CREATED.getStatusCode(), null);
        return Response.status(Status.CREATED).entity(message).build();
    }

    // update pdf file to language/education_level/courses
    // */comalat/languages/{lang}/levels/{lvl}/courses/{coursesXY}/units/update
    @PUT
    @Path("/update")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateFile(
            @FormDataParam("uploadFile") InputStream in,
            @FormDataParam("uploadFile") FormDataContentDisposition info,
            @FormDataParam("name") String unit,
            @PathParam("lang") String lang,
            @PathParam("lvl") String lvl,
            @PathParam("coursesXY") String coursesXY) {
        
        Unit file = new Unit(lang, lvl, coursesXY, unit);

        if (in == null || !info.getFileName().endsWith(Constants.PDF_FORMAT)) {
            throw new InvalidInputException("Please select pdf format file");
        }

        if (unit == null || unit.replace(" ", "").isEmpty()) {
            throw new InvalidInputException("Please input file name");
        }
        
        if(file.getUnitDirecoty() != null && new File(file.getUnitDirecoty()).exists()){
            file.delete();
        }
        
        file.save(in, info.getFileName());        
        ResponseMessage message = new ResponseMessage("Update " + info.getFileName(), Status.OK.getStatusCode(), null);
        return Response.status(Status.OK).entity(message).build();
    }
}
