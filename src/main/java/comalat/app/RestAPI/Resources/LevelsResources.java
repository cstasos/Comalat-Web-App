package comalat.app.RestAPI.Resources;

import comalat.app.Domain.Folder;
import comalat.app.Domain.lesson.Level;
import comalat.app.Constants;
import comalat.app.RestAPI.ResponseMessage.ResponseMessage;
import comalat.app.RestAPI.Exception.ConflictException;
import comalat.app.RestAPI.Exception.DataNotFoundException;
import comalat.app.RestAPI.Exception.InvalidInputException;
import comalat.app.HelperManager.FolderHelper.FolderManager;

import java.io.File;
import java.io.InputStream;
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
// */comalat/languages/{lang}/levels/
@Produces(MediaType.APPLICATION_JSON)
public class LevelsResources {

    @GET
    public Response get() {
        ResponseMessage message = new ResponseMessage("LEVELS", Status.OK.getStatusCode(), null);
        return Response.status(Status.OK).entity(message).build();
    }

    // get education level from a language 
    // */comalat/languages/{lang}/levels/{lvl}
    @GET
    @Path("/{lvl}")
    public Response getEducationLevel(
            @PathParam("lang") String lang,
            @PathParam("lvl") String lvl,
            @HeaderParam("serialNo") long serialNo) {

        Folder file = new Level(lang, lvl);
        
        if (serialNo == 0) {
            serialNo = Date.from(Instant.now()).getTime();
        }
        String tmpname = lang + "_" + lvl;
        String zipname = tmpname + "_" + serialNo + Constants.ZIP_FORMAT;
        
        if (!file.exists()) {
            throw new DataNotFoundException("Can not find folder/file " + "{" + lvl + "} at folder {" + lang + "}");
        }

        File zipFile = file.compress(zipname);
        return Response.ok(zipFile, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + tmpname + Constants.ZIP_FORMAT + "\"")
                .header("x-zipfilename", zipFile.getName())
                .header("x-fileformat", Constants.ZIP_FORMAT)
                .build();
    }

    // delete education level from a language
    // */comalat/languages/{lang}/levels/{lvl}
    @DELETE
    @Path("/{lvl}")
    public Response deleteEducationLevel(
            @PathParam("lang") String lang,
            @PathParam("lvl") String lvl) {
        
        Folder file = new Level(lang, lvl);

        if (file.exists()) {
            ResponseMessage em = new ResponseMessage(lvl + " does not exist at folder "+lang, Status.NOT_FOUND.getStatusCode(), null);
            return Response.status(Status.NOT_FOUND).entity(em).build();
        }

        file.delete();

        ResponseMessage sm = new ResponseMessage(lvl + " successfully deleted", Status.OK.getStatusCode(), null);
        return Response.status(Status.OK).entity(sm).build();
    }

    // upload education level from a language
    // */comalat/languages/{lang}/levels/upload
    // Response 201 CREATE || 404 NOT FOUND || 409 CONFLICT
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @FormDataParam("uploadFile") InputStream in,
            @FormDataParam("uploadFile") FormDataContentDisposition info,
            @FormDataParam("name") String filename,
            @PathParam("lang") String lang,
            @HeaderParam("serialNo") long serialNo) {

        Level file = new Level();
        if (serialNo == 0) {
            serialNo = Date.from(Instant.now()).getTime();
        }

        if (in == null || !info.getFileName().endsWith(Constants.ZIP_FORMAT)) {
            throw new InvalidInputException("Please select zip format file");
        }

        if (filename == null || filename.replace(" ", "").isEmpty()) {
            // invalid filename input
            throw new InvalidInputException("Please input file name");
        }
        filename = filename.replace(" ", "");

        String source = FolderManager.getPath(Constants.SOURCE_FOLDER, lang);
        if (FolderManager.exist(source, filename)) {
            // conflict error the education level exist
            throw new ConflictException("Edication level "+filename+" already exist!");
        }
        filename = filename.concat("_" + serialNo + Constants.ZIP_FORMAT);

        file.save(in, filename);
        file.decompress(source, filename);

        ResponseMessage message = new ResponseMessage("Upload " + info.getFileName(), Status.CREATED.getStatusCode(), null);
        return Response.status(Status.CREATED).entity(message).build();
    }

    // update education level from a language
    // */comalat/languages/{lang}/levels/update
    @PUT
    @Path("/update")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateFile(
            @FormDataParam("uploadFile") InputStream in,
            @FormDataParam("uploadFile") FormDataContentDisposition info,
            @FormDataParam("name") String filename,
            @PathParam("lang") String lang,
            @HeaderParam("serialNo") long serialNo) {

        Level file = new Level();
        if (serialNo == 0) {
            serialNo = Date.from(Instant.now()).getTime();
        }

        if (in == null || !info.getFileName().endsWith(Constants.ZIP_FORMAT)) {
            throw new InvalidInputException("Please select zip format file");
        }

        if (filename == null || filename.replace(" ", "").isEmpty()) {
            throw new InvalidInputException("Please input file name");
        }
        String lvlName = new String(filename);
        filename = filename.replace(" ", "");

        String source = FolderManager.getPath(Constants.SOURCE_FOLDER, lang);
        filename = filename.concat("_" + serialNo + Constants.ZIP_FORMAT);

        file.save(in, filename);
        if (FolderManager.getPath(source, lvlName) != null) {
            FolderManager.delete(FolderManager.getPath(source, lvlName));
        }
        
        file.decompress(source, filename);

        ResponseMessage message = new ResponseMessage("Updated " + info.getFileName(), Status.CREATED.getStatusCode(), null);
        return Response.status(Status.OK).entity(message).build();
    }
    
    /*********** SubResources ***********/
    
    // units subresource
    // */comalat/languages/{lang}/levels/{lvl}/courses
    @Path("/{lvl}/courses")
    public CoursesResources UnitSubresource(){
        return new CoursesResources();
    }
}
