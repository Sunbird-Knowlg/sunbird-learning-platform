package org.ekstep.jobs.samza.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.jobs.samza.service.CompositeSearchIndexerService;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import info.aduna.io.FileUtil;

public class CompositeSearchServiceTest {

	String validMessage = "{\"nodeUniqueId\":\"org.ekstep.jul03.story.test01\",\"requestId\":\"110dc48a-b0ee-4a64-b822-1053ab7ef276\",\"transactionData\":{\"properties\":{\"owner\":{\"ov\":null,\"nv\":\"EkStep\"},\"code\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"subject\":{\"ov\":null,\"nv\":\"literacy\"},\"portalOwner\":{\"ov\":null,\"nv\":\"EkStep\"},\"description\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"mediaType\":{\"ov\":null,\"nv\":\"content\"},\"osId\":{\"ov\":null,\"nv\":\"org.ekstep.quiz.app\"},\"mimeType\":{\"ov\":null,\"nv\":\"application/vnd.ekstep.ecml-archive\"},\"ageGroup\":{\"ov\":null,\"nv\":[\"5-6\"]},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"gradeLevel\":{\"ov\":null,\"nv\":[\"Grade 1\"]},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"developer\":{\"ov\":null,\"nv\":\"EkStep\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"contentType\":{\"ov\":null,\"nv\":\"Story\"},\"status\":{\"ov\":null,\"nv\":\"Mock\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":974,\"label\":\"शेर का साथी हाथी\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2016-07-03T15:39:34.570+0530\",\"objectType\":\"Content\"}";
	String invalidMessage = "{\"requestId\":\"110dc48a-b0ee-4a64-b822-1053ab7ef276\",\"transactionData\":{\"properties\":{\"owner\":{\"ov\":null,\"nv\":\"EkStep\"},\"code\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"subject\":{\"ov\":null,\"nv\":\"literacy\"},\"portalOwner\":{\"ov\":null,\"nv\":\"EkStep\"},\"description\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"mediaType\":{\"ov\":null,\"nv\":\"content\"},\"osId\":{\"ov\":null,\"nv\":\"org.ekstep.quiz.app\"},\"mimeType\":{\"ov\":null,\"nv\":\"application/vnd.ekstep.ecml-archive\"},\"ageGroup\":{\"ov\":null,\"nv\":[\"5-6\"]},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"gradeLevel\":{\"ov\":null,\"nv\":[\"Grade 1\"]},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"developer\":{\"ov\":null,\"nv\":\"EkStep\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"contentType\":{\"ov\":null,\"nv\":\"Story\"},\"status\":{\"ov\":null,\"nv\":\"Mock\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":974,\"label\":\"शेर का साथी हाथी\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2016-07-03T15:39:34.570+0530\",\"objectType\":\"Content\"}";
	String messageWithRelations = "{\"ets\":1502102183388,\"nodeUniqueId\":\"do_112276071067320320114\",\"requestId\":null,\"transactionData\":{\"removedTags\":[],\"addedRelations\":[{\"rel\":\"hasSequenceMember\",\"id\":\"do_1123032073439723521148\",\"label\":\"Test unit 11\",\"dir\":\"IN\",\"type\":\"Content\"}],\"removedRelations\":[],\"addedTags\":[],\"properties\":{}},\"operationType\":\"UPDATE\",\"nodeGraphId\":105631,\"label\":\"collaborator test\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2017-08-07T10:36:23.388+0000\",\"objectType\":\"Content\"}";

	CompositeSearchIndexerService service = new CompositeSearchIndexerService();
	static File tempDir = null;
	static Settings settings = null;
	static Node server = null;
	static String clusterName = "";
	Map<String, Object> messageData = new HashMap<String, Object>();
	ObjectMapper mapper = new ObjectMapper();

	public void compositeSearchTest(String message) throws JsonParseException, JsonMappingException, IOException {
		messageData = mapper.readValue(message, new TypeReference<Map<String, Object>>() {
		});
	}

	@BeforeClass
	public static void beforeClass() {
		tempDir = new File(System.getProperty("user.dir") + "/tmp");
		settings = Settings.builder().put("path.home", tempDir.getAbsolutePath()).build();
		server = NodeBuilder.nodeBuilder().settings(settings).build();
		clusterName = server.settings().get("cluster.name");
		server.start();
	}

	@AfterClass
	public static void afterClass() throws IOException {
		server.close();
		FileUtil.deleteDir(tempDir);
	}

	@Test
	public void addPropertiesToIndex() throws IOException, InterruptedException {
		compositeSearchTest(validMessage);
		Map<String, Object> definitionNode = new HashMap<String, Object>();
		Map<String, String> relationDefinition = new HashMap<String, String>();
		Map<String, Object> event = service.getIndexDocument(messageData, definitionNode, relationDefinition, false);
		Client client = server.client();
		ElasticSearchUtil util = new ElasticSearchUtil(client);
		util.add(event);
		Thread.sleep(2000);
		Map<String, Object> result = util.findById("org.ekstep.jul03.story.test01");
		assertEquals(result.get("graph_id"), "domain");
		assertEquals(result.containsKey("ageGroup"), true);
		assertEquals("EkStep", (String) result.get("owner"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addRelationsToIndex() throws IOException, InterruptedException {
		compositeSearchTest(messageWithRelations);
		Map<String, Object> definitionNode = new HashMap<String, Object>();
		Map<String, String> relationDefinition = new HashMap<String, String>();
		relationDefinition.put("OUT_Library_pre-requisite", "libraries");
		relationDefinition.put("OUT_Concept_associatedTo", "concepts");
		relationDefinition.put("OUT_Content_associatedTo", "screenshots");
		relationDefinition.put("OUT_Content_hasSequenceMember", "children");
		relationDefinition.put("OUT_ItemSet_associatedTo", "item_sets");
		relationDefinition.put("OUT_Method_associatedTo", "methods");
		relationDefinition.put("IN_Content_hasSequenceMember", "collections");

		Map<String, Object> event = service.getIndexDocument(messageData, definitionNode, relationDefinition, false);
		Client client = server.client();
		ElasticSearchUtil util = new ElasticSearchUtil(client);
		util.add(event);
		Thread.sleep(2000);
		Map<String, Object> result = util.findById("do_112276071067320320114");
		assertEquals(result.get("graph_id"), "domain");
		List<String> collection = (List<String>) result.get("collections");
		assertEquals("do_1123032073439723521148", collection.get(0));

	}

}
