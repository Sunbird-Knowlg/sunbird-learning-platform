package org.ekstep.graph.model.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.common.mgr.BaseGraphManager;
import org.ekstep.graph.dac.enums.RelationTypes;
import org.ekstep.graph.dac.enums.SystemNodeTypes;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.exception.GraphRelationErrorCodes;

public class SetMembershipRelation extends AbstractRelation {

    public SetMembershipRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId) {
        super(manager, graphId, startNodeId, endNodeId);
    }

    @Override
    public String getRelationType() {
        return RelationTypes.SET_MEMBERSHIP.relationName();
    }

    @Override
    public Map<String, List<String>> validateRelation(Request request) {
        try {
            List<String> futures = new ArrayList<String>();
            // Check node types: start node type should be Set.
            // end node type should be data node
            Node startNode = getNode(request, this.startNodeId);
            String startNodeMsg = null;
            
            if (null == startNode) {
            	startNodeMsg = "Start Node Id is invalid";
            } else {
                String nodeType = startNode.getNodeType();
                if (StringUtils.equals(SystemNodeTypes.SET.name(), nodeType))
                	startNodeMsg = null;
                 else {
                	 startNodeMsg = "Start Node " + startNodeId + " should be one a Set";
                }
            }
            futures.add(startNodeMsg);
            Node endNode = getNode(request, this.endNodeId);
			String endNodeMsg = getNodeTypeFuture(this.endNodeId, endNode,
					new String[] { SystemNodeTypes.DATA_NODE.name() });
            futures.add(endNodeMsg);
			return getMessageMap(futures);
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

}
