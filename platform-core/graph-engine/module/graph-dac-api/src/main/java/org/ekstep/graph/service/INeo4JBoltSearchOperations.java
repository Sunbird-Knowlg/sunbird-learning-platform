/**
 * 
 */
package org.ekstep.graph.service;

import java.util.List;
import java.util.Map;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.graph.dac.model.Graph;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.SubGraph;
import com.ilimi.graph.dac.model.Traverser;

/**
 * @author pradyumna
 *
 */
public interface INeo4JBoltSearchOperations {

	/**
	 * Gets the node by id.
	 *
	 * @param graphId
	 *            the graph id
	 * @param nodeId
	 *            the node id
	 * @param getTags
	 *            the get tags
	 * @param request
	 *            the request
	 * @return the node by id
	 */
	Node getNodeById(String graphId, Long nodeId, Boolean getTags, Request request);

	/**
	 * Gets the node by unique id.
	 *
	 * @param graphId
	 *            the graph id
	 * @param nodeId
	 *            the node id
	 * @param getTags
	 *            the get tags
	 * @param request
	 *            the request
	 * @return the node by unique id
	 */
	Node getNodeByUniqueId(String graphId, String nodeId, Boolean getTags, Request request);

	/**
	 * Gets the nodes by property.
	 *
	 * @param graphId
	 *            the graph id
	 * @param property
	 *            the property
	 * @param getTags
	 *            the get tags
	 * @param request
	 *            the request
	 * @return the nodes by property
	 */
	List<Node> getNodesByProperty(String graphId, Property property, Boolean getTags, Request request);

	/**
	 * Gets the node by unique ids.
	 *
	 * @param graphId
	 *            the graph id
	 * @param searchCriteria
	 *            the search criteria
	 * @param request
	 *            the request
	 * @return the node by unique ids
	 */
	List<Node> getNodeByUniqueIds(String graphId, SearchCriteria searchCriteria, Request request);

	List<Map<String, Object>> executeQueryForProps(String graphId, String query, List<String> propKeys);

	/**
	 * Gets the node property.
	 *
	 * @param graphId
	 *            the graph id
	 * @param nodeId
	 *            the node id
	 * @param key
	 *            the key
	 * @param request
	 *            the request
	 * @return the node property
	 */
	Property getNodeProperty(String graphId, String nodeId, String key, Request request);

	/**
	 * Gets the all nodes.
	 *
	 * @param graphId
	 *            the graph id
	 * @param request
	 *            the request
	 * @return the all nodes
	 */
	List<Node> getAllNodes(String graphId, Request request);

	/**
	 * Gets the all relations.
	 *
	 * @param graphId
	 *            the graph id
	 * @param request
	 *            the request
	 * @return the all relations
	 */
	List<Relation> getAllRelations(String graphId, Request request);

	/**
	 * Gets the relation property.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeId
	 *            the start node id
	 * @param relationType
	 *            the relation type
	 * @param endNodeId
	 *            the end node id
	 * @param key
	 *            the key
	 * @param request
	 *            the request
	 * @return the relation property
	 */
	Property getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId, String key,
			Request request);

	Relation getRelationById(String graphId, Long relationId, Request request);

	/**
	 * Gets the relation.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeId
	 *            the start node id
	 * @param relationType
	 *            the relation type
	 * @param endNodeId
	 *            the end node id
	 * @param request
	 *            the request
	 * @return the relation
	 */
	Relation getRelation(String graphId, String startNodeId, String relationType, String endNodeId, Request request);

	/**
	 * Check cyclic loop.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeId
	 *            the start node id
	 * @param relationType
	 *            the relation type
	 * @param endNodeId
	 *            the end node id
	 * @param request
	 *            the request
	 * @return the map
	 */
	Map<String, Object> checkCyclicLoop(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request);

	/**
	 * Execute query.
	 *
	 * @param graphId
	 *            the graph id
	 * @param query
	 *            the query
	 * @param paramMap
	 *            the param map
	 * @param request
	 *            the request
	 * @return the list
	 */
	List<Map<String, Object>> executeQuery(String graphId, String query, Map<String, Object> paramMap, Request request);

	/**
	 * Search nodes.
	 *
	 * @param graphId
	 *            the graph id
	 * @param searchCriteria
	 *            the search criteria
	 * @param getTags
	 *            the get tags
	 * @param request
	 *            the request
	 * @return the list
	 */
	List<Node> searchNodes(String graphId, SearchCriteria searchCriteria, Boolean getTags, Request request);

	/**
	 * Gets the nodes count.
	 *
	 * @param graphId
	 *            the graph id
	 * @param searchCriteria
	 *            the search criteria
	 * @param request
	 *            the request
	 * @return the nodes count
	 */
	Long getNodesCount(String graphId, SearchCriteria searchCriteria, Request request);

	/**
	 * Traverse.
	 *
	 * @param graphId
	 *            the graph id
	 * @param traverser
	 *            the traverser
	 * @param request
	 *            the request
	 * @return the sub graph
	 */
	SubGraph traverse(String graphId, Traverser traverser, Request request);

	/**
	 * Traverse sub graph.
	 *
	 * @param graphId
	 *            the graph id
	 * @param traverser
	 *            the traverser
	 * @param request
	 *            the request
	 * @return the graph
	 */
	Graph traverseSubGraph(String graphId, Traverser traverser, Request request);

	/**
	 * Gets the sub graph.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeId
	 *            the start node id
	 * @param relationType
	 *            the relation type
	 * @param depth
	 *            the depth
	 * @param request
	 *            the request
	 * @return the sub graph
	 */
	Graph getSubGraph(String graphId, String startNodeId, String relationType, Integer depth, Request request);

}