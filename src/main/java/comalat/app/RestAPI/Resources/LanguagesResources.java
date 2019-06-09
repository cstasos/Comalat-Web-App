package comalat.app.RestAPI.Resources;

import comalat.app.Domain.Folder;
import comalat.app.Domain.lesson.Language;
import comalat.app.Constants;
import comalat.app.RestAPI.ResponseMessage.ResponseMessage;
import comalat.app.RestAPI.Exceptions.ConflictException;
import comalat.app.RestAPI.Exceptions.DataNotFoundException;
import comalat.app.RestAPI.Exceptions.InvalidInputException;
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
// */comalat/languages/
@Path("languages")
@Produces(MediaType.APPLICATION_JSON)
public class LanguagesResources {
    
    // get a language
    // */comalat/languages/{lang}
    @GET
    @Path("/{lang}")
    public Response getLanguage(
            @PathParam("lang") String lang,
            @HeaderParam("serialNo") long serialNo) {

        if (serialNo == 0) {
            serialNo = Date.from(Instant.now()).getTime();
        }
        
        Folder language = new Language(lang);
        
        String zipname = lang + "_" + serialNo + Constants.ZIP_FORMAT;
        if(!language.exists()){
            throw new DataNotFoundException("Can not find folder/file " + "{" + lang + "}");
        }

        File zipFile = language.compress(zipname);
        return Response.ok(zipFile, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + lang + Constants.ZIP_FORMAT + "\"")
                .header("x-zipfilename", zipFile.getName())
                .header("x-fileformat", Constants.ZIP_FORMAT)
                .build();
    }
    
    // delete a language
    // */comalat/languages/{lang}
    // Response 200 OK || 404 NOT FOUND
    @DELETE
    @Path("/{lang}")
    public Response deleteLanguage(
            @PathParam("lang") String lang) {
        
        Folder language = new Language(lang);
        if(!language.exists()){
            ResponseMessage em = new ResponseMessage(lang + " does not exist", Status.NOT_FOUND.getStatusCode(), null);
            return Response.status(Status.NOT_FOUND).entity(em).build();
        }

        language.delete();
        ResponseMessage sm = new ResponseMessage(lang + " successfully deleted", Status.OK.getStatusCode(), null);
        return Response.status(Status.OK).entity(sm).build();
    }

    // */comalat/languages/upload
    // Response 201 CREATE || 404 NOT FOUND || 404 CONFLICT
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @FormDataParam("uploadFile") InputStream in,
            @FormDataParam("uploadFile") FormDataContentDisposition info,
            @FormDataParam("name") String filename,
            @HeaderParam("serialNo") long serialNo) {

        Language file = new Language();
        
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

        if (FolderManager.exist(Constants.SOURCE_FOLDER, filename)) {
            // conflict error the language exist
            throw new ConflictException("Language "+filename+" already exist!");
        }
        filename = filename.concat("_"+serialNo+Constants.ZIP_FORMAT);
        
        file.save(in, filename);
        file.decompress(filename);
        ResponseMessage message = new ResponseMessage("Upload "+info.getFileName(), Status.CREATED.getStatusCode(), null);
        return Response.status(Status.CREATED).entity(message).build();
    }
    
    // */comalat/languages/update
    @PUT
    @Path("/update")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateFile(
            @FormDataParam("uploadFile") InputStream in,
            @FormDataParam("uploadFile") FormDataContentDisposition info,
            @FormDataParam("name") String filename,
            @HeaderParam("serialNo") long serialNo) {

        Language file = new Language();
        
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
        String langName = new String(filename);
        filename = filename.replace(" ", "");
        filename = filename.concat("_"+serialNo+Constants.ZIP_FORMAT);
        
        file.save(in, filename);
        if (FolderManager.getPath(Constants.SOURCE_FOLDER, langName) != null) {
            FolderManager.delete(FolderManager.getPath(Constants.SOURCE_FOLDER, langName));
        }
        
        file.decompress(filename);
        
        ResponseMessage message = new ResponseMessage("Updated "+info.getFileName(), Status.OK.getStatusCode(), null);
        return Response.status(Status.OK).entity(message).build();
    }
    
    /*********** SubResources ***********/
    
    // levels subresource
    // */comalat/languages/{lang}/levels/
    @Path("/{lang}/levels")
    public LevelsResources levelSubresource(){
        return new LevelsResources();
    }
}