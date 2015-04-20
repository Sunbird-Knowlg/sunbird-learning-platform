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
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.StringValue;
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue objectType = (StringValue) request.get(GraphDACParams.OBJECT_TYPE.name());
        BaseValueObjectList<StringValue> requiredMetadata = (BaseValueObjectList<StringValue>) request
                .get(GraphDACParams.REQUIRED_METADATA_KEY.name());
        BaseValueObjectList<StringValue> indexedMetadata = (BaseValueObjectList<StringValue>) request
                .get(GraphDACParams.INDEXABLE_METADATA_KEY.name());
        BaseValueObjectList<StringValue> nonIndexedMetadata = (BaseValueObjectList<StringValue>) request
                .get(GraphDACParams.NON_INDEXABLE_METADATA_KEY.name());
        BaseValueObjectList<StringValue> outRelations = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.OUT_RELATIONS_KEY
                .name());
        BaseValueObjectList<StringValue> inRelations = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.IN_RELATIONS_KEY
                .name());
        if (!manager.validateRequired(objectType)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SAVE_DEF_NODE_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String requiredMetadataKey = RedisKeyGenerator.getDefNodeRequiredMetadataKey(graphId, objectType.getId());
            saveList(jedis, requiredMetadata, requiredMetadataKey, false);

            String indexedMetadataKey = RedisKeyGenerator.getDefNodeIndexedMetadataKey(graphId, objectType.getId());
            saveList(jedis, indexedMetadata, indexedMetadataKey, true);

            String nonIndexedMetadataKey = RedisKeyGenerator.getDefNodeNonIndexedMetadataKey(graphId, objectType.getId());
            saveList(jedis, nonIndexedMetadata, nonIndexedMetadataKey, false);

            String outRelationsKey = RedisKeyGenerator.getDefNodeOutRelationsKey(graphId, objectType.getId());
            saveList(jedis, outRelations, outRelationsKey, false);

            String inRelationsKey = RedisKeyGenerator.getDefNodeInRelationsKey(graphId, objectType.getId());
            saveList(jedis, inRelations, inRelationsKey, false);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_DEF_NODE_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public BaseValueObjectList<StringValue> getRequiredMetadataFields(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue objectType = (StringValue) request.get(GraphDACParams.OBJECT_TYPE.name());
        if (!manager.validateRequired(objectType)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getDefNodeRequiredMetadataKey(graphId, objectType.getId());
            return getDefNodeList(jedis, key);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public BaseValueObjectList<StringValue> getIndexedMetadataFields(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue objectType = (StringValue) request.get(GraphDACParams.OBJECT_TYPE.name());
        if (!manager.validateRequired(objectType)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getDefNodeIndexedMetadataKey(graphId, objectType.getId());
            return getDefNodeList(jedis, key);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public BaseValueObjectList<StringValue> getNonIndexedMetadataFields(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue objectType = (StringValue) request.get(GraphDACParams.OBJECT_TYPE.name());
        if (!manager.validateRequired(objectType)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getDefNodeNonIndexedMetadataKey(graphId, objectType.getId());
            return getDefNodeList(jedis, key);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public BaseValueObjectList<StringValue> getOutRelationObjectTypes(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue objectType = (StringValue) request.get(GraphDACParams.OBJECT_TYPE.name());
        if (!manager.validateRequired(objectType)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getDefNodeOutRelationsKey(graphId, objectType.getId());
            return getDefNodeList(jedis, key);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public BaseValueObjectList<StringValue> getInRelationObjectTypes(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue objectType = (StringValue) request.get(GraphDACParams.OBJECT_TYPE.name());
        if (!manager.validateRequired(objectType)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getDefNodeInRelationsKey(graphId, objectType.getId());
            return getDefNodeList(jedis, key);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_DEF_NODE_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    private BaseValueObjectList<StringValue> getDefNodeList(Jedis jedis, String key) {
        Set<String> members = jedis.smembers(key);
        List<StringValue> requiredList = new ArrayList<StringValue>();
        if (null != members && members.size() > 0) {
            for (String s : members) {
                requiredList.add(new StringValue(s));
            }
        }
        return new BaseValueObjectList<StringValue>(requiredList);
    }

    private void saveList(Jedis jedis, BaseValueObjectList<StringValue> metadata, String key, boolean sort) {
        if (null != metadata && null != metadata.getValueObjectList() && !metadata.getValueObjectList().isEmpty()) {
            List<String> list = new ArrayList<String>();
            for (StringValue val : metadata.getValueObjectList()) {
                list.add(val.getId());
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
