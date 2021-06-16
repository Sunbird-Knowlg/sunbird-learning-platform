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

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Slug;
import org.ekstep.telemetry.logger.TelemetryManager;

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
			return CommonCloudStore.download(fileURL, saveDir);

		} catch (Exception e) {
			e.printStackTrace();
			TelemetryManager.error("Error! While Downloading File:"+ e.getMessage(), e);
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