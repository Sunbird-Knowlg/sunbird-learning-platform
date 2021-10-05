package org.sunbird.graph.model.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.exception.GraphRelationErrorCodes;

public class SequenceMembershipRelation extends AbstractRelation {

    public SequenceMembershipRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId, Map<String, Object> metadata) {
        super(manager, graphId, startNodeId, endNodeId, metadata);
    }

    @Override
    public String getRelationType() {
        return RelationTypes.SEQUENCE_MEMBERSHIP.relationName();
    }

    @Override
	public Map<String, List<String>> validateRelation(Request request) {
        try {
			List<String> futures = new ArrayList<String>();
            // check for cycle
			String cyclicCheck = checkCycle(request);
            futures.add(cyclicCheck);
        	
			List<Node> nodeFutures = new ArrayList<Node>();
            String startNodeId = this.startNodeId;
            String endNodeId = this.endNodeId;
			Node startNodeFuture = getNode(request, this.startNodeId);
			Node endNodeFuture = getNode(request, this.endNodeId);
            nodeFutures.add(startNodeFuture);
            nodeFutures.add(endNodeFuture);
            
			Node startNode = null;
			Node endNode = null;
			if (null != nodeFutures && !nodeFutures.isEmpty()) {
				for (Node node : nodeFutures) {
					if (startNode == null)
						startNode = node;
					else
						endNode = node;
				}
				if (null == startNode)
					futures.add("Invalid node: could not find node: " + startNodeId);
				if (null == endNode)
					futures.add("Invalid node: could not find node: " + endNodeId);
			} else {
				futures.add("Invalid nodes: could not find one or more nodes");
			}

			return getMessageMap(futures);
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

}
