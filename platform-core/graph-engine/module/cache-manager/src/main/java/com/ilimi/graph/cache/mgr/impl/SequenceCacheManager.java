package com.ilimi.graph.cache.mgr.impl;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import com.ilimi.common.Platform;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.cache.util.CacheKeyGenerator;
import redis.clients.jedis.Jedis;

public class SequenceCacheManager {

	private static Map<String, Object> sequenceCache = new HashMap<String,Object>();
	
    public static void createSequence(String graphId, String sequenceId, List<String> members) {
    	validateRequired(graphId, sequenceId, members, GraphCacheErrorCodes.ERR_CACHE_CREATE_SEQ_ERROR.name());
        Jedis jedis = getRedisConncetion();
        String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
        if ("redis".equalsIgnoreCase(Platform.config.getString("cache.type"))) {
	        try {
	            Map<String, Double> sortedMap = new HashMap<String, Double>();
	            double i = 1;
	            for (String memberId : members) {
	                sortedMap.put(memberId, i);
	                i += 1;
	            }
	            jedis.zadd(key, sortedMap);
	        } catch (Exception e) {
	            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_CREATE_SEQ_ERROR.name(), e.getMessage());
	        } finally {
	            returnConnection(jedis);
	        }
        } else {
        	 Collections.sort(members);
        	 sequenceCache.put(key, members);
        }
    }

    @SuppressWarnings("unchecked")
	public static Long addSequenceMember(String graphId, String sequenceId, Long index, String memberId) {
        validateRequired(graphId, sequenceId, memberId, GraphCacheErrorCodes.ERR_CACHE_SEQ_ADD_MEMBER_ERROR.name());
        Jedis jedis = getRedisConncetion();
        String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
        if ("redis".equalsIgnoreCase(Platform.config.getString("cache.type"))) {
	        try {
	            if (null == index || index.longValue() <= 0) {
	                index = jedis.zcard(key) + 1;
	            }
	            jedis.zadd(key, index, memberId);
	            return index;
	        } catch (Exception e) {
	            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SEQ_ADD_MEMBER_ERROR.name(), e.getMessage());
	        } finally {
	            returnConnection(jedis);
	        }
        } else {
        	List<String> existingMembers = (List<String>) sequenceCache.get(key);
			if (null == existingMembers) existingMembers = new ArrayList<String>();
			if (!existingMembers.contains(memberId)) existingMembers.add(memberId);
				sequenceCache.put(key, existingMembers);
        }
		return index;
    }

    @SuppressWarnings("unchecked")
	public static void removeSequenceMember(String graphId, String sequenceId, String memberId) {
        validateRequired(graphId, sequenceId, memberId, GraphCacheErrorCodes.ERR_CACHE_SEQ_REMOVE_MEMBER_ERROR.name());
        Jedis jedis = getRedisConncetion();
        String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
        if ("redis".equalsIgnoreCase(Platform.config.getString("cache.type"))) {
	        try {
	            jedis.zrem(key, memberId);
	        } catch (Exception e) {
	            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SEQ_REMOVE_MEMBER_ERROR.name(), e.getMessage());
	        } finally {
	            returnConnection(jedis);
	        }
        } else {
	    	List<String> existingMembers = (List<String>) sequenceCache.get(key);
	    	if(null != existingMembers && existingMembers.contains(memberId))
	    		sequenceCache.remove(memberId);
        }
    }

    public static void dropSequence(String graphId, String sequenceId) {
        validateRequired(graphId, sequenceId, GraphCacheErrorCodes.ERR_CACHE_DROP_SEQ_ERROR.name());
        Jedis jedis = getRedisConncetion();
        String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
        if ("redis".equalsIgnoreCase(Platform.config.getString("cache.type"))) {
	        try {
	            jedis.del(key);
	        } catch (Exception e) {
	            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_DROP_SEQ_ERROR.name(), e.getMessage());
	        } finally {
	            returnConnection(jedis);
	        }
        } else {
        	if(sequenceCache.containsKey(key))
        		sequenceCache.remove(key);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<String> getSequenceMembers(String graphId, String sequenceId) {
    	validateRequired(graphId, sequenceId, GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name());
        Jedis jedis = getRedisConncetion();
        String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
        List<String> membersList = new ArrayList<String>();
        if ("redis".equalsIgnoreCase(Platform.config.getString("cache.type"))) {
	        try {   
	            Set<String> members = jedis.zrange(key, 0, -1);
	            List<String> memberIds = new LinkedList<String>();
	            if (null != members && !members.isEmpty()) {
	                for (String memberId : members) {
	                    memberIds.add(memberId);
	                }
	            }
	            return memberIds;
	        } catch (Exception e) {
	            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name(), e.getMessage(), e);
	        } finally {
	            returnConnection(jedis);
	        }
        } else {
        	membersList = (List) sequenceCache.get(key);
			if (null != membersList && membersList.size() > 0){
				return membersList;
			}	
        }
		return membersList;
    }

    public static Long getSequenceCardinality(String graphId, String sequenceId) {
    	validateRequired(graphId, sequenceId, GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name());
        Jedis jedis = getRedisConncetion();
        String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
        if ("redis".equalsIgnoreCase(Platform.config.getString("cache.type"))) {
	        try {
	            Long cardinality = jedis.zcard(key);
	            return cardinality;
	        } catch (Exception e) {
	            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name(), e.getMessage(), e);
	        } finally {
	            returnConnection(jedis);
	        }
        } else {
        	return 0L;
        }
    }

    @SuppressWarnings("unchecked")
	public static Boolean isSequenceMember(String graphId, String sequenceId, String memberId) {
    	validateRequired(graphId, sequenceId, memberId, GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name());
        Jedis jedis = getRedisConncetion();
        String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
        Boolean isMember;
        if ("redis".equalsIgnoreCase(Platform.config.getString("cache.type"))) {
	        try {
	            Double score = jedis.zscore(key, memberId);
	            if (null == score || score.doubleValue() <= 0) {
	                return false;
	            } else {
	                return true;
	            }
	        } catch (Exception e) {
	            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name(), e.getMessage(), e);
	        } finally {
	            returnConnection(jedis);
	        }
        } else {
        	isMember = false;
        	List<String> memberIds = (List<String>) sequenceCache.get(key);
        	for(String id: memberIds){
        		if(memberId.equals(id))
        		  isMember = true;
        		else
        	      isMember = false;
        	}
        }
        return isMember;
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
