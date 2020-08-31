package org.ekstep.jobs.samza.util;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveRequestInitializer;
import com.google.api.services.drive.DriveScopes;
import org.ekstep.common.Platform;
import org.ekstep.common.Slug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GoogleDriveUtil {

	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	private static final String ERR_MSG = "Please Provide Valid Google Drive URL!";
	private static final String SERVICE_ERROR = "Unable to Connect to Google Service. Please Try Again After Sometime!";
	private static final List<String> errorCodes = Arrays.asList("dailyLimitExceeded402", "limitExceeded",
			"dailyLimitExceeded", "quotaExceeded", "userRateLimitExceeded", "quotaExceeded402", "keyExpired",
			"keyInvalid");
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
	private static final String APP_NAME = Platform.config.hasPath("auto_creator.gdrive.application_name") ? Platform.config.getString("auto_creator.gdrive.application_name") : "drive-download-sunbird";
	private static final String KEY = Platform.config.hasPath("auto_creator.gdrive.api_key") ? Platform.config.getString("auto_creator.gdrive.api_key") : "";
	public static final Integer INITIAL_BACKOFF_DELAY = Platform.config.hasPath("auto_creator.initial_backoff_delay") ? Platform.config.getInt("auto_creator.initial_backoff_delay") : 1200000;    // 20 min
	public static final Integer MAXIMUM_BACKOFF_DELAY = Platform.config.hasPath("auto_creator.maximum_backoff_delay") ? Platform.config.getInt("auto_creator.maximum_backoff_delay") : 3900000;    // 65 min
	public static final Integer INCREMENT_BACKOFF_DELAY = Platform.config.hasPath("auto_creator.increment_backoff_delay") ? Platform.config.getInt("auto_creator.increment_backoff_delay") : 300000; // 5 min
	public static Integer BACKOFF_DELAY = INITIAL_BACKOFF_DELAY;
	public static final GoogleClientRequestInitializer KEY_INITIALIZER = new DriveRequestInitializer(KEY);
	private static boolean limitExceeded = false;
	private static Drive drive = null;
	private static JobLogger LOGGER = new JobLogger(GoogleDriveUtil.class);

	static {
		try {
			HttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, null).setApplicationName(APP_NAME).setGoogleClientRequestInitializer(KEY_INITIALIZER).setSuppressAllChecks(true).build();
		} catch (Exception e) {
			LOGGER.error("Error occurred while creating google drive client ::: " + e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public static File downloadFile(String fileId, String saveDir) {
		try {
			Drive.Files.Get getFile = drive.files().get(fileId);
			LOGGER.info("key ::: " + getFile.getKey());
			getFile.setFields("id,name,size,owners,properties,permissionIds,webContentLink");
			com.google.api.services.drive.model.File googleDriveFile = getFile.execute();
			LOGGER.info("GoogleDriveUtil :: downloadFile ::: Drive File Details:: " + googleDriveFile);
			String fileName = googleDriveFile.getName();
			File saveFile = new File(saveDir);
			if (!saveFile.exists()) {
				saveFile.mkdirs();
			}
			String saveFilePath = saveDir + File.separator + fileName;
			LOGGER.info("File Id :" + fileId + " | Save File Path: " + saveFilePath);

			OutputStream outputStream = new FileOutputStream(saveFilePath);
			getFile.executeMediaAndDownloadTo(outputStream);
			outputStream.close();
			File file = new File(saveFilePath);
			file = Slug.createSlugFile(file);
			LOGGER.info("Sluggified File Name: " + file.getAbsolutePath());
			if (null != file && BACKOFF_DELAY != INITIAL_BACKOFF_DELAY)
				BACKOFF_DELAY = INITIAL_BACKOFF_DELAY;
			return file;
		} catch (Exception e) {
			LOGGER.error("Error While Downloading Google Drive File having Id " + fileId + " : " + e.getMessage(), e);
			e.printStackTrace();
			if (BACKOFF_DELAY <= MAXIMUM_BACKOFF_DELAY)
				delay(BACKOFF_DELAY);
			BACKOFF_DELAY = BACKOFF_DELAY + INCREMENT_BACKOFF_DELAY;
		}
		return null;
	}

	public static void delay(int time) {
		LOGGER.info("delay is called with : " + time);
		try {
			Thread.sleep(time);
		} catch (Exception e) {

		}
	}
}
