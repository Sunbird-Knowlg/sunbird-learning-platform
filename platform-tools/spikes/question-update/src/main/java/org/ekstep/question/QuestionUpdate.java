package org.ekstep.question;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionUpdate {

    private static ObjectMapper mapper = new ObjectMapper();
    private static String BASE_URL = Platform.config.getString("base_url");

    private static Response getQuestionBody(String id) throws Exception {
        String url = BASE_URL + "/assessment/v3/items/read/" + id;

        HttpResponse<String> httpResponse = Unirest.get(url).asString();
        Response response = mapper.readValue(httpResponse.getBody(), Response.class);
        return response;
    }

    private static String updateBody(String body, String pattern) {
        if(StringUtils.isNotBlank(body))
            body = body.replaceAll(pattern, "");
        return body;
    }

    private static Response updateQuestion(String id, Map<String, Object> request, String channel) throws Exception {
        String url  = BASE_URL + "/assessment/v3/items/update/"+ id;
        HttpResponse<String> httpResponse = Unirest.patch(url).header("Content-Type", "application/json").header("X-Channel-Id", channel).body(mapper
                .writeValueAsString(request)).asString();
        Response response = mapper.readValue(httpResponse.getBody(), Response.class);
        return response;
    }

    private static boolean isSuccess(Response response) {
        boolean success = StringUtils.equals(ResponseParams.StatusType.successful.name(), response.getParams().getStatus());
        if (!success) {
            TelemetryManager.info("API call unsuccessful. ", response.getResult());
        }
        return success;
    }

    public static void main(String[] args) throws Exception {
        if(null != args && args.length > 0 ) {
            List<String> idList = Arrays.asList(args[0].split(","));

            for(String id: idList) {
                correctQuestionData(id);
            }
        } else{
            System.out.println("No Questions to update");
        }

    }

    private static void correctQuestionData(String id) throws Exception {
        Response getQuest = getQuestionBody(id);
        if(isSuccess(getQuest)) {
            Map<String, Object> question = (Map<String, Object>) getQuest.get("assessment_item");
            String body = (String) question.get("body");
            String channel = (String) question.get("channel");
            System.out.println(id + " BODY : " + body);
            body = updateBody(body, Platform.config.getString("question.pattern"));
            question.put("body", body);
            Map<String, Object> request = prepareQuestionRequest(question);
            Response updateResponse = updateQuestion(id, request, channel);

            if(isSuccess(updateResponse))
                System.out.println("Successfully updated Question : " + id);
            else{
                System.out.println("Question " + id  + " update failed for : " + updateResponse.getParams().getErrmsg() + " " + updateResponse.getResult());
            }

        }
    }


    private static Map<String,Object> prepareQuestionRequest(Map<String, Object> question) {
        Map<String, Object> metadata = new HashMap<>(question);
        metadata.remove("identifier");
        metadata.remove("objectType");
        metadata.remove("concepts");
        metadata.remove("subject");

        if(null != question.get("concepts")){
            List<Map<String, Object>> concepts = (List<Map<String, Object>>) question.get("concepts");
            List<Map<String, Object>> outRelations = new ArrayList<>();
            for(Map<String, Object> concept : concepts) {
                Map<String, Object> relation = new HashMap<>();
                relation.put("endNodeId", concept.get("identifier"));
                relation.put("relationType", concept.get("relation"));
                outRelations.add(relation);
            }
            question.put("outRelations", outRelations);
            question.remove("concepts");
        }
        Map<String, Object> item = new HashMap<>();
        item.put("identifier", question.get("identifier"));
        item.put("objectType", "AssessmentItem");
        item.put("metadata", metadata);
        item.put("outRelations", question.get("outRelations"));

        Map<String, Object> assessmentItem = new HashMap<>();
        assessmentItem.put("assessment_item", item);

        Map<String, Object> request = new HashMap<>();
        request.put("request", assessmentItem);
        return request;
    }


}
