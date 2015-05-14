package com.ilimi.graph.dac.mgr;

import com.ilimi.common.dto.Request;

public interface IGraphDACNodeMgr {

    void upsertNode(Request request);
    
    void addNode(Request request);
    
    void updateNode(Request request);

    void importNodes(Request request);

    void updatePropertyValue(Request request);

    void updatePropertyValues(Request request);

    void removePropertyValue(Request request);

    void removePropertyValues(Request request);

    void deleteNode(Request request);
}
