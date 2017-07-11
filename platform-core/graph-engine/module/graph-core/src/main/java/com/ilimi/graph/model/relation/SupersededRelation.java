package com.ilimi.graph.model.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.exception.GraphRelationErrorCodes;

import akka.dispatch.Futures;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.Promise;

public class SupersededRelation extends AbstractRelation {

    protected SupersededRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId) {
        super(manager, graphId, startNodeId, endNodeId);
    }

    @Override
    public String getRelationType() {
        return RelationTypes.SUPERSEDED.relationName();
    }

    @Override
    public Future<Map<String, List<String>>> validateRelation(Request request) {
        try {
            List<Future<String>> futures = new ArrayList<Future<String>>();
            final ExecutionContext ec = manager.getContext().dispatcher();
            Future<Node> startNode = getNode(request, this.startNodeId);
            Future<Node> endNode = getNode(request, this.endNodeId);
            Future<String> startNodeType = getNodeTypeFuture(startNode, ec);
            Future<String> endNodeType = getNodeTypeFuture(endNode, ec);
            Promise<String> nodeTypepromise = Futures.promise();
            Future<String> nodeTypeMessages = nodeTypepromise.future();
            compareFutures(startNodeType, endNodeType, nodeTypepromise, "nodeType", ec);
            futures.add(nodeTypeMessages);

            // check if the relation is valid between object type definitions.
            Future<String> startNodeObjectType = getObjectTypeFuture(startNode, ec);
            Future<String> endNodeObjectType = getObjectTypeFuture(endNode, ec);
            Promise<String> objectTypePromise = Futures.promise();
            Future<String> objectTypeMessages = objectTypePromise.future();
            compareFutures(startNodeObjectType, endNodeObjectType, nodeTypepromise, "objectType", ec);
            futures.add(objectTypeMessages);

            Future<Iterable<String>> aggregate = Futures.sequence(futures, manager.getContext().dispatcher());
            return getMessageMap(aggregate, ec);
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

}
