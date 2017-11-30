/**
 * 
 */
package com.ilimi.framework.controller.test;

import java.util.Map;

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
import com.ilimi.common.dto.Response;
import com.ilimi.framework.mgr.impl.CategoryManagerImpl;
import com.ilimi.framework.test.common.TestSetup;

/**
 * @author pradyumna
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TermV3ControllerTest extends TestSetup {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;
	private ResultActions actions;
	private final String base_category_path = "/v3/category/term";
	private static String categoryId = null;
	private static CategoryManagerImpl categoryManager = new CategoryManagerImpl();
	static String termId = null;
	static ObjectMapper mapper = new ObjectMapper();
	private static String createCategoryReq = "{ \"name\":\"Class\", \"description\":\"\", \"code\":\"class\" }";

	@BeforeClass
	public static void beforeClass() {
		createCategory();
	}

	/**
	 * 
	 */
	private static void createCategory() {
		try {
			Map<String, Object> requestMap = mapper.readValue(createCategoryReq,
					new TypeReference<Map<String, Object>>() {
					});

			Response resp = categoryManager.createCategory(requestMap);
			categoryId = (String) resp.getResult().get("node_id");
			System.out.println("CategoryId : " + categoryId);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	@Test
	public void createCategoryTerm() {
		String request = "{ \"request\": { \"term\": { \"label\": \"Standard2\", \"value\": \"Standard2\", \"description\":\"Second Standard\" } } }";
		try {
			String path = base_category_path + "/create?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse  response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
			Response resp = mapper.readValue(response.getContentAsString(),
					new TypeReference<Response>() {
					});
			termId = (String) resp.getResult().get("node_id");
			System.out.println("TERM ID : " + termId);
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void readCategoryTerm() {
		try {
			String path = base_category_path + "/read/" + termId + "?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void searchCategoryTerm() {
		String request = "{ \"request\": { } }";
		try {
			String path = base_category_path + "/search?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void updateCategoryTerm() {
		String request = "{ \"request\": { \"term\": { \"value\": \"Class2\" } } }";
		try {
			String path = base_category_path + "/update/" + termId + "?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void zRetireCategoryTerm() {
		try {
			String path = base_category_path + "/retire/" + termId + "?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path).header("user-id", "ilimi"));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

}
