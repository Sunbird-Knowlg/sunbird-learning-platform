package com.ilimi.graph.model.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.exception.GraphRelationErrorCodes;

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
        	List<Future<String>> futures = new ArrayList<Future<String>>();
            // check for cycle
            Future<String> cyclicCheck = checkCycle(request);
            futures.add(cyclicCheck);
        	
            List<Future<Node>> nodeFutures = new ArrayList<Future<Node>>();
            final ExecutionContext ec = manager.getContext().dispatcher();
            String startNodeId = this.startNodeId;
            String endNodeId = this.endNodeId;
            Future<Node> startNodeFuture = getNode(request, this.startNodeId);
            Future<Node> endNodeFuture = getNode(request, this.endNodeId);
            nodeFutures.add(startNodeFuture);
            nodeFutures.add(endNodeFuture);
            Future<Iterable<Node>> nodeAggregate = Futures.sequence(nodeFutures, manager.getContext().dispatcher());
            Future<String> messages = nodeAggregate
                    .map(new Mapper<Iterable<Node>, String>() {
                        @Override
                        public String apply(Iterable<Node> parameter) {
                            Node startNode = null;
                            Node endNode = null;
                            if (null != parameter) {
                                for (Node node : parameter) {
                                    if (startNode == null)
                                        startNode = node;
                                    else
                                        endNode = node;
                                }
                                if (null == startNode)
                                	return "Invalid node: could not find node: " + startNodeId;
                                if(null == endNode)
                                	return "Invalid node: could not find node: " + endNodeId;
                            } else {
                                return "Invalid nodes: could not find one or more nodes";
                            }
                            return null;
                        }
                    }, ec);
            futures.add(messages);
            
            Future<Iterable<String>> aggregate = Futures.sequence(futures, manager.getContext().dispatcher());
            return getMessageMap(aggregate, ec);
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

}
