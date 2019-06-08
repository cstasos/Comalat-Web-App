package comalat.Application.Domain;

import comalat.Application.Domain.Enum.Skill;
import comalat.Constants;
import comalat.HelperManager.FileManager.PDFManager;
import comalat.HelperManager.FolderHelper.FolderManager;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author SyleSakis
 */
@XmlRootElement
public class Unit extends Folder<Unit>  {

    @XmlElement(name = "Unit")
    private String unitName;
    private String[] unitContents = new String[4];
    private List<Skill> exercisedSkills;
    @XmlTransient
    private int noUnits = 0;
    @XmlTransient
    private long lastupdate = 0;

    public Unit() {
    }

    public Unit(String lang, String level, String course, String unit) {
        super(FolderManager.getPath(FolderManager.getPath(FolderManager.getPath(Constants.SOURCE_FOLDER, lang), level), course));
        unitName = unit;
    }

    public Unit(String unitName, String[] unitContents, List<Skill> exercisedSkills, File file) {
        super(file);
        this.unitName = unitName;
        this.unitContents = unitContents;
        this.exercisedSkills = exercisedSkills;
    }

    @XmlElement(name = "Skills")
    public List<String> getExercisedSkillsValue() {
        return Skill.convertSkillstoValues(this.exercisedSkills);
    }

    @XmlTransient
    public List<Skill> getExercisedSkills() {
        return exercisedSkills;
    }

    public void setExercisedSkills(List<Skill> exercisedSkills) {
        this.exercisedSkills = exercisedSkills;
    }

    @XmlElement(name = "Contents")
    public String[] getUnitContents() {
        return unitContents;
    }

    public void setUnitContents(String[] unitContents) {
        this.unitContents = unitContents;
    }

    public void setUnitContents(List<String[]> unitsContents) {
        try {
            String name = unitName.replaceAll("\\D+", "");
            String col = unitName.replace("unit", "");
            int column = Integer.parseInt(name);
            column = (column - 1) % 5;

            this.unitContents = unitsContents.get(column);
        } catch (NumberFormatException ex) {
            return;
        } catch (NullPointerException ex) {
            return;
        }
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    @XmlElement(name = "File")
    public String getFileName() {
        if (file == null) {
            return "EMPTY";
        }
        return file.getName();
    }

    @Override
    @XmlElement(name = "size")
    public long getSize() {
        if (file != null) {
            return file.length();
        }
        return 0;
    }

    @Override
    //sourcePath = */Comalat-Folders/comalat-pdf-files/{langName}/{levelType}/{courses-x-y}/{unitz}
    public Unit readFromFolder(String sourcePath) {
        File directory = new File(sourcePath);
        this.unitName = directory.getName();
        setLastUpdate(directory.lastModified());

        for (File pdfFile : directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (pathname.getPath().endsWith(Constants.PDF_FORMAT)
                        && pathname.isFile());
            }
        })) {
            noUnits = 1;
            this.file = pdfFile;
            this.setExercisedSkills(PDFManager.getSkills(file));
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

    @XmlElement(name = "Update")
    private String getModified() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm dd//MM/yyyy");
        return new String(df.format(new Date(lastupdate)));
    }

    @XmlTransient
    public String getUnitDirecoty() {
        return Paths.get(file.getPath(), unitName).toString();
    }

    @XmlTransient
    public File getUnitFile() {
        return FolderManager.getFileName(getUnitDirecoty());
    }

    @Override
    public void save(InputStream in, String filename) {
        FolderManager.saveUploadedFile(in, getUnitDirecoty(), filename);
    }

    @Override
    public void delete() {
        FolderManager.delete(getUnitDirecoty());
    }
}
