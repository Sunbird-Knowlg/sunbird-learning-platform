package com.ilimi.graph.model.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        return RelationTypes.SUB_SET.relationName();
    }

    @Override
	public Map<String, List<String>> validateRelation(Request request) {
        try {
			List<String> futures = new ArrayList<String>();
            // Check node types: start node type should be Set.
            // and end node type should be Set
			Node startNode = getNode(request, this.startNodeId);
			Node endNode = getNode(request, this.endNodeId);
			String startNodeMsg = getNodeTypeFuture(this.startNodeId, startNode,
					new String[] { SystemNodeTypes.SET.name() });
            futures.add(startNodeMsg);
			String endNodeMsg = getNodeTypeFuture(this.endNodeId, endNode, new String[] { SystemNodeTypes.SET.name() });
            futures.add(endNodeMsg);
			return getMessageMap(futures);
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }

    }

}
