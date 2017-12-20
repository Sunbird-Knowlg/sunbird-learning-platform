package com.ilimi.framework.controller.test;
import java.util.Map;
import java.util.Random;

import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.dto.Response;
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

import com.ilimi.framework.manager.test.BaseCategoryInstanceMgrTest;
import com.ilimi.framework.mgr.IChannelManager;


/**
 * 
 * @author rashmi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChannelV3ControllerTest extends BaseCategoryInstanceMgrTest {
	
	@Autowired
	private WebApplicationContext context;
	private MockMvc mockMvc;
	private ResultActions actions;
	private final String base_channel_path = "/v3/channel";
	static int rn = generateRandomNumber(0, 9999);
	static String node_id = "";

	@Autowired
	IChannelManager mgr;
	
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}
	
	@Test
	public void createChannelWithNodeId() throws Exception {
		String request = "{\"request\":{\"channel\":{\"name\":\"channel\",\"description\":\"sample description of channel\",\"code\":\"karnataka"
				+ rn + "\"}}}";
		String path = base_channel_path + "/create";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = jsonToObject(actions);
		node_id = (String) resp.get("node_id");
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}
	
	@Test
	public void createChannelWithEmptyRequest() throws Exception {
		String path = base_channel_path + "/create";
		String request = "";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void createChannelWithInvalidRequest() throws Exception {
		String request = "{\"request\":{\"channelss\":{\"name\":\"channel\",\"description\":\"sample description of channel\",\"code\":\"karnataka"
				+ rn + "\"}}}";
		String path = base_channel_path + "/create";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void createChannelWithoutCode() throws Exception {
		String request = "{\"request\":{\"channel\":{\"name\":\"channel\",\"description\":\"sample description of channel\"}}}";
		String path = base_channel_path + "/create";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void createChannelWithInvalidUrl() throws Exception {
		String request = "{\"request\":{\"channel\":{\"name\":\"channel\",\"description\":\"sample description of channel\"}}}";
		String path = "/creaate";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void readChannelForValidNodeId() throws Exception {
		String path = base_channel_path + "/read/" + node_id;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = jsonToObject(actions);
		Map<String, Object> channelMap = (Map) resp.get("channel");
		Assert.assertEquals("Live", channelMap.get("status"));
	}

	@Test
	public void readChannelForInValidNodeId() throws Exception {
		String path = base_channel_path + "/read/1279";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void readChannelForInValidUrl() throws Exception {
		String path = base_channel_path + "/reaaaad/1279";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	@Test
	public void updateChannelForValidNodeId() throws Exception {
		String channelId = createChannel(mgr);
		String request = "{\"request\":{\"channel\":{\"description\":\"LP channel API\"}}}";
		String path = base_channel_path + "/update/" + channelId;
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		String readPath = base_channel_path + "/read/" + channelId;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.get(readPath).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp1 = jsonToObject(actions);
		Map<String,Object> map = resp1.getResult();
		Map<String,Object> channelMap = (Map)map.get("channel");
		Assert.assertEquals("LP channel API", channelMap.get("description"));
		
	}

	@Test
	public void updateChannelForInValidRequest() throws Exception {
		String request = "{\"request\":{\"channelss\":{\"description\":\"LP channel API\",\"name\":\"test\",\"code\":\"testUpdate\"}}}";
		String path = base_channel_path + "/update/" + node_id;
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	@Test
	public void updateChannelForEmptyRequest() throws Exception {
		String request = "";
		String path = base_channel_path + "/update/" + node_id;
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void updateChannelForInValidNodeId() throws Exception {
		String request = "{\"request\":{\"channel\":{\"description\":\"LP channel API\",\"name\":\"test\"}}}";
		String path = base_channel_path + "/update/do_9089786";
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void updateChannelForInValidUrl() throws Exception {
		String request = "{\"request\":{\"channel\":{\"description\":\"LP Channel API\",\"name\":\"test\",\"code\":\"testUpdate\"}}}";
		String path = base_channel_path + "/updatyre/" + node_id;
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void searchChannelForValidSearch() throws Exception {
		createChannel(mgr);
		String request = "{\"request\":{\"search\":{}}}";
		String path = base_channel_path + "/list";
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void searchChannelForInValidSearchRequest() throws Exception {
		String request = "";
		String path = base_channel_path + "/list";
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void searchChannelForInValidUrl() throws Exception {
		String request = "{\"request\":{\"search\":{}}}";
		String path = base_channel_path + "/listdss";
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	@Test
	public void retireChannelForValidNodeId() throws Exception {
		String channelId = createChannel(mgr);
		String path = base_channel_path + "/retire/" + channelId;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		String readPath = base_channel_path + "/read/" + channelId;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.get(readPath).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp1 = jsonToObject(actions);
		Map<String,Object> map = resp1.getResult();
		Map<String,Object> channelMap = (Map)map.get("channel");
		Assert.assertEquals("Retired", channelMap.get("status"));
		
	}

	@Test
	public void retireChannelWithoutNodeId() throws Exception {
		String path = base_channel_path + "/retire/";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void retireChannelForInValidNodeId() throws Exception {
		String request = "{\"request\":{}}";
		String path = base_channel_path + "/reitre/do_989775";
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.delete(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void retireChannelForInValidUrl() throws Exception {
		String request = "{\"request\":{}}";
		String path = base_channel_path + "/reitssre/do_989775";
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.delete(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	public static Response jsonToObject(ResultActions actions) throws Exception {
		String content = null;
		Response resp = null;
		content = actions.andReturn().getResponse().getContentAsString();
		ObjectMapper objectMapper = new ObjectMapper();
		resp = objectMapper.readValue(content, Response.class);
		return resp;
	}

	// method to generate random numbers for a given range of input
	private static int generateRandomNumber(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
}
