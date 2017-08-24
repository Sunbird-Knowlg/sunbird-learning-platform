package com.ilimi.graph.cache.mgr.impl;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.cache.util.CacheKeyGenerator;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import redis.clients.jedis.Jedis;

public class SetCacheManager {

    private static BaseGraphManager manager;

    public static void createSet(String graphId, String setId, List<String> memberIds) {
        if (!manager.validateRequired(setId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_CREATE_SET_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            if (manager.validateRequired(memberIds)) {
                String[] members = new String[memberIds.size()];
                for (int i = 0; i < memberIds.size(); i++) {
                    members[i] = memberIds.get(i);
                }
                String setMembersKey = CacheKeyGenerator.getSetMembersKey(graphId, setId);
                jedis.sadd(setMembersKey, members);
            }
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_CREATE_SET_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    
    public static void addSetMember(String graphId, String setId, String memberId) {
        if (!manager.validateRequired(setId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String setMembersKey = CacheKeyGenerator.getSetMembersKey(graphId, setId);
            jedis.sadd(setMembersKey, memberId);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    public static void addSetMembers(String graphId, String setId, List<String> memberIds) {
        if (!manager.validateRequired(setId, memberIds)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String setMembersKey = CacheKeyGenerator.getSetMembersKey(graphId, setId);
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

    public static void removeSetMember(String graphId, String setId, String memberId) {
        if (!manager.validateRequired(setId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_REMOVE_SET_MEMBER.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String setMembersKey = CacheKeyGenerator.getSetMembersKey(graphId, setId);
            jedis.srem(setMembersKey, memberId);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_REMOVE_SET_MEMBER.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }
    
    public static void dropSet(String graphId, String setId) {
        if (!manager.validateRequired(setId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_DROP_SET.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String setMembersKey = CacheKeyGenerator.getSetMembersKey(graphId, setId);
            jedis.del(setMembersKey);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_DROP_SET.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    public static List<String> getSetMembers(String graphId, String setId) {
        if (!manager.validateRequired(setId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
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
    
    public static Long getSetCardinality(String graphId, String setId) {
        if (!manager.validateRequired(setId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
            Long cardinality = jedis.scard(key);
            return cardinality;
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

    
    public static Boolean isSetMember(String graphId, String setId, String members) {
        if (!manager.validateRequired(setId, members)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), "IsSetMember: Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
            Boolean isMember = jedis.sismember(key, members);
            return isMember;
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }
}