/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sunbird.common.optimizr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.activation.MimetypesFileTypeMap;

import org.sunbird.telemetry.logger.TelemetryManager;

/**
 *
 * @author feroz
 */
public class FileUtils {

    private static DecimalFormat decimalFormat = new DecimalFormat("#.00");
    private static MimetypesFileTypeMap mimeTypesMap = null;

    public static long printFileSize(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            long bytes = file.length();
            double kilobytes = (bytes / 1024);
            double megabytes = (kilobytes / 1024);
            System.err.println(fileName + " Size: " + decimalFormat.format(megabytes) + " MB");
            //LOGGER.error(fileName + " Size: " + decimalFormat.format(megabytes) + " MB");
            return bytes;
        }
        return 0;
    }

    public static void extract(String fileName, String outputFolder) {
    	extract(new File(fileName), outputFolder);
    }
    
    public static void extract(File zfile, String outputFolder) {

        try {
            // Open the zip file
        	TelemetryManager.log("extract | file =" + zfile.getName() + " | outputFolder =" + outputFolder);
            ZipFile zipFile = new ZipFile(zfile);
            Enumeration<?> enu = zipFile.entries();
            while (enu.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enu.nextElement();

                String name = zipEntry.getName();

                if (name.startsWith("__MACOSX") || (name.startsWith("."))) {
                    continue;
                }
                
                long size = zipEntry.getSize();
                long compressedSize = zipEntry.getCompressedSize();
                // System.out.printf("name: %-20s | size: %6d | compressed size: %6d \n", name, size, compressedSize);

                // Do we need to create a directory ?
                File file = new File(outputFolder + "/" + name);
                if (name.endsWith("/")) {
                    file.mkdirs();
                    continue;
                }

                File parent = file.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }

                // Extract the file
                InputStream is = zipFile.getInputStream(zipEntry);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = is.read(bytes)) >= 0) {
                    fos.write(bytes, 0, length);
                }
                is.close();
                fos.close();

            }
            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void compress(String zipFileName, String dir) throws Exception {
    	TelemetryManager.log("compress | zipFileName =" + zipFileName + " | dir =" + dir);
    	File dirObj = new File(dir);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName, false));
        System.out.println("Creating : " + zipFileName);
        addDir(dirObj, out, dirObj);
        out.close();
    }
    
    private static void addDir(File dirObj, ZipOutputStream out, File baseDir) throws IOException {
        File[] files = dirObj.listFiles();
        byte[] tmpBuf = new byte[1024];

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addDir(files[i], out, baseDir);
                continue;
            }
            FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
            String relative = baseDir.toURI().relativize(files[i].toURI()).getPath();
            // System.out.println(" Adding: " + relative);
            
            out.putNextEntry(new ZipEntry(relative));
            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                out.write(tmpBuf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
    }
    
    public static String getOutputFileName(File input) {
        String outputFileName = input.getName().replaceAll("\\.", "\\.out\\.");
        String outputFolder = input.getParent();
        return outputFolder + "/" + outputFileName;
    }
    
    public static void replace(File input, File output) {
        String inputFile = input.getAbsolutePath();
        input.delete();
        output.renameTo(new File(inputFile));
    }
    
    public static FileType getFileType(File file) {

        if (file.isDirectory()) return FileType.Directory;
        
        if (mimeTypesMap == null) {
            mimeTypesMap = new MimetypesFileTypeMap();
            mimeTypesMap.addMimeTypes("image png jpg jpeg");
            mimeTypesMap.addMimeTypes("audio mp3 ogg wav");
            mimeTypesMap.addMimeTypes("video mp4");
        }
        
        String mimeType = mimeTypesMap.getContentType(file);
        
        if (mimeType != null) {
            String type = mimeType.split("/")[0];
            if (type.equals("image")) {
                return FileType.Image;
            } else if (type.equals("audio")) {
                return FileType.Audio;
            } else if (type.equals("video")) {
                return FileType.Video;
            }

        }
        
        return FileType.Other;
    }
    
    public static String getThumbnailFileName(File input) {
        String outputFileName = input.getName().replaceAll("\\.", "\\.thumb\\.");
        String outputFolder = input.getParent();
        return outputFolder + "/" + outputFileName;
    }

	/**
	 * @param file
	 * @return Boolean
	 */
	public static boolean deleteFile(File file) {
		if (!file.exists())
			return true;

		int count = 0;
		boolean deleted;
		do {
			deleted = file.delete();
			if (!deleted) {
				count++;
				waitAndThenTriggerGC();
			}
		} while (!deleted && count <= 5);
		return deleted;
	}

	private static void waitAndThenTriggerGC() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
		System.gc();
	}
}
