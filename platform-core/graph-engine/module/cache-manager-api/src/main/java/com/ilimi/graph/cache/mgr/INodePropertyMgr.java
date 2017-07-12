package com.ilimi.graph.cache.mgr;

import com.ilimi.common.dto.Request;

public interface INodePropertyMgr {

    void saveNodeProperty(Request request);

    String getNodeProperty(Request request);

    void saveNodeProperties(Request request);
}
