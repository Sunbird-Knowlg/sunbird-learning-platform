package org.ekstep.jobs.samza.test;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.apache.samza.config.Config;
import org.apache.samza.config.MapConfig;
import org.apache.samza.task.MessageCollector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.jobs.samza.service.CompositeSearchIndexerService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.searchindex.util.CompositeSearchConstants;
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

import info.aduna.io.FileUtil;

public class CompositeSearchServiceTest {

	String validMessage = "{\"nodeUniqueId\":\"org.ekstep.jul03.story.test01\",\"requestId\":\"110dc48a-b0ee-4a64-b822-1053ab7ef276\",\"transactionData\":{\"properties\":{\"owner\":{\"ov\":null,\"nv\":\"EkStep\"},\"code\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"subject\":{\"ov\":null,\"nv\":\"literacy\"},\"portalOwner\":{\"ov\":null,\"nv\":\"EkStep\"},\"description\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"mediaType\":{\"ov\":null,\"nv\":\"content\"},\"osId\":{\"ov\":null,\"nv\":\"org.ekstep.quiz.app\"},\"mimeType\":{\"ov\":null,\"nv\":\"application/vnd.ekstep.ecml-archive\"},\"ageGroup\":{\"ov\":null,\"nv\":[\"5-6\"]},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"gradeLevel\":{\"ov\":null,\"nv\":[\"Grade 1\"]},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"developer\":{\"ov\":null,\"nv\":\"EkStep\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"contentType\":{\"ov\":null,\"nv\":\"Story\"},\"status\":{\"ov\":null,\"nv\":\"Mock\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":974,\"label\":\"शेर का साथी हाथी\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2016-07-03T15:39:34.570+0530\",\"objectType\":\"Content\"}";
	String invalidMessage = "{\"requestId\":\"110dc48a-b0ee-4a64-b822-1053ab7ef276\",\"transactionData\":{\"properties\":{\"owner\":{\"ov\":null,\"nv\":\"EkStep\"},\"code\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"subject\":{\"ov\":null,\"nv\":\"literacy\"},\"portalOwner\":{\"ov\":null,\"nv\":\"EkStep\"},\"description\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"mediaType\":{\"ov\":null,\"nv\":\"content\"},\"osId\":{\"ov\":null,\"nv\":\"org.ekstep.quiz.app\"},\"mimeType\":{\"ov\":null,\"nv\":\"application/vnd.ekstep.ecml-archive\"},\"ageGroup\":{\"ov\":null,\"nv\":[\"5-6\"]},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"gradeLevel\":{\"ov\":null,\"nv\":[\"Grade 1\"]},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"developer\":{\"ov\":null,\"nv\":\"EkStep\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"contentType\":{\"ov\":null,\"nv\":\"Story\"},\"status\":{\"ov\":null,\"nv\":\"Mock\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":974,\"label\":\"शेर का साथी हाथी\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2016-07-03T15:39:34.570+0530\",\"objectType\":\"Content\"}";
	String messageWithAddedRelations = "{\"ets\":1502102183388,\"nodeUniqueId\":\"do_112276071067320320114\",\"requestId\":null,\"transactionData\":{\"removedTags\":[],\"addedRelations\":[{\"rel\":\"hasSequenceMember\",\"id\":\"do_1123032073439723521148\",\"label\":\"Test unit 11\",\"dir\":\"IN\",\"type\":\"Content\"}],\"removedRelations\":[],\"addedTags\":[],\"properties\":{}},\"operationType\":\"CREATE\",\"nodeGraphId\":105631,\"label\":\"collaborator test\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-08-07T10:36:23.388+0000\",\"objectType\":\"Content\"}";
    String messageWithRemovedRelations = "{\"ets\":1502102183388,\"nodeUniqueId\":\"do_1123032073439723521148\",\"requestId\":null,\"transactionData\":{\"removedTags\":[],\"addedRelations\":[],\"removedRelations\":[{\"rel\":\"hasSequenceMember\",\"id\":\"do_11225638792242790415\",\"label\":\"collabarotor issue\",\"dir\":\"OUT\",\"type\":\"Content\"}],\"addedTags\":[],\"properties\":{}},\"operationType\":\"UPDATE\",\"nodeGraphId\":110067,\"label\":\"Test unit 11\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-08-07T10:36:23.388+0000\",\"objectType\":\"Content\"}";
    String messageWithAddedTags = "{\"ets\":1502174143305,\"nodeUniqueId\":\"do_1123058108461793281287\",\"requestId\":null,\"transactionData\":{\"removedTags\":[],\"addedTags\":[\"test_Content\"],\"properties\":{}},\"operationType\":\"UPDATE\",\"nodeGraphId\":110196,\"label\":\"name\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-08-08T06:35:43.305+0000\",\"objectType\":\"Content\"}";
    String messageWithRemovedTags = "{\"ets\":1502174143305,\"nodeUniqueId\":\"do_1123058108461793281287\",\"requestId\":null,\"transactionData\":{\"removedTags\":[\"test_Content\"],\"addedTags\":[],\"properties\":{}},\"operationType\":\"UPDATE\",\"nodeGraphId\":110196,\"label\":\"name\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-08-08T06:35:43.305+0000\",\"objectType\":\"Content\"}";
	
	private CompositeSearchIndexerService service = new CompositeSearchIndexerService();
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
	public void test() throws Exception {
			Map<String,Object>	messageData = mapper.readValue(messageWithAddedRelations, new TypeReference<Map<String, Object>>() {
			});
		  	Map<String,String> props = new HashMap<String,String>();
			props.put("elastic-search-host", "http://localhost");
			props.put("elastic-search-port", "9200");
			props.put("platform-api-url", "http://localhost:8080/learning-service");
			props.put("ekstepPlatformApiUserId", "ilimi");
			Config config = new MapConfig(props);
			service.initialize(config);
			JobMetrics metrics = mock(JobMetrics.class);
			service.processMessage(messageData);
			Thread.sleep(2000);
			Map<String,Object> map = findById("do_112276071067320320114", client);
			System.out.println(map);
			assertEquals(true, null!=map);
	}
	
	public Map<String, Object> findById(String identifier, Client client) {
		SearchResponse response = client.prepareSearch(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX)
				.setTypes(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE).execute().actionGet();
//				.setQuery(QueryBuilders.termQuery("_id", identifier)).execute().actionGet();
		SearchHits hits = response.getHits();
		for (SearchHit hit : hits.getHits()) {
			Map<String, Object> fields = hit.getSource();
			System.out.println("fields" + fields);
			return fields;
		}
		return null;
	}
}
