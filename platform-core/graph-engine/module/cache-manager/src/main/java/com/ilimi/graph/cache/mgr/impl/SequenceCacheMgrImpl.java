package com.ilimi.graph.cache.mgr.impl;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.cache.mgr.ISequenceCacheMgr;
import com.ilimi.graph.cache.util.CacheKeyGenerator;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;

import redis.clients.jedis.Jedis;

public class SequenceCacheMgrImpl implements ISequenceCacheMgr {

    private BaseGraphManager manager;

    public SequenceCacheMgrImpl(BaseGraphManager manager) {
        this.manager = manager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void createSequence(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String sequenceId = (String) request.get(GraphDACParams.sequence_id.name());
        List<String> memberIds = (List<String>) request.get(GraphDACParams.members.name());
        if (!manager.validateRequired(sequenceId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_CREATE_SEQ_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
            Map<String, Double> sortedMap = new HashMap<String, Double>();
            double i = 1;
            for (String memberId : memberIds) {
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

    @Override
    public Long addSequenceMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String sequenceId = (String) request.get(GraphDACParams.sequence_id.name());
        Long index = (Long) request.get(GraphDACParams.index.name());
        String memberId = (String) request.get(GraphDACParams.member_id.name());
        if (!manager.validateRequired(sequenceId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SEQ_ADD_MEMBER_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
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

    @Override
    public void removeSequenceMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String sequenceId = (String) request.get(GraphDACParams.sequence_id.name());
        String memberId = (String) request.get(GraphDACParams.member_id.name());
        if (!manager.validateRequired(sequenceId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SEQ_REMOVE_MEMBER_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
            jedis.zrem(key, memberId);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SEQ_REMOVE_MEMBER_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public void dropSequence(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String sequenceId = (String) request.get(GraphDACParams.sequence_id.name());
        if (!manager.validateRequired(sequenceId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_DROP_SEQ_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
            jedis.del(key);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_DROP_SEQ_ERROR.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
    }

    @Override
    public List<String> getSequenceMembers(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String sequenceId = (String) request.get(GraphDACParams.sequence_id.name());
        if (!manager.validateRequired(sequenceId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
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

    @Override
    public Long getSequenceCardinality(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String sequenceId = (String) request.get(GraphDACParams.sequence_id.name());
        if (!manager.validateRequired(sequenceId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name(), "Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
            Long cardinality = jedis.zcard(key);
            return cardinality;
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name(), e.getMessage(), e);
        } finally {
            returnConnection(jedis);
        }
    }

    public Boolean isSequenceMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String sequenceId = (String) request.get(GraphDACParams.sequence_id.name());
        String memberId = (String) request.get(GraphDACParams.member_id.name());
        if (!manager.validateRequired(sequenceId, memberId)) {
            throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SEQ_GET_MEMBERS_ERROR.name(),
                    "IsSequenceMember: Required parameters are missing");
        }
        Jedis jedis = getRedisConncetion();
        try {
            String key = CacheKeyGenerator.getSequenceMembersKey(graphId, sequenceId);
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

}
