package org.sunbird.jobs.samza.service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ServerException;

import java.util.Map;

public class BaseCourseBatchUpdater {

    protected static ObjectMapper mapper = new ObjectMapper();
    protected static final String KP_LEARNING_BASE_URL = Platform.config.hasPath("kp.learning_service.base_url")
            ? Platform.config.getString("kp.learning_service.base_url"): "http://localhost:8080/learning-service";

    protected static final String KP_CONTENT_SERVICE_BASE_URL = Platform.config.hasPath("kp.content_service.base_url")
            ? Platform.config.getString("kp.content_service.base_url"): "http://localhost:9000";

    protected Map<String,Object> getContent(String courseId, String fields) throws Exception {
        String url = KP_CONTENT_SERVICE_BASE_URL + "/content/v3/read/" + courseId;
        if(StringUtils.isNotBlank(fields))
            url += "?fields=" + fields;

        HttpResponse<String> httpResponse = Unirest.get(url).header("Content-Type", "application/json").asString();
        if(200 != httpResponse.getStatus()){
            System.err.println("Error while reading content from KP : " + courseId + " : " + httpResponse.getStatus() + " : " + httpResponse.getBody());
            throw new ServerException("ERR_COURSE_BATCH_SAMZA", "Error while reading content from KP : " + courseId + " : " + httpResponse.getStatus() + " : " + httpResponse.getBody());
        }
        Response response = mapper.readValue(httpResponse.getBody(), Response.class);
        Map<String, Object> content = (Map<String, Object>) response.getResult().get("content");
        return content;
    }

    protected void systemUpdate(String courseId, Request payload) throws Exception {
        String url = KP_LEARNING_BASE_URL + "/system/v3/content/update/" + courseId;
        HttpResponse<String> httpResponse = Unirest.patch(url).header("Content-Type", "application/json").body(mapper.writeValueAsString(payload)).asString();
        Response response = mapper.readValue(httpResponse.getBody(), Response.class);
        if(200 != response.getResponseCode().code()){
            System.err.println("Error while reading content from KP : " + courseId + " : " + response.getParams().getStatus() + " : " + response.getResult());
            throw new ServerException("ERR_COURSE_BATCH_SAMZA", "Error while updating metadata content from KP : " + courseId + " : " + response.getParams().getStatus() + " : " + response.getResult());
        }
    }

}
