/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.utils;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.body.Body;
import com.mashape.unirest.request.body.RequestBodyEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.tools.loader.service.ExecutionContext;
import org.json.JSONObject;

/**
 *
 * @author feroz
 */
public class RestUtil {
    
    private static Logger logger = LogManager.getLogger(RestUtil.class);
    private static final String CHANNEL = "channel";
    
    public static void init(ExecutionContext context, String tokenKey) {
        String apiKey = context.getString(tokenKey);
        String channel = context.getString(CHANNEL);
        String userId = context.getCurrentUser();
        
        Unirest.setDefaultHeader("Content-Type", "application/json");
        Unirest.setDefaultHeader("accept", "application/json");
        Unirest.setDefaultHeader("X-Channel-id", channel);
        Unirest.setDefaultHeader("X-Authenticated-Userid", userId);
        Unirest.setDefaultHeader("Authorization", "Bearer " + apiKey);
    }
    
    public static HttpResponse<JsonNode> execute(BaseRequest request) throws Exception {
        if (logger.isTraceEnabled()) {
            logger.trace(request.getHttpRequest().getUrl());
            
            Body body = request.getHttpRequest().getBody();
            if ((body != null) && (body instanceof RequestBodyEntity)) {
                RequestBodyEntity rbody = (RequestBodyEntity) body;
                logger.trace(rbody.getBody());
            }
        }
        
        HttpResponse<JsonNode> response = request.asJson();
        
        if (logger.isTraceEnabled()) {
            logger.trace("Response: " + response.getStatusText() + " : " + response.getBody());
        }
        
        return response;
    }
    
    public static String getFromResponse(HttpResponse<JsonNode> resp, String key) {
        String[] nestedKeys = key.split("\\.");
        JSONObject obj = resp.getBody().getObject();
        
        for (int i = 0; i < nestedKeys.length - 1; i++) {
            String nestedKey = nestedKeys[i];
            if (obj.has(nestedKey)) obj = obj.getJSONObject(nestedKey);
        }
        
        String val = obj.getString(nestedKeys[nestedKeys.length - 1]);
        return val;
    }
    
    public static boolean isSuccessful(HttpResponse<JsonNode> resp) {
        int status = resp.getStatus();
        String code = resp.getBody().getObject().getString("responseCode");
        return ((status == 200) && (code.equals("OK")));
    }
}
