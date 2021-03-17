package org.sunbird.jobs.samza.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.samza.config.Config;
import org.apache.samza.config.MapConfig;
import org.apache.samza.task.MessageCollector;
import org.sunbird.dac.enums.AuditHistoryConstants;
import org.sunbird.jobs.samza.service.AuditHistoryIndexerService;
import org.sunbird.jobs.samza.service.task.JobMetrics;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.aduna.io.FileUtil;

public class AuditHistoryIndexerServiceTest {

	private String validMessage = "{\"nodeUniqueId\":\"org.sunbird.jul03.story.test01\",\"requestId\":\"110dc48a-b0ee-4a64-b822-1053ab7ef276\",\"transactionData\":{\"properties\":{\"owner\":{\"ov\":null,\"nv\":\"EkStep\"},\"code\":{\"ov\":null,\"nv\":\"org.sunbird.jul03.story.test01\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"subject\":{\"ov\":null,\"nv\":\"literacy\"},\"portalOwner\":{\"ov\":null,\"nv\":\"EkStep\"},\"description\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"mediaType\":{\"ov\":null,\"nv\":\"content\"},\"osId\":{\"ov\":null,\"nv\":\"org.sunbird.quiz.app\"},\"mimeType\":{\"ov\":null,\"nv\":\"application/vnd.ekstep.ecml-archive\"},\"ageGroup\":{\"ov\":null,\"nv\":[\"5-6\"]},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"gradeLevel\":{\"ov\":null,\"nv\":[\"Grade 1\"]},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"developer\":{\"ov\":null,\"nv\":\"EkStep\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"org.sunbird.jul03.story.test01\"},\"contentType\":{\"ov\":null,\"nv\":\"Story\"},\"status\":{\"ov\":null,\"nv\":\"Mock\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":974,\"label\":\"शेर का साथी हाथी\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2016-07-03T15:39:34.570+0530\",\"objectType\":\"Content\"}";
	private String invalidMessage = "{\"ets\":1500888709490,\"requestId\":null,\"transactionData\":{\"properties\":{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":433342,\"label\":\"ವಿಶ್ಲೇಷಣೆ\",\"graphId\":\"ka\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"Word\"}";
	
	private AuditHistoryIndexerService service = new AuditHistoryIndexerService();
	private static String hostAddress = "localhost";
	private static int port = 9200;
	private static File tempDir = null;
	protected static RestHighLevelClient client = null;
	private MessageCollector collector;
	private ObjectMapper mapper = new ObjectMapper();

	@BeforeClass
	public static void beforeClass() throws UnknownHostException {
		tempDir = new File(System.getProperty("user.dir") + "/tmp");
		client = new RestHighLevelClient(RestClient.builder(new HttpHost(hostAddress, port)));
	}
	
	@AfterClass
	public static void afterClass() throws IOException{
		client.close();
		FileUtil.deleteDir(tempDir);
	}
	
	@Test
	public void getAuditHistoryRecordWithValidMessage() throws Exception{
	    Map<String,String> props = new HashMap<String,String>();
		props.put("search.es_conn_info", "localhost:9200");
		
		Config config = new MapConfig(props);
		service.initialize(config);
		JobMetrics metrics = mock(JobMetrics.class);
		
		Map<String,Object> messageData = mapper.readValue(validMessage, new TypeReference<Map<String, Object>>() {});
		service.processMessage(messageData, metrics, collector);
		Thread.sleep(2000);
		
		Map<String, Object> map = findById("domain");
		assertEquals(true, map.containsKey("summary"));
		assertEquals(true, map.containsKey("logRecord"));
		assertEquals("org.sunbird.jul03.story.test01", (String)map.get("objectId"));
	}
	
	@Test
	public void getAuditHistoryRecordWithInValidMessage() throws Exception{
	    Map<String,String> props = new HashMap<String,String>();
		props.put("search.es_conn_info", "localhost:9200");
		
		Config config = new MapConfig(props);
		service.initialize(config);
		JobMetrics metrics = mock(JobMetrics.class);
		
		Map<String,Object> messageData = mapper.readValue(invalidMessage, new TypeReference<Map<String, Object>>() {});
		service.processMessage(messageData, metrics, collector);
		Thread.sleep(2000);
		
		Map<String, Object> map = findById("ka");
		assertEquals(null, (String)map.get("objectId"));
	}
	
	public Map<String, Object> findById(String graphId) throws IOException {
		SearchResponse response = client.search(new SearchRequest(AuditHistoryConstants.AUDIT_HISTORY_INDEX)
				.source(new SearchSourceBuilder().query(QueryBuilders.termQuery("graphId", graphId))));
		SearchHits hits = response.getHits();
		for (SearchHit hit : hits.getHits()) {
			Map<String, Object> fields = hit.getSourceAsMap();
			return fields;
		}
		return null;
	}
}