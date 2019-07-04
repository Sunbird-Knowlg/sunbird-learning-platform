package org.ekstep.content.mgr.impl.operation.content;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.common.DateUtils;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class RetireOperation extends BaseContentManager {

    /**
     * @param contentId
     * @return
     */
    public Response retire(String contentId) {
        Boolean isImageNodeExist = false;
        if (StringUtils.isBlank(contentId) || StringUtils.endsWithIgnoreCase(contentId, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX))
            throw new ClientException(ContentErrorCodes.ERR_INVALID_CONTENT_ID.name(),
                    "Please Provide Valid Content Identifier.");

        Response response = getDataNode(TAXONOMY_ID, contentId);
        if (checkError(response))
            return response;

        Node node = (Node) response.get(GraphDACParams.node.name());
        String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
        String status = (String) node.getMetadata().get(ContentAPIParams.status.name());

        if (StringUtils.equalsIgnoreCase(ContentAPIParams.Retired.name(), status)) {
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_RETIRE.name(),
                    "Content with Identifier [" + contentId + "] is already Retired.");
        }

        Boolean isCollWithFinalStatus = (StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", mimeType) && finalStatus.contains(status)) ? true : false;
        RedisStoreUtil.delete(contentId);
        Response imageNodeResponse = getDataNode(TAXONOMY_ID, getImageId(contentId));
        if (!checkError(imageNodeResponse))
            isImageNodeExist = true;

        List<String> identifiers = (isImageNodeExist) ? Arrays.asList(contentId, getImageId(contentId)) : Arrays.asList(contentId);

        if (isCollWithFinalStatus) {
            RedisStoreUtil.delete(COLLECTION_CACHE_KEY_PREFIX + contentId);
            Response hierarchyResponse = getCollectionHierarchy(contentId);
            if (checkError(hierarchyResponse)) {
                throw new ServerException(ContentErrorCodes.ERR_CONTENT_RETIRE.name(),
                        "Unable to fetch Hierarchy for Root Node: [" + contentId + "]");
            }
            Map<String, Object> rootHierarchy = (Map<String, Object>) hierarchyResponse.getResult().get("hierarchy");
            List<Map<String, Object>> rootChildren = (List<Map<String, Object>>) rootHierarchy.get("children");
            List<String> childrenIdentifiers = getChildrenIdentifiers(rootChildren);

            if (CollectionUtils.isNotEmpty(childrenIdentifiers)) {
                String[] unitIds = childrenIdentifiers.stream().map(id -> (COLLECTION_CACHE_KEY_PREFIX + id)).collect(Collectors.toList()).toArray(new String[childrenIdentifiers.size()]);
                if (unitIds.length > 0)
                    RedisStoreUtil.delete(unitIds);
            }

            try {
                ElasticSearchUtil.bulkDeleteDocumentById(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, childrenIdentifiers);
            } catch (Exception e) {
                TelemetryManager.error("Exception Occured While Removing Children's from ES | Exception is : " + e);
                throw new ServerException(ContentErrorCodes.ERR_CONTENT_RETIRE.name(), "Something Went Wrong While Removing Children's from ES.");
            }
        }

        String date = DateUtils.formatCurrentDate();
        Map<String, Object> params = new HashMap<>();
        params.put("status", "Retired");
        params.put("lastStatusChangedOn", date);
        params.put("lastUpdatedOn", date);
        response = updateDataNodes(params, identifiers, TAXONOMY_ID);
        if (checkError(response)) {
            return response;
        }
        if (isCollWithFinalStatus)
            deleteHierarchy(Arrays.asList(contentId));

        Response responseNode = validateAndGetNodeResponseForOperation(contentId);
        node = (Node) responseNode.get("node");

        Response res = getSuccessResponse();
        res.put(ContentAPIParams.node_id.name(), node.getIdentifier());
        res.put(ContentAPIParams.versionKey.name(), node.getMetadata().get("versionKey"));
        return res;

    }

    /**
     * @param map
     * @param idList
     * @param graphId
     * @return
     */
    private Response updateDataNodes(Map<String, Object> map, List<String> idList, String graphId) {
        Response response;
        TelemetryManager.log("Getting Update Node Request For Node ID: " + idList);
        Request updateReq = getRequest(graphId, GraphEngineManagers.NODE_MANAGER, "updateDataNodes");
        updateReq.put(GraphDACParams.node_ids.name(), idList);
        updateReq.put(GraphDACParams.metadata.name(), map);
        TelemetryManager.log("Updating DialCodes for :" + idList);
        response = getResponse(updateReq);
        TelemetryManager.log("Returning Node Update Response.");
        return response;
    }

    /**
     *
     * @param children
     * @return List<String>
     */
    private List<String> getChildrenIdentifiers(List<Map<String, Object>> children) {
        List<String> identifiers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(children)) {
            children.forEach(child -> {
                if (StringUtils.equalsIgnoreCase("Parent", (String) child.get(ContentAPIParams.visibility.name())))
                    identifiers.add((String) child.get(ContentAPIParams.identifier.name()));

                getChildrenIdentifiers((List<Map<String, Object>>) child.get(ContentAPIParams.children.name()));
            });
        }
        return identifiers;
    }

}
