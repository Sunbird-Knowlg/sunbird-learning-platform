package org.sunbird.graph.model.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.exception.GraphRelationErrorCodes;

public class HasMetadataRelation extends AbstractRelation {
    
    public static final String RELATION_NAME = "hasMetadata"; 

    public HasMetadataRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId) {
        super(manager, graphId, startNodeId, endNodeId);
    }

    @Override
    public String getRelationType() {
        return RELATION_NAME;
    }

    @Override
	public Map<String, List<String>> validateRelation(Request request) {
        try {
			List<String> futures = new ArrayList<String>();
            // Check node types: start node type should be Definition Node.
            // end node type should be Metadata Node
			Node startNode = getNode(request, this.startNodeId);
			Node endNode = getNode(request, this.endNodeId);
			String startNodeMsg = getNodeTypeFuture(this.startNodeId, startNode,
					new String[] { SystemNodeTypes.DEFINITION_NODE.name() });
            futures.add(startNodeMsg);
			String endNodeMsg = getNodeTypeFuture(this.endNodeId, endNode,
					new String[] { SystemNodeTypes.METADATA_NODE.name() });
            futures.add(endNodeMsg);
			return getMessageMap(futures);
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

}
