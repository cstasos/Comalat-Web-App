package comalat.app.Domain.lesson;

import comalat.app.Domain.lesson.Course;
import comalat.app.Domain.Enum.LevelType;
import comalat.app.Constants;
import comalat.app.Domain.Folder;
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
public class Level extends Folder<Level> {

    @XmlElement(name = "EducationLevel")
    private LevelType level;
    @XmlElement(name = "Courses")
    private List<Course> courses;
    @XmlTransient
    private int noUnits = 0;
    @XmlTransient
    private long lastupdate = 0;

    public Level() {
        courses = new ArrayList<>();
    }
    
    public Level(File file){
        super(file);
        courses = new ArrayList<>();
    }
    
    public Level(String lang, String level){
        super(FolderManager.getPath(FolderManager.getPath(Constants.SOURCE_FOLDER, lang), level));
        courses = new ArrayList<>();
    }

    public Level(LevelType level, List<Course> courses) {
        this.level = level;
        this.courses = courses;
    }

    @XmlTransient
    public LevelType getLevelType() {
        return level;
    }

    @XmlTransient
    public void setLevelType(LevelType level) {
        this.level = level;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public void addCourse(Course c) {
        courses.add(c);
    }

    @Override
    @XmlElement(name = "size")
    public long getSize() {
        long size = 0;
        for (Course c : courses) {
            size += c.getSize();
        }
        return size;
    }

    @Override
    //sourcePath = */Comalat-Folders/comalat-pdf-files/{langName}/{levelType}
    public Level readFromFolder(String sourcePath) {

        File directory = new File(sourcePath);
        lastupdate = directory.lastModified();
        if (directory.getName().equalsIgnoreCase(LevelType.ELEMENTARY.toString())) {
            this.setLevelType(LevelType.ELEMENTARY);
        } else {
            this.setLevelType(LevelType.INTERMEDIATE);
        }

        for (File folder : directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        })) {
            Course course = new Course();
            course.readFromFolder(folder.getPath());
            noUnits += course.getNoOfUnits();
            setLastUpdate(course.getLastUpdate());
            courses.add(course);
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
        if (update > lastupdate) {
            lastupdate = update;
        }
    }
    
    @Override
    public void decompress(String source, String filename){
        super.decompress(source, filename);
    }
}
