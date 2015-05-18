package com.ilimi.graph.model.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import akka.dispatch.Futures;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.exception.GraphRelationErrorCodes;

public class SubsetRelation extends AbstractRelation {

    public SubsetRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId) {
        super(manager, graphId, startNodeId, endNodeId);
    }

    @Override
    public String getRelationType() {
        return RelationTypes.SUB_SET.name();
    }

    @Override
    public Future<Map<String, List<String>>> validateRelation(Request request) {
        try {
            List<Future<String>> futures = new ArrayList<Future<String>>();
            // Check node types: start node type should be Set.
            // and end node type should be Set
            final ExecutionContext ec = manager.getContext().dispatcher();
            Future<Node> startNode = getNode(request, this.startNodeId);
            Future<Node> endNode = getNode(request, this.endNodeId);
            Future<String> startNodeMsg = getNodeTypeFuture(startNode, SystemNodeTypes.SET.name(), ec);
            futures.add(startNodeMsg);
            Future<String> endNodeMsg = getNodeTypeFuture(endNode, SystemNodeTypes.SET.name(), ec);
            futures.add(endNodeMsg);
            Future<Iterable<String>> aggregate = Futures.sequence(futures, manager.getContext().dispatcher());
            return getMessageMap(aggregate, ec);
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }

    }

}
