package org.ekstep.content.mgr.impl.operation.content;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.HttpRestUtil;
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

    private static String COMPOSITE_SEARCH_URL = Platform.config.getString("kp.search_service.base_url") +"/v3/search";

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
            List<String> shallowIds = getShallowCopy(node.getIdentifier());
            if(CollectionUtils.isNotEmpty(shallowIds))
                throw new ClientException(ContentErrorCodes.ERR_CONTENT_RETIRE.name(),
                        "Content With Identifier [" + contentId + "] Can Not Be Retired. It Has Been Adopted By Other Users.");
            RedisStoreUtil.delete(COLLECTION_CACHE_KEY_PREFIX + contentId);
            Response hierarchyResponse = getCollectionHierarchy(contentId);
            if (checkError(hierarchyResponse)) {
                throw new ServerException(ContentErrorCodes.ERR_CONTENT_RETIRE.name(),
                        "Unable to fetch Hierarchy for Root Node: [" + contentId + "]");
            }
            Map<String, Object> rootHierarchy = (Map<String, Object>) hierarchyResponse.getResult().get("hierarchy");
            List<Map<String, Object>> rootChildren = (List<Map<String, Object>>) rootHierarchy.get("children");
            List<String> childrenIdentifiers = new ArrayList<String>();
            getChildrenIdentifiers(childrenIdentifiers, rootChildren);

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

        Response res = getSuccessResponse();
        res.put(ContentAPIParams.node_id.name(), contentId);
        return res;
    }

    private List<String> getShallowCopy(String identifier) {
        List<String> result = new ArrayList<String>();
        Map<String, Object> reqMap = getSearchRequest(identifier);
        try {
            Response searchResponse = HttpRestUtil.makePostRequest(COMPOSITE_SEARCH_URL, reqMap, new HashMap<String, String>());
            if (searchResponse.getResponseCode() == ResponseCode.OK && MapUtils.isNotEmpty(searchResponse.getResult())) {
                Map<String, Object> searchResult = searchResponse.getResult();
                Integer count = (Integer) searchResult.getOrDefault("count", 0);
                if (count > 0) {
                    result = ((List<Map<String, Object>>) searchResult.getOrDefault("content", new ArrayList<Map<String, Object>>())).stream().filter(map -> map.containsKey("identifier")).map(map -> (String) map.get("identifier")).collect(Collectors.toList());
                }
            } else {
                TelemetryManager.info("Recevied Invalid Search Response For Shallow Copy. Response is : "+searchResponse);
                throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
                        "Something Went Wrong While Processing Your Request. Please Try Again After Sometime!");
            }
        } catch (Exception e) {
            TelemetryManager.error("Exception Occurred While Making Search Call for Shallow Copy Validation. Exception is ",e);
            throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
                    "Something Went Wrong While Processing Your Request. Please Try Again After Sometime!");
        }
        return result;
    }

    private Map<String, Object> getSearchRequest(String identifier) {
        return new HashMap<String, Object>(){{
            put("request", new HashMap<String, Object>(){{
                put("filters", new HashMap<String, Object>(){{
                    put("objectType", "Content");
                    put("status", Arrays.asList());
                    put("origin",identifier);
                }});
            }});
        }};
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
     * @param unitNodes
     * @param children
     */
    private void getChildrenIdentifiers(List<String> unitNodes, List<Map<String, Object>> children) {
        if(CollectionUtils.isNotEmpty(children)) {
            children.stream().forEach(child -> {
                if(StringUtils.equalsIgnoreCase("Parent", (String) child.get("visibility"))) {
                    unitNodes.add((String)child.get("identifier"));
                    getChildrenIdentifiers(unitNodes, (List<Map<String, Object>>) child.get("children"));
                }
            });
        }
    }

}
