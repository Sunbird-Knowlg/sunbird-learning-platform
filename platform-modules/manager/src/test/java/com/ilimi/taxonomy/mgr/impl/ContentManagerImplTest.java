package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
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

import com.ilimi.common.dto.Response;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ContentManagerImplTest {
	@Autowired
	private WebApplicationContext context;
	//private ContentManagerImpl contentManager = new ContentManagerImpl();
	private ResultActions actions;
	//private static Logger LOGGER = LogManager.getLogger(IContentManager.class.getName());
	private static String contentId = "";
	private static String TAXONOMY_ID = "numeracy";
	private static String OBJECT_TYPE = "Story";

	public String createDefinitionNode(String contentString) {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/content?taxonomyId=" + TAXONOMY_ID + "&type=" + OBJECT_TYPE;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes())
					.header("user-id", "ilimi"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		return (String) resp.getResult().get("node_id");
	}

	public void deleteDefinition(String contentId) {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/content/" + contentId + "?taxonomyId=" + TAXONOMY_ID + "&type="
				+ OBJECT_TYPE;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.delete(path)
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		System.out.println("Deletion Done");
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}
	
	@SuppressWarnings("unchecked")
	public void upload(String mimeType, File uploadFile) throws IOException {
		String contentString = "{\"request\":{\"content\":{\"metadata\":{\"osId\":\"org.ekstep.demo.TestCase\",\"status\":\"Live\",\"visibility\":\"Default\",\"description\":\"Build a TestCase\",\"name\":\"Build A TestCase\",\"language\":\"English\",\"contentType\":\"Story\",\"code\":\"org.ekstep.demo.TestCase\",\"lastUpdatedOn\":\"2016-02-15T18:03:28.593+0000\",\"identifier\":\"org.ekstep.num.build.sentence\",\"mimeType\":\""+mimeType+"\",\"pkgVersion\":3,\"owner\":\"EkStep\",\"body\":\"<content></content>\"}}}}";
		FileInputStream inputFile = new FileInputStream(uploadFile);
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", uploadFile.getName(),
				"multipart/form-data", inputFile);
		contentId = createDefinitionNode(contentString);
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v2/content/upload/" + contentId + "?type=" + OBJECT_TYPE;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.fileUpload(path)
					.file(mockMultipartFile).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		String actualArtifactUrl = (String)(((Map<String,Object>)((Map<String,Object>)resp.getResult().get("updated_node")).get("metadata")).get("artifactUrl"));
		String expectedArtifactUrl  = "https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/demo_.*"+".zip";
		System.out.println(actualArtifactUrl.matches(expectedArtifactUrl));
		Assert.assertTrue(actualArtifactUrl.matches(expectedArtifactUrl));
		deleteDefinition(contentId);
	}
	
	public void publish(String mimeType) throws IOException {
		String contentString = "{\"request\":{\"content\":{\"metadata\":{\"osId\":\"org.ekstep.demo.TestCase\",\"status\":\"Live\",\"visibility\":\"Default\",\"description\":\"Build a TestCase\",\"name\":\"Build A TestCase\",\"language\":\"English\",\"contentType\":\"Story\",\"code\":\"org.ekstep.demo.TestCase\",\"lastUpdatedOn\":\"2016-02-15T18:03:28.593+0000\",\"identifier\":\"org.ekstep.num.build.sentence\",\"mimeType\":\"application/vnd.ekstep.ecml-archive\",\"pkgVersion\":3,\"owner\":\"EkStep\",\"body\":\"{\\\"theme\\\":{\\\"manifest\\\":{\\\"media\\\":[{\\\"id\\\":\\\"barber_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/barber_1454918396799.png\\\",\\\"type\\\":\\\"image\\\"},{\\\"id\\\":\\\"tailor_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/tailor_1454918475261.png\\\",\\\"type\\\":\\\"image\\\"},{\\\"id\\\":\\\"carpenter_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/carpenter_1454918523295.png\\\",\\\"type\\\":\\\"image\\\"}]}}}\"}}}}";
		String contentId = createDefinitionNode(contentString);
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v2/content/publish/" + contentId;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path)
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		deleteDefinition(contentId);
	}
	
	public void extract(String mimeType) throws IOException {
		String contentString = "{\"request\":{\"content\":{\"metadata\":{\"name\":\"Math Magic\",\"description\":\"Math Magic\",\"body\":\"<content/>\",\"code\":\"org.ekstep.story.math.magic\",\"owner\":\"EkStep\",\"artifactUrl\":\"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/content/1452494498502_Hindihathibhaloojan11new.zip\",\"status\":\"Live\",\"mimeType\":\""+mimeType+"\"},\"outRelations\":[],\"tags\":[]}}}";
		String contentId = createDefinitionNode(contentString);
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v2/content/extract/" + contentId;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path)
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		deleteDefinition(contentId);
	}
	
	public void bundle(String mimeType) throws IOException {
		String contentStringFirst = "{\"request\":{\"content\":{\"metadata\":{\"name\":\"Math Magic\",\"description\":\"Math Magic\",\"body\":\"<content/>\",\"code\":\"org.ekstep.story.math.magic\",\"owner\":\"EkStep\",\"artifactUrl\":\"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/content/1452494498502_Hindihathibhaloojan11new.zip\",\"status\":\"Live\",\"mimeType\":\""+mimeType+"\"},\"outRelations\":[],\"tags\":[]}}}";
		String contentIdOne = createDefinitionNode(contentStringFirst);
		String contentStringSecond = "{\"request\":{\"content\":{\"metadata\":{\"name\":\"Math Magic\",\"description\":\"Math Magic\",\"body\":\"<content/>\",\"code\":\"org.ekstep.story.math.magic\",\"owner\":\"EkStep\",\"artifactUrl\":\"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/content/1452494498502_Hindihathibhaloojan11new.zip\",\"status\":\"Live\",\"mimeType\":\""+mimeType+"\"},\"outRelations\":[],\"tags\":[]}}}";
		String contentIdTwo = createDefinitionNode(contentStringSecond);
		String contentStringThird = "{\"request\":{\"content\":{\"metadata\":{\"name\":\"Math Magic\",\"description\":\"Math Magic\",\"body\":\"<content/>\",\"code\":\"org.ekstep.story.math.magic\",\"owner\":\"EkStep\",\"artifactUrl\":\"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/content/1452494498502_Hindihathibhaloojan11new.zip\",\"status\":\"Live\",\"mimeType\":\""+mimeType+"\"},\"outRelations\":[],\"tags\":[]}}}";
		String contentIdThree = createDefinitionNode(contentStringThird);
		String contentBundleString ="{\"request\":{\"content_identifiers\":[\""+contentIdOne+"\",\""+contentIdThree+"\",\""+contentIdTwo+"\"],\"file_name\":\"Pratham_Reading\"}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/content/bundle?taxonomyId=" +TAXONOMY_ID;
		try {
			actions =mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON).content(contentBundleString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		deleteDefinition(contentIdOne);
		deleteDefinition(contentIdTwo);
		deleteDefinition(contentIdThree);
	}
	ClassLoader classLoader = getClass().getClassLoader();
	@Ignore
	@Test
	public void uploadECMLContent() throws Exception{
		upload("application/octet-stream", new File(classLoader.getResource("ecml.zip").getFile()));
		upload("application/vnd.ekstep.ecml-archive", new File(classLoader.getResource("ecml.zip").getFile()));
	}
	@Ignore
	@Test
	public void uploadHTMLContent() throws Exception{
		upload("application/vnd.ekstep.html-archive", new File(classLoader.getResource("demo.zip").getFile()));
	}
	@Ignore
	@Test
	public void uploadApkContent() throws Exception{
		upload("application/vnd.android.package-archive", new File(classLoader.getResource("android.apk").getFile()));
	}
	@Ignore
	@Test
	public void uploadCollectionContent() throws Exception{
		upload("application/vnd.ekstep.content-collection", new File(classLoader.getResource("collection.zip").getFile()));
	}
	@Ignore
	@Test
	public void uploadAssetsContent() throws Exception{
		upload("", new File(classLoader.getResource("Assets.*").getFile()));
	}
	@Ignore
	@Test
	public void publishECMLContent() throws Exception{
		publish("application/octet-stream");
		//publish("application/vnd.ekstep.ecml-archive");
	}
	@Ignore
	@Test
	public void publishHTMLContent() throws Exception{
		publish("application/vnd.ekstep.html-archive");
	}
	@Ignore
	@Test
	public void publishApkContent() throws Exception{
		publish("application/vnd.android.package-archive");
	}
	@Ignore
	@Test
	public void publishCollectionContent() throws Exception{
		publish("application/vnd.ekstep.content-collection");
	}
	@Ignore
	@Test
	public void publishAssetsContent() throws Exception{
		publish("");
	}
	@Ignore
	@Test
	public void extractECMLContent() throws Exception{
		extract("application/octet-stream");
		extract("application/vnd.ekstep.ecml-archive");
	}
	@Test
	public void bundle() throws IOException {
		bundle("application/octet-stream");
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
}
