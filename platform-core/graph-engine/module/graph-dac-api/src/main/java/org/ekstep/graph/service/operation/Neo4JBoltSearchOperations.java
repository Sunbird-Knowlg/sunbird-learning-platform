package org.ekstep.graph.service.operation;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluators;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Graph;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.RelationTraversal;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.SubGraph;
import com.ilimi.graph.dac.model.Traverser;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.dac.util.Neo4jGraphUtil;
import com.ilimi.graph.dac.util.RelationType;

public class Neo4JBoltSearchOperations {
	
	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(Neo4JEmbeddedSearchOperations.class.getName());

	/**
	 * Gets the node by id.
	 *
	 * @param graphId the graph id
	 * @param nodeId the node id
	 * @param getTags the get tags
	 * @param request the request
	 * @return the node by id
	 */
	public Node getNodeById(String graphId, Long nodeId, Boolean getTags, Request request) {
		return new Node();
	}

	/**
	 * Gets the node by unique id.
	 *
	 * @param graphId the graph id
	 * @param nodeId the node id
	 * @param getTags the get tags
	 * @param request the request
	 * @return the node by unique id
	 */
	public Node getNodeByUniqueId(String graphId, String nodeId, Boolean getTags, Request request) {
		return new Node();
	}

	/**
	 * Gets the nodes by property.
	 *
	 * @param graphId the graph id
	 * @param property the property
	 * @param getTags the get tags
	 * @param request the request
	 * @return the nodes by property
	 */
	public List<Node> getNodesByProperty(String graphId, Property property, Boolean getTags, Request request) {
		return new ArrayList<Node>();
	}

	/**
	 * Gets the node by unique ids.
	 *
	 * @param graphId the graph id
	 * @param searchCriteria the search criteria
	 * @param request the request
	 * @return the node by unique ids
	 */
	public List<Node> getNodeByUniqueIds(String graphId, SearchCriteria searchCriteria, Request request) {
		return new ArrayList<Node>();
	}

	/**
	 * Gets the node property.
	 *
	 * @param graphId the graph id
	 * @param nodeId the node id
	 * @param key the key
	 * @param request the request
	 * @return the node property
	 */
	public Property getNodeProperty(String graphId, String nodeId, String key, Request request) {
		return new Property();
	}

	/**
	 * Gets the all nodes.
	 *
	 * @param graphId the graph id
	 * @param request the request
	 * @return the all nodes
	 */
	public List<Node> getAllNodes(String graphId, Request request) {
		return new ArrayList<Node>();
	}

	/**
	 * Gets the all relations.
	 *
	 * @param graphId the graph id
	 * @param request the request
	 * @return the all relations
	 */
	public List<Relation> getAllRelations(String graphId, Request request) {
		return new ArrayList<Relation>();
	}

	/**
	 * Gets the relation property.
	 *
	 * @param graphId the graph id
	 * @param startNodeId the start node id
	 * @param relationType the relation type
	 * @param endNodeId the end node id
	 * @param key the key
	 * @param request the request
	 * @return the relation property
	 */
	public Property getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId,
			String key, Request request) {
		return new Property();
	}

	/**
	 * Gets the relation.
	 *
	 * @param graphId the graph id
	 * @param startNodeId the start node id
	 * @param relationType the relation type
	 * @param endNodeId the end node id
	 * @param request the request
	 * @return the relation
	 */
	public Relation getRelation(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request) {
		return new Relation();
	}

	/**
	 * Check cyclic loop.
	 *
	 * @param graphId the graph id
	 * @param startNodeId the start node id
	 * @param relationType the relation type
	 * @param endNodeId the end node id
	 * @param request the request
	 * @return the map
	 */
	public Map<String, Object> checkCyclicLoop(String graphId, String startNodeId, String relationType,
			String endNodeId, Request request) {
		return new HashMap<String, Object>(); 
	}

	/**
	 * Execute query.
	 *
	 * @param graphId the graph id
	 * @param query the query
	 * @param paramMap the param map
	 * @param request the request
	 * @return the list
	 */
	public List<Map<String, Object>> executeQuery(String graphId, String query, Map<String, Object> paramMap,
			Request request) {
		return new ArrayList<Map<String, Object>>();
	}

	/**
	 * Search nodes.
	 *
	 * @param graphId the graph id
	 * @param searchCriteria the search criteria
	 * @param getTags the get tags
	 * @param request the request
	 * @return the list
	 */
	public List<Node> searchNodes(String graphId, SearchCriteria searchCriteria, Boolean getTags, Request request) {
		return new ArrayList<Node>();
	}

	/**
	 * Gets the nodes count.
	 *
	 * @param graphId the graph id
	 * @param searchCriteria the search criteria
	 * @param request the request
	 * @return the nodes count
	 */
	public Long getNodesCount(String graphId, SearchCriteria searchCriteria, Request request) {
		return 1536326153L;
	}

	/**
	 * Traverse.
	 *
	 * @param graphId the graph id
	 * @param traverser the traverser
	 * @param request the request
	 * @return the sub graph
	 */
	public SubGraph traverse(String graphId, Traverser traverser, Request request) {
		return new SubGraph();
	}

	/**
	 * Traverse sub graph.
	 *
	 * @param graphId the graph id
	 * @param traverser the traverser
	 * @param request the request
	 * @return the graph
	 */
	public Graph traverseSubGraph(String graphId, Traverser traverser, Request request) {
		return new Graph();
	}

	/**
	 * Gets the sub graph.
	 *
	 * @param graphId the graph id
	 * @param startNodeId the start node id
	 * @param relationType the relation type
	 * @param depth the depth
	 * @param request the request
	 * @return the sub graph
	 */
	public Graph getSubGraph(String graphId, String startNodeId, String relationType, Integer depth, Request request) {
		return new Graph();
	}

	
}
