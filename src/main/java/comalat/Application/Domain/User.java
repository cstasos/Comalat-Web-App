package comalat.Application.Domain;

import comalat.HelperManager.FileManager.AccessData;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author SyleSakis
 */
@XmlRootElement
public class User {
    
    private String username;
    private String password;
    private String fullname;

    public User() {
    }

    public User(String username, String password, String fullname) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public boolean isAuthorized() {
        if(AccessData.compareData(username, password)){
            this.fullname = AccessData.getFullname();
            return true;
        }
        return false;
    }

    public void save() {
        AccessData.updateAccessFile(username, password, fullname);
    }
    
}
