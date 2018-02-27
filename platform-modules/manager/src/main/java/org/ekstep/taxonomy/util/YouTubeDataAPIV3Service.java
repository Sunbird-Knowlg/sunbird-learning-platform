package org.ekstep.taxonomy.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.dac.model.Node;

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
	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/**
	 * Define a global instance of the JSON factory.
	 */
	public static final JsonFactory JSON_FACTORY = new JacksonFactory();

	/**
	 * This method will check YouTube License and Insert as Node MetaData
	 * 
	 * @param String
	 * @param Node
	 * @return Node
	 */
	public static Node validateYoutubeLicense(String artifactUrl, Node node) throws Exception {
		Boolean isValReq = Platform.config.hasPath("learning.content.youtube.validate.license")
				? Platform.config.getBoolean("learning.content.youtube.validate.license") : false;

		if (isValReq) {
			String licenseType = null;
			String videoId = getVideoIdFromUrl(artifactUrl);
			if (StringUtils.isBlank(videoId))
				throw new ClientException("ERR_YOUTUBE_LICENSE_VALIDATION", "Please Provide Valid YouTube URL!");
			licenseType = getYoutubeLicense(videoId);
			if (StringUtils.isBlank(licenseType)) {
				throw new ClientException("ERR_YOUTUBE_LICENSE_VALIDATION", "Please Provide Valid YouTube URL!");
			}
			if (StringUtils.equalsIgnoreCase("youtube", licenseType))
				node.getMetadata().put("license", "Standard YouTube License");
			if (StringUtils.equalsIgnoreCase("creativeCommon", licenseType))
				node.getMetadata().put("license", "Creative Commons Attribution (CC BY)");
		}
		return node;
	}

	private static String getYoutubeLicense(String videoId) throws Exception {
		String licenceType = "";
		YouTube youtube = getYouTubeService();
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
		return licenceType;
	}

	private static String getVideoIdFromUrl(String artifactUrl) {
		String[] videoIdRegex = { "\\?vi?=([^&]*)", "watch\\?.*v=([^&]*)", "(?:embed|vi?)/([^/?]*)",
				"^([A-Za-z0-9\\-]*)" };

		for (String regex : videoIdRegex) {
			Pattern compiledPattern = Pattern.compile(regex);
			Matcher matcher = compiledPattern.matcher(artifactUrl);
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		return null;
	}

	/**
	 * This Method will return YouTube Service Object
	 */
	private static YouTube getYouTubeService() {
		YouTube youtube = null;
		String youtubeAppName = Platform.config.hasPath("learning.content.youtube.application.name")
				? Platform.config.getString("learning.content.youtube.application.name") : "fetch-youtube-license";
		youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
			public void initialize(HttpRequest request) throws IOException {
			}
		}).setApplicationName(youtubeAppName).build();
		return youtube;
	}

}
