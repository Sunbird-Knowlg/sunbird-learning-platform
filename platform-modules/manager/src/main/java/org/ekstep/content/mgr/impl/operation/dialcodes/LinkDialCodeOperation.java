package org.ekstep.content.mgr.impl.operation.dialcodes;

import org.apache.commons.lang3.StringUtils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LinkDialCodeOperation extends BaseContentManager {

    private final String ERR_DIALCODE_LINK_REQUEST = "Invalid Request.";

    private final String DIALCODE_SEARCH_URI = Platform.config.hasPath("dialcode.api.search.url")
            ? Platform.config.getString("dialcode.api.search.url") : "http://localhost:8080/learning-service/v3/dialcode/search";


    public Response linkDialCode(String channelId, Object reqObj, String mode, String contentId) throws Exception {

        List<String> dialcodeList = new ArrayList<String>();
        List<String> contentList = new ArrayList<String>();
        Map<String, Set<String>> resultMap = initializeResultMap();
        List<Map<String, Object>> reqList = getRequestList(reqObj);
        //TODO: Validation logic need to be change for restricting dialcode with mode=collection
        // All Units should have unique dialcodes.
        validateDialCodeLinkRequest(channelId, reqList);
        //TODO: Implementation need to be done.s
        if(StringUtils.isNotBlank(mode) && StringUtils.equalsIgnoreCase("collection",mode) && StringUtils.isNotBlank(contentId)){
            return getSuccessResponse();
        }

        for (Map<String, Object> map : reqList) {
            Object dialObj = map.get(DialCodeEnum.dialcode.name());
            Object contentObj = map.get("identifier");
            List<String> dialcodes = getList(dialObj);
            List<String> contents = getList(contentObj);
            dialcodeList.addAll(dialcodes);
            contentList.addAll(contents);
            updateDialCodeToContents(contents, dialcodes, resultMap);
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

    /**
     * @param contents
     * @param dialcodes
     * @param resultMap
     * @throws Exception
     */
    private void updateDialCodeToContents(List<String> contents, List<String> dialcodes,
                                          Map<String, Set<String>> resultMap) throws Exception {
        Response resp;
        for (String contentId : contents) {
            Map<String, Object> map = new HashMap<String, Object>();
            if (!dialcodes.isEmpty())
                map.put(DialCodeEnum.dialcodes.name(), dialcodes);
            else
                map.put(DialCodeEnum.dialcodes.name(), null);

            Response responseNode = getDataNode(TAXONOMY_ID, contentId);
            if (checkError(responseNode)) {
                resultMap.get("invalidContentList").add(contentId);
            } else {
                resp = updateDialCode(contentId, map);
                if (!checkError(resp))
                    resultMap.get("updateSuccessList").add(contentId);
                else
                    resultMap.get("updateFailedList").add(contentId);
            }
        }
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
                messages.add("Content link with dialcode(s) fialed for id(s): " + String.join(",", updateFailedList));

            resp.setParams(getErrorStatus(DialCodeEnum.ERR_DIALCODE_LINK.name(), String.join(",", messages)));
        }

        return resp;
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
     * @param map
     * @param contentId
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private Response updateDialCode(String identifier, Map<String, Object> map) throws Exception {
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
                if (!checkError) {
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

}
