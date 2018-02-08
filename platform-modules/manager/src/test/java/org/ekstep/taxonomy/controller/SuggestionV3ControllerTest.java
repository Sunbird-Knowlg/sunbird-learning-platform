/**
 * 
 */
package org.ekstep.taxonomy.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.ekstep.graph.engine.common.TestParams;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.taxonomy.enums.SuggestionConstants;
import org.ekstep.taxonomy.mgr.ISuggestionManager;
import org.ekstep.taxonomy.mgr.impl.ContentManagerImpl;
import org.junit.AfterClass;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author gauraw
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class SuggestionV3ControllerTest extends GraphEngineTestSetup {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ISuggestionManager suggestionManager;

	private ResultActions actions;

	MockMvc mockMvc;

	private static final String basePath = "/v3/suggestions";
	private static ObjectMapper mapper = new ObjectMapper();

	private static String INDEX = "test_suggestion";
	private static String contentId = "";
	private static String suggestionId = "";

	@BeforeClass
	public static void setup() throws Exception {
		loadDefinition("definitions/content_definition.json", "definitions/concept_definition.json",
				"definitions/dimension_definition.json", "definitions/domain_definition.json");
		SuggestionConstants.SUGGESTION_INDEX = INDEX;
	}

	@AfterClass
	public static void clean() throws IOException {
		ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
		elasticSearchUtil.deleteIndex(INDEX);

	}

	@Before
	public void init() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
		if (StringUtils.isBlank(contentId))
			createContent();
	}

	private void createContent() throws Exception {
		ContentManagerImpl contentManager = new ContentManagerImpl();
		String createDocumentContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"application/pdf\"}";
		Map<String, Object> documentContentMap = mapper.readValue(createDocumentContent,
				new TypeReference<Map<String, Object>>() {
				});
		Response documentResponse = contentManager.createContent(documentContentMap);
		contentId = (String) documentResponse.getResult().get(TestParams.node_id.name());
	}

	private void createSuggestion() throws Exception {
		String path = basePath + "/create";
		String createReq = "{\"request\": {\"content\":{\"objectId\":\"" + contentId
				+ "\",\"objectType\":\"content\",\"suggestedBy\":\"User001\",\"command\":\"update\",\"params\":{\"gradeLevel\":[\"Grade 7\"]}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(createReq));
		suggestionId = (String) jsonToObject(actions).get("suggestion_id");
	}

	/*
	 * Create Suggestion with Valid URI, Valid Request Body & Valid Content
	 * Expect : 200 - OK
	 */
	@Test
	public void testSuggestions_01() throws Exception {
		String path = basePath + "/create";
		String createReq = "{\"request\": {\"content\":{\"objectId\":\"" + contentId
				+ "\",\"objectType\":\"content\",\"suggestedBy\":\"User001\",\"command\":\"update\",\"params\":{\"gradeLevel\":[\"Grade 7\"]}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(createReq));
		suggestionId = (String) jsonToObject(actions).get("suggestion_id");
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Create Suggestion with Valid URI, Valid Request Body & Invalid Content
	 * Expect : 400 - CLIENT_ERROR
	 */
	@Test
	public void testSuggestions_02() throws Exception {
		String path = basePath + "/create";
		String createReq = "{\"request\": {\"content\":{\"objectId\":\"" + "ABC"
				+ "\",\"objectType\":\"content\",\"suggestedBy\":\"User001\",\"command\":\"update\",\"params\":{\"gradeLevel\":[\"Grade 7\"]}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(createReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Create Suggestion with Valid URI, Invalid Request Body - Blank objectId
	 * Expect : 400 - CLIENT_ERROR
	 */
	@Test
	public void testSuggestions_03() throws Exception {
		String path = basePath + "/create";
		String createReq = "{\"request\": {\"content\":{\"objectId\":\"" + ""
				+ "\",\"objectType\":\"content\",\"suggestedBy\":\"User001\",\"command\":\"update\",\"params\":{\"gradeLevel\":[\"Grade 7\"]}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(createReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Create Suggestion with Valid URI, Invalid Request Body - Blank objectType
	 * Expect : 400 - CLIENT_ERROR
	 */
	@Test
	public void testSuggestions_04() throws Exception {
		String path = basePath + "/create";
		String createReq = "{\"request\": {\"content\":{\"objectId\":\"" + "ABC"
				+ "\",\"objectType\":\"\",\"suggestedBy\":\"User001\",\"command\":\"update\",\"params\":{\"gradeLevel\":[\"Grade 7\"]}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(createReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Create Suggestion with Valid URI, Invalid Request Body - Blank command
	 * Expect : 400 - CLIENT_ERROR
	 */
	@Test
	public void testSuggestions_05() throws Exception {
		String path = basePath + "/create";
		String createReq = "{\"request\": {\"content\":{\"objectId\":\"" + "ABC"
				+ "\",\"objectType\":\"content\",\"suggestedBy\":\"User001\",\"command\":\"\",\"params\":{\"gradeLevel\":[\"Grade 7\"]}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(createReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Create Suggestion with Valid URI, Invalid Request Body - Blank
	 * suggestedBy. Expect : 400 - CLIENT_ERROR
	 */
	@Test
	public void testSuggestions_06() throws Exception {
		String path = basePath + "/create";
		String createReq = "{\"request\": {\"content\":{\"objectId\":\"" + "ABC"
				+ "\",\"objectType\":\"content\",\"suggestedBy\":\"\",\"command\":\"update\",\"params\":{\"gradeLevel\":[\"Grade 7\"]}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(createReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Create Suggestion with Valid URI, Invalid Request Body - Blank params.
	 * Expect : 400 - CLIENT_ERROR
	 */
	@Test
	public void testSuggestions_07() throws Exception {
		String path = basePath + "/create";
		String createReq = "{\"request\": {\"content\":{\"objectId\":\"" + "ABC"
				+ "\",\"objectType\":\"content\",\"suggestedBy\":\"User001\",\"command\":\"update\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(createReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Read Suggestion by valid Object Id Expect : 200 - OK
	 */
	@Test
	public void testSuggestions_08() throws Exception {
		createSuggestion();
		Thread.sleep(3000);
		String path = basePath + "/read/" + contentId + "?status=new&start=2018-02-02T17:49:43&end=2099-02-02T17:49:43";
		actions = mockMvc.perform(
				MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON).header("user-id", "ilimi"));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * List Suggestion with Valid URI, Valid Request Body Expect : 200 - OK
	 */
	@Test
	public void testSuggestions_09() throws Exception {
		String path = basePath + "/list";
		String createReq = "{\"request\": {\"content\": {\"status\": \"new\",\"suggestedBy\":\"User001\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("user-id", "ilimi").header("X-Channel-Id", "channelTest").content(createReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * List Suggestion with Valid URI, Valid Request Body (with suggestion id)
	 * Expect : 200 - OK
	 */
	@Test
	public void testSuggestions_10() throws Exception {
		String path = basePath + "/list";
		String createReq = "{\"request\": {\"content\": {\"status\": \"new\",\"suggestedBy\":\"User001\",\"suggestion_id\":\""
				+ suggestionId + "\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("user-id", "ilimi").header("X-Channel-Id", "channelTest").content(createReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Approve Suggestion with Valid URI, Valid Request Body. Expect : 200 - OK
	 */
	@Test
	public void testSuggestions_11() throws Exception {
		createSuggestion();
		String path = basePath + "/approve/" + suggestionId;
		String createReq = "{\"request\": {\"content\": {\"comments\": [\"suggestion applicable\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("user-id", "ilimi").header("X-Channel-Id", "channelTest").content(createReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Approve Suggestion with Valid URI, Valid Request Body (duplicate request
	 * to approve) Id. Expect : 400 - CLIENT_ERROR
	 */
	@Test
	public void testSuggestions_12() throws Exception {
		createSuggestion();
		String path = basePath + "/approve/" + suggestionId;
		String createReq = "{\"request\": {\"content\": {\"comments\": [\"suggestion applicable\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("user-id", "ilimi").header("X-Channel-Id", "channelTest").content(createReq));
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("user-id", "ilimi").header("X-Channel-Id", "channelTest").content(createReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Reject Suggestion with Valid URI, Valid Request Body. Expect : 200 - OK
	 */
	@Test
	public void testSuggestions_13() throws Exception {
		createSuggestion();
		String path = basePath + "/reject/" + suggestionId;
		String rejectReq = "{\"request\": {\"content\": {\"status\":\"reject\",\"comments\": [\"suggestion not applicable\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("user-id", "ilimi").header("X-Channel-Id", "channelTest").content(rejectReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Reject Suggestion with Valid URI, Valid Request Body. Duplicate Reject
	 * Request. Expect : 400 - CLIENT_ERROR
	 */
	@Test
	public void testSuggestions_14() throws Exception {
		createSuggestion();
		String path = basePath + "/reject/" + suggestionId;
		String rejectReq = "{\"request\": {\"content\": {\"status\":\"reject\",\"comments\": [\"suggestion not applicable\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("user-id", "ilimi").header("X-Channel-Id", "channelTest").content(rejectReq));
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("user-id", "ilimi").header("X-Channel-Id", "channelTest").content(rejectReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	private Response jsonToObject(ResultActions actions) {
		String content = null;
		Response resp = null;
		try {
			content = actions.andReturn().getResponse().getContentAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			if (StringUtils.isNotBlank(content))
				resp = objectMapper.readValue(content, Response.class);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

}
