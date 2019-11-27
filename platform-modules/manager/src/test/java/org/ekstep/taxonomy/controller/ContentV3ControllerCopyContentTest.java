/**
 * 
 */
package org.ekstep.taxonomy.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.graph.engine.common.TestParams;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.taxonomy.mgr.impl.ContentManagerImpl;
import org.ekstep.test.common.CommonTestSetup;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author gauraw
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ContentV3ControllerCopyContentTest extends CommonTestSetup {

	@Autowired
	private WebApplicationContext context;

	private ResultActions actions;

	MockMvc mockMvc;

	private static final String basePath = "/content/v3/";
	private static ObjectMapper mapper = new ObjectMapper();
	private static ContentManagerImpl contentManager = new ContentManagerImpl();
	private static String contentId = "";
	private static String contentId2 = "";
	private static String versionKey = "";
	private static String versionKey2 = "";

	private static String script_1 = "CREATE KEYSPACE IF NOT EXISTS content_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static String script_2 = "CREATE TABLE IF NOT EXISTS content_store_test.content_data (content_id text, last_updated_on timestamp,body blob,oldBody blob,stageIcons blob,PRIMARY KEY (content_id));";
	private static String copyContentReq = "{\"request\": {\"content\":{\"name\" : \"CopyContent001\",\"createdBy\":\"testUser\",\"createdFor\": [\"Ekstep\"],\"organisation\": [\"ekstep\"],\"description\":\"copy content\",\"framework\":\"NCF\"}}}";
	private static String topic = Platform.config.getString("kafka.topics.instruction");
	private static String channelId = "in.ekstep";

	@BeforeClass
	public static void setup() throws Exception {
		loadDefinition("definitions/content_definition.json", "definitions/concept_definition.json",
				"definitions/dimension_definition.json", "definitions/domain_definition.json");
		executeScript(script_1, script_2);
		LearningRequestRouterPool.init();
		//startKafkaServer();
		//createTopic(topic);
		createDocumentContent();
	}

	@AfterClass
	public static void clean() {
		//tearKafkaServer();
	}

	@Before
	public void init() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	public void delay() {
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
		}
	}

	public static void createDocumentContent() throws Exception {
		for (int i = 1; i <= 2; i++) {
			String createDocumentContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Resource\",\"code\":\"testcontent\",\"mimeType\":\"application/pdf\",\"creator\": \"ContentCreator\", \"author\": \"ContentAuthor\", \"license\": \"Creative Commons Attribution (CC BY)\", \"copyright\": \"copyright - Content qoned by\", \"organisation\": [\"Content_Creator_Organisation\"], \"attributions\":[\"Content_Attributions1\", \"Content_Attributions2\"]}";
			Map<String, Object> documentContentMap = mapper.readValue(createDocumentContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response documentResponse = contentManager.create(documentContentMap, channelId);
			if (i == 1) {
				contentId = (String) documentResponse.getResult().get(TestParams.node_id.name());
				versionKey = (String) documentResponse.getResult().get(TestParams.versionKey.name());
			} else if (i == 2) {
				contentId2 = (String) documentResponse.getResult().get(TestParams.node_id.name());
				versionKey2 = (String) documentResponse.getResult().get(TestParams.versionKey.name());
			}
		}
	}

	private static Map<String, Object> uploadContent(String contentId) {
		String versionKey = "";
		String artifactUrl = "";
		Map<String, Object> map = new HashMap<String, Object>();
		String mimeType = "application/pdf";
		String fileUrl = "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/u_document_04/artifact/pdf.pdf";
		Response response = contentManager.upload(contentId, fileUrl, mimeType);
		String responseCode = (String) response.getResponseCode().toString();
		if ("OK".equalsIgnoreCase(responseCode)) {
			versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			artifactUrl = (String) response.getResult().get("content_url");
		}
		if (StringUtils.isNotBlank(versionKey) && StringUtils.isNotBlank(artifactUrl)) {
			map.put(TestParams.versionKey.name(), versionKey);
			map.put(TestParams.artifactUrl.name(), artifactUrl);
		}

		return map;
	}

	private static boolean publishContent(String contentId, boolean isUnlisted) {
		Map<String, Object> requestMap = new HashMap<String, Object>();
		requestMap.put("lastPublishedBy", "Ekstep");
		requestMap.put("publishChecklist", Arrays.asList("test"));
		if (isUnlisted)
			requestMap.put("publish_type", ContentWorkflowPipelineParams.Unlisted.name().toLowerCase());
		Response response = contentManager.publish(contentId, requestMap);
		String responseCode = (String) response.getResponseCode().toString();
		if ("OK".equalsIgnoreCase(responseCode)) {
			System.out.println("Content Published Successfully");
			return true;
		}
		return false;
	}

	/*
	 * Copy Content with Draft Status without artifactUrl
	 * 
	 */
	@SuppressWarnings("unchecked")
    @Ignore
	@Test
	public void copyContentTest_01() throws Exception {
		actions = mockMvc.perform(MockMvcRequestBuilders.get(basePath + "read/" + contentId + "?mode=edit")
				.contentType(MediaType.APPLICATION_JSON));
		Response readResp = getResponse(actions);
		String  createdOn = (String) ((Map<String, Object>) readResp.getResult().get("content")).get("createdOn");

		String reqPath = basePath + "copy/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.post(reqPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(copyContentReq));
		Response resp = getResponse(actions);
		String id = (String) ((Map<String, Object>) resp.getResult().get("node_id")).get(contentId);
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Assert.assertNotEquals(id, contentId);

		actions = mockMvc.perform(MockMvcRequestBuilders.get(basePath + "read/" + id + "?mode=edit").contentType
				(MediaType.APPLICATION_JSON));
		Response readCopyResp = getResponse(actions);
		String  createdOnCopy = (String) ((Map<String, Object>) readCopyResp.getResult().get("content")).get("createdOn");
        System.out.println("Original CreatedOn : " + createdOn);
        System.out.println("Copied  CreatedOn : " + createdOnCopy);
		Assert.assertNotEquals(createdOn, createdOnCopy);
		Assert.assertNotNull(((Map<String, Object>) readCopyResp.getResult().get("content")).get("originData"));
	}

	/*
	 * Copy Content with Invalid createdBy Value
	 * 
	 */
	@Test
	public void copyContentTest_02() throws Exception {
		String reqPath = basePath + "copy/" + contentId;
		String copyContentReq = "{\"request\": {\"content\":{\"name\" : \"CopyContent001\",\"createdBy\":\"\",\"createdFor\": [\"Ekstep\"],\"organisation\": [\"ekstep\"],\"description\":\"copy content\",\"framework\":\"NCF\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(reqPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(copyContentReq));
		Response resp = getResponse(actions);
		String errorCode = resp.getParams().getErr();
		/*Assert.assertEquals("ERR_INVALID_CREATEDBY", errorCode);*/
		Assert.assertEquals("ERR_INVALID_REQUEST", errorCode);
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Copy Content with Invalid createdFor Value
	 * 
	 */
	@Ignore
	@Test
	public void copyContentTest_03() throws Exception {
		String reqPath = basePath + "copy/" + contentId;
		String copyContentReq = "{\"request\": {\"content\":{\"name\" : \"CopyContent001\",\"createdBy\":\"\testUser\",\"createdFor\": [],\"organisation\": [\"ekstep\"],\"description\":\"copy content\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(reqPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(copyContentReq));
		System.out.println("Response::" + actions.andReturn().getResponse().getContentAsString());
		Response resp = getResponse(actions);
		String errorCode = resp.getParams().getErr();
		Assert.assertEquals("ERR_CONTENT_BLANK_OBJECT", errorCode);
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Copy Content with Invalid Organisation Value
	 * 
	 */
	@Ignore
	@Test
	public void copyContentTest_04() throws Exception {
		String reqPath = basePath + "copy/" + contentId;
		String copyContentReq = "{\"request\": {\"content\":{\"name\" : \"CopyContent001\",\"createdBy\":\"\testUser\",\"createdFor\": [\"Ekstep\"],\"organisation\": [],\"description\":\"copy content\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(reqPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(copyContentReq));
		Response resp = getResponse(actions);
		String errorCode = resp.getParams().getErr();
		Assert.assertEquals("ERR_CONTENT_BLANK_OBJECT", errorCode);
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Copy Content with Invalid Content Id
	 * 
	 */
	@Test
	public void copyContentTest_05() throws Exception {
		String reqPath = basePath + "copy/" + "do_ABC1111";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(reqPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(copyContentReq));
		Response resp = getResponse(actions);
		String errorCode = resp.getParams().getErr();
		Assert.assertEquals("ERR_CONTENT_NOT_FOUND", errorCode);
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());

	}

	/*
	 * Copy Content with Draft Status with artifactUrl
	 * 
	 */
	@SuppressWarnings("unchecked")
	//@Test
	public void copyContentTest_06() throws Exception {
		Map<String, Object> map = uploadContent(contentId);
		delay();
		// copy content
		String reqPath = basePath + "copy/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.post(reqPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(copyContentReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		String id = (String) ((Map<String, Object>) resp.getResult().get("node_id")).get(contentId);

		// Get Copied Content
		reqPath = basePath + "read/" + id;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(reqPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest"));
		resp = getResponse(actions);
		String artifactUrl = (String) ((Map<String, Object>) resp.getResult().get("content")).get("artifactUrl");
		Assert.assertNotNull(artifactUrl);
		Assert.assertNotEquals((String) map.get(TestParams.artifactUrl.name()), artifactUrl);
		Assert.assertEquals(true, artifactUrl.contains(id));
	}

	/*
	 * Copy Content with Live Status
	 * 
	 */
	@Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void copyContentTest_07() throws Exception {
		Map<String, Object> map = uploadContent(contentId);
		publishContent(contentId, false);
		delay();
		String reqPath = basePath + "copy/" + contentId;
		String copyContentReq = "{\"request\": {\"content\":{\"name\" : \"CopyContent001\",\"createdBy\":\"testUser\",\"createdFor\": [\"Ekstep\"],\"organisation\": [\"ekstep\"],\"description\":\"copy content\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(reqPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(copyContentReq));
		Response resp = getResponse(actions);
		String id = (String) ((Map<String, Object>) resp.getResult().get("node_id")).get(contentId);
		System.out.println("Response : " + actions.andReturn().getResponse().getContentAsString());
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Assert.assertNotEquals(id, contentId);
	}

	/*
	 * Copy Content with Unlisted Status
	 * 
	 */
	@Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void copyContentTest_08() throws Exception {
		Map<String, Object> map = uploadContent(contentId);
		publishContent(contentId, true);
		delay();
		String reqPath = basePath + "copy/" + contentId;
		String copyContentReq = "{\"request\": {\"content\":{\"name\" : \"CopyContent001\",\"createdBy\":\"testUser\",\"createdFor\": [\"Ekstep\"],\"organisation\": [\"ekstep\"],\"description\":\"copy content\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(reqPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(copyContentReq));
		Response resp = getResponse(actions);
		String id = (String) ((Map<String, Object>) resp.getResult().get("node_id")).get(contentId);
		System.out.println("Response : " + actions.andReturn().getResponse().getContentAsString());
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Assert.assertNotEquals(id, contentId);
	}

	/*
	 * Copy Asset Content
	 * 
	 */
	@SuppressWarnings("unchecked")
	// @Test
	public void copyContentTest_09() throws Exception {
		String reqPath = basePath + "create";
		String assetCreateReq = "{\"request\": {\"content\": {\"name\": \"Test Resource\",\"code\": \"test.asset\",\"contentType\":\"Asset\",\"mimeType\": \"image/jpeg\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(reqPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(assetCreateReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		String identifier = (String) resp.getResult().get("node_id");

		reqPath = basePath + "upload/" + identifier;
		File inputFile = getResourceFile("fly.png");
		FileInputStream fileStream = new FileInputStream(inputFile);
		MockMultipartFile testFile = new MockMultipartFile("file", "fly.png", "image/jpeg", fileStream);
		actions = mockMvc.perform(MockMvcRequestBuilders.fileUpload(reqPath).file(testFile).header("user-id", "ilimi"));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());

		reqPath = basePath + "copy/" + identifier;
		String copyContentReq = "{\"request\": {\"content\":{\"name\" : \"CopyContent001\",\"createdBy\":\"testUser\",\"createdFor\": [\"Ekstep\"],\"organisation\": [\"ekstep\"],\"description\":\"copy content\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(reqPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(copyContentReq));
		resp = getResponse(actions);
		String id = (String) ((Map<String, Object>) resp.getResult().get("node_id")).get(contentId);
		System.out.println("Response : " + actions.andReturn().getResponse().getContentAsString());
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Assert.assertNotEquals(id, contentId);
	}

	/**
	 * @param actions
	 * @return
	 */
	public static Response getResponse(ResultActions actions) {
		String content = null;
		Response resp = null;
		try {
			content = actions.andReturn().getResponse().getContentAsString();
			if (StringUtils.isNotBlank(content))
				resp = mapper.readValue(content, Response.class);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	private static File getResourceFile(String fileName) {
		File file = new File(
				ContentV3ControllerCopyContentTest.class.getResource("/UploadFiles/" + fileName).getFile());
		return file;
	}

}
