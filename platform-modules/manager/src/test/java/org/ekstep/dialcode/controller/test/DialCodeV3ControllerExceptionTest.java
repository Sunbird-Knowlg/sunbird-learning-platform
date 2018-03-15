package org.ekstep.dialcode.controller.test;

import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
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

/**
 * @author gauraw
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class DialCodeV3ControllerExceptionTest {

	@Autowired
	private WebApplicationContext context;

	MockMvc mockMvc;
	private ResultActions actions;
	private static final String basePath = "/v3/dialcode";
	
	@BeforeClass
	public static void setup(){
		CompositeSearchConstants.DIAL_CODE_INDEX ="test000000000011";
	}
	
	@Before
	public void init() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	// List Dial Code - 500 - SERVER_ERROR
	@Test
	public void testDialCode_01() throws Exception {
		String path = basePath + "/list";
		String req = "{\"request\": {\"search\": {\"publisher\":\"test0001000001000\",\"status\":\"Draft\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(500, actions.andReturn().getResponse().getStatus());
	}

	// Create Publisher - 500 - SERVER_ERROR
	@Test
	public void testDialCode_02() throws Exception {
		String path = basePath + "/publisher/create";
		String req = "{\"request\":{\"publisher\": {\"identifier\":\"publisher2\",\"name\": \"PUBLISHER2\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(500, actions.andReturn().getResponse().getStatus());
	}

	// Read Publisher - 500 - SERVER_ERROR
	@Test
	public void testDialCode_03() throws Exception {
		String path = basePath + "/publisher/read/" + "ABC123";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(500, actions.andReturn().getResponse().getStatus());
	}

	// Update Publisher - 500 - SERVER_ERROR
	@Test
	public void testDialCode_04() throws Exception {
		String path = basePath + "/publisher/update/" + "ABC123";
		String req = "{\"request\":{\"publisher\": {\"name\": \"PUBLISHER001\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(500, actions.andReturn().getResponse().getStatus());
	}

	// List Dial Code - 500 - SERVER_ERROR
	@Test
	@Ignore
	public void testDialCode_05() throws Exception {
		String path = basePath + "/list";
		String req = "{\"request\": {\"search\": {\"publisher\":\"testpub010001000001000\",\"status\":\"Draft\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(500, actions.andReturn().getResponse().getStatus());
	}

	// Search Dial Code -405 -
	@Test
	public void testDialCode_06() throws Exception {
		String path = basePath + "/search";
		String req = "{\"request\": {\"search\": {\"publisher\":\"testpub01\",\"status\":\"Draft\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(405, actions.andReturn().getResponse().getStatus());
	}

	// Sync Dial Code - 500 - SERVER_ERROR
	@Ignore
	@Test
	public void testDialCode_07() throws Exception {
		String path = basePath + "/sync";
		String req = "{\"request\":{\"sync\": {\"name\": \"PUBLISHER001\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(500, actions.andReturn().getResponse().getStatus());
	}

}