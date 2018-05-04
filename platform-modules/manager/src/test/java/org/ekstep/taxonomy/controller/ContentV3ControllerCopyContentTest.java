/**
 * 
 */
package org.ekstep.taxonomy.controller;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
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

	private static final String basePath = "/v3/content/";
	private static ObjectMapper mapper = new ObjectMapper();
	private static ContentManagerImpl contentManager = new ContentManagerImpl();
	private static String contentId = "";
	private static String contentId2 = "";
	private static String versionKey = "";
	private static String versionKey2 = "";

	private static String script_1 = "CREATE KEYSPACE IF NOT EXISTS content_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static String script_2 = "CREATE TABLE IF NOT EXISTS content_store_test.content_data_test (content_id text, last_updated_on timestamp,body blob,oldBody blob,stageIcons blob,PRIMARY KEY (content_id));";
	private static String copyContentReq = "{\"request\": {\"content\":{\"name\" : \"CopyContent001\",\"createdBy\":\"testUser\",\"createdFor\": [\"Ekstep\"],\"organization\": [\"ekstep\"],\"description\":\"copy content\"}}}";

	@BeforeClass
	public static void setup() throws Exception {
		loadDefinition("definitions/content_definition.json", "definitions/concept_definition.json",
				"definitions/dimension_definition.json", "definitions/domain_definition.json");
		executeScript(script_1, script_2);
		LearningRequestRouterPool.init();
		createDocumentContent();
	}

	@AfterClass
	public static void clean() {

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
			String createDocumentContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"testcontent\",\"mimeType\":\"application/pdf\"}";
			Map<String, Object> documentContentMap = mapper.readValue(createDocumentContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response documentResponse = contentManager.create(documentContentMap);
			if (i == 1) {
				contentId = (String) documentResponse.getResult().get(TestParams.node_id.name());
				versionKey = (String) documentResponse.getResult().get(TestParams.versionKey.name());
			} else if (i == 2) {
				contentId2 = (String) documentResponse.getResult().get(TestParams.node_id.name());
				versionKey2 = (String) documentResponse.getResult().get(TestParams.versionKey.name());
			}
		}
	}

	private static Map<String,Object> uploadContent(String contentId) {
		String versionKey = "";
		String artifactUrl="";
		Map<String,Object> map=new HashMap<String,Object>();
		String mimeType = "application/pdf";
		String fileUrl = "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/u_document_04/artifact/pdf.pdf";
		Response response = contentManager.upload(contentId, fileUrl, mimeType);
		String responseCode = (String) response.getResponseCode().toString();
		if ("OK".equalsIgnoreCase(responseCode)){
			versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			artifactUrl=(String)response.getResult().get("content_url");
		}
		if(StringUtils.isNotBlank(versionKey) && StringUtils.isNotBlank(artifactUrl)){
			map.put(TestParams.versionKey.name(), versionKey);
			map.put(TestParams.artifactUrl.name(), artifactUrl);
		}
			
		return map;
	}

	private static boolean publishContent(String contentId) {
		Map<String, Object> requestMap = new HashMap<String, Object>();
		requestMap.put("lastPublishedBy", "Ekstep");
		requestMap.put("publishChecklist", Arrays.asList("test"));
		Response response = contentManager.publish(contentId, requestMap);
		String responseCode = (String) response.getResponseCode().toString();
		if ("OK".equalsIgnoreCase(responseCode)){
			System.out.println("Content Published Successfully");
			return true;
		}
		return false;
	}

	/*
	 * Copy Content with Draft Status without artifactUrl
	 * 
	 */
	@Test
	public void copyContentTest_01() throws Exception {
		String reqPath = basePath +"copy/"+ contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.post(reqPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(copyContentReq));
		Response resp = getResponse(actions);
		String id = (String) ((Map<String, Object>) resp.getResult().get("node_id")).get(contentId);
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Assert.assertNotEquals(id, contentId);
	}

	/*
	 * Copy Content with Draft Status with artifactUrl
	 * 
	 */
	@Test
	public void copyContentTest_02() throws Exception {
		Map<String,Object> map=uploadContent(contentId);
		delay();
		//copy content
		String reqPath = basePath +"copy/"+ contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.post(reqPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(copyContentReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		String id = (String) ((Map<String, Object>) resp.getResult().get("node_id")).get(contentId);
		
		//Get Copied Content
		reqPath=basePath+"read/"+id;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(reqPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest"));
		resp = getResponse(actions);
		String artifactUrl=(String)((Map<String,Object>)resp.getResult().get("content")).get("artifactUrl");
		Assert.assertNotNull(artifactUrl);
		Assert.assertNotEquals((String)map.get(TestParams.artifactUrl.name()), artifactUrl);
		Assert.assertEquals(true,artifactUrl.contains(id));
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

}
