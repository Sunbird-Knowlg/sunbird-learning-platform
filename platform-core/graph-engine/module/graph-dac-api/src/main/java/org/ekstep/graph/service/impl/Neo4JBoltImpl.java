package org.ekstep.graph.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.IGraphDatabaseService;
import org.ekstep.graph.service.operation.Neo4JBoltGraphOperations;
import org.ekstep.graph.service.operation.Neo4JBoltNodeOperations;
import org.ekstep.graph.service.operation.Neo4JBoltSearchOperations;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.graph.dac.model.Graph;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.SubGraph;
import com.ilimi.graph.dac.model.Traverser;
import com.ilimi.graph.importer.ImportData;

public class Neo4JBoltImpl implements IGraphDatabaseService {

	private static Logger LOGGER = LogManager.getLogger(Neo4JBoltImpl.class.getName());

	@Override
	public void createGraph(String graphId, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createGraphUniqueContraint(String graphId, List<String> indexProperties, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createIndex(String graphId, List<String> indexProperties, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteGraph(String graphId, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeRelationMetadataByKey(String graphId, String startNodeId, String endNodeId, String relationType,
			String key, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createCollection(String graphId, String collectionId, Node collection, String relationType,
			List<String> members, String indexProperty, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteCollection(String graphId, String collectionId, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, List<String>> importGraph(String graphId, String taskId, ImportData input, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node upsertNode(String graphId, Node node, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node addNode(String graphId, Node node, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node updateNode(String graphId, Node node, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void importNodes(String graphId, List<Node> nodes, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updatePropertyValue(String graphId, String nodeId, Property property, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updatePropertyValues(String graphId, String nodeId, Map<String, Object> metadata, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removePropertyValue(String graphId, String nodeId, String key, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removePropertyValues(String graphId, String nodeId, List<String> keys, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteNode(String graphId, String nodeId, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Node getNodeById(String graphId, Long nodeId, Boolean getTags, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getNodeByUniqueId(String graphId, String nodeId, Boolean getTags, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Node> getNodesByProperty(String graphId, Property property, Boolean getTags, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Node> getNodesByUniqueIds(String graphId, SearchCriteria searchCriteria, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Property getNodeProperty(String graphId, String nodeId, String key, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Node> getAllNodes(String graphId, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Relation> getAllRelations(String graphId, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Property getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId,
			String key, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Relation getRelation(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> checkCyclicLoop(String graphId, String startNodeId, String relationType,
			String endNodeId, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> executeQuery(String graphId, String query, Map<String, Object> paramMap,
			Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Node> searchNodes(String graphId, SearchCriteria searchCriteria, Boolean getTags, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getNodesCount(String graphId, SearchCriteria searchCriteria, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SubGraph traverse(String graphId, Traverser traverser, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Graph traverseSubGraph(String graphId, Traverser traverser, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Graph getSubGraph(String graphId, String startNodeId, String relationType, int depth, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public org.neo4j.graphdb.Node upsertRootNode(String graphId, Request request) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
