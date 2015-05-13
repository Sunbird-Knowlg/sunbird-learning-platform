package com.ilimi.graph.cache.mgr.impl;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.cache.mgr.IDefinitionNodeCacheMgr;
import com.ilimi.graph.cache.util.RedisKeyGenerator;
import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;

public class DefinitionNodeCacheMgrImpl implements IDefinitionNodeCacheMgr {

    private BaseGraphManager manager;

    public DefinitionNodeCacheMgrImpl(BaseGraphManager manager) {
        this.manager = manager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void saveDefinitionNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String objectType = (String) request.get(GraphDACParams.object_type.name());
        List<String> requiredMetadata = (List<String>) request.get(GraphDACParams.required_metadata_key.name());
        List<String> indexedMetadata = (List<String>) request.get(GraphDACParams.indexable_metadata_key.name());
        List<String> nonIndexedMetadata = (List<String>) request.get(GraphDACParams.non_indexable_metadata_key.name());
        List<String> outRelations = (List<String>) request.get(GraphDACParams.out_relations_key.name());
        List<String> inRelations = (List<String>) request.get(GraphDACParams.in_relations_key.name());
        if (!manager.validateRequired(objectType)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SAVE_DEF_NODE_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String requiredMetadataKey = RedisKeyGenerator.getDefNodeRequiredMetadataKey(graphId, objectType);
            saveList(jedis, requiredMetadata, requiredMetadataKey, false);

            String indexedMetadataKey = RedisKeyGenerator.getDefNodeIndexedMetadataKey(graphId, objectType);
            saveList(jedis, indexedMetadata, indexedMetadataKey, true);

            String nonIndexedMetadataKey = RedisKeyGenerator.getDefNodeNonIndexedMetadataKey(graphId, objectType);
            saveList(jedis, nonIndexedMetadata, nonIndexedMetadataKey, false);

            String outRelationsKey = RedisKeyGenerator.getDefNodeOutRelationsKey(graphId, objectType);
            saveList(jedis, outRelations, outRelationsKey, false);

            String inRelationsKey = RedisKeyGenerator.getDefNodeInRelationsKey(graphId, objectType);
            saveList(jedis, inRelations, inRelationsKey, false);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_DEF_NODE_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public List<String> getRequiredMetadataFields(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String objectType = (String) request.get(GraphDACParams.object_type.name());
        if (!manager.validateRequired(objectType)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getDefNodeRequiredMetadataKey(graphId, objectType);
            return getDefNodeList(jedis, key);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public List<String> getIndexedMetadataFields(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String objectType = (String) request.get(GraphDACParams.object_type.name());
        if (!manager.validateRequired(objectType)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getDefNodeIndexedMetadataKey(graphId, objectType);
            return getDefNodeList(jedis, key);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public List<String> getNonIndexedMetadataFields(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String objectType = (String) request.get(GraphDACParams.object_type.name());
        if (!manager.validateRequired(objectType)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getDefNodeNonIndexedMetadataKey(graphId, objectType);
            return getDefNodeList(jedis, key);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public List<String> getOutRelationObjectTypes(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String objectType = (String) request.get(GraphDACParams.object_type.name());
        if (!manager.validateRequired(objectType)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getDefNodeOutRelationsKey(graphId, objectType);
            return getDefNodeList(jedis, key);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public List<String> getInRelationObjectTypes(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String objectType = (String) request.get(GraphDACParams.object_type.name());
        if (!manager.validateRequired(objectType)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getDefNodeInRelationsKey(graphId, objectType);
            return getDefNodeList(jedis, key);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    private List<String> getDefNodeList(Jedis jedis, String key) {
        Set<String> members = jedis.smembers(key);
        List<String> requiredList = new ArrayList<String>();
        if (null != members && members.size() > 0) {
            requiredList.addAll(members);
        }
        return requiredList;
    }

    private void saveList(Jedis jedis, List<String> metadata, String key, boolean sort) {
        if (null != metadata && !metadata.isEmpty()) {
            List<String> list = new ArrayList<String>();
            for (String val : metadata) {
                list.add(val);
            }
            if (sort)
                Collections.sort(list);
            jedis.sadd(key, convertListToArray(list));
        }
    }

    private String[] convertListToArray(List<String> list) {
        if (null != list && !list.isEmpty()) {
            String[] array = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = list.get(i);
            }
            return array;
        }
        return null;
    }

}
