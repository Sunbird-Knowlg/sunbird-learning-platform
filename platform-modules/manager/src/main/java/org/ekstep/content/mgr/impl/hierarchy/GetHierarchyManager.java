package org.ekstep.content.mgr.impl.hierarchy;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.common.enums.LearningActorNames;
import org.ekstep.learning.contentstore.ContentStoreOperations;
import org.ekstep.learning.contentstore.ContentStoreParams;
import org.ekstep.taxonomy.mgr.impl.DummyBaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GetHierarchyManager extends DummyBaseContentManager {

    public Response getHierarchy(String contentId, String mode) {
        if(StringUtils.equalsIgnoreCase("edit", mode)){
            Node node = getContentNode(TAXONOMY_ID, contentId, mode);

            boolean fetchAll = true;
            String nodeStatus = (String) node.getMetadata().get("status");
            if(StringUtils.equalsIgnoreCase(nodeStatus, "Retired")) {
                throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
                        "Content not found with id: " + contentId);
            }else if(!(StringUtils.equalsIgnoreCase(mode, "edit")) && (StringUtils.equalsIgnoreCase(nodeStatus, "Live") || StringUtils.equalsIgnoreCase(nodeStatus, "Unlisted"))) {
                fetchAll = false;
            }

            TelemetryManager.log("Collecting Hierarchical Data For Content Id: " + node.getIdentifier());
            DefinitionDTO definition = getDefinition(TAXONOMY_ID, node.getObjectType());
            Map<String, Object> map = util.getContentHierarchyRecursive(TAXONOMY_ID, node, definition, mode, fetchAll);
            Map<String, Object> dataMap = contentCleanUp(map);
            Response response = new Response();
            response.put("content", dataMap);
            response.setParams(getSucessStatus());
            return response;
        } else{
            Response hierarchyResponse = getCollectionHierarchy(contentId);
            Response response = new Response();
            if(!checkError(hierarchyResponse) && (null != hierarchyResponse.getResult().get("hierarchy"))){
                String cachedStatus = RedisStoreUtil.getNodeProperty(TAXONOMY_ID, contentId, "status");
                Map<String, Object> hierarchy = (Map<String, Object>) hierarchyResponse.getResult().get("hierarchy");
                if(StringUtils.isNotBlank(cachedStatus)){
                    hierarchy.put("status", cachedStatus);
                } else{
                    hierarchy.put("status", getStatus(contentId, mode));
                }

                response.put("content", hierarchy);
                response.setParams(getSucessStatus());
            } else {
                response = hierarchyResponse;
            }
            return response;
        }
    }

    private Response getCollectionHierarchy(String contentId) {
        Request request = new Request();
        request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
        request.setOperation(ContentStoreOperations.getCollectionHierarchy.name());
        request.put(ContentStoreParams.content_id.name(), contentId);
        Response response = makeLearningRequest(request);
        return response;
    }

    private String getStatus(String contentId, String mode) {
        Node node  = getContentNode(TAXONOMY_ID, contentId, mode);
        RedisStoreUtil.saveNodeProperty(TAXONOMY_ID, contentId, "status", (String) node.getMetadata().get("status"));
        return (String) node.getMetadata().get("status");
    }

}
