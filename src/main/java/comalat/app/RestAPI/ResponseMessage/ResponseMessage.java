package comalat.app.RestAPI.ResponseMessage;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author SyleSakis
 */
@XmlRootElement
public class ResponseMessage {
    
    private String message;
    private int code;
    private String documentation;

    public ResponseMessage() {
    }
    
    public ResponseMessage(String message, int code, String documantation) {
        this.message = message;
        this.code = code;
        this.documentation = documantation;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
