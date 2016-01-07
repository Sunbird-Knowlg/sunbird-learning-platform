package com.ilimi.taxonomy.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * A utility that downloads a file from a URL.
 * 
 * @author Mohammad Azharuddin
 *
 */
public class HttpDownloadUtility {
    private static final int BUFFER_SIZE = 4096;

    /**
     * Downloads a file from a URL
     * 
     * @param fileURL
     *            HTTP URL of the file to be downloaded
     * @param saveDir
     *            path of the directory to save the file
     */
    public static File downloadFile(String fileURL, String saveDir) {
        try {
            URL url = new URL(fileURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();
            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = "";
                String disposition = httpConn.getHeaderField("Content-Disposition");
                httpConn.getContentType();
                httpConn.getContentLength();

                if (disposition != null) {
                    // extracts file name from header field
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10, disposition.length() - 1);
                    }
                } else {
                    // extracts file name from URL
                    fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
                }

                // opens input stream from the HTTP connection
                InputStream inputStream = httpConn.getInputStream();
                String saveFilePath = saveDir + File.separator + fileName;

                // opens an output stream to save into file
                FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                int bytesRead = -1;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();
                return new File(saveFilePath);
            } else {
                System.out.println("No file to download. Server replied HTTP code: " + responseCode);
            }
            httpConn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean isValidUrl(Object url) {
        if (null != url) {
            try {
                new URL(url.toString());
            } catch (MalformedURLException e) {
                return false;
            }
            return true;
        } else {
            return false;
        }

    }

    public static InputStream downloadFile(String fileURL) {
        try {
            final InputStream is = (new URL(fileURL).openConnection()).getInputStream();
            return is;
        } catch (IOException ioe) {
            return null;
        }
    }

    public static void DeleteFiles(List<File> files) {
        for (File file : files) {
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println(file.getName() + " is deleted!");
                }
            }
        }
    }
}
