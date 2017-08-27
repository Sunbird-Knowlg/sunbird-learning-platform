package com.ilimi.graph.cache.mgr.impl;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.cache.util.CacheKeyGenerator;
import redis.clients.jedis.Jedis;

public class SetCacheManager {

    public static void createSet(String graphId, String setId, List<String> memberIds) {
    	validateRequired(graphId, setId, memberIds, GraphCacheErrorCodes.ERR_CACHE_CREATE_SET_ERROR.name());
        Jedis jedis = getRedisConncetion();
        try {
            String[] members = new String[memberIds.size()];
            for (int i = 0; i < memberIds.size(); i++) {
                members[i] = memberIds.get(i);
            }
            String setMembersKey = CacheKeyGenerator.getSetMembersKey(graphId, setId);
            jedis.sadd(setMembersKey, members);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_CREATE_SET_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    
    public static void addSetMember(String graphId, String setId, String memberId) {
    	validateRequired(graphId, setId, memberId, GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name());
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
    	validateRequired(graphId, setId, memberIds, GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name());
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
    	validateRequired(graphId, setId, memberId, GraphCacheErrorCodes.ERR_CACHE_REMOVE_SET_MEMBER.name());
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
        validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_DROP_SET.name());
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
    	validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name());
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
        validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name());
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

    
    public static Boolean isSetMember(String graphId, String setId, String member) {
    	validateRequired(graphId, setId, member, GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name());
        Jedis jedis = getRedisConncetion();
        try {
            String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
            Boolean isMember = jedis.sismember(key, member);
            return isMember;
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }
    
    private static void validateRequired(String graphId, String id, Object members, String errCode) {
		validateRequired(graphId, id, errCode);
		if (null == members)
			throw new ClientException(errCode, "member(s) is null.");
	}

	private static void validateRequired(String graphId, String id, String errCode) {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(errCode, "graphId is missing");
		if (StringUtils.isBlank(id))
			throw new ClientException(errCode, "id is missing");
	}
}