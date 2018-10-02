package org.ekstep.content.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;

import java.util.Map;

public class BaseRESTAPIManager {

    protected ObjectMapper mapper = new ObjectMapper();

    public boolean isSuccess(Response response) {
        return StringUtils.equals(ResponseParams.StatusType.successful.name(), response.getParams().getStatus());
    }

    protected Response executePOST(String url, String authKey, Map<String, Object> request, String channel) throws Exception {
        if (StringUtils.isBlank(channel)) {
            channel = "in.ekstep";
        }
        HttpResponse<String> httpResponse = Unirest.post(url).header("Authorization", authKey).header("Content-Type", "application/json").header("X-Channel-ID", channel).body(mapper
                .writeValueAsString(request)).asString();
        Response response = mapper.readValue(httpResponse.getBody(), Response.class);
        return response;
    }

    protected Response executePATCH(String url, String authKey, Map<String, Object> request, String channel) throws Exception {
        if (StringUtils.isBlank(channel)) {
            channel = "in.ekstep";
        }
        HttpResponse<String> httpResponse = Unirest.patch(url).header("Authorization", authKey).header("Content-Type", "application/json").header("X-Channel-ID", channel).body(mapper
                .writeValueAsString(request)).asString();
        Response response = mapper.readValue(httpResponse.getBody(), Response.class);
        return response;
    }


    protected Response executeGET(String url, String authKey) throws Exception {
        HttpResponse<String> httpResponse = Unirest.get(url).header("Authorization", authKey).asString();
        Response response = mapper.readValue(httpResponse.getBody(), Response.class);
        return response;
    }
}
