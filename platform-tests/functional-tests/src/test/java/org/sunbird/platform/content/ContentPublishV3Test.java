package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.jayway.restassured.specification.RequestSpecification;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.platform.domain.BaseTest;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import net.lingala.zip4j.core.ZipFile;

/**
 * Test Cases for Content Publish Usecase.
 * @author gauraw
 */
public class ContentPublishV3Test extends BaseTest{

	private static ClassLoader classLoader = ContentPublishV3Test.class.getClassLoader();
	private static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	private static ObjectMapper mapper=new ObjectMapper();


	//Publish Content with Checklist & Comment
	@Test
	public void publishContentWithCheckListExpect200() {
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn + "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_" + rn + "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		String identifier = createContent(contentType, createValidContent);

		//Upload content
		uploadContent(identifier, "/pdf.pdf");

		//Publish Content
		publishContent(identifier, null, true);
		delay(15000);

		//Read Content and Validate
		JsonPath jsonResponse = readContent(identifier);
		String status = jsonResponse.get("result.content.status");
		List<String> publishChecklist = jsonResponse.get("result.content.publishChecklist");
		String publishComment = jsonResponse.get("result.content.publishComment");
		assertEquals("Live", status);
		assertTrue(publishChecklist.contains("GoodQuality") && publishChecklist.contains("CorrectConcept"));
		assertEquals("OK", publishComment);
	}

	//Publish Content with empty check list.
	//Expected : publishCheckList metadata should not be present under content metadata
	@Test
	public void publishContentWithEmptyCheckList() {
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn + "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_" + rn + "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		String identifier = createContent(contentType, createValidContent);

		//Upload Content
		uploadContent(identifier, "/pdf.pdf");

		//publish content
		String publishContentReq = "{\"request\": {\"content\": {\"publisher\": \"EkStep\",\"lastPublishedBy\": \"Ekstep\",\"publishChecklist\":[],\"publishComment\":\"OK\"}}}";
		publishContent(identifier, publishContentReq, true);
		delay(20000);

		//Read Content and Validate
		JsonPath jsonResponse = readContent(identifier);
		String status = jsonResponse.get("result.content.status");
		List<String> publishChecklist = jsonResponse.get("result.content.publishChecklist");
		String publishComment = jsonResponse.get("result.content.publishComment");
		assertEquals("Live", status);
		assertNull(publishChecklist);
		assertEquals("OK", publishComment);
	}

	// ECAR and Spine ECAR Should Not Have "posterImage" metadata.
	// Ecar Should not have large size image file.
	@Test
	public void testEcarAndSpineEcarForResourceContent() {
		//Create Asset Content
		int rn = generateRandomInt(0, 999999);
		String createAssetContentReq = "{\"request\":{\"content\":{\"name\":\"Test Asset\",\"code\":\"test.asset.1\",\"mimeType\":\"image/jpeg\",\"contentType\":\"Asset\",\"mediaType\":\"image\"}}}";
		String assetId = createContent(contentType, createAssetContentReq);

		//Upload Asset
		String assetUrl = uploadContent(assetId, "/edu-success.jpeg");
		delay(15000);

		//Create Content
		String createResourceContentReq = "{\"request\":{\"content\":{\"name\":\"LP_FT_" + rn + "\",\"code\":\"test.res.1\",\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"appIcon\":\"" + assetUrl + "\"}}}";
		String identifier = createContent(contentType, createResourceContentReq);

		// Upload Content
		uploadContent(identifier, "/pdf.pdf");

		//publish content
		publishContent(identifier, null, true);
		delay(15000);

		// Get Content and Validate
		JsonPath jsonResponse = readContent(identifier);
		String status = jsonResponse.get("result.content.status");
		String posterImage=jsonResponse.get("result.content.posterImage");
		String appIcon=jsonResponse.get("result.content.appIcon");
		assertEquals("Live", status);
		assertEquals(assetUrl,posterImage);
		assertNotNull(appIcon);
		assertTrue(validateEcarForPosterImage(jsonResponse.get("result.content.downloadUrl")));
		assertTrue(validateEcarForPosterImage(jsonResponse.get("result.content.variants.spine.ecarUrl")));
		assertTrue(validateEcarManifestMetadata(jsonResponse.get("result.content.downloadUrl"),"previewUrl","artifactUrl"));
	}

	/*
	 * Given: Publish Document Resource Content
	 * When: Content Publish API Hits
	 * Then: 200-OK, Content Should be in "Live" Status with all required metadata.
	 *
	 */
	@Test
	public void publishDocumentContentExpect200() {
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createDocumentResourceContentReq = "{\"request\":{\"content\":{\"identifier\":\"LP_FT_" + rn + "\",\"name\":\"LP_FT_" + rn + "\",\"code\":\"test.res.1\",\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\"}}}";
		String identifier = createContent(contentType, createDocumentResourceContentReq);

		// Upload Content
		uploadContent(identifier, "/pdf.pdf");

		//publish content
		publishContent(identifier, null, true);
		delay(15000);

		// Get Content and Validate
		JsonPath jsonResponse = readContent(identifier);
		String status = jsonResponse.get("result.content.status");
		String prevState = jsonResponse.get("result.content.prevState");
		String downloadUrl = jsonResponse.get("result.content.downloadUrl");
		String artifactUrl = jsonResponse.get("result.content.artifactUrl");
		String previewUrl = jsonResponse.get("result.content.previewUrl");
		String lastPublishedBy = jsonResponse.get("result.content.lastPublishedBy");
		String lastPublishedOn = jsonResponse.get("result.content.lastPublishedOn");
		String pkgVersion = Float.toString(jsonResponse.get("result.content.pkgVersion"));
		String streamingUrl = jsonResponse.get("result.content.streamingUrl");
		assertEquals("Live", status);
		assertEquals("Draft", prevState);
		assertNotNull(downloadUrl);
		assertNotNull(artifactUrl);
		assertNotNull(lastPublishedOn);
		assertEquals(artifactUrl, previewUrl);
		assertEquals(artifactUrl, streamingUrl);
		assertEquals("EkStep", lastPublishedBy);
		assertEquals("1.0", pkgVersion);
		assertTrue(validateEcarManifestMetadata(downloadUrl, "previewUrl", "artifactUrl"));
	}

	/*
	 * Given: Publish Youtube Resource Content
	 * When: Content Publish API Hits
	 * Then: 200-OK, Content Should be in "Live" Status with all required metadata.
	 *
	 */
	@Test
	public void publishYoutubeContentExpect200() {
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createYoutubeResourceContentReq = "{\"request\":{\"content\":{\"identifier\":\"LP_FT_" + rn + "\",\"name\":\"LP_FT_" + rn + "\",\"code\":\"test.resource.1\",\"mimeType\":\"video/x-youtube\",\"contentType\":\"Resource\"}}}";
		String identifier = createContent(contentType, createYoutubeResourceContentReq);

		//Upload Youtube Video to the Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart("fileUrl", "http://youtu.be/-wtIMTCHWuI").
		when().
		post("/content/v3/upload/" + identifier).
		then().//log().all().
		spec(get200ResponseSpec());

		//publish content
		publishContent(identifier, null, true);
		delay(15000);

		// Get Content and Validate
		JsonPath jsonResponse = readContent(identifier);
		String status = jsonResponse.get("result.content.status");
		String prevState = jsonResponse.get("result.content.prevState");
		String downloadUrl = jsonResponse.get("result.content.downloadUrl");
		String artifactUrl = jsonResponse.get("result.content.artifactUrl");
		String previewUrl = jsonResponse.get("result.content.previewUrl");
		String lastPublishedBy = jsonResponse.get("result.content.lastPublishedBy");
		String lastPublishedOn = jsonResponse.get("result.content.lastPublishedOn");
		String pkgVersion = Float.toString(jsonResponse.get("result.content.pkgVersion"));
		assertEquals("Live", status);
		assertEquals("Draft", prevState);
		assertNotNull(downloadUrl);
		assertNotNull(artifactUrl);
		assertNotNull(lastPublishedOn);
		assertEquals(artifactUrl, previewUrl);
		assertEquals("EkStep", lastPublishedBy);
		assertEquals("1.0", pkgVersion);
		assertTrue(validateEcarManifestMetadata(downloadUrl, "previewUrl", "artifactUrl"));
	}

	/*
	 * Given: Publish Ecml Resource Content
	 * When: Content Publish API Hits
	 * Then: 200-OK, Content Should be in "Live" Status with all required metadata.
	 *
	 */
	@Test
	public void publishEcmlContentExpect200() {
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createEcmlResourceContentReq = "{\"request\":{\"content\":{\"identifier\":\"LP_FT_" + rn + "\",\"name\":\"LP_FT_" + rn + "\",\"code\":\"test.resource.1\",\"mimeType\":\"application/vnd.ekstep.ecml-archive\",\"contentType\":\"Resource\"}}}";
		String identifier = createContent(contentType, createEcmlResourceContentReq);

		//Upload Content
		uploadContent(identifier, "/uploadContent.zip");

		//publish Content
		publishContent(identifier, null, true);
		delay(15000);

		// Get Content and Validate
		JsonPath jsonResponse = readContent(identifier);
		String status = jsonResponse.get("result.content.status");
		String prevState = jsonResponse.get("result.content.prevState");
		String downloadUrl = jsonResponse.get("result.content.downloadUrl");
		String artifactUrl = jsonResponse.get("result.content.artifactUrl");
		String previewUrl = jsonResponse.get("result.content.previewUrl");
		//String publisher = jsonResponse.get("result.content.publisher");
		String lastPublishedBy = jsonResponse.get("result.content.lastPublishedBy");
		String lastPublishedOn = jsonResponse.get("result.content.lastPublishedOn");
		String pkgVersion = Float.toString(jsonResponse.get("result.content.pkgVersion"));
		assertEquals("Live", status);
		assertEquals("Draft", prevState);
		assertNotNull(downloadUrl);
		assertNotNull(artifactUrl);
		assertNotNull(lastPublishedOn);
		assertTrue(previewUrl.endsWith(identifier + "-latest"));
		//assertEquals("EkStep", publisher);
		assertEquals("EkStep", lastPublishedBy);
		assertEquals("1.0", pkgVersion);
		assertTrue(validateEcarManifestMetadata(downloadUrl, "previewUrl", "artifactUrl"));
	}

	/*
	 * Given: Publish Html Resource Content
	 * When: Content Publish API Hits
	 * Then: 200-OK, Content Should be in "Live" Status with all required metadata.
	 *
	 */
	@Test
	public void publishHtmlContentExpect200() {
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createHtmlResourceContentReq = "{\"request\":{\"content\":{\"identifier\":\"LP_FT_" + rn + "\",\"name\":\"LP_FT_" + rn + "\",\"code\":\"test.resource.1\",\"mimeType\":\"application/vnd.ekstep.html-archive\",\"contentType\":\"Resource\"}}}";
		String identifier = createContent(contentType, createHtmlResourceContentReq);

		//Upload Content
		uploadContent(identifier, "/uploadHtml.zip");

		//publish Content
		publishContent(identifier, null, true);
		delay(15000);

		// Get Content and Validate
		JsonPath jsonResponse = readContent(identifier);
		String status = jsonResponse.get("result.content.status");
		String prevState = jsonResponse.get("result.content.prevState");
		String downloadUrl = jsonResponse.get("result.content.downloadUrl");
		String artifactUrl = jsonResponse.get("result.content.artifactUrl");
		String previewUrl = jsonResponse.get("result.content.previewUrl");
		//String publisher = jsonResponse.get("result.content.publisher");
		String lastPublishedBy = jsonResponse.get("result.content.lastPublishedBy");
		String lastPublishedOn = jsonResponse.get("result.content.lastPublishedOn");
		String pkgVersion = Float.toString(jsonResponse.get("result.content.pkgVersion"));
		assertEquals("Live", status);
		assertEquals("Draft", prevState);
		assertNotNull(downloadUrl);
		assertNotNull(artifactUrl);
		assertNotNull(lastPublishedOn);
		assertTrue(previewUrl.endsWith(identifier + "-latest"));
		//assertEquals("EkStep", publisher);
		assertEquals("EkStep", lastPublishedBy);
		assertEquals("1.0", pkgVersion);
		assertTrue(validateEcarManifestMetadata(downloadUrl, "previewUrl", "artifactUrl"));
	}

	/*
	 * Given: Publish H5P Resource Content
	 * When: Content Publish API Hits
	 * Then: 200-OK, Content Should be in "Live" Status with all required metadata.
	 *
	 */
	@Test
	public void publishH5PContentExpect200() {
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createH5pResourceContentReq = "{\"request\":{\"content\":{\"identifier\":\"LP_FT_" + rn + "\",\"name\":\"LP_FT_" + rn + "\",\"code\":\"test.resource.1\",\"mimeType\":\"application/vnd.ekstep.h5p-archive\",\"contentType\":\"Resource\"}}}";
		String identifier = createContent(contentType, createH5pResourceContentReq);

		//Upload Content
		uploadContent(identifier, "/valid_h5p_content.h5p");

		//publish Content
		publishContent(identifier, null, true);
		delay(15000);

		// Get Content and Validate
		JsonPath jsonResponse = readContent(identifier);
		String status = jsonResponse.get("result.content.status");
		String prevState = jsonResponse.get("result.content.prevState");
		String downloadUrl = jsonResponse.get("result.content.downloadUrl");
		String artifactUrl = jsonResponse.get("result.content.artifactUrl");
		String previewUrl = jsonResponse.get("result.content.previewUrl");
		//String publisher = jsonResponse.get("result.content.publisher");
		String lastPublishedBy = jsonResponse.get("result.content.lastPublishedBy");
		String lastPublishedOn = jsonResponse.get("result.content.lastPublishedOn");
		String pkgVersion = Float.toString(jsonResponse.get("result.content.pkgVersion"));
		assertEquals("Live", status);
		assertEquals("Draft", prevState);
		assertNotNull(downloadUrl);
		assertNotNull(artifactUrl);
		assertNotNull(lastPublishedOn);
		assertTrue(previewUrl.endsWith(identifier + "-latest"));
		//assertEquals("EkStep", publisher);
		assertEquals("EkStep", lastPublishedBy);
		assertEquals("1.0", pkgVersion);
		assertTrue(validateEcarManifestMetadata(downloadUrl, "previewUrl", "artifactUrl"));
	}

	/*
	 * Given: Publish MP4 Resource Content
	 * When: Content Publish API Hits
	 * Then: 200-OK, Content Should be in "Live" Status with all required metadata. ("streamingUrl" should be available after 30 min.)
	 *
	 */
	@Test
	public void publishVideoMP4ContentExpectStreamingUrlSuccess() {
		//Create Content
		String createVideoMP4ContentReq = "{\"request\":{\"content\":{\"name\":\"Resource Content for Video Streaming\",\"code\":\"test.resource.1\",\"mimeType\":\"video/mp4\",\"contentType\":\"Resource\"}}}";
		String identifier = createContent(contentType, createVideoMP4ContentReq);

		//Upload Content
		uploadContent(identifier, "/small.mp4");

		//publish Content
		publishContent(identifier, null, true);
		delay(1500000);

		// Get Content and Validate
		JsonPath jsonResponse = readContent(identifier);
		String status = jsonResponse.get("result.content.status");
		String prevState = jsonResponse.get("result.content.prevState");
		String downloadUrl = jsonResponse.get("result.content.downloadUrl");
		String artifactUrl = jsonResponse.get("result.content.artifactUrl");
		String streamingUrl = jsonResponse.get("result.content.streamingUrl");
		String lastPublishedOn = jsonResponse.get("result.content.lastPublishedOn");
		String pkgVersion = Float.toString(jsonResponse.get("result.content.pkgVersion"));
		assertEquals("Live", status);
		assertEquals("Draft", prevState);
		assertNotNull(downloadUrl);
		assertNotNull(artifactUrl);
		assertNotNull(lastPublishedOn);
		assertTrue(streamingUrl.endsWith("(format=m3u8-aapl-v3)"));
		assertEquals("1.0", pkgVersion);
	}

	/*
	 * Given: Publish WEBM Resource Content
	 * When: Content Publish API Hits
	 * Then: 200-OK, Content Should be in "Live" Status with all required metadata. ("streamingUrl" should be available after 30 min.)
	 *
	 */
	@Test
	public void publishVideoWEBMContentExpectStreamingUrlSuccess() {
		//Create Content
		String createVideoMP4ContentReq = "{\"request\":{\"content\":{\"name\":\"Resource Content for Video Streaming\",\"code\":\"test.resource.1\",\"mimeType\":\"video/webm\",\"contentType\":\"Resource\"}}}";
		String identifier = createContent(contentType, createVideoMP4ContentReq);

		//Upload Content
		uploadContent(identifier, "/small.webm");

		//publish Content
		publishContent(identifier, null, true);
		delay(1500000);

		// Get Content and Validate
		JsonPath jsonResponse = readContent(identifier);
		String status = jsonResponse.get("result.content.status");
		String prevState = jsonResponse.get("result.content.prevState");
		String downloadUrl = jsonResponse.get("result.content.downloadUrl");
		String artifactUrl = jsonResponse.get("result.content.artifactUrl");
		String streamingUrl = jsonResponse.get("result.content.streamingUrl");
		String lastPublishedOn = jsonResponse.get("result.content.lastPublishedOn");
		String pkgVersion = Float.toString(jsonResponse.get("result.content.pkgVersion"));
		assertEquals("Live", status);
		assertEquals("Draft", prevState);
		assertNotNull(downloadUrl);
		assertNotNull(artifactUrl);
		assertNotNull(lastPublishedOn);
		assertTrue(streamingUrl.endsWith("(format=m3u8-aapl-v3)"));
		assertEquals("1.0", pkgVersion);
	}

	/*
	 * Given: Publish MPEG Video Resource Content
	 * When: Content Publish API Hits
	 * Then: 200-OK, Content Should be in "Live" Status with all required metadata. ("streamingUrl" should be same as "artifactUrl")
	 *
	 */
	@Test
	public void publishVideoMPEGContentExpectStreamingUrlSuccess() {
		//Create Content
		String createVideoMP4ContentReq = "{\"request\":{\"content\":{\"name\":\"Resource Content for Video Streaming Test\",\"code\":\"test.resource.1\",\"mimeType\":\"video/mpeg\",\"contentType\":\"Resource\"}}}";
		String identifier = createContent(contentType, createVideoMP4ContentReq);

		//Upload Content
		uploadContent(identifier, "/delta.mpg");

		//publish Content
		publishContent(identifier, null, true);
		delay(15000);

		// Get Content and Validate
		JsonPath jsonResponse = readContent(identifier);
		String status = jsonResponse.get("result.content.status");
		String prevState = jsonResponse.get("result.content.prevState");
		String downloadUrl = jsonResponse.get("result.content.downloadUrl");
		String artifactUrl = jsonResponse.get("result.content.artifactUrl");
		String streamingUrl = jsonResponse.get("result.content.streamingUrl");
		String lastPublishedOn = jsonResponse.get("result.content.lastPublishedOn");
		String pkgVersion = Float.toString(jsonResponse.get("result.content.pkgVersion"));
		assertEquals(artifactUrl, streamingUrl);
		assertEquals("Live", status);
		assertEquals("Draft", prevState);
		assertNotNull(downloadUrl);
		assertNotNull(artifactUrl);
		assertNotNull(lastPublishedOn);
		assertEquals("1.0", pkgVersion);
	}

	@Test
	public void hierarchyJsonCreationSuccess() {
		String ecarName = "test_bundle";
		String downloadPath = "/data/testBundle/";
		String ecarPath = downloadPath + ecarName + ".zip";
		int rn = generateRandomInt(0, 999999);
		//Create Content
		String createResourceContentReq = "{\"request\":{\"content\":{\"name\":\"LP_FT_" + rn + "\",\"code\":\"test.res.1\",\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\"}}}";
		String assetId = createContent(contentType, createResourceContentReq);

		// Upload Content
		uploadContent(assetId, "/pdf.pdf");
		delay(15000);

		//publish content
		publishContent(assetId, null, false);
		delay(15000);

		//Create TextBook
		String createTextBook = "{\"request\":{\"content\":{\"osId\":\"org.sunbird.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Text Book in English for Class III\",\"name\":\"Marigold\",\"language\":[\"English\"],\"contentType\":\"TextBook\",\"code\":\"org.sunbird.feb16.story.test01\",\"tags\":[\"QA_Content\"],\"mimeType\":\"application/vnd.ekstep.content-collection\",\"children\":[]}}}";
		String identifier = createContent(contentType, createTextBook);

		//Update Hierarchy
		String updateTextBookHierarchy = "{\"request\":{\"data\":{\"nodesModified\":{\"" + identifier + "\":{\"isNew\":true,\"root\":true,\"reservedDialcodes\":{\"ZDYAKA\":0,\"DAFKJN\":1,\"ZNLDAJ\":2},\"metadata\":{\"name\":\"YO\"}},\"textbookunit_0\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U1\",\"code\":\"testbook 0\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbookunit_1\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U2\",\"code\":\"testbook 1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbookunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3\",\"code\":\"testbook 2\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_0\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U1.1\",\"code\":\"testbook 1.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_1\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U2.1\",\"code\":\"testbook 2.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3.1\",\"code\":\"testbook 3.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubsubunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3.1.1\",\"code\":\"testbook 3.1.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}}},\"hierarchy\":{\""+identifier+"\":{\"name\":\"Test Undefined\",\"contentType\":\"TextBook\",\"children\":[\"textbookunit_0\",\"textbookunit_1\",\"textbookunit_2\"],\"root\":true},\"textbookunit_0\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_0\"],\"root\":false},\"textbookunit_1\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_1\"],\"root\":false},\"textbookunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_2\"],\"root\":false},\"textbooksubunit_0\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false},\"textbooksubunit_1\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false},\"textbooksubunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubsubunit_2\"],\"root\":false},\"textbooksubsubunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\""+assetId+"\",\""+assetId+"\"],\"root\":false}},\"lastUpdatedBy\":\"pradyumna\"}}}";
		updateHierarchy(contentType,updateTextBookHierarchy);
		//Publish TextBook
		publishContent(identifier, null, false);
		delay(15000);

		JsonPath response = readContent(identifier);
		String downloadUrl = response.get("result.content.downloadUrl");
		assertNotNull(response.get("result.content.totalCompressedSize"));

		try {
			// Download Ecar
			FileUtils.copyURLToFile(new URL(downloadUrl), new File(downloadPath + ecarName + ".zip"));

			// Extract Ecar
			File bundleExtract = new File(downloadPath + ecarName);
			String bundleExtractPath = bundleExtract.getPath();

			ZipFile bundleZip = new ZipFile(ecarPath);
			bundleZip.extractAll(bundleExtractPath);

			File fileName = new File(bundleExtractPath);
			File[] files = fileName.listFiles();
			List<File> fileList = Arrays.asList(files);
			long fileNo = fileList.stream().filter(file -> file.getName().equalsIgnoreCase("hierarchy.json")).count();
			assertEquals(1, fileNo);
			FileUtils.deleteDirectory(new File(downloadPath));
		}catch (Exception e) {
			e.printStackTrace();
			try {
				FileUtils.deleteDirectory(new File(downloadPath));
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}
	}

	@Test
	public void testTextbookRePublishWithRetiredResourceSuccess() {
		String ecarName = "test_bundle";
		String downloadPath = "/data/testBundle/";
		String ecarPath = downloadPath + ecarName + ".zip";
		int rn = generateRandomInt(0, 999999);
		//Create Content
		String createResourceContentReq = "{\"request\":{\"content\":{\"name\":\"LP_FT_" + rn + "\",\"code\":\"test.res.1\",\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\"}}}";
		String assetId = createContent(contentType, createResourceContentReq);

		// Upload Content
		uploadContent(assetId, "/pdf.pdf");
		delay(15000);

		//publish content
		publishContent(assetId, null, false);
		delay(15000);

		//Create TextBook
		String createTextBook = "{\"request\":{\"content\":{\"osId\":\"org.sunbird.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Text Book in English for Class III\",\"name\":\"Marigold\",\"language\":[\"English\"],\"contentType\":\"TextBook\",\"code\":\"org.sunbird.feb16.story.test01\",\"tags\":[\"QA_Content\"],\"mimeType\":\"application/vnd.ekstep.content-collection\",\"children\":[]}}}";
		String identifier = createContent(contentType, createTextBook);

		//Update Hierarchy
		String updateTextBookHierarchy = "{\"request\":{\"data\":{\"nodesModified\":{\"" + identifier + "\":{\"isNew\":true,\"root\":true,\"reservedDialcodes\":{\"ZDYAKA\":0,\"DAFKJN\":1,\"ZNLDAJ\":2},\"metadata\":{\"name\":\"YO\"}},\"textbookunit_0\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U1\",\"code\":\"testbook 0\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbookunit_1\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U2\",\"code\":\"testbook 1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbookunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3\",\"code\":\"testbook 2\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_0\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U1.1\",\"code\":\"testbook 1.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_1\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U2.1\",\"code\":\"testbook 2.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3.1\",\"code\":\"testbook 3.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubsubunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3.1.1\",\"code\":\"testbook 3.1.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}}},\"hierarchy\":{\""+identifier+"\":{\"name\":\"Test Undefined\",\"contentType\":\"TextBook\",\"children\":[\"textbookunit_0\",\"textbookunit_1\",\"textbookunit_2\"],\"root\":true},\"textbookunit_0\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_0\"],\"root\":false},\"textbookunit_1\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_1\"],\"root\":false},\"textbookunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_2\"],\"root\":false},\"textbooksubunit_0\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false},\"textbooksubunit_1\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false},\"textbooksubunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubsubunit_2\"],\"root\":false},\"textbooksubsubunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\""+assetId+"\",\""+assetId+"\"],\"root\":false}},\"lastUpdatedBy\":\"pradyumna\"}}}";
		updateHierarchy(contentType,updateTextBookHierarchy);
		//Publish TextBook
		publishContent(identifier, null, false);
		delay(15000);

		JsonPath response = readContent(identifier);
		String downloadUrl = response.get("result.content.downloadUrl");
		assertNotNull(response.get("result.content.totalCompressedSize"));

		try {
			// Download Ecar
			FileUtils.copyURLToFile(new URL(downloadUrl), new File(downloadPath + ecarName + ".zip"));

			// Extract Ecar
			File bundleExtract = new File(downloadPath + ecarName);
			String bundleExtractPath = bundleExtract.getPath();

			ZipFile bundleZip = new ZipFile(ecarPath);
			bundleZip.extractAll(bundleExtractPath);

			File fileName = new File(bundleExtractPath);
			File[] files = fileName.listFiles();
			List<File> fileList = Arrays.asList(files);
			long fileNo = fileList.stream().filter(file -> file.getName().equalsIgnoreCase("hierarchy.json")).count();
			assertEquals(1, fileNo);
			FileUtils.deleteDirectory(new File(downloadPath));
		}catch (Exception e) {
			e.printStackTrace();
			try {
				FileUtils.deleteDirectory(new File(downloadPath));
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}

		retireContent(assetId);
		//Publish TextBook
		publishContent(identifier, null, false);
		delay(15000);

		JsonPath rePublishedResponse = getHierarchy(identifier);
		String rePublishedDownloadUrl = rePublishedResponse.get("result.content.downloadUrl");
		assertNotNull(rePublishedResponse.get("result.content.totalCompressedSize"));
		try {
			assertFalse(mapper.writeValueAsString(rePublishedResponse).contains(assetId));
			// Download Ecar
			FileUtils.copyURLToFile(new URL(rePublishedDownloadUrl), new File(downloadPath + ecarName + ".zip"));

			// Extract Ecar
			File bundleExtract = new File(downloadPath + ecarName);
			String bundleExtractPath = bundleExtract.getPath();

			ZipFile bundleZip = new ZipFile(ecarPath);
			bundleZip.extractAll(bundleExtractPath);

			File fileName = new File(bundleExtractPath);
			File[] files = fileName.listFiles();
			List<File> fileList = Arrays.asList(files);
			long fileNo = fileList.stream().filter(file -> file.getName().equalsIgnoreCase("hierarchy.json")).count();
			assertEquals(1, fileNo);
			FileUtils.deleteDirectory(new File(downloadPath));
		}catch (Exception e) {
			e.printStackTrace();
			try {
				FileUtils.deleteDirectory(new File(downloadPath));
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}
	}

	@Test
	public void hierarchyJsonSuccessWithNoCreation() {
		String ecarName = "test_bundle";
		String downloadPath = "/data/testBundle/";
		String ecarPath = downloadPath + ecarName + ".zip";
		//Create Asset Content
		int rn = generateRandomInt(0, 999999);

		//Create Content
		String createResourceContentReq = "{\"request\":{\"content\":{\"name\":\"LP_FT_" + rn + "\",\"code\":\"test.res.1\",\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\"}}}";
		String identifier = createContent(contentType, createResourceContentReq);

		// Upload Content
		uploadContent(identifier, "/pdf.pdf");

		//publish content
		publishContent(identifier, null, false);
		delay(15000);

		// Get Content and Validate
		JsonPath response = readContent(identifier);
		String downloadUrl = response.get("result.content.downloadUrl");
		// TODO:Add Total compressed size to other mime types
//		assertNotNull(response.get("result.content.totalCompressedSize"));

		assertNull(response.get("result.content.totalCompressedSize"));
		try {
			// Download Ecar
			FileUtils.copyURLToFile(new URL(downloadUrl), new File(downloadPath + ecarName + ".zip"));

			// Extract Ecar
			File bundleExtract = new File(downloadPath + ecarName);
			String bundleExtractPath = bundleExtract.getPath();

			ZipFile bundleZip = new ZipFile(ecarPath);
			bundleZip.extractAll(bundleExtractPath);

			File fileName = new File(bundleExtractPath);
			File[] files = fileName.listFiles();
			List<File> fileList = Arrays.asList(files);
			long fileNo = fileList.stream().filter(file -> file.getName().equalsIgnoreCase("hierarchy.json")).count();
			assertEquals(0, fileNo);
			FileUtils.deleteDirectory(new File(downloadPath));
		}catch (Exception e) {
			e.printStackTrace();
			try {
				FileUtils.deleteDirectory(new File(downloadPath));
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}
	}
	/**
	 *
	 * @param contentType
	 * @param request
	 * @return
	 */
	private String createContent(String contentType, String request) {
		String identifier;
		setURI();
		Response response =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(request).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().//log().all().
				extract().
				response();
		JsonPath jsonResponse = response.jsonPath();
		identifier = jsonResponse.get("result.node_id");
		return identifier;
	}

	/**
	 *
	 * @param identifier
	 * @param filePath
	 * @return
	 */
	private String uploadContent(String identifier, String filePath) {
		String contentUrl;
		setURI();
		Response response =
				given().
				spec(getRequestSpecification(uploadContentType, userId, APIToken)).
				multiPart(new File(path + filePath)).
				when().
				post("/content/v3/upload/" + identifier)
				.then().
				extract().response();
		JsonPath jsonResponse = response.jsonPath();
		contentUrl = jsonResponse.get("result.content_url");
		return contentUrl;
	}



	/**
	 * @param contentId
	 * @param request
	 * @param isAuthRequired
	 */
	private void publishContent(String contentId, String request, Boolean isAuthRequired) {
		String publishContentReq = request;
		if (StringUtils.isBlank(request))
			publishContentReq = "{\"request\": {\"content\": {\"publisher\": \"EkStep\",\"lastPublishedBy\": \"EkStep\",\"publishChecklist\":[\"GoodQuality\",\"CorrectConcept\"],\"publishComment\":\"OK\"}}}";
		RequestSpecification spec = getRequestSpec(isAuthRequired);
		setURI();
		given().
		spec(spec).
		body(publishContentReq).
		with().
		contentType(JSON).
		when().
		post("content/v3/publish/" + contentId).
		then().log().all().
		spec(get200ResponseSpec());
	}

	/**
	 *
	 * @param contentId
	 * @return
	 */
	private JsonPath readContent(String contentId) {
		JsonPath resp;
		setURI();
		Response response =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + contentId).
				then().
				spec(get200ResponseSpec()).
				extract().response();

		resp = response.jsonPath();
		return resp;
	}

	private boolean validateEcarManifestMetadata(String url, String... metadata) {
		boolean isValidEcar = false;
		String ecarName = "test_bundle";
		String downloadPath = "/data/testBundle/";
		String ecarPath = downloadPath + ecarName + ".zip";
		File[] files;
		try {
			// Download Ecar
			FileUtils.copyURLToFile(new URL(url), new File(downloadPath + ecarName + ".zip"));

			// Extract Ecar
			File bundleExtract = new File(downloadPath + ecarName);
			String bundleExtractPath = bundleExtract.getPath();

			ZipFile bundleZip = new ZipFile(ecarPath);
			bundleZip.extractAll(bundleExtractPath);

			File fileName = new File(bundleExtractPath);
			files = fileName.listFiles();

			for (File file : files) {
				if (file.isFile() && file.getName().endsWith("json")) {
					Map<String, Object> manifestMap = mapper.readValue(file, new TypeReference<Map<String, Object>>() {
					});
					Map<String, Object> archive = (Map<String, Object>) manifestMap.get("archive");
					List<Map<String, Object>> items = (List<Map<String, Object>>) archive.get("items");
					Map<String, Object> props = items.get(0);
					for (String prop : metadata) {
						isValidEcar = StringUtils.isNotBlank((String) props.get(prop)) ? true : false;
					}
				}
			}
			FileUtils.deleteDirectory(new File(downloadPath));
		} catch (Exception e) {
			e.printStackTrace();
			try {
				FileUtils.deleteDirectory(new File(downloadPath));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return isValidEcar;
	}

	@SuppressWarnings("unchecked")
	private boolean validateEcarForPosterImage(String url) {
		boolean isValidEcar = false;
		String ecarName = "test_bundle";
		String downloadPath = "/data/testBundle/";
		String ecarPath = downloadPath + ecarName + ".zip";
		File[] files;
		try {
			// Download Ecar
			FileUtils.copyURLToFile(new URL(url), new File(downloadPath + ecarName + ".zip"));

			// Extract Ecar
			File bundleExtract = new File(downloadPath + ecarName);
			String bundleExtractPath = bundleExtract.getPath();

			ZipFile bundleZip = new ZipFile(ecarPath);
			bundleZip.extractAll(bundleExtractPath);

			File fileName = new File(bundleExtractPath);
			files = fileName.listFiles();

			for (File file : files) {
				if (file.isFile() && file.getName().endsWith("json")) {
					Map<String, Object> manifestMap = mapper.readValue(file, new TypeReference<Map<String, Object>>() {
					});
					Map<String, Object> archive = (Map<String, Object>) manifestMap.get("archive");
					List<Map<String, Object>> items = (List<Map<String, Object>>) archive.get("items");
					Map<String, Object> props = items.get(0);
					String appIcon = (String) props.get("appIcon");
					String posterImage = (String) props.get("posterImage");

					if (null != appIcon && new File(bundleExtractPath + "/" + appIcon).exists() && null == posterImage)
						isValidEcar = true;
				}
			}
			FileUtils.deleteDirectory(new File(downloadPath));
		} catch (Exception e) {
			e.printStackTrace();
			try {
				FileUtils.deleteDirectory(new File(downloadPath));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return isValidEcar;
	}

	private String updateHierarchy(String identifier, String request) {
		String responseCode;
		setURI();
		Response response =
				given().
						spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
						body(request).
						with().
						contentType(JSON).
						when().
						patch("content/v3/hierarchy/update").
						then().log().all().
						extract().
						response();
		JsonPath jsonResponse = response.jsonPath();
		return jsonResponse.get("responseCode");
	}

	private JsonPath retireContent(String contentId) {
		setURI();
		Response response = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				with().
				contentType(JSON).
				when().
				delete("content/v3/retire/" + contentId).
				then().log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath resp = response.jsonPath();
		return resp;
	}

	private JsonPath getHierarchy(String identifier) {
		setURI();
		Response response =
				given().
						spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
						with().
						contentType(JSON).
						when().
						get("content/v3/hierarchy/" + identifier).
						then().log().all().
						extract().
						response();
		JsonPath jsonResponse = response.jsonPath();
		return jsonResponse.get("responseCode");
	}

}
