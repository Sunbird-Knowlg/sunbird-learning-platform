package org.sunbird.graph.model.relation;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.exception.GraphRelationErrorCodes;
import org.sunbird.graph.model.IRelation;

public class RelationHandler {

    public static IRelation getRelation(BaseGraphManager manager, String graphId, String startNodeId,
            String relationType, String endNodeId, Map<String, Object> metadata) {

        if (StringUtils.isNotBlank(relationType) && RelationTypes.isValidRelationType(relationType)) {
            if (StringUtils.equals(RelationTypes.HIERARCHY.relationName(), relationType)) {
                return new HierarchyRelation(manager, graphId, startNodeId, endNodeId);
			} else if (StringUtils.equals(RelationTypes.CONSTITUENCY.relationName(), relationType)) {
				return new ConstituentRelation(manager, graphId, startNodeId, endNodeId);
            } else if (StringUtils.equals(RelationTypes.ASSOCIATED_TO.relationName(), relationType)) {
                return new AssociationRelation(manager, graphId, startNodeId, endNodeId, metadata);
            } else if (StringUtils.equals(RelationTypes.SET_MEMBERSHIP.relationName(), relationType)) {
                return new SetMembershipRelation(manager, graphId, startNodeId, endNodeId);
            } else if (StringUtils.equals(RelationTypes.SEQUENCE_MEMBERSHIP.relationName(), relationType)) {
                return new SequenceMembershipRelation(manager, graphId, startNodeId, endNodeId, metadata);
            } else if (StringUtils.equals(RelationTypes.SUB_SET.relationName(), relationType)) {
                return new SubsetRelation(manager, graphId, startNodeId, endNodeId);
			} else if (StringUtils.equals(RelationTypes.CO_OCCURRENCE.relationName(), relationType)) {
				return new CoOccurrenceRelation(manager, graphId, startNodeId, endNodeId);
            } else if (StringUtils.equals(RelationTypes.PRE_REQUISITE.relationName(), relationType)) {
                return new PreRequisiteRelation(manager, graphId, startNodeId, endNodeId);
			} else if (StringUtils.equals(RelationTypes.SUPERSEDED.relationName(), relationType)) {
				return new SupersededRelation(manager, graphId, startNodeId, endNodeId);
			} else {
				return new PropositionRelation(manager, graphId, startNodeId, relationType, endNodeId, metadata);
            }
        }
        throw new ClientException(GraphRelationErrorCodes.ERR_RELATION_CREATE.name(),
                "UnSupported Relation: " + relationType);
    }
}
