package com.ilimi.graph.cache.mgr.impl;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.cache.mgr.ISetCacheMgr;
import com.ilimi.graph.cache.util.RedisKeyGenerator;
import com.ilimi.graph.cache.util.RedisPropValueUtil;
import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.BaseValueObjectMap;
import com.ilimi.graph.common.dto.BooleanValue;
import com.ilimi.graph.common.dto.LongIdentifier;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;

public class SetCacheMgrImpl implements ISetCacheMgr {

    private BaseGraphManager manager;

    public SetCacheMgrImpl(BaseGraphManager manager) {
        this.manager = manager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void createSet(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue setId = (StringValue) request.get(GraphDACParams.SET_ID.name());
        BaseValueObjectList<StringValue> memberIds = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.MEMBERS.name());
        StringValue objectType = (StringValue) request.get(GraphDACParams.OBJECT_TYPE.name());
        BaseValueObjectMap<Object> criteria = (BaseValueObjectMap<Object>) request.get(GraphDACParams.CRITERIA.name());
        BaseValueObjectList<StringValue> indexedFields = (BaseValueObjectList<StringValue>) request
                .get(GraphDACParams.INDEXABLE_METADATA_KEY.name());
        if (!manager.validateRequired(setId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_CREATE_SET_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            if (manager.validateRequired(objectType)) {
                String criteriaKey = RedisKeyGenerator.getSetCriteriaKey(graphId, objectType.getId());
                if (manager.validateRequired(criteria)) {
                    if (null == indexedFields || null == indexedFields.getValueObjectList() || indexedFields.getValueObjectList().isEmpty()) {
                        throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_CREATE_SET_ERROR.name(),
                                "There are no indexed fields defined for :" + objectType.getId());
                    } else {
                        List<String> indexes = new ArrayList<String>();
                        for (StringValue val : indexedFields.getValueObjectList()) {
                            indexes.add(val.getId());
                        }
                        Collections.sort(indexes);
                        Map<String, Object> criteriaMap = criteria.getBaseValueMap();
                        for (String index : indexes) {
                            if (criteriaMap.containsKey(index)) {
                                criteriaKey += index;
                            } else {
                                criteriaKey += "*";
                            }
                        }
                        for (Entry<String, Object> entry : criteriaMap.entrySet()) {
                            String criteriaValueKey = RedisKeyGenerator.getSetCriteriaValueKey(graphId, setId.getId(), entry.getKey());
                            Set<String> list = RedisPropValueUtil.getStringifiedValue(entry.getValue());
                            if (null != list && !list.isEmpty()) {
                                jedis.sadd(criteriaValueKey, RedisPropValueUtil.convertSetToArray(list));
                            }
                        }
                    }
                } else {
                    criteriaKey += "*";
                }
                jedis.sadd(criteriaKey, setId.getId());
            }
            if (manager.validateRequired(memberIds)) {
                String[] members = new String[memberIds.getValueObjectList().size()];
                for (int i = 0; i < memberIds.getValueObjectList().size(); i++) {
                    members[i] = memberIds.getValueObjectList().get(i).getId();
                }
                String setMembersKey = RedisKeyGenerator.getSetMembersKey(graphId, setId.getId());
                jedis.sadd(setMembersKey, members);
            }
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_CREATE_SET_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public void addSetMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue setId = (StringValue) request.get(GraphDACParams.SET_ID.name());
        StringValue memberId = (StringValue) request.get(GraphDACParams.MEMBER_ID.name());
        if (!manager.validateRequired(setId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String setMembersKey = RedisKeyGenerator.getSetMembersKey(graphId, setId.getId());
            jedis.sadd(setMembersKey, memberId.getId());
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addSetMembers(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue setId = (StringValue) request.get(GraphDACParams.SET_ID.name());
        BaseValueObjectList<StringValue> memberIds = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.MEMBERS.name());
        if (!manager.validateRequired(setId, memberIds)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String setMembersKey = RedisKeyGenerator.getSetMembersKey(graphId, setId.getId());
            String[] members = new String[memberIds.getValueObjectList().size()];
            for (int i = 0; i < memberIds.getValueObjectList().size(); i++) {
                members[i] = memberIds.getValueObjectList().get(i).getId();
            }
            jedis.sadd(setMembersKey, members);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public void removeSetMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue setId = (StringValue) request.get(GraphDACParams.SET_ID.name());
        StringValue memberId = (StringValue) request.get(GraphDACParams.MEMBER_ID.name());
        if (!manager.validateRequired(setId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_REMOVE_SET_MEMBER.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String setMembersKey = RedisKeyGenerator.getSetMembersKey(graphId, setId.getId());
            jedis.srem(setMembersKey, memberId.getId());
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_REMOVE_SET_MEMBER.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public void dropSet(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue setId = (StringValue) request.get(GraphDACParams.SET_ID.name());
        if (!manager.validateRequired(setId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_DROP_SET.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String setMembersKey = RedisKeyGenerator.getSetMembersKey(graphId, setId.getId());
            jedis.del(setMembersKey);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_DROP_SET.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }

    }

    @Override
    public BaseValueObjectList<StringValue> getSetMembers(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue setId = (StringValue) request.get(GraphDACParams.SET_ID.name());
        if (!manager.validateRequired(setId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getSetMembersKey(graphId, setId.getId());
            Set<String> members = jedis.smembers(key);
            List<StringValue> memberIds = new LinkedList<StringValue>();
            if (null != members && !members.isEmpty()) {
                for (String memberId : members) {
                    memberIds.add(new StringValue(memberId));
                }
            }
            return new BaseValueObjectList<StringValue>(memberIds);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public LongIdentifier getSetCardinality(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue setId = (StringValue) request.get(GraphDACParams.SET_ID.name());
        if (!manager.validateRequired(setId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getSetMembersKey(graphId, setId.getId());
            Long cardinality = jedis.scard(key);
            return new LongIdentifier(cardinality);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public BooleanValue isSetMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue setId = (StringValue) request.get(GraphDACParams.SET_ID.name());
        StringValue memberId = (StringValue) request.get(GraphDACParams.MEMBER_ID.name());
        if (!manager.validateRequired(setId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), "IsSetMember: Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getSetMembersKey(graphId, setId.getId());
            Boolean isMember = jedis.sismember(key, memberId.getId());
            return new BooleanValue(isMember);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

}
