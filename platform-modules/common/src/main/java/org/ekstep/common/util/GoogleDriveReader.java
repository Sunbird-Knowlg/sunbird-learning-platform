package org.ekstep.common.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ekstep.common.Platform;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.telemetry.logger.TelemetryManager;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.api.services.drive.model.File;

public class GoogleDriveReader {

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
	/*private static YouTube youtube = null;
	private static List<String> validLicenses = Platform.config.hasPath("learning.valid_license") ? 
			Platform.config.getStringList("learning.valid_license") : Arrays.asList("creativeCommon");
*/
	private static Drive drive = null;
	private static long sizeLimit = 50000000; 
	static {
		String driveAppName = Platform.config.hasPath("learning.content.drive.application.name")
				? Platform.config.getString("learning.content.drive.application.name") : "google-drive-url-validation";
		drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, null).setApplicationName(driveAppName).build();
	}

	/**
	 * This Method will fetch license for given YouTube Video URL.
	 * 
	 * @param videoUrl
	 * @return licenceType
	 */
	public static Long getSize(String driveUrl) {
		String videoId = getIdFromUrl(driveUrl);
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
		} catch(IOException ex) { 
			ex.printStackTrace();
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
	private static String getIdFromUrl(String url) {
		String videoLink = getVideoLink(url);
		List<String> videoIdRegex = Platform.config.getStringList("google.drive.regex.pattern");
		
		for (String regex : videoIdRegex) {
			Pattern compiledPattern = Pattern.compile(regex);
			Matcher matcher = compiledPattern.matcher(videoLink);
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		return null;
	}

	private static String getVideoLink(String url) {
		final String youTubeUrlRegEx = "^(https?)?(://)?(drive.)?(google.)?(com/)";
		Pattern compiledPattern = Pattern.compile(youTubeUrlRegEx);
		Matcher matcher = compiledPattern.matcher(url);

		if (matcher.find()) {
			return url.replace(matcher.group(), "");
		}
		return url;
	}
}
