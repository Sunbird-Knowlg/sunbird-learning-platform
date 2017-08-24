package com.ilimi.graph.cache.mgr.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.cache.util.CacheKeyGenerator;
import com.ilimi.graph.common.mgr.BaseGraphManager;

public class SetCacheMemoryManager {

    private static BaseGraphManager manager;
    
    private static Map<String, Object> setNodeCache = new HashMap<String,Object>();
	
    public static void createSet(String graphId, String setId, List<String> memberIds) {
    	validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_CREATE_SET_ERROR.name());
        if (manager.validateRequired(memberIds)) {
            String[] members = new String[memberIds.size()];
            for (int i = 0; i < memberIds.size(); i++) {
                members[i] = memberIds.get(i);
            }
            String setMembersKey = CacheKeyGenerator.getSetMembersKey(graphId, setId);
            setNodeCache.put(setMembersKey, members);
        }
    }

    public static void addSetMember(String graphId, String setId, String memberId) {
    	validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name());
        if (!manager.validateRequired(setId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name(), "Required parameters are missing");
        }
        String setMembersKey = CacheKeyGenerator.getSetMembersKey(graphId, setId);
        setNodeCache.put(setMembersKey, memberId);
    }

    public static void addSetMembers(String graphId, String setId, List<String> memberIds) {
    	validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name());
        if (!manager.validateRequired(setId, memberIds))
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_ADD_SET_MEMBER.name(), "Required parameters are missing");
        String setMembersKey = CacheKeyGenerator.getSetMembersKey(graphId, setId);
        String[] members = new String[memberIds.size()];
        for (int i = 0; i < memberIds.size(); i++) {
            members[i] = memberIds.get(i);
        }
        setNodeCache.put(setMembersKey, members);
    }

    public static void removeSetMember(String graphId, String setId, String memberId) {
    	validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_REMOVE_SET_MEMBER.name());
        if (!manager.validateRequired(setId, memberId))
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_REMOVE_SET_MEMBER.name(), "Required parameters are missing");
        String setMembersKey = CacheKeyGenerator.getSetMembersKey(graphId, setId);
        setNodeCache.remove(setMembersKey, memberId);
    }

    public static void dropSet(String graphId, String setId) {
    	validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_DROP_SET.name());
        if (!manager.validateRequired(setId))
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_DROP_SET.name(), "Required parameters are missing");
        String setMembersKey = CacheKeyGenerator.getSetMembersKey(graphId, setId);
        setNodeCache.remove(setMembersKey);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<String> getSetMembers(String graphId, String setId) {
    	validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name());
        if (!manager.validateRequired(setId))
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), "Required parameters are missing");
        String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
		Set<String> members = (Set)setNodeCache.get(key);
        List<String> memberIds = new LinkedList<String>();
        if (null != members && !members.isEmpty()) {
            memberIds.addAll(members);
        }
        return memberIds;
    }

    public static Long getSetCardinality(String graphId, String setId) {
    	validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name());
        if (!manager.validateRequired(setId))
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), "Required parameters are missing");
        String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
        Long cardinality = (Long)setNodeCache.get(key);
        return cardinality;
    }

    public static Boolean isSetMember(String graphId, String setId, String memberId) {
    	validateRequired(graphId, setId, GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name());
        if (!manager.validateRequired(setId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), "IsSetMember: Required parameters are missing");
        }
        String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
        Boolean isMember = setNodeCache.get(memberId).equals(key);
        return isMember;
    }
    
    private static void validateRequired(String graphId, String id, String errCode) {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(errCode, "graphId is missing");
		if (StringUtils.isBlank(id))
			throw new ClientException(errCode, "id/objectType is missing");
	}
}
