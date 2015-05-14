package com.ilimi.graph.engine.mgr;

import com.ilimi.common.dto.Request;

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
     * Removes the specified relation type between the two given nodes.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - START_NODE_ID unique of the relation start node
     * @request - RELATION_TYPE relation type
     * @request - END_NODE_ID unique of the relation end node
     * @response - STATUS: API call status
     */
    void removeRelation(Request request);
}
