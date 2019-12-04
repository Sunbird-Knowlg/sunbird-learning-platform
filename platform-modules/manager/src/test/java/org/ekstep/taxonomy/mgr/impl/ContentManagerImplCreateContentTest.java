package org.ekstep.taxonomy.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.ekstep.graph.service.common.GraphOperation;
import org.ekstep.graph.service.util.DriverUtil;
import org.ekstep.taxonomy.content.common.BaseTestUtil;
import org.ekstep.taxonomy.content.common.TestParams;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ContentManagerImplCreateContentTest extends GraphEngineTestSetup {

	static ContentManagerImpl contentManager = new ContentManagerImpl();

	static Map<String, Object> versionKeyMap = new HashMap<String, Object>();

	static ObjectMapper mapper = new ObjectMapper();

	static String createECMLContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Resource\",\"code\":\"test content\",\"mimeType\":\"application/vnd.ekstep.ecml-archive\"}";
	static String createHTMLContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Resource\",\"code\":\"test content\",\"mimeType\":\"application/vnd.ekstep.html-archive\"}";
	static String createAPKContent = "{\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Resource\",\"code\":\"test content\",\"mimeType\":\"application/vnd.android.package-archive\"}";
	static String createPluginContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Resource\",\"code\":\"test content\",\"mimeType\":\"application/vnd.ekstep.plugin-archive\"}";
	static String createYouTubeContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Resource\",\"code\":\"test content\",\"mimeType\":\"video/x-youtube\"}";
	static String createDocumentContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Resource\",\"code\":\"test content\",\"mimeType\":\"application/pdf\"}";
	static String createH5PContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Resource\",\"code\":\"test content\",\"mimeType\":\"application/vnd.ekstep.h5p-archive\"}";
	static String createDefaultContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Resource\",\"code\":\"test content\",\"mimeType\":\"video/mp4\"}";
	static String createCollectionContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Resource\",\"code\":\"test content\",\"mimeType\":\"application/vnd.ekstep.content-collection\", \"children\": [{ \"identifier\": \"id1\"}, { \"identifier\": \"id2\"}]}";
	static String createAssetContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Resource\",\"code\":\"test content\",\"mimeType\":\"image/jpeg\"}";
	static String updateContent = "{\"request\":{\"content\":{\"appIcon\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/89c173618416f8561f785bd076d2b73a_1475228619822.jpeg\"}}}";
	static String requestForReview = "{\"request\":{\"content\":{\"lastPublishedBy\":\"Ekstep\"}}}";
	static String taxonomyId = "domain";
	static String channelId = "in.ekstep";

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void beforeClass() throws Exception {
		loadDefinition("definitions/content_definition.json", "definitions/concept_definition.json",
				"definitions/dimension_definition.json", "definitions/domain_definition.json");
		seedContent();
	}

	@AfterClass
	public static void afterClass() {

	}

	/*
	 * Create Content without body
	 */
	@Test
	public void testCreateContent_01() throws Exception {
		try {
			Map<String, Object> contentMap = mapper.readValue(createECMLContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response response = contentManager.create(contentMap, channelId);
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
	@Ignore
	@Test
	public void testCreateContent_02() {
		try {
			Map<String, Object> contentMap = mapper.readValue(createECMLContent,
					new TypeReference<Map<String, Object>>() {
					});
			contentMap.put(TestParams.body.name(), BaseTestUtil.getFileString("Sample_XML_1.ecml"));
			Response response = contentManager.create(contentMap, channelId);
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
			Response response = contentManager.create(contentMap, channelId);
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
			Response response = contentManager.create(contentMap, channelId);
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
	public void testCreateContent_10() {
		try {
			// Creating First Child
			Map<String, Object> firstChildMap = mapper.readValue(createECMLContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response firstChildCreationResponse = contentManager.create(firstChildMap, channelId);
			String firstChildNodeId = (String) firstChildCreationResponse.getResult().get(TestParams.node_id.name());

			// Creating Second Child
			Map<String, Object> secondChildMap = mapper.readValue(createHTMLContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response secondChildCreationResponse = contentManager.create(secondChildMap, channelId);
			String secondChildNodeId = (String) secondChildCreationResponse.getResult().get(TestParams.node_id.name());

			Map<String, Object> contentMap = mapper.readValue(
					createCollectionContent.replace("id1", firstChildNodeId).replace("id2", secondChildNodeId),
					new TypeReference<Map<String, Object>>() {
					});

			Response response = contentManager.create(contentMap, channelId);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Create Asset Content
	 */
	@Test
	public void testCreateContent_11() {
		try {
			Map<String, Object> contentMap = mapper.readValue(createAssetContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response response = contentManager.create(contentMap, channelId);
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
			Response response = contentManager.create(contentMap, channelId);
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
			Response response = contentManager.create(contentMap, channelId);
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
			Response response = contentManager.create(contentMap, channelId);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Create H5P Content
	 */
	@Test
	public void testCreateContent_08() {
		try {
			Map<String, Object> contentMap = mapper.readValue(createH5PContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response response = contentManager.create(contentMap, channelId);
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
	public void testCreateContent_09() {
		try {
			Map<String, Object> contentMap = mapper.readValue(createDocumentContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response response = contentManager.create(contentMap, channelId);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Update ECML Content
	 */
	@Test
	public void testUpdateContent_01() {
		try {
			String contentId = "U_ECML_01";
			Map<String, Object> contentMap = mapper.readValue(updateContent, new TypeReference<Map<String, Object>>() {
			});
			contentMap.put(TestParams.versionKey.name(), versionKeyMap.get(contentId));
			Response response = contentManager.update(contentId, contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
			Assert.assertFalse(
					StringUtils.equalsIgnoreCase(versionKey, (String) versionKeyMap.get(TestParams.versionKey.name())));
			if (StringUtils.isNotBlank(versionKey))
				versionKeyMap.put(contentId, versionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Update HTML Content
	 */
	@Test
	public void testUpdateContent_02() {
		try {
			String contentId = "U_HTML_01";
			Map<String, Object> contentMap = mapper.readValue(updateContent, new TypeReference<Map<String, Object>>() {
			});
			contentMap.put(TestParams.versionKey.name(), versionKeyMap.get(contentId));
			Response response = contentManager.update(contentId, contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
			Assert.assertFalse(
					StringUtils.equalsIgnoreCase(versionKey, (String) versionKeyMap.get(TestParams.versionKey.name())));
			if (StringUtils.isNotBlank(versionKey))
				versionKeyMap.put(contentId, versionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Update APK Content
	 */
	@Test
	public void testUpdateContent_03() {
		try {
			String contentId = "U_APK_01";
			Map<String, Object> contentMap = mapper.readValue(updateContent, new TypeReference<Map<String, Object>>() {
			});
			contentMap.put(TestParams.versionKey.name(), versionKeyMap.get(contentId));
			Response response = contentManager.update(contentId, contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
			Assert.assertFalse(
					StringUtils.equalsIgnoreCase(versionKey, (String) versionKeyMap.get(TestParams.versionKey.name())));
			if (StringUtils.isNotBlank(versionKey))
				versionKeyMap.put(contentId, versionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Update Collection Content
	 */
	@Ignore
	@Test
	public void testUpdateContent_04() {
		try {
			String contentId = "U_Collection_01";
			Map<String, Object> contentMap = mapper.readValue(updateContent, new TypeReference<Map<String, Object>>() {
			});
			contentMap.put(TestParams.versionKey.name(), versionKeyMap.get(contentId));
			Response response = contentManager.update(contentId, contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
			Assert.assertFalse(
					StringUtils.equalsIgnoreCase(versionKey, (String) versionKeyMap.get(TestParams.versionKey.name())));
			if (StringUtils.isNotBlank(versionKey))
				versionKeyMap.put(contentId, versionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Update Asset Content
	 */
	@Test
	public void testUpdateContent_05() {
		try {
			String contentId = "U_Asset_01";
			Map<String, Object> contentMap = mapper.readValue(updateContent, new TypeReference<Map<String, Object>>() {
			});
			contentMap.put(TestParams.versionKey.name(), versionKeyMap.get(contentId));
			Response response = contentManager.update(contentId, contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
			Assert.assertFalse(
					StringUtils.equalsIgnoreCase(versionKey, (String) versionKeyMap.get(TestParams.versionKey.name())));
			if (StringUtils.isNotBlank(versionKey))
				versionKeyMap.put(contentId, versionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Update Plugin Content
	 */
	@Test
	public void testUpdateContent_06() {
		try {
			String contentId = "U_Plugin_01";
			Map<String, Object> contentMap = mapper.readValue(updateContent, new TypeReference<Map<String, Object>>() {
			});
			contentMap.put(TestParams.versionKey.name(), versionKeyMap.get(contentId));
			Response response = contentManager.update(contentId, contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
			Assert.assertFalse(
					StringUtils.equalsIgnoreCase(versionKey, (String) versionKeyMap.get(TestParams.versionKey.name())));
			if (StringUtils.isNotBlank(versionKey))
				versionKeyMap.put(contentId, versionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Update YouTube Content
	 */
	@Test
	public void testUpdateContent_07() {
		try {
			String contentId = "U_YouTube_01";
			Map<String, Object> contentMap = mapper.readValue(updateContent, new TypeReference<Map<String, Object>>() {
			});
			contentMap.put(TestParams.versionKey.name(), versionKeyMap.get(contentId));
			Response response = contentManager.update(contentId, contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
			Assert.assertFalse(
					StringUtils.equalsIgnoreCase(versionKey, (String) versionKeyMap.get(TestParams.versionKey.name())));
			if (StringUtils.isNotBlank(versionKey))
				versionKeyMap.put(contentId, versionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Update Document Content
	 */
	@Test
	public void testUpdateContent_08() {
		try {
			String contentId = "U_Document_01";
			Map<String, Object> contentMap = mapper.readValue(updateContent, new TypeReference<Map<String, Object>>() {
			});
			contentMap.put(TestParams.versionKey.name(), versionKeyMap.get(contentId));
			Response response = contentManager.update(contentId, contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
			Assert.assertFalse(
					StringUtils.equalsIgnoreCase(versionKey, (String) versionKeyMap.get(TestParams.versionKey.name())));
			if (StringUtils.isNotBlank(versionKey))
				versionKeyMap.put(contentId, versionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Update Default (video/mp4) Content
	 */
	@Test
	public void testUpdateContent_09() {
		try {
			String contentId = "U_Default_01";
			Map<String, Object> contentMap = mapper.readValue(updateContent, new TypeReference<Map<String, Object>>() {
			});
			contentMap.put(TestParams.versionKey.name(), versionKeyMap.get(contentId));
			Response response = contentManager.update(contentId, contentMap);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
			Assert.assertFalse(
					StringUtils.equalsIgnoreCase(versionKey, (String) versionKeyMap.get(TestParams.versionKey.name())));
			if (StringUtils.isNotBlank(versionKey))
				versionKeyMap.put(contentId, versionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Upload ECML Content
	 */
	@Ignore
	@Test
	public void testUploadContent_01() {
		try {
			String contentId = "U_ECML_01";
			String mimeType = "application/vnd.ekstep.ecml-archive";
			File file = new File("");
			Response response = contentManager.upload(contentId, file, mimeType);
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
			Assert.assertFalse(
					StringUtils.equalsIgnoreCase(versionKey, (String) versionKeyMap.get(TestParams.versionKey.name())));
			if (StringUtils.isNotBlank(versionKey))
				versionKeyMap.put(contentId, versionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create Content without contentDisposition and contentEncoding should take
	 * default values.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateContentWithDefaultEncodingAndDisposition() throws Exception {
		String contentId = "CONTENT_CREATE_TEST_1";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("identifier", contentId);
		map.put("osId", "org.ekstep.quiz.app");
		map.put("mediaType", "content");
		map.put("visibility", "Default");
		map.put("description", "Books for learning about colours, animals, fruits, vegetables, shapes");
		map.put("name", "Learning Books");
		List<String> languages = new ArrayList<String>();
		languages.add("English");
		map.put("language", languages);
		map.put("contentType", "Resource");
		map.put("code", "org.ekstep.feb03.story.learningbooks");
		map.put("mimeType", "application/vnd.ekstep.ecml-archive");
		Response response = contentManager.create(map, channelId);
		Assert.assertTrue(StringUtils.equalsIgnoreCase("OK", response.getResponseCode().name()));
		Map<String, Object> metadata = getMetadata(contentId);
		Assert.assertTrue(StringUtils.equalsIgnoreCase("gzip", (String) metadata.get("contentEncoding")));
		Assert.assertTrue(StringUtils.equalsIgnoreCase("inline", (String) metadata.get("contentDisposition")));
	}

	@Test
	public void testCreateContentWithKeywords() throws Exception {
		String contentId = "CONTENT_CREATE_TEST_2";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("identifier", contentId);
		map.put("osId", "org.ekstep.quiz.app");
		map.put("mediaType", "content");
		map.put("visibility", "Default");
		map.put("description", "Books for learning about colours, animals, fruits, vegetables, shapes");
		map.put("name", "Learning Books");
		List<String> languages = new ArrayList<String>();
		languages.add("English");
		map.put("language", languages);
		map.put("contentType", "Resource");
		map.put("code", "org.ekstep.feb03.story.learningbooks");
		map.put("mimeType", "application/vnd.ekstep.ecml-archive");
		map.put("keywords", Arrays.asList("colors", "animals"));
		Response response = contentManager.create(map, channelId);
		Assert.assertTrue(StringUtils.equalsIgnoreCase("OK", response.getResponseCode().name()));
		Map<String, Object> metadata = getMetadata(contentId);
		Assert.assertTrue(metadata.containsKey("keywords"));
	}

	private static void seedContent() throws Exception {
		try {
			// Create ECML Content
			Map<String, Object> ecmlContentMap = mapper.readValue(createECMLContent,
					new TypeReference<Map<String, Object>>() {
					});
			ecmlContentMap.put(TestParams.identifier.name(), "U_ECML_01");
			Response ecmlResponse = contentManager.create(ecmlContentMap, channelId);
			String ecmlVersionKey = (String) ecmlResponse.getResult().get(TestParams.versionKey.name());
			if (StringUtils.isNotBlank(ecmlVersionKey))
				versionKeyMap.put("U_ECML_01", ecmlVersionKey);

			// Create HTML Content
			Map<String, Object> htmlContentMap = mapper.readValue(createHTMLContent,
					new TypeReference<Map<String, Object>>() {
					});
			htmlContentMap.put(TestParams.identifier.name(), "U_HTML_01");
			Response htmlResponse = contentManager.create(htmlContentMap, channelId);
			String htmlVersionKey = (String) htmlResponse.getResult().get(TestParams.versionKey.name());
			if (StringUtils.isNotBlank(htmlVersionKey))
				versionKeyMap.put("U_HTML_01", htmlVersionKey);

			// Create APK Content
			Map<String, Object> apkContentMap = mapper.readValue(createAPKContent,
					new TypeReference<Map<String, Object>>() {
					});
			apkContentMap.put(TestParams.identifier.name(), "U_APK_01");
			Response apkResponse = contentManager.create(apkContentMap, channelId);
			String apkVersionKey = (String) apkResponse.getResult().get(TestParams.versionKey.name());
			if (StringUtils.isNotBlank(apkVersionKey))
				versionKeyMap.put("U_APK_01", apkVersionKey);

			// Create Collection Content
			Map<String, Object> collectionContentMap = mapper.readValue(
					createCollectionContent.replace("id1", "U_ECML_01").replace("id2", "U_HTML_01"),
					new TypeReference<Map<String, Object>>() {
					});
			collectionContentMap.put(TestParams.identifier.name(), "U_Collection_01");
			Response collectionResponse = contentManager.create(collectionContentMap, channelId);
			String collectionVersionKey = (String) collectionResponse.getResult().get(TestParams.versionKey.name());
			if (StringUtils.isNotBlank(collectionVersionKey))
				versionKeyMap.put("U_Collection_01", collectionVersionKey);

			// Create Asset Content
			Map<String, Object> assetContentMap = mapper.readValue(createAssetContent,
					new TypeReference<Map<String, Object>>() {
					});
			assetContentMap.put(TestParams.identifier.name(), "U_Asset_01");
			Response assetResponse = contentManager.create(assetContentMap, channelId);
			String assetVersionKey = (String) assetResponse.getResult().get(TestParams.versionKey.name());
			if (StringUtils.isNotBlank(assetVersionKey))
				versionKeyMap.put("U_Asset_01", assetVersionKey);

			// Create Plugin Content
			Map<String, Object> pluginContentMap = mapper.readValue(createPluginContent,
					new TypeReference<Map<String, Object>>() {
					});
			pluginContentMap.put(TestParams.identifier.name(), "U_Plugin_01");
			pluginContentMap.put(TestParams.code.name(), "U_Plugin_01");
			Response pluginResponse = contentManager.create(pluginContentMap, channelId);
			String pluginVersionKey = (String) pluginResponse.getResult().get(TestParams.versionKey.name());
			if (StringUtils.isNotBlank(pluginVersionKey))
				versionKeyMap.put("U_Plugin_01", pluginVersionKey);

			// Create YouTube Content
			Map<String, Object> youtubeContentMap = mapper.readValue(createYouTubeContent,
					new TypeReference<Map<String, Object>>() {
					});
			youtubeContentMap.put(TestParams.identifier.name(), "U_YouTube_01");
			Response youtubeResponse = contentManager.create(youtubeContentMap, channelId);
			String youtubeVersionKey = (String) youtubeResponse.getResult().get(TestParams.versionKey.name());
			if (StringUtils.isNotBlank(youtubeVersionKey))
				versionKeyMap.put("U_YouTube_01", youtubeVersionKey);

			// Create Document Content
			Map<String, Object> documentContentMap = mapper.readValue(createDocumentContent,
					new TypeReference<Map<String, Object>>() {
					});
			documentContentMap.put(TestParams.identifier.name(), "U_Document_01");
			Response documentResponse = contentManager.create(documentContentMap, channelId);
			String documentVersionKey = (String) documentResponse.getResult().get(TestParams.versionKey.name());
			if (StringUtils.isNotBlank(documentVersionKey))
				versionKeyMap.put("U_Document_01", documentVersionKey);

			// Create H5P Content
			Map<String, Object> h5pContentMap = mapper.readValue(createH5PContent,
					new TypeReference<Map<String, Object>>() {
					});
			h5pContentMap.put(TestParams.identifier.name(), "U_H5P_01");
			Response h5pResponse = contentManager.create(h5pContentMap, channelId);
			String h5pVersionKey = (String) h5pResponse.getResult().get(TestParams.versionKey.name());
			if (StringUtils.isNotBlank(h5pVersionKey))
				versionKeyMap.put("U_H5P_01", h5pVersionKey);

			// Create Default Content
			Map<String, Object> defaultContentMap = mapper.readValue(createDefaultContent,
					new TypeReference<Map<String, Object>>() {
					});
			defaultContentMap.put(TestParams.identifier.name(), "U_Default_01");
			Response defaultResponse = contentManager.create(defaultContentMap, channelId);
			String defaultVersionKey = (String) defaultResponse.getResult().get(TestParams.versionKey.name());
			if (StringUtils.isNotBlank(defaultVersionKey))
				versionKeyMap.put("U_Default_01", defaultVersionKey);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private Map<String, Object> getMetadata(String contentId) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Driver driver = DriverUtil.getDriver("domain", GraphOperation.READ);

		try (Session session = driver.session()) {
			String QUERY = "MATCH (n:domain) WHERE n.IL_UNIQUE_ID = { IL_UNIQUE_ID } RETURN n";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("IL_UNIQUE_ID", contentId);
			StatementResult result = session.run(QUERY, params);
			List<Record> records = result.list();
			if (null != result) {
				if (null != records && records.size() > 0) {
					Record record = records.get(0);
					org.neo4j.driver.v1.types.Node neo4JNode = record.get("n").asNode();
					returnMap.putAll(neo4JNode.asMap());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnMap;

	}
}
