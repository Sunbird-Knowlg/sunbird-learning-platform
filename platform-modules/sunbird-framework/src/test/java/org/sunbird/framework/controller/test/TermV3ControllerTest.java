/**
 * 
 */
package org.sunbird.framework.controller.test;

import java.util.Map;

import org.sunbird.common.dto.Response;
import org.sunbird.framework.mgr.ICategoryManager;
import org.sunbird.framework.mgr.impl.CategoryManagerImpl;
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
public class TermV3ControllerTest extends GraphEngineTestSetup {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;
	private ResultActions actions;
	private final String base_category_path = "/framework/v3/category/term";
	private static String categoryId = null;
	private static String termId = null;
	private static ObjectMapper mapper = new ObjectMapper();
	private static ICategoryManager categoryManager = new CategoryManagerImpl();

	private static String createCategoryReq = "{ \"name\":\"Class\", \"description\":\"\", \"code\":\"class\" }";

	@BeforeClass
	public static void beforeClass() throws Exception {
		loadDefinition("definitions/category_definition.json", "definitions/category_definition.json", "definitions/term_definition.json");
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
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
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
		String request = "{ \"request\": { \"term\": [{ \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\" }, { \"name\": \"Standard3\", \"code\": \"Standard3\", \"description\":\"Third Standard\" }] } }";
		try {
			String path = base_category_path + "/create?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testA2() {
		String request = "{ \"request\": { \"term\": [{ \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\" }, { \"name\": \"Standard3\", \"description\":\"Third Standard\" }] } }";
		try {
			String path = base_category_path + "/create?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(207, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testA3() {
		String request = "{ \"request\": { \"term\": [{}] } }";
		try {
			String path = base_category_path + "/create?category=" + categoryId;
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
		String request = "{ \"request\": { \"term\": [] } }";
		try {
			String path = base_category_path + "/create?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testA5() {
		String request = "{ \"request\": {}}";
		try {
			String path = base_category_path + "/create?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testA6() {
		String request = "{ \"request\": { \"term\": [{ \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\" }, { \"name\": \"Standard3\", \"description\":\"Third Standard\" }] } }";
		try {
			String path = base_category_path + "/create?category=" + "";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testA7() {
		String request = "{ \"request\": { \"term\": [{ \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\" }, { \"name\": \"Standard3\", \"description\":\"Third Standard\" }] } }";
		try {
			String path = base_category_path + "/create?category=" + "a1234";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testA8() {
		String request = "{ \"request\": { \"term\": [{ \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\", \"parents\":[{\"identifier\":\"term_class\"}] }] } }";
		try {
			String path = base_category_path + "/create?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(500, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testA9() {
		String request = "{ \"request\": { \"term\": [{ \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\", \"parents\":[{\"identifier\":\"term_class\"}] }, { \"name\": \"Standard3\", \"code\": \"Standard2\", \"description\":\"Third Standard\" }] } }";
		try {
			String path = base_category_path + "/create?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(207, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testA10() {
		String request = "{ \"request\": { \"term\": [{\"name\": \"Standard3\", \"code\": \"Standard2\", \"description\":\"Third Standard\" },{ \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\", \"parents\":[{\"identifier\":\"term_class\"}] }] } }";
		try {
			String path = base_category_path + "/create?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(207, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testA11() {
		String request = "{ \"request\": { \"term\": [{\"name\": \"Standard3\", \"description\":\"Third Standard\" },{ \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\", \"parents\":[{\"identifier\":\"term_class\"}] }] } }";
		try {
			String path = base_category_path + "/create?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(500, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}
	@Test
	public void testA12() {
		String request = "{ \"request\": { \"term\": [{ \"name\": \"Standard2\", \"code\": \"Standard2\", \"description\":\"Second Standard\" }, { \"name\": \"Standard3\", \"description\":\"Third Standard\" }] } }";
		try {
			String path = base_category_path + "/create?category=" + categoryId.toUpperCase();
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
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
			actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testB1() {
		try {
			String path = base_category_path + "/read/" + termId + "?category=" + "";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
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
	public void testC1() {
		String request = "{ \"request\": { } }";
		try {
			String path = base_category_path + "/search?category=" + "";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
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
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(200, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testD1() {
		String request = "{ \"request\": {} }";
		try {
			String path = base_category_path + "/update/" + termId + "?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testD2() {
		String request = "{ \"request\": { \"term\": { \"code\": \"Class2\" } } }";
		try {
			String path = base_category_path + "/update/" + termId + "?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	@Test
	public void testD3() {
		String request = "{ \"request\": { \"term\": { \"name\": \"Class2\", \"parents\":[{\"identifier\":\"term_class\"}]} } }";
		try {
			String path = base_category_path + "/update/" + termId + "?category=" + categoryId;
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
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
			actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path).header("user-id", "ilimi"));
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
			actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path).header("user-id", "ilimi"));
			MockHttpServletResponse response = actions.andReturn().getResponse();
			Assert.assertEquals(400, response.getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

}
