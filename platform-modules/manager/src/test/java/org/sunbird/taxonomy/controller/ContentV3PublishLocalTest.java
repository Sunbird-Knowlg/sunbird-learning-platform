package org.sunbird.taxonomy.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.graph.engine.common.TestParams;
import org.sunbird.learning.router.LearningRequestRouterPool;
import org.sunbird.learning.util.CloudStore;
import org.sunbird.taxonomy.mgr.impl.ContentManagerImpl;
import org.sunbird.test.common.CommonTestSetup;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
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

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ContentV3PublishLocalTest extends CommonTestSetup{

	/** The context. */
	@Autowired
	private WebApplicationContext context;

	/** The actions. */
	private ResultActions actions;

	MockMvc mockMvc;

	private static final String basePath = "/content/v3";
	private static ObjectMapper mapper = new ObjectMapper();

	private static String contentId = ""; 
	private static String mimeType = "";
	private static String cassandraScript_1 = "CREATE KEYSPACE IF NOT EXISTS content_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static String cassandraScript_2 = "CREATE TABLE IF NOT EXISTS content_store_test.content_data ( content_id text, last_updated_on timestamp, body blob, oldBody blob, stageIcons blob, PRIMARY KEY (content_id) );";
	private static String channelId = "in.ekstep";
	
	@BeforeClass
	public static void setup() throws Exception {
        LearningRequestRouterPool.init();
		loadDefinition("definitions/content_definition.json", "definitions/concept_definition.json",
				"definitions/dimension_definition.json", "definitions/domain_definition.json");
		executeScript(cassandraScript_1, cassandraScript_2);

	}


	@Before
	public void init() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
		contentId = createECMLContent();
		mimeType = "application/vnd.ekstep.ecml-archive";
		System.out.println(contentId);

	}

	@After
	public void teardown() throws Exception {
		//delete AWS uploads if any
		//deleteAWSuploads(contentId, mimeType);
	}
	
	@Test
	public void testContentPreview() throws Exception {
		
		
		
		//upload content
		String path = basePath + "/upload/" + contentId;
		File inputFile = new File(ContentV3ControllerTest.class.getResource("/UploadFiles/uploadContent.zip").getFile());
		FileInputStream fileStream = new FileInputStream(inputFile);
		MockMultipartFile testFile = new MockMultipartFile("file",
				"uploadContent.zip", "application/zip", fileStream);
		actions = mockMvc.perform(MockMvcRequestBuilders.fileUpload(path)
				.file(testFile).header("user-id", "ilimi"));
		Response uploadResponse  = jsonToObject(actions);
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		
		path = basePath + "/publish/" + contentId;
		String publishReqBody = "{\"request\": {\"content\" : {\"lastPublishedBy\" : \"Ekstep\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(publishReqBody));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());

		//read content
		path = basePath + "/read/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Response contentResponse  = jsonToObject(actions);
		Assert.assertEquals("successful", contentResponse.getParams().getStatus());
		Map<String, Object> content = (Map<String, Object>) contentResponse.getResult().get("content");
		String contentMimeType = (String) content.get(ContentWorkflowPipelineParams.mimeType.name());
		Assert.assertEquals(mimeType, contentMimeType);
		//previewUrl should not be null
		String previewUrl = (String) content.get(ContentWorkflowPipelineParams.previewUrl.name());
		Assert.assertNotNull(previewUrl);
		
		
	}
	
	public static String createECMLContent() throws Exception {
			String ecmlContentId = "";
			ContentManagerImpl contentManager = new ContentManagerImpl();
			String createECMLContent = "{ \"identifier\":\"do_13034\",\"description\":\"Test Content\", \"name\":\"Test Content\", \"contentType\":\"Story\", \"code\":\"test content\", \"mimeType\":\"application/vnd.ekstep.ecml-archive\" } ";
			Map<String, Object> documentContentMap = mapper.readValue(createECMLContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response documentResponse = contentManager.create(documentContentMap, channelId);
			ecmlContentId = (String) documentResponse.getResult().get(TestParams.node_id.name());
			//versionKey = (String) documentResponse.getResult().get(TestParams.versionKey.name());
			return ecmlContentId;

	}
	
	public static Response jsonToObject(ResultActions actions) {
		String content = null;
		Response resp = null;
		try {
			content = actions.andReturn().getResponse().getContentAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			if (StringUtils.isNotBlank(content))
				resp = objectMapper.readValue(content, Response.class);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	
	private void deleteAWSuploads(String contentId, String mimeType) throws Exception {
		String artifactUrlKey = "content"+ File.separator +contentId;
		CloudStore.deleteFile(artifactUrlKey, false);
		System.out.println("deleted file :"+artifactUrlKey);
		CloudStore.deleteFile(getExtractionPath(contentId, "snapshot", mimeType), true);
		CloudStore.deleteFile(getExtractionPath(contentId, "latest", mimeType), true);
		System.out.println("deleted extracted files");
	}
	
	private String getExtractionPath(String contentId, String pathSuffix, String mimeType) {
		String path = "";
		String contentFolder = "content";
		String DASH = "-";
		
		switch (mimeType) {
		case "application/vnd.ekstep.ecml-archive":
			path += contentFolder + File.separator + "ecml"+ File.separator + contentId + DASH
					+ pathSuffix;
			break;
		case "application/vnd.ekstep.html-archive":
			path += contentFolder + File.separator + "html" + File.separator + contentId + DASH
					+ pathSuffix;
			break;
		case "application/vnd.ekstep.h5p-archive":
			path += contentFolder + File.separator + "h5p" + File.separator + contentId + DASH
					+ pathSuffix;
			break;
		default:
			break;
		}
		return path;
	}
}
