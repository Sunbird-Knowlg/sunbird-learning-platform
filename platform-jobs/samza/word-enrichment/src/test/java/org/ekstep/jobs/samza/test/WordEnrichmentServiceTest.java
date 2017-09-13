package org.ekstep.jobs.samza.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.service.WordEnrichmentService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.language.common.LanguageMap;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.graph.cache.factory.JedisFactory;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class WordEnrichmentServiceTest extends BaseTest{
	
	JobMetrics mockMetrics;
	MessageCollector mockCollector;
	ObjectMapper mapper = new ObjectMapper();
	
	private CountDownLatch lock = new CountDownLatch(1);
	  
	WordEnrichmentService service = new WordEnrichmentService();
	String validMessage = "{\"ets\":1500888709490,\"nodeUniqueId\":\"ka_11229528054276096015\",\"requestId\":null,\"transactionData\":{\"properties\":{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":433342,\"label\":\"ವಿಶ್ಲೇಷಣೆ\",\"graphId\":\"ka\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"Word\"}";
	String messageWithInvalidObjectType = "{\"ets\":1500888709490,\"nodeUniqueId\":\"ka_11229528054276096015\",\"requestId\":null,\"transactionData\":{\"properties\":{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":433342,\"label\":\"ವಿಶ್ಲೇಷಣೆ\",\"graphId\":\"ka\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"domain\"}";
	String messageWithoutOperationType = "{\"ets\":1500888709490,\"nodeUniqueId\":\"ka_11229528054276096015\",\"requestId\":null,\"transactionData\":{\"properties\":{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"nodeGraphId\":433342,\"label\":\"ವಿಶ್ಲೇಷಣೆ\",\"graphId\":\"ka\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"domain\"}";
	String messageWithoutNodeType = "{\"ets\":1500888709490,\"nodeUniqueId\":\"ka_11229528054276096015\",\"requestId\":null,\"transactionData\":{\"properties\":{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"nodeGraphId\":433342,\"label\":\"ವಿಶ್ಲೇಷಣೆ\",\"graphId\":\"ka\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"domain\"}";
	String messageWithInvalidNodeType = "{\"ets\":1500888709490,\"nodeUniqueId\":\"ka_11229528054276096015\",\"requestId\":null,\"transactionData\":{\"properties\":{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":433342,\"label\":\"ವಿಶ್ಲೇಷಣೆ\",\"graphId\":\"ka\",\"nodeType\":\"ROOT_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"domain\"}";
	String messageWithInvalidOperationType = "{\"ets\":1500888709490,\"nodeUniqueId\":\"ka_11229528054276096015\",\"requestId\":null,\"transactionData\":{\"properties\":{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"operationType\":\"DELETE\", \"nodeGraphId\":433342,\"label\":\"ವಿಶ್ಲೇಷಣೆ\",\"graphId\":\"ka\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"domain\"}";
	String validPropertiesWithLemmaChange = "{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}";
	String propertiesWithoutLemmaChange = "{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}";
	String synsetEventWithoutPropertyChange = "{\"lastUpdatedOn\":{\"ov\":\"2017-09-08T19:04:46.878+0530\",\"nv\":\"2017-09-08T19:18:58.237+0530\"},\"pictures\":{\"ov\":[\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/do_112292368575586304111/artifact/742259e6325159266db7492cff311c42_1500533273582.jpeg\"],\"nv\":[\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/do_112292368575586304111/artifact/742259e6325159266db7492cff311c42_1500533273582.jpeg\"]},\"versionKey\":{\"ov\":\"1504877686878\",\"nv\":\"1504878538237\"}}";
	String synsetEventWithPictureChange1 = "{\"lastUpdatedOn\":{\"ov\":\"2017-09-08T19:04:46.878+0530\",\"nv\":\"2017-09-08T19:18:58.237+0530\"},\"pictures\":{\"ov\":null,\"nv\":[\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/do_112292368575586304111/artifact/742259e6325159266db7492cff311c42_1500533273582.jpeg\"]},\"versionKey\":{\"ov\":\"1504877686878\",\"nv\":\"1504878538237\"}}";
	String synsetEventWithPictureChange2 = "{\"lastUpdatedOn\":{\"ov\":\"2017-09-08T19:04:46.878+0530\",\"nv\":\"2017-09-08T19:18:58.237+0530\"},\"pictures\":{\"ov\":[\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/do_112292368575586304111/artifact/742259e6325159266db7492cff311c42_1500533273582.jpeg\"],\"nv\":[\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/do_112292368575586304111/artifact/742259e6325159266db7492cff311c42_1500533273582.jpeg\",\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/do_112292368575586304111/artifact/742259e6325159266db7492cff311c42_1500533273582Test.jpeg\"]},\"versionKey\":{\"ov\":\"1504877686878\",\"nv\":\"1504878538237\"}}";
	String synsetEventWithCategoryChange1 = "{\"lastUpdatedOn\":{\"ov\":\"2017-09-08T19:04:46.878+0530\",\"nv\":\"2017-09-08T19:18:58.237+0530\"},\"category\":{\"ov\":null,\"nv\":\"Thing\"},\"versionKey\":{\"ov\":\"1504877686878\",\"nv\":\"1504878538237\"}}";
	String synsetEventWithCategoryChange2 = "{\"lastUpdatedOn\":{\"ov\":\"2017-09-08T19:04:46.878+0530\",\"nv\":\"2017-09-08T19:18:58.237+0530\"},\"category\":{\"ov\":\"Thing\",\"nv\":\"Place\"},\"versionKey\":{\"ov\":\"1504877686878\",\"nv\":\"1504878538237\"}}";
	Map<String, Object> messageData = new HashMap<String,Object>();
	
	@BeforeClass
	public static void init() throws Exception{
		Map<String, Object> props = new HashMap<String, Object>();
		Configuration.loadProperties(props);
		org.ekstep.language.util.PropertiesUtil.loadProperties(props);
		org.ekstep.searchindex.util.PropertiesUtil.loadProperties(props);
		LanguageMap.loadProperties(props);
		LanguageRequestRouterPool.init();
		JedisFactory.initialize(props);
		BaseTest.before();
	}
	
	@AfterClass
	public static void destroy() throws Exception {
		BaseTest.after();
	}
	
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
	
	@Test
	public void testSyncIsRequiredWithCategory() throws JsonParseException, JsonMappingException, IOException{
		WordEnrichmentTest(synsetEventWithCategoryChange1);
		boolean isSyncNeeded = service.isSyncNeeded(messageData);
		assertEquals(isSyncNeeded, true);
		WordEnrichmentTest(synsetEventWithCategoryChange2);
		isSyncNeeded = service.isSyncNeeded(messageData);
		assertEquals(isSyncNeeded, true);
		WordEnrichmentTest(synsetEventWithPictureChange1);
		isSyncNeeded = service.isSyncNeeded(messageData);
		assertEquals(isSyncNeeded, true);
		WordEnrichmentTest(synsetEventWithPictureChange2);
		isSyncNeeded = service.isSyncNeeded(messageData);
		assertEquals(isSyncNeeded, true);
	}
	
	@Test
	public void testSyncIsRequiredWithoutCategory() throws JsonParseException, JsonMappingException, IOException{
		WordEnrichmentTest(synsetEventWithoutPropertyChange);
		boolean isEnrichNeeded = service.isSyncNeeded(messageData);
		assertEquals(isEnrichNeeded, false);
	}
	
	@Test
	public void testEnrichment() throws JsonParseException, JsonMappingException, IOException{
		try {
			String wordLemma = "testLemma";
			String wordId = createWord(wordLemma);
			assertNotEquals(wordId, null);
			String eventMsg = "{\"ets\":1500888709490,\"nodeUniqueId\":\""+wordId+"\",\"transactionData\":{\"properties\":{\"lemma\":{\"ov\":null,\"nv\":\""+wordLemma+"\"}}},\"operationType\":\"CREATE\",\"graphId\":\""+BaseTest.languageId+"\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"Word\"}";
			WordEnrichmentTest(eventMsg);
			service.processMessage(messageData, null, null);
			lock.await(2000, TimeUnit.MILLISECONDS);
			Node word = getWord(wordId);
			Map<String, Object> meta = word.getMetadata();
			//checking word enrichment few metadata
			assertNotEquals(meta.get("syllableCount"), null);
			List<Relation> rels =word.getInRelations();
			//checking words akshra/rhyming wordset associations
			Assert.assertTrue(rels.size()  > 1 );  
			System.out.println(word.getMetadata().toString());
		} catch(Exception e) {			
		}
	}
	
	public void WordEnrichmentTest(String message) throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(message, new TypeReference<Map<String, Object>>() {});
	}
}