package com.ilimi.graph.dac.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.ekstep.graph.service.IGraphDatabaseService;
import org.ekstep.graph.service.factory.GraphServiceFactory;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Graph;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchCriteria;

@Ignore
@FixMethodOrder(MethodSorters.DEFAULT)
public class GraphPolicyTest {
	
	static String graphId =  "domain";
	static String graphId1 =  "123";
	static IGraphDatabaseService graphDb = GraphServiceFactory.getDatabaseService();
	String relationType = "ASSOCIATED_TO"; 
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void init(){
		graphDb.createGraph(graphId, null);
	}
	
	@Test
	public void createUniqueConstraint(){	
		List<String> indexProperties = new ArrayList<String>();
		indexProperties.add("Node");
		Request request = new Request();
		request.setId(graphId);
		graphDb.createGraphUniqueContraint(graphId, indexProperties, request);
	}
	
	@Test(expected= org.neo4j.driver.v1.exceptions.ClientException.class)
	public void createUniqueConstraintWithoutIndexProperties(){
		
		List<String> indexProperties = new ArrayList<String>();
		Request request = new Request();
		request.setId(graphId);
		graphDb.createGraphUniqueContraint(graphId, indexProperties, request);
	}
	
	@Test
	public void createIndex(){
		
		List<String> indexProperties = new ArrayList<String>();
		indexProperties.add("label");
		Request request = new Request();
		request.setId(graphId);
		graphDb.createIndex(graphId, indexProperties, request);
	}	
	
	@Test(expected= org.neo4j.driver.v1.exceptions.ClientException.class)
	public void createIndexWithoutIndexProperties(){
		
		List<String> indexProperties = new ArrayList<String>();
		Request request = new Request();
		request.setId(graphId);
		graphDb.createIndex(graphId, indexProperties, request);
	}	
	
	@Test
	public void createNode_1(){
		String IL_UNIQUE_ID = "JAVA001";
		Node node = new Node();
		node.setGraphId(graphId);
		node.setIdentifier(IL_UNIQUE_ID);
		node.setMetadata(setMetadata());
		node.setNodeType("DEFINITION_NODE");
		node.setObjectType("Content");
		Request request = new Request();
		request.setId(graphId);
		Node res = graphDb.addNode(graphId, node, request);
		assertEquals(graphId, res.getGraphId());
		assertEquals(IL_UNIQUE_ID, res.getIdentifier());
		assertEquals("Content", res.getObjectType());
	}
	
	@Test
	public void createNode_2(){
		String IL_UNIQUE_ID = "SCALA001";
		Node node = new Node();
		node.setGraphId(graphId);
		node.setIdentifier(IL_UNIQUE_ID);
		node.setNodeType("DEFINITION_NODE");
		node.setMetadata(setNodeMetadata());
		node.setObjectType("Content");
		Request request = new Request();
		request.setId(graphId);
		Node res = graphDb.addNode(graphId, node, request);
		assertEquals(graphId, res.getGraphId());
		assertEquals(IL_UNIQUE_ID, res.getIdentifier());
		assertEquals("Content", res.getObjectType());
	}
	
	@Test
	public void createNode_3(){
		String IL_UNIQUE_ID = "DotNet001";
		Node node = new Node();
		node.setGraphId(graphId);
		node.setIdentifier(IL_UNIQUE_ID);
		node.setNodeType("DEFINITION_NODE");
		node.setMetadata(setNodeData());
		node.setObjectType("Content");
		Request request = new Request();
		request.setId(graphId);
		Node res = graphDb.addNode(graphId, node, request);
		assertEquals(graphId, res.getGraphId());
		assertEquals(IL_UNIQUE_ID, res.getIdentifier());
		assertEquals("Content", res.getObjectType());
		
	}
	
	@Test
	public void createNode_4(){
		String IL_UNIQUE_ID = "python001";
		Node node = new Node();
		node.setGraphId(graphId);
		node.setIdentifier(IL_UNIQUE_ID);
		node.setNodeType("DEFINITION_NODE");
		node.setMetadata(setData());
		node.setObjectType("Content");
		Request request = new Request();
		request.setId(graphId);
		Node res = graphDb.addNode(graphId, node, request);
		assertEquals(graphId, res.getGraphId());
		assertEquals(IL_UNIQUE_ID, res.getIdentifier());
		assertEquals("Content", res.getObjectType());
	}
	
	@Test
	public void createNode(){
		String IL_UNIQUE_ID = "python0013";
		Node node = new Node();
		node.setGraphId(graphId);
		node.setIdentifier(IL_UNIQUE_ID);
		node.setNodeType("DEFINITION_NODE");
		node.setMetadata(setData());
		node.setObjectType("Content");
		Request request = new Request();
		request.setId(graphId);
		Node res = graphDb.addNode(graphId, node, request);
		assertEquals(graphId, res.getGraphId());
		assertEquals(IL_UNIQUE_ID, res.getIdentifier());
		assertEquals("Content", res.getObjectType());
		
	}

	@Test
	public void createNode_5(){
		String IL_UNIQUE_ID = "Spring001";
		Node node = new Node();
		node.setGraphId(graphId);
		node.setIdentifier(IL_UNIQUE_ID);
		node.setNodeType("DEFINITION_NODE");
		node.setMetadata(set_Data());
		node.setObjectType("Content");
		Request request = new Request();
		request.setId(graphId);
		Node res = graphDb.addNode(graphId, node, request);
		assertEquals(graphId, res.getGraphId());
		assertEquals(IL_UNIQUE_ID, res.getIdentifier());
		assertEquals("Content", res.getObjectType());
		
	}
	
	@Test
	public void createNodeWithoutIdentifier(){
		Node node = new Node();
		node.setGraphId(graphId);
		node.setNodeType("DEFINITION_NODE");
		node.setMetadata(setData());
		node.setObjectType("Content");
		Request request = new Request();
		request.setId(graphId);
		Node res = graphDb.addNode(graphId, node, request);
		assertEquals(graphId, res.getGraphId());
		assertEquals("Content", res.getObjectType());
	}
	
	@Test
	public void createNodeWithoutObjectType(){
		String IL_UNIQUE_ID = "SCALA0012";
		Node node = new Node();
		node.setGraphId(graphId);
		node.setIdentifier(IL_UNIQUE_ID);
		node.setNodeType("DEFINITION_NODE");
		node.setMetadata(setData());
		Request request = new Request();
		request.setId(graphId);
		Node res = graphDb.addNode(graphId, node, request);
		assertEquals(graphId, res.getGraphId());
		assertEquals(IL_UNIQUE_ID, res.getIdentifier());
	}
	
	@Test
	public void createNodeWithNonExistingNodeType(){
		String IL_UNIQUE_ID = "SCALA001";
		Node node = new Node();
		node.setGraphId(graphId);
		node.setIdentifier(IL_UNIQUE_ID);
		node.setNodeType("TEST_NODE");
		node.setMetadata(setData());
		Request request = new Request();
		request.setId(graphId);
		Node res = graphDb.addNode(graphId, node, request);
		assertEquals(graphId, res.getGraphId());
		assertEquals("Content", res.getObjectType());
	}
	
	
	@Test
	public void createNodeNonExistingObjectType(){
		String IL_UNIQUE_ID = "SCALA001";
		Node node = new Node();
		node.setGraphId(graphId);
		node.setIdentifier(IL_UNIQUE_ID);
		node.setNodeType("DEFINITION_NODE");
		node.setObjectType("testObject");
		node.setMetadata(setData());
		Request request = new Request();
		request.setId(graphId);
		Node res = graphDb.addNode(graphId, node, request);
		assertEquals(graphId, res.getGraphId());
		assertEquals(true, res.getObjectType()!="Content");
	}

	@Test
	public void createNodeWithoutNodeType(){
		Node node = new Node();
		String IL_UNIQUE_ID = "SCALA001";
		node.setGraphId(graphId);
		node.setIdentifier(IL_UNIQUE_ID);
		node.setMetadata(setData());
		node.setNodeType("DEFINITION_NODE");
		node.setObjectType("Content");
		Request request = new Request();
		request.setId(graphId);
		Node res = graphDb.addNode(graphId, node, request);
		assertEquals(graphId, res.getGraphId());
		assertEquals("Content", res.getObjectType());
	}
	
	@Test(expected = NullPointerException.class)
	public void createNodeWithoutMetadata(){
		Node node = new Node();
		String IL_UNIQUE_ID = "SCALA001";
		node.setGraphId(graphId);
		node.setNodeType("DEFINITION_NODE");
		node.setIdentifier(IL_UNIQUE_ID);
		node.setNodeType("DEFINITION_NODE");
		node.setObjectType("Content");
		Request request = new Request();
		request.setId(graphId);
		Node res = graphDb.addNode(graphId, node, request);
		assertEquals(graphId, res.getGraphId());
		assertEquals("Content", res.getObjectType());
	}
	
	@Test(expected= ClientException.class)
	public void createNodeWithoutGraphId(){
		Node node = new Node();
		String graphId = "";
		String IL_UNIQUE_ID = "SCALA001";
		node.setNodeType("DEFINITION_NODE");
		node.setIdentifier(IL_UNIQUE_ID);
		node.setNodeType("DEFINITION_NODE");
		node.setObjectType("Content");
		Request request = new Request();
		request.setId(graphId);
		Node res = graphDb.addNode(null, node, request);
		assertEquals(graphId, res.getGraphId());
		assertEquals("Content", res.getObjectType());
	}

	@Test
	public void updateExistingNode(){
		Request request = new Request();
		request.setId(graphId);
		Node  node = graphDb.getNodeByUniqueId(graphId, "JAVA001", null, request);
		List<String> tags = new ArrayList<String>();
		tags.add("Language");
		tags.add("Programming Language");
		node.setTags(tags);
		Node  res = graphDb.updateNode(graphId, node, request);
		assertEquals(true , CollectionUtils.containsAny(tags, res.getTags()));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void updateNewNode(){
		Request request = new Request();
		request.setId(graphId);
		List<String> tags = new ArrayList<String>();
		Node node = new Node();
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("Title", "Learn Scala Language");
		map.put("code", "Java00122");
		node.setMetadata(map);
		tags.add("Language");
		tags.add("Programming Language");
		node.setTags(tags);
		node.setGraphId(graphId);
		node.setIdentifier("Java00122");
		Node  res = graphDb.updateNode(graphId, node, request);
	}
	
	@Test
	public void upsertExistingNode(){
		Request request = new Request();
		request.setId(graphId);
		Node node = new Node();
		node.setGraphId(graphId);
		node.setIdentifier("SCALA001");
		List<String> tags = new ArrayList<String>();
		tags.add("programming languages");
		Map<String, Object> scalaMap = new HashMap<String,Object>();
		scalaMap.put("Title", "Learn Scala Language");
		node.setMetadata(scalaMap);
		tags.add("oops");
		node.setTags(tags);
		Node res =  graphDb.upsertNode(graphId, node, request);
		assertEquals(tags, res.getTags());
	}
	
	@Test 
	public void upsertNewNode(){
		Request request = new Request();
		request.setId(graphId);
		Node node = new Node();
		node.setGraphId(graphId);
		node.setIdentifier("SCALA0012");
		node.setObjectType("content");
		node.setNodeType("DATA_NODE");
		List<String> tags = new ArrayList<String>();
		tags.add("programming languages");
		Map<String, Object> scalaMap = new HashMap<String,Object>();
		scalaMap.put("Title", "Learn Scala Language");
		scalaMap.put("description", "testing new language");
		node.setMetadata(scalaMap);
		Node res =  graphDb.upsertNode(graphId, node, request);
		assertEquals(scalaMap.get("title"), res.getMetadata().get("title"));
	}
	
	@Test
	public void updatePropertyValue(){
		Request request = new Request();
		request.setId(graphId);
		Property props = new Property();
		props.setPropertyName("Title");
		props.setPropertyValue("Learn Language");
		graphDb.updatePropertyValue(graphId, "SCALA001", props, request);
		Property res = graphDb.getNodeProperty(graphId, "SCALA001", "Title", request);
		assertEquals("Title", res.getPropertyName());
	}
	
	@Test
	public void updatePropertyValues(){
		String nodeId = "python001";
		Request request = new Request();
		request.setId(graphId);
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("Status", "Completed");
		map.put("NoOfChapters", "37");
		graphDb.updatePropertyValues(graphId, nodeId, map, request);
		Property res = graphDb.getNodeProperty(graphId, "python001", "NoOfChapters", request);
		assertEquals("NoOfChapters", res.getPropertyName());
	}
	
	@Test
	public void removePropertyValue(){
		String nodeId = "python001";
		Request request = new Request();
		request.setId(graphId);
		graphDb.removePropertyValue(graphId, nodeId, "Title", request);
		Property res = graphDb.getNodeProperty(graphId, nodeId, "Title", request);
		assertEquals("Title", res.getPropertyName());
		assertEquals("NULL", res.getPropertyValue().toString());
	}
	
//	@AfterClass
	public static void deleteNode(){
		String nodeId = "DotNet001";
		Request request = new Request();
		request.setId(graphId);
		graphDb.deleteNode(graphId, nodeId, request);
		Node node = graphDb.getNodeByUniqueId(graphId, nodeId, false, request);
		assertEquals(null, node.getIdentifier());
	}
	
	@Test
	public void upsertRootNode(){
		Request request = new Request();
		request.setId(graphId);
		Map<String, Object> scalaMap = new HashMap<String,Object>();
		scalaMap.put("Title", "Learn java as Programming Language");
        request.setRequest(scalaMap);
		Node res =  graphDb.upsertRootNode(graphId, request);
		System.out.println(res.getIdentifier()+ res.getMetadata());
	}
	
	@Test
	public void createRelations(){
		Request request = new Request();
		request.setId(graphId);
		graphDb.createRelation(graphId, "JAVA001", "SCALA001", relationType, request);
		Relation rel = graphDb.getRelation(graphId,  "JAVA001", relationType, "SCALA001", request);
		assertEquals("SCALA001", rel.getEndNodeId());
		assertEquals(relationType, rel.getRelationType());
	}
	
	@Test(expected= org.neo4j.driver.v1.exceptions.ClientException.class)
	public void createRelationsWithoutRelationType(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "";
		graphDb.createRelation(graphId, "JAVA001", relationType,  "SCALA001", request);
	}
	
	@Test
	public void createRelationsWithNonExistingRelationType(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "INHERITED_FROM";
		graphDb.createRelation(graphId, "JAVA001", relationType,  "SCALA001", request);
	}
	
	@Test(expected= org.neo4j.driver.v1.exceptions.ClientException.class)
	public void createRelationsWithoutStartNode(){
		Request request = new Request();
		request.setId(graphId);
		String startNode = "";
		graphDb.createRelation(graphId, startNode, relationType,  "SCALA001", request);
	}
	
	@Test(expected= org.neo4j.driver.v1.exceptions.ClientException.class)
	public void createRelationsWithoutEndNode(){
		Request request = new Request();
		request.setId(graphId);
		String endNode = "";
		graphDb.createRelation(graphId,  "JAVA001", relationType, endNode, request);
	}
	
	@Test
	public void createRelationsWithNonExistingStartNode(){
		Request request = new Request();
		request.setId(graphId);
		graphDb.createRelation(graphId,  "JAVA123", relationType, "SCALA001", request);
	}
	
	@Test
	public void createRelationsWithNonExistingEndNode(){
		Request request = new Request();
		request.setId(graphId);
		graphDb.createRelation(graphId,  "JAVA001", relationType, "SCALA123", request);
	}
	
	@Test(expected= org.neo4j.driver.v1.exceptions.ClientException.class)
	public void createRelationsWithoutGraphId(){
		Request request = new Request();
		request.setId(graphId);
		String graphId = "";
		graphDb.createRelation(graphId,  "JAVA001", relationType, "SCALA001", request);
	}
	
	@Test
	public void createExistingRelation(){
		Request request = new Request();
		request.setId(graphId);
		graphDb.createRelation(graphId, "JAVA001", "SCALA001", relationType, request);
	}
	
	@Test
	public void deleteRelation(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "isParentOf";
		graphDb.createRelation(graphId, "JAVA001", "SCALA001", relationType, request);
		graphDb.deleteRelation(graphId, "JAVA001", "SCALA001", relationType, request);
		
	}
	
	@Test(expected= org.neo4j.driver.v1.exceptions.ClientException.class)
	public void deleteRelationsWithoutRelationType(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "";
		graphDb.createRelation(graphId, "JAVA001", "SCALA001", "isParentOf", request);
		graphDb.deleteRelation(graphId, "JAVA001", "SCALA001", relationType, request);
		
	}
	
	@Test
	public void deleteRelationsWithNonExistingRelationType(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "isParentOf";
		graphDb.createRelation(graphId, "JAVA001", "SCALA001", relationType, request);
		graphDb.deleteRelation(graphId, "JAVA001", "SCALA001", "isParentOff", request);
		Relation rel = graphDb.getRelation(graphId, "JAVA001", relationType, "SCALA001", request);
		assertEquals(false, rel.getEndNodeId().isEmpty());
		assertEquals("SCALA001", rel.getEndNodeId());
		assertEquals(relationType, rel.getRelationType());
	}
	
	@Test(expected= org.neo4j.driver.v1.exceptions.ClientException.class)
	public void deleteRelationsWithoutStartNode(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "isParentOf";
		graphDb.createRelation(graphId, "JAVA001", "SCALA001", relationType, request);
		graphDb.deleteRelation(graphId, "", "SCALA001", relationType, request);
	}
	
	@Test(expected= org.neo4j.driver.v1.exceptions.ClientException.class)
	public void deleteRelationsWithoutEndNode(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "isParentOf";
		String endNode = "";
		graphDb.deleteRelation(graphId, "JAVA001", endNode, relationType, request);
	}
	
	@Test
	public void deleteRelationsWithNonExistingStartNode(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "isParentOf";
		graphDb.createRelation(graphId, "JAVA001", "SCALA001", relationType, request);
		graphDb.deleteRelation(graphId, "JAVA0012", "SCALA001", relationType, request);
	}
	
	@Test(expected= org.neo4j.driver.v1.exceptions.ClientException.class)
	public void deleteRelationsWithNonExistingEndNode(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "isParentOf";
		String endNode = "";
		graphDb.deleteRelation(graphId, "JAVA001", endNode, relationType, request);
	}
	
	@Test(expected= org.neo4j.driver.v1.exceptions.ClientException.class)
	public void deleteRelationsWithoutGraphId(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "isParentOf";
		graphDb.deleteRelation("", "JAVA001", "SCALA001", relationType, null);
	}
	
	@Test
	public void updateRelation(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "isParentOf";
		String updateRel = "associated_to";
		graphDb.createRelation(graphId, "JAVA001", "SCALA001", relationType, request);
		graphDb.updateRelation(graphId, "JAVA001", "SCALA001", updateRel, request);
	
	}
	
	@Test(expected= org.neo4j.driver.v1.exceptions.ClientException.class)
	public void updateRelationsWithoutRelationType(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "";
		graphDb.createRelation(graphId, "JAVA001", "SCALA001", "isParentOf", request);
		graphDb.updateRelation(graphId, "JAVA001", "SCALA001", relationType, request);
	}
	
	@Test
	public void updateRelationsWithNonExistingRelationType(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "isParentOf";
		graphDb.createRelation(graphId, "JAVA001", "SCALA001", relationType, request);
		graphDb.updateRelation(graphId, "JAVA001", "SCALA001", "isParentOff", request);
	}
	
	@Test(expected= org.neo4j.driver.v1.exceptions.ClientException.class)
	public void updateRelationsWithoutStartNode(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "isParentOf";
		graphDb.createRelation(graphId, "JAVA001", "SCALA001", relationType, request);
		graphDb.updateRelation(graphId, "", "SCALA001", relationType, request);
	}
	
	@Test(expected= org.neo4j.driver.v1.exceptions.ClientException.class)
	public void updateRelationsWithoutEndNode(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "isParentOf";
		String endNode = "";
		graphDb.updateRelation(graphId, "JAVA001", endNode, relationType, request);
	}
	
	@Test
	public void updateRelationsWithNonExistingStartNode(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "isParentOf";
		graphDb.createRelation(graphId, "JAVA001", "SCALA001", relationType, request);
		graphDb.updateRelation(graphId, "JAVA0012", "SCALA001", relationType, request);
	}
	
	@Test
	public void updateRelationsWithNonExistingEndNode(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "isParentOf";
		String endNode = "Sacala001";
		graphDb.updateRelation(graphId, "JAVA001", endNode, relationType, request);
	}
	
	@Test(expected= org.neo4j.driver.v1.exceptions.ClientException.class)
	public void updateRelationsWithoutGraphId(){
		Request request = new Request();
		request.setId(graphId);
		String relationType = "isParentOf";
		graphDb.updateRelation("", "JAVA001", "SCALA001", relationType, null);
	}
	
//	@Test 
	public void createIncomingRelations(){
		List<String> nodes = new ArrayList<String>();
		Request request = new Request();
		request.setId(graphId);
		nodes.add("python001");
		nodes.add("DotNet001");
		graphDb.createIncomingRelations(graphId, nodes, "SCALA001", relationType, request);
		Relation rel = graphDb.getRelation(graphId, "DotNet001", relationType, "Spring001", request);
		System.out.println(rel.getRelationType() + rel.getEndNodeId() + rel.getStartNodeId());
	}
	
	@Test
	public void createOutgoingRelations(){
		
	} 
	
	@Test
	public void deleteIncomingRelation(){
		
	}
	
	@Test
	public void deleteOutgoingRelation(){
		
	}
	
//	@Test
	public void createCollection(){
		Node node = new Node();
		node.setGraphId(graphId);
		node.setIdentifier("testCollection001");
		node.setMetadata(setData());
		List<String> members = new ArrayList<String>();
		members.add("SCALA001");
		members.add("python001");
		members.add("DotNet001");
		graphDb.createCollection(graphId1, "testCollection001", node, relationType, members, "IL_UNIQUE_ID", null);
	}
	
//	@Test
	public void getNodeById(){
		Request request = new Request();
		request.setId(graphId);
		Long nodeId = (long) 2;
		Node  res = graphDb.getNodeById(graphId, nodeId, true, request);
		assertEquals("Completed", res.getMetadata().get("Status"));
		assertEquals("Content", res.getObjectType());
	}
	
	@Test
	public void getNodeByUniqueId(){
		Request request = new Request();
		request.setId(graphId);
		Node  res = graphDb.getNodeByUniqueId(graphId, "JAVA001", null, request);
		assertEquals("JAVA001" , res.getIdentifier());
		assertEquals("25" , res.getMetadata().get("NoOfChapters"));
	}
	
	@Test
	public void getNodesByProperty(){
		Request request = new Request();
		request.setId(graphId);
		Property prop = new Property();
		prop.setPropertyName("Title");
		prop.setPropertyValue("Learn Java");
		List<Node> nodes = graphDb.getNodesByProperty(graphId, prop, true, request);
		assertEquals(false, nodes.isEmpty());
		assertEquals("DEFINITION_NODE", nodes.iterator().next().getNodeType());
	}
	
	@Test
	public void getNodesByUniqueIds(){
		SearchCriteria search = new SearchCriteria();
//		graphDb.getNodesByUniqueIds(graphId, searchCriteria, request);
	}
	
	@Test
	public void getNodeByProperty(){
		Request request = new Request();
		request.setId(graphId);
		Property property = new Property();
		property.setPropertyName("IL_UNIQUE_ID");
		Property  res = graphDb.getNodeProperty(graphId, "JAVA001", "Title", request);
		assertEquals("Title", res.getPropertyName());
	}
	
	@Test
	public void getAllNodes(){
		Request request = new Request();
		request.setId(graphId);
		List<Node>  res = graphDb.getAllNodes(graphId, request);
		assertEquals("domain", res.get(0).getGraphId().toString());
		
	}
	
	@Test
	public void getAllRelations(){
		Request request = new Request();
		request.setId(graphId);
	    List<Relation> rel = graphDb.getAllRelations(graphId, request);
	    assertEquals(false, rel.isEmpty());
	    assertEquals("SCALA001", rel.get(0).getEndNodeId().toString());
	}
	
//    @Test
    public void getRelationProperty(){
    	Request request = new Request();
		request.setId(graphId);
		Property prop = graphDb.getRelationProperty(graphId, "JAVA001", relationType, "SCALA001", "", request);
		assertEquals(true, prop.getPropertyName().compareToIgnoreCase("ASSOCIATED_TO"));
    }
    
    @Test(expected = org.neo4j.driver.v1.exceptions.ClientException.class)
    public void getRelationPropertyWithEmptyKey(){
    	Request request = new Request();
		request.setId(graphId);
		Property prop = graphDb.getRelationProperty(graphId, "JAVA001", relationType, "SCALA001", null, request);
		assertEquals(true, prop.getPropertyName().compareToIgnoreCase("ASSOCIATED_TO"));
    }
    
    @Test
    public void getRelation(){
    	Request request = new Request();
		request.setId(graphId);
		Relation rel = graphDb.getRelation(graphId, "JAVA001", relationType, "SCALA001", request);
		assertEquals("SCALA001", rel.getEndNodeId());
    }
    
    @Test
	public void checkForCyclicDependency(){
		Request request = new Request();
		request.setId(graphId);
		Map<String,Object> result = graphDb.checkCyclicLoop(graphId, "JAVA001", relationType,  "SCALA001", request);
		assertEquals("JAVA001 and SCALA001 are connected by relation: ASSOCIATED_TO", result.get("message"));
	} 
    
//    @Test
    public void executeQuery(){
    	Request request = new Request();
		request.setId(graphId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(GraphDACParams.graphId.name(), graphId);
		parameterMap.put(GraphDACParams.request.name(), request);
		String query = "MATCH (n:domain) RETURN n LIMIT 4";
		List<Map<String, Object>> res = graphDb.executeQuery(graphId, query, parameterMap, request);
		assertEquals(false, res.isEmpty());
		assertEquals(6, res.size());
    }
    
	@Test
	public void getNodeCount(){
		SearchCriteria search = new SearchCriteria();
		search.setObjectType("Content");
		Long node = graphDb.getNodesCount(graphId, search, null);
	}
	
	@Test
	public void searchNodes(){
		
	}
	
	@Test
	public void traverse(){
		
	}
	
	@Test
	public void traverseSubGraph(){
		
	}
//	@Test
	public void getSubGraph(){
		Graph res = graphDb.getSubGraph(graphId, "JAVA001", relationType, 0, null);
		System.out.println(res.getNodes());
	}
	
//	@AfterClass
//	public static void close() {
//		Request request = new Request();
//		request.setId(graphId);
//			graphDb.deleteGraph(graphId, request);
//	}
	
	public static Map<String, Object> setMetadata(){
		Map<String, Object> javaMap = new HashMap<String,Object>();
		javaMap.put("Title", "Learn Java");
		javaMap.put("NoOfChapters", "25");
		javaMap.put("IL_SYS_NODE_TYPE", "DEFINITION_NODE");
		javaMap.put("IL_FUNC_OBJECT_TYPE", "Content");
		javaMap.put("Status",  "Completed");
		return javaMap;
	}
	
	public static Map<String, Object> setNodeMetadata(){
		Map<String, Object> scalaMap = new HashMap<String,Object>();
		scalaMap.put("Title", "Learn Scala");
		scalaMap.put("NoOfChapters", "20");
		scalaMap.put("IL_SYS_NODE_TYPE", "DATA_NODE");
		scalaMap.put("IL_FUNC_OBJECT_TYPE", "Content");
		scalaMap.put("Status",  "Completed");
		return scalaMap;
	}
	
	public static Map<String, Object> setNodeData(){
		Map<String, Object> DotNetMap = new HashMap<String,Object>();
		DotNetMap.put("Title", "Learn C#&.Net");
		DotNetMap.put("NoOfChapters", "14");
		DotNetMap.put("IL_SYS_NODE_TYPE", "DATA_NODE");
		DotNetMap.put("IL_FUNC_OBJECT_TYPE", "Content");
		DotNetMap.put("Status",  "InProgress");
		return DotNetMap;
	}
	
	public static Map<String, Object> setData(){
		Map<String, Object> pythonMap = new HashMap<String,Object>();
		pythonMap.put("Title", "Learn Language");
		pythonMap.put("NoOfChapters", "30");
		pythonMap.put("IL_SYS_NODE_TYPE", "DATA_NODE");
		pythonMap.put("IL_FUNC_OBJECT_TYPE", "Content");
		pythonMap.put("Status",  "InProgress");
		return pythonMap;
	}
	
	public static Map<String, Object> set_Data(){
		Map<String, Object> pythonMap = new HashMap<String,Object>();
		pythonMap.put("Title", "Learn Springs");
		pythonMap.put("NoOfChapters", "30");
		pythonMap.put("IL_SYS_NODE_TYPE", "DATA_NODE");
		pythonMap.put("IL_FUNC_OBJECT_TYPE", "Content");
		pythonMap.put("Status",  "InProgress");
		return pythonMap;
	}
}
