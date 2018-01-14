package org.ekstep.jobs.samza.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.samza.task.MessageCollector;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
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
		LanguageMap.loadProperties(props);
		LanguageRequestRouterPool.init();
		BaseTest.before();
	}
	
	@AfterClass
	public static void destroy() throws Exception {
		BaseTest.after();
	}

	@Test
	public void englishWordEnrichTest() throws JsonParseException, JsonMappingException, IOException{
		try {
			String wordLemma = "Lion";
			String wordId = createWord(wordLemma);
			assertNotEquals(wordId, null);
			String eventMsg = "{\"ets\":1500888709490,\"nodeUniqueId\":\""+wordId+"\",\"transactionData\":{\"properties\":{\"lemma\":{\"ov\":null,\"nv\":\""+wordLemma+"\"}}},\"operationType\":\"CREATE\",\"graphId\":\""+BaseTest.languageId+"\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"Word\"}";
			WordEnrichmentTest(eventMsg);
			service.processMessage(messageData, null, null);
			lock.await(2000, TimeUnit.MILLISECONDS);
			Node word = getWord(wordId, BaseTest.languageId);
			Map<String, Object> meta = word.getMetadata();
			//checking word enrichment few metadata
			assertEquals((Long)meta.get("syllableCount"), new Long(4));
			List<Relation> rels =word.getInRelations();
			//checking words akshra/rhyming wordset associations
			Assert.assertTrue(rels.size()  > 1 );
		} catch(Exception e) {			
		}
	}
	
	@Test
	public void indicLanguageWordEnrichTest() throws JsonParseException, JsonMappingException, IOException{
		try {
			String wordLemma = "ಸಿಂಹ";
			String wordId = createWord(wordLemma, BaseTest.ka_languageId);
			assertNotEquals(wordId, null);
			String eventMsg = "{\"ets\":1500888709490,\"nodeUniqueId\":\""+wordId+"\",\"transactionData\":{\"properties\":{\"lemma\":{\"ov\":null,\"nv\":\""+wordLemma+"\"}}},\"operationType\":\"CREATE\",\"graphId\":\""+BaseTest.ka_languageId+"\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"Word\"}";
			WordEnrichmentTest(eventMsg);
			service.processMessage(messageData, null, null);
			lock.await(2000, TimeUnit.MILLISECONDS);
			Node word = getWord(wordId, BaseTest.ka_languageId);
			Map<String, Object> meta = word.getMetadata();
			//checking word enrichment few metadata
			assertEquals((Long)meta.get("syllableCount"), new Long(2));
			assertEquals((Double)meta.get("orthographic_complexity"), new Double(1.1));
			assertEquals((Double)meta.get("phonologic_complexity"), new Double(12.2));
			assertEquals(meta.get("syllableNotation").toString(),"CVVCV");
			assertEquals(meta.get("unicodeNotation").toString(),"\\0cb8\\0cbf\\0c82 \\0cb9\\0C85a");
			assertEquals((Double)meta.get("word_complexity"), new Double(0.8));
			List<Relation> rels =word.getInRelations();
			//checking words akshra/rhyming wordset associations
			Assert.assertTrue(rels.size() == 4 );
			List<String> relationTypes = new ArrayList<>();
			List<String> wordSetMembers = new ArrayList<>();
			for(Relation rel:rels) {
				relationTypes.add(rel.getRelationType());
				if(rel.getStartNodeObjectType().equalsIgnoreCase("WordSet"))
					wordSetMembers.add(rel.getStartNodeMetadata().get("lemma").toString());
			}
	        assertThat(relationTypes, containsInAnyOrder("synonym","hasMember","hasMember", "hasMember"));
	        assertThat(wordSetMembers, containsInAnyOrder("rhymingSound_ 0CB9 0C85A","startsWith_ಸ","endsWith_ಹ"));
		} catch(Exception e) {			
		}
	}
	
	@Test
	public void indicLanguageWordSyncTest() throws JsonParseException, JsonMappingException, IOException{
		try {
			String wordLemma = "ಆನೆ";
			String wordId = createWord(wordLemma, BaseTest.ka_languageId);
			assertNotEquals(wordId, null);
			String eventMsg = "{\"ets\":1500888709490,\"nodeUniqueId\":\""+wordId+"\",\"transactionData\":{\"properties\":{\"lemma\":{\"ov\":null,\"nv\":\""+wordLemma+"\"}}},\"operationType\":\"CREATE\",\"graphId\":\""+BaseTest.ka_languageId+"\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"Word\"}";
			WordEnrichmentTest(eventMsg);
			service.processMessage(messageData, null, null);
			lock.await(2000, TimeUnit.MILLISECONDS);
			Node word = getWord(wordId, BaseTest.ka_languageId);
			Map<String, Object> meta = word.getMetadata();
			//checking word enrichment few metadata
			assertEquals((Long)meta.get("syllableCount"), new Long(2));
			assertEquals((Double)meta.get("orthographic_complexity"), new Double(0.4));
			assertEquals((Double)meta.get("phonologic_complexity"), new Double(3.0));
			assertEquals(meta.get("syllableNotation").toString(),"VCV");
			assertEquals(meta.get("unicodeNotation").toString(),"\\0c86\\0C85a \\0ca8\\0cc6");
			assertEquals((Double)meta.get("word_complexity"), new Double(0.8));
			//check meaning and category is null
			assertEquals(meta.get("meaning"), null);
			assertEquals(meta.get("category"), null);
			assertNotEquals(meta.get("primaryMeaningId"), null);
			String pmId = meta.get("primaryMeaningId").toString();
			//synset to word metadata sync message
			String syncMsg = "{\"ets\":1505114540510,\"nodeUniqueId\":\""+wordId+"\",\"transactionData\":{\"addedRelations\":[{\"rel\":\"synonym\",\"id\":\""+pmId+"\",\"label\":\"ಆನೆ\",\"dir\":\"IN\",\"type\":\"Synset\",\"relMetadata\":{\"isPrimary\":true}}],\"properties\":{}},\"operationType\":\"UPDATE\",\"label\":\"ಆನೆ\",\"graphId\":\""+ka_languageId+"\",\"nodeType\":\"DATA_NODE\",\"createdOn\":\"2017-09-11T12:52:20.510+0530\",\"objectType\":\"Word\"}";
			WordEnrichmentTest(syncMsg);
			service.processMessage(messageData, null, null);
			lock.await(2000, TimeUnit.MILLISECONDS);
			word = getWord(wordId, BaseTest.ka_languageId);
			meta = word.getMetadata();
			//check meaning and category of the word is copied from synset
			assertEquals(meta.get("meaning"), "ಆನೆ");
			assertEquals(meta.get("category"), "Place");
		} catch(Exception e) {			
		}
	}
	
	@Test
	public void indicLanguageSynsetSyncTest() throws JsonParseException, JsonMappingException, IOException{
		try {
			String wordLemma = "ಮನೆ";
			String wordId = createWord(wordLemma, BaseTest.ka_languageId);
			assertNotEquals(wordId, null);
			Node word = getWord(wordId, BaseTest.ka_languageId);
			Map<String, Object> meta = word.getMetadata();
			assertNotEquals(meta.get("primaryMeaningId"), null);
			String pmId = meta.get("primaryMeaningId").toString();
			assertEquals(meta.get("category"), null);
			
			String eventMsg = "{\"ets\":1500888709490,\"nodeUniqueId\":\""+wordId+"\",\"transactionData\":{\"properties\":{\"lemma\":{\"ov\":null,\"nv\":\""+wordLemma+"\"}}},\"operationType\":\"CREATE\",\"graphId\":\""+BaseTest.ka_languageId+"\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"Word\"}";
			WordEnrichmentTest(eventMsg);
			service.processMessage(messageData, null, null);
			//synset to word metadata sync message
			String syncMsg = "{\"ets\":1505114540510,\"nodeUniqueId\":\""+wordId+"\",\"transactionData\":{\"addedRelations\":[{\"rel\":\"synonym\",\"id\":\""+pmId+"\",\"label\":\"ಮನೆ\",\"dir\":\"IN\",\"type\":\"Synset\",\"relMetadata\":{\"isPrimary\":true}}],\"properties\":{}},\"operationType\":\"UPDATE\",\"label\":\"ಮನೆ\",\"graphId\":\""+ka_languageId+"\",\"nodeType\":\"DATA_NODE\",\"createdOn\":\"2017-09-11T12:52:20.510+0530\",\"objectType\":\"Word\"}";
			WordEnrichmentTest(syncMsg);
			service.processMessage(messageData, null, null);
			lock.await(2000, TimeUnit.MILLISECONDS);
			word = getWord(wordId, BaseTest.ka_languageId);
			meta = word.getMetadata();
			//check meaning and category of the word is copied from synset
			assertEquals(meta.get("meaning"), "ಮನೆ");
			assertEquals(meta.get("category"), "Place");
			//checking word enrichment few metadata
			assertEquals((Long)meta.get("syllableCount"), new Long(2));
			assertEquals((Double)meta.get("orthographic_complexity"), new Double(1.0));
			assertEquals((Double)meta.get("phonologic_complexity"), new Double(5.4));
			assertEquals(meta.get("syllableNotation").toString(),"CVCV");
			assertEquals(meta.get("unicodeNotation").toString(),"\\0cae\\0C85a \\0ca8\\0cc6");
			assertEquals((Double)meta.get("word_complexity"), new Double(0.8));
			//update synset metadata like category
			String synsetRequest = "{\"nodeType\":\"DATA_NODE\",\"identifier\":\""+pmId+"\", \"objectType\":\"Synset\",\"metadata\":{\"category\":\"Thing\"}}";
			Object synsetNodeObj = mapper.readValue(synsetRequest, Class.forName("org.ekstep.graph.dac.model.Node"));		
			String synsetId = updateNode(pmId, synsetNodeObj, BaseTest.ka_languageId);
			String synsetUpdateEventMsg="{\"ets\":1505489002231,\"nodeUniqueId\":\""+synsetId+"\",\"transactionData\":{\"properties\":{\"category\":{\"ov\":\"Place\",\"nv\":\"Thing\"}}},\"operationType\":\"UPDATE\",\"graphId\":\""+ka_languageId+"\",\"nodeType\":\"DATA_NODE\",\"createdOn\":\"2017-09-15T20:53:22.231+0530\",\"objectType\":\"synset\"}";
			WordEnrichmentTest(synsetUpdateEventMsg);
			service.processMessage(messageData, null, null);
			lock.await(2000, TimeUnit.MILLISECONDS);
			word = getWord(wordId, BaseTest.ka_languageId);
			meta = word.getMetadata();
			//check category of the word
			assertEquals(meta.get("category"), "Thing");
			
		} catch(Exception e) {			
		}
	}
	
	public void WordEnrichmentTest(String message) throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(message, new TypeReference<Map<String, Object>>() {});
	}
}