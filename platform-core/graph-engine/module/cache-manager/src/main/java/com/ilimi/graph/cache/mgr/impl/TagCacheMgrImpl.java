package com.ilimi.graph.cache.mgr.impl;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.cache.mgr.ITagCacheMgr;
import com.ilimi.graph.cache.util.RedisKeyGenerator;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.common.enums.GraphDACParams;

public class TagCacheMgrImpl implements ITagCacheMgr {

    private BaseGraphManager manager;

    public TagCacheMgrImpl(BaseGraphManager manager) {
        this.manager = manager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void createTag(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String tagId = (String) request.get(GraphDACParams.tag_id.name());
        List<String> memberIds = (List<String>) request.get(GraphDACParams.members.name());
        if (!manager.validateRequired(tagId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_CREATE_TAG_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            if (manager.validateRequired(memberIds)) {
                String key = RedisKeyGenerator.getTagMembersKey(graphId, tagId);
                String[] members = new String[memberIds.size()];
                for (int i = 0; i < memberIds.size(); i++) {
                    members[i] = memberIds.get(i);
                }
                jedis.sadd(key, members);
            }
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_CREATE_TAG_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public void addTagMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String tagId = (String) request.get(GraphDACParams.tag_id.name());
        String memberId = (String) request.get(GraphDACParams.member_id.name());
        if (!manager.validateRequired(tagId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_ADD_TAG_MEMBER.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getTagMembersKey(graphId, tagId);
            jedis.sadd(key, memberId);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_ADD_TAG_MEMBER.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addTagMembers(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String tagId = (String) request.get(GraphDACParams.tag_id.name());
        List<String> memberIds = (List<String>) request.get(GraphDACParams.members.name());
        if (!manager.validateRequired(tagId, memberIds)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_ADD_TAG_MEMBER.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getTagMembersKey(graphId, tagId);
            String[] members = new String[memberIds.size()];
            for (int i = 0; i < memberIds.size(); i++) {
                members[i] = memberIds.get(i);
            }
            jedis.sadd(key, members);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_ADD_TAG_MEMBER.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public void removeTagMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String tagId = (String) request.get(GraphDACParams.tag_id.name());
        String memberId = (String) request.get(GraphDACParams.member_id.name());
        if (!manager.validateRequired(tagId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_REMOVE_TAG_MEMBER.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getTagMembersKey(graphId, tagId);
            jedis.srem(key, memberId);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_REMOVE_TAG_MEMBER.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public void dropTag(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String tagId = (String) request.get(GraphDACParams.tag_id.name());
        if (!manager.validateRequired(tagId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_DROP_TAG.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getTagMembersKey(graphId, tagId);
            jedis.del(key);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_DROP_TAG.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }

    }

    @Override
    public List<String> getTagMembers(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String tagId = (String) request.get(GraphDACParams.tag_id.name());
        if (!manager.validateRequired(tagId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_TAG_GET_MEMBERS.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getTagMembersKey(graphId, tagId);
            Set<String> members = jedis.smembers(key);
            List<String> memberIds = new LinkedList<String>();
            if (null != members && !members.isEmpty()) {
                memberIds.addAll(members);
            }
            return memberIds;
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_TAG_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public Long getCardinality(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String tagId = (String) request.get(GraphDACParams.tag_id.name());
        if (!manager.validateRequired(tagId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_TAG_GET_MEMBERS.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getTagMembersKey(graphId, tagId);
            Long cardinality = jedis.scard(key);
            return cardinality;
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_TAG_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public Boolean isTagMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String tagId = (String) request.get(GraphDACParams.tag_id.name());
        String memberId = (String) request.get(GraphDACParams.member_id.name());
        if (!manager.validateRequired(tagId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), "IsSetMember: Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getSetMembersKey(graphId, tagId);
            Boolean isMember = jedis.sismember(key, memberId);
            return isMember;
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_TAG_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

}
