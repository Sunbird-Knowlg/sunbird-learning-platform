/**
 * 
 */
package com.ilimi.framework.controller.test;

import java.io.File;
import java.util.Map;

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
import com.ilimi.common.dto.Response;
import com.ilimi.framework.mgr.ICategoryInstanceManager;
import com.ilimi.framework.mgr.IChannelManager;
import com.ilimi.framework.mgr.impl.CategoryInstanceManagerImpl;
import com.ilimi.framework.mgr.impl.ChannelManagerImpl;
import com.ilimi.framework.mgr.impl.FrameworkManagerImpl;
import com.ilimi.framework.test.common.TestSetup;

import akka.actor.ActorRef;

/**
 * @author pradyumna
 *
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TermFrameworkV3ControllerTest extends TestSetup {

	@Autowired
	private WebApplicationContext context;

	private static IChannelManager channelManager = new ChannelManagerImpl();
	private static String channelId;

	private MockMvc mockMvc;
	private ResultActions actions;
	private final String base_category_path = "/v3/framework/term";
	private static String categoryId = null, frameworkId = null;
	private static ICategoryInstanceManager categoryInstanceManager = new CategoryInstanceManagerImpl();
	private static FrameworkManagerImpl frameworkManager = new FrameworkManagerImpl();
	static String termId = null;
	static ObjectMapper mapper = new ObjectMapper();
	private static final String createCategoryReq = "{ \"name\":\"Class\", \"description\":\"\", \"code\":\"class\" }";
	private static final String createChannelReq = "{\"name\":\"channel\",\"description\":\"\",\"code\":\"channelKA\",\"identifier\":\"channelKA\"}";

	static ClassLoader classLoader = TermFrameworkV3ControllerTest.class.getClassLoader();
	static File definitionLocation = new File(classLoader.getResource("definitions/").getFile());
	static ActorRef reqRouter = null;

	@BeforeClass
	public static void beforeClass() throws Exception {
		loadDefinition("definitions/channel_definition.json", "definitions/framework_definition.json",
				"definitions/category_definition.json", "definitions/categoryInstance_definition.json", "definitions/term_definition.json");
		createChannel();
		createCategoryInstance();
	}

	@SuppressWarnings("unlikely-arg-type")
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
	@SuppressWarnings("unlikely-arg-type")
	private static void createCategoryInstance() {
		try {
			frameworkId = createframework();

			Map<String, Object> requestMap = mapper.readValue(createCategoryReq,
					new TypeReference<Map<String, Object>>() {
					});

			Response resp = categoryInstanceManager.createCategoryInstance(frameworkId, requestMap);
			if ("OK".equals(resp.getResponseCode().name())) {
				categoryId = (String) resp.getResult().get("node_id");
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
		String frameworkReq = "{ \"name\": \"CBSE2\", \"description\": \"CBSE framework of Bihar\", \"code\": \"org.ekstep.framework.create\", \"owner\": \"channelKA\" }";
		try {

			Map<String, Object> requestMap = mapper.readValue(frameworkReq, new TypeReference<Map<String, Object>>() {
			});

			Response resp = frameworkManager.createFramework(requestMap);
			return (String) resp.getResult().get("node_id");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
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
			String path = base_category_path + "/create?framework=" + frameworkId + "&category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
			Response resp = mapper.readValue(response.getContentAsString(), new TypeReference<Response>() {
			});
			termId = (String) resp.getResult().get("node_id");
			System.out.println("TERM ID : " + termId);
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

	/**
	 * updateCategoryTerm
	 */
	@Test
	public void testD() {
		String request = "{ \"request\": { \"term\": { \"value\": \"Class2\" } } }";
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

}
