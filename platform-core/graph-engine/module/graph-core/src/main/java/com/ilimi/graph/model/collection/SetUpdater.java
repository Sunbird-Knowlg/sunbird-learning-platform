package com.ilimi.graph.model.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.Promise;
import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.model.AbstractDomainObject;
import com.ilimi.graph.model.node.MetadataNode;
import com.ilimi.graph.model.node.ValueNode;
import com.ilimi.graph.model.relation.HasValueRelation;
import com.ilimi.graph.model.relation.UsedBySetRelation;

public class SetUpdater extends AbstractDomainObject {

    public SetUpdater(BaseGraphManager manager, String graphId) {
        super(manager, graphId);
    }

    public Future<List<String>> getSets(final Request req, String objectType, Map<String, Object> oldMetadata,
            Map<String, Object> newMetadata) {
        final ExecutionContext ec = manager.getContext().dispatcher();
        if (null == oldMetadata)
            oldMetadata = new HashMap<String, Object>();
        if (null == newMetadata)
            newMetadata = new HashMap<String, Object>();
        List<Future<List<String>>> list = new ArrayList<Future<List<String>>>();
        for (Entry<String, Object> entry : newMetadata.entrySet()) {
            Future<List<String>> setIdsFuture = getSets(req, objectType, entry.getKey(), oldMetadata.get(entry.getKey()), entry.getValue());
            list.add(setIdsFuture);
        }
        for (Entry<String, Object> entry : oldMetadata.entrySet()) {
            if (null == newMetadata.get(entry.getKey())) {
                Future<List<String>> setIdsFuture = getSets(req, objectType, entry.getKey(), entry.getValue(),
                        newMetadata.get(entry.getKey()));
                list.add(setIdsFuture);
            }
        }
        Future<Iterable<List<String>>> composite = Futures.sequence(list, ec);
        Future<List<String>> future = composite.map(new Mapper<Iterable<List<String>>, List<String>>() {
            @Override
            public List<String> apply(Iterable<List<String>> parameter) {
                List<String> setIds = new ArrayList<String>();
                if (null != parameter) {
                    for (List<String> list : parameter) {
                        if (null != list && !list.isEmpty())
                            setIds.addAll(list);
                    }
                }
                return setIds;
            }
        }, ec);
        return future;
    }

    public Future<List<String>> getSets(final Request req, String objectType, String property, final Object oldValue, final Object newValue) {
        final Promise<List<String>> promise = Futures.promise();
        final Future<List<String>> future = promise.future();
        final ExecutionContext ec = manager.getContext().dispatcher();
        MetadataNode mNode = new MetadataNode(getManager(), getGraphId(), objectType, property);
        Future<Node> mNodeFuture = getNodeObject(req, mNode.getNodeId());
        mNodeFuture.onComplete(new OnComplete<Node>() {
            @Override
            public void onComplete(Throwable e, Node node) throws Throwable {
                final List<String> setIds = new ArrayList<String>();
                if (null != node) {
                    List<String> valueNodeIds = new ArrayList<String>();
                    List<Relation> outRels = node.getOutRelations();
                    if (null != outRels && outRels.size() > 0) {
                        for (Relation rel : outRels) {
                            if (StringUtils.equals(UsedBySetRelation.RELATION_NAME, rel.getRelationType())) {
                                setIds.add(rel.getEndNodeId());
                            } else if (StringUtils.equals(HasValueRelation.RELATION_NAME, rel.getRelationType())) {
                                if (null != rel.getEndNodeMetadata()) {
                                    Object val = rel.getEndNodeMetadata().get(ValueNode.VALUE_NODE_VALUE_KEY);
                                    if (null != val) {
                                        if (val == oldValue || val == newValue) {
                                            valueNodeIds.add(rel.getEndNodeId());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!valueNodeIds.isEmpty()) {
                        List<Future<Node>> vNodeFutures = new ArrayList<Future<Node>>();
                        for (String vNodeId : valueNodeIds) {
                            Future<Node> vNodeFuture = getNodeObject(req, vNodeId);
                            vNodeFutures.add(vNodeFuture);
                        }
                        Future<Iterable<Node>> valueNodes = Futures.sequence(vNodeFutures, ec);
                        valueNodes.onComplete(new OnComplete<Iterable<Node>>() {
                            @Override
                            public void onComplete(Throwable arg0, Iterable<Node> vNodes) throws Throwable {
                                if (null != vNodes) {
                                    for (Node vNode : vNodes) {
                                        List<Relation> outRels = vNode.getOutRelations();
                                        if (null != outRels && outRels.size() > 0) {
                                            for (Relation rel : outRels) {
                                                if (StringUtils.equals(UsedBySetRelation.RELATION_NAME, rel.getRelationType())) {
                                                    setIds.add(rel.getEndNodeId());
                                                }
                                            }
                                        }
                                    }
                                }
                                promise.success(setIds);
                            }
                        }, ec);
                    } else {
                        promise.success(setIds);
                    }
                } else {
                    promise.success(setIds);
                }
            }
        }, ec);
        return future;
    }

    private Future<Node> getNodeObject(Request req, String nodeId) {
        ExecutionContext ec = manager.getContext().dispatcher();
        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        Request request = new Request(req);
        request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
        request.setOperation("getNodeByUniqueId");
        request.put(GraphDACParams.node_id.name(), nodeId);
        Future<Object> future = Patterns.ask(dacRouter, request, timeout);
        Future<Node> nodeFuture = future.map(new Mapper<Object, Node>() {
            @Override
            public Node apply(Object obj) {
                if (obj instanceof Response) {
                    Response res = (Response) obj;
                    Node node = (Node) res.get(GraphDACParams.node.name());
                    return node;
                }
                return null;
            }
        }, ec);
        return nodeFuture;
    }

}
