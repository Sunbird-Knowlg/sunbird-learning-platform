package org.ekstep.jobs.samza.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.service.WordEnrichmentService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.WordEnrichmentParams;
import org.junit.Assert;
import org.junit.Before;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Response;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class WordEnrichmentTest {
	
	@Autowired
	private WebApplicationContext context;
	private MockMvc mockMvc;
	private ResultActions actions;
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	JobMetrics mockMetrics;
	MessageCollector mockCollector;
	ObjectMapper mapper = new ObjectMapper();
	
	WordEnrichmentService service = new WordEnrichmentService();
	String createWord = "{\"request\":{\"words\":[{\"lemma\":\"ವಿಶ್ಲೇಷಣೆ\",\"primaryMeaning\":{\"gloss\":\"ss1\",\"category\":\"Action/Event\",\"exampleSentences\":[\"es11\",\"es12\"],\"synonyms\":[{\"lemma\":\"s2\"}]},\"status\":\"Draft\"}]}}";
	String validMessage = "{\"ets\":1500888709490,\"nodeUniqueId\":\"ka_11229528054276096015\",\"requestId\":null,\"transactionData\":{\"properties\":{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":433342,\"label\":\"ವಿಶ್ಲೇಷಣೆ\",\"graphId\":\"ka\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"Word\"}";
	String messageWithInvalidObjectType = "{\"ets\":1500888709490,\"nodeUniqueId\":\"ka_11229528054276096015\",\"requestId\":null,\"transactionData\":{\"properties\":{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":433342,\"label\":\"ವಿಶ್ಲೇಷಣೆ\",\"graphId\":\"ka\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"domain\"}";
	String messageWithoutOperationType = "{\"ets\":1500888709490,\"nodeUniqueId\":\"ka_11229528054276096015\",\"requestId\":null,\"transactionData\":{\"properties\":{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"nodeGraphId\":433342,\"label\":\"ವಿಶ್ಲೇಷಣೆ\",\"graphId\":\"ka\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"domain\"}";
	String messageWithoutNodeType = "{\"ets\":1500888709490,\"nodeUniqueId\":\"ka_11229528054276096015\",\"requestId\":null,\"transactionData\":{\"properties\":{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"nodeGraphId\":433342,\"label\":\"ವಿಶ್ಲೇಷಣೆ\",\"graphId\":\"ka\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"domain\"}";
	String messageWithInvalidNodeType = "{\"ets\":1500888709490,\"nodeUniqueId\":\"ka_11229528054276096015\",\"requestId\":null,\"transactionData\":{\"properties\":{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":433342,\"label\":\"ವಿಶ್ಲೇಷಣೆ\",\"graphId\":\"ka\",\"nodeType\":\"ROOT_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"domain\"}";
	String messageWithInvalidOperationType = "{\"ets\":1500888709490,\"nodeUniqueId\":\"ka_11229528054276096015\",\"requestId\":null,\"transactionData\":{\"properties\":{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"operationType\":\"DELETE\", \"nodeGraphId\":433342,\"label\":\"ವಿಶ್ಲೇಷಣೆ\",\"graphId\":\"ka\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"domain\"}";
	String validPropertiesWithLemmaChange = "{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}";
	String propertiesWithoutLemmaChange = "{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}";
	Map<String, Object> messageData = new HashMap<String,Object>();
	
	@Test
	public void testTransactionEventWithValidMessage() throws Exception {
		WordEnrichmentTest(validMessage);
		Map<String,Object> transactionData = service.getTransactionEvent(messageData);
		assertEquals(transactionData.containsKey("properties"), true);
		assertEquals(transactionData.containsKey("node_type"), false);
	}
	
	@Test
	public void testTransactionEventWithInvalidObjectType() throws JsonParseException, JsonMappingException, IOException{
		WordEnrichmentTest(messageWithInvalidObjectType);
		Map<String,Object> transactionData = service.getTransactionEvent(messageData);
		assertEquals(transactionData, null);
	}
	
	@Test
	public void testTransactionEventWithoutOperationType() throws JsonParseException, JsonMappingException, IOException{
		WordEnrichmentTest(messageWithoutOperationType);
		Map<String,Object> transactionData = service.getTransactionEvent(messageData);
		assertEquals(transactionData, null);
	}
	
	@Test
	public void testTransactionEventWithoutNodeType() throws JsonParseException, JsonMappingException, IOException{
		WordEnrichmentTest(messageWithoutNodeType);
		Map<String,Object> transactionData = service.getTransactionEvent(messageData);
		assertEquals(transactionData, null);
	}
	
	@Test
	public void testTransactionEventWithInvalidNodeType() throws JsonParseException, JsonMappingException, IOException{
		WordEnrichmentTest(messageWithInvalidNodeType);
		Map<String,Object> transactionData = service.getTransactionEvent(messageData);
		assertEquals(transactionData, null);
	}
	
	@Test
	public void testTransactionEventWithInvalidOperationType() throws JsonParseException, JsonMappingException, IOException{
		WordEnrichmentTest(messageWithInvalidOperationType);
		Map<String,Object> transactionData = service.getTransactionEvent(messageData);
		assertEquals(transactionData, null);
	}
	
	@Test
	public void testEnrichmentIsRequiredWithLemmaChange() throws JsonParseException, JsonMappingException, IOException{
		WordEnrichmentTest(validPropertiesWithLemmaChange);
		boolean isEnrichNeeded = service.isEnrichNeeded(messageData);
		assertEquals(isEnrichNeeded, true);
	}
	
	@Test
	public void testEnrichmentIsRequiredWithoutLemmaChange() throws JsonParseException, JsonMappingException, IOException{
		WordEnrichmentTest(propertiesWithoutLemmaChange);
		boolean isEnrichNeeded = service.isEnrichNeeded(messageData);
		assertEquals(isEnrichNeeded, false);
	}
	
	public void WordEnrichmentTest(String message) throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(message, new TypeReference<Map<String, Object>>() {
		});
	}
//	@Test
	public void createWord() throws JsonParseException, JsonMappingException, IOException{
		
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v2/language/dictionary/word/ka";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON_UTF8)
					.content(createWord));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		String node_id = (String)result.get("node_ids");
	}
	
//	@Test
	public void enrichWord() throws JsonParseException, JsonMappingException, IOException{
		
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		Map<String, Object> requestBodyMap = new HashMap<String, Object>();
		Map<String, Object> request = new HashMap<String, Object>();
		WordEnrichmentTest(validMessage);
		Map<String,Object> transactionData = service.getTransactionEvent(messageData);
		String language_id = (String)transactionData.get("graphId");
		String node_id = (String)transactionData.get("nodeUniqueId");
		request.put(WordEnrichmentParams.word_id.name(), node_id);
		requestBodyMap.put(WordEnrichmentParams.request.name(), request);
		String requestBody = mapper.writeValueAsString(requestBodyMap);
		String path = "http://localhost:8080/language-service/v1/language/tools/enrich/" + language_id;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON_UTF8)
					.content(requestBody));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
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
