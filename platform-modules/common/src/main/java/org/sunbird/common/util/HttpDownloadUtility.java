package org.sunbird.common.util;

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

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Slug;
import org.sunbird.telemetry.logger.TelemetryManager;

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
		HttpURLConnection httpConn = null;
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			URL url = new URL(fileURL);
			httpConn = (HttpURLConnection) url.openConnection();
			int responseCode = httpConn.getResponseCode();
			TelemetryManager.log("Response Code: " + responseCode);

			// always check HTTP response code first
			if (responseCode == HttpURLConnection.HTTP_OK) {
				TelemetryManager.log("Response is OK.");

				String fileName = "";
				String disposition = httpConn.getHeaderField("Content-Disposition");
				httpConn.getContentType();
				httpConn.getContentLength();
				TelemetryManager.log("Content Disposition: " + disposition);

				if (StringUtils.isNotBlank(disposition)) {
					int index = disposition.indexOf("filename=");
					if (index > 0) {
						fileName = disposition.substring(index + 10, disposition.indexOf("\"", index+10));
					}
				}
				if (StringUtils.isBlank(fileName)) {
					fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
				}

				// opens input stream from the HTTP connection
				inputStream = httpConn.getInputStream();
				File saveFile = new File(saveDir);
				if (!saveFile.exists()) {
					saveFile.mkdirs();
				}
				String saveFilePath = saveDir + File.separator + fileName;
				TelemetryManager.log("FileUrl :" + fileURL +" , Save File Path: " + saveFilePath);

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
				TelemetryManager.log("Sluggified File Name: " + file.getAbsolutePath());

				return file;
			} else {
				TelemetryManager.log("No file to download. Server replied HTTP code: " + responseCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			TelemetryManager.error("Error! While Downloading File:"+ e.getMessage(), e);
		} finally {
			if (null != httpConn)
				httpConn.disconnect();
			if (null != inputStream)
				try {
					inputStream.close();
				} catch (IOException e) {
					TelemetryManager.error("Error! While Closing the Input Stream: "+ e.getMessage(),e );
				}
			if (null != outputStream)
				try {
					outputStream.close();
				} catch (IOException e) {
					TelemetryManager.error("Error! While Closing the Output Stream: "+ e.getMessage(), e);
				}
		}

		TelemetryManager.warn("Something Went Wrong While Downloading the File '" + fileURL + "' returning 'null'. File url: "+ fileURL);
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

	public static void deleteFiles(List<File> files) {
		for (File file : files) {
			if (file.exists() && !file.isDirectory()) {
				if (file.delete()) {
					System.out.println(file.getAbsolutePath() + " is deleted!");
				} else {
					System.out.println(file.getAbsolutePath() + " not deleted!");
				}
			}
		}
	}

	public static String readFromUrl(String url) {
		URL data;
		StringBuffer sb = new StringBuffer();
		try {
			TelemetryManager.log("The url to be read: " + url);
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
		TelemetryManager.log("Data read from url: " + sb.toString());
		return sb.toString();
	}

}