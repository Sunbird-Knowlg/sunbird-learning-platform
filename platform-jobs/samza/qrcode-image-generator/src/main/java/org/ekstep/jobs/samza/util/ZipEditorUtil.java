package org.ekstep.jobs.samza.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.List;

public class ZipEditorUtil {

    public static File zipFiles(List<File> files, String zipName) throws IOException {
        File zipFile = new File(zipName + ".zip");
        zipFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        for (File file : files) {
            String filePath = file.getAbsolutePath();
            ZipEntry ze = new ZipEntry(file.getName());
            zos.putNextEntry(ze);
            FileInputStream fis = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
            fis.close();
        }
        zos.close();
        fos.close();

        return zipFile;
    }
}