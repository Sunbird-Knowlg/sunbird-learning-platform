package com.ilimi.graph.model.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.pattern.Patterns;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.model.AbstractDomainObject;
import com.ilimi.graph.model.ICollection;

public abstract class AbstractCollection extends AbstractDomainObject implements ICollection {

    private String id;

    public AbstractCollection(BaseGraphManager manager, String graphId, String id) {
        super(manager, graphId);
        this.id = id;
    }

    @Override
    public String getNodeId() {
        return this.id;
    }

    protected void setNodeId(String id) {
        this.id = id;
    }

    @Override
    public void getProperty(Request request) {
        throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_UNSUPPORTED_OPERATION.name(),
                "getProperty is not supported on collections");
    }

    @Override
    public void removeProperty(Request request) {
        throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_UNSUPPORTED_OPERATION.name(),
                "removeProperty is not supported on collections");
    }

    @Override
    public void setProperty(Request request) {
        throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_UNSUPPORTED_OPERATION.name(),
                "setProperty is not supported on collections");
    }

    @Override
    public void updateMetadata(Request request) {
        throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_UNSUPPORTED_OPERATION.name(),
                "updateMetadata is not supported on collections");
    }

    @Override
    public void delete(Request request) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addMembers(Request request) {

    }

    @Override
    public void create(Request request) {

    }

    @Override
    public void getCardinality(Request request) {
        // TODO Auto-generated method stub
    }

    @Override
    public Node toNode() {
        Node node = new Node(this.id, getSystemNodeType(), getFunctionalObjectType());
        return node;
    }

    @Override
    public String getFunctionalObjectType() {
        return null;
    }

    @Override
    public Future<Map<String, List<String>>> validateNode(Request request) {
        Future<List<String>> metadataValidation = Futures.successful(null);
        return getMessageMap(metadataValidation, manager.getContext().dispatcher());
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

    @SuppressWarnings("unchecked")
    protected Future<Boolean> checkMemberNodes(Request req, final List<String> memberIds, final ExecutionContext ec) {
        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        Request request = new Request(req);
        request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
        request.setOperation("getNodesByUniqueIds");
        request.put(GraphDACParams.node_ids.name(), memberIds);
        Future<Object> dacFuture = Patterns.ask(dacRouter, request, timeout);
        Future<Boolean> validMembers = dacFuture.map(new Mapper<Object, Boolean>() {
            @Override
            public Boolean apply(Object parameter) {
                if (parameter instanceof Response) {
                    Response ar = (Response) parameter;
                    List<Node> nodes = (List<Node>) ar.get(GraphDACParams.node_list.name());
                    if (manager.validateRequired(nodes)) {
                        if (memberIds.size() == nodes.size()) {
                            for (Node node : nodes) {
                                if (!StringUtils.equals(SystemNodeTypes.DATA_NODE.name(), node.getNodeType()))
                                    return false;
                            }
                            return true;
                        }
                    }
                }
                return false;
            }
        }, ec);
        return validMembers;
    }

    protected Future<Node> getNodeObject(Request req, ExecutionContext ec, String setId) {
        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        Request request = getRequestObject(req, GraphDACManagers.DAC_SEARCH_MANAGER, "getNodeByUniqueId", GraphDACParams.node_id.name(),
                setId);
        Future<Object> dacFuture = Patterns.ask(dacRouter, request, timeout);
        Future<Node> nodeFuture = dacFuture.map(new Mapper<Object, Node>() {
            @Override
            public Node apply(Object parameter) {
                if (null != parameter && parameter instanceof Response) {
                    Response res = (Response) parameter;
                    Node node = (Node) res.get(GraphDACParams.node.name());
                    return node;
                }
                return null;
            }
        }, ec);
        return nodeFuture;
    }

}
