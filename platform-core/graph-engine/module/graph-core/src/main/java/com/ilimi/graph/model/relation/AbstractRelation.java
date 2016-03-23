package com.ilimi.graph.model.relation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.Promise;
import akka.actor.ActorRef;
import akka.dispatch.Mapper;
import akka.dispatch.OnComplete;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.actor.GraphCacheActorPoolMgr;
import com.ilimi.graph.cache.actor.GraphCacheManagers;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.exception.GraphRelationErrorCodes;
import com.ilimi.graph.model.AbstractDomainObject;
import com.ilimi.graph.model.IRelation;

public abstract class AbstractRelation extends AbstractDomainObject implements IRelation {

    protected String startNodeId;
    protected String endNodeId;
    protected Map<String, Object> metadata;

    protected AbstractRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId,
            Map<String, Object> metadata) {
        this(manager, graphId, startNodeId, endNodeId);
        this.metadata = metadata;
    }

    protected AbstractRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId) {
        super(manager, graphId);
        if (null == manager || StringUtils.isBlank(graphId) || StringUtils.isBlank(startNodeId)
                || StringUtils.isBlank(endNodeId)) {
            throw new ClientException(GraphRelationErrorCodes.ERR_INVALID_RELATION.name(), "Invalid Relation");
        }
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
    }

    public void create(final Request req) {
        try {
            Future<Map<String, List<String>>> aggregate = validateRelation(req);
            aggregate.onSuccess(new OnSuccess<Map<String, List<String>>>() {
                @Override
                public void onSuccess(Map<String, List<String>> messageMap) throws Throwable {
                    List<String> errMessages = getErrorMessages(messageMap);
                    if (null == errMessages || errMessages.isEmpty()) {
                        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                        Request request = new Request(req);
                        request.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
                        request.setOperation("addRelation");
                        request.put(GraphDACParams.start_node_id.name(), getStartNodeId());
                        request.put(GraphDACParams.relation_type.name(), getRelationType());
                        request.put(GraphDACParams.end_node_id.name(), getEndNodeId());
                        request.put(GraphDACParams.metadata.name(), getMetadata());
                        Future<Object> response = Patterns.ask(dacRouter, request, timeout);
                        manager.returnResponse(response, getParent());
                    } else {
                        manager.OK(GraphDACParams.messages.name(), errMessages, getParent());
                    }
                }
            }, manager.getContext().dispatcher());
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_CREATE.name(), e.getMessage(), e);
        }
    }

    public Future<String> createRelation(final Request req) {
        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        Request request = new Request(req);
        request.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
        request.setOperation("addRelation");
        request.put(GraphDACParams.start_node_id.name(), getStartNodeId());
        request.put(GraphDACParams.relation_type.name(), getRelationType());
        request.put(GraphDACParams.end_node_id.name(), getEndNodeId());
        request.put(GraphDACParams.metadata.name(), getMetadata());
        Future<Object> response = Patterns.ask(dacRouter, request, timeout);
        Future<String> message = response.map(new Mapper<Object, String>() {
            @Override
            public String apply(Object parameter) {
                if (parameter instanceof Response) {
                    Response res = (Response) parameter;
                    if (manager.checkError(res)) {
                        return manager.getErrorMessage(res);
                    }
                } else {
                    return "Error creating relation: " + getStartNodeId() + " - " + getRelationType() + " - "
                            + getEndNodeId();
                }
                return null;
            }
        }, manager.getContext().dispatcher());
        return message;
    }

    @Override
    public void delete(Request req) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
            request.setOperation("deleteRelation");
            request.copyRequestValueObjects(req.getRequest());
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            manager.returnResponse(response, getParent());
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_DELETE.name(), e.getMessage(), e);
        }
    }

    @Override
    public Future<String> deleteRelation(Request req) {
        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        Request request = new Request(req);
        request.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
        request.setOperation("deleteRelation");
        request.put(GraphDACParams.start_node_id.name(), getStartNodeId());
        request.put(GraphDACParams.relation_type.name(), getRelationType());
        request.put(GraphDACParams.end_node_id.name(), getEndNodeId());
        Future<Object> response = Patterns.ask(dacRouter, request, timeout);
        Future<String> message = response.map(new Mapper<Object, String>() {
            @Override
            public String apply(Object parameter) {
                if (parameter instanceof Response) {
                    Response res = (Response) parameter;
                    if (manager.checkError(res)) {
                        return manager.getErrorMessage(res);
                    }
                } else {
                    return "Error deleting relation: " + getStartNodeId() + " - " + getRelationType() + " - "
                            + getEndNodeId();
                }
                return null;
            }
        }, manager.getContext().dispatcher());
        return message;
    }

    @Override
    public void validate(final Request request) {
        try {
            Future<Map<String, List<String>>> aggregate = validateRelation(request);
            aggregate.onSuccess(new OnSuccess<Map<String, List<String>>>() {
                @Override
                public void onSuccess(Map<String, List<String>> messageMap) throws Throwable {
                    List<String> errMessages = getErrorMessages(messageMap);
                    if (null == errMessages || errMessages.isEmpty()) {
                        manager.OK(getParent());
                    } else {
                        manager.OK(GraphDACParams.messages.name(), errMessages, getParent());
                    }
                }
            }, manager.getContext().dispatcher());
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

    public Relation toRelation() {
        Relation relation = new Relation(this.startNodeId, getRelationType(), this.endNodeId);
        return relation;
    }

    public String getStartNodeId() {
        return this.startNodeId;
    }

    public String getEndNodeId() {
        return this.endNodeId;
    }

    public Map<String, Object> getMetadata() {
        return this.metadata;
    }

    public boolean isType(String relationType) {
        return StringUtils.equalsIgnoreCase(getRelationType(), relationType);
    }

    public void getProperty(Request req) {
        try {
            String key = (String) req.get(GraphDACParams.property_key.name());
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            request.setOperation("getRelationProperty");
            request.put(GraphDACParams.start_node_id.name(), this.startNodeId);
            request.put(GraphDACParams.relation_type.name(), getRelationType());
            request.put(GraphDACParams.end_node_id.name(), this.endNodeId);
            request.put(GraphDACParams.property_key.name(), key);
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            manager.returnResponse(response, getParent());
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_GET_PROPERTY.name(), e.getMessage(), e);
        }
    }

    public void removeProperty(Request req) {
        throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_UNSUPPORTED_OPERATION.name(),
                "Remove Property is not supported on relations");
    }

    public void setProperty(Request req) {
        throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_UNSUPPORTED_OPERATION.name(),
                "Set Property is not supported on relations");
    }

    protected Future<Map<String, List<String>>> getMessageMap(Future<Iterable<String>> aggregate, ExecutionContext ec) {
        Future<Map<String, List<String>>> messageMap = aggregate
                .map(new Mapper<Iterable<String>, Map<String, List<String>>>() {
                    @Override
                    public Map<String, List<String>> apply(Iterable<String> parameter) {
                        Map<String, List<String>> map = new HashMap<String, List<String>>();
                        List<String> messages = new ArrayList<String>();
                        if (null != parameter) {
                            for (String msg : parameter) {
                                if (StringUtils.isNotBlank(msg))
                                    messages.add(msg);
                            }
                        }
                        map.put(getStartNodeId(), messages);
                        return map;
                    }
                }, ec);
        return messageMap;
    }

    protected Future<Node> getNode(Request request, String nodeId) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request newReq = new Request(request);
            newReq.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            newReq.setOperation("getNodeByUniqueId");
            newReq.put(GraphDACParams.node_id.name(), nodeId);
            Future<Object> response = Patterns.ask(dacRouter, newReq, timeout);
            Future<Node> node = response.map(new Mapper<Object, Node>() {
                @Override
                public Node apply(Object parameter) {
                    if (parameter instanceof Response) {
                        Response res = (Response) parameter;
                        Node node = (Node) res.get(GraphDACParams.node.name());
                        return node;
                    } else {
                        return null;
                    }
                }
            }, manager.getContext().dispatcher());
            return node;
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

    protected Future<String> checkCycle(Request req) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            request.setOperation("checkCyclicLoop");
            request.put(GraphDACParams.start_node_id.name(), this.endNodeId);
            request.put(GraphDACParams.relation_type.name(), getRelationType());
            request.put(GraphDACParams.end_node_id.name(), this.startNodeId);
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            Future<String> message = response.map(new Mapper<Object, String>() {
                @Override
                public String apply(Object parameter) {
                    if (parameter instanceof Response) {
                        Response res = (Response) parameter;
                        if (manager.checkError(res)) {
                            return manager.getErrorMessage(res);
                        } else {
                            Boolean loop = (Boolean) res.get(GraphDACParams.loop.name());
                            if (null != loop && loop.booleanValue()) {
                                String msg = (String) res.get(GraphDACParams.message.name());
                                return msg;
                            } else {
                                return null;
                            }
                        }
                    } else {
                        return "UnKnown error";
                    }
                }
            }, manager.getContext().dispatcher());
            return message;
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

    protected Future<String> getNodeTypeFuture(Future<Node> node, final String[] nodeTypes, final ExecutionContext ec) {
        Future<String> endNodeMsg = node.map(new Mapper<Node, String>() {
            @Override
            public String apply(Node node) {
                if (null == node) {
                    return "Node not found";
                } else {
                    if (Arrays.asList(nodeTypes).contains(node.getNodeType())) {
                        return null;
                    } else {
                        return "Node " + node.getIdentifier() + " is not a " + nodeTypes;
                    }
                }
            }
        }, ec);
        return endNodeMsg;
    }

    protected Future<String> getNodeTypeFuture(Future<Node> nodeFuture, final ExecutionContext ec) {
        Future<String> nodeType = nodeFuture.map(new Mapper<Node, String>() {
            @Override
            public String apply(Node parameter) {
                if (null != parameter)
                    return parameter.getNodeType();
                else
                    return null;
            }
        }, ec);
        return nodeType;
    }

    protected Future<String> getObjectTypeFuture(Future<Node> nodeFuture, final ExecutionContext ec) {
        Future<String> objectType = nodeFuture.map(new Mapper<Node, String>() {
            @Override
            public String apply(Node parameter) {
                if (null != parameter)
                    return parameter.getObjectType();
                else
                    return null;
            }
        }, ec);
        return objectType;
    }

    protected void compareFutures(Future<String> future1, final Future<String> future2, final Promise<String> promise, final String property, 
            final ExecutionContext ec) {
        future1.onSuccess(new OnSuccess<String>() {
            @Override
            public void onSuccess(final String val1) throws Throwable {
                if (StringUtils.isNotBlank(val1)) {
                    future2.onSuccess(new OnSuccess<String>() {
                        @Override
                        public void onSuccess(final String val2) throws Throwable {
                            if (StringUtils.isNotBlank(val2)) {
                                if (StringUtils.equals(val1, val2)) {
                                    promise.success(null);
                                } else {
                                    promise.success(property + " values do not match");
                                }
                            } else {
                                promise.success(property + " cannot be empty");
                            }
                        }
                    }, ec);
                } else {
                    promise.success(property + " cannot be empty");
                }
            }
        }, ec);
    }

    @SuppressWarnings("unchecked")
    protected void validateObjectTypes(Future<String> objectType, final Future<String> endNodeObjectType,
            final Request request, final Promise<String> objectTypePromise, final ExecutionContext ec) {
        objectType.onSuccess(new OnSuccess<String>() {
            @Override
            public void onSuccess(final String type) throws Throwable {
                if (StringUtils.isNotBlank(type)) {
                    endNodeObjectType.onSuccess(new OnSuccess<String>() {
                        @Override
                        public void onSuccess(final String endNodeType) throws Throwable {
                            if (StringUtils.isNotBlank(endNodeType)) {
                                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                                Request cacheReq = new Request(request);
                                cacheReq.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
                                cacheReq.setOperation("getOutRelationObjectTypes");
                                cacheReq.put(GraphDACParams.object_type.name(), type);
                                Future<Object> response = Patterns.ask(cacheRouter, cacheReq, timeout);
                                response.onComplete(new OnComplete<Object>() {
                                    @Override
                                    public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                                        if (null != arg0) {
                                            objectTypePromise.success(null);
                                        } else {
                                            if (arg1 instanceof Response) {
                                                Response res = (Response) arg1;
                                                List<String> outRelations = (List<String>) res
                                                        .get(GraphDACParams.metadata.name());
                                                boolean found = false;
                                                if (null != outRelations && !outRelations.isEmpty()) {
                                                    for (String outRel : outRelations) {
                                                        if (StringUtils.equals(getRelationType() + ":" + endNodeType,
                                                                outRel)) {
                                                            found = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                                if (!found) {
                                                    objectTypePromise
                                                            .success(getRelationType() + " is not allowed between "
                                                                    + type + " and " + endNodeType);
                                                } else {
                                                    objectTypePromise.success(null);
                                                }
                                            } else {
                                                objectTypePromise.success(null);
                                            }
                                        }
                                    }
                                }, ec);
                            } else {
                                objectTypePromise.success(null);
                            }
                        }
                    }, ec);
                } else {
                    objectTypePromise.success(null);
                }
            }
        }, ec);
    }

}
