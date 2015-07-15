package com.ilimi.graph.model.node;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.model.AbstractDomainObject;
import com.ilimi.graph.model.IRelation;
import com.ilimi.graph.model.relation.HasMetadataRelation;

public class MetadataNode extends AbstractDomainObject {

    private String nodeId;
    private String objectType;
    private String name;
    private static final String METADATA_NODE_NAME_KEY = "METADATA_PROPERTY_NAME";

    public MetadataNode(BaseGraphManager manager, String graphId, String objectType, String name) {
        super(manager, graphId);
        if (StringUtils.isBlank(objectType) || StringUtils.isBlank(name))
            throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_NODE.name(), "Invalid Metadata Node");
        this.objectType = objectType;
        this.nodeId = getSystemNodeType() + "_" + objectType + "_" + name;
        this.name = name;
    }

    public void create(final Request req) {
        final String defNodeId = SystemNodeTypes.DEFINITION_NODE.name() + "_" + objectType;
        final ExecutionContext ec = manager.getContext().dispatcher();
        final ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        Future<Object> getFuture = getNodeObject(req, dacRouter, getNodeId());
        getFuture.onComplete(new OnComplete<Object>() {
            @Override
            public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                boolean valid = false;
                if (null != arg0) {
                    manager.ERROR(arg0, getParent());
                } else {
                    if (arg1 instanceof Response) {
                        Response res = (Response) arg1;
                        if (manager.checkError(res)) {
                            if (!StringUtils.equals(ResponseCode.RESOURCE_NOT_FOUND.name(), res.getResponseCode().name())) {
                                manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_CREATE_METADATA_NODE_FAILED.name(),
                                        manager.getErrorMessage(res), res.getResponseCode(), getParent());
                            } else {
                                valid = true;
                            }
                        } else {
                            valid = true;
                        }
                    } else {
                        manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_CREATE_METADATA_NODE_FAILED.name(), "Internal Error",
                                ResponseCode.SERVER_ERROR, getParent());
                    }
                }
                if (valid) {
                    Future<Object> getDefNodeFuture = getNodeObject(req, dacRouter, defNodeId);
                    getDefNodeFuture.onComplete(new OnComplete<Object>() {
                        @Override
                        public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                            boolean valid = manager.checkResponseObject(arg0, arg1, getParent(),
                                    GraphEngineErrorCodes.ERR_GRAPH_CREATE_METADATA_NODE_FAILED.name(), "Definition Node not found: "
                                            + getObjectType());
                            if (valid) {
                                Request request = new Request(req);
                                request.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
                                request.setOperation("addNode");
                                request.put(GraphDACParams.node.name(), toNode());
                                Future<Object> createFuture = Patterns.ask(dacRouter, request, timeout);
                                createFuture.onComplete(new OnComplete<Object>() {
                                    @Override
                                    public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                                        boolean valid = manager.checkResponseObject(arg0, arg1, getParent(),
                                                GraphEngineErrorCodes.ERR_GRAPH_CREATE_METADATA_NODE_FAILED.name(),
                                                "Failed to create Metadata Node");
                                        if (valid) {
                                            IRelation rel = new HasMetadataRelation(getManager(), getGraphId(), defNodeId, getNodeId());
                                            rel.create(req);
                                        }
                                    }
                                }, ec);
                            }
                        }
                    }, ec);
                }
            }
        }, ec);
    }

    public String getSystemNodeType() {
        return SystemNodeTypes.METADATA_NODE.name();
    }

    public Node toNode() {
        Node node = new Node(getNodeId(), getSystemNodeType(), null);
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(METADATA_NODE_NAME_KEY, getName());
        node.setMetadata(metadata);
        return node;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getName() {
        return name;
    }

    private Future<Object> getNodeObject(Request req, ActorRef dacRouter, String nodeId) {
        Request request = new Request(req);
        request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
        request.setOperation("getNodeByUniqueId");
        request.put(GraphDACParams.node_id.name(), nodeId);
        Future<Object> future = Patterns.ask(dacRouter, request, timeout);
        return future;
    }
}
