package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
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
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.taxonomy.mgr.IContentManager;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ContentManagerImplTest {
	@Autowired
	private WebApplicationContext context;
	//private ContentManagerImpl contentManager = new ContentManagerImpl();
	private ResultActions actions;
	private static ILogger LOGGER = new PlatformLogger(IContentManager.class.getName());
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
			LOGGER.log("Create | Exception: " , e.getMessage(), e);
		}
		Response resp = jsonToObject(actions);
		LOGGER.log("Create | Response: " + resp);
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
			LOGGER.log("Delete | Exception: " + e.getMessage(), e);
		}
		Response resp = jsonToObject(actions);
		LOGGER.log("Delete | Response: " + resp);
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
			LOGGER.log("Upload | Exception: " , e.getMessage(), e);
		}
		Response resp = jsonToObject(actions);
		LOGGER.log("Upload | Response: " + resp);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		String actualArtifactUrl = (String)(((Map<String,Object>)((Map<String,Object>)resp.getResult().get("updated_node")).get("metadata")).get("artifactUrl"));
		String expectedArtifactUrl  = "https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/.*";
		System.out.println(actualArtifactUrl.matches(expectedArtifactUrl));
		Assert.assertTrue(actualArtifactUrl.matches(expectedArtifactUrl));
		deleteDefinition(contentId);
	}
	
	public void publish(String contentString) throws IOException {
		//String contentString = "{\"request\":{\"content\":{\"metadata\":{\"osId\":\"org.ekstep.demo.TestCase\",\"status\":\"Live\",\"visibility\":\"Default\",\"description\":\"Build a TestCase\",\"name\":\"Build A TestCase\",\"language\":\"English\",\"contentType\":\"Story\",\"code\":\"org.ekstep.demo.TestCase\",\"lastUpdatedOn\":\"2016-02-15T18:03:28.593+0000\",\"identifier\":\"org.ekstep.num.build.sentence\",\"mimeType\":\""+mimeType+"\",\"pkgVersion\":3,\"owner\":\"EkStep\",\"body\":\"{\\\"theme\\\":{\\\"manifest\\\":{\\\"media\\\":[{\\\"id\\\":\\\"barber_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/barber_1454918396799.png\\\",\\\"type\\\":\\\"image\\\"},{\\\"id\\\":\\\"tailor_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/tailor_1454918475261.png\\\",\\\"type\\\":\\\"image\\\"},{\\\"id\\\":\\\"carpenter_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/carpenter_1454918523295.png\\\",\\\"type\\\":\\\"image\\\"}]}}}\"}}}}";
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
			LOGGER.log("Publish | Exception: " , e.getMessage(), e);
		}
		Response resp = jsonToObject(actions);
		LOGGER.log("Publish | Response: " + resp);
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
			LOGGER.log("Extract | Exception: " , e.getMessage(), e);
		}
		Response resp = jsonToObject(actions);
		LOGGER.log("Extract | Response: " +resp);
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
			LOGGER.log("Bundle | Exception: " , e.getMessage(), e);
		}
		Response resp = jsonToObject(actions);
		LOGGER.log("Bundle | Response: " + resp);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		deleteDefinition(contentIdOne);
		deleteDefinition(contentIdTwo);
		deleteDefinition(contentIdThree);
	}
	ClassLoader classLoader = getClass().getClassLoader();
	
	@Test
	public void uploadECMLContent() throws Exception{
		upload("application/octet-stream", new File(classLoader.getResource("ecml.zip").getFile()));
		//upload("application/vnd.ekstep.ecml-archive", new File(classLoader.getResource("ecml.zip").getFile()));
	}
	@Test
	public void uploadHTMLContent() throws Exception{
		upload("application/vnd.ekstep.html-archive", new File(classLoader.getResource("demo.zip").getFile()));
	}
	@Test
	public void uploadApkContent() throws Exception{
		upload("application/vnd.android.package-archive", new File(classLoader.getResource("android.apk").getFile()));
	}
	@Test
	public void uploadCollectionContent() throws Exception{
		upload("application/vnd.ekstep.content-collection", new File(classLoader.getResource("collection.zip").getFile()));
	}
	@Test
	public void uploadAssetsContent() throws Exception{
		upload("", new File(classLoader.getResource("Assets.jpeg").getFile()));
	}
	@Test
	public void publishECMLContent() throws Exception{
		String contentString = "{\"request\":{\"content\":{\"metadata\":{\"osId\":\"org.ekstep.demo.TestCase\",\"status\":\"Live\",\"visibility\":\"Default\",\"description\":\"Build a TestCase\",\"name\":\"Build A TestCase\",\"language\":\"English\",\"contentType\":\"Story\",\"code\":\"org.ekstep.demo.TestCase\",\"lastUpdatedOn\":\"2016-02-15T18:03:28.593+0000\",\"identifier\":\"org.ekstep.num.build.sentence\",\"artifactUrl\": \"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/content/1452487631391_PrathamStories_Day_1_JAN_9_2016.zip\",\"mimeType\":\"application/octet-stream\",\"pkgVersion\":3,\"owner\":\"EkStep\",\"body\":\"{\\\"theme\\\":{\\\"manifest\\\":{\\\"media\\\":[{\\\"id\\\":\\\"barber_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/barber_1454918396799.png\\\",\\\"type\\\":\\\"image\\\"},{\\\"id\\\":\\\"tailor_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/tailor_1454918475261.png\\\",\\\"type\\\":\\\"image\\\"},{\\\"id\\\":\\\"carpenter_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/carpenter_1454918523295.png\\\",\\\"type\\\":\\\"image\\\"}]}}}\"}}}}";
		publish(contentString);
		//publish("application/vnd.ekstep.ecml-archive");
	}
	@Test
	public void publishHTMLContent() throws Exception{
		String contentString = "{\"request\":{\"content\":{\"metadata\":{\"osId\":\"org.ekstep.demo.TestCase\",\"status\":\"Live\",\"visibility\":\"Default\",\"description\":\"Build a TestCase\",\"name\":\"Build A TestCase\",\"language\":\"English\",\"contentType\":\"Story\",\"code\":\"org.ekstep.demo.TestCase\",\"lastUpdatedOn\":\"2016-02-15T18:03:28.593+0000\",\"identifier\":\"org.ekstep.num.build.sentence\",\"artifactUrl\": \"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/build_a_sentence_1446706423188.zip\",\"mimeType\":\"application/vnd.ekstep.html-archive\",\"pkgVersion\":3,\"owner\":\"EkStep\",\"body\":\"{\\\"theme\\\":{\\\"manifest\\\":{\\\"media\\\":[{\\\"id\\\":\\\"barber_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/barber_1454918396799.png\\\",\\\"type\\\":\\\"image\\\"},{\\\"id\\\":\\\"tailor_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/tailor_1454918475261.png\\\",\\\"type\\\":\\\"image\\\"},{\\\"id\\\":\\\"carpenter_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/carpenter_1454918523295.png\\\",\\\"type\\\":\\\"image\\\"}]}}}\"}}}}";
		publish(contentString);
	}
	@Test
	public void publishApkContent() throws Exception{
		String contentString = "{\"request\":{\"content\":{\"metadata\":{\"osId\":\"org.ekstep.demo.TestCase\",\"status\":\"Live\",\"visibility\":\"Default\",\"description\":\"Build a TestCase\",\"name\":\"Build A TestCase\",\"language\":\"English\",\"contentType\":\"Story\",\"code\":\"org.ekstep.demo.TestCase\",\"lastUpdatedOn\":\"2016-02-15T18:03:28.593+0000\",\"identifier\":\"org.ekstep.num.build.sentence\",\"artifactUrl\":\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/android_1457093660959.apk\",\"mimeType\":\"application/vnd.android.package-archive\",\"pkgVersion\":3,\"owner\":\"EkStep\",\"body\":\"{\\\"theme\\\":{\\\"manifest\\\":{\\\"media\\\":[{\\\"id\\\":\\\"barber_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/barber_1454918396799.png\\\",\\\"type\\\":\\\"image\\\"},{\\\"id\\\":\\\"tailor_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/tailor_1454918475261.png\\\",\\\"type\\\":\\\"image\\\"},{\\\"id\\\":\\\"carpenter_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/carpenter_1454918523295.png\\\",\\\"type\\\":\\\"image\\\"}]}}}\"}}}}";
		publish(contentString);
	}
	@Test
	public void publishCollectionContent() throws Exception{
		String contentString = "{\"request\":{\"content\":{\"metadata\":{\"osId\":\"org.ekstep.demo.TestCase\",\"status\":\"Live\",\"visibility\":\"Default\",\"description\":\"Build a TestCase\",\"name\":\"Build A TestCase\",\"language\":\"English\",\"contentType\":\"Story\",\"code\":\"org.ekstep.demo.TestCase\",\"lastUpdatedOn\":\"2016-02-15T18:03:28.593+0000\",\"identifier\":\"org.ekstep.num.build.sentence\",\"artifactUrl\": \"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/content/1452487631391_PrathamStories_Day_1_JAN_9_2016.zip\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"pkgVersion\":3,\"owner\":\"EkStep\",\"body\":\"{\\\"theme\\\":{\\\"manifest\\\":{\\\"media\\\":[{\\\"id\\\":\\\"barber_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/barber_1454918396799.png\\\",\\\"type\\\":\\\"image\\\"},{\\\"id\\\":\\\"tailor_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/tailor_1454918475261.png\\\",\\\"type\\\":\\\"image\\\"},{\\\"id\\\":\\\"carpenter_img\\\",\\\"src\\\":\\\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/carpenter_1454918523295.png\\\",\\\"type\\\":\\\"image\\\"}]}}}\"}}}}";
		publish(contentString);
	}
	/*@Test
	public void publishAssetsContent() throws Exception{
		publish("");
	}*/
	@Test
	public void extractECMLContent() throws Exception{
		extract("application/octet-stream");
		//extract("application/vnd.ekstep.ecml-archive");
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
