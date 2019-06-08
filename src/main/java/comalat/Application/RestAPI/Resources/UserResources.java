package comalat.Application.RestAPI.Resources;

import comalat.Application.Domain.ResponseMessage.ResponseMessage;
import comalat.Application.Domain.User;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 *
 * @author SyleSakis
 */
@Path("login")
public class UserResources {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(
            @FormDataParam("username") String username,
            @FormDataParam("password") String password) {
        
        User user = new User(username, password);
        
        if(user.isAuthorized())
            return Response.ok().entity(user).build();

        ResponseMessage em = new ResponseMessage();
        em.setMessage("Invalid Username or Password!");
        em.setCode(Status.FORBIDDEN.getStatusCode());
        em.setDocumentation("Contact us for more information");
        return Response.status(Status.FORBIDDEN).entity(em).build();
    }

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response edit(
            @FormDataParam("username") String username,
            @FormDataParam("password") String password,
            @FormDataParam("fullname") String fullname) {

        User user = new User(username, password, fullname);
        user.save();
        
        ResponseMessage sm = new ResponseMessage();
        sm.setMessage("Updated successfully!!");
        sm.setCode(Status.OK.getStatusCode());
        sm.setDocumentation("");
        return Response.ok().entity(sm).build();

    }
}
