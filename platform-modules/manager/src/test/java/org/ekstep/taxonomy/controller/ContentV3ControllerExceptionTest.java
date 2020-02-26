/**
 * 
 */
package org.ekstep.taxonomy.controller;

import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.test.common.CommonTestSetup;
import org.junit.*;
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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author gauraw
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ContentV3ControllerExceptionTest extends CommonTestSetup {

		/** The context. */
	@Autowired
	private WebApplicationContext context;

	/** The actions. */
	private ResultActions actions;

	MockMvc mockMvc;

	private static final String basePath = "/content/v3";
	private static ObjectMapper mapper = new ObjectMapper();

	private static String createDocumentContent = "{\"request\": {\"content\": {\"name\": \"Test Contnet\",\"code\": \"test_code\",\"contentType\": \"Story\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
	private static final String SCRIPT_1 = "CREATE KEYSPACE IF NOT EXISTS content_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static final String SCRIPT_2 = "CREATE TABLE IF NOT EXISTS content_store_test.content_data (content_id text, last_updated_on timestamp,body blob,oldBody blob,screenshots blob,stageIcons blob,PRIMARY KEY (content_id));";
	private static final String SCRIPT_3 = "CREATE KEYSPACE IF NOT EXISTS hierarchy_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static final String SCRIPT_4 = "CREATE TABLE IF NOT EXISTS hierarchy_store_test.content_hierarchy_test (identifier text, hierarchy text, PRIMARY KEY (identifier));";


	@BeforeClass
	public static void setup() throws Exception {
		loadDefinition("definitions/content_definition.json","definitions/content_image_definition.json", "definitions/concept_definition.json",
				"definitions/dimension_definition.json", "definitions/domain_definition.json");
		executeScript(SCRIPT_1, SCRIPT_2, SCRIPT_3, SCRIPT_4);
		LearningRequestRouterPool.init();
	}

	@Before
	public void init() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	/*
	 * Create Document Content
	 * 
	 */

	@Ignore
	@Test
	public void testContentV3ControllerExp_01() throws Exception {
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(createDocumentContent));
		System.out.println("testContentV3ControllerExp_01: "+  actions.andReturn().getResponse().getContentAsString());
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Read Document Content
	 * 
	 */
	@Test
	public void testContentV3ControllerExp_02() throws Exception {
		String path = basePath + "/read/" + "do_1234";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Update Content
	 * 
	 */

	@Test
	public void testContentV3ControllerExp_03() throws Exception {
		String path = basePath + "/update/" + "do_1234";

		String updateDocumentContent = "{\"request\": {\"content\": {\"name\": \"Updated Test Contnet\",\"versionKey\": \"1234\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(updateDocumentContent));
		System.out.println("testContentV3ControllerExp_03: "+  actions.andReturn().getResponse().getContentAsString());
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

}
