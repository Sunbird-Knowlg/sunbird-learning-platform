package org.ekstep.language.controllerstest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
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

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class LanguageWordV3Test extends GraphEngineTestSetup{

	@Autowired
	private WebApplicationContext context;
	private ResultActions actions;
	private static boolean init = false;
	private static String wordId = "";
	private final static String TEST_LANGUAGE = "en";
	private final static String TEST_COMMON_LANGUAGE = "language";


	static {
		LanguageRequestRouterPool.init();
	}

	@BeforeClass
	public static void createDefinition() throws Exception {
		loadDefinitionByGraphId(TEST_LANGUAGE, "definitions/PhoneticBoundaryDefinition.json", "definitions/SynsetDefinition.json", "definitions/TraversalRuleDefinition.json", "definitions/VarnaDefinition.json", "definitions/WordDefinitionNode.json", "definitions/wordset_definition.json");
		loadDefinitionByGraphId(TEST_COMMON_LANGUAGE, "definitions/language/GradeLevelComplexity.json", "definitions/language/WordComplexityDefinition.json");
	}

	@Before
	public void initDictionary() throws Exception {
		if(!init) {
			createWordTest();
			init= true;
		}
	}

	@SuppressWarnings("rawtypes")
	public void createWordTest() throws JsonParseException,
			JsonMappingException, IOException {
		String wordReqString = "{\"request\":{\"words\":[{\"lemma\":\"newtestword\",\"primaryMeaning\":{\"identifier\":\"202707688\",\"gloss\":\"meaning1\",\"category\":\"Person\",\"exampleSentences\":[\"es11\",\"es12\"],\"synonyms\":[{\"lemma\":\"newsynonym\"}]},\"status\":\"Draft\"}]}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v3/words/create?language_id=" + TEST_LANGUAGE;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(wordReqString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		List nodeIds = (List) result.get("node_ids");
		wordId = (String)nodeIds.get(0);
	}

	
	@Test
	public void createDuplicateWordTest() throws JsonParseException,
			JsonMappingException, IOException {
		String wordReqString = "{\"request\":{\"words\":[{\"lemma\":\"newtestword\",\"primaryMeaning\":{\"identifier\":\"202707688\",\"gloss\":\"meaning1\",\"category\":\"Person\",\"exampleSentences\":[\"es11\",\"es12\"],\"synonyms\":[{\"lemma\":\"newsynonym\"}]},\"status\":\"Draft\"}]}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v3/words/create?language_id=" + TEST_LANGUAGE;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(wordReqString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(400, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void listWordsTest() throws JsonParseException,
			JsonMappingException, IOException {
		
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v3/words/list?language_id=" + TEST_LANGUAGE;
				
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header(
					"user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		List<Object> words = (List<Object>) result.get("words");
		Assert.assertNotNull(words);
	}
	
	@Test
	public void updateWordTest() throws JsonParseException,
			JsonMappingException, IOException {
		
		String updateWordRequest = "{\"request\":{\"word\":{\"primaryMeaning\":{\"identifier\":\"202707688\",\"gloss\":\"meanig changed\"}}}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v3/words/update/"+wordId+"?language_id=" + TEST_LANGUAGE;
				
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.patch(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateWordRequest.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		List<String> nodeIds = (List<String>) result.get("node_ids");
		String nodeId = (String) nodeIds.get(0);
		Assert.assertEquals(wordId, nodeId);
		
		path = "/v3/words/read/" +wordId +"?language_id=en";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header(
					"user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		result = resp.getResult();
		Map<String, Object> wordNode = (Map<String, Object>) result
				.get("Word");
		Assert.assertNotNull(wordNode);
		List<Map<String, Object>> synsets= (List<Map<String, Object>>)wordNode.get("synsets");
		Assert.assertNotNull(synsets);
		Map<String,Object> synset = synsets.get(0);
		String gloss = (String)synset.get("gloss");
		Assert.assertEquals(gloss, "meanig changed");
	}
	
	public static Response jsonToObject(ResultActions actions) {
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
