package org.ekstep.language.indexesactortest;

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
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.index.common.BaseLanguageTest;
import org.ekstep.language.parser.SSFParser;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.language.util.ElasticSearchUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

@Ignore
public class LanguageIndexesActorTest extends BaseLanguageTest{

	private WebApplicationContext context;
//	private static TaxonomyManagerImpl taxonomyManager = new TaxonomyManagerImpl();
	private static ObjectMapper mapper = new ObjectMapper();
	private ResultActions actions;
	static ElasticSearchUtil util;
	
//	private static String TEST_LANGUAGE = "test";
//	private static String TEST_LOAD_LANGUAGE = "testload";

	static {
		TEST_LANGUAGE = "test";
		LanguageRequestRouterPool.init();
	}

	@SuppressWarnings("unused")
	@BeforeClass
	public static void initIndex() throws Exception {
		// Definitions
//		deleteDefinitionStatic(TEST_LANGUAGE);
//		deleteDefinitionStatic(TEST_LOAD_LANGUAGE);
//		createDefinitionsStatic(TEST_LANGUAGE);
//		createDefinitionsStatic(TEST_LOAD_LANGUAGE);

		util = new ElasticSearchUtil();
		util.deleteIndex("citation_index_" + TEST_LANGUAGE);
		util.deleteIndex("word_index_" + TEST_LANGUAGE);
		util.deleteIndex("word_info_index_" + TEST_LANGUAGE);
//		util.deleteIndex("citation_index_" + TEST_LOAD_LANGUAGE);
//		util.deleteIndex("word_index_" + TEST_LOAD_LANGUAGE);
//		util.deleteIndex("word_info_index_" + TEST_LOAD_LANGUAGE);
		boolean response = loadCitations();
		Thread.sleep(5000);
		util.deleteIndex("citation_index_" + TEST_LANGUAGE);
		util.deleteIndex("word_index_" + TEST_LANGUAGE);
		addCitationIndex();
		addWordIndex();
		Thread.sleep(5000);
	}

	public void testLanguageIndexAPI() throws JsonParseException,
			JsonMappingException, IOException, InterruptedException {
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
	}

	@AfterClass
	public static void close() throws IOException, InterruptedException {
		util.deleteIndex("citation_index_" + TEST_LANGUAGE);
		util.deleteIndex("word_index_" + TEST_LANGUAGE);
		util.deleteIndex("word_info_index_" + TEST_LANGUAGE);
//		util.deleteIndex("citation_index_" + TEST_LOAD_LANGUAGE);
//		util.deleteIndex("word_index_" + TEST_LOAD_LANGUAGE);
//		util.deleteIndex("word_info_index_" + TEST_LOAD_LANGUAGE);

//		deleteDefinitionStatic(TEST_LANGUAGE);
//		deleteDefinitionStatic(TEST_LOAD_LANGUAGE);
	}

	@SuppressWarnings("unused")
	public static void addWordIndex() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[{\"word\":\"ಮರ\",\"id\":\"ka_4421\"},{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"id\":\"ka_4367\"}]}}";

		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});

		String apiId = "wordIndex.add";
		Request request = LanguageIndexesTestHelper.getRequestObject(map);

		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.addWordIndex.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		try {
			Response response = LanguageIndexesTestHelper.getResponse(request);
			PlatformLogger.log("List | Response: " + response);
			ResponseEntity<Response> responseEntity = LanguageIndexesTestHelper
					.getResponseEntity(response, apiId, (null != request
							.getParams()) ? request.getParams().getMsgid()
							: null);
			PlatformLogger.log("List | Response: " + response);
		} catch (Exception e) {
			PlatformLogger.log("List | Exception: " + e.getMessage(), e);
			ResponseEntity<Response> responseEntity = LanguageIndexesTestHelper
					.getExceptionResponseEntity(e, apiId, (null != request
							.getParams()) ? request.getParams().getMsgid()
							: null);
			PlatformLogger.log("List | Response: " + responseEntity);
		}
	}

	public static void addCitationIndex() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"citations\":[{\"word\":\"ಪೂರ್ವ\",\"rootWord\":\"ಪೂರ್ವ\",\"pos\":\"NST\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಶಾಲೆ\",\"rootWord\":\"ಶಾಲೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಆಟ\",\"rootWord\":\"ಆಟ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಅಳಿಲು\",\"rootWord\":\"ಅಳಿಲು\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಗಂಟೆ\",\"rootWord\":\"ಗಂಟೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಕೋತಿ\",\"rootWord\":\"ಕೋತಿ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"pos\":\"VM\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಪಾಠ\",\"rootWord\":\"ಪಾಠ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಚಟುವಟಿಕೆ\",\"rootWord\":\"ಚಟುವಟಿಕೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹುಡುಗ\",\"rootWord\":\"ಹುಡುಗ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹುಡುಗಿ\",\"rootWord\":\"ಹುಡುಗಿ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಕೇಳಿಸಿ\",\"rootWord\":\"ಕೇಳಿಸು\",\"pos\":\"VM\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಪಾಠ\",\"rootWord\":\"ಪಾಠ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"}]}}";
		String apiId = "citation.add";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.addCitationIndex.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		try {
			Response response = LanguageIndexesTestHelper
					.getBulkOperationResponse(request);
			PlatformLogger.log("List | Response: " + response);
			LanguageIndexesTestHelper.getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams()
							.getMsgid() : null);
		} catch (Exception e) {
			PlatformLogger.log("List | Exception: " + e.getMessage(), e);
			LanguageIndexesTestHelper.getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams()
							.getMsgid() : null);
		}
	}

	@SuppressWarnings("unused")
	public static boolean loadCitations() throws Exception {
		String apiId = "citations.load";
		URL filePath = LanguageIndexesActorTest.class.getClassLoader()
				.getResource("testSsf.txt");
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
	//@Test
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

		String apiId = "rootWords.get";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.getRootWords.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = LanguageIndexesTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);

		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
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
	//@Test
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
		String apiId = "rootWords.get";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.getWordId.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = LanguageIndexesTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
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
	//@Test
	public void getIndexInfo() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{ \"request\": {   \"words\": [\"ಮರ\", \"ಹೇಳಿಸಿ\"] ,   \"groupBy\" : [\"pos\", \"sourceType\"],   \"limit\": 1000 }}";
		String expectedResult = "{\"ಮರ\":{\"rootWord\":\"ಮರ\",\"citations\":{\"pos\":{\"NN\":1},\"sourceType\":{\"textbooks\":1},\"count\":1},\"wordId\":\"ka_4421\"},\"ಹೇಳಿಸಿ\":{\"rootWord\":\"ಹೇಳಿಸು\",\"citations\":{\"pos\":{\"VM\":1},\"sourceType\":{\"textbooks\":1},\"count\":1},\"wordId\":\"ka_4367\"}}";
		String apiId = "wordIndexInfo.get";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.getIndexInfo.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = LanguageIndexesTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		Map<String, Object> indexInfo = (Map<String, Object>) result
				.get("index_info");
		String resultString = mapper.writeValueAsString(indexInfo);
		Assert.assertEquals(expectedResult, resultString);
	}

	//@Test
	public void loadIndexesTest() throws JsonParseException,
			JsonMappingException, IOException {
		URL filePath = LanguageIndexesActorTest.class.getClassLoader()
				.getResource("testSsf.txt");
		String str = filePath.getPath();
		String contentString = "{\"request\":{\"file_path\":\""
				+ str
				+ "\",\"source_type\":\"textbooks\",\"grade\":\"1\",\"source\":\"Class1\"}}";

		String apiId = "citation.load";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.loadCitations.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);//load
		PlatformLogger.log("List | Request: " + request);
		Response response;
		response = LanguageIndexesTestHelper.getBulkOperationResponse(request);
		PlatformLogger.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		Assert.assertEquals("successful", response.getParams().getStatus());
	}

	//@Test
	public void loadIndexesSkipCitationTest() throws JsonParseException,
			JsonMappingException, IOException {
		URL filePath = LanguageIndexesActorTest.class.getClassLoader()
				.getResource("testSsf.txt");
		String str = filePath.getPath();
		String contentString = "{\"request\":{\"file_path\":\""
				+ str
				+ "\",\"source_type\":\"textbooks\",\"grade\":\"1\",\"source\":\"Class1\",\"skipCitations\":true}}";

		String apiId = "citation.load";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.loadCitations.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);//load
		PlatformLogger.log("List | Request: " + request);
		Response response;
		response = LanguageIndexesTestHelper.getBulkOperationResponse(request);
		PlatformLogger.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		Assert.assertEquals("successful", response.getParams().getStatus());
	}
	
	
	//@Test
	public void addCitationsTest() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"citations\":[{\"word\":\"ಪೂರ್ವ\",\"rootWord\":\"ಪೂರ್ವ\",\"pos\":\"NST\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಶಾಲೆ\",\"rootWord\":\"ಶಾಲೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಆಟ\",\"rootWord\":\"ಆಟ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಅಳಿಲು\",\"rootWord\":\"ಅಳಿಲು\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಗಂಟೆ\",\"rootWord\":\"ಗಂಟೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಕೋತಿ\",\"rootWord\":\"ಕೋತಿ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"pos\":\"VM\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಪಾಠ\",\"rootWord\":\"ಪಾಠ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಚಟುವಟಿಕೆ\",\"rootWord\":\"ಚಟುವಟಿಕೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹುಡುಗ\",\"rootWord\":\"ಹುಡುಗ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹುಡುಗಿ\",\"rootWord\":\"ಹುಡುಗಿ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಕೇಳಿಸಿ\",\"rootWord\":\"ಕೇಳಿಸು\",\"pos\":\"VM\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಪಾಠ\",\"rootWord\":\"ಪಾಠ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"}]}}";
		String apiId = "citation.add";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.addCitationIndex.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);//load
		PlatformLogger.log("List | Request: " + request);
		Response response;
		response = LanguageIndexesTestHelper.getBulkOperationResponse(request);
		PlatformLogger.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		Assert.assertEquals("successful", response.getParams().getStatus());
	}

	@SuppressWarnings("unchecked")
	//@Test
	public void wordWildCard() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"word\":\"ಮ*\",\"limit\":1000}}";
		String expectedResult = "[{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"wordIdentifier\":\"ka_4421\"}]";
		String apiId = "wordIndexInfo.get";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.wordWildCard.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = LanguageIndexesTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		ArrayList<Map<String, Object>> indexInfo = (ArrayList<Map<String, Object>>) result
				.get("words");
		String resultString = mapper.writeValueAsString(indexInfo);
		Assert.assertEquals(expectedResult, resultString);
	}

	@SuppressWarnings("unchecked")
	//@Test
	public void getMorphologicalVariants() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[\"ಮರ\",\"ಹೇಳಿಸಿ\"],\"limit\":1000}}";
		String expectedResult = "{\"ಮರ\":[\"ಮರ\"],\"ಹೇಳಿಸು\":[\"ಹೇಳಿಸು\",\"ಹೇಳಿಸಿ\"]}";
		String apiId = "morphologicalVariants.get";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.morphologicalVariants.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = LanguageIndexesTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		Map<String, Object> variants = (Map<String, Object>) result
				.get("morphological_variants");
		String resultString = mapper.writeValueAsString(variants);
		Assert.assertEquals(expectedResult, resultString);
	}

	@SuppressWarnings("unchecked")
	//@Test
	public void getCitationsCount() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[\"ಮರ\",\"ಹೇಳಿಸಿ\"],\"groupBy\":[\"pos\",\"sourceType\"],\"limit\":1000}}";
		String expectedResult = "{\"ಮರ\":{\"pos\":{\"NN\":1},\"sourceType\":{\"textbooks\":1},\"count\":1},\"ಹೇಳಿಸಿ\":{\"pos\":{\"VM\":1},\"sourceType\":{\"textbooks\":1},\"count\":1}}";
		String apiId = "citationsCount.get";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.citationsCount.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = LanguageIndexesTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		Map<String, Object> citationCount = (Map<String, Object>) result
				.get("citation_count");
		String resultString = mapper.writeValueAsString(citationCount);
		Assert.assertEquals(expectedResult, resultString);
	}

	@SuppressWarnings("unchecked")
	//@Test
	public void getWordMetrics() throws JsonParseException,
			JsonMappingException, IOException {
		String expectedResult = "{\"sourceType\":{\"textbooks\":13},\"pos\":{\"NN\":10,\"VM\":2,\"NST\":1},\"grade\":{\"1\":13},\"source\":{\"Class1\":13}}";
		String apiId = "citationsCount.get";
		Request request = new Request();
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.getWordMetrics.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = LanguageIndexesTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		Map<String, Object> wordMetrics = (Map<String, Object>) result
				.get("word_metrics");
		String resultString = mapper.writeValueAsString(wordMetrics);
		Assert.assertEquals(expectedResult, resultString);
	}

	@SuppressWarnings("unchecked")
	//@Test
	public void getCitationsWithAllFilters() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"words\":[\"ಗಂಟೆ\"],\"source_type\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"pos\":\"NN\",\"file_name\":\"testSsf.txt\",\"limit\":1000}}";
		String expectedResult = "{\"ಗಂಟೆ\":[{\"word\":\"ಗಂಟೆ\",\"rootWord\":\"ಗಂಟೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"}]}";
		String apiId = "citations.get";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.citations.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = LanguageIndexesTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		Map<String, Object> citations = (Map<String, Object>) result
				.get("citations");
		String resultString = mapper.writeValueAsString(citations);
		Assert.assertEquals(expectedResult, resultString);
	}
	
	@SuppressWarnings("unchecked")
	//@Test
	public void getCitations() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"words\":[\"ಮರ\",\"ಹೇಳಿಸಿ\"],\"sourceType\":\"textbooks\",\"limit\":1000}}";
		String expectedResult = "{\"ಮರ\":[{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"}],\"ಹೇಳಿಸಿ\":[{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"pos\":\"VM\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"}]}";
		String apiId = "citations.get";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.citations.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = LanguageIndexesTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		Map<String, Object> citations = (Map<String, Object>) result
				.get("citations");
		String resultString = mapper.writeValueAsString(citations);
		Assert.assertEquals(expectedResult, resultString);
	}

	@SuppressWarnings("unchecked")
	//@Test
	public void getWordInfo() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"words\":[\"ಮರ\",\"ಹೇಳಿಸಿ\"],\"limit\":1000}}";
		String expectedResult = "{\"ಮರ\":{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"pos\":\"NN\",\"category\":\"n\",\"gender\":\"n\",\"number\":\"sg\",\"pers\":\"\",\"grammaticalCase\":\"d\",\"inflection\":\"\",\"rts\":\"\"},\"ಹೇಳಿಸಿ\":{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"pos\":\"VM\",\"category\":\"v\",\"gender\":\"\",\"number\":\"pl\",\"pers\":\"2\",\"grammaticalCase\":\"\",\"inflection\":\"\",\"rts\":\"\"}}";
		String apiId = "wordInfo.get";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.wordInfo.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = LanguageIndexesTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		Map<String, Object> wordInfo = (Map<String, Object>) result
				.get("word_info");
		String resultString = mapper.writeValueAsString(wordInfo);
		Assert.assertEquals(expectedResult, resultString);
	}

	@SuppressWarnings("unchecked")
	//@Test
	public void getRootWordInfo() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[\"ಮರ\",\"ಹೇಳಿಸಿ\"],\"limit\":1000}}";
		String expectedResult = "{\"ಮರ\":[{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"pos\":\"NN\",\"category\":\"n\",\"gender\":\"n\",\"number\":\"sg\",\"pers\":\"\",\"grammaticalCase\":\"d\",\"inflection\":\"\",\"rts\":\"\"}]}";
		String apiId = "rootWordInfo.get";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.rootWordInfo.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = LanguageIndexesTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
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

	public void createDefinitions(String language) {
		String contentString = "{  \"definitionNodes\": [    {      \"objectType\": \"Word\",      \"properties\": [        {          \"propertyName\": \"lemma\",          \"title\": \"Lemma\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": true,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 3 }\"        },        {          \"propertyName\": \"sources\",          \"title\": \"Sources\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 2 }\"        },        {          \"propertyName\": \"sourceTypes\",          \"title\": \"Source Types\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 2 }\"        },        {          \"propertyName\": \"commisionedBy\",          \"title\": \"Commisioned By\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 5 }\"        },        {          \"propertyName\": \"defaultSynset\",          \"title\": \"Default Synset\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 1 }\"        },        {          \"propertyName\": \"syllableCount\",          \"title\": \"Syllable Count\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Number\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 4 }\"        },        {          \"propertyName\": \"syllableNotation\",          \"title\": \"Syllable Notation\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 4 }\"        },        {          \"propertyName\": \"unicodeNotation\",          \"title\": \"Unicode Notation\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 4 }\"        },        {          \"propertyName\": \"rtsNotation\",          \"title\": \"RTS Notation\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 4 }\"        },        {          \"propertyName\": \"vectorsRepresentation\",          \"title\": \"Vectors representation\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'select',  'order': 9 }\"        },        {          \"propertyName\": \"orthographicFeatures\",          \"title\": \"Orthographic Features\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"\"        },        {          \"propertyName\": \"orthographic_complexity\",          \"title\": \"Orthographic Complexity\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Number\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea',  'order': 4 }\"        },        {          \"propertyName\": \"phonologic_complexity\",          \"title\": \"Phonological Complexity\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Number\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea',  'order': 4 }\"        },        {          \"propertyName\": \"pos\",          \"title\": \"POS (Parts of Speech)\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 19 }\"        },        {          \"propertyName\": \"grade\",          \"title\": \"Grade\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 19 }\"        },        {          \"propertyName\": \"morphology\",          \"title\": \"Morphology\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 4 }\"        },        {          \"propertyName\": \"parts\",          \"title\": \"Parts\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 4 }\"        },        {          \"propertyName\": \"affixes\",          \"title\": \"Affixes\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"List\",          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 14 }\"        },        {          \"propertyName\": \"namedEntityType\",          \"title\": \"Named Entity Type\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"List\",          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'select', 'order': 14 }\"        },        {          \"propertyName\": \"loanWordSourceLanguage\",          \"title\": \"Loan Word Source Language\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"Select\",          \"range\": [            \"hi\",            \"en\",            \"ka\",            \"te\"          ],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'select',  'order': 14 }\"        },        {          \"propertyName\": \"ageBand\",          \"title\": \"Age Band\",          \"description\": \"\",          \"category\": \"pedagogy\",          \"dataType\": \"Select\",          \"range\": [            \"1-5\",            \"6-10\",            \"11-15\",            \"16-20\"          ],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'select', 'order': 14 }\"        },        {          \"propertyName\": \"microConcepts\",          \"title\": \"Micro Concepts\",          \"description\": \"\",          \"category\": \"pedagogy\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'teaxtarea', 'order': 14 }\"        },        {          \"propertyName\": \"difficultyLevel\",          \"title\": \"Difficulty Level\",          \"description\": \"\",          \"category\": \"pedagogy\",          \"dataType\": \"Select\",          \"range\": [            \"Easy\",            \"Medium\",            \"Difficult\"          ],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'select', 'order': 4 }\"        },        {          \"propertyName\": \"occurrenceCount\",          \"title\": \"Occurrence Count\",          \"description\": \"\",          \"category\": \"frequency\",          \"dataType\": \"Number\",          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 4 }\"        },        {          \"propertyName\": \"senseSetCount\",          \"title\": \"Sense Set Count\",          \"description\": \"\",          \"category\": \"frequency\",          \"dataType\": \"Number\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'Number', 'order': 4 }\"        },        {          \"propertyName\": \"posCount\",          \"title\": \"Parts of the Speech Count\",          \"description\": \"\",          \"category\": \"frequency\",          \"dataType\": \"Number\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'number', 'order': 4 }\"        },        {          \"propertyName\": \"userSets\",          \"title\": \"User Sets\",          \"description\": \"\",          \"category\": \"frequency\",          \"dataType\": \"Number\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'number', 'order': 4 }\"        },        {          \"propertyName\": \"sampleUsages\",          \"title\": \"Sample Usages\",          \"description\": \"\",          \"category\": \"sampleData\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"\"        },        {          \"propertyName\": \"audio\",          \"title\": \"Audio\",          \"description\": \"\",          \"category\": \"sampleData\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': '',  'order': 9 }\"        },        {          \"propertyName\": \"pictures\",          \"title\": \"Pictures\",          \"description\": \"\",          \"category\": \"sampleData\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': '',  'order': 14 }\"        },        {          \"propertyName\": \"pronunciations\",          \"title\": \"Pronunciations\",          \"description\": \"\",          \"category\": \"sampleData\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': '',  'order': 14 }\"        },        {          \"propertyName\": \"reviewers\",          \"title\": \"Reviewers\",          \"description\": \"\",          \"category\": \"audit\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text',  'order': 14 }\"        },        {          \"propertyName\": \"lastUpdatedBy\",          \"title\": \"Last Updated By\",          \"description\": \"\",          \"category\": \"audit\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text',  'order': 14 }\"        },        {          \"propertyName\": \"lastUpdatedOn\",          \"title\": \"Last Updated On\",          \"description\": \"\",          \"category\": \"audit\",          \"dataType\": \"Date\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 22 }\"        },        {            \"propertyName\": \"status\",            \"title\": \"Status\",            \"description\": \"Status of the domain\",            \"category\": \"audit\",            \"dataType\": \"Select\",            \"range\":            [                \"Draft\",                \"Live\",                \"Review\",                \"Retired\"            ],            \"required\": false,			\"indexed\": true,            \"displayProperty\": \"Editable\",            \"defaultValue\": \"Live\",            \"renderingHints\": \"{'inputType': 'select', 'order': 23}\"        },        {          \"propertyName\": \"source\",          \"title\": \"Source\",          \"description\": \"\",          \"category\": \"conflicts\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"conflictStatus\",          \"title\": \"Conflict Status\",          \"description\": \"\",          \"category\": \"conflicts\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"relevancy\",          \"title\": \"Relevancy\",          \"description\": \"\",          \"category\": \"analytics\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"complexity\",          \"title\": \"Complexity\",          \"description\": \"\",          \"category\": \"analytics\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"possibleSpellings\",          \"title\": \"Possible Spellings\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"allowedSuffixes\",          \"title\": \"Allowed Suffixes\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"allowedPrefixes\",          \"title\": \"Allowed Prefixes\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"allowedInfixes\",          \"title\": \"Allowed Infixes\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"tenseForms\",          \"title\": \"Tense Forms\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"pluralForms\",          \"title\": \"Plural Forms\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"singularForms\",          \"title\": \"Singular Forms\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"genders\",          \"title\": \"Genders\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'select', 'order': 25 }\"        },        {          \"propertyName\": \"pronouns\",          \"title\": \"Pronouns\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        }      ],      \"inRelations\": [        {          \"relationName\": \"synonym\",          \"title\": \"synonyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        }      ],      \"outRelations\": [        {          \"relationName\": \"hasAntonym\",          \"title\": \"antonyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Word\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasHypernym\",          \"title\": \"hypernyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Word\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasHolonym\",          \"title\": \"holonyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Word\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasHyponym\",          \"title\": \"hyponyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Word\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasMeronym\",          \"title\": \"meronyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Word\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        }      ],      \"systemTags\": [        {          \"name\": \"Review Tags\",          \"description\": \"Need to Review this Word.\"        },        {          \"name\": \"Missing Information\",          \"description\": \"Some the information is missing.\"        },        {          \"name\": \"Incorrect Data\",          \"description\": \"Wrong information about this word.\"        },        {          \"name\": \"Spelling Mistakes\",          \"description\": \"Incorrect Spellings\"        }      ],      \"metadata\": {        \"ttl\": 24,        \"limit\": 50      }    }  ]}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/taxonomy/" + language + "/definition";
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

		contentString = "{  \"definitionNodes\": [    {      \"objectType\": \"Synset\",      \"properties\": [        {          \"propertyName\": \"gloss\",          \"title\": \"Gloss\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 3 }\"        },        {          \"propertyName\": \"glossInEnglish\",          \"title\": \"Gloss in English\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 5 }\"        },        {          \"propertyName\": \"exampleSentences\",          \"title\": \"Example Sentences\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 1 }\"        },        {          \"propertyName\": \"frames\",          \"title\": \"Sentence Frames\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 1 }\"        },        {          \"propertyName\": \"pos\",          \"title\": \"POS\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 19 }\"        },        {          \"propertyName\": \"namedEntityType\",          \"title\": \"Named Entity Type\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 4 }\"        },        {          \"propertyName\": \"pictures\",          \"title\": \"Pictures\",          \"description\": \"URL\",          \"category\": \"sampleData\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"\"        },        {          \"propertyName\": \"Audio\",          \"title\": \"audio\",          \"description\": \"URL\",          \"category\": \"sampleData\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'teaxtarea',  'order': 9 }\"        },        {          \"propertyName\": \"reviewers\",          \"title\": \"Reviewers\",          \"description\": \"\",          \"category\": \"audit\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text',  'order': 14 }\"        },        {          \"propertyName\": \"lastUpdatedBy\",          \"title\": \"Last Updated By\",          \"description\": \"\",          \"category\": \"audit\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text',  'order': 14 }\"        },        {          \"propertyName\": \"lastUpdatedOn\",          \"title\": \"Last Updated On\",          \"description\": \"\",          \"category\": \"audit\",          \"dataType\": \"Date\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 22 }\"        },        {            \"propertyName\": \"status\",            \"title\": \"Status\",            \"description\": \"Status of the domain\",            \"category\": \"audit\",            \"dataType\": \"Select\",            \"range\":            [                \"Draft\",                \"Live\",                \"Retired\"            ],            \"required\": false,			\"indexed\": true,            \"displayProperty\": \"Editable\",            \"defaultValue\": \"Draft\",            \"renderingHints\": \"{'inputType': 'select', 'order': 23}\"        },        {          \"propertyName\": \"source\",          \"title\": \"Source\",          \"description\": \"\",          \"category\": \"conflicts\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"conflictStatus\",          \"title\": \"Conflict Status\",          \"description\": \"\",          \"category\": \"conflicts\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        }      ],      \"inRelations\": [],      \"outRelations\": [        {          \"relationName\": \"synonym\",          \"title\": \"Synonyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Word\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasAntonym\",          \"title\": \"Antonyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasHypernym\",          \"title\": \"Hypernyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasHolonym\",          \"title\": \"Holonyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasHyponym\",          \"title\": \"Hyponyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasMeronym\",          \"title\": \"Meronyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        }      ],      \"systemTags\": [        {          \"name\": \"Review Tags\",          \"description\": \"Need to Review this Synset.\"        },        {          \"name\": \"Missing Information\",          \"description\": \"Some the information is missing.\"        },        {          \"name\": \"Incorrect Data\",          \"description\": \"Wrong information about this Synset.\"        }      ],      \"metadata\": {        \"ttl\": 24,        \"limit\": 50      }    }  ]}";
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
		resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}

	@SuppressWarnings("unused")
	@Test
	public void addWordIndexTest() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"id\":\"ka_4421\"},{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"id\":\"ka_4367\"}]}}";

		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});

		String apiId = "wordIndex.add";
		Request request = LanguageIndexesTestHelper.getRequestObject(map);

		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.addWordIndex.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);//load
		PlatformLogger.log("List | Request: " + request);
		Response response = LanguageIndexesTestHelper.getResponse(request);
		PlatformLogger.log("List | Response: " + response);
		ResponseEntity<Response> responseEntity = LanguageIndexesTestHelper
				.getResponseEntity(response, apiId, (null != request
						.getParams()) ? request.getParams().getMsgid() : null);
		PlatformLogger.log("List | Response: " + response);
		Assert.assertEquals("successful", response.getParams().getStatus());
	}

}
