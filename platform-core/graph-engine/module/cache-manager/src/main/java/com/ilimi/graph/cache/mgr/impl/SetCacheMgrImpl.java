package com.ilimi.graph.cache.mgr.impl;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String setId = (String) request.get(GraphDACParams.set_id.name());
        List<String> memberIds = (List<String>) request.get(GraphDACParams.members.name());
        String objectType = (String) request.get(GraphDACParams.object_type.name());
        Map<String, Object> criteria = (Map<String, Object>) request.get(GraphDACParams.criteria.name());
        List<String> indexedFields = (List<String>) request.get(GraphDACParams.indexable_metadata_key.name());
        if (!manager.validateRequired(setId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_CREATE_SET_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            if (manager.validateRequired(objectType)) {
                String criteriaKey = RedisKeyGenerator.getSetCriteriaKey(graphId, objectType);
                if (manager.validateRequired(criteria)) {
                    if (null == indexedFields || indexedFields.isEmpty()) {
                        throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_CREATE_SET_ERROR.name(),
                                "There are no indexed fields defined for :" + objectType);
                    } else {
                        Collections.sort(indexedFields);
                        for (String index : indexedFields) {
                            if (criteria.containsKey(index)) {
                                criteriaKey += index;
                            } else {
                                criteriaKey += "*";
                            }
                        }
                        for (Entry<String, Object> entry : criteria.entrySet()) {
                            String criteriaValueKey = RedisKeyGenerator.getSetCriteriaValueKey(graphId, setId, entry.getKey());
                            Set<String> list = RedisPropValueUtil.getStringifiedValue(entry.getValue());
                            if (null != list && !list.isEmpty()) {
                                jedis.sadd(criteriaValueKey, RedisPropValueUtil.convertSetToArray(list));
                            }
                        }
                    }
                } else {
                    criteriaKey += "*";
                }
                jedis.sadd(criteriaKey, setId);
            }
            if (manager.validateRequired(memberIds)) {
                String[] members = new String[memberIds.size()];
                for (int i = 0; i < memberIds.size(); i++) {
                    members[i] = memberIds.get(i);
                }
                String setMembersKey = RedisKeyGenerator.getSetMembersKey(graphId, setId);
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String setId = (String) request.get(GraphDACParams.set_id.name());
        String memberId = (String) request.get(GraphDACParams.member_id.name());
        if (!manager.validateRequired(setId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String setMembersKey = RedisKeyGenerator.getSetMembersKey(graphId, setId);
            jedis.sadd(setMembersKey, memberId);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addSetMembers(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String setId = (String) request.get(GraphDACParams.set_id.name());
        List<String> memberIds = (List<String>) request.get(GraphDACParams.members.name());
        if (!manager.validateRequired(setId, memberIds)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String setMembersKey = RedisKeyGenerator.getSetMembersKey(graphId, setId);
            String[] members = new String[memberIds.size()];
            for (int i = 0; i < memberIds.size(); i++) {
                members[i] = memberIds.get(i);
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String setId = (String) request.get(GraphDACParams.set_id.name());
        String memberId = (String) request.get(GraphDACParams.member_id.name());
        if (!manager.validateRequired(setId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_REMOVE_SET_MEMBER.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String setMembersKey = RedisKeyGenerator.getSetMembersKey(graphId, setId);
            jedis.srem(setMembersKey, memberId);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_REMOVE_SET_MEMBER.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public void dropSet(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String setId = (String) request.get(GraphDACParams.set_id.name());
        if (!manager.validateRequired(setId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_DROP_SET.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String setMembersKey = RedisKeyGenerator.getSetMembersKey(graphId, setId);
            jedis.del(setMembersKey);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_DROP_SET.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }

    }

    @Override
    public List<String> getSetMembers(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String setId = (String) request.get(GraphDACParams.set_id.name());
        if (!manager.validateRequired(setId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getSetMembersKey(graphId, setId);
            Set<String> members = jedis.smembers(key);
            List<String> memberIds = new LinkedList<String>();
            if (null != members && !members.isEmpty()) {
                memberIds.addAll(members);
            }
            return memberIds;
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public Long getSetCardinality(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String setId = (String) request.get(GraphDACParams.set_id.name());
        if (!manager.validateRequired(setId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getSetMembersKey(graphId, setId);
            Long cardinality = jedis.scard(key);
            return cardinality;
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public Boolean isSetMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String setId = (String) request.get(GraphDACParams.set_id.name());
        String memberId = (String) request.get(GraphDACParams.member_id.name());
        if (!manager.validateRequired(setId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), "IsSetMember: Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getSetMembersKey(graphId, setId);
            Boolean isMember = jedis.sismember(key, memberId);
            return isMember;
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

}
