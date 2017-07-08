package com.ilimi.graph.cache.mgr.impl;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import java.util.Map;

import redis.clients.jedis.Jedis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.cache.mgr.IDefinitionNodeCacheMgr;
import com.ilimi.graph.cache.util.RedisKeyGenerator;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;

public class DefinitionNodeCacheMgrImpl implements IDefinitionNodeCacheMgr {

    private BaseGraphManager manager;
    private ObjectMapper mapper = new ObjectMapper();

    public DefinitionNodeCacheMgrImpl(BaseGraphManager manager) {
        this.manager = manager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void saveDefinitionNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String objectType = (String) request.get(GraphDACParams.object_type.name());
        Map<String, Object> defNode = (Map<String, Object>) request.get(GraphDACParams.definition_node.name());
        if (!manager.validateRequired(objectType, defNode)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SAVE_DEF_NODE_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getDefNodeKey(graphId, objectType);
            String defNodeStr = mapper.writeValueAsString(defNode);
            jedis.set(key, defNodeStr);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_DEF_NODE_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    public Map<String, Object> getDefinitionNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String objectType = (String) request.get(GraphDACParams.object_type.name());
        if (!manager.validateRequired(objectType)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getDefNodeKey(graphId, objectType);
            String value = jedis.get(key);
            Map<String, Object> map = mapper.readValue(value, Map.class);
            return map;
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

}
