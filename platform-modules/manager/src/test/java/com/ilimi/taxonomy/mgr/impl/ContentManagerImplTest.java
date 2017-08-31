package com.ilimi.taxonomy.mgr.impl;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.content.common.BaseTestUtil;
import com.ilimi.taxonomy.content.common.TestParams;
import com.ilimi.taxonomy.content.common.TestSetup;

public class ContentManagerImplTest extends TestSetup {

	ContentManagerImpl contentManager = new ContentManagerImpl();

	ObjectMapper mapper = new ObjectMapper();

	String createECMLContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"application/vnd.ekstep.ecml-archive\"}";
	String createHTMLContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"application/vnd.ekstep.html-archive\"}";
	String createAPKContent = "{\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"application/vnd.android.package-archive\"}";
	String createPluginContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"application/vnd.ekstep.plugin-archive\"}";
	String createYouTubeContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"video/x-youtube\"}";
	String createDocumentContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"application/pdf\"}";
	String createH5PContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"application/vnd.ekstep.h5p-archive\"}";
	String createDefaultContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"video/mp4\"}";
	String createCollectionContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"application/vnd.ekstep.content-collection\", \"children\": [{ \"identifier\": \"id1\"}, { \"identifier\": \"id2\"}]}";
	String createAssetContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"image/jpeg\", \"children\": [{ \"identifier\": \"id1\"}, { \"identifier\": \"id2\"}]}";
	String requestForReview = "{\"request\":{\"content\":{\"lastPublishedBy\":\"Ekstep\"}}}";

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void beforeSetupTestSuit() {
		System.out.println("ContentManagerImplTest -- Before");

	}

	@AfterClass
	public static void afterCleanData() {
		System.out.println("ContentManagerImplTest -- After");
	}

	/*
	 * Create Content without body
	 */
	@Test
	public void testCreateContent_01() {
		try {
			Map<String, Object> contentMap = mapper.readValue(createECMLContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response response = contentManager.createContent(contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Create Content with body
	 */
	@Test
	public void testCreateContent_02() {
		try {
			Map<String, Object> contentMap = mapper.readValue(createECMLContent,
					new TypeReference<Map<String, Object>>() {
					});
			contentMap.put(TestParams.body.name(), BaseTestUtil.getFileString("Sample_XML_1.ecml"));
			Response response = contentManager.createContent(contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Create HTML Content
	 */
	@Test
	public void testCreateContent_03() {
		try {
			Map<String, Object> contentMap = mapper.readValue(createHTMLContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response response = contentManager.createContent(contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Create APK Content
	 */
	@Test
	public void testCreateContent_04() {
		try {
			Map<String, Object> contentMap = mapper.readValue(createAPKContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response response = contentManager.createContent(contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Create Plugin Content
	 */
	@Test
	public void testCreateContent_05() {
		try {
			Map<String, Object> contentMap = mapper.readValue(createPluginContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response response = contentManager.createContent(contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Create YouTube Content
	 */
	@Test
	public void testCreateContent_06() {
		try {
			Map<String, Object> contentMap = mapper.readValue(createYouTubeContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response response = contentManager.createContent(contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Create Document Content
	 */
	@Test
	public void testCreateContent_07() {
		try {
			Map<String, Object> contentMap = mapper.readValue(createDocumentContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response response = contentManager.createContent(contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Create Default (video/mp4) Content
	 */
	@Test
	public void testCreateContent_08() {
		try {
			Map<String, Object> contentMap = mapper.readValue(createDocumentContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response response = contentManager.createContent(contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Create Collection Content
	 */
	@Test
	public void testCreateContent_09() {
		try {
			// Creating First Child
			Map<String, Object> childrenMap1 = mapper.readValue(createECMLContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response resChildren1 = contentManager.createContent(childrenMap1);
			String childNode1 = (String) resChildren1.getResult().get(TestParams.node_id.name());

			// Creating Second Child
			Map<String, Object> childrenMap2 = mapper.readValue(createHTMLContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response resChildren2 = contentManager.createContent(childrenMap2);
			String childNode2 = (String) resChildren2.getResult().get(TestParams.node_id.name());

			Map<String, Object> contentMap = mapper.readValue(
					createCollectionContent.replace("id1", childNode1).replace("id2", childNode2),
					new TypeReference<Map<String, Object>>() {
					});
			
			Response response = contentManager.createContent(contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
