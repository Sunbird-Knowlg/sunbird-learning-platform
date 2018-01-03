package org.ekstep.language.controllerstest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.language.common.BaseLanguageTest;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.parser.SSFParser;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.language.test.util.RequestResponseTestHelper;
import org.ekstep.language.util.ElasticSearchUtil;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class LanguageIndexTest extends BaseLanguageTest{

	@Autowired
	private WebApplicationContext context;
    
	private static ObjectMapper mapper = new ObjectMapper();
	private ResultActions actions;
	static ElasticSearchUtil util;
	
	//private static String TEST_LANGUAGE = "testone";
//	private static String TEST_LOAD_LANGUAGE = "testoneload";

	static {
		TEST_LANGUAGE = "test";
		LanguageRequestRouterPool.init();
	}

	@BeforeClass
	public static void initIndexTest() throws Exception {
		
		util = new ElasticSearchUtil();
		util.deleteIndex("citation_index_" + TEST_LANGUAGE);
		util.deleteIndex("word_index_" + TEST_LANGUAGE);
		util.deleteIndex("word_info_index_" + TEST_LANGUAGE);
//		util.deleteIndex("citation_index_" + TEST_LOAD_LANGUAGE);
//		util.deleteIndex("word_index_" + TEST_LOAD_LANGUAGE);
//		util.deleteIndex("word_info_index_" + TEST_LOAD_LANGUAGE);
		loadCitations();
		Thread.sleep(5000);
		util.deleteIndex("citation_index_" + TEST_LANGUAGE);
		util.deleteIndex("word_index_" + TEST_LANGUAGE);
		addCitationIndex();
		addWordIndex();
		Thread.sleep(5000);
	}


	@AfterClass
	public static void close() throws IOException, InterruptedException {
		util.deleteIndex("citation_index_" + TEST_LANGUAGE);
		util.deleteIndex("word_index_" + TEST_LANGUAGE);
		util.deleteIndex("word_info_index_" + TEST_LANGUAGE);
//		util.deleteIndex("citation_index_" + TEST_LOAD_LANGUAGE);
//		util.deleteIndex("word_index_" + TEST_LOAD_LANGUAGE);
//		util.deleteIndex("word_info_index_" + TEST_LOAD_LANGUAGE);
		
	}

	@SuppressWarnings("unused")
	public static void addWordIndex() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"id\":\"ka_4421\"},{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"id\":\"ka_4367\"}]}}";

		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});

		String apiId = "wordIndex.add";
		Request request = RequestResponseTestHelper.getRequestObject(map);

		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.addWordIndex.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		TelemetryManager.log("List | Request: " , request);
		try {
			Response response = RequestResponseTestHelper.getResponse(request);
			TelemetryManager.log("List | Response: " , response);
			ResponseEntity<Response> responseEntity = RequestResponseTestHelper
					.getResponseEntity(response, apiId, (null != request
							.getParams()) ? request.getParams().getMsgid()
							: null);
			TelemetryManager.log("List | Response: " , response);
		} catch (Exception e) {
			TelemetryManager.log("List | Exception: " , e.getMessage(), e);
			ResponseEntity<Response> responseEntity = RequestResponseTestHelper
					.getExceptionResponseEntity(e, apiId, (null != request
							.getParams()) ? request.getParams().getMsgid()
							: null);
			TelemetryManager.log("List | Response: " , responseEntity);
		}
	}

	public static void addCitationIndex() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"citations\":[{\"word\":\"ಪೂರ್ವ\",\"rootWord\":\"ಪೂರ್ವ\",\"pos\":\"NST\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಶಾಲೆ\",\"rootWord\":\"ಶಾಲೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಆಟ\",\"rootWord\":\"ಆಟ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಅಳಿಲು\",\"rootWord\":\"ಅಳಿಲು\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಗಂಟೆ\",\"rootWord\":\"ಗಂಟೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಕೋತಿ\",\"rootWord\":\"ಕೋತಿ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"pos\":\"VM\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಪಾಠ\",\"rootWord\":\"ಪಾಠ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಚಟುವಟಿಕೆ\",\"rootWord\":\"ಚಟುವಟಿಕೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹುಡುಗ\",\"rootWord\":\"ಹುಡುಗ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹುಡುಗಿ\",\"rootWord\":\"ಹುಡುಗಿ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಕೇಳಿಸಿ\",\"rootWord\":\"ಕೇಳಿಸು\",\"pos\":\"VM\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಪಾಠ\",\"rootWord\":\"ಪಾಠ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"}]}}";
		String apiId = "citation.add";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = RequestResponseTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.addCitationIndex.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		TelemetryManager.log("List | Request: " , request);
		try {
			Response response = RequestResponseTestHelper
					.getBulkOperationResponse(request);
			TelemetryManager.log("List | Response: " , response);
			RequestResponseTestHelper.getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams()
							.getMsgid() : null);
		} catch (Exception e) {
			TelemetryManager.log("List | Exception: " , e.getMessage(), e);
			RequestResponseTestHelper.getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams()
							.getMsgid() : null);
		}
	}

	@SuppressWarnings("unused")
	public static boolean loadCitations() throws Exception {
		String apiId = "citations.load";
		URL filePath = LanguageIndexTest.class.getClassLoader().getResource(
				"testSsf.txt");
		String str = filePath.getPath();
		String contentString = "{\"request\":{\"file_path\":\""
				+ str
				+ "\",\"source_type\":\"textbooks\",\"grade\":\"1\",\"source\":\"Class1\"}}";

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		try {
			SSFParser.parseSsfFiles(str, "textbooks", "class1", "1", false, ""
					+ TEST_LANGUAGE);
		} catch (Exception e) {
			SSFParser.parseSsfFiles(str, "textbooks", "class1", "1", false, ""
					+ TEST_LANGUAGE);
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Test
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
		String path = "/v1/language/indexes/getRootWords/" + TEST_LANGUAGE;
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
	@Test
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
		String path = "/v1/language/indexes/getWordId/" + TEST_LANGUAGE;
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
	@Test
	public void getIndexInfo() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{ \"request\": {   \"words\": [\"ಮರ\", \"ಹೇಳಿಸಿ\"] ,   \"groupBy\" : [\"pos\", \"sourceType\"],   \"limit\": 1000 }}";
		String expectedResult = "{\"ಮರ\":{\"rootWord\":\"ಮರ\",\"citations\":{\"pos\":{\"NN\":1},\"sourceType\":{\"textbooks\":1},\"count\":1},\"wordId\":\"ka_4421\"},\"ಹೇಳಿಸಿ\":{\"rootWord\":\"ಹೇಳಿಸು\",\"citations\":{\"pos\":{\"VM\":1},\"sourceType\":{\"textbooks\":1},\"count\":1},\"wordId\":\"ka_4367\"}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/getIndexInfo/" + TEST_LANGUAGE;
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

	/*	@Test
	public void loadCitationsTest() throws JsonParseException,
			JsonMappingException, IOException {
		URL filePath = LanguageIndexTest.class.getClassLoader().getResource(
				"testSsf.txt");
		String str = filePath.getPath();
		String contentString = "{\"request\":{\"file_path\":\""
				+ str
				+ "\",\"source_type\":\"textbooks\",\"grade\":\"1\",\"source\":\"Class1\"}}";

		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/loadCitations/"
				+ TEST_LOAD_LANGUAGE;
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
	
	@Test
	public void loadCitationsErrorTest() throws JsonParseException,
			JsonMappingException, IOException {
		URL filePath = LanguageIndexTest.class.getResource(
				"/testData/");
		
		String str = filePath.getPath();
		String contentString = "{\"request\":{\"file_path\":\""
				+ str
				+ "\",\"source_type\":\"textbooks\",\"grade\":\"1\",\"source\":\"Class1\"}}";

		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/loadCitations/"
				+ TEST_LOAD_LANGUAGE;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertNotEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}

	@Test
	public void addCitationsTest() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"citations\":[{\"word\":\"ಪೂರ್ವ\",\"rootWord\":\"ಪೂರ್ವ\",\"pos\":\"NST\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಶಾಲೆ\",\"rootWord\":\"ಶಾಲೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಆಟ\",\"rootWord\":\"ಆಟ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಅಳಿಲು\",\"rootWord\":\"ಅಳಿಲು\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಗಂಟೆ\",\"rootWord\":\"ಗಂಟೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಕೋತಿ\",\"rootWord\":\"ಕೋತಿ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"pos\":\"VM\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಪಾಠ\",\"rootWord\":\"ಪಾಠ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಚಟುವಟಿಕೆ\",\"rootWord\":\"ಚಟುವಟಿಕೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹುಡುಗ\",\"rootWord\":\"ಹುಡುಗ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹುಡುಗಿ\",\"rootWord\":\"ಹುಡುಗಿ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಕೇಳಿಸಿ\",\"rootWord\":\"ಕೇಳಿಸು\",\"pos\":\"VM\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಪಾಠ\",\"rootWord\":\"ಪಾಠ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"}]}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/addCitationIndex/"
				+ TEST_LOAD_LANGUAGE;
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

	@Test
	public void addWordIndexTest() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"id\":\"ka_4421\"},{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"id\":\"ka_4367\"}]}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/addWordIndex/" + TEST_LOAD_LANGUAGE;
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
	}*/

	@SuppressWarnings("unchecked")
	@Test
	public void wordWildCard() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"word\":\"ಮ*\",\"limit\":1000}}";
		String expectedResult = "[{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"wordIdentifier\":\"ka_4421\"}]";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/wordWildCard/" + TEST_LANGUAGE;
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
	@Test
	public void getMorphologicalVariants() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[\"ಮರ\",\"ಹೇಳಿಸಿ\"],\"limit\":1000}}";
		String expectedResult = "{\"ಮರ\":[\"ಮರ\"],\"ಹೇಳಿಸು\":[\"ಹೇಳಿಸು\",\"ಹೇಳಿಸಿ\"]}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/morphologicalVariants/"
				+ TEST_LANGUAGE;
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
	@Test
	public void getCitationsCount() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[\"ಮರ\",\"ಹೇಳಿಸಿ\"],\"groupBy\":[\"pos\",\"sourceType\"],\"limit\":1000}}";
		String expectedResult = "{\"ಮರ\":{\"pos\":{\"NN\":1},\"sourceType\":{\"textbooks\":1},\"count\":1},\"ಹೇಳಿಸಿ\":{\"pos\":{\"VM\":1},\"sourceType\":{\"textbooks\":1},\"count\":1}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/citationsCount/" + TEST_LANGUAGE;
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
	@Test
	public void getWordMetrics() throws JsonParseException,
			JsonMappingException, IOException {
		String expectedResult = "{\"sourceType\":{\"textbooks\":13},\"pos\":{\"NN\":10,\"VM\":2,\"NST\":1},\"grade\":{\"1\":13},\"source\":{\"Class1\":13}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/getWordMetrics/" + TEST_LANGUAGE;
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
		Map<String, Object> result = resp.getResult();
		Map<String, Object> wordMetrics = (Map<String, Object>) result
				.get("word_metrics");
		String resultString = mapper.writeValueAsString(wordMetrics);
		Assert.assertEquals(expectedResult, resultString);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getCitations() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"words\":[\"ಮರ\",\"ಹೇಳಿಸಿ\"],\"sourceType\":\"textbooks\",\"limit\":1000}}";
		String expectedResult = "{\"ಮರ\":[{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"}],\"ಹೇಳಿಸಿ\":[{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"pos\":\"VM\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"}]}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/citations/" + TEST_LANGUAGE;
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
	@Test
	public void getWordInfo() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"words\":[\"ಮರ\",\"ಹೇಳಿಸಿ\"],\"limit\":1000}}";
		String expectedResult = "{\"ಮರ\":{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"pos\":\"NN\",\"category\":\"n\",\"gender\":\"n\",\"number\":\"sg\",\"pers\":\"\",\"grammaticalCase\":\"d\",\"inflection\":\"\",\"rts\":\"\"},\"ಹೇಳಿಸಿ\":{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"pos\":\"VM\",\"category\":\"v\",\"gender\":\"\",\"number\":\"pl\",\"pers\":\"2\",\"grammaticalCase\":\"\",\"inflection\":\"\",\"rts\":\"\"}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/wordInfo/" + TEST_LANGUAGE;
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
	@Test
	public void getRootWordInfo() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[\"ಮರ\",\"ಹೇಳಿಸಿ\"],\"limit\":1000}}";
		String expectedResult = "{\"ಮರ\":[{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"pos\":\"NN\",\"category\":\"n\",\"gender\":\"n\",\"number\":\"sg\",\"pers\":\"\",\"grammaticalCase\":\"d\",\"inflection\":\"\",\"rts\":\"\"}]}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/indexes/rootWordInfo/" + TEST_LANGUAGE;
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
