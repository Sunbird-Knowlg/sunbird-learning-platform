package org.sunbird.framework.controller.test;

import java.util.Map;
import java.util.Random;

import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.dto.Response;
import org.sunbird.graph.engine.common.GraphEngineTestSetup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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
public class CategoryV3ControllerTest extends GraphEngineTestSetup {

	@Autowired
	private WebApplicationContext context;
	private MockMvc mockMvc;
	private ResultActions actions;
	private final String base_category_path = "/framework/v3/category/master";
	static int rn = generateRandomNumber(0, 9999);
	static String node_id = "";

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}
	
	@BeforeClass()
	public static void beforeClass() throws Exception {
		loadDefinition("definitions/category_definition.json");
	}

	@Test
	public void createCategoryWithNodeId() throws Exception {
		String request = "{\"request\":{\"category\":{\"name\":\"category\",\"description\":\"sample description of category\",\"code\":\"subject"
				+ rn + "\"}}}";
		String path = base_category_path + "/create";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = jsonToObject(actions);
		node_id = (String) resp.get("node_id");
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}

	@Test
	public void createCategoryWithInvalidRequest() throws Exception {
		String request = "{\"request\":{\"categorysRequest\":{\"name\":\"category\",\"description\":\"sample description of category\",\"code\":\"subject"
				+ rn + "\"}}}";
		String path = base_category_path + "/create";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void createCategoryWithoutCode() throws Exception {
		String request = "{\"request\":{\"category\":{\"name\":\"category\",\"description\":\"sample description of category\"}}}";
		String path = base_category_path + "/create";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void createCategoryWithInvalidUrl() throws Exception {
		String request = "{\"request\":{\"category\":{\"name\":\"category\",\"description\":\"sample description of category\"}}}";
		String path = "/creaate";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void readCategoryForValidNodeId() throws Exception {
		String path = base_category_path + "/read/" + node_id;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = jsonToObject(actions);
		Map<String, Object> categoryMap = (Map) resp.get("category");
		Assert.assertEquals("Live", categoryMap.get("status"));
	}

	@Test
	public void readCategoryForInValidNodeId() throws Exception {
		String path = base_category_path + "/read/1279";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void readCategoryForInValidUrl() throws Exception {
		String path = base_category_path + "/reaaaad/1279";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void updateCategoryForValidNodeId() throws Exception {
		String request = "{\"request\":{\"category\":{\"description\":\"LP category API\",\"name\":\"test\"}}}";
		String path = base_category_path + "/update/" + node_id;
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = jsonToObject(actions);
		String node_id_result = (String) resp.get("node_id");
		Assert.assertEquals(node_id, node_id_result);
	}

	@Test
	public void updateCategoryForInValidRequest() throws Exception {
		String request = "{\"request\":{\"categorysRequest\":{\"description\":\"LP category API\",\"name\":\"test\",\"code\":\"testUpdate\"}}}";
		String path = base_category_path + "/update/" + node_id;
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void updateCategoryForInValidNodeId() throws Exception {
		String request = "{\"request\":{\"category\":{\"description\":\"LP category API\",\"name\":\"test\"}}}";
		String path = base_category_path + "/update/do_9089786";
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void updateCategoryForInValidUrl() throws Exception {
		String request = "{\"request\":{\"category\":{\"description\":\"LP category API\",\"name\":\"test\",\"code\":\"testUpdate\"}}}";
		String path = base_category_path + "/updatyre/" + node_id;
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void searchCategoryForValidSearch() throws Exception {
		String request = "{\"request\":{\"search\":{}}}";
		String path = base_category_path + "/search";
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void searchCategoryForInValidSearchRequest() throws Exception {
		String request = "";
		String path = base_category_path + "/search";
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void searchCategoryForInValidSearchRequest1() throws Exception {
		String request = "{\"request\":{}}";
		String path = base_category_path + "/search";
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void searchCategoryForInValidUrl() throws Exception {
		String request = "{\"request\":{}}";
		String path = base_category_path + "/seaxarch";
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void retireCategoryForValidNodeId() throws Exception {
		String path = base_category_path + "/retire/" + node_id;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = jsonToObject(actions);
		String node = (String) resp.get("node_id");
		Assert.assertEquals(node, node_id);
	}

	@Test
	public void retireCategoryWithoutNodeId() throws Exception {
		String path = base_category_path + "/retire/";
		actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void retireCategoryWithNullNodeId() throws Exception {
		String path = base_category_path + "/retire/" + null;
		actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void retireCategoryForInValidNodeId() throws Exception {
		String request = "{\"request\":{}}";
		String path = base_category_path + "/reitre/do_989775";
		actions = this.mockMvc
				.perform(MockMvcRequestBuilders.delete(path).contentType(MediaType.APPLICATION_JSON).content(request));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void retireCategoryForInValidUrl() throws Exception {
		String request = "{\"request\":{}}";
		String path = base_category_path + "/reitssre/do_989775";
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
