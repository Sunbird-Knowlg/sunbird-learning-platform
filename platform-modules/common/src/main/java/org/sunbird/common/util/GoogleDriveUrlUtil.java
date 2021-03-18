package org.sunbird.common.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.enums.TaxonomyErrorCodes;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.telemetry.logger.TelemetryManager;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.api.services.drive.model.File;

public class GoogleDriveUrlUtil {

	/**
	 * Define a global instance of the HTTP transport.
	 */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/**
	 * Define a global instance of the JSON factory.
	 */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	
	private static final String ERR_MSG = "Please Provide Valid Google Drive URL!";
	private static final String SERVICE_ERROR = "Unable to Check Size. Please Try Again After Sometime!";
	private static final List<String> errorCodes = Arrays.asList("dailyLimitExceeded402", "limitExceeded",
			"dailyLimitExceeded", "quotaExceeded", "userRateLimitExceeded", "quotaExceeded402", "keyExpired",
			"keyInvalid");
	private static boolean limitExceeded = false;
	private static Drive drive = null;
	private static final String googleDriveUrlRegEx = "[-\\w]{25,}";
	private static long sizeLimit = Platform.config.hasPath("MAX_ASSET_FILE_SIZE_LIMIT")
				? Platform.config.getLong("MAX_ASSET_FILE_SIZE_LIMIT") : 52428800;
	static {
		String driveAppName = Platform.config.hasPath("learning.content.drive.application.name")
				? Platform.config.getString("learning.content.drive.application.name") : "google-drive-url-validation";
		drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, null).setApplicationName(driveAppName).build();
	}

	public static Map<String, Object> getMetadata(String driveUrl){
		String videoId = getVideoLink(driveUrl);
		if(StringUtils.isBlank(videoId))
			throw new ClientException(TaxonomyErrorCodes.ERR_INVALID_URL.name(), ERR_MSG);
		Map<String, Object> metadata = new HashMap<>();
		try {
			Get getFile = drive.files().get(videoId);
			getFile.setKey("AIzaSyDeYU2HIyS0I2J4NV72BWUPai13NE_eucE");
			getFile.setFields("id, name, size");
			File googleDriveFile = getFile.execute();
			metadata.put("size", googleDriveFile.get("size"));
			metadata.put("id", googleDriveFile.get("id"));
			metadata.put("name", googleDriveFile.get("name"));
		} catch (GoogleJsonResponseException ex) {
			Map<String, Object> error = ex.getDetails().getErrors().get(0);
			String reason = (String) error.get("reason");
			if (errorCodes.contains(reason)) {
				limitExceeded = true;
				TelemetryManager
						.log("Google Drive API Limit Exceeded. Reason is: " + reason + " | Error Details : " + ex);
			}
		} catch (Exception e) {
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
					"Something Went Wrong While Processing Your Request. Please Try Again After Sometime!");
		}
		
		if (limitExceeded)
			throw new ClientException(TaxonomyErrorCodes.ERR_GOOGLE_DRIVE_SIZE_VALIDATION.name(), SERVICE_ERROR);
		
		return metadata;
	}
	/**
	 * This Method will fetch license for given YouTube Video URL.
	 * 
	 * @param videoUrl
	 * @return licenceType
	 */
	public static Long getSize(String driveUrl) {
		String videoId = getVideoLink(driveUrl);
		if(StringUtils.isBlank(videoId))
			throw new ClientException(TaxonomyErrorCodes.ERR_INVALID_URL.name(), ERR_MSG);
		long size = 0;
		try {
			Get getFile = drive.files().get(videoId);
			getFile.setKey("AIzaSyDeYU2HIyS0I2J4NV72BWUPai13NE_eucE");
			getFile.setFields("id, name, size");
			File googleDriveFile = getFile.execute();
			size = googleDriveFile.get("size")==null?0:(Long)googleDriveFile.get("size");
		} catch (GoogleJsonResponseException ex) {
			Map<String, Object> error = ex.getDetails().getErrors().get(0);
			String reason = (String) error.get("reason");
			if (errorCodes.contains(reason)) {
				limitExceeded = true;
				TelemetryManager
						.log("Google Drive API Limit Exceeded. Reason is: " + reason + " | Error Details : " + ex);
			}
		} catch (Exception e) {
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
					"Something Went Wrong While Processing Your Request. Please Try Again After Sometime!");
		}
		
		if (size == 0 && !limitExceeded)
			throw new ClientException(TaxonomyErrorCodes.ERR_GOOGLE_DRIVE_SIZE_VALIDATION.name(), ERR_MSG);

		if (size == 0 && limitExceeded)
			throw new ClientException(TaxonomyErrorCodes.ERR_GOOGLE_DRIVE_SIZE_VALIDATION.name(), SERVICE_ERROR);
		
		return size;
	}

	public static boolean isValidSize(long size) {
        return sizeLimit>=size;
    }

	private static String getVideoLink(String url) {
		Pattern compiledPattern = Pattern.compile(googleDriveUrlRegEx);
		Matcher matcher = compiledPattern.matcher(url);

		if (matcher.find())
			return matcher.group();
		return url;
	}
}
