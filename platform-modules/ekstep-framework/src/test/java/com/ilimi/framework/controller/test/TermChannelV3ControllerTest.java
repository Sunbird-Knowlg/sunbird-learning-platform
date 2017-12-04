/**
 * 
 */
package com.ilimi.framework.controller.test;

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
import com.ilimi.framework.mgr.impl.CategoryInstanceManagerImpl;
import com.ilimi.framework.mgr.impl.ChannelManagerImpl;
import com.ilimi.framework.test.common.TestSetup;

/**
 * @author pradyumna
 *
 */
@Ignore
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
	private static String categoryId = null, channelId = null;
	private static CategoryInstanceManagerImpl categoryInstanceManager = new CategoryInstanceManagerImpl();
	private static ChannelManagerImpl channelManager = new ChannelManagerImpl();
	static String termId = null;
	static ObjectMapper mapper = new ObjectMapper();
	private static String createCategoryReq = "{ \"name\":\"Class\", \"description\":\"\", \"code\":\"class\" }";

	@BeforeClass
	public static void beforeClass() throws Exception {
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
		Response resp = categoryInstanceManager.createCategoryInstance(channelId, requestMap);
		categoryId = (String) resp.getResult().get("node_id");
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
			Response resp = mapper.readValue(response.getContentAsString(), new TypeReference<Response>() {
			});
			termId = (String) resp.getResult().get("node_id");			
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
