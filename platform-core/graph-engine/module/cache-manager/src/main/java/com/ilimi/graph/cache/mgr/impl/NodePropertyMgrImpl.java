package com.ilimi.graph.cache.mgr.impl;

import java.util.Map;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.cache.mgr.INodePropertyMgr;
import com.ilimi.graph.cache.util.RedisStoreUtil;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;

public class NodePropertyMgrImpl implements INodePropertyMgr {

    private BaseGraphManager manager;
    
    public NodePropertyMgrImpl(BaseGraphManager manager) {
        this.manager = manager;
    }
    
	@Override
	public void saveNodeProperty(Request request) {

        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        String nodeProperty = (String) request.get(GraphDACParams.propertyName.name());
        String nodeValue = (String) request.get(GraphDACParams.value.name());
        if (!manager.validateRequired(graphId, nodeId, nodeProperty, nodeValue)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), "Required parameters are missing");
        }
        RedisStoreUtil.saveNodeProperty(graphId, nodeId, nodeProperty, nodeValue);
	}

	@Override
	public String getNodeProperty(Request request) {
	
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        String nodeProperty = (String) request.get(GraphDACParams.propertyName.name());
        if (!manager.validateRequired(graphId, nodeId, nodeProperty)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), "Required parameters are missing");
        }
        return RedisStoreUtil.getNodeProperty(graphId, nodeId, nodeProperty);
	}

	@Override
	public void saveNode(Request request) {
	
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        Map<String, Object> nodeProperty = (Map<String, Object>) request.get(GraphDACParams.properties.name());
        if (!manager.validateRequired(graphId, nodeId, nodeProperty)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), "Required parameters are missing");
        }
        RedisStoreUtil.saveNode(graphId, nodeId, nodeProperty);
	}
}
