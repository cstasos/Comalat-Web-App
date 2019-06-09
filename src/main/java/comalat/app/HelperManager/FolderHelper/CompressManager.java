package comalat.app.HelperManager.FolderHelper;

import comalat.app.Constants;
import comalat.app.RestAPI.Exceptions.ServerProcedureException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author SyleSakis
 */
public class CompressManager {

    public static void addFolderTOZip(String path, String folder, ZipOutputStream zip) {
        File directory = new File(folder);

        if (path.equals("")) {
            path = directory.getName();
        }

        for (File file : directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                } else if (pathname.isFile() && pathname.getName().endsWith(Constants.PDF_FORMAT)) {
                    return true;
                }
                return false;
            }
        })) {
            addFileTOZip(path, Paths.get(folder, file.getName()).toString(), zip);
        }
    }

    private static void addFileTOZip(String path, String source_file, ZipOutputStream zip) {
        File file = new File(source_file);

        if (file.isDirectory()) {
            path = Paths.get(path, file.getName()).toString();
            addFolderTOZip(path, source_file, zip);
        } else {
            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            int length;

            FileInputStream input = null;
            try {
                input = new FileInputStream(file);
                zip.putNextEntry(new ZipEntry(Paths.get(path, file.getName()).toString()));
                while ((length = input.read(buffer)) > 0) {
                    zip.write(buffer, 0, length);
                }
            } catch (IOException ex) {
                Logger.getLogger(CompressManager.class
                        .getName()).log(Level.SEVERE, null, ex);
                throw new ServerProcedureException("Server procedure error. Please try later!");
            } finally {
                try {
                    input.close();

                } catch (IOException ex) {
                    Logger.getLogger(CompressManager.class
                            .getName()).log(Level.SEVERE, null, ex);
                    throw new ServerProcedureException("Server procedure error. Please try later!");
                }
            }
        }
    }

    public static void extractFile(ZipInputStream zis, String filepath) {

        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filepath));
            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            int read = 0;
            while ((read = zis.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
            bos.close();
        } catch (IOException ex) {
            Logger.getLogger(CompressManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerProcedureException("Server procedure error. Please try later!");
        }
    }
}
