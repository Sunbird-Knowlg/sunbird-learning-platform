package org.ekstep.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.slugs.Slug;

/**
 * A utility that downloads a file from a URL.
 * 
 * @author Mohammad Azharuddin
 *
 */
public class HttpDownloadUtility {

	private static Logger LOGGER = LogManager.getLogger(HttpDownloadUtility.class.getName());

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
		HttpURLConnection httpConn = null;
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			LOGGER.info("Start Downloading for File: " + fileURL);

			URL url = new URL(fileURL);
			httpConn = (HttpURLConnection) url.openConnection();
			int responseCode = httpConn.getResponseCode();
			LOGGER.info("Response Code: " + responseCode);

			// always check HTTP response code first
			if (responseCode == HttpURLConnection.HTTP_OK) {
				LOGGER.info("Response is OK.");

				String fileName = "";
				String disposition = httpConn.getHeaderField("Content-Disposition");
				httpConn.getContentType();
				httpConn.getContentLength();
				LOGGER.info("Content Disposition: " + disposition);

				if (disposition != null) {
					// extracts file name from header field
					/*int index = disposition.indexOf("filename=");
					if (index > 0) {
						fileName = disposition.substring(index + 10, disposition.length() - 1);
					}*/
					int index = disposition.indexOf("filename=");
					if (index > 0) {
						fileName = disposition.substring(index + 10, disposition.indexOf("\"", index+10));
					}
				} else {
					// extracts file name from URL
					fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
					LOGGER.info("File Name: " + fileName);
				}

				// opens input stream from the HTTP connection
				inputStream = httpConn.getInputStream();
				File saveFile = new File(saveDir);
				if (!saveFile.exists()) {
					saveFile.mkdirs();
				}
				String saveFilePath = saveDir + File.separator + fileName;
				LOGGER.info("Save File Path: " + saveFilePath);

				// opens an output stream to save into file
				outputStream = new FileOutputStream(saveFilePath);

				int bytesRead = -1;
				byte[] buffer = new byte[BUFFER_SIZE];
				while ((bytesRead = inputStream.read(buffer)) != -1)
					outputStream.write(buffer, 0, bytesRead);
				outputStream.close();
				inputStream.close();
				File file = new File(saveFilePath);
				file = Slug.createSlugFile(file);
				LOGGER.info("Sliggified File Name: " + file);

				return file;
			} else {
				LOGGER.info("No file to download. Server replied HTTP code: " + responseCode);
			}
		} catch (Exception e) {
			LOGGER.error("Error! While Downloading File.", e);
		} finally {
			if (null != httpConn)
				httpConn.disconnect();
			if (null != inputStream)
				try {
					inputStream.close();
				} catch (IOException e) {
					LOGGER.error("Error! While Closing the Input Stream.", e);
				}
			if (null != outputStream)
				try {
					outputStream.close();
				} catch (IOException e) {
					LOGGER.error("Error! While Closing the Output Stream.", e);
				}
		}

		LOGGER.info("Something Went Wrong While Downloading the File '" + fileURL + "' returning 'null'.");
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

	public static void DeleteFiles(List<File> files) {
		for (File file : files) {
			if (file.exists() && !file.isDirectory()) {
				if (file.delete()) {
					System.out.println(file.getName() + " is deleted!");
				}
			}
		}
	}

	public static String readFromUrl(String url) {
		URL data;
		StringBuffer sb = new StringBuffer();
		try {
			LOGGER.info("The url to be read: " + url);
			data = new URL(url);
			try (BufferedReader in = new BufferedReader(new InputStreamReader(data.openStream()))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null)
					sb.append(inputLine);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("Data read from url" + sb.toString());
		return sb.toString();
	}

}