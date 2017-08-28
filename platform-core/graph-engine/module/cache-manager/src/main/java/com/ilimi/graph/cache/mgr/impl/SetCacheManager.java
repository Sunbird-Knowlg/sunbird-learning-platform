package com.ilimi.graph.cache.mgr.impl;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import java.util.ArrayList;

import java.util.HashMap;
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

public class SetCacheManager {

	private static Map<String, Object> setCache = new HashMap<String, Object>();

	public static void createSet(String graphId, String setId, List<String> members) {
		validateRequired(graphId, setId, members, GraphCacheErrorCodes.ERR_CACHE_CREATE_SET_ERROR.name());
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
		if ("redis".equalsIgnoreCase(Platform.config.getString("cache.type"))) {
			Jedis jedis = getRedisConncetion();
			try {
				String[] tempMembers = new String[members.size()];
				members.toArray(tempMembers);
				jedis.sadd(key, tempMembers);
			} finally {
				returnConnection(jedis);
			}
		} else {
			System.out.println(key instanceof String);
			setCache.put(key, members);
			System.out.println(setCache);
		}
	}

	@SuppressWarnings("unchecked")
	public static void addSetMember(String graphId, String setId, String memberId) {
		validateRequired(graphId, setId, memberId, GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name());
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
		Jedis jedis = getRedisConncetion();
		if ("redis".equalsIgnoreCase(Platform.config.getString("cache.type"))) {
			try {
				jedis.sadd(key, memberId);
			} finally {
				returnConnection(jedis);
			}
		} else {
			List<String> existingMembers = (List<String>) setCache.get(key);
			if (null == existingMembers)
				existingMembers = new ArrayList<String>();
			if (!existingMembers.contains(memberId))
				existingMembers.add(memberId);
			setCache.put(key, existingMembers);
		}
	}

	@SuppressWarnings("unchecked")
	public static void addSetMembers(String graphId, String setId, List<String> memberIds) {
		validateRequired(graphId, setId, memberIds, GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name());
		Jedis jedis = getRedisConncetion();
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
		if ("redis".equalsIgnoreCase(Platform.config.getString("cache.type"))) {
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
		} else {
			List<String> existingMembers = (List<String>) setCache.get(key);
			if (null == existingMembers)
				existingMembers = new ArrayList<String>();
			for (String id : memberIds) {
				if (!existingMembers.contains(id))
					existingMembers.add(id);
			}
			setCache.put(key, existingMembers);
		}
	}

	@SuppressWarnings("unchecked")
	public static void removeSetMember(String graphId, String setId, String memberId) {
		validateRequired(graphId, setId, memberId, GraphCacheErrorCodes.ERR_CACHE_REMOVE_SET_MEMBER.name());
		Jedis jedis = getRedisConncetion();
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
		if ("redis".equalsIgnoreCase(Platform.config.getString("cache.type"))) {
			try {
				jedis.srem(key, memberId);
			} catch (Exception e) {
				throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_REMOVE_SET_MEMBER.name(), e.getMessage());
			} finally {
				returnConnection(jedis);
			}
		} else {
			List<String> existingMembers = (List<String>) setCache.get(key);
			if (null == existingMembers)
				existingMembers = new ArrayList<String>();
			if (existingMembers.contains(memberId))
				setCache.remove(memberId);
		}
	}

	public static void dropSet(String graphId, String setId) {
		validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_DROP_SET.name());
		Jedis jedis = getRedisConncetion();
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
		if ("redis".equalsIgnoreCase(Platform.config.getString("cache.type"))) {
			try {
				jedis.del(key);
			} catch (Exception e) {
				throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_DROP_SET.name(), e.getMessage());
			} finally {
				returnConnection(jedis);
			}
		} else {
			if (setCache.containsKey(key))
				setCache.remove(key);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<String> getSetMembers(String graphId, String setId) {
		validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name());
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
		List<String> members = new ArrayList<String>();
		if ("redis".equalsIgnoreCase(Platform.config.getString("cache.type"))) {
			Jedis jedis = getRedisConncetion();
			try {
				Set<String> memberIds = jedis.smembers(key);
				if (null != memberIds && !memberIds.isEmpty()) {
					members.addAll(memberIds);
				}
			} finally {
				returnConnection(jedis);
			}
		} else {
			members = (List) setCache.get(key);
			if (null != members && members.size() > 0) {
				return members;
			}
		}
		return members;
	}

	public static Long getSetCardinality(String graphId, String setId) {
		validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name());
		Jedis jedis = getRedisConncetion();
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
		if ("redis".equalsIgnoreCase(Platform.config.getString("cache.type"))) {
			try {
				Long cardinality = jedis.scard(key);
				System.out.println("cardinality" + cardinality);
				return cardinality;
			} catch (Exception e) {
				throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), e.getMessage(), e);
			} finally {
				returnConnection(jedis);
			}
		} else {
			return 0L;
		}
	}

	@SuppressWarnings("unchecked")
	public static Boolean isSetMember(String graphId, String setId, String member) {
		validateRequired(graphId, setId, member, GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name());
		Jedis jedis = getRedisConncetion();
		String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
		Boolean isMember;
		if ("redis".equalsIgnoreCase(Platform.config.getString("cache.type"))) {
			try {
				isMember = jedis.sismember(key, member);
				return isMember;
			} catch (Exception e) {
				throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), e.getMessage(), e);
			} finally {
				returnConnection(jedis);
			}
		} else {
			isMember = false;
			List<String> memberIds = (List<String>) setCache.get(key);
			for (String id : memberIds) {
				if (member.equals(id))
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