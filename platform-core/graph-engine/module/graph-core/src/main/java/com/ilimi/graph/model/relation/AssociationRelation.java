package com.ilimi.graph.model.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.Promise;
import akka.dispatch.Futures;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.exception.GraphRelationErrorCodes;

public class AssociationRelation extends AbstractRelation {

    public AssociationRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId, Map<String, Object> metadata) {
        super(manager, graphId, startNodeId, endNodeId, metadata);
    }
    
    public AssociationRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId) {
        super(manager, graphId, startNodeId, endNodeId);
    }

    @Override
    public String getRelationType() {
        return RelationTypes.ASSOCIATED_TO.relationName();
    }

    @Override
    public Future<Map<String, List<String>>> validateRelation(Request request) {
        try {
            List<Future<String>> futures = new ArrayList<Future<String>>();
            // Check node types: start node type should be data node.
            // end node type should be data node
            final ExecutionContext ec = manager.getContext().dispatcher();
            Future<Node> startNode = getNode(request, this.startNodeId);
            Future<Node> endNode = getNode(request, this.endNodeId);
            Future<String> startNodeMsg = getNodeTypeFuture( this.startNodeId, startNode, new String[]{SystemNodeTypes.DATA_NODE.name()}, ec);
            futures.add(startNodeMsg);
            Future<String> endNodeMsg = getNodeTypeFuture(this.endNodeId, endNode, new String[]{SystemNodeTypes.DATA_NODE.name(), SystemNodeTypes.SET.name()}, ec);
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
