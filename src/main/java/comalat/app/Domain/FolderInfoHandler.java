package comalat.app.Domain;

import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author SyleSakis
 */
public interface FolderInfoHandler<T> {

    public long getSize();
    public int getNoOfUnits();
    @XmlTransient
    public long getLastUpdate();
    public T readFromFolder(String sourcePath);  
}
