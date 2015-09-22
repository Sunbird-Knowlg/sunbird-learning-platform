package org.apache.cordova;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

import org.apache.cordova.CallbackContext;


/**
 * 
 * The utility class is responsible for unpacking tar or tar.gz packages. 
 * The class uses <a href="http://commons.apache.org/compress/">Apache Commons Compress</a> library.
 * 
 * @author johnkil
 * 
 */
public class TarUtils {
    private static final String LOG_TAG = TarUtils.class.getSimpleName();

    private static final int BUFFER_SIZE = 8 * 1024;

    /**
     * Unpack the archive to specified directory.
     * 
     * @param file tar or tar.gz filed
     * @param outputDir destination directory
     * @param isGZipped true if the file is gzipped.
     * @return true in case of success, otherwise - false
     */
    public static void untarFile(File file, File outputDir, boolean isGZipped, CallbackContext callbackContext) {
        FileInputStream fileInputStream = null;
        TarArchiveInputStream tarArchiveInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            tarArchiveInputStream = (isGZipped) ?
                    new TarArchiveInputStream(new GZIPInputStream(fileInputStream), BUFFER_SIZE) :
                    new TarArchiveInputStream(new BufferedInputStream(fileInputStream), BUFFER_SIZE);
            untar(tarArchiveInputStream, outputDir, callbackContext);
        } catch (IOException e) {
            callbackContext.error(DownloaderService.getErrorJSONObject("SYSTEM_ERROR", e.getMessage()));
        } finally {
            if (tarArchiveInputStream != null) {
                try {
                    tarArchiveInputStream.close();
                } catch (IOException e) {
                    System.out.println("Error:" + e);
                }
            } else if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    System.out.println("Error:" + e);
                }
            }
        }
    }

    /**
     * Unpack data from the stream to specified directory.
     * 
     * @param in stream with tar data
     * @param outputDir destination directory
     * @return true in case of success, otherwise - false
     */
    public static void untar(TarArchiveInputStream in, File outputDir, CallbackContext callbackContext) {
        try {
            TarArchiveEntry entry;
            while ((entry = in.getNextTarEntry()) != null) {
                final File file = new File(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    if (!file.exists()) {
                        if (file.mkdirs()) {
                            System.out.println(file.getAbsolutePath() + "directory created");
                        } else {
                          System.out.println("failure to create directory: " +file.getAbsolutePath());
                        }
                    } else {
                        System.out.println(file.getAbsolutePath()+ " directory is already created");
                    }
                } else if (entry.isFile()) {
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                    try {
                        IOUtils.copy(in, out);
                        out.flush();
                    } finally {
                        try {
                            out.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
            callbackContext.success("OK");
        } catch (IOException e) {
            callbackContext.error(DownloaderService.getErrorJSONObject("SYSTEM_ERROR", e.getMessage()));
        }
    }

}