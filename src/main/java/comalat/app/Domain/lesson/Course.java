package comalat.app.Domain.lesson;

import comalat.app.Domain.Folder;
import comalat.app.Constants;
import comalat.app.HelperManager.FileManager.WordManager;
import comalat.app.HelperManager.FolderHelper.FolderManager;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author SyleSakis
 */
@XmlRootElement
public class Course extends Folder<Course> {

    @XmlElement(name = "Course")
    private String courseName;
    @XmlElement(name = "Units")
    private List<Unit> units;
    @XmlTransient
    private int noUnits=0;
    @XmlTransient
    private long lastupdate = 0;

    public Course() {
        units = new ArrayList<>();
    }
    
    public Course(File file) {
        super(file);
        units = new ArrayList<>();
    }
    
    public Course(String lang, String level, String course) {
        super(FolderManager.getPath(FolderManager.getPath(FolderManager.getPath(Constants.SOURCE_FOLDER, lang), level), course));
        units = new ArrayList<>();
    }

    public Course(String courseName, List<Unit> units) {
        this.courseName = courseName;
        this.units = units;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void addUnit(Unit u) {
        units.add(u);
    }

    @Override
    @XmlElement(name = "size")
    public long getSize() {
        long size = 0;
        for (Unit unit : units) {
            size += unit.getSize();
        }
        return size;
    }

    @Override
    //sourcePath = */Comalat-Folders/comalat-pdf-files/{langName}/{levelType}/{courses-x-y}
    public Course readFromFolder(String sourcePath) {
        File directory = new File(sourcePath);
        File contents = null;
        this.courseName = directory.getName();
        lastupdate = directory.lastModified();

        for (File folder : directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile()) {
                    return pathname.getName().endsWith(".doc") || pathname.getName().endsWith(".docx");
                }
                return false;
            }
        })) {
            if (folder != null) {
                contents = new File(folder.getAbsolutePath());
            }
        }

        for (File folder : directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        })) {
            Unit unit = new Unit();
            unit = unit.readFromFolder(folder.getPath());
            unit.setUnitContents(WordManager.readTable(contents));
            noUnits += unit.getNoOfUnits();
            setLastUpdate(unit.getLastUpdate());
            units.add(unit);

        }
        return this;
    }

    @Override
    public int getNoOfUnits() {
        return noUnits;
    }

    @Override
    @XmlTransient
    public long getLastUpdate() {
        return lastupdate;
    }
    
    public void setLastUpdate(long update) {
        if(update > lastupdate){
            lastupdate = update;
        }
    }

    @Override
    public void decompress(String source, String filename){
        super.decompress(source, filename);
    }
    
}
