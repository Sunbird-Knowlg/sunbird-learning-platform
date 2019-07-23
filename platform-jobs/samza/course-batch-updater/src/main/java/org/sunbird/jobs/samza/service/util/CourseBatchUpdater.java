package org.sunbird.jobs.samza.service.util;

import com.datastax.driver.core.Row;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ServerException;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.jobs.samza.util.ESUtil;
import org.sunbird.jobs.samza.util.SunbirdCassandraUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CourseBatchUpdater {

    private ObjectMapper mapper = new ObjectMapper();

    private static final String KP_LEARNING_BASE_URL = Platform.config.getString("kp.learning_service.base_url");
    private String keyspace = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name")
            : "sunbird_courses";
    private String table = "user_courses";
    private static final String ES_INDEX_NAME = "user-courses";
    private static final String ES_DOC_TYPE = "_doc";


    public CourseBatchUpdater() {
        ElasticSearchUtil.initialiseESClient(ES_INDEX_NAME, Platform.config.getString("search.es_conn_info"));
    }

    public void updateBatchStatus(Map<String, Object> edata) throws Exception {
        //Get data from content read
        String courseId = (String) edata.get("courseId");
        Map<String, Object> content = getContent(courseId);
        List<String> leafNodes = (List<String>) content.get("leafNodes");
        //Compute status
        Map<String, Object> data = getStatusUpdateData(edata, leafNodes);
        if(MapUtils.isNotEmpty(data)) {
            Map<String, Object> dataToUpdate = new HashMap<String, Object>() {{
                put("contentStatus", data.get("contentStatus"));
                put("status", data.get("status"));
                put("completionPercentage", data.get("completionPercentage"));
            }};
            Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
                put("batchid", edata.get("batchId"));
                put("userid", edata.get("userId"));
            }};
            //Update cassandra
            SunbirdCassandraUtil.update(keyspace, table, dataToUpdate, dataToSelect);

            dataToUpdate.put("contentStatus", mapper.writeValueAsString(data.get("contentStatus")));
            ESUtil.updateCoureBatch(ES_INDEX_NAME, ES_DOC_TYPE, dataToUpdate, dataToSelect);
        }
    }

    private Map<String,Object> getContent(String courseId) throws Exception {
       HttpResponse<String> httpResponse = Unirest.get(KP_LEARNING_BASE_URL + "/content/v3/read/" + courseId +"?fields=leafNodes")
               .header("Content-Type", "application/json").asString();
       if(200 != httpResponse.getStatus()){
           System.err.println("Error while reading content from KP : " + courseId + " : " + httpResponse.getStatus() + " : " + httpResponse.getBody());
           throw new ServerException("ERR_COURSE_BATCH_SAMZA", "Error while reading content from KP : " + courseId + " : " + httpResponse.getStatus() + " : " + httpResponse.getBody());
       }
        Response response = mapper.readValue(httpResponse.getBody(), Response.class);
        Map<String, Object> content = (Map<String, Object>) response.getResult().get("content");
        if(CollectionUtils.isEmpty((List<String>) content.get("leafNodes"))){
            System.err.println("Content does not have leafNodes : " + courseId);
            throw new ServerException("ERR_COURSE_BATCH_SAMZA", "Content does not have leafNodes : " + courseId);
        }

       return content;
    }

    private Map<String,Object> getStatusUpdateData(Map<String, Object> edata, List<String> leafNodes) throws IOException {
        List<Map<String, Object>> contents = (List<Map<String, Object>>) edata.get("contents");
        if(CollectionUtils.isNotEmpty(contents)) {
            Map<String, Object> contentStatus = new HashMap<>();
            Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
                put("batchid", edata.get("batchId"));
                put("userid", edata.get("userId"));
            }};
            List<Row> rows =  SunbirdCassandraUtil.read(keyspace, table, dataToSelect);
            if(CollectionUtils.isNotEmpty(rows)){
                Map<String, Integer> contentStatusMap =  rows.get(0).getMap("contentStatus", String.class, Integer.class);
                if(MapUtils.isNotEmpty(contentStatusMap))
                    contentStatus.putAll(contentStatusMap);
            }

            contentStatus.putAll(contents.stream().collect(Collectors.toMap(c -> (String) c.get("contentId"),c -> c.get("status"))));

            List<String> completedIds = contentStatus.entrySet().stream()
                    .filter(entry -> (2 == ((Number) entry.getValue()).intValue()))
                    .map(entry -> entry.getKey()).distinct().collect(Collectors.toList());

            int size = CollectionUtils.intersection(completedIds, leafNodes).size();
            double completionPercentage = (((Number)size).doubleValue()/((Number)leafNodes.size()).doubleValue())*100;

            int status = (size == leafNodes.size()) ? 2 : 1;

            return new HashMap<String, Object>() {{
                put("contentStatus", contentStatus);
                put("status", status);
                put("completionPercentage", ((Number)completionPercentage).intValue());
                put("progress", size);
            }};
        }
       return null;
    }

}
