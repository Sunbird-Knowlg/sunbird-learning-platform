package org.ekstep.jobs.samza.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;

import org.apache.samza.config.Config;
import org.apache.samza.config.MapConfig;
import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.service.AuditHistoryIndexerService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.dac.enums.AuditHistoryConstants;

import static org.mockito.Mockito.mock;
import info.aduna.io.FileUtil;

public class AuditHistoryIndexerServiceTest {

	private String validMessage = "{\"nodeUniqueId\":\"org.ekstep.jul03.story.test01\",\"requestId\":\"110dc48a-b0ee-4a64-b822-1053ab7ef276\",\"transactionData\":{\"properties\":{\"owner\":{\"ov\":null,\"nv\":\"EkStep\"},\"code\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"subject\":{\"ov\":null,\"nv\":\"literacy\"},\"portalOwner\":{\"ov\":null,\"nv\":\"EkStep\"},\"description\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"mediaType\":{\"ov\":null,\"nv\":\"content\"},\"osId\":{\"ov\":null,\"nv\":\"org.ekstep.quiz.app\"},\"mimeType\":{\"ov\":null,\"nv\":\"application/vnd.ekstep.ecml-archive\"},\"ageGroup\":{\"ov\":null,\"nv\":[\"5-6\"]},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"gradeLevel\":{\"ov\":null,\"nv\":[\"Grade 1\"]},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"developer\":{\"ov\":null,\"nv\":\"EkStep\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"contentType\":{\"ov\":null,\"nv\":\"Story\"},\"status\":{\"ov\":null,\"nv\":\"Mock\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":974,\"label\":\"शेर का साथी हाथी\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2016-07-03T15:39:34.570+0530\",\"objectType\":\"Content\"}";
	private String invalidMessage = "{\"ets\":1500888709490,\"requestId\":null,\"transactionData\":{\"properties\":{\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"morphology\":{\"ov\":null,\"nv\":false},\"consumerId\":{\"ov\":null,\"nv\":\"a6654129-b58d-4dd8-9cf2-f8f3c2f458bc\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"lemma\":{\"ov\":null,\"nv\":\"ವಿಶ್ಲೇಷಣೆ\"},\"createdOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"versionKey\":{\"ov\":null,\"nv\":\"1500888738130\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"ekstepWordnet\":{\"ov\":null,\"nv\":false},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2017-07-24T09:32:18.130+0000\"},\"isPhrase\":{\"ov\":null,\"nv\":false},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"ka_11229528054276096015\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":433342,\"label\":\"ವಿಶ್ಲೇಷಣೆ\",\"graphId\":\"ka\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-07-24T09:31:49.490+0000\",\"objectType\":\"Word\"}";
	
	private AuditHistoryIndexerService service = new AuditHistoryIndexerService();
	private static File tempDir = null;
	private static Settings settings = null;
	private static Node server = null;
	private MessageCollector collector;
	private ObjectMapper mapper = new ObjectMapper();
	static String clusterName = null;
	static Client client = null;

	@BeforeClass
	public static void beforeClass(){
		tempDir = new File(System.getProperty("user.dir") + "/tmp");
		settings = Settings.builder()
				.put("path.home", tempDir.getAbsolutePath())
				.put("transport.tcp.port","9500")
				.build();
		server = NodeBuilder.nodeBuilder().settings(settings).build();
		clusterName = server.settings().get("cluster.name");
		server.start();
		client = server.client();
	}
	
	@AfterClass
	public static void afterClass() throws IOException{
		server.close();
		FileUtil.deleteDir(tempDir);
	}
	
	@Test
	public void getAuditHistoryRecordWithValidMessage() throws Exception{
	    Map<String,String> props = new HashMap<String,String>();
		props.put("elastic-search-host", "http://localhost");
		props.put("elastic-search-port", "9200");
		
		Config config = new MapConfig(props);
		service.initialize(config);
		JobMetrics metrics = mock(JobMetrics.class);
		
		Map<String,Object> messageData = mapper.readValue(validMessage, new TypeReference<Map<String, Object>>() {});
		service.processMessage(messageData, metrics, collector);
		Thread.sleep(2000);
		
		Map<String,Object> map = findById("domain", client);
		assertEquals(true, map.containsKey("summary"));
		assertEquals(true, map.containsKey("logRecord"));
		assertEquals("org.ekstep.jul03.story.test01", (String)map.get("objectId"));
	}
	
	@Test
	public void getAuditHistoryRecordWithInValidMessage() throws Exception{
	    Map<String,String> props = new HashMap<String,String>();
		props.put("elastic-search-host", "http://localhost");
		props.put("elastic-search-port", "9200");
		
		Config config = new MapConfig(props);
		service.initialize(config);
		JobMetrics metrics = mock(JobMetrics.class);
		
		Map<String,Object> messageData = mapper.readValue(invalidMessage, new TypeReference<Map<String, Object>>() {});
		service.processMessage(messageData, metrics, collector);
		Thread.sleep(2000);
		
		Map<String,Object> map = findById("ka", client);
		assertEquals(null, (String)map.get("objectId"));
	}
	
	public Map<String, Object> findById(String graphId, Client client) {
		SearchResponse response = client.prepareSearch(AuditHistoryConstants.AUDIT_HISTORY_INDEX)
				.setTypes(AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE)
				.setQuery(QueryBuilders.termQuery("graphId", graphId)).execute().actionGet();
		SearchHits hits = response.getHits();
		for (SearchHit hit : hits.getHits()) {
			Map<String, Object> fields = hit.getSource();
			return fields;
		}
		return null;
	}
}