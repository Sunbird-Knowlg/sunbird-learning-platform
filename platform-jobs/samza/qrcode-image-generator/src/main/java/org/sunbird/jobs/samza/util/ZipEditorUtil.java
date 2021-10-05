package org.sunbird.jobs.samza.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.List;

public class ZipEditorUtil {

    private static JobLogger LOGGER = new JobLogger(ZipEditorUtil.class);

    public static File zipFiles(List<File> files, String zipName, String basePath) throws IOException {
        File zipFile = new File(basePath + File.separator + zipName + ".zip");
        LOGGER.info("ZipEditorUtil:zipFiles: creating file - " + zipFile.getAbsolutePath());
        zipFile.createNewFile();
        LOGGER.info("ZipEditorUtil:zipFiles: created file - " + zipFile.getAbsolutePath());
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