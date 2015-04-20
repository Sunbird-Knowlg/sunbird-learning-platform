package com.ilimi.graph.model.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;

import com.ilimi.graph.cache.actor.GraphCacheActorPoolMgr;
import com.ilimi.graph.cache.actor.GraphCacheManagers;
import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.BaseValueObjectMap;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.exception.ResponseCode;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;

public class Set extends AbstractCollection {

    public static final String SET_OBJECT_TYPE_KEY = "SET_OBJECT_TYPE_KEY";
    public static final String SET_CRITERIA_KEY = "SET_CRITERIA_KEY";
    private SetCriteria criteria;
    private List<StringValue> memberIds;
    private List<StringValue> indexFields;
    private ObjectMapper mapper = new ObjectMapper();

    public Set(BaseGraphManager manager, String graphId, String id) {
        super(manager, graphId, id);
    }

    public Set(BaseGraphManager manager, String graphId, String id, SetCriteria criteria) {
        super(manager, graphId, id);
        setCriteria(criteria);
    }

    public Set(BaseGraphManager manager, String graphId, String id, List<StringValue> memberIds) {
        super(manager, graphId, id);
        this.memberIds = memberIds;
    }

    @Override
    public Node toNode() {
        Node node = new Node(getNodeId(), getSystemNodeType(), getFunctionalObjectType());
        if (null != criteria) {
            Map<String, Object> metadata = new HashMap<String, Object>();
            if (StringUtils.isNotBlank(criteria.getObjectType()))
                metadata.put(SET_OBJECT_TYPE_KEY, criteria.getObjectType());
            if (null != criteria.getCriteria() && !criteria.getCriteria().isEmpty()) {
                try {
                    metadata.put(SET_CRITERIA_KEY, mapper.writeValueAsString(criteria.getCriteria()));
                } catch (Exception e) {
                }
            }
            if (!metadata.isEmpty()) {
                node.setMetadata(metadata);
            }
        }
        return node;
    }

    @Override
    public void create(Request request) {
        if (null != this.criteria) {
            createCriteriaSet(request);
        } else {
            createSet(request);
        }
    }

    @Override
    public void addMember(final Request req) {
        final StringValue setId = (StringValue) req.get(GraphDACParams.COLLECTION_ID.name());
        final StringValue memberId = (StringValue) req.get(GraphDACParams.MEMBER_ID.name());
        if (!manager.validateRequired(setId, memberId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER.name(), "Required parameters are missing...");
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
                            if (null != metadata && null != metadata.get(SET_OBJECT_TYPE_KEY)) {
                                manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER.name(),
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
        final StringValue setId = (StringValue) req.get(GraphDACParams.COLLECTION_ID.name());
        final BaseValueObjectList<StringValue> members = (BaseValueObjectList<StringValue>) req.get(GraphDACParams.MEMBERS.name());
        if (!manager.validateRequired(setId, members)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER.name(), "Required parameters are missing...");
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
                            if (null != metadata && null != metadata.get(SET_OBJECT_TYPE_KEY)) {
                                manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER.name(),
                                        "Member cannot be added to criteria sets", ResponseCode.CLIENT_ERROR, getParent());
                            } else {
                                Future<Boolean> nodeFuture = checkMemberNodes(req, members.getValueObjectList(), ec);
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
                };
                setFuture.onComplete(getSetObject, manager.getContext().dispatcher());
            } catch (Exception e) {
                manager.handleException(e, getParent());
            }
        }
    }

    @Override
    public void removeMember(Request req) {
        try {
            StringValue setId = (StringValue) req.get(GraphDACParams.COLLECTION_ID.name());
            StringValue memberId = (StringValue) req.get(GraphDACParams.MEMBER_ID.name());
            if (!manager.validateRequired(setId, memberId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_REMOVE_SET_MEMBER.name(), "Required parameters are missing...");
            } else {
                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                Request request = new Request(req);
                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                request.setOperation("removeSetMember");
                request.put(GraphDACParams.SET_ID.name(), setId);
                request.put(GraphDACParams.MEMBER_ID.name(), memberId);
                Future<Object> response = Patterns.ask(cacheRouter, request, timeout);

                ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                Request dacRequest = new Request(req);
                dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
                dacRequest.setOperation("deleteRelation");
                dacRequest.put(GraphDACParams.START_NODE_ID.name(), setId);
                dacRequest.put(GraphDACParams.RELATION_TYPE.name(), new StringValue(RelationTypes.SET_MEMBERSHIP.name()));
                dacRequest.put(GraphDACParams.END_NODE_ID.name(), memberId);
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
            StringValue setId = (StringValue) req.get(GraphDACParams.COLLECTION_ID.name());
            if (!manager.validateRequired(setId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_GET_SET_MEMBERS.name(), "Required parameters are missing...");
            } else {
                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                Request request = new Request(req);
                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                request.setOperation("getSetMembers");
                request.put(GraphDACParams.SET_ID.name(), setId);
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
            StringValue setId = (StringValue) req.get(GraphDACParams.COLLECTION_ID.name());
            StringValue memberId = (StringValue) req.get(GraphDACParams.MEMBER_ID.name());
            if (!manager.validateRequired(setId, memberId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IS_SET_MEMBER.name(), "Required parameters are missing...");
            } else {
                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                Request request = new Request(req);
                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                request.setOperation("isSetMember");
                request.put(GraphDACParams.SET_ID.name(), setId);
                request.put(GraphDACParams.MEMBER_ID.name(), memberId);
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
            StringValue setId = (StringValue) req.get(GraphDACParams.COLLECTION_ID.name());
            if (!manager.validateRequired(setId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_DROP_SET.name(), "Required parameters are missing...");
            } else {
                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                Request request = new Request(req);
                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                request.setOperation("dropSet");
                request.put(GraphDACParams.SET_ID.name(), setId);
                Future<Object> response = Patterns.ask(cacheRouter, request, timeout);

                ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                Request dacRequest = new Request(req);
                dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
                dacRequest.setOperation("deleteCollection");
                dacRequest.put(GraphDACParams.COLLECTION_ID.name(), setId);
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
            StringValue setId = (StringValue) req.get(GraphDACParams.COLLECTION_ID.name());
            if (!manager.validateRequired(setId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_GET_CARDINALITY.name(),
                        "Required parameters are missing...");
            } else {
                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                Request request = new Request(req);
                request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                request.setOperation("getSetCardinality");
                request.put(GraphDACParams.SET_ID.name(), setId);
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

    public SetCriteria getCriteria() {
        return this.criteria;
    }

    public void setCriteria(SetCriteria criteria) {
        if (null != criteria) {
            if (StringUtils.isBlank(criteria.getObjectType())) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SET_CRITERIA.name(), "Object Type is mandatory for Set criteria");
            }
        }
        this.criteria = criteria;
    }

    @SuppressWarnings("unchecked")
    private void createCriteriaSet(final Request req) {
        try {
            final ExecutionContext ec = manager.getContext().dispatcher();
            ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
            final Request cacheReq = new Request(req);
            cacheReq.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
            cacheReq.setOperation("getIndexedMetadataFields");
            cacheReq.put(GraphDACParams.OBJECT_TYPE.name(), new StringValue(criteria.getObjectType()));
            Future<Object> cacheFuture = Patterns.ask(cacheRouter, cacheReq, timeout);

            OnComplete<Object> getIndexFields = new OnComplete<Object>() {
                @Override
                public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                    boolean resValid = manager.checkResponseObject(arg0, arg1, getParent(),
                            GraphEngineErrorCodes.ERR_GRAPH_CREATE_SET.name(), "Object Type not found");
                    if (resValid) {
                        Response res = (Response) arg1;
                        BaseValueObjectList<StringValue> indexFields = (BaseValueObjectList<StringValue>) res
                                .get(GraphDACParams.OBJECT_TYPE.name());
                        if (null != criteria.getCriteria() && !criteria.getCriteria().isEmpty()) {
                            if (null == indexFields || null == indexFields.getValueObjectList()
                                    || indexFields.getValueObjectList().isEmpty()) {
                                manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_CREATE_SET.name(),
                                        "Set criteria should contain only indexable metadata fields", res.getResponseCode(), getParent());
                            } else {
                                setIndexFields(indexFields.getValueObjectList());
                                List<String> indexableFields = new ArrayList<String>();
                                for (StringValue val : indexFields.getValueObjectList()) {
                                    indexableFields.add(val.getId());
                                }
                                boolean valid = true;
                                SearchCriteria sc = new SearchCriteria();
                                sc.add(SearchConditions.eq(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DATA_NODE.name()))
                                        .add(SearchConditions.eq(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), criteria.getObjectType()));
                                for (Entry<String, Object> entry : criteria.getCriteria().entrySet()) {
                                    if (indexableFields.contains(entry.getKey())) {
                                        sc.add(SearchConditions.eq(entry.getKey(), entry.getValue()));
                                    } else {
                                        valid = false;
                                        break;
                                    }
                                }
                                if (!valid) {
                                    manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_CREATE_SET.name(),
                                            "Set criteria should contain only indexable metadata fields", ResponseCode.CLIENT_ERROR,
                                            getParent());
                                } else {
                                    getMembersByCriteria(req, sc, ec);
                                }
                            }
                        } else {
                            createSetNode(req, ec);
                        }
                    }
                }
            };
            cacheFuture.onComplete(getIndexFields, ec);
        } catch (Exception e) {
            manager.ERROR(e, getParent());
        }
    }

    @SuppressWarnings("unchecked")
    private void getMembersByCriteria(final Request req, SearchCriteria sc, final ExecutionContext ec) {
        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        final Request request = new Request(req);
        request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
        request.setOperation("searchNodes");
        request.put(GraphDACParams.SEARCH_CRITERIA.name(), sc);
        Future<Object> dacFuture = Patterns.ask(dacRouter, request, timeout);
        dacFuture.onComplete(new OnComplete<Object>() {
            @Override
            public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                boolean valid = manager.checkResponseObject(arg0, arg1, getParent(), GraphEngineErrorCodes.ERR_GRAPH_CREATE_SET.name(),
                        "Error searching nodes");
                if (valid) {
                    Response res = (Response) arg1;
                    BaseValueObjectList<Node> nodes = (BaseValueObjectList<Node>) res.get(GraphDACParams.NODE_LIST.name());
                    List<StringValue> memberIds = new ArrayList<StringValue>();
                    if (null != nodes && null != nodes.getValueObjectList() && !nodes.getValueObjectList().isEmpty()) {
                        for (Node node : nodes.getValueObjectList()) {
                            memberIds.add(new StringValue(node.getIdentifier()));
                        }
                    }
                    setMemberIds(memberIds);
                    createSetNode(req, ec);
                }
            }
        }, ec);
    }

    private void createSet(final Request req) {
        try {
            final ExecutionContext ec = manager.getContext().dispatcher();
            if (null != memberIds && memberIds.size() > 0) {
                Future<Boolean> validMembers = checkMemberNodes(req, memberIds, ec);
                validMembers.onComplete(new OnComplete<Boolean>() {
                    @Override
                    public void onComplete(Throwable arg0, Boolean arg1) throws Throwable {
                        boolean valid = manager.checkResponseObject(arg0, arg1, getParent(),
                                GraphEngineErrorCodes.ERR_GRAPH_CREATE_SET.name(), "Member Ids are invalid");
                        if (valid) {
                            createSetNode(req, ec);
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
        request.put(GraphDACParams.NODE.name(), toNode());
        Future<Object> dacFuture = Patterns.ask(dacRouter, request, timeout);
        dacFuture.onComplete(new OnComplete<Object>() {
            @Override
            public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                boolean valid = manager.checkResponseObject(arg0, arg1, getParent(), GraphEngineErrorCodes.ERR_GRAPH_CREATE_SET.name(),
                        "Failed to create Set node");
                if (valid) {
                    Response res = (Response) arg1;
                    StringValue setId = (StringValue) res.get(GraphDACParams.NODE_ID.name());
                    ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                    Request request = new Request(req);
                    request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                    request.setOperation("createSet");
                    request.put(GraphDACParams.SET_ID.name(), setId);
                    request.put(GraphDACParams.MEMBERS.name(), new BaseValueObjectList<StringValue>(memberIds));
                    request.put(GraphDACParams.OBJECT_TYPE.name(), new StringValue(criteria.getObjectType()));
                    request.put(GraphDACParams.CRITERIA.name(), new BaseValueObjectMap<Object>(criteria.getCriteria()));
                    request.put(GraphDACParams.INDEXABLE_METADATA_KEY.name(), new BaseValueObjectList<StringValue>(indexFields));
                    Future<Object> response = Patterns.ask(cacheRouter, request, timeout);

                    if (null != memberIds && memberIds.size() > 0) {
                        Request dacRequest = new Request(req);
                        dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
                        dacRequest.setOperation("createCollection");
                        dacRequest.put(GraphDACParams.COLLECTION_ID.name(), setId);
                        dacRequest.put(GraphDACParams.RELATION_TYPE.name(), new StringValue(RelationTypes.SET_MEMBERSHIP.name()));
                        dacRequest.put(GraphDACParams.MEMBERS.name(), new BaseValueObjectList<StringValue>(memberIds));
                        dacRouter.tell(dacRequest, manager.getSelf());
                    }
                    manager.returnResponse(response, getParent());
                }
            }
        }, ec);
    }

    private void setMemberIds(List<StringValue> memberIds) {
        this.memberIds = memberIds;
    }

    private void setIndexFields(List<StringValue> indexFields) {
        this.indexFields = indexFields;
    }

    private void addMemberToSet(Request req, StringValue setId, StringValue memberId) {
        ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
        Request request = new Request(req);
        request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
        request.setOperation("addSetMember");
        request.put(GraphDACParams.SET_ID.name(), setId);
        request.put(GraphDACParams.MEMBER_ID.name(), memberId);
        Future<Object> response = Patterns.ask(cacheRouter, request, timeout);

        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        Request dacRequest = new Request(req);
        dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
        dacRequest.setOperation("addRelation");
        dacRequest.put(GraphDACParams.START_NODE_ID.name(), setId);
        dacRequest.put(GraphDACParams.RELATION_TYPE.name(), new StringValue(RelationTypes.SET_MEMBERSHIP.name()));
        dacRequest.put(GraphDACParams.END_NODE_ID.name(), memberId);
        dacRouter.tell(dacRequest, manager.getSelf());

        manager.returnResponse(response, getParent());
    }

    private void addMembersToSet(Request req, StringValue setId, BaseValueObjectList<StringValue> memberIds) {
        ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
        Request request = new Request(req);
        request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
        request.setOperation("addSetMembers");
        request.put(GraphDACParams.SET_ID.name(), setId);
        request.put(GraphDACParams.MEMBERS.name(), memberIds);
        Future<Object> response = Patterns.ask(cacheRouter, request, timeout);

        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        for (StringValue memberId : memberIds.getValueObjectList()) {
            Request dacRequest = new Request(req);
            dacRequest.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
            dacRequest.setOperation("addRelation");
            dacRequest.put(GraphDACParams.START_NODE_ID.name(), setId);
            dacRequest.put(GraphDACParams.RELATION_TYPE.name(), new StringValue(RelationTypes.SET_MEMBERSHIP.name()));
            dacRequest.put(GraphDACParams.END_NODE_ID.name(), memberId);
            dacRouter.tell(dacRequest, manager.getSelf());
        }
        manager.returnResponse(response, getParent());
    }

}
