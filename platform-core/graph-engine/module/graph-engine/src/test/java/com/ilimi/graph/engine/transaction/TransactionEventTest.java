package com.ilimi.graph.engine.transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.neo4j.graphdb.GraphDatabaseService;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.engine.loadtest.TestUtil;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.graph.model.node.MetadataDefinition;
import com.ilimi.graph.model.node.RelationDefinition;
import com.ilimi.kafka.util.KafkaConsumer;
import com.ilimi.kafka.util.KafkaMessage;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TransactionEventTest {

	private static String graphId = "test";
	private static KafkaMessage message;
	private static KafkaConsumer consumer;
    ActorRef reqRouter = null;
    final static String word1 = "test_word1";
    final static String word2 = "test_word2";

//	@BeforeClass
    public static void setup(){
		createGraph();
		message =  new KafkaMessage();
		consumer  = new KafkaConsumer(message);
		consumer.start();
    }
    
//	@AfterClass
	public static void teardown(){
		deleteGraph();
		consumer.stopExecuting();
	}
	
//	@Before
	public void init() throws Exception{
		reqRouter = TestUtil.initReqRouter();
		Future<Object> defNodeRes = saveDefinitionNode();
        handleFutureBlock(defNodeRes, "successful");
        Thread.sleep(5000);
	}
	
//	@After
	public void clean(){
		message.flush();
	}
	
	public static void createGraph(){
		if (!Neo4jGraphFactory.graphExists(graphId)) 
			Neo4jGraphFactory.createGraph(graphId);		
	}

	public static void deleteGraph(){
        GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
        if (null != graphDb) {
            Neo4jGraphFactory.shutdownGraph(graphId);
        }
        Neo4jGraphFactory.deleteGraph(graphId);

	}
	

	
//	@Test
	public void createNode1(){
        String nodeId = word1;
        String objectType = "Word";
        Map<String, Object> metadata = new HashMap<>();
        Future<Object> nodeReq = createDataNode(reqRouter, graphId, nodeId, objectType, metadata, null, null, false);
        handleFutureBlock(nodeReq, "failed"); //lemma is required property of node 'Word
	}
	
//	@Test
	public void createNode2(){
        try {
            String nodeId = word1;
            String objectType = "Word";
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("lemma","dummyLemmaTest"+"_"+Thread.currentThread().getId());
            Future<Object> nodeReq = createDataNode(reqRouter, graphId, nodeId, objectType, metadata, null, null, false);
            handleFutureBlock(nodeReq, "successful");
            Thread.sleep(5000);
			Map<String, Object> kafkaMsg = message.getMessage(nodeId);
			
			Assert.assertNotNull("Kafka message is null", kafkaMsg);
			//consumer.stopExecuting();
			String trans_nodeId = (String) kafkaMsg.get("nodeUniqueId");
			String trans_nodeType = (String) kafkaMsg.get("nodeType");
			String trans_objectType = (String) kafkaMsg.get("objectType");
			String trans_graphId = (String) kafkaMsg.get("graphId");
			Map <String, Object> transactionData = (Map<String, Object>) kafkaMsg.get("transactionData");
			
			Assert.assertEquals(" NodeId is not same as created NodeId", nodeId, trans_nodeId);
			
        } catch (Exception e) {
            e.printStackTrace();
        }
        
	}
	
//	@Test
	public void createNode3(){
        try {
            String nodeId = word2;
            String objectType = "Word";
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("lemma","dummyLemma2");
            metadata.put("grade","3");
            metadata.put("thresholdLevel","3");
            Future<Object> nodeReq = createDataNode(reqRouter, graphId, nodeId, objectType, metadata, null, null, false);
            handleFutureBlock(nodeReq, "successful");
            Thread.sleep(5000);
			Map<String, Object> kafkaMsg = message.getMessage(nodeId);
			
			Assert.assertNotNull("Kafka message is null", kafkaMsg);
			//consumer.stopExecuting();
			String trans_nodeId = (String) kafkaMsg.get("nodeUniqueId");
			String trans_nodeType = (String) kafkaMsg.get("nodeType");
			String trans_objectType = (String) kafkaMsg.get("objectType");
			String trans_graphId = (String) kafkaMsg.get("graphId");
			Map <String, Object> transactionData = (Map<String, Object>) kafkaMsg.get("transactionData");
			
			Assert.assertEquals(" NodeId is not same as created NodeId", nodeId, trans_nodeId);
			
        } catch (Exception e) {
            e.printStackTrace();
        }
        
	}
	
//	@Test
	public void updateNode() {
		
        try {
            String nodeId = word1;
            String objectType = "Word";
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("grade","1");
            metadata.put("thresholdLevel","5");
            Future<Object> nodeReq = createDataNode(reqRouter, graphId, nodeId, objectType, metadata, null, null, true);
            handleFutureBlock(nodeReq, "successful");
			Thread.sleep(5000);
			Map<String, Object> kafkaMsg = message.getMessage(nodeId);
			
			Assert.assertNotNull("Kafka message is null", kafkaMsg);
			//consumer.stopExecuting();
			String trans_nodeId = (String) kafkaMsg.get("nodeUniqueId");
			String trans_objectType = (String) kafkaMsg.get("objectType");
			Map <String, Object> transactionData = (Map<String, Object>) kafkaMsg.get("transactionData");
			
			Assert.assertEquals(" NodeId is not same as updated NodeId", nodeId, trans_nodeId);
			Assert.assertEquals(" Node ObjectType is not same as created Node ObjectType", "Word", trans_objectType);
			
			Assert.assertNotNull("transaction data is empty", transactionData);
			Map<String , Object> properties = (Map<String, Object>) transactionData.get("properties");
			Assert.assertNotNull("transaction properties is empty", properties);
			Map<String, String> trans_prop_grade = (Map<String, String>) properties.get("grade");
			Assert.assertNotNull("updated property(grade) is empty", trans_prop_grade);
			Map<String, String> trans_prop_level = (Map<String, String>) properties.get("thresholdLevel");
			Assert.assertNotNull("updated property(thresholdLevel) is empty", trans_prop_level);
			
        } catch (Exception e) {
			e.printStackTrace();
		}

	}
	
//	@Test
	public void updateNodeWithRelatoin() {
		

        try {
            String nodeId = word1;
            String objectType = "Word";
            Map<String, Object> metadata = new HashMap<>();
            
            String nodeId2 = word2;
            
            Relation rel = new Relation();
            rel.setStartNodeId(nodeId);
            rel.setStartNodeObjectType("Word");
            rel.setEndNodeId(nodeId2);
            rel.setEndNodeObjectType("Word");
            rel.setRelationType("hasAntonym");
            
            Future<Object> nodeReq = createDataNode(reqRouter, graphId, nodeId, objectType, metadata, Arrays.asList(rel), null, true);
            handleFutureBlock(nodeReq, "successful");
			Thread.sleep(10000);
			Map<String, Object> kafkaMsg1 = message.getMessage(nodeId);
			Map<String, Object> kafkaMsg2 = message.getMessage(nodeId2);
			
			Assert.assertNotNull("Kafka message is null for start nodeID", kafkaMsg1);
			Assert.assertNotNull("Kafka message is null for end nodeID", kafkaMsg2);
			//consumer.stopExecuting();
			String trans_nodeId1 = (String) kafkaMsg1.get("nodeUniqueId");
			String trans_objectType1 = (String) kafkaMsg1.get("objectType");
			Map <String, Object> transactionData1 = (Map<String, Object>) kafkaMsg1.get("transactionData");
			
			Assert.assertEquals(" start NodeId is not same", nodeId, trans_nodeId1);

			Assert.assertNotNull("transaction data is empty", transactionData1);
			List<Map<String, Object>> addedRelations = (List<Map<String, Object>>) transactionData1.get("addedRelations");
			Assert.assertNotNull("addedRelations is empty", addedRelations);
			
			String trans_nodeId2 = (String) kafkaMsg2.get("nodeUniqueId");
			String trans_objectType2 = (String) kafkaMsg2.get("objectType");
			Map <String, Object> transactionData2 = (Map<String, Object>) kafkaMsg2.get("transactionData");
			
			Assert.assertEquals(" end NodeId is not same", nodeId2, trans_nodeId2);

			Assert.assertNotNull("transaction data is empty", transactionData2);
			List<Map<String, Object>> addedRelations2 = (List<Map<String, Object>>) transactionData2.get("addedRelations");
			Assert.assertNotNull("addedRelations is empty", addedRelations2);
			
        } catch (Exception e) {
			e.printStackTrace();
		}

	}
	
//	@Test
	public void updateNodeWithRelatoinRemoval() {
		

        try {
            String nodeId = word1;
            String objectType = "Word";
            Map<String, Object> metadata = new HashMap<>();
            
            String nodeId2 = word2;
            
            Relation rel = new Relation();
            rel.setStartNodeId(nodeId);
            rel.setStartNodeObjectType("Word");
            rel.setEndNodeId(nodeId2);
            rel.setEndNodeObjectType("Word");
            rel.setRelationType("hasHypernym");
            
            Future<Object> nodeReq = createDataNode(reqRouter, graphId, nodeId, objectType, metadata, Arrays.asList(rel), null, true);
            handleFutureBlock(nodeReq, "successful");
			Thread.sleep(10000);
			Map<String, Object> kafkaMsg1 = message.getMessage(nodeId);
			Map<String, Object> kafkaMsg2 = message.getMessage(nodeId2);
			
			Assert.assertNotNull("Kafka message is null for start nodeID", kafkaMsg1);
			Assert.assertNotNull("Kafka message is null for end nodeID", kafkaMsg2);
			//consumer.stopExecuting();
			String trans_nodeId1 = (String) kafkaMsg1.get("nodeUniqueId");
			String trans_objectType1 = (String) kafkaMsg1.get("objectType");
			Map <String, Object> transactionData1 = (Map<String, Object>) kafkaMsg1.get("transactionData");
			
			Assert.assertEquals(" start NodeId is not same", nodeId, trans_nodeId1);

			Assert.assertNotNull("transaction data is empty", transactionData1);
			List<Map<String, Object>> addedRelations = (List<Map<String, Object>>) transactionData1.get("addedRelations");
			Assert.assertNotNull("addedRelations is empty", addedRelations);
			
			String trans_nodeId2 = (String) kafkaMsg2.get("nodeUniqueId");
			String trans_objectType2 = (String) kafkaMsg2.get("objectType");
			Map <String, Object> transactionData2 = (Map<String, Object>) kafkaMsg2.get("transactionData");
			
			Assert.assertEquals(" end NodeId is not same", nodeId2, trans_nodeId2);

			Assert.assertNotNull("transaction data is empty", transactionData2);
			List<Map<String, Object>> removedRelations = (List<Map<String, Object>>) transactionData2.get("removedRelations");
			Assert.assertNotNull("removedRelations is empty", removedRelations);
			
        } catch (Exception e) {
			e.printStackTrace();
		}

	}
	
//	@Test
	public void updateNodeWithTags() {
		
        try {
            String nodeId = word1;
            String objectType = "Word";
            Map<String, Object> metadata = new HashMap<>();
            
            List<String> tags = new ArrayList<>();
            tags.add("TestLanguage");
            tags.add("API");
            
            Future<Object> nodeReq = createDataNode(reqRouter, graphId, nodeId, objectType, metadata, null, tags,  true);
            handleFutureBlock(nodeReq, "successful");
			Thread.sleep(5000);
			Map<String, Object> kafkaMsg = message.getMessage(nodeId);
			
			Assert.assertNotNull("Kafka message is null", kafkaMsg);
			//consumer.stopExecuting();
			String trans_nodeId = (String) kafkaMsg.get("nodeUniqueId");
			String trans_objectType = (String) kafkaMsg.get("objectType");
			Map <String, Object> transactionData = (Map<String, Object>) kafkaMsg.get("transactionData");
			
			Assert.assertEquals(" NodeId is not same as updated NodeId", nodeId, trans_nodeId);
			
			Assert.assertNotNull("transaction data is empty", transactionData);
			List<String> addedTags = (List<String>) transactionData.get("addedTags");
			Assert.assertNotNull("addedTags is empty", addedTags);

        } catch (Exception e) {
			e.printStackTrace();
		}

	}
	
//	@Test
	public void updateNodeWithTagsRemoval() {
		
        try {
            String nodeId = word1;
            String objectType = "Word";
            Map<String, Object> metadata = new HashMap<>();
            
            List<String> tags = new ArrayList<>();
            tags.add("TestLanguage");
            
            Future<Object> nodeReq = createDataNode(reqRouter, graphId, nodeId, objectType, metadata, null, tags,  true);
            handleFutureBlock(nodeReq, "successful");
			Thread.sleep(5000);
			Map<String, Object> kafkaMsg = message.getMessage(nodeId);
			
			Assert.assertNotNull("Kafka message is null", kafkaMsg);
			//consumer.stopExecuting();
			String trans_nodeId = (String) kafkaMsg.get("nodeUniqueId");
			String trans_objectType = (String) kafkaMsg.get("objectType");
			Map <String, Object> transactionData = (Map<String, Object>) kafkaMsg.get("transactionData");
			
			Assert.assertEquals(" NodeId is not same as updated NodeId", nodeId, trans_nodeId);
			
			Assert.assertNotNull("transaction data is empty", transactionData);
			List<String> removedTags = (List<String>) transactionData.get("removedTags");
			Assert.assertNotNull("removedTags is empty", removedTags);

        } catch (Exception e) {
			e.printStackTrace();
		}

	}
	
    private Future<Object> saveDefinitionNode() {
        Request request = new Request();
        request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
        request.setManagerName(GraphEngineManagers.NODE_MANAGER);
        request.setOperation("saveDefinitionNode");
        DefinitionDTO dto = new DefinitionDTO();
        dto.setObjectType("Word");
        MetadataDefinition prop= new MetadataDefinition();
        prop.setPropertyName("lemma");
        prop.setTitle("Word");
        prop.setDataType("Text");
        prop.setRequired(true);
        prop.setDefaultValue("");
        dto.setProperties(Arrays.asList(prop));
        RelationDefinition antRelationDef = new RelationDefinition();
        antRelationDef.setObjectTypes(Arrays.asList("Word"));
        antRelationDef.setRequired(false);
        antRelationDef.setRelationName("hasAntonym");
        antRelationDef.setTitle("antonyms");
        
        RelationDefinition hypRelationDef = new RelationDefinition();
        hypRelationDef.setObjectTypes(Arrays.asList("Word"));
        hypRelationDef.setRequired(false);
        hypRelationDef.setRelationName("hasHypernym");
        hypRelationDef.setTitle("hypernyms");
        
        dto.setOutRelations(Arrays.asList(antRelationDef, hypRelationDef));
        request.put(GraphDACParams.definition_node.name(), dto);
        Future<Object> req = Patterns.ask(reqRouter, request, TestUtil.timeout);
        return req;
    }
    
    private Future<Object> createDataNode(ActorRef reqRouter, String graphId, String nodeId, String objectType, Map<String, Object> metadata, List<Relation> outRel,  List<String> tags, boolean isUpdate) {
        Request request = new Request();
        request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
        request.getContext().put(GraphHeaderParams.request_id.name(), "REQUEST_"+Thread.currentThread().getId());
        request.setManagerName(GraphEngineManagers.NODE_MANAGER);
        if(isUpdate)
        	request.setOperation("updateDataNode");
        else
        	request.setOperation("createDataNode");
        Node node = new Node(graphId, metadata);
        node.setIdentifier(nodeId);
        node.setObjectType(objectType);
        if(outRel!=null) 
        	node.setOutRelations(outRel);
        request.put(GraphDACParams.node.name(), node);
        request.put(GraphDACParams.node_id.name(), nodeId);
        Future<Object> req = Patterns.ask(reqRouter, request, TestUtil.timeout);
        return req;
    }
	
    public void handleFutureBlock(Future<Object> req , String status) {
        try {
            Object arg1 = Await.result(req, TestUtil.timeout.duration());
            if (arg1 instanceof Response) {
                Response response = (Response) arg1;
        		Assert.assertEquals(status, response.getParams().getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
