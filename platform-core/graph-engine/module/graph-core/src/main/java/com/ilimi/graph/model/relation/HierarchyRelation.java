package com.ilimi.graph.model.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.Promise;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.exception.GraphRelationErrorCodes;

public class HierarchyRelation extends AbstractRelation {

    public HierarchyRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId) {
        super(manager, graphId, startNodeId, endNodeId);
    }

    @Override
    public String getRelationType() {
        return RelationTypes.HIERARCHY.relationName();
    }

    @Override
    public Future<Map<String, List<String>>> validateRelation(final Request request) {
        try {
            List<Future<String>> futures = new ArrayList<Future<String>>();
            // check for cycle
            Future<String> cyclicCheck = checkCycle(request);
            futures.add(cyclicCheck);

            // Check node types: start node type should be data node.
            // end node type can be data node, sequence or set
            final ExecutionContext ec = manager.getContext().dispatcher();
            Future<Node> startNode = getNode(request, this.startNodeId);
            Future<Node> endNode = getNode(request, this.endNodeId);
            Future<String> startNodeMsg = getNodeTypeFuture(this.startNodeId, startNode, new String[]{SystemNodeTypes.DATA_NODE.name()}, ec);
            futures.add(startNodeMsg);
            Future<String> endNodeMsg = endNode.map(new Mapper<Node, String>() {
                @Override
                public String apply(Node node) {
                    if (null == node) {
                        return "End Node Id is invalid";
                    } else {
                        String nodeType = node.getNodeType();
                        if (StringUtils.equals(SystemNodeTypes.DATA_NODE.name(), nodeType)
                                || StringUtils.equals(SystemNodeTypes.SEQUENCE.name(), nodeType)
                                || StringUtils.equals(SystemNodeTypes.SET.name(), nodeType)) {
                            List<Relation> inRels = node.getInRelations();
                            if (null != inRels && inRels.size() > 0) {
                                for (Relation rel : inRels) {
                                    if (StringUtils.equals(RelationTypes.HIERARCHY.relationName(), rel.getRelationType())) {
                                        if (!StringUtils.equals(getStartNodeId(), rel.getStartNodeId())) {
                                            return "Node " + endNodeId + " has more than one parent";
                                        }
                                    }
                                }
                            }
                            return null;
                        } else {
                            return "End Node " + endNodeId + " should be one of Data Node, Set or Sequence";
                        }
                    }
                }
            }, ec);
            futures.add(endNodeMsg);

            // check if the relation is valid between object type definitions.
            Future<String> objectType = getObjectTypeFuture(startNode, ec);
            Future<String> endNodeObjectType = getObjectTypeFuture(endNode, ec);
            Promise<String> objectTypePromise = Futures.promise();
            Future<String> objectTypeMessages = objectTypePromise.future();
            validateObjectTypes(objectType, endNodeObjectType, request, objectTypePromise, ec);
            futures.add(objectTypeMessages);

            Future<Iterable<String>> aggregate = Futures.sequence(futures, manager.getContext().dispatcher());
            return getMessageMap(aggregate, ec);
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

}
