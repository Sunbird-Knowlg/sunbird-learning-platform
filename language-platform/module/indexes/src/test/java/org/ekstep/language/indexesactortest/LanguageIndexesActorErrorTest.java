package org.ekstep.language.indexesactortest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.index.common.BaseLanguageTest;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.language.util.ElasticSearchUtil;
import org.junit.Assert;
import org.springframework.test.web.servlet.ResultActions;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;

import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

public class LanguageIndexesActorErrorTest extends BaseLanguageTest{

	//	private static TaxonomyManagerImpl taxonomyManager = new TaxonomyManagerImpl();
	private static ObjectMapper mapper = new ObjectMapper();
	static ElasticSearchUtil util;
	private static ILogger LOGGER = new PlatformLogger(LanguageIndexesActorTest.class
			.getName());
//	private static String TEST_LANGUAGE = "testindexactorerror";
//	private static String TEST_LOAD_LANGUAGE = "testindexactorerrorload";

	static {
		TEST_LANGUAGE = "testindexactorerror";
		LanguageRequestRouterPool.init();
	}

	/*@BeforeClass
	public static void init() throws Exception {
		// Definitions
		deleteDefinitionStatic(TEST_LANGUAGE);
		deleteDefinitionStatic(TEST_LOAD_LANGUAGE);
		createDefinitionsStatic(TEST_LANGUAGE);
		createDefinitionsStatic(TEST_LOAD_LANGUAGE);

		util = new ElasticSearchUtil();
		util.deleteIndex("citation_index_" + TEST_LANGUAGE);
		util.deleteIndex("word_index_" + TEST_LANGUAGE);
		util.deleteIndex("word_info_index_" + TEST_LANGUAGE);
		util.deleteIndex("citation_index_" + TEST_LOAD_LANGUAGE);
		util.deleteIndex("word_index_" + TEST_LOAD_LANGUAGE);
		util.deleteIndex("word_info_index_" + TEST_LOAD_LANGUAGE);
	}

	@AfterClass
	public static void close() throws IOException, InterruptedException {
		util.deleteIndex("citation_index_" + TEST_LANGUAGE);
		util.deleteIndex("word_index_" + TEST_LANGUAGE);
		util.deleteIndex("word_info_index_" + TEST_LANGUAGE);
		util.deleteIndex("citation_index_" + TEST_LOAD_LANGUAGE);
		util.deleteIndex("word_index_" + TEST_LOAD_LANGUAGE);
		util.deleteIndex("word_info_index_" + TEST_LOAD_LANGUAGE);

		deleteDefinitionStatic(TEST_LANGUAGE);
		deleteDefinitionStatic(TEST_LOAD_LANGUAGE);
	}*/

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
		request.setOperation("rootWordsError");
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		LOGGER.log("List | Request: " + request);
		Response response = LanguageIndexesTestHelper.getResponse(
				request, LOGGER);
		LOGGER.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);

		Assert.assertEquals("failed", response.getParams().getStatus());
	}

	//@Test
	public void addCitationsTest() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"citations\":[{\"rootWord\":\"ಪೂರ್ವ\",\"pos\":\"NST\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಶಾಲೆ\",\"rootWord\":\"ಶಾಲೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಆಟ\",\"rootWord\":\"ಆಟ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಅಳಿಲು\",\"rootWord\":\"ಅಳಿಲು\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಗಂಟೆ\",\"rootWord\":\"ಗಂಟೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಕೋತಿ\",\"rootWord\":\"ಕೋತಿ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹೇಳಿಸಿ\",\"rootWord\":\"ಹೇಳಿಸು\",\"pos\":\"VM\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಮರ\",\"rootWord\":\"ಮರ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಪಾಠ\",\"rootWord\":\"ಪಾಠ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಚಟುವಟಿಕೆ\",\"rootWord\":\"ಚಟುವಟಿಕೆ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹುಡುಗ\",\"rootWord\":\"ಹುಡುಗ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಹುಡುಗಿ\",\"rootWord\":\"ಹುಡುಗಿ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಕೇಳಿಸಿ\",\"rootWord\":\"ಕೇಳಿಸು\",\"pos\":\"VM\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"},{\"word\":\"ಪಾಠ\",\"rootWord\":\"ಪಾಠ\",\"pos\":\"NN\",\"date\":\"04-Feb-2016 18:02:17\",\"sourceType\":\"textbooks\",\"source\":\"Class1\",\"grade\":\"1\",\"fileName\":\"testSsf.txt\"}]}}";
		String apiId = "citation.add";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.addCitationIndex.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		LOGGER.log("List | Request: " + request);
		Response response;
		response = LanguageIndexesTestHelper.getBulkOperationResponse(request,
				LOGGER);
		LOGGER.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		Assert.assertEquals("failed", response.getParams().getStatus());
	}

	//@Test
	public void wordWildCard() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"word\":\"ಮ*\",\"limit\":\"aaa1000\"}}";
		String apiId = "wordIndexInfo.get";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = LanguageIndexesTestHelper.getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.wordWildCard.name());
		request.getContext().put(LanguageParams.language_id.name(),
				"" + TEST_LANGUAGE);
		LOGGER.log("List | Request: " + request);
		Response response = LanguageIndexesTestHelper.getResponse(
				request, LOGGER);
		LOGGER.log("List | Response: " + response);
		LanguageIndexesTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("failed", response.getParams().getStatus());
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
