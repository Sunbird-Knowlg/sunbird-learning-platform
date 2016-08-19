package org.ekstep.graph.service.operation;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Traverser;

public class Neo4JEmbeddedSearchOperations {
	
	private static Logger LOGGER = LogManager.getLogger(Neo4JEmbeddedSearchOperations.class.getName());
	
	public void getNodeById(String graphId, Long nodeId, Boolean getTags, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void getNodeByUniqueId(String graphId, Long nodeId, Boolean getTags, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void getNodesByProperty(String graphId, Property property, Boolean getTags, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void getNodeByUniqueIds(String graphId, SearchCriteria searchCriteria, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void getNodeProperty(String graphId, String nodeId, String key, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void getAllNodes(String graphId, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void getAllRelations(String graphId, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId,
			String key, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request) {
		// TODO Auto-generated method stub
		
	}

	public void checkCyclicLoop(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request) {
		// TODO Auto-generated method stub
		
	}

	public void executeQuery(String graphId, String query, Map<String, Object> paramMap, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void searchNodes(String graphId, SearchCriteria searchCriteria, Boolean getTags, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void getNodesCount(String graphId, SearchCriteria searchCriteria, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void traverse(String graphId, Traverser traverser, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void traverseSubGraph(String graphId, Traverser traverser, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void getSubGraph(String graphId, String startNodeId, String relationType, int depth, Request request) {
		// TODO Auto-generated method stub
		
	}

}
