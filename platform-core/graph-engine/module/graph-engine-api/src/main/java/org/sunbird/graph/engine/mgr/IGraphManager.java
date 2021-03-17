package org.sunbird.graph.engine.mgr;

import org.sunbird.common.dto.Request;

/**
 * Graph Engine Manager with API for operations on Graph and Relations.
 * 
 * @author rayulu
 * 
 */
public interface IGraphManager {

    /**
     * API to create a new graph.
     * 
     * @request - GRAPH_ID as request context variable.
     * @response - GRAPH_ID: id of the newly created graph
     * @response - STATUS: API call status
     */
    void createGraph(Request request);
    
    /**
     * Creates unique constraint on given set of properties keys
     * 
     * @request - GRAPH_ID as request context variable
     * @request - property_keys keys on which unique constraint should be added
     * @response - STATUS: API call status
     */
    void createUniqueConstraint(Request request);
    
    /**
     * Creates an index on given list of keys
     * 
     * @request - GRAPH_ID as request context variable
     * @request - property_keys keys which should be indexed
     * @response - STATUS: API call status
     */
    void createIndex(Request request);

    /**
     * Loads all the cached objects of the graph into cache.
     * 
     * @request - GRAPH_ID as request context variable.
     * @response - STATUS: API call status
     */
    void loadGraph(Request request);

    /**
     * API to validate a graph. Validates all data nodes against their
     * definition nodes and all relations.
     * 
     * @request - GRAPH_ID as request context variable.
     * @response - MESSAGES: Map of validation messages. Empty, if there are no
     *           validation errors.
     * @response - STATUS: API call status
     */
    void validateGraph(Request request);

    /**
     * Deletes a given graph from the system.
     * 
     * @request - GRAPH_ID as request context variable.
     * @response - GRAPH_ID: id of the deleted graph
     * @response - STATUS: API call status
     */
    void deleteGraph(Request request);

    /**
     * Imports the given graph in RDF or JSON format into the database.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - FORMAT format of the input
     * @request - INPUT Graph in the specified format
     * @response - GRAPH_ID: id of the imported graph
     * @response - STATUS: API call status
     */
    void importGraph(Request request);
    
    /**
     * Exports the graph database into RDF or JSON format.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - FORMAT format of the output
     * @response - OUTPUT Graph in the specified format
     * @response - STATUS API call status 
     */
    void exportGraph(Request request);
    
    /**
     * Creates the specified relation between two given nodes. Relation
     * validation is performed before creating the relation.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - START_NODE_ID unique of the relation start node
     * @request - RELATION_TYPE relation type
     * @request - END_NODE_ID unique of the relation end node
     * @response - STATUS: API call status
     */
    void createRelation(Request request);
    
    /**
     * Creates the specified relation between a given start node and list of end nodes. 
     * Relation validation is not performed before creating the relations.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - START_NODE_ID unique id of the relation start node
     * @request - RELATION_TYPE relation type
     * @request - END_NODE_ID list of relation end node ids
     * @response - STATUS: API call status
     */
    void addOutRelations(Request request);
    
    /**
     * Creates the specified relation between a list of start nodes and an end node. 
     * Relation validation is not performed before creating the relations.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - START_NODE_ID list of relation start node ids
     * @request - RELATION_TYPE relation type
     * @request - END_NODE_ID unique id of the relation end node
     * @response - STATUS: API call status
     */
    void addInRelations(Request request);

    /**
     * Removes the specified relation type between the two given nodes.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - START_NODE_ID unique of the relation start node
     * @request - RELATION_TYPE relation type
     * @request - END_NODE_ID unique of the relation end node
     * @response - STATUS: API call status
     */
    void removeRelation(Request request);

	void createTaskNode(Request request);
	
	/**
     * Updates multiple nodes in the graph. It does the following:
     * - validates the input nodes metadata and relations
     * - create/update nodes
     * - update node relations: this involves removing all existing relations of the same type and with the nodes of same object type
     * - add tags 
     * 
     * @request - GRAPH_ID as request context variable
     * @request - NODES list of nodes to be updated
     * @response - STATUS: API call status
     */
	void bulkUpdateNodes(Request request);

    /**
     * Updates Local Definition Cache.
     *
     * @param request
     */
	void updateDefinitionCache(Request request);
}
