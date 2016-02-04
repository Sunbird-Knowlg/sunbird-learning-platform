package org.ekstep.language.indexestest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.language.util.ElasticSearchUtil;
import org.junit.Assert;
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

import com.ilimi.common.dto.Response;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class LanguageIndexTest {

	@Autowired
	private WebApplicationContext context;
	private ObjectMapper mapper = new ObjectMapper();
	private ResultActions actions;

	static {
		LanguageRequestRouterPool.init();
	}
	
	@Test
	public void testLanguageIndexAPI() throws JsonParseException, JsonMappingException, IOException, InterruptedException{
		ElasticSearchUtil util = new ElasticSearchUtil();
		util.deleteIndex("citation_index_test");
		util.deleteIndex("word_index_test");
		util.deleteIndex("word_info_index_test");
		loadCitations(util);
		Thread.sleep(20000);
		getRootWords();
		getWordIds();
		wordWildCard();
		getWordMetrics();
		getIndexInfo();
		getMorphologicalVariants();
		getCitations();
		getCitationsCount();
		getWordInfo();
		getRootWordInfo();
		util.deleteIndex("citation_index_test");
		util.deleteIndex("word_index_test");
		util.deleteIndex("word_info_index_test");
	}
	
	public void addWordIndex() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"words\":[{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"id\":\"ka_4421\"},{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"id\":\"ka_4367\"}]}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/addWordIndex/test";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}
	
	public void addCitationIndex() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"citations\":[{\"word\":\"ಪೂರ್ವ\",\"rootWord\":\"ಪೂರ್ವ\",\"pos\":\"NST\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಶಾಲೆ\",\"rootWord\":\"ಶಾಲೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಆಟ\",\"rootWord\":\"ಆಟ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಅಳಿಲು\",\"rootWord\":\"ಅಳಿಲು\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಗಂಟೆ\",\"rootWord\":\"ಗಂಟೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಕೋತಿ\",\"rootWord\":\"ಕೋತಿ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"pos\":\"VM\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಪಾಠ\",\"rootWord\":\"ಪಾಠ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಚಟುವಟಿಕೆ\",\"rootWord\":\"ಚಟುವಟಿಕೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹುಡುಗ\",\"rootWord\":\"ಹುಡುಗ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹುಡುಗಿ\",\"rootWord\":\"ಹುಡುಗಿ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಕೇಳಿಸಿ\",\"rootWord\":\"ಕೇಳಿಸು\",\"pos\":\"VM\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಪಾಠ\",\"rootWord\":\"ಪಾಠ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"}]}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/addCitationIndex/test";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		addWordIndex();
	}

	public void loadCitations(ElasticSearchUtil util) throws JsonParseException,
			JsonMappingException, IOException {
		URL filePath = this.getClass().getClassLoader().getResource("testSsf.txt");
		String str = filePath.getPath();
		String contentString = "{\"request\":{\"file_path\":\""+str+"\",\"source_type\":\"textbooks\",\"grade\":\"1\",\"source\":\"Class1\"}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/loadCitations/test";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		util.deleteIndex("citation_index_test");
		util.deleteIndex("word_index_test");
		addCitationIndex();
		addWordIndex();
	}
	
	@SuppressWarnings("unchecked")
	public void getRootWords() throws JsonParseException, JsonMappingException,
			IOException {
		Map<String, String> wordtoRootMap = new HashMap<String, String>();
		wordtoRootMap.put("ಮರ", "ಮರ");
		wordtoRootMap.put("ಹೇಳಿಸಿ", "ಹೇಳಿಸು");
		JSONBuilder builder = new JSONStringer();
		builder.object().key("request").object().key("words").array()
				.value("ಮರ").value("ಹೇಳಿಸಿ").endArray().key("limit")
				.value(1000).endObject().endObject();
		String contentString = builder.toString();
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/getRootWords/test";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		Map<String, Object> result = resp.getResult();
		Map<String, Object> rootWords = (Map<String, Object>) result
				.get("root_words");
		for (Map.Entry<String, String> entry : wordtoRootMap.entrySet()) {
			Map<String, Object> word = (Map<String, Object>) rootWords
					.get(entry.getKey());
			String rootword = (String) word.get("rootWord");
			Assert.assertEquals(entry.getValue(), rootword);
		}
	}

	@SuppressWarnings("unchecked")
	public void getWordIds() throws JsonParseException, JsonMappingException,
			IOException {
		Map<String, String> wordIdMap = new HashMap<String, String>();
		wordIdMap.put("ಮರ", "ka_4421");
		wordIdMap.put("ಹೇಳಿಸಿ", "ka_4367");

		JSONBuilder builder = new JSONStringer();
		builder.object().key("request").object().key("words").array()
				.value("ಮರ").value("ಹೇಳಿಸಿ").endArray().key("limit")
				.value(1000).endObject().endObject();
		String contentString = builder.toString();
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/getWordId/test";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		Map<String, Object> result = resp.getResult();
		Map<String, Object> wordIds = (Map<String, Object>) result
				.get("word_ids");
		for (Map.Entry<String, String> entry : wordIdMap.entrySet()) {
			Map<String, Object> word = (Map<String, Object>) wordIds.get(entry
					.getKey());
			String wordId = (String) word.get("wordId");
			Assert.assertEquals(entry.getValue(), wordId);
		}
	}

	@SuppressWarnings("unchecked")
	public void getIndexInfo() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{ \"request\": {   \"words\": [\"ಮರ\", \"ಹೇಳಿಸಿ\"] ,   \"groupBy\" : [\"pos\", \"sourceType\"],   \"limit\": 1000 }}";
		String expectedResult = "{\"ಮರ\":{\"rootWord\":\"ಮರ\",\"citations\":{\"pos\":{\"nn\":1},\"sourceType\":{\"textbooks\":1},\"count\":1},\"wordId\":\"ka_4421\"},\"ಹೇಳಿಸಿ\":{\"rootWord\":\"ಹೇಳಿಸು\",\"citations\":{\"pos\":{\"vm\":1},\"sourceType\":{\"textbooks\":1},\"count\":1},\"wordId\":\"ka_4367\"}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/getIndexInfo/test";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		Map<String, Object> result = resp.getResult();
		Map<String, Object> indexInfo = (Map<String, Object>) result
				.get("index_info");
		String resultString = mapper.writeValueAsString(indexInfo);
		Assert.assertEquals(expectedResult, resultString);
	}

	@SuppressWarnings("unchecked")
	public void wordWildCard() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"word\":\"ಮ*\",\"limit\":1000}}";
		String expectedResult = "[{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"wordIdentifier\":\"ka_4421\"}]";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/wordWildCard/test";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		Map<String, Object> result = resp.getResult();
		ArrayList<Map<String, Object>> indexInfo = (ArrayList<Map<String, Object>>) result
				.get("words");
		String resultString = mapper.writeValueAsString(indexInfo);
		Assert.assertEquals(expectedResult, resultString);
	}

	@SuppressWarnings("unchecked")
	public void getMorphologicalVariants() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[\"ಮರ\",\"ಹೇಳಿಸಿ\"],\"limit\":1000}}";
		String expectedResult = "{\"ಮರ\":[\"ಮರ\"],\"ಹೇಳಿಸು\":[\"ಹೇಳಿಸು\",\"ಹೇಳಿಸಿ\"]}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/morphologicalVariants/test";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		Map<String, Object> result = resp.getResult();
		Map<String, Object> variants = (Map<String, Object>) result
				.get("morphological_variants");
		String resultString = mapper.writeValueAsString(variants);
		Assert.assertEquals(expectedResult, resultString);
	}

	@SuppressWarnings("unchecked")
	public void getCitationsCount() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[\"ಮರ\",\"ಹೇಳಿಸಿ\"],\"groupBy\":[\"pos\",\"sourceType\"],\"limit\":1000}}";
		String expectedResult = "{\"ಮರ\":{\"pos\":{\"nn\":1},\"sourceType\":{\"textbooks\":1},\"count\":1},\"ಹೇಳಿಸಿ\":{\"pos\":{\"vm\":1},\"sourceType\":{\"textbooks\":1},\"count\":1}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/citationsCount/test";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		Map<String, Object> result = resp.getResult();
		Map<String, Object> citationCount = (Map<String, Object>) result
				.get("citation_count");
		String resultString = mapper.writeValueAsString(citationCount);
		Assert.assertEquals(expectedResult, resultString);
	}

	@SuppressWarnings("unchecked")
	public void getWordMetrics() throws JsonParseException,
			JsonMappingException, IOException {
		String expectedResult = "{\"sourceType\":{\"textbooks\":13},\"pos\":{\"nn\":10,\"vm\":2,\"nst\":1},\"grade\":{\"1\":13},\"source\":{\"Class1\":13}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/getWordMetrics/test";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path)
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		Map<String, Object> result = resp.getResult();
		Map<String, Object> wordMetrics = (Map<String, Object>) result
				.get("word_metrics");
		String resultString = mapper.writeValueAsString(wordMetrics);
		Assert.assertEquals(expectedResult, resultString);
	}

	
	@SuppressWarnings("unchecked")
	public void getCitations() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[\"ಮರ\",\"ಹೇಳಿಸಿ\"],\"sourceType\":\"textbooks\",\"limit\":1000}}";
		String expectedResult = "{\"ಮರ\":[{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"}],\"ಹೇಳಿಸಿ\":[{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"pos\":\"VM\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"}]}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/citations/test";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		Map<String, Object> result = resp.getResult();
		Map<String, Object> citations = (Map<String, Object>) result
				.get("citations");
		String resultString = mapper.writeValueAsString(citations);
		Assert.assertEquals(expectedResult, resultString);
	}

	@SuppressWarnings("unchecked")
	public void getWordInfo() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[\"ಮರ\",\"ಹೇಳಿಸಿ\"],\"limit\":1000}}";
		String expectedResult = "{\"ಮರ\":{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"pos\":\"NN\",\"category\":\"n\",\"gender\":\"n\",\"number\":\"sg\",\"pers\":\"\",\"grammaticalCase\":\"d\",\"inflection\":\"\",\"rts\":\"\"},\"ಹೇಳಿಸು\":{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"pos\":\"VM\",\"category\":\"v\",\"gender\":\"\",\"number\":\"pl\",\"pers\":\"2\",\"grammaticalCase\":\"\",\"inflection\":\"\",\"rts\":\"\"}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/wordInfo/test";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		Map<String, Object> result = resp.getResult();
		Map<String, Object> wordInfo = (Map<String, Object>) result
				.get("word_info");
		String resultString = mapper.writeValueAsString(wordInfo);
		Assert.assertEquals(expectedResult, resultString);
	}
	
	@SuppressWarnings("unchecked")
	public void getRootWordInfo() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[\"ಮರ\",\"ಹೇಳಿಸಿ\"],\"limit\":1000}}";
		String expectedResult = "{\"ಮರ\":[{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"pos\":\"NN\",\"category\":\"n\",\"gender\":\"n\",\"number\":\"sg\",\"pers\":\"\",\"grammaticalCase\":\"d\",\"inflection\":\"\",\"rts\":\"\"}]}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/rootWordInfo/test";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		Map<String, Object> result = resp.getResult();
		Map<String, Object> rootWordInfo = (Map<String, Object>) result
				.get("root_word_info");
		String resultString = mapper.writeValueAsString(rootWordInfo);
		Assert.assertEquals(expectedResult, resultString);
	}
	
	public Response jsonToObject(ResultActions actions) {
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
