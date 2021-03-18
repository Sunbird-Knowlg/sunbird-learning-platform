package org.sunbird.graph.cache.mgr.impl;

import static org.sunbird.graph.cache.factory.JedisFactory.getRedisConncetion;
import static org.sunbird.graph.cache.factory.JedisFactory.returnConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.cache.exception.GraphCacheErrorCodes;
import org.sunbird.graph.cache.util.CacheKeyGenerator;

import redis.clients.jedis.Jedis;

public class SetCacheManager {

	public static void createSet(String graphId, String setId, List<String> members) {
		validateRequired(graphId, setId, members, GraphCacheErrorCodes.ERR_CACHE_CREATE_SET_ERROR.name());
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
			Jedis jedis = getRedisConncetion();
			try {
				String[] tempMembers = new String[members.size()];
				members.toArray(tempMembers);
				jedis.sadd(key, tempMembers);
			} finally {
				returnConnection(jedis);
			}
	}

	public static void addSetMember(String graphId, String setId, String memberId) {
		validateRequired(graphId, setId, memberId, GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name());
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
		Jedis jedis = getRedisConncetion();
			try {
				jedis.sadd(key, memberId);
			} finally {
				returnConnection(jedis);
			}
	}

	public static void addSetMembers(String graphId, String setId, List<String> memberIds) {
		validateRequired(graphId, setId, memberIds, GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name());
		Jedis jedis = getRedisConncetion();
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
			try {
				String[] members = new String[memberIds.size()];
				for (int i = 0; i < memberIds.size(); i++) {
					members[i] = memberIds.get(i);
				}
				jedis.sadd(key, members);
			} catch (Exception e) {
				throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name(), e.getMessage());
			} finally {
				returnConnection(jedis);
			}
	}

	public static void removeSetMember(String graphId, String setId, String memberId) {
		validateRequired(graphId, setId, memberId, GraphCacheErrorCodes.ERR_CACHE_REMOVE_SET_MEMBER.name());
		Jedis jedis = getRedisConncetion();
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
			try {
				jedis.srem(key, memberId);
			} catch (Exception e) {
				throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_REMOVE_SET_MEMBER.name(), e.getMessage());
			} finally {
				returnConnection(jedis);
			}
	}

	public static void dropSet(String graphId, String setId) {
		validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_DROP_SET.name());
		Jedis jedis = getRedisConncetion();
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
			try {
				jedis.del(key);
			} catch (Exception e) {
				throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_DROP_SET.name(), e.getMessage());
			} finally {
				returnConnection(jedis);
			}
	}

	public static List<String> getSetMembers(String graphId, String setId) {
		validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name());
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
		List<String> members = new ArrayList<String>();
			Jedis jedis = getRedisConncetion();
			try {
				Set<String> memberIds = jedis.smembers(key);
				if (null != memberIds && !memberIds.isEmpty()) {
					members.addAll(memberIds);
				}
			} finally {
				returnConnection(jedis);
		}
		return members;
	}

	public static Long getSetCardinality(String graphId, String setId) {
		validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name());
		Jedis jedis = getRedisConncetion();
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
			try {
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
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
		Boolean isMember;
			try {
				isMember = jedis.sismember(key, member);
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