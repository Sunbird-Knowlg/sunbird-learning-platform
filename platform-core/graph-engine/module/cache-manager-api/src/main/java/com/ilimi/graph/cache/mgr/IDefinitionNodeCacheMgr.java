package com.ilimi.graph.cache.mgr;

import java.util.Map;

import com.ilimi.common.dto.Request;

public interface IDefinitionNodeCacheMgr {

    void saveDefinitionNode(Request request);

    Map<String, Object> getDefinitionNode(Request request);
    
}
