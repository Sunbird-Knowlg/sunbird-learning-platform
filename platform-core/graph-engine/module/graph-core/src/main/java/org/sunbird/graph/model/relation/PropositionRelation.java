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
import org.sunbird.graph.exception.GraphRelationErrorCodes;

public class PropositionRelation extends AbstractRelation {

	private String relationType;

	protected PropositionRelation(BaseGraphManager manager, String graphId, String startNodeId, String relationType,
			String endNodeId, Map<String, Object> metadata) {
		super(manager, graphId, startNodeId, endNodeId, metadata);
		this.relationType = relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}

	@Override
	public String getRelationType() {
		return StringUtils.isBlank(relationType) ? RelationTypes.PRE_REQUISITE.relationName() : relationType;
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
					new String[] { SystemNodeTypes.DATA_NODE.name(), SystemNodeTypes.SET.name() });
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
