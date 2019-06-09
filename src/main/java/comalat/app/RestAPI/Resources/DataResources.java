package comalat.app.RestAPI.Resources;

import comalat.app.Domain.Folder;
import comalat.app.Constants;
import comalat.app.Domain.Lessons;
import comalat.app.RestAPI.ResponseMessage.ResponseMessage;
import comalat.app.RestAPI.Exceptions.DataNotFoundException;
import comalat.app.HelperManager.FolderHelper.FolderManager;
import java.io.File;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author SyleSakis
 */
// */comalat/data
@Path("/data")
@Produces(MediaType.APPLICATION_JSON)
public class DataResources {
    
    @GET
    public Response getData(){
        Lessons lessons = new Lessons();
        lessons = lessons.readFromFolder(Constants.SOURCE_FOLDER);
        if(lessons == null){
            throw new DataNotFoundException("Data not found at folder "+ Constants.SOURCE_FOLDER);
        }
        return Response.ok().entity(lessons).build();
    }
    
        // return zip file with all languages
    @GET
    @Path("languages")
    public Response getAllLangauge(
            @HeaderParam("serialNo") long serialNo,
            @HeaderParam("foldername") String foldername) {

        Folder file = new Lessons();
        
        if (serialNo == 0) {
            serialNo = Date.from(Instant.now()).getTime();
        }
        
        if(foldername == null || foldername.isEmpty()){
            foldername = "langs_";
        }
        
        String zipname = foldername + serialNo + Constants.ZIP_FORMAT;
        
        File zipFile = file.compress(zipname);
        return Response.ok(zipFile, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"Languages.zip\"")
                .header("x-zipfilename", zipFile.getName())
                .header("x-fileformat", Constants.ZIP_FORMAT)
                .build();
    }

    // delete source folder
    // */comalat/languages/
    // Response 200 OK || 404 NOT FOUND
    @DELETE
    @Path("languages")
    public Response deleteAllLangauges() {
        
        Folder file = new Lessons();        
        file.delete();

        ResponseMessage sm = new ResponseMessage("Main folder successfully deleted", Status.OK.getStatusCode(), null);
        return Response.status(Status.OK).entity(sm).build();
    }
    
    @DELETE
    @Path("/{zipfilename}")
    public Response deleteDownLoadedZip(@PathParam("zipfilename") String zipFileName){
        if(zipFileName != null){
            String source = Paths.get(Constants.DOWNLOAD_FOLDER, zipFileName).toString();
            FolderManager.delete(source);
            return Response.status(Status.NO_CONTENT).build();
        }
        
        return Response.status(Status.NOT_MODIFIED).build();
    } 
}
