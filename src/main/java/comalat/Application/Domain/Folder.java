package comalat.Application.Domain;

import comalat.Application.Exception.ServerProcedureException;
import comalat.Constants;
import comalat.HelperManager.FolderHelper.CompressManager;
import comalat.HelperManager.FolderHelper.FolderManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Sakis
 */
public abstract class Folder<T> implements FolderInfoHandler<T>{

    protected File file;

    public Folder() {
    }

    public Folder(File file) {
        this.file = file;
    }
    
    public Folder(String path) {
        if(path == null)
            file = new File("");
        this.file = new File(path);
    }

    public File compress(String zipname) {
        if (file == null || !file.exists()) {
            throw new ServerProcedureException("Server procedure error. Please try later!");
        }

        try {
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(Paths.get(Constants.DOWNLOAD_FOLDER, zipname).toFile()));
            CompressManager.addFolderTOZip("", file.getPath(), zip);
            zip.flush();
            zip.close();
            return Paths.get(Constants.DOWNLOAD_FOLDER, zipname).toFile();
        } catch (IOException ex) {
            Logger.getLogger(CompressManager.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
            throw new ServerProcedureException("Server procedure error. Please try later!");
        }
    }

    protected void decompress(String destination, String zipfilename) {
        if (!file.exists()) {
            //System.out.println("Zip file does not exist! throw Exception!");
            throw new ServerProcedureException("Server procedure error. Please try later!");
        }

        File destFolder = new File(destination);
        if (!destFolder.exists()) {
            destFolder.mkdir();
        }

        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {
                String filename = ze.getName();
                String filepath = Paths.get(destination, filename).toString();
                if (!ze.isDirectory()) {
                    File tmp = new File(filepath).getParentFile();
                    if (!tmp.exists()) {
                        tmp.mkdirs();
                    }
                    CompressManager.extractFile(zis, filepath);
                } else {
                    File dir = new File(filepath);
                    dir.mkdir();
                }
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            zis.close();
        } catch (IOException ex) {
            Logger.getLogger(CompressManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            throw new ServerProcedureException("Server procedure error. Please try later!");
        } finally {
            // Delete zip file after unzip
            this.delete();
        }
    }
    
    public void save(InputStream in, String filename){
        this.file = Paths.get(Constants.UPLOAD_FOLDER, filename).toFile();
        FolderManager.saveUploadedFile(in, Constants.UPLOAD_FOLDER, filename);
    }

    public void delete() {
        if (this.file != null) {
            String source = file.getPath();
            FolderManager.delete(source);
        }
    }

    @XmlTransient
    public File getFile() {
        return this.file;
    }
    
    public void setFile(File file) {
        this.file = file;
    }

    public boolean exists() {
        
        if(file == null)
            return false;
        
        return file.exists();
    }

}
