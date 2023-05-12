package org.sunbird.taxonomy.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.util.YouTubeUrlUtil;
import org.sunbird.graph.cache.util.RedisStoreUtil;
import org.sunbird.graph.engine.common.GraphEngineTestSetup;
import org.sunbird.taxonomy.content.common.TestParams;
import org.sunbird.taxonomy.mgr.IContentManager;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test Cases for YouTube Service
 * 
 * @see YouTubeUrlUtil
 * 
 * @author gauraw
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class YouTubeUrlUtilTest extends GraphEngineTestSetup {

	@Autowired
	private IContentManager contentManager;

	private static ObjectMapper mapper = new ObjectMapper();

	private static String contentId = "UT_YT_01";
	private static boolean isContentCreated = false;
	private static String channelId = "in.ekstep";

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void init() throws Exception {
		loadDefinition("definitions/content_definition.json", "definitions/concept_definition.json",
				"definitions/dimension_definition.json", "definitions/domain_definition.json");
	}

	@AfterClass
	public static void clean() {

	}

	@Before
	public void setup() throws Exception {
		if (!isContentCreated)
			createYoutubeContent();
	}

	private void createYoutubeContent() throws Exception {
		String youtubeContentReq = "{\"identifier\": \"" + contentId
				+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"Test Content\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"video/x-youtube\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}";
		Map<String, Object> youtubeContentMap = mapper.readValue(youtubeContentReq,
				new TypeReference<Map<String, Object>>() {
				});
		youtubeContentMap.put(TestParams.identifier.name(), contentId);
		Response youtubeResponse = contentManager.create(youtubeContentMap, channelId);
		if ("OK".equals(youtubeResponse.getResponseCode().toString()))
			isContentCreated = true;
	}

	// check license of valid youtube url.
	@Test
	public void testYouTubeService_01() throws Exception {
		String artifactUrl = "https://www.youtube.com/watch?v=srVPLrmlBJY";
		String result = YouTubeUrlUtil.getLicense(artifactUrl);
		assertEquals("creativeCommon", result);
	}

	// check license of valid youtube url.
	@Test
	public void testYouTubeService_02() throws Exception {
		String artifactUrl = "https://www.youtube.com/watch?v=_UR-l3QI2nE";
		String result = YouTubeUrlUtil.getLicense(artifactUrl);
		assertEquals("youtube", result);
	}

	// check license of Invalid youtube url.
	@Test
	public void testYouTubeService_03() throws Exception {
		exception.expect(ClientException.class);
		String artifactUrl = "https://goo.gl/bVBJNK";
		String result = YouTubeUrlUtil.getLicense(artifactUrl);
	}

	// check license of valid youtube url.
	@Test
	public void testYouTubeService_04() throws Exception {
		String artifactUrl = "http://youtu.be/-wtIMTCHWuI";
		String result = YouTubeUrlUtil.getLicense(artifactUrl);
		assertEquals("youtube", result);
	}

	// check license of valid youtube url.
	@Test
	public void testYouTubeService_05() throws Exception {
		String artifactUrl = "http://www.youtube.com/v/-wtIMTCHWuI?version=3&autohide=1";
		String result = YouTubeUrlUtil.getLicense(artifactUrl);
		assertEquals("youtube", result);
	}

	// check license of valid youtube url.
	@Test
	public void testYouTubeService_06() throws Exception {
		String artifactUrl = "https://www.youtube.com/embed/7IP0Ch1Va44";
		String result = YouTubeUrlUtil.getLicense(artifactUrl);
		assertEquals("youtube", result);
	}

	// Content Portal also don't have support for such url type.
	@Ignore
	@Test
	public void testYouTubeService_07() throws Exception {
		String artifactUrl = "http://www.youtube.com/attribution_link?a=JdfC0C9V6ZI&u=%2Fwatch%3Fv%3DEhxJLojIE_o%26feature%3Dshare";
		String result = YouTubeUrlUtil.getLicense(artifactUrl);
		assertEquals("youtube", result);
	}

	/*
	 * Upload Valid Youtube URL. 
	 * Expected: 200-OK, license=Creative Commons Attribution (CC BY)
	 */
	@Test
	public void testYouTubeService_08() throws Exception {
		//upload content
		String mimeType = "video/x-youtube";
		String fileUrl = "https://www.youtube.com/watch?v=eKT1IbPjH1Q";
		Response response = contentManager.upload(contentId, fileUrl, mimeType);
		String responseCode = (String) response.getResponseCode().toString();
		assertEquals("OK", responseCode);
		RedisStoreUtil.delete(contentId);
		//Read Content and Verify Result
		Response resp = contentManager.find(contentId, null, null);
		String license = (String) ((Map<String, Object>) resp.getResult().get("content")).get("license");
		String licenseValue = Platform.config.getString("content.license");
		assertEquals(licenseValue, license);
	}

	/*
	 * Upload Valid Youtube URL. 
	 * Expected: 200-OK, license=Standard YouTube License
	 */
	@Test
	public void testYouTubeService_09() throws Exception {
		//upload content
		String mimeType = "video/x-youtube";
		String fileUrl = "http://www.youtube.com/v/-wtIMTCHWuI?version=3&autohide=1";
		Response response = contentManager.upload(contentId, fileUrl, mimeType);
		String responseCode = (String) response.getResponseCode().toString();
		assertEquals("OK", responseCode);
		RedisStoreUtil.delete(contentId);
		delay(2000);
		//Read Content and Verify Result
		Response resp = contentManager.find(contentId, null, null);
		String license = (String) ((Map<String, Object>) resp.getResult().get("content")).get("license");
		assertEquals("Standard YouTube License", license);
	}

	/*
	 * Upload Invalid Youtube URL. 
	 * Expected: 400-CLIENT_ERROR :
	 */
	@Test
	public void testYouTubeService_10() throws Exception {
		exception.expect(ClientException.class);
		String mimeType = "video/x-youtube";
		String fileUrl = "https://goo.gl/bVBJNK";
		Response response = contentManager.upload(contentId, fileUrl, mimeType);
		
	}
	
	// check license of valid youtube url.
	@Test
	public void testYouTubeService_11() throws Exception {
		String artifactUrl = "https://youtu.be/WM4ys_PnrUY";
		String result = YouTubeUrlUtil.getLicense(artifactUrl);
		assertEquals("youtube", result);
	}

	// get thumbnail for youtube video
	@Test
	public void tesGetVideoInfoExpectThumbnail() throws Exception {
		String videoUrl = "https://youtu.be/WM4ys_PnrUY";
		Map<String, Object> result = YouTubeUrlUtil.getVideoInfo(videoUrl, "snippet", "thumbnail");
		assertEquals("https://i.ytimg.com/vi/WM4ys_PnrUY/mqdefault.jpg", (String) result.get("thumbnail"));
	}

	// get video duration for youtube video
	@Test
	public void tesGetVideoInfoExpectDuration() throws Exception {
		String videoUrl = "https://youtu.be/WM4ys_PnrUY";
		Map<String, Object> result = YouTubeUrlUtil.getVideoInfo(videoUrl, "contentDetails", "duration");
		assertEquals("1918", (String) result.get("duration"));
	}

	// get multiple information about youtube video
	@Test
	public void tesGetVideoInfoExpectMultipleData() throws Exception {
		String videoUrl = "https://youtu.be/WM4ys_PnrUY";
		Map<String, Object> result = YouTubeUrlUtil.getVideoInfo(videoUrl, "status,snippet,contentDetails", "license", "thumbnail", "duration");
		assertEquals("youtube", (String) result.get("license"));
		assertEquals("https://i.ytimg.com/vi/WM4ys_PnrUY/mqdefault.jpg", (String) result.get("thumbnail"));
		assertEquals("1918", (String) result.get("duration"));
	}
}
