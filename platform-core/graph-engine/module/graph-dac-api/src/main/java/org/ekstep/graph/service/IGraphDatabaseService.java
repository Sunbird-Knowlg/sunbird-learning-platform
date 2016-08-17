package org.ekstep.graph.service;

import com.ilimi.common.dto.Request;

/**
* IGraphService is Contract between the Platform and Neo4J System
*
* @author  Mohammad Azharuddin
* @version 1.0
* @since   2016-08-17 
*/
public interface IGraphDatabaseService {
	
	// Node Managements APIs
	void upsertNode(Request request);
    
    void addNode(Request request);
    
    void updateNode(Request request);

    void importNodes(Request request);

    void updatePropertyValue(Request request);

    void updatePropertyValues(Request request);

    void removePropertyValue(Request request);

    void removePropertyValues(Request request);

    void deleteNode(Request request);
    
    // Graph Managements APIs
    void createGraph(Request request);
    
    void createUniqueConstraint(Request request);
    
    void createIndex(Request request);

    void deleteGraph(Request request);

    void addRelation(Request request);

    void deleteRelation(Request request);

    void updateRelation(Request request);

    void removeRelationMetadata(Request request);

    void importGraph(Request request);

    void createCollection(Request request);

    void deleteCollection(Request request);

    void addOutgoingRelations(Request request);

    void addIncomingRelations(Request request);
    
    void deleteIncomingRelations(Request request);
    
    void deleteOutgoingRelations(Request request);
    
    // Search Handling APIs
    void getNodeById(Request request);

    void getNodeByUniqueId(Request request);

    void getNodesByUniqueIds(Request request);

    void getNodesByProperty(Request request);

    void getNodeProperty(Request request);

    void getAllRelations(Request request);

    void getAllNodes(Request request);

    void getRelation(Request request);

    void getRelationProperty(Request request);

    void checkCyclicLoop(Request request);
    
    void executeQuery(Request request);

    void searchNodes(Request request);

    void getNodesCount(Request request);

    void traverse(Request request);
    
    void traverseSubGraph(Request request);
    
    void getSubGraph(Request request);

}
