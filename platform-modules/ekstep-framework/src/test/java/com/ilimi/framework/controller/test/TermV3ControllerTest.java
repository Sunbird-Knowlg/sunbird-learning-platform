/**
 * 
 */
package com.ilimi.framework.controller.test;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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
import com.ilimi.common.Platform;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.framework.mgr.impl.CategoryManagerImpl;
import com.ilimi.framework.test.common.TestParams;
import com.ilimi.framework.test.common.TestSetup;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.engine.router.ActorBootstrap;
import com.ilimi.graph.engine.router.GraphEngineActorPoolMgr;
import com.ilimi.graph.engine.router.GraphEngineManagers;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

/**
 * @author pradyumna
 *
 */
@Ignore
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

	private static Map<String, String> definitions = new HashMap<String, String>();
	static ClassLoader classLoader = TermV3ControllerTest.class.getClassLoader();
	static File definitionLocation = new File(classLoader.getResource("definitions/").getFile());
	static ActorRef reqRouter = null;

	@BeforeClass
	public static void beforeClass() {
		loadDefinition();
		createCategory();
	}

	/**
	 * 
	 */
	private static void loadDefinition() {
		ActorBootstrap.getActorSystem();
		reqRouter = GraphEngineActorPoolMgr.getRequestRouter();
		definitions = loadAllDefinitions(definitionLocation);
	}

	private static Response createDefinition(String graphId, String objectType) {
		Response resp = null;
		try {
			Request request = new Request();
			request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			request.setManagerName(GraphEngineManagers.NODE_MANAGER);
			request.setOperation("importDefinitions");
			request.put(GraphEngineParams.input_stream.name(), objectType);
			Future<Object> response = Patterns.ask(reqRouter, request, timeout);

			Object obj = Await.result(response, t.duration());

			resp = (Response) obj;
			if (!resp.getParams().getStatus().equalsIgnoreCase(TestParams.successful.name())) {
				System.out.println(resp.getParams().getErr() + " :: " + resp.getParams().getErrmsg());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	private static Map<String, String> loadAllDefinitions(File folder) {
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				loadAllDefinitions(fileEntry);
			} else {
				String definition;
				try {
					definition = FileUtils.readFileToString(fileEntry);
					Response resp = createDefinition(Platform.config.getString(TestParams.graphId.name()), definition);
					definitions.put(fileEntry.getName(), resp.getResponseCode().toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return definitions;
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

	/**
	 * createCategoryTerm
	 */
	@Test
	public void testA() {
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

	/**
	 * updateCategoryTerm
	 */
	@Test
	public void testD() {
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

}
