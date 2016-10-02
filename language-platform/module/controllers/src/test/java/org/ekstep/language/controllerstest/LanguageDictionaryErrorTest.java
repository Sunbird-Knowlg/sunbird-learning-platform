package org.ekstep.language.controllerstest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.common.BaseLanguageTest;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.language.util.ElasticSearchUtil;
import org.junit.Assert;
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
public class LanguageDictionaryErrorTest extends BaseLanguageTest{

	@Autowired
	private WebApplicationContext context;
	private static ObjectMapper mapper = new ObjectMapper();
	private ResultActions actions;
	private static String TEST_CREATE_LANGUAGE = "testcreatedictionaryerror";

	static {
		LanguageRequestRouterPool.init();
	}

	
	@Test
	public void createWordTest() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[{\"identifier\":\"en_w_708\",\"lemma\":\"newtestword\",\"primaryMeaning\":{\"identifier\":\"202707688\",\"gloss\":\"ss1\",\"category\":\"PersonKKK\",\"exampleSentences\":[\"es11\",\"es12\"],\"synonyms\":[{\"lemma\":\"newsynonym\"}]},\"status\":\"Draft\"}]}}";
		String expectedResult = "[\"en_w_707\"]";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v2/language/dictionary/word/" + TEST_CREATE_LANGUAGE;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
//			Assert.assertNotEquals(200, actions.andReturn().getResponse()
//					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
		Map<String, Object> result = response.getResult();
		List nodeIds = (List) result.get("node_ids");
		String resultString = mapper.writeValueAsString(nodeIds);
		Assert.assertNotEquals(expectedResult, resultString);
	}

	@Test
	public void searchWord() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"lemma\":[\"newtestword\"],\"limit\":10}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/search/" + TEST_CREATE_LANGUAGE;
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

		Response response = jsonToObject(actions);
		Assert.assertEquals("failed", response.getParams().getStatus());
	}
	
	@Test
	public void updateWordTest() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"word\":{\"identifier\":\"en_w_707\",\"lemma\":\"newtestword\",\"primaryMeaning\":{\"identifier\":\"202707688\",\"gloss\":\"ss1\",\"category\":\"Person\",\"exampleSentences\":[\"es11\",\"es12\"],\"synonyms\":[{\"lemma\":\"newsynonym\"}], \"antonyms\":[{\"name\":\"newtestwordantonym\"}]},\"status\":\"Draft\"}}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v2/language/dictionary/word/" + TEST_CREATE_LANGUAGE
				+ "/en_w_707";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.patch(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertNotEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
		Assert.assertEquals("failed", response.getParams().getStatus());
	}

	//@Test
	public void translateWord() throws JsonParseException, JsonMappingException,
			IOException {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/"+TEST_CREATE_LANGUAGE+"/translation";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path)
					.param("words", new String[]{"en_w_709"})
					.param("languages", new String[]{"hi"})
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
	public void addRelation() throws JsonParseException, JsonMappingException,
			IOException {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/" + TEST_CREATE_LANGUAGE
				+ "/202707688/synonym/en_w_707";
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

//	@Test
	public void upload() throws JsonParseException, JsonMappingException,
			IOException {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/media/upload";
		//MockMultipartFile testFile = new MockMultipartFile("file",
			//	null, "text/plain", "file".getBytes());
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.fileUpload(path)
					.header("user-id", "ilimi"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
		Assert.assertEquals("failed", response.getParams().getStatus());
	}

	@Test
	public void deleteRelation() throws JsonParseException,
			JsonMappingException, IOException {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/" + TEST_CREATE_LANGUAGE
				+ "/202707688/synonym/en_w_707";
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

	@Test
	public void getWord() throws JsonParseException, JsonMappingException,
			IOException {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/" + TEST_CREATE_LANGUAGE
				+ "/en_w_707";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header(
					"user-id", "ilimi"));
			Assert.assertNotEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
		Assert.assertEquals("failed", response.getParams().getStatus());
	}

	//@Test
	public void getSynonyms() throws JsonParseException, JsonMappingException,
			IOException {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/" + TEST_CREATE_LANGUAGE
				+ "/synonym/202707688";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header(
					"user-id", "ilimi"));
			Assert.assertNotEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
		Assert.assertEquals("failed", response.getParams().getStatus());
	}

	@Test
	public void getWords() throws JsonParseException, JsonMappingException,
			IOException {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/dictionary/word/" + TEST_CREATE_LANGUAGE;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header(
					"user-id", "ilimi"));
			Assert.assertNotEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
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
