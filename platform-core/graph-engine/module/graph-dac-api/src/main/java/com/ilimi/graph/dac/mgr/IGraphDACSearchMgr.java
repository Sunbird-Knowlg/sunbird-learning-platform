package com.ilimi.graph.dac.mgr;

import com.ilimi.common.dto.Request;

public interface IGraphDACSearchMgr {

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
