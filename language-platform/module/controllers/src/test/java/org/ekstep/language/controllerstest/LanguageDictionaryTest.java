package org.ekstep.language.controllerstest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.language.common.BaseLanguageTest;
import org.ekstep.language.mgr.impl.DictionaryManagerImpl;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.language.util.ElasticSearchUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
public class LanguageDictionaryTest extends BaseLanguageTest{

	@Autowired
	private WebApplicationContext context;
	private static DictionaryManagerImpl dictionaryManager = new DictionaryManagerImpl();
	private static ObjectMapper mapper = new ObjectMapper();
	private ResultActions actions;
	static ElasticSearchUtil util;
	private String bucketName = "ekstep-public";
	String uploadfolder = "language_assets";
	private String uploadFileName = "testSsf.txt";

	static {
		LanguageRequestRouterPool.init();
	}

	@BeforeClass
	public static void initDictionary() throws Exception {
		//createWordTest();
	}

	@SuppressWarnings("rawtypes")
	public void createWordTest() throws JsonParseException,
			JsonMappingException, IOException {
//		String contentString = "{\"request\":{\"words\":[{\"identifier\":\"en_w_707\",\"lemma\":\"newtestword\",\"difficultyLevel\":\"Easy\",\"synonyms\":[{\"identifier\":\"202707688\",\"gloss\":\"newsynonym\"}],\"antonyms\":[{\"name\":\"newtestwordantonym\"}],\"tags\":[\"English\",\"API\"]}]}}";
		String contentString = "{\"request\":{\"words\":[{\"identifier\":\"en_w_707\",\"lemma\":\"newtestword\",\"primaryMeaning\":{\"identifier\":\"202707688\",\"gloss\":\"ss1\",\"category\":\"Person\",\"exampleSentences\":[\"es11\",\"es12\"],\"synonyms\":[{\"lemma\":\"newsynonym\"}]},\"status\":\"Draft\"}]}}";
		String expectedResult = "[\"en_w_707\"]";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v2/language/dictionary/word/" + TEST_LANGUAGE;
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

		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		List nodeIds = (List) result.get("node_ids");
		String resultString = mapper.writeValueAsString(nodeIds);
		Assert.assertEquals(expectedResult, resultString);
	}

	@SuppressWarnings("rawtypes")
	//@Test
	public void createWordForTranslation() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[{\"identifier\":\"en_w_709\",\"lemma\":\"newtestword\",\"difficultyLevel\":\"Easy\",\"synonyms\":[{\"identifier\":\"202707688\",\"gloss\":\"newsynonym\"}],\"antonyms\":[{\"name\":\"newtestwordantonym\"}],\"translations\":[{\"words\":[\"सालाना\",\"वार्षिक\",\"प्रकार\"],\"language_id\":\"hi\"},{\"words\":[\"ಅಳತೊಡಗಿದ\",\"ಪ್ರಧಾನಿಯಾದ\",\"ಹಾರಿಸಿದರು\"],\"language_id\":\"ka\"}],\"tags\":[\"English\",\"API\"]}]}}";
		String expectedResult = "[\"en_w_709\"]";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/" + TEST_LANGUAGE;
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

		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		List nodeIds = (List) result.get("node_id");
		String resultString = mapper.writeValueAsString(nodeIds);
		Assert.assertEquals(expectedResult, resultString);
	}

	@SuppressWarnings({ "unchecked"})
	//@Test
	public void translateWord() throws JsonParseException, JsonMappingException,
			IOException {
		createWordForTranslation();
		String expectedResult = "[{\"words\":[\"सालाना\",\"वार्षिक\",\"प्रकार\"],\"language_id\":\"hi\"},{\"words\":[\"ಅಳತೊಡಗಿದ\",\"ಪ್ರಧಾನಿಯಾದ\",\"ಹಾರಿಸಿದರು\"],\"language_id\":\"ka\"}]";
		String lemma = "en_w_709";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/"+TEST_LANGUAGE+"/translation";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path)
					.param("words", new String[]{"en_w_709"})
					.param("languages", new String[]{"hi"})
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		Map<String, Object> result = resp.getResult();
		Map<String, Object> wordMap = (Map<String, Object>) result
				.get("translations");
		String translations = (String) wordMap
				.get(lemma);
		Assert.assertEquals(expectedResult, translations);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void searchWord() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"lemma\":[\"newtestword\"],\"limit\":10}}";
		String lemma = "newtestword";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/search/" + TEST_LANGUAGE;
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
		ArrayList<Map<String, Object>> words = (ArrayList<Map<String, Object>>) result
				.get("words");
		for(Map<String, Object> wordMap : words){
			Assert.assertEquals(wordMap.get("lemma"), lemma);
		}
	}
	
	@Test
	public void updateWordTest() throws JsonParseException,
			JsonMappingException, IOException {
		//String contentString = "{\"request\":{\"word\":{\"lemma\":\"newtestword\",\"synonyms\":[{\"identifier\":\"202707688\",\"gloss\":\"newsynonym\",\"words\":[\"newtestword\"]}],\"antonyms\":[{\"name\":\"newtestwordantonym\"}]}}}";
		String contentString = "{\"request\":{\"word\":{\"identifier\":\"en_w_707\",\"lemma\":\"newtestword\",\"primaryMeaning\":{\"identifier\":\"202707688\",\"gloss\":\"ss1\",\"category\":\"Person\",\"exampleSentences\":[\"es11\",\"es12\"],\"synonyms\":[{\"lemma\":\"newsynonym\"}], \"antonyms\":[{\"name\":\"newtestwordantonym\"}]},\"status\":\"Draft\"}}}";
		String expectedResult = "\"en_w_707\"";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v2/language/dictionary/word/" + TEST_LANGUAGE
				+ "/en_w_707";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.patch(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
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
		String resultString = mapper.writeValueAsString(nodeId);
		Assert.assertEquals(expectedResult, resultString);
	}

	@Test
	public void addRelation() throws JsonParseException, JsonMappingException,
			IOException {
		createWordTest();
		String expectedResult = "\"" + TEST_LANGUAGE + "\"";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/" + TEST_LANGUAGE
				+ "/202707688/synonym/en_w_707";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		String nodeIds = (String) result.get("graph_id");
		String resultString = mapper.writeValueAsString(nodeIds);
		Assert.assertEquals(expectedResult, resultString);
	}

	@Test
	public void addRelationError() throws JsonParseException, JsonMappingException,
			IOException {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/" + TEST_LANGUAGE
				+ "/2654/synonym/en_w_800";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.header("user-id", "ilimi"));
			Assert.assertNotEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
		Assert.assertEquals("failed", response.getParams().getStatus());
	}
	
	@Test
	public void upload() throws Exception {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/media/upload";
		MockMultipartFile testFile = new MockMultipartFile("file",
				uploadFileName, "text/plain", "file".getBytes());
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.fileUpload(path)
					.file(testFile).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		
		//Will be executed only the upload is successful, need not be moved out
		String url = (String) result.get("url");
		String fileName = url.substring(url.lastIndexOf('/') + 1);
		AWSUploader.deleteFile(uploadfolder + "/" + fileName);
	}

	@Test
	public void deleteRelation() throws JsonParseException,
			JsonMappingException, IOException {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/" + TEST_LANGUAGE
				+ "/202707688/synonym/en_w_707";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.delete(path)
					.contentType(MediaType.APPLICATION_JSON)
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
	}
	
	@Test
	public void deleteRelationError() throws JsonParseException,
			JsonMappingException, IOException {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/" + TEST_LANGUAGE
				+ "/233444/synonym/en_w_800";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.delete(path)
					.contentType(MediaType.APPLICATION_JSON)
					.header("user-id", "ilimi"));
			Assert.assertNotEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
		Assert.assertEquals("failed", response.getParams().getStatus());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getWord() throws JsonParseException, JsonMappingException,
			IOException {
		String identifier = "en_w_707";
		String lemma = "newtestword";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/" + TEST_LANGUAGE
				+ "/en_w_707";
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
		Map<String, Object> wordMap = (Map<String, Object>) result.get("Word");
		Assert.assertEquals(wordMap.get("identifier"), identifier);
		Assert.assertEquals(wordMap.get("lemma"), lemma);
	}

	@SuppressWarnings("unchecked")
	//@Test
	public void getSynonyms() throws JsonParseException, JsonMappingException,
			IOException {
		String lemma = "newtestword";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/" + TEST_LANGUAGE
				+ "/synonym/202707688";
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
		ArrayList<Map<String, Object>> words = (ArrayList<Map<String, Object>>) result
				.get("synonyms");
		for(Map<String, Object> wordMap : words){
			Assert.assertEquals(wordMap.get("lemma"), lemma);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getWords() throws JsonParseException, JsonMappingException,
			IOException {
		String lemmas[] = {"newsynonym" , "newtestword", "newtestwordantonym"};

		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/" + TEST_LANGUAGE;
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
		ArrayList<Map<String, Object>> words = (ArrayList<Map<String, Object>>) result
				.get("words");
		List<String> lemmaResult = new ArrayList<>();
		for(Map<String, Object> wordMap : words){
			lemmaResult.add((String)wordMap.get("lemma"));
		}
		Collections.sort(lemmaResult, Collator.getInstance());
		Assert.assertArrayEquals("", lemmas, lemmaResult.toArray(new String[lemmaResult.size()]));
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
