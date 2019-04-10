package org.ekstep.content.mgr.impl.operation.hierarchy;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.common.router.RequestRouterPool;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.kafka.KafkaClient;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.ekstep.telemetry.util.LogTelemetryEventUtil;
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
     * @param rootId
     * @param bookmarkId
     * @param mode
     * @param fields
     * @return
     */
    public Response getContentHierarchy(String rootId, String bookmarkId, String mode, List<String> fields) {
        // Check bookmarkId is same as rootId
        if (StringUtils.isBlank(rootId))
            throw new ClientException(ContentErrorCodes.ERR_INVALID_INPUT.name(), "Requested ID is null or empty");

        if (StringUtils.equalsIgnoreCase("edit", mode)) {
            return getUnPublishedHierarchy(rootId, bookmarkId, fields);
        } else {
            return getPublishedHierarchy(rootId, bookmarkId);
        }
    }

    /**
     * Get Hierarchy for Unpublished Collections
     *
     * @param rootId
     * @param bookmarkId
     * @param fields
     * @return
     */
    private Response getUnPublishedHierarchy(String rootId, String bookmarkId, List<String> fields) {
        String mode = "edit";
        Node rootNode = getContentNode(TAXONOMY_ID, rootId, mode);

        if(null != rootNode) {
            if(!StringUtils.equalsIgnoreCase(COLLECTION_MIME_TYPE, (String) rootNode.getMetadata().get("mimeType")))
                throw new ClientException(ContentErrorCodes.ERR_INVALID_INPUT.name(), "Given content id is not of collection : " + rootId);

            if(StringUtils.equalsIgnoreCase("Retired", (String) rootNode.getMetadata().get("status")))
                throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(), "Content not found with id: " + rootId);

            Response hierarchyResponse = getCollectionHierarchy(rootId + IMAGE_SUFFIX);
            DefinitionDTO definition = getDefinition(TAXONOMY_ID, rootNode.getObjectType());
            Map<String, Object> dataMap = ConvertGraphNode.convertGraphNode(rootNode, TAXONOMY_ID, definition, fields);
            if(!checkError(hierarchyResponse)) {
                Map<String, Object> hierarchy = (Map<String, Object>) hierarchyResponse.getResult().get("hierarchy");
                List<Map<String, Object>> children = (List<Map<String, Object>>) hierarchy.get("children");
                if (StringUtils.isNotBlank(bookmarkId)) {
                    dataMap = filterBookmark(children, bookmarkId);
                    if (MapUtils.isEmpty(dataMap)) {
                        throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(), "Content not found with id: " + bookmarkId);
                    }
                } else {
                    dataMap.put("children", children);
                }

            } else {
                dataMap = util.getHierarchyMap(TAXONOMY_ID, rootNode.getIdentifier(), definition, mode,null);
                String visibility = (String) rootNode.getMetadata().get("visibility");
                if (StringUtils.isNotBlank(bookmarkId)) {
                    List<Map<String, Object>> children = (List<Map<String, Object>>) dataMap.get("children");
                    dataMap = filterBookmark(children, bookmarkId);
                    if (MapUtils.isEmpty(dataMap)) {
                        throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(), "Content not found with id: " + bookmarkId);
                    }
                }
                if (StringUtils.equalsIgnoreCase("Parent", visibility)) {
                    String actualRootId = searchRootId(rootId);
                    if (StringUtils.isNotBlank(actualRootId)) {
                        generateMigrationInstructionEvent(actualRootId);
                    } else {
                        TelemetryManager.info("Root id not found for content id: "+ rootId + " for collection migration.");
                    }
                } else {
                    generateMigrationInstructionEvent(rootId);
                }
            }
            return OK("content", dataMap);
        } else {
            if (StringUtils.isNotBlank(bookmarkId)) {
                throw new ClientException(ContentErrorCodes.ERR_INVALID_INPUT.name(), "Given content id is not of root collection : " + rootId);
            } else {
                bookmarkId = rootId;
                rootId = searchRootId(bookmarkId);
                if(StringUtils.isNotBlank(rootId)) {
                    Response hierarchyResponse = getCollectionHierarchy(rootId + IMAGE_SUFFIX);
                    if(checkError(hierarchyResponse)){
                        return hierarchyResponse;
                    } else {
                        Map<String, Object> rootHierarchy = (Map<String, Object>) hierarchyResponse.getResult().get("hierarchy");
                        List<Map<String, Object>> rootChildren = (List<Map<String, Object>>) rootHierarchy.get("children");
                        Map<String, Object> hierarchy = filterBookmark(rootChildren, bookmarkId);
                        if (MapUtils.isNotEmpty(hierarchy)) {
                            generateMigrationInstructionEvent(rootId);
                            return OK("content", hierarchy);
                        } else {
                            throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(), "Content not found with id: " + bookmarkId);
                        }
                    }
                } else {
                    throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(), "Content not found with id: " + bookmarkId);
                }
            }
        }

    }

    /**
     * Get Hierarchy for Published Collection
     *
     * @param rootId
     * @param bookmarkId
     * @return
     */
    private Response getPublishedHierarchy(String rootId, String bookmarkId) {
        Response rootResponse = getCollectionHierarchy(rootId);
        if (!checkError(rootResponse)) {
            Map<String, Object> rootHierarchy = (Map<String, Object>) rootResponse.getResult().get("hierarchy");
            return getHierarchyResponse(rootHierarchy, bookmarkId);
        } else {
            if (StringUtils.isBlank(bookmarkId)) {
                bookmarkId = rootId;
                rootId = searchRootId(bookmarkId);
                if (StringUtils.isNotBlank(rootId)) {
                    rootResponse = getCollectionHierarchy(rootId);
                    Map<String, Object> rootHierarchy = (Map<String, Object>) rootResponse.getResult().get("hierarchy");
                    return getHierarchyResponse(rootHierarchy, bookmarkId);
                } else {
                    throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(), "Content not found with id: " + bookmarkId);
                }
            } else {
                throw new ClientException(ContentErrorCodes.ERR_INVALID_INPUT.name(), "Given collection root object ID is invalid: " + rootId);
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
            List<String> fields = new ArrayList<>();
            fields.add("identifier");
            searchDto.setFields(fields);
            Future<SearchResponse> searchResp = processor.processSearchQueryWithSearchResult(searchDto, false,
                    CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, false);
            SearchResponse searchResponse = Await.result(searchResp, RequestRouterPool.WAIT_TIMEOUT.duration());
            List<Object> searchResult = ElasticSearchUtil.getDocumentsFromHits(searchResponse.getHits());
            if (CollectionUtils.isNotEmpty(searchResult))
                return (String) ((Map<String, Object>) searchResult.get(0)).get("identifier");
            else
                return null;
        } catch (Exception e) {
            throw new ServerException(ContentErrorCodes.ERR_CONTENT_SEARCH_ERROR.name(), "Error while searching bookmarkId",
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

    private Response getHierarchyResponse(Map<String, Object> hierarchy, String bookmarkId) {
        if (StringUtils.isBlank(bookmarkId)) {
            return OK("content", hierarchy);
        } else {
            List<Map<String, Object>> rootChildren = (List<Map<String, Object>>) hierarchy.get("children");
            hierarchy = filterBookmark(rootChildren, bookmarkId);
            if (MapUtils.isNotEmpty(hierarchy)) {
                return OK("content", hierarchy);
            } else {
                throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(), "Content not found with id: " + bookmarkId);
            }
        }
    }

    /**
     * Filter and return bookMark Hierarchy
     *
     * @param children
     * @param bookMarkId
     * @return
     */
    private static Map<String, Object> filterBookmark(List<Map<String, Object>> children, String bookMarkId) {
        if (CollectionUtils.isNotEmpty(children)) {
            List<Map<String, Object>> response = children.stream().filter(child -> StringUtils.equalsIgnoreCase
                    (bookMarkId, (String)
                            child.get("identifier"))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(response))
                return response.get(0);
            else {
                List<Map<String, Object>> nextChildren = children.stream()
                        .map(child -> (List<Map<String, Object>>) child.get("children"))
                        .filter(f -> CollectionUtils.isNotEmpty(f)).flatMap(f -> f.stream())
                        .collect(Collectors.toList());

                return filterBookmark(nextChildren, bookMarkId);
            }

        }
        return null;

    }

    /**
     * Aysnc call to update cassandra and clean up neo4j for collection hierarchy
     *
     * @param identifier
     */
    private void generateMigrationInstructionEvent(String identifier) {
        System.out.println("Migration should be triggered for content: " + identifier);
        try {
            pushInstructionEvent(identifier);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pushInstructionEvent(String contentId) throws Exception {
        Map<String,Object> actor = new HashMap<String,Object>();
        Map<String,Object> context = new HashMap<String,Object>();
        Map<String,Object> object = new HashMap<String,Object>();
        Map<String,Object> edata = new HashMap<String,Object>();

        generateInstructionEventMetadata(actor, context, object, edata, contentId);
        String beJobRequestEvent = LogTelemetryEventUtil.logInstructionEvent(actor, context, object, edata);
        String topic = Platform.config.getString("kafka.topics.instruction");
        if(StringUtils.isBlank(beJobRequestEvent)) {
            throw new ClientException("BE_JOB_REQUEST_EXCEPTION", "Event is not generated properly.");
        }
        if(StringUtils.isNotBlank(topic)) {
            KafkaClient.send(beJobRequestEvent, topic);
        } else {
            throw new ClientException("BE_JOB_REQUEST_EXCEPTION", "Invalid topic id.");
        }
    }

    private void generateInstructionEventMetadata(Map<String,Object> actor, Map<String,Object> context,
                                                  Map<String,Object> object, Map<String,Object> edata, String contentId) {

        actor.put("id", "Collection Migration Samza Job");
        actor.put("type", "System");

        Map<String, Object> pdata = new HashMap<>();
        pdata.put("id", "org.ekstep.platform");
        pdata.put("ver", "1.0");
        context.put("pdata", pdata);
        if (Platform.config.hasPath("cloud_storage.env")) {
            String env = Platform.config.getString("cloud_storage.env");
            context.put("env", env);
        }

        object.put("id", contentId);
        object.put("type", "content");

        edata.put("action", "collection-migration");
        edata.put("contentType", "TextBook");
    }


}
