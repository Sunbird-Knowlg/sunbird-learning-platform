package org.ekstep.common.mgr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.collections.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AutoReviewerScheduler {

    static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        try {
            while (true) {
                try {
                    makeSearchAPICall();
                    delay(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void makeSearchAPICall() throws Exception {
        String request = "{\n" +
                "    \"request\": {\n" +
                "        \"filters\": {\n" +
                "        \t\"objectType\":\"Content\",\n" +
                "            \"status\": [\"Draft\",\"Review\",\"Live\"],\n" +
                "            \"finalUpdate\": \"Pending\",\n" +
                "            \"contentType\": [\"Collection\",\"TextBook\",\"Course\",\"LessonPlan\",\"Resource\",\"LearningOutcomeDefinition\",\"ExplanationResource\",\"ExperientialResource\"]\n" +
                "        },\n" +
                "        \"exists\":[\"extractedText\", \"finalUpdate\"],\n" +
                "        \"fields\":[\"extractedText\"],\n" +
                "        \"sort_by\":{\"createdOn\":\"desc\"},\n" +
                "        \"limit\":10\n" +
                "    }\n" +
                "}";
        HttpResponse<String> httpResponse = Unirest.post("https://devcon.sunbirded.org/action/composite/v3/search").header("Content-Type", "application/json")
                .body(request).asString();


        if (httpResponse.getStatus() == 200) {
            Map<String, Object> responseMap = mapper.readValue(httpResponse.getBody(), Map.class);
            Map<String, Object> result = (Map<String, Object>) responseMap.get("result");
            if (MapUtils.isNotEmpty(result)) {
                List<Map<String, Object>> contents = ((List<Map<String, Object>>) result.get("content"));
                contents.forEach(content -> {
                    try {
                        String identifier = (String) content.get("identifier");
                        String extractedText = (String) content.get("extractedText");
                        String sysUpdate = languageKeywordsRequest(extractedText);
//                        String sysUpdate = languageKeywordsRequest("A Simple PDF File This is a small demonstration .pdf file - just for use in the Virtual Mechanics tutorials. More text. And more text. And more text. And more text. And more text. And more text. And more text. And more text. And more text. And more text. And more text. Boring, zzzzz. And more text. And more text. And more text. And more text. And more text. And more text. And more text. And more text. And more text. And more text. And more text. And more text. And more text. And more text. And more text. And more text. Even more. Continued on page 2 ... Simple PDF File 2 ...continued from page 1. Yet more text. And more text. And more text. And more text. And more text. And more text. And more text. And more text. Oh, how boring typing this stuff. But not as boring as watching paint dry. And more text. And more text. And more text. And more text. Boring.  More, a little more text. The end, and just as well.");
                        callSystemUpdate(identifier, sysUpdate);
                        delay(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } else {
            System.out.println("Response Body SEARCH : " + httpResponse.getBody());
        }
    }

    public static String languageKeywordsRequest(String text) throws Exception {
        String newText = text.replaceAll("\n", " ");
        String languageRequest = "{\n" +
                "  \"request\": {\n" +
                "    \"language_id\": \"en\",\n" +
                "    \"text\": \" " + newText + "\"\n" +
                "  }\n" +
                "}";
        String systemUpdateTemplate = "";
        List<String> newKeywords = new ArrayList<>();
        Map<String, Object> thresholdVocabulary = new HashMap<>();
        Map<String, Object> nonThresholdVocabulary = new HashMap<>();
        Map<String, Object> partsOfSpeech = new HashMap<>();
        int totalWordCount = 0;

        HttpResponse<String> httpResponseLang = Unirest.post("https://api.ekstep.in//language/v3/tools/text/analysis")
                .headers(new HashMap<String, String>() {{
                    put("Content-Type", "application/json");
                    put("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIyNDUzMTBhZTFlMzc0NzU1ODMxZTExZmQyMGRjMDg0MiIsImlhdCI6bnVsbCwiZXhwIjpudWxsLCJhdWQiOiIiLCJzdWIiOiIifQ.M1H_Z7WvwRPM0suBCofHs7iuDMMHyBjIRd3xGS4hqy8");
                }})
                .body(languageRequest).asString();

        if (httpResponseLang.getStatus() == 200) {
            Map<String, Object> responseMap = mapper.readValue(httpResponseLang.getBody(), Map.class);
            Map<String, Object> result = (Map<String, Object>) ((Map<String, Object>) responseMap.get("result")).get("text_complexity");
            if (MapUtils.isNotEmpty(result)) {
                thresholdVocabulary = ((Map<String, Object>) result.get("thresholdVocabulary"));
                nonThresholdVocabulary = ((Map<String, Object>) result.get("nonThresholdVocabulary"));
                partsOfSpeech = ((Map<String, Object>) result.get("partsOfSpeech"));
                totalWordCount = ((Number) result.get("totalWordCount")).intValue();

            }
        } else {
            System.out.println("Response Body Language: " + httpResponseLang.getBody());
        }


        HttpResponse<String> httpResponseKey = Unirest.post("https://api.aylien.com/api/v1/entities")
                .headers(new HashMap<String, String>() {{
                    put("X-AYLIEN-TextAPI-Application-Key", "7f6ddf5416da2b022145472610f03f08");
                    put("X-AYLIEN-TextAPI-Application-ID", "68f7c606");
                    put("Content-Type", "application/x-www-form-urlencoded");

                }})
                .body("text=" + text + "")
                .asString();

        if (httpResponseKey.getStatus() == 200) {
            Map<String, Object> responseMap = mapper.readValue(httpResponseKey.getBody(), Map.class);
            Map<String, Object> entities = (Map<String, Object>) responseMap.get("entities");
            if (MapUtils.isNotEmpty(entities)) {
                List<String> keywords = (List<String>) entities.get("keyword");
                newKeywords = keywords.stream().map(key -> "\"" + key + "\"").collect(Collectors.toList());
            }
        } else {
            System.out.println("Response Body Keywords: " + httpResponseKey.getBody());
        }

        systemUpdateTemplate = "{\n" +
                "    \"request\": {\n" +
                "        \"content\": {\n" +
                "            \"finalUpdate\": \"Passed\"," +
                "            \"ckp_lng_analysis\": {\n" +
                "                \"name\": \"Language Analysis\",\n" +
                "                \"type\": \"ln_analysis\",\n" +
                "                \"status\": \"Passed\",\n" +
                "                \"result\": {\n" +
                "                    \"totalWordCount\": " + totalWordCount + ",\n" +
                "                    \"partsOfSpeech\": " + partsOfSpeech + ",\n" +
                "                    \"thresholdVocabulary\": " + thresholdVocabulary + ",\n" +
                "                    \"nonThresholdVocabulary\": " + nonThresholdVocabulary + "\n" +
                "                }\n" +
                "            },\n" +
                "            \"ckp_keywords\": {\n" +
                "\t\t\"name\":\"Suggested Keywords\",\n" +
                "\t\t\"type\":\"keywords\",\n" +
                "\t\t\"status\":\"Passed\",\n" +
                "\t\t\"result\": " + newKeywords + "\n" +
                "}\n" +
                "        }\n" +
                "    }\n" +
                "}";
        systemUpdateTemplate = systemUpdateTemplate.replaceAll("([A-Za-z%]+)=([0-9.]+)", "\"$1\":$2");
        return systemUpdateTemplate;
    }


    public static void callSystemUpdate(String identifier, String systemUpdateRequest) throws Exception {
        HttpResponse<String> httpResponse = Unirest.patch("http://50.1.0.5:8080/learning-service//system/v3/content/update/" + identifier)
                .headers(new HashMap<String, String>() {{
                    put("Content-Type", "application/json");
                    put("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIyNDUzMTBhZTFlMzc0NzU1ODMxZTExZmQyMGRjMDg0MiIsImlhdCI6bnVsbCwiZXhwIjpudWxsLCJhdWQiOiIiLCJzdWIiOiIifQ.M1H_Z7WvwRPM0suBCofHs7iuDMMHyBjIRd3xGS4hqy8");
                }})
                .body(systemUpdateRequest).asString();
        if (httpResponse.getStatus() == 200) {
            System.out.println("System Update done for : " + identifier);
        } else {
            System.out.println("System Update failed for : " + identifier);
        }
    }

    public static void delay(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
