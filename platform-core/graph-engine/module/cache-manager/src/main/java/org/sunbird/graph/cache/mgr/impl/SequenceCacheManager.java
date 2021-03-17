package org.sunbird.graph.cache.mgr.impl;

import static org.sunbird.graph.cache.factory.JedisFactory.getRedisConncetion;
import static org.sunbird.graph.cache.factory.JedisFactory.returnConnection;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.cache.exception.GraphCacheErrorCodes;
import org.sunbird.graph.cache.util.CacheKeyGenerator;

import redis.clients.jedis.Jedis;

public class SequenceCacheManager {

    public static void createSequence(String graphId, String sequenceId, List<String> members) {
    	validateRequired(graphId, sequenceId, members, GraphCacheErrorCodes.ERR_CACHE_CREATE_SEQ_ERROR.name());
        Jedis jedis = getRedisConncetion();
        String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
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
    }

	public static Long addSequenceMember(String graphId, String sequenceId, Long index, String memberId) {
        validateRequired(graphId, sequenceId, memberId, GraphCacheErrorCodes.ERR_CACHE_SEQ_ADD_MEMBER_ERROR.name());
        Jedis jedis = getRedisConncetion();
        String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
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
    }
	
	public static void removeSequenceMember(String graphId, String sequenceId, String memberId) {
        validateRequired(graphId, sequenceId, memberId, GraphCacheErrorCodes.ERR_CACHE_SEQ_REMOVE_MEMBER_ERROR.name());
        Jedis jedis = getRedisConncetion();
        String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
	        try {
	            jedis.zrem(key, memberId);
	        } catch (Exception e) {
	            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SEQ_REMOVE_MEMBER_ERROR.name(), e.getMessage());
	        } finally {
	            returnConnection(jedis);
	        }
    }

    public static void dropSequence(String graphId, String sequenceId) {
        validateRequired(graphId, sequenceId, GraphCacheErrorCodes.ERR_CACHE_DROP_SEQ_ERROR.name());
        Jedis jedis = getRedisConncetion();
        String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
	        try {
	            jedis.del(key);
	        } catch (Exception e) {
	            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_DROP_SEQ_ERROR.name(), e.getMessage());
	        } finally {
	            returnConnection(jedis);
	        }
    }

	public static List<String> getSequenceMembers(String graphId, String sequenceId) {
    	validateRequired(graphId, sequenceId, GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name());
        Jedis jedis = getRedisConncetion();
        String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
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
    }

	public static Long getSequenceCardinality(String graphId, String sequenceId) {
    	validateRequired(graphId, sequenceId, GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name());
        Jedis jedis = getRedisConncetion();
        String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
	        try {
	            Long cardinality = jedis.zcard(key);
	            return cardinality;
	        } catch (Exception e) {
	            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name(), e.getMessage(), e);
	        } finally {
	            returnConnection(jedis);
	        }
    }

	public static Boolean isSequenceMember(String graphId, String sequenceId, String memberId) {
    	validateRequired(graphId, sequenceId, memberId, GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name());
        Jedis jedis = getRedisConncetion();
        String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
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
