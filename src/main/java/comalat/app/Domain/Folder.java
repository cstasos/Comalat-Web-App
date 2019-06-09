package comalat.app.Domain;

import comalat.app.RestAPI.Exceptions.ServerProcedureException;
import comalat.app.Constants;
import comalat.app.Domain.FolderInfoHandler;
import comalat.app.HelperManager.FolderHelper.CompressManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Sakis
 */
public abstract class Folder<T> implements FolderInfoHandler<T> {

    protected File file;

    public Folder() { }

    public Folder(File file) { this.file = file; }

    public Folder(String path) {
        if (path == null) {
            file = new File("");
        }
        this.file = new File(path);
    }
    
    public boolean exists() {
        if (file == null) {
            return false;
        }
        return file.exists();
    }

    public File compress(String zipname) {
        if (file == null || !file.exists())
            throw new ServerProcedureException("Server procedure error. Please try later!");

        try {
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(Paths.get(Constants.DOWNLOAD_FOLDER, zipname).toFile()));
            CompressManager.addFolderTOZip("", file.getPath(), zip);
            zip.flush();
            zip.close();
            return Paths.get(Constants.DOWNLOAD_FOLDER, zipname).toFile();
        } catch (IOException ex){
            throw new ServerProcedureException("Server procedure error. Please try later!");
        }
    }

    protected void decompress(String destination, String zipfilename) {
        if (!file.exists())
            throw new ServerProcedureException("Server procedure error. Please try later!");

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
            throw new ServerProcedureException("Server procedure error. Please try later!");
        } finally {
            this.delete();
        }
    }

    public void save(InputStream in, String filename) {
        String destination = Constants.UPLOAD_FOLDER;
        this.file = Paths.get(destination, filename).toFile();
        File dir = new File(destination);
        if (!dir.exists()) {
            dir.mkdir();
        }
        
        File upload = new File(Paths.get(destination, filename).toString());
        try {
            Files.copy(in, upload.toPath());
        } catch (IOException ex) {
            throw new ServerProcedureException("Server procedure error. Please try later!");
        }
    }

    public void delete() {
        if (this.file != null) {
            String source = file.getPath();
            Path directory = Paths.get(source);
            try {
                Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(final Path file, final IOException e) {
                        return handleException(e);
                    }

                    private FileVisitResult handleException(final IOException e) {
                        return TERMINATE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
                            throws IOException {
                        if (e != null) {
                            return handleException(e);
                        }
                        Files.delete(dir);
                        return CONTINUE;
                    }
                });
            } catch (IOException ex) {
                delete();
            }
        }
    }

    @XmlTransient
    public File getFile() {
        return this.file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
