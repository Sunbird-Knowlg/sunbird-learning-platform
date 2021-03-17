package org.sunbird.content.tool;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.exception.ServerException;
import org.sunbird.content.tool.util.Input;
import org.sunbird.content.tool.util.InputList;

import java.util.*;

public class PlatformAPIManager extends BaseRESTAPIManager {

    protected String sourceKey = Platform.config.getString("source.key");
    protected String destKey = Platform.config.getString("destination.key");

    protected String sourceUrl = Platform.config.getString("source.url");
    protected String destUrl = Platform.config.getString("destination.url");
    protected int defaultLimit = Platform.config.hasPath("default.limit")? Platform.config.getInt("default.limit"): 10;

    protected boolean validChannel(String channel) throws Exception {
        Response readResponse = executeGET(destUrl + "/channel/v3/read/" + channel, destKey);
        return isSuccess(readResponse);
    }

    protected Response getContent(String id, boolean isDestination, String fields) throws Exception {
        String url = (isDestination)? destUrl : sourceUrl;
        String key = (isDestination)? destKey : sourceKey;
        url += "/content/v3/read/" + id;
        if(StringUtils.isNotBlank(fields))
            url += "?fields=" + fields;
        return executeGET(url, key);
    }


    protected Response systemUpdate(String id, Map<String, Object> request, String channel, boolean isDestination) throws Exception {
        String url = (isDestination)? destUrl : sourceUrl;
        String key = (isDestination)? destKey : sourceKey;
        url += "/system/v3/content/update/" + id;
        return executePATCH(url, key, request, channel);
    }

    protected void syncHierarchy(String id) throws Exception {
        executePOST(destUrl + "/content/v3/hierarchy/sync/" + id, destKey, new HashMap<>(), null);
    }


    protected int searchCount(Map<String, Object> filters) throws Exception {
        Map<String, Object> searchRequest = new HashMap<>();
        filters.remove("limit");
        filters.remove("offset");
        searchRequest.put("filters", filters);
        Map<String, Object> request = new HashMap<>();
        request.put("request", searchRequest);
        Response searchResponse = executePOST(sourceUrl + "/search/v2/search/count", sourceKey, request, null);
        if(isSuccess(searchResponse)){
            return (int) searchResponse.getResult().get("count");
        }else{
            throw new ServerException("ERR_CONTENT_SYNC", "Error while getting count of records : " + searchResponse.getParams().getErrmsg());
        }
    }

    protected InputList search(Map<String, Object> filters) throws Exception {
        InputList inputList = new InputList(new ArrayList<>());
        Map<String, Object> searchRequest = new HashMap<>();
        searchRequest.put("limit", filters.get("limit"));
        boolean isLimited = (null != filters.get("limit"));
        searchRequest.put("offset", filters.get("offset"));

        filters.remove("limit");
        filters.remove("offset");

        searchRequest.put("filters", filters);

        searchRequest.put("fields", Arrays.asList("identifier", "name", "pkgVersion", "objectType","status"));

        Map<String, Object> request = new HashMap<>();
        request.put("request", searchRequest);

        Response searchResponse = executePOST(sourceUrl + "/composite/v3/search", sourceKey, request, null);
        if (isSuccess(searchResponse)) {
            int count = (int) searchResponse.getResult().get("count");
            inputList.setCount(count);
            if(count > 0)
                getIdsFromResponse(searchResponse.getResult(), count, inputList, 0, request, isLimited);
        }

        return inputList;
    }

    private void getIdsFromResponse(Map<String, Object> result, int count, InputList inputList, int offset, Map<String, Object> request, boolean isLimited) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        if(null != result.get("content")) {
            results = (List<Map<String, Object>>) result.get("content");
        }else if(null != result.get("items")) {
            results = (List<Map<String, Object>>) result.get("items");
        }
        if (!isLimited && (count - 100) >= 0) {
            for (Map<String, Object> res : results) {
                double pkgVersion = (null != res.get("pkgVersion"))? ((Number)res.get("pkgVersion")).doubleValue(): 0d;
                inputList.add(new Input((String) res.get("identifier"), (String) res.get("name"), pkgVersion, (String)res.get("objectType"), (String)res.get("status")));
            }
            count -= 100;
            offset += 100;

            ((Map<String, Object>) request.get("request")).put("offset", offset);

            Response searchResponse = executePOST(sourceUrl + "/composite/v3/search", sourceKey, request, null);
            if (isSuccess(searchResponse)) {
                getIdsFromResponse(searchResponse.getResult(), count, inputList, offset, request, isLimited);
            } else {
                throw new ServerException("ERR_SYNC_SERVICE", "Error while fetching identifiers", searchResponse.getParams().getErr());
            }

        } else {
            for (Map<String, Object> res : results) {
                double pkgVersion = (null != res.get("pkgVersion"))? ((Number)res.get("pkgVersion")).doubleValue(): 0d;
                inputList.add(new Input((String) res.get("identifier"), (String) res.get("name"), pkgVersion, (String)res.get("objectType"), (String)res.get("status")));
            }
        }
    }



    protected Response readQuestion(String id, boolean isDestination) throws Exception {
        String url = (isDestination)? destUrl : sourceUrl;
        String key = (isDestination)? destKey : sourceKey;
        url += "/assessment/v3/items/read/" + id;
        return executeGET(url, key);
    }

    protected Response createQuestion(Map<String, Object> request, String channel, boolean isDestination) throws Exception {
        String url = (isDestination)? destUrl : sourceUrl;
        String key = (isDestination)? destKey : sourceKey;
        url += "/assessment/v3/items/create";
        return executePOST(url, key, request, channel);
    }

    protected Response updateQuestion(String id, Map<String, Object> request, String channel, boolean isDestination) throws Exception {
        String url = (isDestination)? destUrl : sourceUrl;
        String key = (isDestination)? destKey : sourceKey;
        url += "/assessment/v3/items/update/"+ id;
        return executePATCH(url, key, request, channel);
    }
}
