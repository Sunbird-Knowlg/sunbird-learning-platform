package org.ekstep.content.mgr.impl.operation.hierarchy;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.common.router.RequestRouterPool;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.common.enums.LearningActorNames;
import org.ekstep.learning.contentstore.ContentStoreOperations;
import org.ekstep.learning.contentstore.ContentStoreParams;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.elasticsearch.action.search.SearchResponse;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetHierarchyOperation extends BaseContentManager {

    private static final String COLLECTION_MIME_TYPE = "application/vnd.ekstep.content-collection";
    private SearchProcessor processor = new SearchProcessor();
    private static final String IMAGE_SUFFIX = ".img";

    /**
     * Get Collection Hierarchy
     *
     * @param contentId
     * @param bookMarkId
     * @param mode
     * @param fields
     * @return
     */
    public Response getContentHierarchy(String contentId, String bookMarkId, String mode, List<String> fields) {
        // Check bookMarkId is same as contentId
        String id = getNodeIdToBeFetched(contentId, bookMarkId);

        if (StringUtils.equalsIgnoreCase("edit", mode)) {
            return getUnPublishedHierarchy(id, mode, fields);
        } else {
            return getPublishedHierarchy(id);
        }
    }

    /**
     * Get Hierarchy for Unpublished Collections
     *
     * @param bookMarkId
     * @param mode
     * @param fields
     * @return
     */
    private Response getUnPublishedHierarchy(String bookMarkId, String mode, List<String> fields) {
        // Fetch collection root node from neo4j
        Node node = getContentNode(TAXONOMY_ID, bookMarkId, mode);
        /*
         * If Node exists, get Hierarchy from cassandra.
         * check cassandra response. If ok, return hierarchy.
         * else get hierarchy from neo4j and asynchronously migrate hierarchy and cleanUp neo4j
         */
        if(null != node){
            if(!StringUtils.equalsIgnoreCase(COLLECTION_MIME_TYPE, (String) node.getMetadata().get("mimeType")))
                throw new ClientException(ContentErrorCodes.ERR_INVALID_INPUT.name(),
                        "Requested ID is not of collection mimeType : " + bookMarkId);

            Response hierarchyResponse = getCollectionHierarchy(bookMarkId + IMAGE_SUFFIX);
            DefinitionDTO definition = getDefinition(TAXONOMY_ID, node.getObjectType());
            Map<String, Object> dataMap = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, fields);
            if(!checkError(hierarchyResponse)) {
                Map<String, Object> hierarchy = (Map<String, Object>) hierarchyResponse.getResult().get("hierarchy");
                dataMap.put("children", hierarchy.get("children"));
            } else {
                dataMap = util.getHierarchyMap(TAXONOMY_ID, node.getIdentifier(), definition, mode,
                        fields);
                createCollectionHierarchy(node.getIdentifier());
            }
            return OK("content", dataMap);
        }
        /*
         * Search for rootID from ES.
         * fetch collectionHierarchy from cassandra, filter for bookMarkId and return the response.
         */
        else {
            String rootId = searchRootId(bookMarkId);
            if(StringUtils.isNotBlank(rootId)){
                Response hierarchyResponse = getCollectionHierarchy(rootId + IMAGE_SUFFIX);
                if(checkError(hierarchyResponse)){
                    return hierarchyResponse;
                } else {
                    Map<String, Object> hierarchy = filterBookMark((List<Map<String, Object>>) ((Map<String, Object>) hierarchyResponse.getResult().get
                            ("hierarchy")).get("children"), bookMarkId);
                    if (MapUtils.isNotEmpty(hierarchy))
                        return OK("content", hierarchy);
                    else
                        throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
                                "Content not found with id: " + bookMarkId);
                }
            } else {
                throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
                        "Content not found with id: " + bookMarkId);
            }
        }

    }

    /**
     * Get Hierarchy for Published Collection
     *
     * @param bookMarkId
     * @return
     */
    private Response getPublishedHierarchy(String bookMarkId) {
        Map<String, Object> hierarchy;
        // Get live content hierarchy of bookMarkId
        Response hierarchyResponse = getCollectionHierarchy(bookMarkId);

        /* If hierarchy exists, return hierarchy
         *   Else search ES for rootID and fetch hierarchy for root ID and filter for bookMarkID
         */
        if (!checkError(hierarchyResponse)) {
            hierarchy = (Map<String, Object>) hierarchyResponse.getResult().get("hierarchy");
            return OK("content", hierarchy);
        }
        /*
         * Search for rootID from ES.
         * fetch collectionHierarchy from cassandra, filter for bookMarkId and return the response.
         */
        else {
            String rootId = searchRootId(bookMarkId);
            if (StringUtils.isNotBlank(rootId)) {
                Response rootResp = getCollectionHierarchy(rootId);
                hierarchy = filterBookMark((List<Map<String, Object>>) ((Map<String, Object>) rootResp.getResult().get
                        ("hierarchy")).get("children"), bookMarkId);
                if (MapUtils.isNotEmpty(hierarchy))
                    return OK("content", hierarchy);
                else
                    throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
                            "Content not found with id: " + bookMarkId);
            } else {
                throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
                        "Content not found with id: " + bookMarkId);
            }
        }
    }

    /**
     * Search Root Id for a bookMarkID from ES
     *
     * @param bookMarkId
     * @return
     */
    private String searchRootId(String bookMarkId) {
        try {
            SearchDTO searchDto = new SearchDTO();
            searchDto.setFuzzySearch(false);
            searchDto.setProperties(setSearchProperties(bookMarkId));
            searchDto.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
            searchDto.setFields(Arrays.asList("identifier"));
            Future<SearchResponse> searchResp = processor.processSearchQueryWithSearchResult(searchDto, false,
                    CompositeSearchConstants.DIAL_CODE_INDEX, true);
            SearchResponse searchResponse = Await.result(searchResp, RequestRouterPool.WAIT_TIMEOUT.duration());
            List<Object> searchResult = ElasticSearchUtil.getDocumentsFromHits(searchResponse.getHits());
            if (CollectionUtils.isNotEmpty(searchResult))
                return (String) ((Map<String, Object>) searchResult.get(0)).get("identifier");
            else
                return null;
        } catch (Exception e) {
            throw new ServerException(ContentErrorCodes.ERR_CONTENT_SEARCH_ERROR.name(), "Error while searching bookMarkId",
                    e);
        }
    }

    private List<Map> setSearchProperties(String bookMarkId) {
        return new ArrayList<Map>() {{
            add(new HashMap<String, Object>() {{
                put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
                put("propertyName", "childNodes");
                put("values", Arrays.asList(bookMarkId));
            }});
            add(new HashMap<String, Object>() {{
                put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
                put("propertyName", "status");
                put("values", "Live");
            }});
        }};

    }

    /**
     * Validate and return bookMarkId
     *
     * @param contentId
     * @param bookMarkId
     * @return
     */
    private String getNodeIdToBeFetched(String contentId, String bookMarkId) {
        if (StringUtils.isBlank(contentId) || StringUtils.isBlank(bookMarkId))
            throw new ClientException(ContentErrorCodes.ERR_INVALID_INPUT.name(), "Requested ID is null or empty");
        return StringUtils.equalsIgnoreCase(contentId, bookMarkId) ? contentId : bookMarkId;
    }


    /**
     * Filter and return bookMark Hierarchy
     *
     * @param children
     * @param bookMarkId
     * @return
     */
    private static Map<String, Object> filterBookMark(List<Map<String, Object>> children, String bookMarkId) {
        if (CollectionUtils.isNotEmpty(children)) {
            List<Map<String, Object>> response = children.stream().filter(child -> StringUtils.equalsIgnoreCase
                    (bookMarkId, (String)
                            child.get("identifier"))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(response))
                return response.get(0);
            else {
                List<Map<String, Object>> nextChildren = children.stream().flatMap(child -> ((List<Map<String,
                        Object>>) child.get("children")).stream()).collect(Collectors.toList());

                return filterBookMark(nextChildren, bookMarkId);
            }

        }
        return null;

    }

    /**
     * Cassandra call to fetch hierarchy data
     *
     * @param contentId
     * @return
     */
    private Response getCollectionHierarchy(String contentId) {
        Request request = new Request();
        request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
        request.setOperation(ContentStoreOperations.getCollectionHierarchy.name());
        request.put(ContentStoreParams.content_id.name(), contentId);
        Response response = makeLearningRequest(request);
        return response;
    }

    /**
     * Aysnc call to update cassandra and clean up neo4j for collection hierarchy
     *
     * @param identifier
     */
    private void createCollectionHierarchy(String identifier) {
    }


}
