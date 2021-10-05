package org.sunbird.graph.model.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.exception.GraphRelationErrorCodes;

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
	public Map<String, List<String>> validateRelation(Request request) {
        try {
			List<String> futures = new ArrayList<String>();
            // Check node types: start node type should be data node.
            // end node type should be data node
			Node startNode = getNode(request, this.startNodeId);
			Node endNode = getNode(request, this.endNodeId);
			String startNodeMsg = getNodeTypeFuture(this.startNodeId, startNode,
					new String[] { SystemNodeTypes.DATA_NODE.name() });
            futures.add(startNodeMsg);
			String endNodeMsg = getNodeTypeFuture(this.endNodeId, endNode,
					new String[] { SystemNodeTypes.DATA_NODE.name(), SystemNodeTypes.SET.name() });
            futures.add(endNodeMsg);

            // check if the relation is valid between object type definitions.
			String objectType = getObjectTypeFuture(startNode);
			String endNodeObjectType = getObjectTypeFuture(endNode);
			String objectTypeMessages = validateObjectTypes(objectType, endNodeObjectType, request);
            futures.add(objectTypeMessages);

			return getMessageMap(futures);
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

}
