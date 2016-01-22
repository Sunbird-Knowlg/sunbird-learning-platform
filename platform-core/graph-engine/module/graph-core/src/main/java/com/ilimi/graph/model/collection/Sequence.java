package com.ilimi.graph.model.collection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.actor.GraphCacheActorPoolMgr;
import com.ilimi.graph.cache.actor.GraphCacheManagers;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;

public class Sequence extends AbstractCollection {

    private List<String> memberIds;

    public Sequence(BaseGraphManager manager, String graphId, String id) {
        super(manager, graphId, id, null); // TODO: Will add metadata if required.
    }

    public Sequence(BaseGraphManager manager, String graphId, String id, List<String> memberIds) {
        super(manager, graphId, id, null); // TODO: Will add metadata if required.
        this.memberIds = memberIds;
    }

    @Override
    public void create(final Request req) {
        try {
            final ExecutionContext ec = manager.getContext().dispatcher();
            if (null != memberIds && memberIds.size() > 0) {
                Future<Boolean> validMembers = checkMemberNodes(req, memberIds, ec);
                validMembers.onComplete(new OnComplete<Boolean>() {
                    @Override
                    public void onComplete(Throwable arg0, Boolean arg1) throws Throwable {
                        if (null != arg0) {
                            manager.ERROR(arg0, getParent());
                        } else {
                            if (arg1) {
                                createSequenceObject(req, ec);
                            } else {
                                manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_CREATE_SEQUENCE_INVALID_MEMBERIDS.name(),
                                        "Member Ids are invalid", ResponseCode.CLIENT_ERROR, getParent());
                            }
                        }
                    }
                }, ec);
            } else {
                createSequenceObject(req, ec);
            }
        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_CREATE_SEQUENCE_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    @Override
    public void addMember(final Request req) {
        final String sequenceId = (String) req.get(GraphDACParams.collection_id.name());
        final String memberId = (String) req.get(GraphDACParams.member_id.name());
        if (!manager.validateRequired(sequenceId, memberId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_SEQUENCE_MEMBER_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            try {
                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                Request request = new Request(req);
                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                request.setOperation("addSequenceMember");
                request.put(GraphDACParams.sequence_id.name(), sequenceId);
                request.put(GraphDACParams.member_id.name(), memberId);
                Future<Object> response = Patterns.ask(cacheRouter, request, timeout);
                response.onComplete(new OnComplete<Object>() {
                    @Override
                    public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                        if (null != arg0) {
                            manager.handleException(arg0, getParent());
                        } else {
                            if (arg1 instanceof Response) {
                                Response res = (Response) arg1;
                                if (manager.checkError(res)) {
                                    manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SEQUENCE_MEMBER_UNKNOWN_ERROR.name(),
                                            manager.getErrorMessage(res), res.getResponseCode(), getParent());
                                } else {
                                    Long index = (Long) res.get(GraphDACParams.index.name());
                                    ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                                    Request dacRequest = new Request(req);
                                    dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
                                    dacRequest.setOperation("addRelation");
                                    dacRequest.put(GraphDACParams.start_node_id.name(), sequenceId);
                                    dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SEQUENCE_MEMBERSHIP.relationName());
                                    dacRequest.put(GraphDACParams.end_node_id.name(), memberId);
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    map.put(SystemProperties.IL_SEQUENCE_INDEX.name(), index);
                                    dacRequest.put(GraphDACParams.metadata.name(), map);
                                    dacRouter.tell(dacRequest, manager.getSelf());
                                    manager.OK(getParent());
                                }
                            } else {
                                manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SEQUENCE_MEMBER_UNKNOWN_ERROR.name(),
                                        "Add Sequence Member failed", ResponseCode.SERVER_ERROR, getParent());
                            }
                        }
                    }
                }, manager.getContext().dispatcher());
            } catch (Exception e) {
                manager.handleException(e, getParent());
            }
        }
    }

    @Override
    public void removeMember(Request req) {
        try {
            String sequenceId = (String) req.get(GraphDACParams.collection_id.name());
            String memberId = (String) req.get(GraphDACParams.member_id.name());
            if (!manager.validateRequired(sequenceId, memberId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_REMOVE_SEQUENCE_MEMBER_MISSING_REQ_PARAMS.name(),
                        "Required parameters are missing...");
            } else {
                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                Request request = new Request(req);
                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                request.setOperation("removeSequenceMember");
                request.put(GraphDACParams.sequence_id.name(), sequenceId);
                request.put(GraphDACParams.member_id.name(), memberId);
                Future<Object> response = Patterns.ask(cacheRouter, request, timeout);

                ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                Request dacRequest = new Request(req);
                dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
                dacRequest.setOperation("deleteRelation");
                dacRequest.put(GraphDACParams.start_node_id.name(), sequenceId);
                dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SEQUENCE_MEMBERSHIP.relationName());
                dacRequest.put(GraphDACParams.end_node_id.name(), memberId);
                dacRouter.tell(dacRequest, manager.getSelf());

                manager.returnResponse(response, getParent());
            }
        } catch (Exception e) {
            manager.handleException(e, getParent());
        }
    }

    @Override
    public void getMembers(Request req) {
        try {
            String sequenceId = (String) req.get(GraphDACParams.collection_id.name());
            if (!manager.validateRequired(sequenceId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_GET_SEQUENCE_MEMBERS_INVALID_SEQID.name(),
                        "Required parameters are missing...");
            } else {
                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                Request request = new Request(req);
                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                request.setOperation("getSequenceMembers");
                request.put(GraphDACParams.sequence_id.name(), sequenceId);
                Future<Object> response = Patterns.ask(cacheRouter, request, timeout);
                manager.returnResponse(response, getParent());
            }
        } catch (Exception e) {
            manager.handleException(e, getParent());
        }
    }

    @Override
    public void isMember(Request req) {
        try {
            String sequenceId = (String) req.get(GraphDACParams.collection_id.name());
            String memberId = (String) req.get(GraphDACParams.member_id.name());
            if (!manager.validateRequired(sequenceId, memberId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IS_SEQUENCE_MEMBER_MISSING_REQ_PARAMS.name(),
                        "Required parameters are missing...");
            } else {
                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                Request request = new Request(req);
                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                request.setOperation("isSequenceMember");
                request.put(GraphDACParams.sequence_id.name(), sequenceId);
                request.put(GraphDACParams.member_id.name(), memberId);
                Future<Object> response = Patterns.ask(cacheRouter, request, timeout);
                manager.returnResponse(response, getParent());
            }
        } catch (Exception e) {
            manager.handleException(e, getParent());
        }
    }

    @Override
    public void delete(Request req) {
        try {
            String sequenceId = (String) req.get(GraphDACParams.collection_id.name());
            if (!manager.validateRequired(sequenceId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_DROP_SEQUENCE_MISSING_REQ_PARAMS.name(),
                        "Required parameters are missing...");
            } else {
                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                Request request = new Request(req);
                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                request.setOperation("dropSequence");
                request.put(GraphDACParams.sequence_id.name(), sequenceId);
                Future<Object> response = Patterns.ask(cacheRouter, request, timeout);

                ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                Request dacRequest = new Request(req);
                dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
                dacRequest.setOperation("deleteCollection");
                dacRequest.put(GraphDACParams.collection_id.name(), sequenceId);
                dacRouter.tell(dacRequest, manager.getSelf());

                manager.returnResponse(response, getParent());
            }
        } catch (Exception e) {
            manager.handleException(e, getParent());
        }
    }

    @Override
    public void getCardinality(Request req) {
        try {
            String sequenceId = (String) req.get(GraphDACParams.collection_id.name());
            if (!manager.validateRequired(sequenceId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_GET_CARDINALITY_MISSING_REQ_PARAMS.name(),
                        "Required parameters are missing...");
            } else {
                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                Request request = new Request(req);
                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                request.setOperation("getSequenceCardinality");
                request.put(GraphDACParams.sequence_id.name(), sequenceId);
                Future<Object> response = Patterns.ask(cacheRouter, request, timeout);
                manager.returnResponse(response, getParent());
            }
        } catch (Exception e) {
            manager.handleException(e, getParent());
        }
    }

    @Override
    public String getSystemNodeType() {
        return SystemNodeTypes.SEQUENCE.name();
    }

    private void createSequenceObject(final Request req, final ExecutionContext ec) {
        final ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        Request request = new Request(req);
        request.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
        request.setOperation("addNode");
        request.put(GraphDACParams.node.name(), toNode());
        Future<Object> dacFuture = Patterns.ask(dacRouter, request, timeout);

        dacFuture.onComplete(new OnComplete<Object>() {
            @Override
            public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                if (null != arg0) {
                    manager.ERROR(arg0, getParent());
                } else {
                    if (arg1 instanceof Response) {
                        Response res = (Response) arg1;
                        if (manager.checkError(res)) {
                            manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_CREATE_SEQUENCE_UNKNOWN_ERROR.name(),
                                    manager.getErrorMessage(res), res.getResponseCode(), getParent());
                        } else {
                            String sequenceId = (String) res.get(GraphDACParams.node_id.name());
                            ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                            Request request = new Request(req);
                            request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                            request.setOperation("createSequence");
                            request.put(GraphDACParams.sequence_id.name(), sequenceId);
                            request.put(GraphDACParams.members.name(), memberIds);
                            Future<Object> response = Patterns.ask(cacheRouter, request, timeout);

                            if (null != memberIds && memberIds.size() > 0) {
                                Request dacRequest = new Request(req);
                                dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
                                dacRequest.setOperation("createCollection");
                                dacRequest.put(GraphDACParams.collection_id.name(), sequenceId);
                                dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SEQUENCE_MEMBERSHIP.relationName());
                                dacRequest.put(GraphDACParams.index.name(), SystemProperties.IL_SEQUENCE_INDEX.name());
                                dacRequest.put(GraphDACParams.members.name(), memberIds);
                                dacRouter.tell(dacRequest, manager.getSelf());
                            }
                            manager.returnResponse(response, getParent());
                        }
                    } else {
                        manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_CREATE_SEQUENCE_UNKNOWN_ERROR.name(),
                                "Failed to create Sequence node", ResponseCode.SERVER_ERROR, getParent());
                    }
                }
            }
        }, ec);
    }

}
