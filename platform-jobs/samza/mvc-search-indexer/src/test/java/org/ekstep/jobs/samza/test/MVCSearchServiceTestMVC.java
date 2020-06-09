package org.ekstep.jobs.samza.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.samza.config.Config;
import org.apache.samza.config.MapConfig;
import org.apache.samza.task.MessageCollector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.jobs.samza.service.MVCSearchIndexerService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;

public class MVCSearchServiceTestMVC extends MVCBaseTest {

	String validMessage = "{\"nodeUniqueId\":\"org.ekstep.jul03.story.test01\",\"requestId\":\"110dc48a-b0ee-4a64-b822-1053ab7ef276\",\"transactionData\":{\"properties\":{\"owner\":{\"ov\":null,\"nv\":\"EkStep\"},\"code\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"subject\":{\"ov\":null,\"nv\":\"literacy\"},\"portalOwner\":{\"ov\":null,\"nv\":\"EkStep\"},\"description\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"mediaType\":{\"ov\":null,\"nv\":\"content\"},\"osId\":{\"ov\":null,\"nv\":\"org.ekstep.quiz.app\"},\"mimeType\":{\"ov\":null,\"nv\":\"application/vnd.ekstep.ecml-archive\"},\"ageGroup\":{\"ov\":null,\"nv\":[\"5-6\"]},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"gradeLevel\":{\"ov\":null,\"nv\":[\"Grade 1\"]},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"developer\":{\"ov\":null,\"nv\":\"EkStep\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"contentType\":{\"ov\":null,\"nv\":\"Story\"},\"status\":{\"ov\":null,\"nv\":\"Mock\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":974,\"label\":\"शेर का साथी हाथी\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2016-07-03T15:39:34.570+0530\",\"objectType\":\"Content\"}";
	String messageWithAddedRelations = "{\"ets\":1502102183388,\"nodeUniqueId\":\"do_112276071067320320114\",\"requestId\":null,\"transactionData\":{\"removedTags\":[],\"addedRelations\":[{\"rel\":\"hasSequenceMember\",\"id\":\"do_1123032073439723521148\",\"label\":\"Test unit 11\",\"dir\":\"IN\",\"type\":\"Content\"}],\"removedRelations\":[],\"addedTags\":[],\"properties\":{}},\"operationType\":\"CREATE\",\"nodeGraphId\":105631,\"label\":\"collaborator test\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-08-07T10:36:23.388+0000\",\"objectType\":\"Content\"}";
    String messageWithRemovedRelations = "{\"ets\":1502102183388,\"nodeUniqueId\":\"do_1123032073439723521148\",\"requestId\":null,\"transactionData\":{\"removedTags\":[],\"addedRelations\":[],\"removedRelations\":[{\"rel\":\"hasSequenceMember\",\"id\":\"do_11225638792242790415\",\"label\":\"collabarotor issue\",\"dir\":\"OUT\",\"type\":\"Content\"}],\"addedTags\":[],\"properties\":{}},\"operationType\":\"UPDATE\",\"nodeGraphId\":110067,\"label\":\"Test unit 11\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-08-07T10:36:23.388+0000\",\"objectType\":\"Content\"}";
	
	private MVCSearchIndexerService service = new MVCSearchIndexerService();
	private MessageCollector collector;
	private ObjectMapper mapper = new ObjectMapper();
	static String clusterName = null;

	@Test
	public void testMessageWithProperties() throws Exception {
		Map<String,Object>	messageData = mapper.readValue(validMessage, new TypeReference<Map<String, Object>>() {
		});
	  	Map<String,String> props = new HashMap<String,String>();
		props.put("search.es_conn_info", "localhost:9200");
		props.put("platform-api-url", "http://localhost:8080/learning-service");
		props.put("ekstepPlatformApiUserId", "ilimi");
		Config config = new MapConfig(props);
		service.initialize(config);
		JobMetrics metrics = mock(JobMetrics.class);
		service.processMessage(messageData, metrics, collector);
		Thread.sleep(2000);
		Map<String, Object> map = findById("org.ekstep.jul03.story.test01");
		assertEquals(true, null!=map);
		assertEquals("literacy", map.get("subject"));
	}
	
	@Test
	public void testMessageWithAddedRelations() throws Exception {
		Map<String,Object>	messageData = mapper.readValue(messageWithAddedRelations, new TypeReference<Map<String, Object>>() {
		});
	  	Map<String,String> props = new HashMap<String,String>();
		props.put("search.es_conn_info", "localhost:9200");
		props.put("platform-api-url", "http://localhost:8080/learning-service");
		props.put("ekstepPlatformApiUserId", "ilimi");
		Config config = new MapConfig(props);
		service.initialize(config);
		JobMetrics metrics = mock(JobMetrics.class);
		service.processMessage(messageData, metrics, collector);
		Thread.sleep(2000);
		Map<String, Object> map = findById("do_112276071067320320114");
		assertEquals(true, null!=map);
		assertEquals(true, map.containsKey("collections"));
	}
	
	@Test
	public void testMessageWithRemovedRelations() throws Exception {
		Map<String,Object>	messageData = mapper.readValue(messageWithRemovedRelations, new TypeReference<Map<String, Object>>() {
		});
	  	Map<String,String> props = new HashMap<String,String>();
		props.put("search.es_conn_info", "localhost:9200");
		props.put("platform-api-url", "http://localhost:8080/learning-service");
		props.put("ekstepPlatformApiUserId", "ilimi");
		Config config = new MapConfig(props);
		service.initialize(config);
		JobMetrics metrics = mock(JobMetrics.class);
		service.processMessage(messageData, metrics, collector);
		Thread.sleep(2000);
		Map<String, Object> map = findById("do_1123032073439723521148");
		assertEquals(true, null!=map);
		assertEquals(false, map.containsKey("collections"));
	}
	
	public Map<String, Object> findById(String identifier) throws IOException {
		SearchResponse response = client.search(new SearchRequest(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX)
				.source(new SearchSourceBuilder().query(QueryBuilders.termQuery("_id", identifier))), RequestOptions.DEFAULT);
		SearchHits hits = response.getHits();
		for (SearchHit hit : hits.getHits()) {
			Map<String, Object> fields = hit.getSourceAsMap();
			return fields;
		}
		return null;
	}
}
