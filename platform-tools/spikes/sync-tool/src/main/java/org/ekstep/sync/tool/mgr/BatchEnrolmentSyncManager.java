package org.ekstep.sync.tool.mgr;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.common.Platform;
import org.ekstep.kafka.KafkaClient;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class BatchEnrolmentSyncManager {

    private static ObjectMapper mapper = new ObjectMapper();
    private static int batchSize = Platform.config.hasPath("batch.size") ? Platform.config.getInt("batch.size"): 50;
    Map<String, String> esIndexObjecTypeMap = new HashMap<String, String>() {{
        put("course-batch", "course-batch");
        put("user-courses", "user-courses");
    }};

    private static Map<String, String> tableObjecTypeMap = new HashMap<String, String>() {{
        put("course-batch", "course_batch");
        put("user-courses", "user_courses");
    }};
    private static final String KAFKA_TOPIC = Platform.config.hasPath("courses.topic")? Platform.config.getString("courses.topic"): "local.coursebatch.job.request";

    private static final String keyspace = Platform.config.hasPath("courses.keyspace.name") ? Platform.config.getString("courses.keyspace.name"): "sunbird_courses";
    public void sync(String objectType, String offset, String limit, String resetProgress) throws Exception {
        String index = esIndexObjecTypeMap.get(objectType);
        ElasticSearchUtil.initialiseESClient(index, Platform.config.getString("search.es_conn_info"));

        //FetchData from cassandra
        int lmt = (StringUtils.isNotBlank(limit)) ? Integer.parseInt(limit) : 0;
        List<Row> rows = read(tableObjecTypeMap.get(objectType), lmt);
        System.out.println("Number of rows to be synced : " + rows.size());
        //Prepare ES Docs
        List<String> docids = new ArrayList<>();
        if(StringUtils.equalsIgnoreCase("course-batch", objectType))
            docids = Arrays.asList("batchId");
        if(StringUtils.equalsIgnoreCase("user-courses", objectType)){
            docids = Arrays.asList("batchId", "userID");
        }

        pushDocsToES(rows, docids, index);
        //TODO: If resetProgress Push the events to kafka
        if(StringUtils.equalsIgnoreCase("user-courses", objectType) && Boolean.valueOf(resetProgress)){
            System.out.println("-----------------------------------------");
            System.out.println("Pushing the events to kafka");
            pushEventsToKafka(rows);
            System.out.println("-----------------------------------------");
        }

    }

    private void pushEventsToKafka(List<Row> rows) throws Exception {
        long startTime = System.currentTimeMillis();
        long total = ((Number) rows.size()).longValue();
        long current = 0;
        for(Row row: rows) {
            Map<String, Object> rowMap = mapper.readValue(row.getString("[json]"), Map.class);
            String event = generatekafkaEvent(rowMap);
            KafkaClient.send(event, KAFKA_TOPIC);
            current += 1;
            printProgress(startTime, total, current);
        }

    }

    private String generatekafkaEvent(Map<String, Object> rowMap) throws JsonProcessingException {
        Map<String, Object> event = new HashMap<String, Object>() {{
            put("eid", "BE_JOB_REQUEST");
            put("ets", System.currentTimeMillis());
            put("mid", "LP." + System.currentTimeMillis() +"." + UUID.randomUUID());
            put("actor", new HashMap<String, Object>(){{
                put("type", "System");
                put("id", "Course Batch Updater");
            }});
            put("context", new HashMap<String, Object>(){{
                put("pdata", new HashMap<String, Object>(){{
                    put("id", "org.sunbird.platform");
                    put("ver", "1.0");
                }});
            }});
            put("object", new HashMap<String, Object>(){{
                put("type", "CourseBatchEnrolment");
                put("id", rowMap.get("batchid") + "_" + rowMap.get("userid"));
            }});
            put("edata", new HashMap<String, Object>(){{
                put("action", "batch-enrolment-sync");
                put("iteration", 1);
                put("batchId", rowMap.get("batchid"));
                put("userId", rowMap.get("userid"));
                put("courseID", rowMap.get("courseid"));
                put("reset", Arrays.asList("contentStatus","completionPercentage","status","progress"));
            }});
        }};

        return mapper.writeValueAsString(event);

    }

    private void pushDocsToES(List<Row> rows, List<String> docids, String index) throws Exception {
        List<Row> rowClone = new ArrayList<>();
        rowClone.addAll(rows);
        long startTime = System.currentTimeMillis();
        long total = ((Number) rows.size()).longValue();
        long current = 0;
        while (CollectionUtils.isNotEmpty(rowClone)){
            Map<String , Object> esDocs = new HashMap<>();
            int currentBatchSize = (rowClone.size() >= batchSize) ? batchSize : rowClone.size();
            List<Row> dbRows = rowClone.subList(0, currentBatchSize);

            for(Row row : dbRows) {
                String docString = row.getString("[json]");
                Map<String, Object> docMap = mapper.readValue(docString, Map.class);
                String docId = docids.stream().map(key -> (String) docMap.get(key.toLowerCase())).collect(Collectors.toList())
                        .stream().collect(Collectors.joining("_"));
                esDocs.put(docId, docMap);
            }
            if(MapUtils.isNotEmpty(esDocs)) {
                ElasticSearchUtil.bulkIndexWithIndexId(index, "_doc", esDocs);
            }
            current +=dbRows.size();
            printProgress(startTime, total, current);
            System.out.println("DocIds synced : " + esDocs.keySet());
            rowClone.subList(0, currentBatchSize).clear();
        }
    }

    private static List<Row> read(String table, int limit) {
        Session session = CassandraConnector.getSession("platform-courses");
        Select selectQuery = QueryBuilder.select().json().all().from(keyspace, table);
        if(limit != 0)
            selectQuery.limit(limit);
        ResultSet results = session.execute(selectQuery);
        return results.all();
    }

    private static void printProgress(long startTime, long total, long current) {
        long eta = current == 0 ? 0 :
                (total - current) * (System.currentTimeMillis() - startTime) / current;

        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        StringBuilder string = new StringBuilder(140);
        int percent = (int) (current * 100 / total);
        string
                .append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
                .append(String.format(" %d%% [", percent))
                .append(String.join("", Collections.nCopies(percent, "=")))
                .append('>')
                .append(String.join("", Collections.nCopies(100 - percent, " ")))
                .append(']')
                .append(String.join("", Collections.nCopies((int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
                .append(String.format(" %d/%d, ETA: %s", current, total, etaHms));

        System.out.print(string);
    }
}
