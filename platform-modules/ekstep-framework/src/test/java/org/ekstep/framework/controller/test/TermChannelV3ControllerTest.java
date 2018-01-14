/**
 * 
 */
package org.ekstep.framework.controller.test;

import java.util.Map;

import org.ekstep.common.dto.Response;
import org.ekstep.framework.mgr.ICategoryInstanceManager;
import org.ekstep.framework.mgr.ICategoryManager;
import org.ekstep.framework.mgr.IChannelManager;
import org.ekstep.framework.mgr.ITermManager;
import org.ekstep.framework.mgr.impl.CategoryInstanceManagerImpl;
import org.ekstep.framework.mgr.impl.CategoryManagerImpl;
import org.ekstep.framework.mgr.impl.ChannelManagerImpl;
import org.ekstep.framework.mgr.impl.TermManagerImpl;
import org.ekstep.framework.test.common.TestSetup;
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
public class TermChannelV3ControllerTest extends TestSetup {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;
	private ResultActions actions;
	private final String base_category_path = "/v3/channel/term";
	private static String categoryId = null, channelId = null, masterCategoryId=null, masterTermId=null;
	static String termId = null;
	static ObjectMapper mapper = new ObjectMapper();
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
		String createMasterTermJson="{ \"label\": \"Standard2\", \"value\": \"Standard2\", \"description\":\"Second Standard\" }";
		Map<String, Object> requestMap = mapper.readValue(createMasterTermJson,
				new TypeReference<Map<String, Object>>() {
				});
		termManager.createTerm(null, masterCategoryId, requestMap);
		masterTermId = "standard2";
		System.out.println("masterTermId : "+masterTermId);
	}

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	/**
	 * createCategoryTerm
	 */
	@Test
	public void testA() {
		String request = "{ \"request\": { \"term\": { \"label\": \"Standard2\", \"value\": \"Standard2\", \"description\":\"Second Standard\" } } }";
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

	/**
	 * updateCategoryTerm
	 */
	@Test
	public void testD() {
		String request = "{ \"request\": { \"term\": { \"value\": \"Class2\" } } }";
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

}
