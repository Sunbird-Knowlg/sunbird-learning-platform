/**
 * 
 */
package org.ekstep.framework.controller.test;

import java.util.Map;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.framework.mgr.ICategoryInstanceManager;
import org.ekstep.framework.mgr.ICategoryManager;
import org.ekstep.framework.mgr.IChannelManager;
import org.ekstep.framework.mgr.IFrameworkManager;
import org.ekstep.framework.mgr.ITermManager;
import org.ekstep.framework.mgr.impl.CategoryInstanceManagerImpl;
import org.ekstep.framework.mgr.impl.CategoryManagerImpl;
import org.ekstep.framework.mgr.impl.ChannelManagerImpl;
import org.ekstep.framework.mgr.impl.FrameworkManagerImpl;
import org.ekstep.framework.mgr.impl.TermManagerImpl;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
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
public class TermFrameworkV3ControllerTest extends GraphEngineTestSetup {

	@Autowired
	private WebApplicationContext context;


	private static String channelId;

	private MockMvc mockMvc;
	private ResultActions actions;
	private final String base_category_path = "/framework/v3/term";
	private static String categoryId = null, frameworkId = null, masterCategoryId=null, masterTermId=null;
	private static String termId = null;
	private static ObjectMapper mapper = new ObjectMapper();
	private static ICategoryInstanceManager categoryInstanceManager = new CategoryInstanceManagerImpl();
	private static ICategoryManager categoryManager = new CategoryManagerImpl();
	private static ITermManager termManager = new TermManagerImpl();
	private static IChannelManager channelManager = new ChannelManagerImpl();
	private static IFrameworkManager frameworkManager = new FrameworkManagerImpl();

	private static final String createCategoryReq = "{ \"name\":\"Class\", \"description\":\"\", \"code\":\"class\" }";
	private static final String createChannelReq = "{\"name\":\"channel\",\"description\":\"\",\"code\":\"channelKA\",\"identifier\":\"channelKA\"}";

	@BeforeClass
	public static void beforeClass() throws Exception {
		loadDefinition("definitions/channel_definition.json", "definitions/framework_definition.json",
				"definitions/category_definition.json", "definitions/categoryInstance_definition.json", "definitions/term_definition.json");
		createChannel();
		createMasterCategory();
		createMasterTerm();
		createCategoryInstance();
	}

	private static void createChannel() {
		try {
			Map<String, Object> requestMap = mapper.readValue(createChannelReq,
					new TypeReference<Map<String, Object>>() {
					});

			Response resp = channelManager.createChannel(requestMap);
			if ("OK".equals(resp.getResponseCode().name())) {
				channelId = (String) resp.getResult().get("node_id");
			} else {
				System.out.println("Response: "+ resp.getParams());
			}
			System.out.println("Channel Id: " + channelId);
		} catch (Exception e) {
			System.out.println("Exception Occured while creating Channel :" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private static void createCategoryInstance() {
		try {
			frameworkId = createframework();

			Map<String, Object> requestMap = mapper.readValue(createCategoryReq,
					new TypeReference<Map<String, Object>>() {
					});

			Response resp = categoryInstanceManager.createCategoryInstance(frameworkId, requestMap);
			if ("OK".equals(resp.getResponseCode().name())) {
				categoryId = "class";
			} else {
				System.out.println("Response: "+ resp.getParams());
			}
			System.out.println("CategoryId : " + categoryId);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @return
	 */
	private static String createframework() {
		String frameworkReq = "{ \"name\": \"CBSE\", \"description\": \"CBSE framework of Bihar\", \"code\": \"cbse\" }";
		try {

			Map<String, Object> requestMap = mapper.readValue(frameworkReq, new TypeReference<Map<String, Object>>() {
			});
			String channelId="channelKA";
			frameworkManager.createFramework(requestMap, channelId);
			return "cbse";
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
		masterTermId = "class_standard2";
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
			String path = base_category_path + "/create?framework=" + frameworkId + "&category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
			termId = "standard2";
			System.out.println("TERM ID : " + termId);
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testA2() {
		String request = "{ \"request\": { \"term\": [{ \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\", \"parents\":[{\"identifier\":\"term_class\"}] }] } }";
		try {
			String path = base_category_path + "/create?framework=" + frameworkId + "&category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(500, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testA3() {
		String request = "{ \"request\": { \"term\": [{ \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\", \"parents\":[{\"identifier\":\"term_class\"}] }] } }";
		try {
			String path = base_category_path + "/create?framework=" + "" + "&category=" + "";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testA4() {
		String request = "{ \"request\": { \"term\": { \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\" } } }";
		try {
			String path = base_category_path + "/create?framework=" + frameworkId + "&category=" + categoryId.toUpperCase();
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
			Response response1 = new ObjectMapper().readValue(response.getContentAsString(),new TypeReference<Response>(){});
			Assert.assertEquals("ERR_INVALID_CATEGORY", response1.getParams().getErr());
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
			String path = base_category_path + "/read/" + termId + "?framework=" + frameworkId + "&category="
					+ categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testB1() {
		try {
			String path = base_category_path + "/read/" + termId + "?framework=" + frameworkId + "&category=" + "";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testB2() {
		try {
			String path = base_category_path + "/read/" + termId + "?framework=" + frameworkId + "&category=" + "a1234";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testB3() {
		try {
			String path = base_category_path + "/read/" + termId + "?framework=" + "" + "&category=" + "";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
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
			String path = base_category_path + "/search?framework=" + frameworkId + "&category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON_UTF8).content(request));
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
			String path = base_category_path + "/search?framework=" + "" + "&category=" + "";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON_UTF8).content(request));
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
			String path = base_category_path + "/update/" + termId + "?framework=" + frameworkId + "&category="
					+ categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON_UTF8).content(request));
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
			String path = base_category_path + "/update/" + termId + "?framework=" + "" + "&category=" + "";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON_UTF8).content(request));
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
			String path = base_category_path + "/retire/" + termId + "?framework=" + frameworkId + "&category="
					+ categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testZ1() {
		try {
			String path = base_category_path + "/retire/" + termId + "?framework=" + frameworkId + "&category="
					+ "a1234";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testZ2() {
		try {
			String path = base_category_path + "/retire/" + "a1234" + "?framework=" + frameworkId + "&category="
					+ categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testZ3() {
		try {
			String path = base_category_path + "/retire/" + "a1234" + "?framework=" + "" + "&category=" + "";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

}
