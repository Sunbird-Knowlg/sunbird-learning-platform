package org.ekstep.content.mgr.impl.operation.hierarchy;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.common.enums.LearningActorNames;
import org.ekstep.learning.contentstore.ContentStoreOperations;
import org.ekstep.learning.contentstore.ContentStoreParams;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetHierarchyOperation extends BaseContentManager {

    private static final String COLLECTION_MIME_TYPE = "application/vnd.ekstep.content-collection";

    public Response getHierarchy(String contentId, String mode) {
        if(StringUtils.equalsIgnoreCase("edit", mode)){
            Node node = getContentNode(TAXONOMY_ID, contentId, mode);

            boolean fetchAll = true;
            String nodeStatus = (String) node.getMetadata().get("status");
            validateIsNodeRetired(node.getMetadata());

            if(!(StringUtils.equalsIgnoreCase(mode, "edit")) && (StringUtils.equalsIgnoreCase(nodeStatus, "Live") || StringUtils.equalsIgnoreCase(nodeStatus, "Unlisted"))) {
                fetchAll = false;
            }

            TelemetryManager.log("Collecting Hierarchical Data For Content Id: " + node.getIdentifier());
            DefinitionDTO definition = getDefinition(TAXONOMY_ID, node.getObjectType());
            Map<String, Object> map = util.getContentHierarchyRecursive(TAXONOMY_ID, node, definition, mode, true);
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

    public Response getContentHierarchy(String contentId, String bookMarkId, String mode, List<String> fields) {
        String id = getNodeIdToBeFetched(contentId, bookMarkId);
        if(StringUtils.equalsIgnoreCase("edit", mode)){
            Node node = getContentNode(TAXONOMY_ID, id, mode);
            String nodeStatus = (String) node.getMetadata().get("status");

            if(StringUtils.equalsIgnoreCase(nodeStatus, "Retired")) {
                throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
                        "Content not found with id: " + id);
            }

            if(!StringUtils.equalsIgnoreCase(COLLECTION_MIME_TYPE, (String) node.getMetadata().get("mimeType"))) {
                throw new ClientException(ContentErrorCodes.ERR_INVALID_INPUT.name(),
                        "Requested ID is not of collection mimeType : " + id);
            }

            DefinitionDTO definition = getDefinition(TAXONOMY_ID, node.getObjectType());
            long startTime = System.currentTimeMillis();
            Map<String,Object> dataMap = util.getHierarchyMap(TAXONOMY_ID, node.getIdentifier(), definition, mode,
                    fields);
            System.out.println("Time to fetchNodes and construct hierarchy: " + (System.currentTimeMillis() - startTime));

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
                Map<String, Object> responseHierarchy = hierarchy;
                if(!StringUtils.equalsIgnoreCase(id, contentId)) {
                    responseHierarchy = getPublishedBookMark((List<Map<String, Object>>) hierarchy.get("children"), id);
                    if(MapUtils.isEmpty(responseHierarchy)){
                        throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
                                "Content not found with id: " + id);
                    }
                }
                response.put("content", responseHierarchy);
                response.setParams(getSucessStatus());
            } else {
                response = hierarchyResponse;
            }
            return response;
        }
    }

    private String getNodeIdToBeFetched(String contentId, String bookMarkId) {
        if(StringUtils.isBlank(contentId) || StringUtils.isBlank(bookMarkId))
            throw new ClientException(ContentErrorCodes.ERR_INVALID_INPUT.name(), "Requested ID is null or empty");
        return StringUtils.equalsIgnoreCase(contentId, bookMarkId) ? contentId : bookMarkId;
    }



    private static Map<String,Object> getPublishedBookMark(List<Map<String, Object>> children, String bookMarkId) {
        if(CollectionUtils.isNotEmpty(children)){
            List<Map<String ,Object>> response = children.stream().filter(child -> StringUtils.equalsIgnoreCase
                    (bookMarkId, (String)
                    child.get("identifier"))).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(response))
                return response.get(0);
            else{
                List<Map<String, Object>> nextChildren = children.stream().flatMap(child -> ((List<Map<String,
                        Object>>)child.get("children")).stream()).collect(Collectors.toList());

                return getPublishedBookMark(nextChildren, bookMarkId);
            }

        }
        return null;

    }
}
