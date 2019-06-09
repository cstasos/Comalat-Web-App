package comalat.app.RestAPI.Resources;

import comalat.app.Domain.lesson.Course;
import comalat.app.Domain.Folder;
import comalat.app.Constants;
import comalat.app.RestAPI.ResponseMessage.ResponseMessage;
import comalat.app.RestAPI.Exception.ConflictException;
import comalat.app.RestAPI.Exception.DataNotFoundException;
import comalat.app.RestAPI.Exception.InvalidInputException;
import comalat.app.HelperManager.FolderHelper.FolderManager;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.HeaderParam;
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
// */comalat/languages/{lang}/levels/{lvl}/courses
@Produces(MediaType.APPLICATION_JSON)
public class CoursesResources {

    @GET
    public Response get() {
        ResponseMessage message = new ResponseMessage("Courses", Status.OK.getStatusCode(), null);
        return Response.status(Status.OK).entity(message).build();
    }

    // get courses from a language/education_level 
    // */comalat/languages/{lang}/levels/{lvl}/courses/{coursesXY}
    @GET
    @Path("/{coursesXY}")
    public Response getUnitsXY(
            @PathParam("lang") String lang,
            @PathParam("lvl") String lvl,
            @PathParam("coursesXY") String coursesXY,
            @HeaderParam("serialNo") long serialNo) {

        Folder file = new Course(lang, lvl, coursesXY);
        
        if (serialNo == 0) {
            serialNo = Date.from(Instant.now()).getTime();
        }

        String tmpname = lang + "_" + lvl + "_" + coursesXY;
        String zipname = tmpname + "_" + serialNo + Constants.ZIP_FORMAT;

        if (!file.exists()) {
            throw new DataNotFoundException("Can not find folder/file " + "{" + coursesXY + "} at folder {" + lang + "/" + lvl + "}");
        }

        file.compress(zipname);
        File zipFile = new File(Paths.get(Constants.DOWNLOAD_FOLDER, zipname).toString());
        return Response.ok(zipFile, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + tmpname + Constants.ZIP_FORMAT + "\"")
                .header("x-zipfilename", zipFile.getName())
                .header("x-fileformat", Constants.ZIP_FORMAT)
                .build();
    }

    // delete courses from language/education_level
    // */comalat/languages/{lang}/levels/{lvl}/courses/{coursesXY}
    // Response 200 OK || 404 NOT FOUND
    @DELETE
    @Path("/{coursesXY}")
    public Response deleteUnitsXY(
            @PathParam("lang") String lang,
            @PathParam("lvl") String lvl,
            @PathParam("coursesXY") String coursesXY) {

        Folder file = new Course(lang, lvl, coursesXY);        
        if (!file.exists()) {
            ResponseMessage em = new ResponseMessage(coursesXY + " does not exist at folder " + lvl, Status.NOT_FOUND.getStatusCode(), null);
            return Response.status(Status.NOT_FOUND).entity(em).build();
        }
        file.delete();
        ResponseMessage sm = new ResponseMessage(coursesXY + " successfully deleted", Status.OK.getStatusCode(), null);
        return Response.status(Status.OK).entity(sm).build();
    }

    // upload courses to language/education_level
    // */comalat/languages/{lang}/levels/{lvl}/courses/upload
    // Response 201 CREATE || 404 NOT FOUND || 409 CONFLICT
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @FormDataParam("uploadFile") InputStream in,
            @FormDataParam("uploadFile") FormDataContentDisposition info,
            @FormDataParam("name") String filename,
            @PathParam("lang") String lang,
            @PathParam("lvl") String lvl,
            @HeaderParam("serialNo") long serialNo) {

        Course file = new Course();
        if (serialNo == 0) {
            serialNo = Date.from(Instant.now()).getTime();
        }

        if (in == null || !info.getFileName().endsWith(Constants.ZIP_FORMAT)) {
            throw new InvalidInputException("Please select zip format file");
        }

        if (filename == null || filename.replace(" ", "").isEmpty()) {
            // invalid filename input
            // if is null get name from info
            throw new InvalidInputException("Please input file name");
        }
        filename = filename.replace(" ", "");

        String source = FolderManager.getPath(FolderManager.getPath(Constants.SOURCE_FOLDER, lang), lvl);
        if (FolderManager.exist(source, filename)) {
            // conflict error the course exist
            throw new ConflictException("Courses "+filename+" already exist!");
        }
        filename = filename.concat("_" + serialNo + Constants.ZIP_FORMAT);

        file.save(in, filename);
        file.decompress(source, filename);

        ResponseMessage message = new ResponseMessage("Upload " + info.getFileName(), Status.CREATED.getStatusCode(), null);
        return Response.status(Status.CREATED).entity(message).build();
    }

    // update courses to language/education_level
    // */comalat/languages/{lang}/levels/{lvl}/courses/update
    @PUT
    @Path("/update")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateFile(
            @FormDataParam("uploadFile") InputStream in,
            @FormDataParam("uploadFile") FormDataContentDisposition info,
            @FormDataParam("name") String filename,
            @PathParam("lang") String lang,
            @PathParam("lvl") String lvl,
            @HeaderParam("serialNo") long serialNo) {

        Course file = new Course();
        if (serialNo == 0) {
            serialNo = Date.from(Instant.now()).getTime();
        }

        if (in == null || !info.getFileName().endsWith(Constants.ZIP_FORMAT)) {
            // throw invalid input exception
            throw new InvalidInputException("Please select zip format file");
        }

        if (filename == null || filename.replace(" ", "").isEmpty()) {
            // invalid filename input
            throw new InvalidInputException("Please input file name");
        }
        String courseName = new String(filename);
        filename = filename.replace(" ", "");

        String source = FolderManager.getPath(FolderManager.getPath(Constants.SOURCE_FOLDER, lang), lvl);
        filename = filename.concat("_" + serialNo + Constants.ZIP_FORMAT);

        file.save(in, filename);
        if (FolderManager.getPath(source, courseName) != null) {
            FolderManager.delete(FolderManager.getPath(source, courseName));
        }
        file.decompress(source, filename);

        ResponseMessage message = new ResponseMessage("Updated " + info.getFileName(), Status.CREATED.getStatusCode(), null);
        return Response.status(Status.OK).entity(message).build();
    }

    /**
     * ********* SubResources **********
     */
    // unit subresource
    // */comalat/languages/{lang}/levels/{lvl}/courses/{coursesXY}/units
    @Path("/{coursesXY}/units")
    public UnitResources FileSubresource() {
        return new UnitResources();
    }
}
