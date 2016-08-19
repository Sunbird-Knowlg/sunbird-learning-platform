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
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchCriteria;
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
	public void createIndex(String graphid, List<String> indexProperties, Request request) {
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
	public void createCollection(String graphId, String conllectionId, Node collection, String relationType,
			List<String> members, String indexProperty, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteCollection(String graphId, String collectionId, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void importGraph(String graphId, String taskId, ImportData importData, Map<String, List<String>> messages, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void upsertNode(String graphId, Node node, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addNode(String graphId, Node node, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateNode(String graphId, Node node, Request request) {
		// TODO Auto-generated method stub
		
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
	public void getNodeById(String graphId, Long nodeId, Boolean getTags, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getNodeByUniqueId(String graphId, Long nodeId, Boolean getTags, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getNodesByProperty(String graphId, Property property, Boolean getTags, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getNodeByUniqueIds(String graphId, SearchCriteria searchCriteria, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getNodeProperty(String graphId, String nodeId, String key, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getAllNodes(String graphId, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getAllRelations(String graphId, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId,
			String key, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkCyclicLoop(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeQuery(String graphId, String query, Map<String, Object> paramMap, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void searchNodes(String graphId, SearchCriteria searchCriteria, Boolean getTags, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getNodesCount(String graphId, SearchCriteria searchCriteria, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void traverse(String graphId, Traverser traverser, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void traverseSubGraph(String graphId, Traverser traverser, Request request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getSubGraph(String graphId, String startNodeId, String relationType, int depth, Request request) {
		// TODO Auto-generated method stub
		
	}



}
