package org.ekstep.taxonomy.mgr.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.controller.BaseController;
import org.ekstep.common.dto.Response;
import org.ekstep.taxonomy.mgr.IContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class LibraryManagerImplTest extends BaseController{
	@Autowired
	private WebApplicationContext context;
	
	@Autowired
    private IContentManager contentManager;
	//private ContentManagerImpl contentManager = new ContentManagerImpl();
	private ResultActions actions;
	

	
	
	@SuppressWarnings("unchecked")
	public void upload(File uploadFile,String libraryId) throws IOException {
		FileInputStream inputFile = new FileInputStream(uploadFile);
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", uploadFile.getName(),
				"multipart/form-data", inputFile);
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/library/upload/" + libraryId;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.fileUpload(path)
					.file(mockMultipartFile).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			TelemetryManager.error("Upload | Exception: " + e.getMessage(), e);
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		String actualArtifactUrl = (String)(((Map<String,Object>)((Map<String,Object>)resp.getResult().get("updated_node")).get("metadata")).get("artifactUrl"));
		String expectedArtifactUrl  = "https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/.*";
		System.out.println(actualArtifactUrl.matches(expectedArtifactUrl));
		Assert.assertTrue(actualArtifactUrl.matches(expectedArtifactUrl));
	}
	
	public void publish(String libraryId) throws IOException {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/library/publish/" + libraryId;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path)
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			TelemetryManager.error("Publish | Exception: " + e.getMessage(), e);
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}
	

	ClassLoader classLoader = getClass().getClassLoader();
	
	@Test
	public void uploadApkContent() throws Exception{
		upload(new File(classLoader.getResource("android.apk").getFile()),"org.ekstep.library");
	}
	@Test
	public void publishApkContent() throws Exception{
			publish("org.ekstep.library");
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
