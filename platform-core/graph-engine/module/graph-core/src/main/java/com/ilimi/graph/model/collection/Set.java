package com.ilimi.graph.model.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.graph.cache.actor.GraphCacheActorPoolMgr;
import com.ilimi.graph.cache.actor.GraphCacheManagers;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;

public class Set extends AbstractCollection {

    public static final String SET_OBJECT_TYPE_KEY = "SET_OBJECT_TYPE_KEY";
    public static final String SET_CRITERIA_KEY = "SET_CRITERIA_KEY";
    public static final String SET_CRITERIA_QUERY_KEY = "SET_CRITERIA_QUERY_KEY";
    public static final String SET_TYPE_KEY = "SET_TYPE";
    private SearchCriteria criteria;
    private String setObjectType;
    private String setCriteria;
    private String setType = SET_TYPES.MATERIALISED_SET.name();
    private List<String> memberIds;
    private ObjectMapper mapper = new ObjectMapper();

    public static enum SET_TYPES {
        MATERIALISED_SET, CRITERIA_SET;
    }

    public Set(BaseGraphManager manager, String graphId, String id, String setObjectType, SearchCriteria criteria) {
        super(manager, graphId, id);
        setCriteria(criteria);
        this.setObjectType = setObjectType;
        this.setType = SET_TYPES.CRITERIA_SET.name();
    }

    public Set(BaseGraphManager manager, String graphId, String id) {
        super(manager, graphId, id);
        this.setType = SET_TYPES.MATERIALISED_SET.name();
    }

    public Set(BaseGraphManager manager, String graphId, String id, String setObjectType, List<String> memberIds) {
        super(manager, graphId, id);
        this.memberIds = memberIds;
        this.setObjectType = setObjectType;
        this.setType = SET_TYPES.MATERIALISED_SET.name();
    }

    @Override
    public Node toNode() {
        Node node = new Node(getNodeId(), getSystemNodeType(), getFunctionalObjectType());
        if (null != criteria) {
            Map<String, Object> metadata = new HashMap<String, Object>();
            metadata.put(SET_OBJECT_TYPE_KEY, criteria.getObjectType());
            metadata.put(SET_CRITERIA_QUERY_KEY, criteria.getQuery());
            metadata.put(SET_TYPE_KEY, getSetType());
            metadata.put(SET_CRITERIA_KEY, getSetCriteria());
            if (!metadata.isEmpty()) {
                node.setMetadata(metadata);
            }
        }
        return node;
    }

    @Override
    public void create(Request request) {
        if (StringUtils.equalsIgnoreCase(SET_TYPES.CRITERIA_SET.name(), getSetType())) {
            createCriteriaSet(request);
        } else {
            createSet(request);
        }
    }

    @Override
    public void addMember(final Request req) {
        final String setId = (String) req.get(GraphDACParams.collection_id.name());
        final String memberId = (String) req.get(GraphDACParams.member_id.name());
        if (!manager.validateRequired(setId, memberId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            try {
                final ExecutionContext ec = manager.getContext().dispatcher();
                Future<Node> setFuture = getNodeObject(req, ec, setId);
                OnComplete<Node> getSetObject = new OnComplete<Node>() {
                    @Override
                    public void onComplete(Throwable arg0, Node set) throws Throwable {
                        if (null != arg0 || null == set) {
                            manager.ERROR(arg0, getParent());
                        } else {
                            Map<String, Object> metadata = set.getMetadata();
                            if (null == metadata) {
                                manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(), "Invalid Set",
                                        ResponseCode.CLIENT_ERROR, getParent());
                            } else {
                                String type = (String) metadata.get(SET_TYPE_KEY);
                                if (StringUtils.equalsIgnoreCase(SET_TYPES.CRITERIA_SET.name(), type)) {
                                    manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(),
                                            "Member cannot be added to criteria sets", ResponseCode.CLIENT_ERROR, getParent());
                                } else {
                                    Future<Node> nodeFuture = getNodeObject(req, ec, memberId);
                                    nodeFuture.onComplete(new OnComplete<Node>() {
                                        public void onComplete(Throwable arg0, Node member) throws Throwable {
                                            if (null != arg0 || null == member) {
                                                manager.ERROR(arg0, getParent());
                                            } else {
                                                addMemberToSet(req, setId, memberId);
                                            }
                                        };
                                    }, ec);
                                }
                            }
                        }
                    }
                };
                setFuture.onComplete(getSetObject, manager.getContext().dispatcher());
            } catch (Exception e) {
                manager.handleException(e, getParent());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addMembers(final Request req) {
        final String setId = (String) req.get(GraphDACParams.collection_id.name());
        final List<String> members = (List<String>) req.get(GraphDACParams.members.name());
        if (!manager.validateRequired(setId, members)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            try {
                final ExecutionContext ec = manager.getContext().dispatcher();
                Future<Node> setFuture = getNodeObject(req, ec, setId);
                OnComplete<Node> getSetObject = new OnComplete<Node>() {
                    @Override
                    public void onComplete(Throwable arg0, Node set) throws Throwable {
                        if (null != arg0 || null == set) {
                            manager.ERROR(arg0, getParent());
                        } else {
                            Map<String, Object> metadata = set.getMetadata();
                            if (null == metadata) {
                                manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(), "Invalid Set",
                                        ResponseCode.CLIENT_ERROR, getParent());
                            } else {
                                String type = (String) metadata.get(SET_TYPE_KEY);
                                if (StringUtils.equalsIgnoreCase(SET_TYPES.CRITERIA_SET.name(), type)) {
                                    manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(),
                                            "Member cannot be added to criteria sets", ResponseCode.CLIENT_ERROR, getParent());
                                } else {
                                    Future<Boolean> nodeFuture = checkMemberNodes(req, members, ec);
                                    nodeFuture.onComplete(new OnComplete<Boolean>() {
                                        public void onComplete(Throwable arg0, Boolean member) throws Throwable {
                                            if (null != arg0 || null == member || !member) {
                                                manager.ERROR(arg0, getParent());
                                            } else {
                                                addMembersToSet(req, setId, members);
                                            }
                                        };
                                    }, ec);
                                }
                            }
                        }
                    }
                };
                setFuture.onComplete(getSetObject, manager.getContext().dispatcher());
            } catch (Exception e) {
                manager.handleException(e, getParent());
            }
        }
    }

    @Override
    public void removeMember(final Request req) {
        try {
            final String setId = (String) req.get(GraphDACParams.collection_id.name());
            final String memberId = (String) req.get(GraphDACParams.member_id.name());
            if (!manager.validateRequired(setId, memberId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_REMOVE_SET_MEMBER_MISSING_REQ_PARAMS.name(),
                        "Required parameters are missing...");
            } else {
                final ExecutionContext ec = manager.getContext().dispatcher();
                Future<Node> setFuture = getNodeObject(req, ec, setId);
                OnComplete<Node> getSetObject = new OnComplete<Node>() {
                    @Override
                    public void onComplete(Throwable arg0, Node set) throws Throwable {
                        if (null != arg0 || null == set) {
                            manager.ERROR(arg0, getParent());
                        } else {
                            Map<String, Object> metadata = set.getMetadata();
                            if (null == metadata) {
                                manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(), "Invalid Set",
                                        ResponseCode.CLIENT_ERROR, getParent());
                            } else {
                                String type = (String) metadata.get(SET_TYPE_KEY);
                                if (StringUtils.equalsIgnoreCase(SET_TYPES.CRITERIA_SET.name(), type)) {
                                    manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(),
                                            "Member cannot be removed from criteria sets", ResponseCode.CLIENT_ERROR, getParent());
                                } else {
                                    ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                                    Request request = new Request(req);
                                    request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                                    request.setOperation("removeSetMember");
                                    request.put(GraphDACParams.set_id.name(), setId);
                                    request.put(GraphDACParams.member_id.name(), memberId);
                                    Future<Object> response = Patterns.ask(cacheRouter, request, timeout);

                                    ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                                    Request dacRequest = new Request(req);
                                    dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
                                    dacRequest.setOperation("deleteRelation");
                                    dacRequest.put(GraphDACParams.start_node_id.name(), setId);
                                    dacRequest.put(GraphDACParams.relation_type.name(),
                                            new String(RelationTypes.SET_MEMBERSHIP.relationName()));
                                    dacRequest.put(GraphDACParams.end_node_id.name(), memberId);
                                    dacRouter.tell(dacRequest, manager.getSelf());
                                    manager.returnResponse(response, getParent());
                                }
                            }
                        }
                    }
                };
                setFuture.onComplete(getSetObject, manager.getContext().dispatcher());
            }
        } catch (Exception e) {
            manager.handleException(e, getParent());
        }
    }

    @Override
    public void getMembers(Request req) {
        try {
            String setId = (String) req.get(GraphDACParams.collection_id.name());
            if (!manager.validateRequired(setId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_GET_SET_MEMBERS_INVALID_SET_ID.name(),
                        "Required parameters are missing...");
            } else {
                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                Request request = new Request(req);
                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                request.setOperation("getSetMembers");
                request.put(GraphDACParams.set_id.name(), setId);
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
            String setId = (String) req.get(GraphDACParams.collection_id.name());
            String memberId = (String) req.get(GraphDACParams.member_id.name());
            if (!manager.validateRequired(setId, memberId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IS_SET_MEMBER_INVALID_SET_ID.name(),
                        "Required parameters are missing...");
            } else {
                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                Request request = new Request(req);
                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                request.setOperation("isSetMember");
                request.put(GraphDACParams.set_id.name(), setId);
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
            String setId = (String) req.get(GraphDACParams.collection_id.name());
            if (!manager.validateRequired(setId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_DROP_SET_INVALID_SET_ID.name(),
                        "Required parameters are missing...");
            } else {
                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                Request request = new Request(req);
                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                request.setOperation("dropSet");
                request.put(GraphDACParams.set_id.name(), setId);
                Future<Object> response = Patterns.ask(cacheRouter, request, timeout);

                ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                Request dacRequest = new Request(req);
                dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
                dacRequest.setOperation("deleteCollection");
                dacRequest.put(GraphDACParams.collection_id.name(), setId);
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
            String setId = (String) req.get(GraphDACParams.collection_id.name());
            if (!manager.validateRequired(setId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_GET_CARDINALITY_MISSING_REQ_PARAMS.name(),
                        "Required parameters are missing...");
            } else {
                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                Request request = new Request(req);
                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                request.setOperation("getSetCardinality");
                request.put(GraphDACParams.set_id.name(), setId);
                Future<Object> response = Patterns.ask(cacheRouter, request, timeout);
                manager.returnResponse(response, getParent());
            }
        } catch (Exception e) {
            manager.handleException(e, getParent());
        }
    }

    @Override
    public String getSystemNodeType() {
        return SystemNodeTypes.SET.name();
    }

    public String getSetCriteria() {
        return setCriteria;
    }

    public void setCriteria(SearchCriteria criteria) {
        if (null != criteria) {
            if (StringUtils.isBlank(criteria.getObjectType())) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SET_CRITERIA_INVALID_OBJ_TYPE.name(),
                        "Object Type is mandatory for Set criteria");
            }
            List<String> fields = new ArrayList<String>();
            fields.add(SystemProperties.IL_UNIQUE_ID.name());
            criteria.setFields(fields);
            criteria.setNodeType(SystemNodeTypes.DATA_NODE.name());
            this.criteria = criteria;
            try {
                this.setCriteria = mapper.writeValueAsString(criteria);
            } catch (Exception e) {
            }
        } else {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SET_CRITERIA_INVALID.name(), "Set Criteria is null");
        }
    }

    @SuppressWarnings("unchecked")
    private void createCriteriaSet(final Request req) {
        try {
            final ExecutionContext ec = manager.getContext().dispatcher();
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            final Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            request.setOperation("searchNodes");
            request.put(GraphDACParams.search_criteria.name(), this.criteria);
            Future<Object> dacFuture = Patterns.ask(dacRouter, request, timeout);
            dacFuture.onComplete(new OnComplete<Object>() {
                @Override
                public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                    boolean valid = manager.checkResponseObject(arg0, arg1, getParent(),
                            GraphEngineErrorCodes.ERR_GRAPH_CREATE_SET_UNKNOWN_ERROR.name(), "Error searching nodes");
                    if (valid) {
                        Response res = (Response) arg1;
                        List<Node> nodes = (List<Node>) res.get(GraphDACParams.node_list.name());
                        List<String> memberIds = new ArrayList<String>();
                        if (null != nodes && !nodes.isEmpty()) {
                            for (Node node : nodes) {
                                memberIds.add(node.getIdentifier());
                            }
                        }
                        setMemberIds(memberIds);
                        createSetNode(req, ec);
                        // TODO: create/update metadata, relation and value
                        // nodes using criteria
                    }
                }
            }, ec);
        } catch (Exception e) {
            manager.ERROR(e, getParent());
        }
    }

    private void createSet(final Request req) {
        try {
            final ExecutionContext ec = manager.getContext().dispatcher();
            if (null != memberIds && memberIds.size() > 0) {
                Future<Boolean> validMembers = checkMemberNodes(req, memberIds, ec);
                validMembers.onComplete(new OnComplete<Boolean>() {
                    @Override
                    public void onComplete(Throwable arg0, Boolean valid) throws Throwable {
//                        boolean valid = manager.checkResponseObject(arg0, arg1, getParent(),
//                                GraphEngineErrorCodes.ERR_GRAPH_CREATE_SET_INVALID_MEMBER_IDS.name(), "Member Ids are invalid");
                        if (valid) {
                            createSetNode(req, ec);
                        } else {
                            manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_CREATE_SET_INVALID_MEMBER_IDS.name(), "Member Ids are invalid", ResponseCode.CLIENT_ERROR, getParent());
                        }
                    }
                }, ec);
            } else {
                createSetNode(req, ec);
            }
        } catch (Exception e) {
            manager.ERROR(e, getParent());
        }
    }

    private void createSetNode(final Request req, final ExecutionContext ec) {
        final ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        Request request = new Request(req);
        request.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
        request.setOperation("addNode");
        request.put(GraphDACParams.node.name(), toNode());
        Future<Object> dacFuture = Patterns.ask(dacRouter, request, timeout);
        dacFuture.onComplete(new OnComplete<Object>() {
            @Override
            public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                boolean valid = manager.checkResponseObject(arg0, arg1, getParent(),
                        GraphEngineErrorCodes.ERR_GRAPH_CREATE_SET_UNKNOWN_ERROR.name(), "Failed to create Set node");
                if (valid) {
                    Response res = (Response) arg1;
                    String setId = (String) res.get(GraphDACParams.node_id.name());
                    ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                    Request request = new Request(req);
                    request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                    request.setOperation("createSet");
                    request.put(GraphDACParams.set_id.name(), setId);
                    request.put(GraphDACParams.members.name(), memberIds);
                    Future<Object> response = Patterns.ask(cacheRouter, request, timeout);

                    if (null != memberIds && memberIds.size() > 0) {
                        Request dacRequest = new Request(req);
                        dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
                        dacRequest.setOperation("createCollection");
                        dacRequest.put(GraphDACParams.collection_id.name(), setId);
                        dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SET_MEMBERSHIP.relationName());
                        dacRequest.put(GraphDACParams.members.name(), memberIds);
                        dacRouter.tell(dacRequest, manager.getSelf());
                    }
                    manager.returnResponse(response, getParent());
                }
            }
        }, ec);
    }

    private void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    @Override
    public String getFunctionalObjectType() {
        return this.setObjectType;
    }

    public String getSetType() {
        return this.setType;
    }

    private void addMemberToSet(Request req, String setId, String memberId) {
        ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
        Request request = new Request(req);
        request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
        request.setOperation("addSetMember");
        request.put(GraphDACParams.set_id.name(), setId);
        request.put(GraphDACParams.member_id.name(), memberId);
        Future<Object> response = Patterns.ask(cacheRouter, request, timeout);

        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        Request dacRequest = new Request(req);
        dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
        dacRequest.setOperation("addRelation");
        dacRequest.put(GraphDACParams.start_node_id.name(), setId);
        dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SET_MEMBERSHIP.relationName());
        dacRequest.put(GraphDACParams.end_node_id.name(), memberId);
        dacRouter.tell(dacRequest, manager.getSelf());

        manager.returnResponse(response, getParent());
    }

    private void addMembersToSet(Request req, String setId, List<String> memberIds) {
        ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
        Request request = new Request(req);
        request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
        request.setOperation("addSetMembers");
        request.put(GraphDACParams.set_id.name(), setId);
        request.put(GraphDACParams.members.name(), memberIds);
        Future<Object> response = Patterns.ask(cacheRouter, request, timeout);

        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        for (String memberId : memberIds) {
            Request dacRequest = new Request(req);
            dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
            dacRequest.setOperation("addRelation");
            dacRequest.put(GraphDACParams.start_node_id.name(), setId);
            dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SET_MEMBERSHIP.relationName());
            dacRequest.put(GraphDACParams.end_node_id.name(), memberId);
            dacRouter.tell(dacRequest, manager.getSelf());
        }
        manager.returnResponse(response, getParent());
    }

}
