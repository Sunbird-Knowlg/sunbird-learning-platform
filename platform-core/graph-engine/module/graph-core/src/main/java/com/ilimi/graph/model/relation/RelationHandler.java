package com.ilimi.graph.model.relation;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.exception.GraphRelationErrorCodes;
import com.ilimi.graph.model.IRelation;

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
            } else if (StringUtils.equals(RelationTypes.SYNONYM.relationName(), relationType)
                    || StringUtils.equals(RelationTypes.ANTONYM.relationName(), relationType)
                    || StringUtils.equals(RelationTypes.HYPERNYM.relationName(), relationType)
                    || StringUtils.equals(RelationTypes.HYPONYM.relationName(), relationType)
                    || StringUtils.equals(RelationTypes.HOLONYM.relationName(), relationType)
                    || StringUtils.equals(RelationTypes.MERONYM.relationName(), relationType)) {
                return new PropositionRelation(manager, graphId, startNodeId, relationType, endNodeId);
            }
            throw new ClientException(GraphRelationErrorCodes.ERR_RELATION_CREATE.name(),
                    "UnSupported Relation: " + relationType);
        }
        throw new ClientException(GraphRelationErrorCodes.ERR_RELATION_CREATE.name(),
                "UnSupported Relation: " + relationType);
    }
}
