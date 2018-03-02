package org.ekstep.taxonomy.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

/**
 * Contains methods for authorizing a user and caching credentials.
 * 
 * @author gauraw
 *
 */
public class YouTubeDataAPIV3Service {

	/**
	 * Define a global instance of the HTTP transport.
	 */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/**
	 * Define a global instance of the JSON factory.
	 */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	private static final String ERR_MSG = "Please Provide Valid YouTube URL!";

	private static YouTube youtube = null;

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
	public static String getLicense(String videoUrl) throws Exception {
		String videoId = getIdFromUrl(videoUrl);
		String licenceType = "";
		try {
			YouTube.Videos.List videosListByIdRequest = youtube.videos().list("status");
			String apiKey = Platform.config.getString("learning.content.youtube.apikey");
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
		} catch (Exception e) {
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
					"Something Went Wrong While Processing Your Request. Please Try Again After Sometime!");
		}
		if (StringUtils.isBlank(licenceType))
			throw new ClientException(TaxonomyErrorCodes.ERR_YOUTUBE_LICENSE_VALIDATION.name(), ERR_MSG);

		return licenceType;
	}

	private static String getIdFromUrl(String url) {
		String[] videoIdRegex = { "\\?vi?=([^&]*)", "watch\\?.*v=([^&]*)", "(?:embed|vi?)/([^/?]*)",
				"^([A-Za-z0-9\\-]*)" };
		for (String regex : videoIdRegex) {
			Pattern compiledPattern = Pattern.compile(regex);
			Matcher matcher = compiledPattern.matcher(url);
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		return null;
	}
}
