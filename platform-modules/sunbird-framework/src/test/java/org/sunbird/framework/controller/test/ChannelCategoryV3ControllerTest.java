package org.sunbird.framework.controller.test;


import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.dto.Response;
import org.sunbird.framework.manager.test.BaseCategoryInstanceMgrTest;
import org.sunbird.framework.mgr.ICategoryManager;
import org.sunbird.framework.mgr.IChannelManager;
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

/**
 * 
 * @author rashmi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChannelCategoryV3ControllerTest extends BaseCategoryInstanceMgrTest {

	@Autowired
	private WebApplicationContext context;
	private MockMvc mockMvc;
	private ResultActions actions;
	private final String base_category_path = "/channel/v3/category";
	private static String node_id = "";
		
	@Autowired
	private ICategoryManager categoryMgr;
	
	@Autowired
	private IChannelManager channelMgr;

	private String createCategoryValidRequest = "{\"category\":{\"name\":\"category\",\"description\":\"\",\"code\":\"class_1" + "\"}}";
	private String createCategoryWithoutCode = "{\"request\":{\"category\":{\"name\":\"category\",\"description\":\"\"}}}";
	private String updateCategoryRequest = "{\"request\":{\"category\":{\"description\":\"LP category API\"}}}";
	private String searchCategory = "{\"request\":{\"search\":{}}}";
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Before
	public void setUp() { this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build(); }
		
	@SuppressWarnings({"unchecked","rawtypes"})
	@Test
	public void createCategoryInstance() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, Map.class);
		String categoryId = createCategory(categoryMgr);
		Map<String,Object> requestData = (Map)requestMap.get("category");
		requestData.put("code", categoryId);
		Map<String,Object> data = new HashMap<String,Object>();
		requestMap.put("category", requestData);
		data.put("request", requestMap);
		String channelId = createChannel(channelMgr);
		String path = base_category_path + "/create";
		String request  = mapper.writeValueAsString(data);
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = jsonToObject(actions);
		node_id = (String) resp.get("node_id");
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void createCategoryInstanceWithoutCode() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryWithoutCode, Map.class);
		String request  = mapper.writeValueAsString(requestMap);
		String channelId = createChannel(channelMgr);
		String path = base_category_path + "/create";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		Response resp = jsonToObject(actions);
		node_id = (String) resp.get("node_id");
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}
	
	@Test
	public void createCategoryWithEmptyRequest() throws Exception {
		Map<String, Object> requestMap = new HashMap<String,Object>();
		String request  = mapper.writeValueAsString(requestMap);
		String channelId = createChannel(channelMgr);
		String path = base_category_path + "/create";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		Response resp = jsonToObject(actions);
		node_id = (String) resp.get("node_id");
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	@Test
	public void createCategoryWithInvalidChannelId() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, Map.class);
		String categoryId = createCategory(categoryMgr);
		Map<String,Object> requestData = (Map)requestMap.get("category");
		requestData.put("code", categoryId);
		Map<String,Object> data = new HashMap<String,Object>();
		requestMap.put("category", requestData);
		data.put("request", requestMap);
		String path = base_category_path + "/create";
		String request  = mapper.writeValueAsString(data);
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", "do_maharastra")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	@Test
	public void createCategoryWithInvalidUrl() throws Exception {
		String channelId = createChannel(channelMgr);
		Map<String,Object> data = new HashMap<String,Object>();
		String path = base_category_path + "/creasxste";
		String request  = mapper.writeValueAsString(data);
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	@Test
	public void readCategoryWithValidCategoryInstanceId() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, Map.class);
		String categoryId = createCategory(categoryMgr);
		Map<String,Object> requestData = (Map)requestMap.get("category");
		requestData.put("code", categoryId);
		Map<String,Object> data = new HashMap<String,Object>();
		requestMap.put("category", requestData);
		data.put("request", requestMap);
		String channelId = createChannel(channelMgr);
		String path = base_category_path + "/create";
		String request  = mapper.writeValueAsString(data);
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Response resp = jsonToObject(actions);
		node_id = (String) resp.get("node_id");
		String readPath = base_category_path + "/read/" + categoryId;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.get(readPath).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response response = jsonToObject(actions);
		Map<String,Object> resultMap = (Map) response.getResult();
		Map<String,Object> categoryData = (Map) resultMap.get("category");
		Assert.assertEquals("Live", categoryData.get("status"));
	}
	
	@Test
	public void readCategoryInstanceWithInvalidCategoryId() throws Exception {
		String channelId = createChannel(channelMgr);
		String readPath = base_category_path + "/read/do_898";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.get(readPath).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	@Test
	public void readCategoryInstanceWithInvalidChannelId() throws Exception {
		String readPath = base_category_path + "/read/do_898";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.get(readPath).header("X-Channel-Id", "do_kar")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	@Test
	public void readCategoryInstanceWithInvalidUrl() throws Exception {
		String channelId = createChannel(channelMgr);
		String readPath = base_category_path + "/reacsd/do_898";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.get(readPath).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	@Test
	public void updateCategoryInstance() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, Map.class);
		String categoryId = createCategory(categoryMgr);
		Map<String,Object> requestData = (Map)requestMap.get("category");
		requestData.put("code", categoryId);
		Map<String,Object> data = new HashMap<String,Object>();
		requestMap.put("category", requestData);
		data.put("request", requestMap);
		String channelId = createChannel(channelMgr);
		String path = base_category_path + "/create";
		String request  = mapper.writeValueAsString(data);
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Response resp = jsonToObject(actions);
		node_id = (String) resp.get("node_id");
		Map<String, Object> updateMap = mapper.readValue(updateCategoryRequest, Map.class);
		String updateRequest  = mapper.writeValueAsString(updateMap);
		String updatePath = base_category_path + "/update/" + categoryId;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(updatePath).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(updateRequest));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		String readPath = base_category_path + "/read/" + categoryId;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.get(readPath).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response response = jsonToObject(actions);
		Map<String,Object> resultMap = (Map) response.getResult();
		Map<String,Object> categoryData = (Map) resultMap.get("category");
		Assert.assertEquals("LP category API", categoryData.get("description"));
	}
	
	@SuppressWarnings({"unchecked"})
	@Test
	public void updateCategoryInstanceWithInvalidCategoryId() throws Exception {
		String channelId = createChannel(channelMgr);
		Map<String, Object> updateMap = mapper.readValue(updateCategoryRequest, Map.class);
		String updateRequest  = mapper.writeValueAsString(updateMap);
		String updatePath = base_category_path + "/update/do_test";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(updatePath).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(updateRequest));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	@SuppressWarnings({"unchecked"})
	@Test
	public void updateCategoryInstanceWithInvalidChannelId() throws Exception {
		String categoryId = createCategory(categoryMgr);
		Map<String, Object> updateMap = mapper.readValue(updateCategoryRequest, Map.class);
		String updateRequest  = mapper.writeValueAsString(updateMap);
		String updatePath = base_category_path + "/update/"+ categoryId;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(updatePath).header("X-Channel-Id", "do_punjab")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(updateRequest));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	@Test
	public void updateCategoryInstanceWithoutRequest() throws Exception {
		String categoryId = createCategory(categoryMgr);
		String channelId = createChannel(channelMgr);
		String updateRequest = "";
		String updatePath = base_category_path + "/update/"+ categoryId;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(updatePath).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(updateRequest));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	@Test
	public void updateCategoryInstanceWithInvalidUrl() throws Exception {
		String categoryId = createCategory(categoryMgr);
		String channelId = createChannel(channelMgr);
		String updateRequest = "";
		String updatePath = base_category_path + "/updatdcsxase/"+ categoryId;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(updatePath).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(updateRequest));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	@Test
	public void searchCategoryInstance() throws Exception {
		Map<String, Object> searchMap = mapper.readValue(searchCategory, Map.class);
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, Map.class);
		String categoryId = createCategory(categoryMgr);
		Map<String,Object> requestData = (Map)requestMap.get("category");
		requestData.put("code", categoryId);
		Map<String,Object> data = new HashMap<String,Object>();
		requestMap.put("category", requestData);
		data.put("request", requestMap);
		String channelId = createChannel(channelMgr);
		String path = base_category_path + "/create";
		String request  = mapper.writeValueAsString(data);
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		String searchPath = base_category_path + "/search";
		String searchReq = mapper.writeValueAsString(searchMap);
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(searchPath).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(searchReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response response = jsonToObject(actions);
		Map<String,Object> map = response.getResult();
		Integer searchResult = (Integer)map.get("count");
		Assert.assertEquals(1, searchResult.intValue());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void searchCategoryInstanceWithInvalidChannel() throws Exception {
		Map<String, Object> searchMap = mapper.readValue(searchCategory, Map.class);
		String searchPath = base_category_path + "/search";
		String request  = mapper.writeValueAsString(searchMap);
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(searchPath).header("X-Channel-Id", "do_nepal")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	@Test
	public void searchCategoryInstanceWithoutRequest() throws Exception {
		String channelId = createChannel(channelMgr);
		String searchPath = base_category_path + "/search?framework=do_0987654345";
		String request = "";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(searchPath).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	@Test
	public void searchCategoryWithInvalidUrl() throws Exception {
		String channelId = createChannel(channelMgr);
		String searchPath = base_category_path + "/searcxsxah";
		String request = "";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(searchPath).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	@Test
	public void retireCategoryInstance() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, Map.class);
		String categoryId = createCategory(categoryMgr);
		Map<String,Object> requestData = (Map)requestMap.get("category");
		requestData.put("code", categoryId);
		Map<String,Object> data = new HashMap<String,Object>();
		requestMap.put("category", requestData);
		data.put("request", requestMap);
		String channelId = createChannel(channelMgr);
		String path = base_category_path + "/create";
		String request  = mapper.writeValueAsString(data);
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		String retirePath = base_category_path + "/retire/"+ categoryId;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(retirePath).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		String readPath = base_category_path + "/read/" + categoryId;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.get(readPath).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response response = jsonToObject(actions);
		Map<String,Object> resultMap = (Map) response.getResult();
		Map<String,Object> categoryData = (Map) resultMap.get("category");
		Assert.assertEquals("Retired", categoryData.get("status"));
	}
	
	@Test
	public void retireCategoryWithInvalidCategoryId() throws Exception {
		String channelId = createChannel(channelMgr);
		String retirePath = base_category_path + "/retire/do_876434564";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(retirePath).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	@Test
	public void retireCategoryWithInvalidChannelId() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, Map.class);
		String categoryId = createCategory(categoryMgr);
		Map<String,Object> requestData = (Map)requestMap.get("category");
		requestData.put("code", categoryId);
		Map<String,Object> data = new HashMap<String,Object>();
		requestMap.put("category", requestData);
		data.put("request", requestMap);
		String channelId = createChannel(channelMgr);
		String path = base_category_path + "/create";
		String request  = mapper.writeValueAsString(data);
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", channelId)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		String retirePath = base_category_path + "/retire/" + categoryId;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(retirePath).header("X-Channel-Id", "do_karnataka")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	@Test
	public void retireCategoryWithInvalidUrl() throws Exception {
		String retirePath = base_category_path + "/retisxsxre/do_98513";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(retirePath).header("X-Channel-Id", "do_9632")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8));
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
	}
