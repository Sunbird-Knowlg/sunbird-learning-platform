package org.ekstep.content.tool;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.exception.ServerException;
import org.ekstep.content.tool.util.Input;
import org.ekstep.content.tool.util.InputList;

import java.util.*;

public class PlatformAPIManager extends BaseRESTAPIManager {

    protected String sourceKey = Platform.config.getString("source.key");
    protected String destKey = Platform.config.getString("destination.key");

    protected String sourceUrl = Platform.config.getString("source.url");
    protected String destUrl = Platform.config.getString("destination.url");

    protected boolean validChannel(String channel) throws Exception {
        Response readResponse = executeGET(destUrl + "/channel/v3/read/" + channel, destKey);
        return isSuccess(readResponse);
    }

    protected Response getContent(String id, boolean isDestination, String fields) throws Exception {
        if(isDestination) {
            String url = destUrl + "/content/v3/read/" + id;
            if(StringUtils.isNotBlank(fields))
                url += "?fields=" + fields;
            return executeGET(url, destKey);
        }
        else{
            String url = sourceUrl + "/content/v3/read/" + id;
            if(StringUtils.isNotBlank(fields))
                url += "?fields=" + fields;
            return executeGET(url, sourceKey);
        }
    }


    protected Response systemUpdate(String id, Map<String, Object> request, String channel, boolean isDestination) throws Exception {
        if(isDestination)
            return executePATCH(destUrl + "/system/v3/content/update/" + id, destKey, request, channel);
        else
            return executePATCH(sourceUrl + "/system/v3/content/update/" + id, sourceKey, request, channel);
    }

    protected InputList search(String filter) throws Exception {
        InputList inputList = new InputList(new ArrayList<>());
        Map<String, Object> filters = mapper.readValue(filter, Map.class);
        filters.remove("status");
        Map<String, Object>  removeAsset = new HashMap<>();
        removeAsset.put("ne", Arrays.asList("Asset", "Plugin"));
        filters.put("contentType", removeAsset);
        Map<String, Object> searchRequest = new HashMap<>();

        searchRequest.put("filters", filters);
        searchRequest.put("fields", Arrays.asList("identifier", "name", "pkgVersion", "objectType","status"));

        Map<String, Object> request = new HashMap<>();
        request.put("request", searchRequest);

        Response searchResponse = executePOST(sourceUrl + "/composite/v3/search", sourceKey, request, null);
        if (StringUtils.equals(ResponseParams.StatusType.successful.name(), searchResponse.getParams().getStatus())) {
            int count = (int) searchResponse.getResult().get("count");
            System.out.println("Count: " + count);
            getIdsFromResponse(searchResponse.getResult(), count, inputList, 0, request);
        }

        return inputList;
    }

    private void getIdsFromResponse(Map<String, Object> result, int count, InputList inputList, int offset, Map<String, Object> request) throws Exception {
        if ((count - 100) >= 0) {
            for (Map<String, Object> res : (List<Map<String, Object>>) result.get("content")) {
                inputList.add(new Input((String) res.get("identifier"), (String) res.get("name"), ((Number)res.get("pkgVersion")).doubleValue(), (String)res.get("objectType"), (String)res.get("status")));
            }
            count -= 100;
            offset += 100;

            ((Map<String, Object>) request.get("request")).put("offset", offset);

            Response searchResponse = executePOST(sourceUrl + "/composite/v3/search", sourceKey, request, null);
            if (isSuccess(searchResponse)) {
                getIdsFromResponse(searchResponse.getResult(), count, inputList, 0, request);
            } else {
                throw new ServerException("ERR_SYNC_SERVICE", "Error while fetching identifiers", searchResponse.getParams().getErr());
            }

        } else {
            for (Map<String, Object> res : (List<Map<String, Object>>) result.get("content")) {
                inputList.add(new Input((String) res.get("identifier"), (String) res.get("name"), ((Number)res.get("pkgVersion")).doubleValue(), (String)res.get("objectType"), (String)res.get("status")));
            }
        }
    }
}
