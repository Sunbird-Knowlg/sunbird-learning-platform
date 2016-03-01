package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

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

	public String createDefinitionNode(String mimeType) {
		String contentString = "{\"request\":{\"content\":{\"metadata\":{\"osId\":\"org.ekstep.demo.TestCase\",\"status\":\"Live\",\"visibility\":\"Default\",\"description\":\"Build a TestCase\",\"name\":\"Build A TestCase\",\"language\":\"English\",\"contentType\":\"Story\",\"code\":\"org.ekstep.demo.TestCase\",\"lastUpdatedOn\":\"2016-02-15T18:03:28.593+0000\",\"identifier\":\"org.ekstep.num.build.sentence\",\"mimeType\":\""+mimeType+"\",\"pkgVersion\":3,\"owner\":\"EkStep\",\"body\":\"<content></content>\"}}}}";
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

	public void upload(String mimeType, File uploadFile) throws IOException {
		//File uploadFile = new File("C:\\ilimi\\test\\demo.zip");
		FileInputStream inputFile = new FileInputStream(uploadFile);
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", uploadFile.getName(),
				"multipart/form-data", inputFile);
		contentId = createDefinitionNode(mimeType);
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
		deleteDefinition(contentId);
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}
	ClassLoader classLoader = getClass().getClassLoader();
	@Ignore
	@Test
	public void uploadECML() throws Exception{
		//File file = new File(classLoader.getResource("").getFile());
		upload("application/octet-stream", new File(classLoader.getResource("").getFile()));
		upload("application/vnd.ekstep.ecml-archive", new File(classLoader.getResource("ecml.zip").getFile()));
	}
	@Test
	public void uploadHTML() throws Exception{
		upload("application/vnd.ekstep.html-archive", new File(classLoader.getResource("demo.zip").getFile()));
	}
	@Ignore
	@Test
	public void uploadAndroid() throws Exception{
		
		upload("application/vnd.android.package-archive", new File(classLoader.getResource("android.apk").getFile()));
	}
	@Ignore
	@Test
	public void uploadCollection() throws Exception{
		
		upload("application/vnd.ekstep.content-collection", new File(classLoader.getResource("collection.zip").getFile()));
	}
	@Ignore
	@Test
	public void uploadAssets() throws Exception{
		
		upload("", new File(classLoader.getResource("Assets.*").getFile()));
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
