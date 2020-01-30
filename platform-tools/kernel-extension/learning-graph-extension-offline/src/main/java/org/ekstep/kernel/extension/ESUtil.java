package org.ekstep.kernel.extension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;

import java.util.HashMap;
import java.util.Map;

public class ESUtil {

    private static final String BASE_URL = Platform.config.hasPath("lp_es_ip_port")? Platform.config.getString("lp_es_ip_port"): "http://localhost:9200";
    private static ObjectMapper mapper = new ObjectMapper();

    public static void addIndex(String index, String type, String settings, String mappings) throws Exception {
        String url = BASE_URL + "/" + index;
        Map<String, Object> payload = new HashMap<String, Object>() {{
            if(StringUtils.isNotBlank(settings)) {
                put("settings", mapper.readValue(settings, Map.class));
            }
            if(StringUtils.isNotBlank(mappings)) {
                put("mappings", mapper.readValue(mappings, Map.class));
            }
        }};
        HttpResponse<String> httpResponse = Unirest.put(url).header("Content-Type", "application/json")
                .body(mapper.writeValueAsString(payload)).asString();
    }

    public static String getDocumentAsStringById(String index, String type, String id) throws Exception {
        String url = BASE_URL + "/" + index + "/" + type + "/" + id;
        HttpResponse<String> httpResponse = Unirest.get(url).header("Content-Type", "application/json").asString();
        if(200 == httpResponse.getStatus()) {
            Map<String, Object> doc = mapper.readValue(httpResponse.getBody(), new TypeReference<Map<String, Object>>(){});
            return mapper.writeValueAsString(doc.get("_source"));
        }
        return null;
    }

    public static void addDocumentWithId(String index, String type, String uniqueId, String jsonIndexDocument) throws Exception {
        String url = BASE_URL + "/" + index + "/" + type + "/" + uniqueId;
        Unirest.put(url).header("Content-Type", "application/json")
                .body(jsonIndexDocument).asString();
    }

    public static void deleteDocument(String index, String type, String uniqueId) {
        String url = BASE_URL + "/" + index + "/" + type + "/" + uniqueId;
        Unirest.delete(url);
    }
}
