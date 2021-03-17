package org.sunbird.graph.engine.mgr;

import org.sunbird.common.dto.Request;

public interface ISearchManager {

    /**
     * Get all nodes of a given object type
     * 
     * @request - GRAPH_ID as request context variable
     * @request - Property Object
     * @response - NODE_LIST: List of nodes of the given object type
     * @response - STATUS: API call status
     */
    void getNodesByProperty(Request request);

    /**
     * Get all nodes of a given object type
     * 
     * @request - GRAPH_ID as request context variable
     * @request - OBJECT_TYPE object type
     * @response - NODE_LIST: List of nodes of the given object type
     * @response - STATUS: API call status
     */
    void getNodesByObjectType(Request request);

    /**
     * Get all children (connected via Hierarchical relation) of a given node to
     * the specified depth. If depth <= 0, then full graph is traversed.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - NODE_ID start node
     * @request - DEPTH depth to which the graph should be traversed
     * @response - SUB_GRAPH: Sub graph containing multiple paths
     * @response - STATUS: API call status
     */
    void getChildren(Request request);

    /**
     * Get all nodes connected via specified relation to a given start node.
     * Graph is traversed to the specified depth from the start node. If depth
     * <= 0, then full graph is traversed.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - NODE_ID start node
     * @request - RELATION_TYPE relation to be traversed
     * @request - DEPTH depth to which the graph should be traversed
     * @response - SUB_GRAPH: Sub graph containing multiple paths
     * @response - STATUS: API call status
     */
    void getDescendants(Request request);

    /**
     * Get all node definitions in a graph
     * 
     * @request - GRAPH_ID as request context variable
     * @response - DEFINITION_NODES list of definition nodes
     * @response - STATUS: API call status
     */
    void getAllDefinitions(Request request);

    /**
     * Get node definition for a given object type
     * 
     * @request - GRAPH_ID as request context variable
     * @request - OBJECT_TYPE object type
     * @response - DEFINITION_NODE definition node object
     * @response - STATUS: API call status
     */
    void getNodeDefinition(Request request);
    
    /**
     * Get node definition for a given object type from cache
     * 
     * @request - GRAPH_ID as request context variable
     * @request - OBJECT_TYPE object type
     * @response - DEFINITION_NODE definition node object
     * @response - STATUS: API call status
     */
    void getNodeDefinitionFromCache(Request request);

    /**
     * Get data node for the given id
     * 
     * @request - GRAPH_ID as request context variable
     * @request - NODE_ID node id
     * @request - GET_TAGS boolean value to specify if tags should also be
     *          returned
     * @response - NODE node object
     * @response - STATUS: API call status
     */
    void getDataNode(Request request);

    /**
     * Get data nodes for a list of node ids
     * 
     * @request - GRAPH_ID as request context variable
     * @request - NODE_IDS node ids list
     * @request - GET_TAGS boolean value to specify if tags should also be
     *          returned
     * @response - NODE_LIST list of node objects
     * @response - STATUS: API call status
     */
    void getDataNodes(Request request);

    /**
     * Search nodes by given criteria
     * 
     * @request - GRAPH_ID as request context variable
     * @request - SEARCH_CRITERIA node id
     * @response - NODE_LIST list of nodes
     * @response - STATUS: API call status
     */
    void searchNodes(Request request);

    /**
     * Search nodes by given criteria
     * 
     * @request - GRAPH_ID as request context variable
     * @request - SEARCH_CRITERIA node id
     * @response - COUNT count of nodes
     * @response - STATUS: API call status
     */
    void getNodesCount(Request request);

    /**
     * Traverse the graph starting from a node using the given traversal
     * description. Returns a sub graph containing list of paths.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - TRAVERSAL_DESCRIPTION traversal description to traverse the
     *          graph
     * @response - SUB_GRAPH containing multiple paths
     * @response - STATUS: API call status
     */
    void traverse(Request request);

    /**
     * Get the sub graph starting from a node using the given relation type and
     * traversing to specified depth. Returns a sub graph containing list of
     * nodes and relations.
     * 
     * @request - GRAPH_ID as request context variable
     * @request - START_NODE_ID start node of the sub graph
     * @request - RELATION_TYPE relation to be traversed
     * @request - DEPTH traversal depth
     * @response - SUB_GRAPH containing list of nodes and relations
     * @response - STATUS: API call status
     */
    void getSubGraph(Request request);
    
    void traverseSubGraph(Request request);
    
    void searchRelations(Request request);
    
    /**
     * Get proxy node for the given id
     * 
     * @request - GRAPH_ID as request context variable
     * @request - NODE_ID proxy node id
     * @response - NODE node object
     * @response - STATUS: API call status
     */
    void getProxyNode(Request request);
    
    
    
    void executeQueryForProps(Request request);

}
