package org.ekstep.language.controllerstest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.CoreMatchers;
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
public class LanugageWordExampleSentencesTest{

	@Autowired
	private WebApplicationContext context;
	private static ObjectMapper mapper = new ObjectMapper();
	private ResultActions actions;
	
	@SuppressWarnings("rawtypes")
	@Test
	public void createWordWithPrimaryMeaningTest() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[{\"lemma\":\"newtestword\",\"primaryMeaning\":{\"identifier\":\"202707688\",\"gloss\":\"ss1\",\"category\":\"Person\",\"exampleSentences\":[\"es11\",\"es12\"]},\"status\":\"Draft\"}]}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v3/words/create?language_id=en";
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
		String wordId  = nodeIds.get(0).toString();
		String resultString = mapper.writeValueAsString(nodeIds);
//		System.out.println("resultString="+resultString);
		
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
//		System.out.println(wordNode.toString());
		List<Map<String, Object>> synsets= (List<Map<String, Object>>)wordNode.get("synsets");
		Assert.assertNotNull(synsets);
		Map<String,Object> synset = synsets.get(0);
		List<String> exSentences = (List<String>)synset.get("exampleSentences");
		Assert.assertThat(exSentences, CoreMatchers.hasItems("es11","es12"));
		
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void createWordWithPrimaryMeaningAndOtherMeaningsTest() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{\"request\":{\"words\":[{\"lemma\":\"newtestword2\",\"primaryMeaning\":{\"identifier\":\"202707688\",\"gloss\":\"ss1\",\"category\":\"Person\",\"exampleSentences\":[\"esSc1\",\"esSc2\"]},\"otherMeanings\":[{\"identifier\":\"202707689\",\"gloss\":\"ss2\",\"category\":\"Person\",\"exampleSentences\":[\"esScOM1s1\",\"esScOM1s2\"]}],\"status\":\"Draft\"}]}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v3/words/create?language_id=en";
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
		String wordId  = nodeIds.get(0).toString();
		String resultString = mapper.writeValueAsString(nodeIds);
//		System.out.println("resultString="+resultString);
		
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
//		System.out.println(wordNode.toString());
		List<Map<String, Object>> synsets= (List<Map<String, Object>>)wordNode.get("synsets");
		Assert.assertNotNull(synsets);
		
		for(Map<String,Object> synset : synsets){
			List<String> exSentences = (List<String>)synset.get("exampleSentences");
//			System.out.println(exSentences.toString());
			switch(synset.get("identifier").toString()){
				case "202707688":
					Assert.assertThat(exSentences, CoreMatchers.hasItems("esSc1","esSc2"));
					break;
				case "202707689":
					Assert.assertThat(exSentences, CoreMatchers.hasItems("esScOM1s1","esScOM1s2"));
					break;
			}
		}
		
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
