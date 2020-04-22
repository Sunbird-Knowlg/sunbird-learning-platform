package org.ekstep.content.mgr.impl.operation.dialcodes;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.common.util.HttpRestUtil;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.contentstore.ContentStoreParams;
import org.ekstep.taxonomy.enums.DialCodeEnum;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class LinkDialCodeOperation extends BaseContentManager {

    private final String ERR_DIALCODE_LINK_REQUEST = "Invalid Request.";
    
    private final List<String> publishStatusList = Arrays.asList("Live", "Unlisted");
    private final List<String> collectionDraftProperties = Arrays.asList("identifier", "children");

    private final String DIALCODE_SEARCH_URI = Platform.config.hasPath("dialcode.api.search.url")
            ? Platform.config.getString("dialcode.api.search.url") : "http://localhost:8080/learning-service/v3/dialcode/search";

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     *
     * @param channelId
     * @param reqObj
     * @param mode
     * @param contentId
     * @return
     * @throws Exception
     */
    public Response linkDialCode(String channelId, Object reqObj, String mode, String contentId) throws Exception {

        List<String> dialcodeList = new ArrayList<String>();
        List<String> contentList = new ArrayList<String>();
        Map<String, List<String>> requestMap = new HashMap<>();
        Map<String, Set<String>> resultMap = initializeResultMap();
        List<Map<String, Object>> reqList = getRequestList(reqObj);
        Boolean isCollectionMode = false;

        // collection dial link
        if (StringUtils.equalsIgnoreCase("collection", mode) && StringUtils.isNotBlank(contentId)) {
            validateRootNode(contentId);
            isCollectionMode = true;

        }
        validateDialCodeLinkRequest(channelId, reqList);
        prepareRequestMap(reqList, requestMap);

        if (isCollectionMode) {
            updateDialCodeToCollection(contentId, requestMap, resultMap);
        } else {
            updateDialCodeToContents(requestMap, resultMap);
        }

        Response resp = prepareResponse(resultMap);

        if (!checkError(resp) && ResponseCode.OK.name().equals(resp.getResponseCode().name())) {
            Map<String, Object> props = new HashMap<String, Object>();
            props.put(DialCodeEnum.dialcode.name(), dialcodeList);
            props.put("identifier", contentList);
            TelemetryManager.info("DIAL code linked to content", props);
        } else
            TelemetryManager.error(resp.getParams().getErrmsg());

        return resp;
    }

    /**
     * @return
     */
    private Map<String, Set<String>> initializeResultMap() {
        Map<String, Set<String>> resultMap = new HashMap<String, Set<String>>();
        resultMap.put("invalidContentList", new HashSet<String>());
        resultMap.put("updateFailedList", new HashSet<String>());
        resultMap.put("updateSuccessList", new HashSet<String>());
        return resultMap;
    }

    /**
     * @param reqObj
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getRequestList(Object reqObj) {
        List<Map<String, Object>> reqList = null;
        try {
            reqList = (List<Map<String, Object>>) reqObj;
        } catch (Exception e) {
            Map<String, Object> reqMap = (Map<String, Object>) reqObj;
            if (null != reqMap)
                reqList = Arrays.asList(reqMap);
        }
        return reqList;
    }

    /**
     * This Method Validate Root Node from collection content.
     * @param rootIdentifier
     */
    private void validateRootNode(String rootIdentifier) {
        Response nodeResponse = getDataNode(TAXONOMY_ID, rootIdentifier);
        if (checkError(nodeResponse)) {
            throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK.name(),
                    "Unable to fetch Content with Identifier : [" + rootIdentifier + "]");
        }
        Node node = (Node) nodeResponse.get("node");
        String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
        String visibility = (String) node.getMetadata().get(ContentAPIParams.visibility.name());
        if (!StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", mimeType) &&
                !StringUtils.equalsIgnoreCase("default", visibility)) {
            throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK.name(),
                    "Invalid Root Node Identifier : [" + rootIdentifier + "]");
        }
    }

    /**
     * @param channelId
     * @param reqList
     */
    private void validateDialCodeLinkRequest(String channelId, List<Map<String, Object>> reqList) throws Exception {
        if (null == reqList || 0 == reqList.size())
            throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK_REQUEST.name(), ERR_DIALCODE_LINK_REQUEST);

        Set<String> dialCodeList = new HashSet<String>();
        for (Map<String, Object> map : reqList) {
            if (null == map)
                throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK_REQUEST.name(), ERR_DIALCODE_LINK_REQUEST);
            Object dialObj = map.get(DialCodeEnum.dialcode.name());
            Object contentObj = map.get("identifier");
            List<String> dialcodes = getList(dialObj);
            List<String> contents = getList(contentObj);
            validateReqStructure(dialcodes, contents);
            if (!dialcodes.isEmpty())
                dialCodeList.addAll(dialcodes);
        }
        Boolean isValReq = Platform.config.hasPath("learning.content.link_dialcode_validation")
                ? Platform.config.getBoolean("learning.content.link_dialcode_validation") : true;
        if (isValReq)
            validateDialCodes(channelId, dialCodeList);
    }

    private void prepareRequestMap(List<Map<String, Object>> reqList, Map<String, List<String>> requestMap){
        for (Map<String, Object> map : reqList) {
            Object dialObj = map.get(DialCodeEnum.dialcode.name());
            Object contentObj = map.get("identifier");
            List<String> dialcodes = getList(dialObj);
            List<String> contents = getList(contentObj);
            for(String content: contents){
                requestMap.put(content,dialcodes);
            }
        }
    }

    /**
     * @param dialcodes
     * @param contents
     */
    private void validateReqStructure(List<String> dialcodes, List<String> contents) {
        if (null == dialcodes || null == contents || contents.isEmpty())
            throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK_REQUEST.name(),
                    "Pelase provide required properties in request.");

        int maxLimit = 10;
        if (Platform.config.hasPath("dialcode.link.content.max"))
            maxLimit = Platform.config.getInt("dialcode.link.content.max");

        if (dialcodes.size() >= maxLimit || contents.size() >= maxLimit)
            throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK_REQUEST.name(),
                    "Max limit for link content to dialcode in a request is " + maxLimit);
    }

    /**
     * @param channelId
     * @param dialcodesList
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked" })
    private void validateDialCodes(String channelId, Set<String> dialcodesList) throws Exception {
        if (!dialcodesList.isEmpty()) {
            List<Object> resultList = null;
            List<String> dialcodes = new ArrayList<String>(dialcodesList);
            List<String> invalidDialCodeList = new ArrayList<String>(dialcodes);
            Integer dialcodeCount = dialcodes.size();

            Map<String, Object> requestMap = new HashMap<String, Object>();
            Map<String, Object> searchMap = new HashMap<String, Object>();
            Map<String, Object> data = new HashMap<String, Object>();
            data.put(ContentAPIParams.identifier.name(), dialcodes);
            searchMap.put("search", data);
            requestMap.put("request", searchMap);

            Map<String, String> headerParam = new HashMap<String, String>();
            headerParam.put("X-Channel-Id", channelId);

            Response searchResponse = HttpRestUtil.makePostRequest(DIALCODE_SEARCH_URI, requestMap, headerParam);
            if (searchResponse.getResponseCode() == ResponseCode.OK) {
                Map<String, Object> result = searchResponse.getResult();
                Integer count = (Integer) result.get(DialCodeEnum.count.name());
                if (dialcodeCount != count) {
                    resultList = (List<Object>) result.get(DialCodeEnum.dialcodes.name());
                    for (Object obj : resultList) {
                        Map<String, Object> map = (Map<String, Object>) obj;
                        String identifier = (String) map.get(ContentAPIParams.identifier.name());
                        invalidDialCodeList.remove(identifier);
                    }
                    throw new ResourceNotFoundException(DialCodeEnum.ERR_DIALCODE_LINK.name(),
                            "DIAL Code not found with id(s):" + invalidDialCodeList);
                }
            } else {
                throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
                        "Something Went Wrong While Processing Your Request. Please Try Again After Sometime!");
            }
        }
    }

    /**
     * @param requestMap
     * @param resultMap
     * @throws Exception
     */
    private void updateDialCodeToContents(Map<String, List<String>> requestMap,
                                          Map<String, Set<String>> resultMap) throws Exception {
        Response resp;
        for (String contentId : requestMap.keySet()) {
            Map<String, Object> map = new HashMap<String, Object>();
            if (!requestMap.get(contentId).isEmpty())
                map.put(DialCodeEnum.dialcodes.name(), requestMap.get(contentId));
            else
                map.put(DialCodeEnum.dialcodes.name(), null);

            Response responseNode = getDataNode(TAXONOMY_ID, contentId);
            if (checkError(responseNode)) {
                resultMap.get("invalidContentList").add(contentId);
            } else {
                resp = updateDataNode(contentId, map, null);
                if (!checkError(resp))
                    resultMap.get("updateSuccessList").add(contentId);
                else
                    resultMap.get("updateFailedList").add(contentId);
            }
        }
    }


    /**
     *
     * @param requestMap
     * @param resultMap
     * @throws Exception
     */
    private void updateDialCodeToCollection(String rootNodeId, Map<String, List<String>> requestMap,
                                            Map<String, Set<String>> resultMap) throws Exception {
        List<String> existingDialcodes = new ArrayList<String>();
        Map<String, List<String>> collReqMap = new HashMap<String, List<String>>();
        if (requestMap.containsKey(rootNodeId)) {
            collReqMap.put(rootNodeId, getList(requestMap.get(rootNodeId)));
            requestMap.remove(rootNodeId);
        }

        String rootHierarchyId = getImageId(rootNodeId);
        Response hierarchyResponse = getCollectionHierarchy(rootHierarchyId);
        if(checkError(hierarchyResponse)){
        	rootHierarchyId = rootNodeId;
        	hierarchyResponse = getCollectionHierarchy(rootHierarchyId);
        	if(checkError(hierarchyResponse)) {
        		throw new ServerException(DialCodeEnum.ERR_DIALCODE_LINK.name(),
                        "Unable to fetch Hierarchy for Root Node: [" + rootNodeId + "]");
        	}
        }
        Map<String, Object> rootHierarchy = (Map<String, Object>) hierarchyResponse.getResult().get("hierarchy");
        List<Map<String, Object>> rootChildren = (List<Map<String, Object>>) rootHierarchy.get("children");

        updateCollectionUnits(rootNodeId, rootChildren, requestMap, resultMap);
        if (MapUtils.isNotEmpty(collReqMap)) {
            List<String> dials = getList(collReqMap.get(rootNodeId));
            validateDuplicateDialCodes(rootNodeId, existingDialcodes, rootChildren, dials);
        } else {
            validateDuplicateDialCodes(rootNodeId, existingDialcodes, rootChildren, null);
        }

        if(rootHierarchy.containsKey("status") && publishStatusList.contains((String)rootHierarchy.get("status"))) {
        	rootHierarchy.entrySet().removeIf(e -> !collectionDraftProperties.contains(e.getKey()));
        	rootHierarchyId = getImageId(rootNodeId);
        }
        //update cassandra
        Response response = updateCollectionHierarchy(rootHierarchyId,rootHierarchy);
        
        if (!checkError(response))
            resultMap.get("updateSuccessList").addAll(requestMap.keySet());
        else
            resultMap.get("updateFailedList").addAll(requestMap.keySet());

        Map<String, Object> req = new HashMap<String, Object>();
        if (null != collReqMap.get(rootNodeId)) {
            List<String> dialcodes = getList(collReqMap.get(rootNodeId));
            if (CollectionUtils.isNotEmpty(dialcodes)) {
                req.put(DialCodeEnum.dialcodes.name(), dialcodes);

            } else {
                req.put(DialCodeEnum.dialcodes.name(), null);
            }
        }
       Response nodeResp =  updateDataNode(rootNodeId, req, "collection");
        if (!checkError(nodeResp))
            resultMap.get("updateSuccessList").addAll(req.keySet());
        else
            resultMap.get("updateFailedList").addAll(req.keySet());

    }

    /**
     *
     * @param identifier
     * @param map
     * @param mode
     * @return
     * @throws Exception
     */
    private Response updateDataNode(String identifier, Map<String, Object> map, String mode) throws Exception {
        DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
        String contentId=identifier;
        String objectType = CONTENT_OBJECT_TYPE;
        map.put("objectType", CONTENT_OBJECT_TYPE);
        map.put("identifier", contentId);

        boolean isImageObjectCreationNeeded = false;
        boolean imageObjectExists = false;

        String contentImageId = contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
        Response getNodeResponse = getDataNode(TAXONOMY_ID, contentImageId);
        if (checkError(getNodeResponse)) {
            TelemetryManager.log("Content image not found: " + contentImageId);
            isImageObjectCreationNeeded = true;
            getNodeResponse = getDataNode(TAXONOMY_ID, contentId);
            TelemetryManager.log("Content node response: " + getNodeResponse);
        } else
            imageObjectExists = true;

        List<String> externalPropsList = getExternalPropsList(definition);

        Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
        TelemetryManager.log("Graph node found: " + graphNode.getIdentifier());
        Map<String, Object> metadata = graphNode.getMetadata();
        String status = (String) metadata.get("status");

        boolean checkError = false;
        Response createResponse = null;
        if (finalStatus.contains(status)) {
            if (isImageObjectCreationNeeded) {
                graphNode.setIdentifier(contentImageId);
                graphNode.setObjectType(CONTENT_IMAGE_OBJECT_TYPE);
                metadata.put("status", "Draft");
                Object lastUpdatedBy = map.get("lastUpdatedBy");
                if (null != lastUpdatedBy)
                    metadata.put("lastUpdatedBy", lastUpdatedBy);
                graphNode.setGraphId(TAXONOMY_ID);
                createResponse = createDataNode(graphNode);
                checkError = checkError(createResponse);
                if (!checkError && !StringUtils.equalsIgnoreCase("collection", mode)) {
                    TelemetryManager.log("Updating external props for: " + contentImageId);
                    Response bodyResponse = getContentProperties(contentId, externalPropsList);
                    checkError = checkError(bodyResponse);
                    if (!checkError) {
                        Map<String, Object> extValues = (Map<String, Object>) bodyResponse
                                .get(ContentStoreParams.values.name());
                        if (null != extValues && !extValues.isEmpty()) {
                            updateContentProperties(contentImageId, extValues);
                        }
                    }
                    map.put("versionKey", createResponse.get("versionKey"));
                }
            }
            objectType = CONTENT_IMAGE_OBJECT_TYPE;
            contentId = contentImageId;
        } else if (imageObjectExists) {
            objectType = CONTENT_IMAGE_OBJECT_TYPE;
            contentId = contentImageId;
        }

        if (checkError)
            return createResponse;

        TelemetryManager.log("Updating content node: " + contentId);
        if (imageObjectExists || isImageObjectCreationNeeded) {
            definition = getDefinition(TAXONOMY_ID, CONTENT_IMAGE_OBJECT_TYPE);
        }
        String passportKey = Platform.config.getString("graph.passport.key.base");
        map.put("versionKey", passportKey);
        Node domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, graphNode);
        domainObj.setGraphId(TAXONOMY_ID);
        domainObj.setIdentifier(contentId);
        domainObj.setObjectType(objectType);
        createResponse = updateDataNode(domainObj);

        return createResponse;
    }

    private void updateCollectionUnits(String rootNodeId, List<Map<String, Object>> children, Map<String, List<String>> requestMap, Map<String, Set<String>> resultMap){
        if (CollectionUtils.isNotEmpty(children)) {
            children.forEach(child -> {
                try {
                    String childId = (String)child.get(ContentAPIParams.identifier.name());
                    String visibility = (String)child.get(ContentAPIParams.visibility.name());
                    if(requestMap.containsKey(childId) && StringUtils.equalsIgnoreCase("Default", visibility)) {
                        resultMap.get("invalidContentList").add(childId);
                        requestMap.remove(childId);
                    }else if(requestMap.containsKey(childId) && StringUtils.equalsIgnoreCase("Parent", visibility)){
                        // TODO: Generate Audit History Here.
                        if (!requestMap.get(childId).isEmpty())
                            child.put(DialCodeEnum.dialcodes.name(), requestMap.get(childId));
                        else
                            child.remove(DialCodeEnum.dialcodes.name());

                        requestMap.remove(childId);
                        resultMap.get("updateSuccessList").add(childId);
                    }
                } catch (Exception e) {
                    TelemetryManager.error("Error Occured while linking DIAL Code to Units of Root Node :"+rootNodeId,e);
                    throw new ServerException(DialCodeEnum.ERR_DIALCODE_LINK.name(),
                            "Something Went Wrong While Linking DIAL Code for Root Node: [" + rootNodeId + "]");
                }
                updateCollectionUnits(rootNodeId, (List<Map<String, Object>>) child.get(ContentAPIParams.children.name()), requestMap, resultMap);
            });
        }
    }

    /**
     *
     * @param children
     * @param existingDialcodes
     */
    private void getAssignedDialCodes(List<Map<String, Object>> children, List<String> existingDialcodes) {
        if (CollectionUtils.isNotEmpty(children)) {
            children.forEach(child -> {
                try {
                    String visibility = (String) child.get(ContentAPIParams.visibility.name());
                    if (StringUtils.equalsIgnoreCase("Parent", visibility)) {
                        existingDialcodes.addAll(getDialCodes(child));
                    }
                } catch (Exception e) {
                    throw new ServerException(DialCodeEnum.ERR_DIALCODE_LINK.name(),
                            "Something Went Wrong While Linking DIAL Code");
                }
                getAssignedDialCodes((List<Map<String, Object>>) child.get(ContentAPIParams.children.name()), existingDialcodes);
            });
        }
    }

    /**
     *
     * @param contentId
     * @param existingDialCodes
     */
    private void validateDuplicateDialCodes(String contentId, List<String> existingDialCodes, List<Map<String, Object>> childrens, List<String> dials) {
        Node node = null;
        Response response = getDataNode(TAXONOMY_ID, getImageId(contentId));
        if (!checkError(response)) {
            node = (Node) response.get("node");
        } else {
            response = getDataNode(TAXONOMY_ID, contentId);
            if (!checkError(response)) {
                node = (Node) response.get("node");
            }
        }
        if (null != node) {
            List<String> dialcodes = getDialCodes(node.getMetadata());
            if (null == dials && CollectionUtils.isNotEmpty(dialcodes))
                existingDialCodes.addAll(dialcodes);
            else if (CollectionUtils.isNotEmpty(dials)) {
                existingDialCodes.addAll(dials);
            }
        }

        getAssignedDialCodes(childrens, existingDialCodes);
        Map<String, Long> dialsGroupBy = existingDialCodes.stream().collect(Collectors.groupingByConcurrent(Function.identity(), Collectors.counting()));
        List<String> duplicateDials = dialsGroupBy.entrySet().stream().filter(entry -> entry.getValue() > 1).map(entry -> entry.getKey()).collect(toList());

        if (CollectionUtils.isNotEmpty(duplicateDials))
            throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK.name(), "QR Code should not be linked to multiple contents. Please validate: " + duplicateDials);

    }

    /**
     *
     * @param map
     * @return
     */
    public List<String> getDialCodes(Map<String, Object> map) {
        if (MapUtils.isNotEmpty(map) && map.containsKey("dialcodes")) {
            List<String> dialcodes = mapper.convertValue(map.get("dialcodes"), new TypeReference<List<String>>(){});
            return (dialcodes.stream().filter(f -> StringUtils.isNotBlank(f)).collect(toList()));
        }
        return new ArrayList<>();
    }


    /**
     * @param resultMap
     * @return
     */
    private Response prepareResponse(Map<String, Set<String>> resultMap) {
        Response resp;
        Set<String> invalidContentList = (Set<String>) resultMap.get("invalidContentList");
        Set<String> updateFailedList = (Set<String>) resultMap.get("updateFailedList");
        Set<String> updateSuccessList = (Set<String>) resultMap.get("updateSuccessList");

        if (invalidContentList.isEmpty() && updateFailedList.isEmpty()) {
            resp = new Response();
            resp.setParams(getSucessStatus());
            resp.setResponseCode(ResponseCode.OK);
        } else if (!invalidContentList.isEmpty() && updateSuccessList.size() == 0) {
            resp = new Response();
            resp.setResponseCode(ResponseCode.RESOURCE_NOT_FOUND);
            resp.setParams(getErrorStatus(DialCodeEnum.ERR_DIALCODE_LINK.name(),
                    "Content not found with id(s):" + invalidContentList));
        } else {
            resp = new Response();
            resp.setResponseCode(ResponseCode.PARTIAL_SUCCESS);
            List<String> messages = new ArrayList<String>();
            if (!invalidContentList.isEmpty())
                messages.add("Content not found with id(s): " + String.join(",", invalidContentList));
            if (!updateFailedList.isEmpty())
                messages.add("Content link with dialcode(s) failed for id(s): " + String.join(",", updateFailedList));

            resp.setParams(getErrorStatus(DialCodeEnum.ERR_DIALCODE_LINK.name(), String.join(",", messages)));
        }

        return resp;
    }

}
