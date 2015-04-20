package com.ilimi.graph.cache.mgr.impl;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.cache.mgr.ITagCacheMgr;
import com.ilimi.graph.cache.util.RedisKeyGenerator;
import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.BooleanValue;
import com.ilimi.graph.common.dto.LongIdentifier;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;

public class TagCacheMgrImpl implements ITagCacheMgr {

    private BaseGraphManager manager;

    public TagCacheMgrImpl(BaseGraphManager manager) {
        this.manager = manager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void createTag(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue tagId = (StringValue) request.get(GraphDACParams.TAG_ID.name());
        BaseValueObjectList<StringValue> memberIds = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.MEMBERS.name());
        if (!manager.validateRequired(tagId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_CREATE_TAG_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            if (manager.validateRequired(memberIds)) {
                String key = RedisKeyGenerator.getTagMembersKey(graphId, tagId.getId());
                String[] members = new String[memberIds.getValueObjectList().size()];
                for (int i = 0; i < memberIds.getValueObjectList().size(); i++) {
                    members[i] = memberIds.getValueObjectList().get(i).getId();
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue tagId = (StringValue) request.get(GraphDACParams.TAG_ID.name());
        StringValue memberId = (StringValue) request.get(GraphDACParams.MEMBER_ID.name());
        if (!manager.validateRequired(tagId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_ADD_TAG_MEMBER.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getTagMembersKey(graphId, tagId.getId());
            jedis.sadd(key, memberId.getId());
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_ADD_TAG_MEMBER.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addTagMembers(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue tagId = (StringValue) request.get(GraphDACParams.TAG_ID.name());
        BaseValueObjectList<StringValue> memberIds = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.MEMBERS.name());
        if (!manager.validateRequired(tagId, memberIds)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_ADD_TAG_MEMBER.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getTagMembersKey(graphId, tagId.getId());
            String[] members = new String[memberIds.getValueObjectList().size()];
            for (int i = 0; i < memberIds.getValueObjectList().size(); i++) {
                members[i] = memberIds.getValueObjectList().get(i).getId();
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue tagId = (StringValue) request.get(GraphDACParams.TAG_ID.name());
        StringValue memberId = (StringValue) request.get(GraphDACParams.MEMBER_ID.name());
        if (!manager.validateRequired(tagId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_REMOVE_TAG_MEMBER.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getTagMembersKey(graphId, tagId.getId());
            jedis.srem(key, memberId.getId());
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_REMOVE_TAG_MEMBER.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public void dropTag(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue tagId = (StringValue) request.get(GraphDACParams.TAG_ID.name());
        if (!manager.validateRequired(tagId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_DROP_TAG.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getTagMembersKey(graphId, tagId.getId());
            jedis.del(key);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_DROP_TAG.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }

    }

    @Override
    public BaseValueObjectList<StringValue> getTagMembers(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue tagId = (StringValue) request.get(GraphDACParams.TAG_ID.name());
        if (!manager.validateRequired(tagId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_TAG_GET_MEMBERS.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getTagMembersKey(graphId, tagId.getId());
            Set<String> members = jedis.smembers(key);
            List<StringValue> memberIds = new LinkedList<StringValue>();
            if (null != members && !members.isEmpty()) {
                for (String memberId : members) {
                    memberIds.add(new StringValue(memberId));
                }
            }
            return new BaseValueObjectList<StringValue>(memberIds);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_TAG_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public LongIdentifier getCardinality(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue tagId = (StringValue) request.get(GraphDACParams.TAG_ID.name());
        if (!manager.validateRequired(tagId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_TAG_GET_MEMBERS.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getTagMembersKey(graphId, tagId.getId());
            Long cardinality = jedis.scard(key);
            return new LongIdentifier(cardinality);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_TAG_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public BooleanValue isTagMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue tagId = (StringValue) request.get(GraphDACParams.TAG_ID.name());
        StringValue memberId = (StringValue) request.get(GraphDACParams.MEMBER_ID.name());
        if (!manager.validateRequired(tagId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SET_GET_MEMBERS.name(), "IsSetMember: Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getSetMembersKey(graphId, tagId.getId());
            Boolean isMember = jedis.sismember(key, memberId.getId());
            return new BooleanValue(isMember);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_TAG_GET_MEMBERS.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

}
