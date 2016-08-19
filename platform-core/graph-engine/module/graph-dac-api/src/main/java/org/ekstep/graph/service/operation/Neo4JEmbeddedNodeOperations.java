package org.ekstep.graph.service.operation;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.graph.dac.model.Node;

public class Neo4JEmbeddedNodeOperations {
	
	private static Logger LOGGER = LogManager.getLogger(Neo4JEmbeddedNodeOperations.class.getName());
	
	public void upsertNode(String graphId, Node node, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void addNode(String graphId, Node node, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void updateNode(String graphId, Node node, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void importNodes(String graphId, List<Node> nodes, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void updatePropertyValue(String graphId, Node node, Property property, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void updatePropertyValues(String graphId, Node node, Map<String, Object> metadata, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void removePropertyValue(String graphId, Node node, String key, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void removePropertyValues(String graphId, Node node, List<String> keys, Request request) {
		// TODO Auto-generated method stub
		
	}

	public void deleteNode(String graphId, Node node, Request request) {
		// TODO Auto-generated method stub
		
	}

}
