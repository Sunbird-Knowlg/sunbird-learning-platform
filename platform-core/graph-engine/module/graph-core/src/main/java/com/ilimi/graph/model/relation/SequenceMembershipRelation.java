package com.ilimi.graph.model.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.exception.GraphRelationErrorCodes;

import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

public class SequenceMembershipRelation extends AbstractRelation {

    public SequenceMembershipRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId, Map<String, Object> metadata) {
        super(manager, graphId, startNodeId, endNodeId, metadata);
    }

    @Override
    public String getRelationType() {
        return RelationTypes.SEQUENCE_MEMBERSHIP.relationName();
    }

    @Override
    public Future<Map<String, List<String>>> validateRelation(Request request) {
        try {
            List<Future<Node>> nodeFutures = new ArrayList<Future<Node>>();
            final ExecutionContext ec = manager.getContext().dispatcher();
            Future<Node> startNodeFuture = getNode(request, this.startNodeId);
            Future<Node> endNodeFuture = getNode(request, this.endNodeId);
            nodeFutures.add(startNodeFuture);
            nodeFutures.add(endNodeFuture);
            Future<Iterable<Node>> nodeAggregate = Futures.sequence(nodeFutures, manager.getContext().dispatcher());
            Future<Map<String, List<String>>> messageMap = nodeAggregate
                    .map(new Mapper<Iterable<Node>, Map<String, List<String>>>() {
                        @Override
                        public Map<String, List<String>> apply(Iterable<Node> parameter) {
                            Map<String, List<String>> map = new HashMap<String, List<String>>();
                            List<String> messages = new ArrayList<String>();
                            Node startNode = null;
                            Node endNode = null;
                            if (null != parameter) {
                                for (Node node : parameter) {
                                    if (startNode == null)
                                        startNode = node;
                                    else
                                        endNode = node;
                                }
                                if (null == startNode || null == endNode)
                                    messages.add("Invalid nodes: could not find one or more nodes");
                            } else {
                                messages.add("Invalid nodes: could not find one or more nodes");
                            }
                            map.put(getStartNodeId(), messages);
                            return map;
                        }
                    }, ec);
            return messageMap;
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

}
