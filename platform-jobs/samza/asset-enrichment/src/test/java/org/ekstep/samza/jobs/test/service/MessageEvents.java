package org.ekstep.samza.jobs.test.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.awt.image.ImagingOpException;
import java.util.Map;

public class MessageEvents {

    public static final String SUCCESS_UPLOAD_STREAMURL_EVENT = "{\"eid\":\"BE_JOB_REQUEST\",\"ets\":1554283240056,\"mid\":\"LP.1554283240056.ed300bc9-af90-4c89-966a-95c6a1deba34\",\"actor\":{\"id\":\"Asset Enrichment Samza Job\",\"type\":\"System\"},\"context\":{\"pdata\":{\"ver\":\"1.0\",\"id\":\"org.ekstep.platform\"},\"channel\":\"in.ekstep\",\"env\":\"dev\"},\"object\":{\"ver\":\"1554283240006\",\"id\":\"do_112732688163201024117\"},\"edata\":{\"action\":\"assetenrichment\",\"iteration\":1,\"mediaType\":\"video\",\"contentType\":\"Asset\",\"status\":\"Processing\"}}";
    public static final String EMPTY_JSON = "{}";
    public static final ObjectMapper mapper = new ObjectMapper();

    public static Map<String, Object> getMap(String message) {
        Map map = null;
        try {
            map = mapper.readValue(message, new TypeReference<Map<String,Object>>(){});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
