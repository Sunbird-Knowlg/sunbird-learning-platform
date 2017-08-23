//package com.ilimi.graph.model.collection;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.commons.lang3.StringUtils;
//
//import com.ilimi.common.dto.Request;
//import com.ilimi.common.dto.Response;
//import com.ilimi.common.exception.ClientException;
//import com.ilimi.common.exception.ResponseCode;
//import com.ilimi.graph.cache.actor.GraphCacheActorPoolMgr;
//import com.ilimi.graph.cache.actor.GraphCacheManagers;
//import com.ilimi.graph.common.mgr.BaseGraphManager;
//import com.ilimi.graph.dac.enums.GraphDACParams;
//import com.ilimi.graph.dac.enums.RelationTypes;
//import com.ilimi.graph.dac.enums.SystemNodeTypes;
//import com.ilimi.graph.dac.enums.SystemProperties;
//import com.ilimi.graph.dac.model.Node;
//import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
//import com.ilimi.graph.dac.router.GraphDACManagers;
//import com.ilimi.graph.exception.GraphEngineErrorCodes;
//
//import akka.actor.ActorRef;
//import akka.dispatch.Futures;
//import akka.dispatch.OnComplete;
//import akka.pattern.Patterns;
//import scala.concurrent.ExecutionContext;
//import scala.concurrent.Future;
//import scala.concurrent.Promise;
//
//public class Tag extends AbstractCollection {
//
//    public static final String TAG_NAME = SystemProperties.IL_TAG_NAME.name();
//    public static final String ATTRIBUTE_NAME = SystemProperties.IL_ATTRIBUTE_NAME.name();
//
//    private String tagName;
//    private String attributeName;
//    private List<String> memberIds;
//
//    public Tag(BaseGraphManager manager, String graphId, String id) {
//        super(manager, graphId, id, null); // TODO: Will add metadata if required.
//    }
//
//    public Tag(BaseGraphManager manager, String graphId, String tagName, String attributeName, List<String> memberIds) {
//        super(manager, graphId, null, null); // TODO: Will add metadata if required.
//        if (StringUtils.isBlank(tagName)) {
//            throw new ClientException(GraphEngineErrorCodes.ERR_EMPTY_TAG_NAME.name(), "Tag name cannot be empty");
//        }
//        this.tagName = tagName;
//        this.attributeName = attributeName;
//        String id = SystemNodeTypes.TAG.name() + "_" + this.tagName.trim();
//        if (StringUtils.isNotBlank(this.attributeName)) {
//            id += "_" + this.attributeName.trim();
//        }
//        setNodeId(id);
//        this.memberIds = memberIds;
//    }
//
//    public Future<String> upsert(final Request req) {
//        final Promise<String> promise = Futures.promise();
//        Future<String> future = promise.future();
//        final ExecutionContext ec = manager.getContext().dispatcher();
//        Future<Node> setFuture = getNodeObject(req, ec, getNodeId());
//        OnComplete<Node> getTagObject = new OnComplete<Node>() {
//            @Override
//            public void onComplete(Throwable arg0, Node set) throws Throwable {
//                if (null != arg0 || null == set) {
//                    ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
//                    Request dacRequest = new Request(req);
//                    dacRequest.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
//                    dacRequest.setOperation("addNode");
//                    dacRequest.put(GraphDACParams.node.name(), toNode());
//                    Future<Object> response = Patterns.ask(dacRouter, dacRequest, timeout);
//                    response.onComplete(new OnComplete<Object>() {
//                        @Override
//                        public void onComplete(Throwable arg0, Object arg1) throws Throwable {
//                            if (null != arg0 || !(arg1 instanceof Response)) {
//                                promise.success("Failed to create Tag: " + tagName);
//                            } else {
//                                Response res = (Response) arg1;
//                                if (manager.checkError(res)) {
//                                    promise.success("Failed to create Tag: " + tagName + " - " + manager.getErrorMessage(res));
//                                } else {
//                                    promise.success(null);
//                                }
//                            }
//                        }
//                    }, ec);
//                } else {
//                    promise.success(null);
//                }
//            }
//        };
//        setFuture.onComplete(getTagObject, ec);
//        return future;
//    }
//
//    @Override
//    public void create(final Request req) {
//        try {
//            final ExecutionContext ec = manager.getContext().dispatcher();
//            if (null != memberIds && memberIds.size() > 0) {
//                Future<Boolean> validMembers = checkMemberNodes(req, memberIds, ec);
//                validMembers.onComplete(new OnComplete<Boolean>() {
//                    @Override
//                    public void onComplete(Throwable arg0, Boolean arg1) throws Throwable {
//                        if (null != arg0) {
//                            manager.ERROR(arg0, getParent());
//                        } else {
//                            if (arg1) {
//                                createTagObject(req, ec);
//                            } else {
//                                manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_CREATE_TAG_INVALID_MEMBER_IDS.name(),
//                                        "Member Ids are invalid", ResponseCode.CLIENT_ERROR, getParent());
//                            }
//                        }
//                    }
//                }, ec);
//            } else {
//                createTagObject(req, ec);
//            }
//        } catch (Exception e) {
//            manager.ERROR(e, getParent());
//        }
//    }
//
//    @Override
//    public void addMember(final Request req) {
//        final String tagId = (String) req.get(GraphDACParams.collection_id.name());
//        final String memberId = (String) req.get(GraphDACParams.member_id.name());
//        if (!manager.validateRequired(tagId, memberId)) {
//            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_TAG_MEMBER_MISSING_REQ_PARAMS.name(),
//                    "Required parameters are missing...");
//        } else {
//            try {
//                final ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
//                final ExecutionContext ec = manager.getContext().dispatcher();
//                Future<Node> setFuture = getNodeObject(req, ec, tagId);
//                OnComplete<Node> getTagObject = new OnComplete<Node>() {
//                    @Override
//                    public void onComplete(Throwable arg0, Node set) throws Throwable {
//                        Future<Object> response = null;
//                        if (null != arg0 || null == set) {
//                            Request dacRequest = new Request(req);
//                            dacRequest.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
//                            dacRequest.setOperation("addNode");
//                            dacRequest.put(GraphDACParams.node.name(), toNode());
//                            dacRouter.tell(dacRequest, manager.getSelf());
//                            response = Patterns.ask(dacRouter, dacRequest, timeout);
//                        } else {
//                            response = Futures.successful(null);
//                        }
//                        response.onComplete(new OnComplete<Object>() {
//                            @Override
//                            public void onComplete(Throwable arg0, Object arg1) throws Throwable {
//                                Future<Node> nodeFuture = getNodeObject(req, ec, memberId);
//                                nodeFuture.onComplete(new OnComplete<Node>() {
//                                    public void onComplete(Throwable arg0, Node member) throws Throwable {
//                                        if (null != arg0 || null == member) {
//                                            manager.ERROR(arg0, getParent());
//                                        } else {
//                                            addMemberToTag(req, tagId, memberId);
//                                        }
//                                    };
//                                }, ec);
//                            }
//                        }, ec);
//                    }
//                };
//                setFuture.onComplete(getTagObject, manager.getContext().dispatcher());
//            } catch (Exception e) {
//                manager.handleException(e, getParent());
//            }
//        }
//    }
//
//    @Override
//    public void removeMember(Request req) {
//        try {
//            String tagId = (String) req.get(GraphDACParams.collection_id.name());
//            String memberId = (String) req.get(GraphDACParams.member_id.name());
//            if (!manager.validateRequired(tagId, memberId)) {
//                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_REMOVE_TAG_MEMBER_MISSING_REQ_PARAMS.name(),
//                        "Required parameters are missing...");
//            } else {
//                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
//                Request request = new Request(req);
//                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
//                request.setOperation("removeTagMember");
//                request.put(GraphDACParams.tag_id.name(), tagId);
//                request.put(GraphDACParams.member_id.name(), memberId);
//                Future<Object> response = Patterns.ask(cacheRouter, request, timeout);
//
//                ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
//                Request dacRequest = new Request(req);
//                dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
//                dacRequest.setOperation("deleteRelation");
//                dacRequest.put(GraphDACParams.start_node_id.name(), tagId);
//                dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SET_MEMBERSHIP.relationName());
//                dacRequest.put(GraphDACParams.end_node_id.name(), memberId);
//                dacRouter.tell(dacRequest, manager.getSelf());
//                manager.returnResponse(response, getParent());
//            }
//        } catch (Exception e) {
//            manager.handleException(e, getParent());
//        }
//    }
//
//    @Override
//    public void getMembers(Request req) {
//        try {
//            String tagId = (String) req.get(GraphDACParams.collection_id.name());
//            if (!manager.validateRequired(tagId)) {
//                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_GET_TAG_MEMBERS_INVALID_TAG_ID.name(),
//                        "Required parameters are missing...");
//            } else {
//                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
//                Request request = new Request(req);
//                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
//                request.setOperation("getTagMembers");
//                request.put(GraphDACParams.tag_id.name(), tagId);
//                Future<Object> response = Patterns.ask(cacheRouter, request, timeout);
//                manager.returnResponse(response, getParent());
//            }
//        } catch (Exception e) {
//            manager.handleException(e, getParent());
//        }
//    }
//
//    @Override
//    public void isMember(Request req) {
//        try {
//            String tagId = (String) req.get(GraphDACParams.collection_id.name());
//            String memberId = (String) req.get(GraphDACParams.member_id.name());
//            if (!manager.validateRequired(tagId, memberId)) {
//                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IS_TAG_MEMBER_MISSING_REQ_PARAMS.name(),
//                        "Required parameters are missing...");
//            } else {
//                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
//                Request request = new Request(req);
//                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
//                request.setOperation("isTagMember");
//                request.put(GraphDACParams.tag_id.name(), tagId);
//                request.put(GraphDACParams.member_id.name(), memberId);
//                Future<Object> response = Patterns.ask(cacheRouter, request, timeout);
//                manager.returnResponse(response, getParent());
//            }
//        } catch (Exception e) {
//            manager.handleException(e, getParent());
//        }
//    }
//
//    @Override
//    public void delete(Request req) {
//        try {
//            String tagId = (String) req.get(GraphDACParams.collection_id.name());
//            if (!manager.validateRequired(tagId)) {
//                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_DROP_TAG_INVALID_TAG_ID.name(),
//                        "Required parameters are missing...");
//            } else {
//                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
//                Request request = new Request(req);
//                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
//                request.setOperation("dropTag");
//                request.put(GraphDACParams.tag_id.name(), tagId);
//                Future<Object> response = Patterns.ask(cacheRouter, request, timeout);
//
//                ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
//                Request dacRequest = new Request(req);
//                dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
//                dacRequest.setOperation("deleteCollection");
//                dacRequest.put(GraphDACParams.collection_id.name(), tagId);
//                dacRouter.tell(dacRequest, manager.getSelf());
//
//                manager.returnResponse(response, getParent());
//            }
//        } catch (Exception e) {
//            manager.handleException(e, getParent());
//        }
//    }
//
//    @Override
//    public void getCardinality(Request req) {
//        try {
//            String tagId = (String) req.get(GraphDACParams.collection_id.name());
//            if (!manager.validateRequired(tagId)) {
//                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_GET_CARDINALITY_MISSING_REQ_PARAMS.name(),
//                        "Required parameters are missing...");
//            } else {
//                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
//                Request request = new Request(req);
//                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
//                request.setOperation("getTagCardinality");
//                request.put(GraphDACParams.tag_id.name(), tagId);
//                Future<Object> response = Patterns.ask(cacheRouter, request, timeout);
//                manager.returnResponse(response, getParent());
//            }
//        } catch (Exception e) {
//            manager.handleException(e, getParent());
//        }
//    }
//
//    @Override
//    public String getSystemNodeType() {
//        return SystemNodeTypes.TAG.name();
//    }
//
//    @Override
//    public Node toNode() {
//        Node node = new Node(getNodeId(), getSystemNodeType(), getFunctionalObjectType());
//        Map<String, Object> metadata = new HashMap<String, Object>();
//        if (StringUtils.isNotBlank(tagName)) {
//            metadata.put(TAG_NAME, tagName);
//        }
//        if (StringUtils.isNotBlank(attributeName)) {
//            metadata.put(ATTRIBUTE_NAME, attributeName);
//        }
//        if (!metadata.isEmpty()) {
//            node.setMetadata(metadata);
//        }
//        return node;
//    }
//
//    public void createTag(final Request req) {
//        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
//        String tagId = getNodeId();
//        Request dacRequest = new Request(req);
//        dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
//        dacRequest.setOperation("createCollection");
//        dacRequest.put(GraphDACParams.collection_id.name(), tagId);
//        dacRequest.put(GraphDACParams.node.name(), toNode());
//        dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SET_MEMBERSHIP.relationName());
//        dacRequest.put(GraphDACParams.members.name(), memberIds);
//        dacRouter.tell(dacRequest, manager.getSelf());
//
//        ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
//        Request request = new Request(req);
//        request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
//        request.setOperation("createTag");
//        request.put(GraphDACParams.tag_id.name(), tagId);
//        request.put(GraphDACParams.members.name(), memberIds);
//        cacheRouter.tell(request, manager.getSelf());
//    }
//
//    private void createTagObject(final Request req, final ExecutionContext ec) {
//        final ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
//        Request request = new Request(req);
//        request.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
//        request.setOperation("addNode");
//        request.put(GraphDACParams.node.name(), toNode());
//        Future<Object> dacFuture = Patterns.ask(dacRouter, request, timeout);
//
//        dacFuture.onComplete(new OnComplete<Object>() {
//            @Override
//            public void onComplete(Throwable arg0, Object arg1) throws Throwable {
//                if (null != arg0) {
//                    manager.ERROR(arg0, getParent());
//                } else {
//                    if (arg1 instanceof Response) {
//                        String tagId = getNodeId();
//                        ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
//                        Request request = new Request(req);
//                        request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
//                        request.setOperation("createTag");
//                        request.put(GraphDACParams.tag_id.name(), tagId);
//                        request.put(GraphDACParams.members.name(), memberIds);
//                        Future<Object> response = Patterns.ask(cacheRouter, request, timeout);
//
//                        if (null != memberIds && memberIds.size() > 0) {
//                            Request dacRequest = new Request(req);
//                            dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
//                            dacRequest.setOperation("createCollection");
//                            dacRequest.put(GraphDACParams.collection_id.name(), tagId);
//                            dacRequest.put(GraphDACParams.node.name(), toNode());
//                            dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SET_MEMBERSHIP.relationName());
//                            dacRequest.put(GraphDACParams.members.name(), memberIds);
//                            dacRouter.tell(dacRequest, manager.getSelf());
//                        }
//                        manager.returnResponse(response, getParent());
//                    } else {
//                        manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_CREATE_TAG_UNKNOWN_ERROR.name(), "Failed to create Tag",
//                                ResponseCode.SERVER_ERROR, getParent());
//                    }
//                }
//            }
//        }, ec);
//    }
//
//    private void addMemberToTag(Request req, String tagId, String memberId) {
//        ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
//        Request request = new Request(req);
//        request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
//        request.setOperation("addTagMember");
//        request.put(GraphDACParams.tag_id.name(), tagId);
//        request.put(GraphDACParams.member_id.name(), memberId);
//        Future<Object> response = Patterns.ask(cacheRouter, request, timeout);
//
//        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
//        Request dacRequest = new Request(req);
//        dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
//        dacRequest.setOperation("addRelation");
//        dacRequest.put(GraphDACParams.start_node_id.name(), tagId);
//        dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SET_MEMBERSHIP.relationName());
//        dacRequest.put(GraphDACParams.end_node_id.name(), memberId);
//        dacRouter.tell(dacRequest, manager.getSelf());
//        manager.returnResponse(response, getParent());
//    }
//
//	@Override
//	public void removeMembers(Request request) {
//		// TODO Auto-generated method stub
//		
//	}
//}
