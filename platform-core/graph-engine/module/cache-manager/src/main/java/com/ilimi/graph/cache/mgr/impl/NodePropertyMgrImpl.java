package com.ilimi.graph.cache.mgr.impl;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.cache.mgr.INodePropertyMgr;
import com.ilimi.graph.cache.util.RedisKeyGenerator;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.mgr.BaseGraphManager;

import redis.clients.jedis.Jedis;

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
		Jedis jedis = getRedisConncetion();
		try {
			String redisKey = RedisKeyGenerator.getNodePropertyKey(graphId, nodeId, nodeProperty);
			jedis.set(redisKey, nodeValue);
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	@Override
	public String getNodeProperty(Request request) {
	
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        String nodeProperty = (String) request.get(GraphDACParams.propertyName.name());
        if (!manager.validateRequired(graphId, nodeId, nodeProperty)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), "Required parameters are missing");
        }
		Jedis jedis = getRedisConncetion();
		try {
			String redisKey = RedisKeyGenerator.getNodePropertyKey(graphId, nodeId, nodeProperty);
			String value = jedis.get(redisKey);
			return value;
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

}
