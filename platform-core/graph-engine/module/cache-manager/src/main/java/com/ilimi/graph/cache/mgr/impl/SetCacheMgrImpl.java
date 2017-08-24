package com.ilimi.graph.cache.mgr.impl;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.cache.mgr.ISetCacheMgr;
import com.ilimi.graph.cache.util.CacheKeyGenerator;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;

import redis.clients.jedis.Jedis;

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
            String setMembersKey = CacheKeyGenerator.getSetMembersKey(graphId, setId);
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
            String setMembersKey = CacheKeyGenerator.getSetMembersKey(graphId, setId);
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
            String setMembersKey = CacheKeyGenerator.getSetMembersKey(graphId, setId);
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

    @Override
    public Long getSetCardinality(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String setId = (String) request.get(GraphDACParams.set_id.name());
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
            String key = CacheKeyGenerator.getSetMembersKey(graphId, setId);
            Boolean isMember = jedis.sismember(key, memberId);
            return isMember;
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

}
