package comalat.app.RestAPI.Exceptions;

import comalat.app.RestAPI.ResponseMessage.ResponseMessage;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author SyleSakis
 */
@Provider
public class InvalidInputExceptionMapper implements ExceptionMapper<InvalidInputException>{

    @Override
    public Response toResponse(InvalidInputException ex) {
       ResponseMessage errorMessage = new ResponseMessage(ex.getMessage(), Status.NOT_ACCEPTABLE.getStatusCode(), null);
        return Response.status(Status.NOT_ACCEPTABLE)
                .entity(errorMessage)
                .build(); 
    }
    
}
