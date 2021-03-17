package org.sunbird.graph.model.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.exception.GraphRelationErrorCodes;

public class HierarchyRelation extends AbstractRelation {

    public HierarchyRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId) {
        super(manager, graphId, startNodeId, endNodeId);
    }

    @Override
    public String getRelationType() {
        return RelationTypes.HIERARCHY.relationName();
    }

    @Override
    public Map<String, List<String>> validateRelation(final Request request) {
        try {
            List<String> futures = new ArrayList<String>();
            // check for cycle
            String cyclicCheck = checkCycle(request);
            futures.add(cyclicCheck);

            // Check node types: start node type should be data node.
            // end node type can be data node, sequence or set
            Node startNode = getNode(request, this.startNodeId);
            Node endNode = getNode(request, this.endNodeId);
            String startNodeMsg = getNodeTypeFuture(this.startNodeId, startNode, new String[]{SystemNodeTypes.DATA_NODE.name()});
            futures.add(startNodeMsg);
            String endNodeMsg = null;

            if (null == endNode) {
            	endNodeMsg = "End Node Id is invalid";
            } else {
                String nodeType = endNode.getNodeType();
                if (StringUtils.equals(SystemNodeTypes.DATA_NODE.name(), nodeType)
                        || StringUtils.equals(SystemNodeTypes.SEQUENCE.name(), nodeType)
                        || StringUtils.equals(SystemNodeTypes.SET.name(), nodeType)) {
                    List<Relation> inRels = endNode.getInRelations();
                    if (null != inRels && inRels.size() > 0) {
                        for (Relation rel : inRels) {
                            if (StringUtils.equals(RelationTypes.HIERARCHY.relationName(), rel.getRelationType())) {
                                if (!StringUtils.equals(getStartNodeId(), rel.getStartNodeId())) {
                                	endNodeMsg = "Node " + endNodeId + " has more than one parent";
                                }
                            }
                        }
                    }
                    endNodeMsg = null;
                } else {
                	endNodeMsg = "End Node " + endNodeId + " should be one of Data Node, Set or Sequence";
                }
            }
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
