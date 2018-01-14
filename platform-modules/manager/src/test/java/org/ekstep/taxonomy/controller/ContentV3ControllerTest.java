package org.ekstep.taxonomy.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.ekstep.common.dto.Response;
import org.ekstep.taxonomy.mgr.impl.ContentManagerImpl;
import org.ekstep.test.common.TestParams;
import org.ekstep.test.common.TestSetUp;
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

/**
 * Test Cases for V3 Controller
 * 
 * @author gauraw
 *
 */
//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ContentV3ControllerTest extends TestSetUp{
	
	/** The context. */
	@Autowired
	private WebApplicationContext context;

	/** The actions. */
	private ResultActions actions;
	
	MockMvc mockMvc;
	
	private static final String basePath = "/v3/content";
	private static ObjectMapper mapper = new ObjectMapper();
	
	private static String contentId="";
	private static String versionKey="";
	
	private static String collectionContent1Id="";
	private static String collectionVersion1Key="";
	private static String collectionContent2Id="";
	private static String collectionVersion2Key="";
	private static String collectionContent3Id="";
	private static String collectionVersion3Key="";
	
	private static String createDocumentContent = "{\"request\": {\"content\": {\"name\": \"Test Contnet\",\"code\": \"test_code\",\"contentType\": \"Story\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"],\"status\":\"Draft\"}}}";
	
	//private static String createCollectionContent="{\"request\": {\"content\": {\"name\": \"Collection_Content_2017_Dec_27_102\",\"code\": \"Collection_Content_2017_Dec_27_102\",\"contentType\": \"Collection\",\"mimeType\": \"application/vnd.ekstep.content-collection\"}}}";
	
	@BeforeClass
	public static void setup() throws Exception {
		loadDefinition("definitions/content_definition.json", "definitions/concept_definition.json", "definitions/dimension_definition.json","definitions/domain_definition.json");
		createDocumentContent();
		createCollectionContent();
	}

	@Before
	public void init() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}
	
	public static void createDocumentContent() throws Exception {
		ContentManagerImpl contentManager = new ContentManagerImpl();
		String createDocumentContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"application/pdf\"}";
		Map<String, Object> documentContentMap = mapper.readValue(createDocumentContent,
				new TypeReference<Map<String, Object>>() {
				});
		Response documentResponse = contentManager.createContent(documentContentMap);
		contentId=(String)documentResponse.getResult().get(TestParams.node_id.name());
		versionKey= (String) documentResponse.getResult().get(TestParams.versionKey.name());
		//String fileUrl="https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/u_document_04/artifact/pdf.pdf";
		//Response response = contentManager.upload(contentId, "domain", fileUrl, "");
	}
	
	public static void createCollectionContent() throws Exception {
		ContentManagerImpl contentManager = new ContentManagerImpl();
		String createCollectionContent1 = "{\"name\": \"Collection_Content_2017_Dec_27_102\",\"code\": \"Collection_Content_2017_Dec_27_102\",\"contentType\": \"Collection\",\"mimeType\": \"application/vnd.ekstep.content-collection\"}";
		Map<String, Object> collectionContentMap1 = mapper.readValue(createCollectionContent1,
				new TypeReference<Map<String, Object>>() {
				});
		Response resp1 = contentManager.createContent(collectionContentMap1);
		collectionContent1Id=(String)resp1.getResult().get(TestParams.node_id.name());
		collectionVersion1Key= (String) resp1.getResult().get(TestParams.versionKey.name());
		
		String createCollectionContent2 = "{\"name\": \"Collection_Content_2017_Dec_27_103\",\"code\": \"Collection_Content_2017_Dec_27_102\",\"contentType\": \"Collection\",\"mimeType\": \"application/vnd.ekstep.content-collection\"}";
		Map<String, Object> collectionContentMap2 = mapper.readValue(createCollectionContent2,
				new TypeReference<Map<String, Object>>() {
				});
		Response resp2 = contentManager.createContent(collectionContentMap2);
		collectionContent2Id=(String)resp2.getResult().get(TestParams.node_id.name());
		collectionVersion2Key= (String) resp2.getResult().get(TestParams.versionKey.name());
		
		String createCollectionContent3 = "{\"name\": \"Collection_Content_2017_Dec_27_104\",\"code\": \"Collection_Content_2017_Dec_27_102\",\"contentType\": \"Collection\",\"mimeType\": \"application/vnd.ekstep.content-collection\"}";
		Map<String, Object> collectionContentMap3 = mapper.readValue(createCollectionContent3,
				new TypeReference<Map<String, Object>>() {
				});
		Response resp3 = contentManager.createContent(collectionContentMap3);
		collectionContent3Id=(String)resp3.getResult().get(TestParams.node_id.name());
		collectionVersion3Key= (String) resp3.getResult().get(TestParams.versionKey.name());
	}
	
	private static File getResourceFile(String fileName) {
		File file = new File(ContentV3ControllerTest.class.getResource("/UploadFiles/" + fileName).getFile());
		return file;
	}
	
	/**
	 * Create Document Content
	 * 
	 * */
	
	@Test
	public void testContentV3Controller_01() throws Exception {
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelKA")
				.content(createDocumentContent));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}
	
	/**
	 * Read Document Content
	 * 
	 * */
	@Test
	public void testContentV3Controller_02() throws Exception {
		String path = basePath + "/read/"+contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}
	
	/**
	 * Update Content
	 * 
	 * */
	@Test
	public void testContentV3Controller_03() throws Exception {
		String path = basePath + "/update/"+contentId;
		String updateDocumentContent = "{\"request\": {\"content\": {\"name\": \"Updated Test Contnet\",\"versionKey\": \""+versionKey+"\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(updateDocumentContent));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}
	
	
	/**
	 * Review Content
	 * 
	 * */
	@Ignore
	@Test
	public void testContentV3Controller_04() throws Exception {
		String path = basePath + "/review/"+contentId;
		String publishReqBody = "{\"request\": {\"content\" : {}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(publishReqBody));
		System.out.println("Response :: "+actions.andReturn().getResponse().getContentAsString());
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}
	
	
	/**
	 * Publish Content
	 * 
	 * */
	
	@Test
	public void testContentV3Controller_05() throws Exception {
		String path = basePath + "/publish/"+collectionContent2Id;
		String publishReqBody = "{\"request\": {\"content\" : {\"lastPublishedBy\" : \"Ekstep\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(publishReqBody));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}
	
	/**
	 * Unlisted Publish  Content
	 * 
	 * */
	
	@Test
	public void testContentV3Controller_06() throws Exception {
		String path = basePath + "/unlisted/publish/"+collectionContent3Id;
		String publishReqBody = "{\"request\": {\"content\" : {\"lastPublishedBy\" : \"Ekstep\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(publishReqBody));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}
	
	/**
	 * Get Hierarchy of  Content
	 * 
	 * */
	
	@Test
	public void testContentV3Controller_07() throws Exception {
		String path = basePath + "/hierarchy/"+collectionContent3Id;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}
	
	/**
	 * Bundle
	 * 
	 * Content with Body is Required.
	 * 
	 * */
	@Ignore
	@Test
	public void testContentV3Controller_08() throws Exception {
		String path = basePath + "/bundle";
		String bundleReqBody = "{\"request\": {\"content_identifiers\": [\""+contentId+"\"],\"file_name\": \"test_dev_bundle\"}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(bundleReqBody));
		System.out.println("Response :: "+actions.andReturn().getResponse().getContentAsString());
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}
	
	/**
	 *Upload with File
	 * 
	 * */
	@Ignore
	@Test
	public void testContentV3Controller_09() throws Exception {
		String path = basePath + "/upload/"+contentId;
		FileInputStream fis = new FileInputStream(getResourceFile("test.pdf"));
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		Map<String, String> contentTypeParams = new HashMap<String, String>();
		contentTypeParams.put("boundary", "265001916915724");
		MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);
		actions = mockMvc.perform(
				MockMvcRequestBuilders.post(path).contentType(mediaType).content(multipartFile.getBytes()));
		System.out.println("Response :: "+actions.andReturn().getResponse().getContentAsString());
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}
	
}