package com.ilimi.graph.model.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.exception.ResponseCode;
import com.ilimi.graph.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.model.AbstractDomainObject;
import com.ilimi.graph.model.INode;

public abstract class AbstractNode extends AbstractDomainObject implements INode {

    private String nodeId;
    protected Map<String, Object> metadata;

    protected AbstractNode(BaseGraphManager manager, String graphId, String nodeId, Map<String, Object> metadata) {
        super(manager, graphId);
        if (null == manager || StringUtils.isBlank(graphId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_NODE.name(), "Invalid Node");
        }
        checkMetadata(metadata);
        this.nodeId = nodeId;
        this.metadata = metadata;
    }

    @Override
    public void create(final Request req) {
        try {
            checkMetadata(metadata);
            Future<Map<String, List<String>>> aggregate = validateNode(req);
            aggregate.onSuccess(new OnSuccess<Map<String, List<String>>>() {
                @Override
                public void onSuccess(Map<String, List<String>> messages) throws Throwable {
                    List<StringValue> errMessages = getErrorMessages(messages);
                    if (null == errMessages || errMessages.isEmpty()) {
                        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                        Request request = new Request(req);
                        request.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
                        request.setOperation("addNode");
                        request.put(GraphDACParams.NODE.name(), toNode());
                        Future<Object> response = Patterns.ask(dacRouter, request, timeout);
                        manager.returnResponse(response, getParent());
                    } else {
                        manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_ERROR.name(), "Node validation failed",
                                ResponseCode.CLIENT_ERROR, GraphDACParams.MESSAGES.name(),
                                new BaseValueObjectList<StringValue>(errMessages), getParent());
                    }
                }
            }, manager.getContext().dispatcher());
        } catch (Exception e) {
            manager.ERROR(e, getParent());
        }
    }

    @Override
    public void getProperty(Request req) {
        final StringValue key = (StringValue) req.get(GraphDACParams.PROPERTY_KEY.name());
        if (!manager.validateRequired(key)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_GET_NODE_ERROR.name(),
                    "Get Property: Required Properties are missing");
        } else {
            try {
                ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                Request request = new Request(req);
                request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
                request.setOperation("getNodeProperty");
                request.put(GraphDACParams.NODE_ID.name(), new StringValue(this.nodeId));
                request.put(GraphDACParams.PROPERTY_KEY.name(), key);
                Future<Object> response = Patterns.ask(dacRouter, request, timeout);
                manager.returnResponse(response, getParent());
            } catch (Exception e) {
                manager.ERROR(e, getParent());
            }
        }
    }

    @Override
    public void removeProperty(Request req) {
        throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_UNSUPPORTED_OPERATION.name(),
                "Remove Property is not supported on this node");
    }

    @Override
    public void setProperty(Request req) {
        throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_UNSUPPORTED_OPERATION.name(),
                "Set Property is not supported on this node");
    }

    @Override
    public void updateMetadata(Request req) {
        throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_UNSUPPORTED_OPERATION.name(),
                "Update Metadata is not supported on this node");
    }

    @Override
    public void delete(Request req) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
            request.setOperation("deleteNode");
            request.put(GraphDACParams.NODE_ID.name(), new StringValue(getNodeId()));
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            manager.returnResponse(response, getParent());
        } catch (Exception e) {
            manager.ERROR(e, getParent());
        }
    }

    @Override
    public Node toNode() {
        Node node = new Node(this.nodeId, getSystemNodeType(), getFunctionalObjectType());
        node.setMetadata(this.metadata);
        return node;
    }

    @Override
    public Future<Map<String, List<String>>> validateNode(Request request) {
        Future<List<String>> metadataValidation = Futures.successful(null);
        return getMessageMap(metadataValidation, manager.getContext().dispatcher());
    }

    public String getNodeId() {
        return this.nodeId;
    }

    protected Future<Map<String, List<String>>> getMessageMap(Future<List<String>> aggregate, ExecutionContext ec) {
        Future<Map<String, List<String>>> messageMap = aggregate.map(new Mapper<List<String>, Map<String, List<String>>>() {
            @Override
            public Map<String, List<String>> apply(List<String> parameter) {
                Map<String, List<String>> map = new HashMap<String, List<String>>();
                List<String> messages = new ArrayList<String>();
                if (null != parameter && !parameter.isEmpty()) {
                    messages.addAll(parameter);
                }
                map.put(getNodeId(), messages);
                return map;
            }
        }, ec);
        return messageMap;
    }

    protected List<StringValue> getErrorMessages(Iterable<List<String>> messages) {
        List<StringValue> errMessages = new ArrayList<StringValue>();
        if (null != messages) {
            for (List<String> list : messages) {
                if (null != list && !list.isEmpty()) {
                    for (String msg : list) {
                        if (StringUtils.isNotBlank(msg)) {
                            errMessages.add(new StringValue(msg));
                        }
                    }
                }
            }
        }
        return errMessages;
    }

    protected void checkMetadata(Map<String, Object> metadata) {
        if (null != metadata && metadata.size() > 0) {
            for (Entry<String, Object> entry : metadata.entrySet()) {
                checkMetadata(entry.getKey(), entry.getValue());
            }
        }
    }

    protected void checkMetadata(String key, Object value) {
        if (SystemProperties.isSystemProperty(key)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_PROPERTY.name(), key + " is a reserved system property");
        }
        if (null != value) {
            if (!(value instanceof String) && !(value instanceof String[]) && !(value instanceof Double) && !(value instanceof double[])
                    && !(value instanceof Float) && !(value instanceof float[]) && !(value instanceof Long) && !(value instanceof long[])
                    && !(value instanceof Integer) && !(value instanceof int[]) && !(value instanceof Boolean) && !(value instanceof boolean[])) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_PROPERTY.name(), "Invalid data type for the property: " + key);
            }
        }
    }

    protected String[] convertListToArray(List<String> list) {
        if (null != list && !list.isEmpty()) {
            String[] array = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = list.get(i);
            }
            return array;
        }
        return null;
    }
}
