package com.ilimi.graph.dac.mgr;

import com.ilimi.graph.common.Request;

public interface IGraphDACGraphMgr {

    void createGraph(Request request);

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
}
