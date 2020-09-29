package org.ekstep.content.mgr.impl.operation.event;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.common.DateUtils;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.taxonomy.enums.TaxonomyAPIParams;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;

public class FlagOperation extends BaseContentManager {

    protected static final List<String> FLAGGABLE_STATUS = Arrays.asList(TaxonomyAPIParams.Live.name(), TaxonomyAPIParams.Unlisted.name(), TaxonomyAPIParams.Processing.name(), TaxonomyAPIParams.Flagged.name());

    public Response flag(String contentId, Map<String, Object> requestMap) throws Exception {

        TelemetryManager.log("FlagOperation:flag: Get data node for content: " + contentId);
        Response nodeResponse = getDataNode(TAXONOMY_ID, contentId);
        if (checkError(nodeResponse))
            return nodeResponse;

        List<String> flagReasons = (List<String>) requestMap.get(ContentWorkflowPipelineParams.flagReasons.name());
        String flaggedBy = (String) requestMap.get(ContentAPIParams.flaggedBy.name());
        List<String> flags = (List<String>) requestMap.get(ContentAPIParams.flags.name());
        String versionKey = (String) requestMap.get(ContentAPIParams.versionKey.name());

        Node node = (Node) nodeResponse.getResult().get(ContentAPIParams.node.name());
        String objectType = node.getObjectType();
        if (VALID_FLAG_OBJECT_TYPES.contains(objectType)) {
            Map<String, Object> metadata = node.getMetadata();
            String status = (String) metadata.get(ContentAPIParams.status.name());
            if (FLAGGABLE_STATUS.contains(status)) {
                Map request = new HashMap();

                List<String> flaggedList = addFlaggedBy(flaggedBy, metadata);
                if (CollectionUtils.isNotEmpty(flaggedList)) {
                    request.put(GraphDACParams.lastUpdatedBy.name(), flaggedList.get(0));
                }
                request.put(ContentAPIParams.flaggedBy.name(), flaggedList);

                request.put(ContentAPIParams.flags.name(), flags);
                request.put(ContentAPIParams.versionKey.name(), versionKey);
                request.put(ContentAPIParams.status.name(), ContentAPIParams.Flagged.name());
                request.put(ContentAPIParams.lastFlaggedOn.name(), DateUtils.formatCurrentDate());
                if (CollectionUtils.isNotEmpty(flagReasons)) {
                    request.put(ContentWorkflowPipelineParams.flagReasons.name(),
                            addFlagReasons(flagReasons, metadata));
                }
                request.put(ContentAPIParams.objectType.name(), objectType);
                request.put(ContentAPIParams.identifier.name(), contentId);

                TelemetryManager.log("FlagOperation:flag: Update data node for content: " + contentId);
                DefinitionDTO definition = getDefinition(TAXONOMY_ID, objectType);
                Node domainObj = ConvertToGraphNode.convertToGraphNode(request, definition, null);
                Response updateResponse = updateNode(contentId, objectType, domainObj);

                clearRedisCache(contentId);
                return updateResponse;

            } else {
                return ERROR("ERR_CONTENT_NOT_FLAGGABLE", "Unpublished Content " + contentId + " cannot be flagged", ResponseCode.CLIENT_ERROR);
            }

        } else {
            return ERROR("ERR_NODE_NOT_FOUND", objectType + " " + contentId + " not found", ResponseCode.RESOURCE_NOT_FOUND);
        }

    }

    private List<String> addFlagReasons(List<String> flagReasons, Map<String, Object> metadata) {
        Object existingFlagReasons = metadata.get(ContentWorkflowPipelineParams.flagReasons.name());
        if (null != existingFlagReasons) {
            List<String> existingFlagReasonsList = null;
            if (existingFlagReasons instanceof String[]) {
                existingFlagReasonsList = Arrays.asList((String[]) existingFlagReasons);
            } else if (existingFlagReasons instanceof List) {
                existingFlagReasonsList = (List<String>) existingFlagReasons;
            }
            if (CollectionUtils.isNotEmpty(existingFlagReasonsList)) {
                Set<String> flagReasonsSet = new HashSet<>(existingFlagReasonsList);
                flagReasonsSet.addAll(flagReasons);
                return new ArrayList<>(flagReasonsSet);
            }
        }
        return flagReasons;
    }

    private List<String> addFlaggedBy(String flaggedBy, Map<String, Object> metadata) {
        List<String> flaggedByList = new ArrayList();
        flaggedByList.add(flaggedBy);
        Object existingFlaggedBy = metadata.get(ContentAPIParams.flaggedBy.name());
        if (null != existingFlaggedBy) {
            List<String> existingFlaggedByList = null;
            if (existingFlaggedBy instanceof String[]) {
                existingFlaggedByList = Arrays.asList((String[]) existingFlaggedBy);
            } else if (existingFlaggedBy instanceof List) {
                existingFlaggedByList = (List<String>) existingFlaggedBy;
            }
            if (CollectionUtils.isNotEmpty(existingFlaggedByList)) {
                Set<String> flaggedBySet = new HashSet<>(existingFlaggedByList);
                flaggedBySet.addAll(flaggedByList);
                return new ArrayList<>(flaggedBySet);
            }
        }
        return flaggedByList;
    }
}
