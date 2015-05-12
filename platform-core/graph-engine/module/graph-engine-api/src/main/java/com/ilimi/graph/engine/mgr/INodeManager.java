package com.ilimi.graph.engine.mgr;

import com.ilimi.graph.common.Request;

/**
 * Graph Engine Manager with API for operations on Nodes.
 * 
 * @author rayulu
 * 
 */
public interface INodeManager {

    /**
     * Creates a definition node in the graph.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - NODE_ID unique id for the definition node
     * @request - DEFINITION_NODE definition node object
     * @response - STATUS: API call status
     */
    void saveDefinitionNode(Request request);

    /**
     * Adds new draft metadata definitions to a definition node.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - OBJECT_TYPE definition node object type
     * @request - METADATA_DEFINITIONS list of metadata definitions
     * @response - STATUS: API call status
     */
    void updateDefinition(Request request);

    /**
     * Creates a data node in the graph. Validates the data node against the
     * definition node in the graph.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - NODE_ID unique id for the definition node
     * @request - OBJECT_TYPE object type of the data node
     * @request - METADATA metadata map of the data node
     * @response - STATUS: API call status
     */
    void createDataNode(Request request);

    /**
     * Updates metadata of a data node in the graph. Validates the metadata
     * against the definition node in the graph.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - NODE_ID unique id for the definition node
     * @request - NODE node object to be updated
     * @response - STATUS: API call status
     */
    void updateDataNode(Request request);

    /**
     * Deletes the given data node from the graph. Deletes all the relations on
     * the node before deleting the node.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - NODE_ID unique id for the definition node
     * @response - STATUS: API call status
     */
    void deleteDataNode(Request request);

    /**
     * Deletes the definition for a given object type.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - OBJECT_TYPE object type definition to be deleted
     * @response - STATUS: API call status
     */
    void deleteDefinition(Request request);

    /**
     * Imports a definition nodes JSON into the graph. The import JSON should
     * have an array of definitions and the array variable name should be
     * 'definitionNodes': {"definitionNodes" : []}
     * 
     * @request - GRAPH_ID as request context variable
     * @request - INPUT_STREAM definition nodes JSON input stream
     * @response - STATUS: API call status
     * @response - MESSAGES: List of validation error messages, if any
     */
    void importDefinitions(final Request request);
    
    
    void exportNode(Request request);

}
