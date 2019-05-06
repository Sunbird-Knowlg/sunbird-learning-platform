package org.ekstep.common.util;


import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains methods for authorizing a user and caching credentials.
 * 
 * @author gauraw
 *
 */
public class YouTubeUrlUtil {

	/**
	 * Define a global instance of the HTTP transport.
	 */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/**
	 * Define a global instance of the JSON factory.
	 */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	
	private static final String ERR_MSG = "Please Provide Valid YouTube URL!";
	private static final String SERVICE_ERROR = "Unable to Check License. Please Try Again After Sometime!";
	private static final List<String> errorCodes = Arrays.asList("dailyLimitExceeded402", "limitExceeded",
			"dailyLimitExceeded", "quotaExceeded", "userRateLimitExceeded", "quotaExceeded402", "keyExpired",
			"keyInvalid");
	private static boolean limitExceeded = false;
	private static YouTube youtube = null;
	private static List<String> validLicenses = Platform.config.hasPath("learning.valid_license") ? 
			Platform.config.getStringList("learning.valid_license") : Arrays.asList("creativeCommon");

	static {
		String youtubeAppName = Platform.config.hasPath("learning.content.youtube.application.name")
				? Platform.config.getString("learning.content.youtube.application.name") : "fetch-youtube-license";
		youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
			public void initialize(HttpRequest request) throws IOException {
			}
		}).setApplicationName(youtubeAppName).build();
	}

	/**
	 * This Method will fetch license for given YouTube Video URL.
	 * 
	 * @param videoUrl
	 * @return licenceType
	 */
	public static String getLicense(String videoUrl) {
		String videoId = getIdFromUrl(videoUrl);
		if(StringUtils.isBlank(videoId))
			throw new ClientException(TaxonomyErrorCodes.ERR_INVALID_URL.name(), ERR_MSG);
		
		String licenceType = "";
		try {
			YouTube.Videos.List videosListByIdRequest = youtube.videos().list("status");
			String apiKey = Platform.config.getString("learning_content_youtube_apikey");
			videosListByIdRequest.setKey(apiKey);
			videosListByIdRequest.setId(videoId);
			VideoListResponse response = videosListByIdRequest.execute();
			List<Video> videoList = response.getItems();

			if (null != videoList && !videoList.isEmpty()) {
				Iterator<Video> itr = videoList.iterator();
				while (itr.hasNext()) {
					Video singleVideo = itr.next();
					licenceType = singleVideo.getStatus().getLicense().toString();
				}
			}
		} catch (GoogleJsonResponseException ex) {
			Map<String, Object> error = ex.getDetails().getErrors().get(0);
			String reason = (String) error.get("reason");
			if (errorCodes.contains(reason)) {
				limitExceeded = true;
				TelemetryManager
						.log("Youtube API Limit Exceeded. Reason is: " + reason + " | Error Details : " + ex);
			}
		} catch (Exception e) {
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
					"Something Went Wrong While Processing Your Request. Please Try Again After Sometime!");
		}
		if (StringUtils.isBlank(licenceType) && !limitExceeded)
			throw new ClientException(TaxonomyErrorCodes.ERR_YOUTUBE_LICENSE_VALIDATION.name(), ERR_MSG);

		if (StringUtils.isBlank(licenceType) && limitExceeded)
			throw new ClientException(TaxonomyErrorCodes.ERR_YOUTUBE_LICENSE_VALIDATION.name(), SERVICE_ERROR);

		return licenceType;
	}

	public static boolean isValidLicense(String license) {
        return validLicenses.contains(license);
    }
	private static String getIdFromUrl(String url) {
		String videoLink = getVideoLink(url);
		List<String> videoIdRegex = Platform.config.getStringList("youtube.license.regex.pattern");
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
		final String youTubeUrlRegEx = "^(https?)?(://)?(www.)?(m.)?((youtube.com)|(youtu.be))/";
		Pattern compiledPattern = Pattern.compile(youTubeUrlRegEx);
		Matcher matcher = compiledPattern.matcher(url);

		if (matcher.find()) {
			return url.replace(matcher.group(), "");
		}
		return url;
	}

	/**
	 *
	 * @param videoId
	 * @param params
	 * @return
	 */
	private static List<Video> getVideoList(String videoId, String params) {
		try {
			YouTube.Videos.List videosListByIdRequest = youtube.videos().list(params);
			String apiKey = Platform.config.getString("learning_content_youtube_apikey");
			videosListByIdRequest.setKey(apiKey);
			videosListByIdRequest.setId(videoId);
			VideoListResponse response = videosListByIdRequest.execute();
			return response.getItems();
		} catch (GoogleJsonResponseException ex) {
			Map<String, Object> error = ex.getDetails().getErrors().get(0);
			String reason = (String) error.get("reason");
			if (errorCodes.contains(reason)) {
				limitExceeded = true;
				TelemetryManager
						.log("Youtube API Limit Exceeded. Reason is: " + reason + " | Error Details : " + ex);
			}
		} catch (Exception e) {
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
					"Something Went Wrong While Processing Youtube Video. Please Try Again After Sometime!");
		}
		return null;
	}

	public static Map<String,Object> getVideoInfo(String videoUrl, String apiParams, String... metadata) {
		Video video = null;
		Map<String,Object> result = new HashMap<String, Object>();
		String videoId = getIdFromUrl(videoUrl);
		List<Video> videoList = getVideoList(videoId, apiParams);
		if(null != videoList && !videoList.isEmpty()) {
			video = videoList.get(0);
		}

		for(String str: metadata){
			if("license".equalsIgnoreCase(str)){
				String license = video.getStatus().getLicense().toString();
				if(StringUtils.isNotBlank(license))
					result.put(str,license);
			}

			if("thumbnail".equalsIgnoreCase(str)) {
				
			}
		}

		return result;
	}
}
