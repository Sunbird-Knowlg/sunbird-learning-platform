package com.ilimi.graph.model.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.exception.GraphRelationErrorCodes;

public class SetMembershipRelation extends AbstractRelation {

    public SetMembershipRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId) {
        super(manager, graphId, startNodeId, endNodeId);
    }

    @Override
    public String getRelationType() {
        return RelationTypes.SET_MEMBERSHIP.relationName();
    }

    @Override
    public Future<Map<String, List<String>>> validateRelation(Request request) {
        try {
            List<Future<String>> futures = new ArrayList<Future<String>>();
            // Check node types: start node type should be Set.
            // end node type should be data node
            final ExecutionContext ec = manager.getContext().dispatcher();
            Future<Node> startNode = getNode(request, this.startNodeId);
            Future<String> startNodeMsg = startNode.map(new Mapper<Node, String>() {
                @Override
                public String apply(Node node) {
                    if (null == node) {
                        return "Start Node Id is invalid";
                    } else {
                        String nodeType = node.getNodeType();
                        if (StringUtils.equals(SystemNodeTypes.SET.name(), nodeType)
                                || StringUtils.equals(SystemNodeTypes.TAG.name(), nodeType)) {
                            return null;
                        } else {
                            return "Start Node " + startNodeId + " should be one a Set or Tag";
                        }
                    }
                }
            }, ec);
            futures.add(startNodeMsg);
            Future<Node> endNode = getNode(request, this.endNodeId);
            Future<String> endNodeMsg = getNodeTypeFuture(endNode, SystemNodeTypes.DATA_NODE.name(), ec);
            futures.add(endNodeMsg);
            Future<Iterable<String>> aggregate = Futures.sequence(futures, manager.getContext().dispatcher());
            return getMessageMap(aggregate, ec);
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

}
