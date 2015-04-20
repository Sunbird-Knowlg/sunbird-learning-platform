package com.ilimi.graph.cache.mgr.impl;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.cache.mgr.ISequenceCacheMgr;
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

public class SequenceCacheMgrImpl implements ISequenceCacheMgr {

    private BaseGraphManager manager;

    public SequenceCacheMgrImpl(BaseGraphManager manager) {
        this.manager = manager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void createSequence(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue sequenceId = (StringValue) request.get(GraphDACParams.SEQUENCE_ID.name());
        BaseValueObjectList<StringValue> memberIds = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.MEMBERS.name());
        if (!manager.validateRequired(sequenceId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_CREATE_SEQ_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getSequenceMembersKey(graphId, sequenceId.getId());
            Map<String, Double> sortedMap = new HashMap<String, Double>();
            double i = 1;
            for (StringValue memberId : memberIds.getValueObjectList()) {
                sortedMap.put(memberId.getId(), i);
                i += 1;
            }
            jedis.zadd(key, sortedMap);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_CREATE_SEQ_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public LongIdentifier addSequenceMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue sequenceId = (StringValue) request.get(GraphDACParams.SEQUENCE_ID.name());
        LongIdentifier index = (LongIdentifier) request.get(GraphDACParams.INDEX.name());
        StringValue memberId = (StringValue) request.get(GraphDACParams.MEMBER_ID.name());
        if (!manager.validateRequired(sequenceId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SEQ_ADD_MEMBER_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getSequenceMembersKey(graphId, sequenceId.getId());
            if (null == index || null == index.getId() || index.getId().longValue() <= 0) {
                index = new LongIdentifier(jedis.zcard(key) + 1);
            }
            jedis.zadd(key, index.getId(), memberId.getId());
            return index;
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SEQ_ADD_MEMBER_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public void removeSequenceMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue sequenceId = (StringValue) request.get(GraphDACParams.SEQUENCE_ID.name());
        StringValue memberId = (StringValue) request.get(GraphDACParams.MEMBER_ID.name());
        if (!manager.validateRequired(sequenceId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SEQ_REMOVE_MEMBER_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getSequenceMembersKey(graphId, sequenceId.getId());
            jedis.zrem(key, memberId.getId());
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SEQ_REMOVE_MEMBER_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public void dropSequence(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue sequenceId = (StringValue) request.get(GraphDACParams.SEQUENCE_ID.name());
        if (!manager.validateRequired(sequenceId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_DROP_SEQ_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getSequenceMembersKey(graphId, sequenceId.getId());
            jedis.del(key);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_DROP_SEQ_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public BaseValueObjectList<StringValue> getSequenceMembers(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue sequenceId = (StringValue) request.get(GraphDACParams.SEQUENCE_ID.name());
        if (!manager.validateRequired(sequenceId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getSequenceMembersKey(graphId, sequenceId.getId());
            Set<String> members = jedis.zrange(key, 0, -1);
            List<StringValue> memberIds = new LinkedList<StringValue>();
            if (null != members && !members.isEmpty()) {
                for (String memberId : members) {
                    memberIds.add(new StringValue(memberId));
                }
            }
            return new BaseValueObjectList<StringValue>(memberIds);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public LongIdentifier getSequenceCardinality(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue sequenceId = (StringValue) request.get(GraphDACParams.SEQUENCE_ID.name());
        if (!manager.validateRequired(sequenceId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getSequenceMembersKey(graphId, sequenceId.getId());
            Long cardinality = jedis.zcard(key);
            return new LongIdentifier(cardinality);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

    public BooleanValue isSequenceMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue sequenceId = (StringValue) request.get(GraphDACParams.SEQUENCE_ID.name());
        StringValue memberId = (StringValue) request.get(GraphDACParams.MEMBER_ID.name());
        if (!manager.validateRequired(sequenceId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name(),
                    "IsSequenceMember: Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = RedisKeyGenerator.getSequenceMembersKey(graphId, sequenceId.getId());
            Double score = jedis.zscore(key, memberId.getId());
            if (null == score || score.doubleValue() <= 0) {
                return new BooleanValue(false);
            } else {
                return new BooleanValue(true);
            }
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

}
