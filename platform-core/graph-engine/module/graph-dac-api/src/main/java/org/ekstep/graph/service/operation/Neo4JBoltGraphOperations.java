package org.ekstep.graph.service.operation;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;
import static com.ilimi.graph.dac.util.Neo4jGraphUtil.getNodeByUniqueId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.impl.Neo4JBoltImpl;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.graph.common.DateUtils;
import com.ilimi.graph.common.Identifier;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.dac.enums.AuditProperties;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.dac.util.Neo4jGraphUtil;
import com.ilimi.graph.dac.util.RelationType;
import com.ilimi.graph.importer.ImportData;

public class Neo4JBoltGraphOperations {
	
	private static Logger LOGGER = LogManager.getLogger(Neo4JBoltGraphOperations.class.getName());
	
	/**
	 * Creates the graph.
	 *
	 * @param graphId the graph id
	 * @param request the request
	 */
	public void createGraph(String graphId, Request request) {
		LOGGER.info("Operation Not Allowed in Bolt.");
	}

	/**
	 * Creates the graph unique contraint.
	 *
	 * @param graphId the graph id
	 * @param indexProperties the index properties
	 * @param request the request
	 */
	public void createGraphUniqueContraint(String graphId, List<String> indexProperties, Request request) {
		
	}

	/**
	 * Creates the index.
	 *
	 * @param graphId the graph id
	 * @param indexProperties the index properties
	 * @param request the request
	 */
	public void createIndex(String graphId, List<String> indexProperties, Request request) {
		
	}

	/**
	 * Delete graph.
	 *
	 * @param graphId the graph id
	 * @param request the request
	 */
	public void deleteGraph(String graphId, Request request) {
		
	}

	/**
	 * Creates the relation.
	 *
	 * @param graphId the graph id
	 * @param startNodeId the start node id
	 * @param endNodeId the end node id
	 * @param relationType the relation type
	 * @param request the request
	 */
	@SuppressWarnings("unchecked")
	public void createRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		
	}

	/**
	 * Update relation.
	 *
	 * @param graphId the graph id
	 * @param startNodeId the start node id
	 * @param endNodeId the end node id
	 * @param relationType the relation type
	 * @param request the request
	 */
	@SuppressWarnings("unchecked")
	public void updateRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		
	}

	/**
	 * Delete relation.
	 *
	 * @param graphId the graph id
	 * @param startNodeId the start node id
	 * @param endNodeId the end node id
	 * @param relationType the relation type
	 * @param request the request
	 */
	public void deleteRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		
	}

	/**
	 * Creates the incoming relations.
	 *
	 * @param graphId the graph id
	 * @param startNodeIds the start node ids
	 * @param endNodeId the end node id
	 * @param relationType the relation type
	 * @param request the request
	 */
	public void createIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		
	}

	/**
	 * Creates the outgoing relations.
	 *
	 * @param graphId the graph id
	 * @param startNodeId the start node id
	 * @param endNodeIds the end node ids
	 * @param relationType the relation type
	 * @param request the request
	 */
	public void createOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		
	}

	/**
	 * Delete incoming relations.
	 *
	 * @param graphId the graph id
	 * @param startNodeIds the start node ids
	 * @param endNodeId the end node id
	 * @param relationType the relation type
	 * @param request the request
	 */
	public void deleteIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		
	}

	/**
	 * Delete outgoing relations.
	 *
	 * @param graphId the graph id
	 * @param startNodeId the start node id
	 * @param endNodeIds the end node ids
	 * @param relationType the relation type
	 * @param request the request
	 */
	public void deleteOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		
	}

	/**
	 * Removes the relation metadata by key.
	 *
	 * @param graphId the graph id
	 * @param startNodeId the start node id
	 * @param endNodeId the end node id
	 * @param relationType the relation type
	 * @param key the key
	 * @param request the request
	 */
	public void removeRelationMetadataByKey(String graphId, String startNodeId, String endNodeId, String relationType,
			String key, Request request) {
		
	}

	/**
	 * Creates the collection.
	 *
	 * @param graphId the graph id
	 * @param collectionId the collection id
	 * @param collection the collection
	 * @param relationType the relation type
	 * @param members the members
	 * @param indexProperty the index property
	 * @param request the request
	 */
	public void createCollection(String graphId, String collectionId, com.ilimi.graph.dac.model.Node collection,
			String relationType, List<String> members, String indexProperty, Request request) {
		
	}

	/**
	 * Delete collection.
	 *
	 * @param graphId the graph id
	 * @param collectionId the collection id
	 * @param request the request
	 */
	public void deleteCollection(String graphId, String collectionId, Request request) {
		
	}

	/**
	 * Import graph.
	 *
	 * @param graphId the graph id
	 * @param taskId the task id
	 * @param input the input
	 * @param request the request
	 * @return the map
	 * @throws Exception the exception
	 */
	public Map<String, List<String>> importGraph(String graphId, String taskId, ImportData input, Request request)
			throws Exception {
		return new HashMap<String, List<String>>();
	}

	/**
	 * Update task status.
	 *
	 * @param graphDb the graph db
	 * @param taskId the task id
	 * @param string the string
	 * @throws Exception the exception
	 */
	

}
