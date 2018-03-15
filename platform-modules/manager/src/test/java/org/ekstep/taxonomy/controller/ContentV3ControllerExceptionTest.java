/**
 * 
 */
package org.ekstep.taxonomy.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author gauraw
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ContentV3ControllerExceptionTest {

	/** The context. */
	@Autowired
	private WebApplicationContext context;

	/** The actions. */
	private ResultActions actions;

	MockMvc mockMvc;

	private static final String basePath = "/v3/content";
	private static ObjectMapper mapper = new ObjectMapper();

	private static String createDocumentContent = "{\"request\": {\"content\": {\"name\": \"Test Contnet\",\"code\": \"test_code\",\"contentType\": \"Story\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";

	@Before
	public void init() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	/*
	 * Create Document Content
	 * 
	 */

	@Test
	public void testContentV3ControllerExp_01() throws Exception {
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(createDocumentContent));
		Assert.assertEquals(500, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Read Document Content
	 * 
	 */
	@Test
	public void testContentV3ControllerExp_02() throws Exception {
		String path = basePath + "/read/" + "do_1234";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Update Content
	 * 
	 */

	@Test
	public void testContentV3ControllerExp_03() throws Exception {
		String path = basePath + "/update/" + "do_1234";

		String updateDocumentContent = "{\"request\": {\"content\": {\"name\": \"Updated Test Contnet\",\"versionKey\": \"1234\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(updateDocumentContent));
		Assert.assertEquals(500, actions.andReturn().getResponse().getStatus());
	}

}
