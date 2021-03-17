/**
 * 
 */
package org.sunbird.framework.controller.test;

import java.util.Map;

import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.framework.mgr.ICategoryInstanceManager;
import org.sunbird.framework.mgr.ICategoryManager;
import org.sunbird.framework.mgr.IChannelManager;
import org.sunbird.framework.mgr.ITermManager;
import org.sunbird.framework.mgr.impl.CategoryInstanceManagerImpl;
import org.sunbird.framework.mgr.impl.CategoryManagerImpl;
import org.sunbird.framework.mgr.impl.ChannelManagerImpl;
import org.sunbird.framework.mgr.impl.TermManagerImpl;
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
import org.springframework.mock.web.MockHttpServletResponse;
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
 * @author pradyumna
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TermChannelV3ControllerTest extends GraphEngineTestSetup {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;
	private ResultActions actions;
	private final String base_category_path = "/channel/v3/term";
	private static String categoryId = null, channelId = null, masterCategoryId=null, masterTermId=null;
	private static String termId = null;
	private static ObjectMapper mapper = new ObjectMapper();
	private static ICategoryInstanceManager categoryInstanceManager = new CategoryInstanceManagerImpl();
	private static ICategoryManager categoryManager = new CategoryManagerImpl();
	private static ITermManager termManager = new TermManagerImpl();
	private static IChannelManager channelManager = new ChannelManagerImpl();

	private static String createCategoryReq = "{ \"name\":\"Class\", \"description\":\"\", \"code\":\"class\" }";

	@BeforeClass
	public static void beforeClass() throws Exception {
		loadDefinition("definitions/channel_definition.json", "definitions/category_definition.json", "definitions/categoryInstance_definition.json","definitions/term_definition.json");
		createMasterCategory();
		createMasterTerm();
		createCategoryInstance();
	}

	/**
	 * 
	 */
	private static void createCategoryInstance() throws Exception {
		channelId = createChannel();
		Map<String, Object> requestMap = mapper.readValue(createCategoryReq,
				new TypeReference<Map<String, Object>>() {
				});
		categoryInstanceManager.createCategoryInstance(channelId, requestMap);
		categoryId = "class";
	}

	/**
	 * @return
	 */
	private static String createChannel() {
		String channelReq = "{ \"identifier\":\"hsr\", \"name\":\"hsr\", \"description\":\"\", \"code\":\"hsr\" }";
		try {
			
			Map<String, Object> requestMap = mapper.readValue(channelReq,
					new TypeReference<Map<String, Object>>() {
					});

			Response resp = channelManager.createChannel(requestMap);
			return (String) resp.getResult().get("node_id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * This Method will create a master category.
	 * 
	 * @author gauraw
	 */
	private static void createMasterCategory() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryReq,
				new TypeReference<Map<String, Object>>() {
				});
		Response resp = categoryManager.createCategory(requestMap);
		masterCategoryId = (String) resp.getResult().get("node_id");
		System.out.println("masterCategoryId : "+masterCategoryId);
	}
	
	/**
	 * This Method will create Term under Master Category.
	 * 
	 * @author gauraw
	 */
	private static void createMasterTerm() throws Exception {
		String createMasterTermJson = "{\"term\": { \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\" } }";
		Map<String, Object> requestMap = mapper.readValue(createMasterTermJson,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = new Request();
		request.setRequest(requestMap);
		termManager.createTerm(null, masterCategoryId, request);
		masterTermId = "standard2";
		System.out.println("masterTermId : "+masterTermId);
	}

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	/**
	 * createCategoryTerm
	 */
	@Test
	public void testA() {
		String request = "{ \"request\": { \"term\": { \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\" } } }";
		try {
			String path = base_category_path + "/create?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", channelId)
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
			termId = "standard2";
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testA1() {
		String request = "{ \"request\": { \"term\": { \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\" } } }";
		try {
			String path = base_category_path + "/create?category=" + "";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", channelId)
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testA2() {
		String request = "{ \"request\": { \"term\": { \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\" } } }";
		try {
			String path = base_category_path + "/create?category=" + "1234";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", channelId)
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testA3() {
		String request = "{ \"request\": { \"term\": { \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\" } } }";
		try {
			String path = base_category_path + "/create?category=" + "";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", "")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	/**
	 * readCategoryTerm
	 */
	@Test
	public void testB() {
		try {
			String path = base_category_path + "/read/" + termId + "?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path).header("X-Channel-Id", channelId));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	/**
	 * searchCategoryTerm
	 */
	@Test
	public void testC() {
		String request = "{ \"request\": { } }";
		try {
			String path = base_category_path + "/search?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", channelId)
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testC1() {
		String request = "{ \"request\": { } }";
		try {
			String path = base_category_path + "/search?category=" + "";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", "")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	/**
	 * updateCategoryTerm
	 */
	@Test
	public void testD() {
		String request = "{ \"request\": { \"term\": { \"name\": \"Class2\" } } }";
		try {
			String path = base_category_path + "/update/" + termId + "?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).header("X-Channel-Id", channelId)
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testD1() {
		String request = "{ \"request\": { \"term\": { \"name\": \"Class2\" } } }";
		try {
			String path = base_category_path + "/update/" + termId + "?category=" + "";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).header("X-Channel-Id", "")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	/**
	 * RetireCategoryTerm
	 */
	@Test
	public void testZ() {
		try {
			String path = base_category_path + "/retire/" + termId + "?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path).header("X-Channel-Id", channelId));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testZ1() {
		try {
			String path = base_category_path + "/retire/" + termId + "?category=" + "";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path).header("X-Channel-Id", ""));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

}
