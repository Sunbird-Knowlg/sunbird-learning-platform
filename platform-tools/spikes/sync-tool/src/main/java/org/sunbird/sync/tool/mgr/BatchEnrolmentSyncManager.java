package org.sunbird.sync.tool.mgr;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.cassandra.connector.util.CassandraConnector;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Response;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.kafka.KafkaClient;
import org.sunbird.learning.util.ControllerUtil;
import org.sunbird.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.sync.tool.util.CassandraColumns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class BatchEnrolmentSyncManager {

    @Autowired
    private Neo4jESSyncManager neo4jESSyncManager;
    
    private static ObjectMapper mapper = new ObjectMapper();
    private static int batchSize = Platform.config.hasPath("batch.size") ? Platform.config.getInt("batch.size"): 50;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static List<String>  changeInSimpleDateFormat = Arrays.asList("startDate", "endDate", "enrollmentEndDate", "createdDate", "updatedDate");
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
    
    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    }
    
    public void sync(String objectType, String offset, String limit, String resetProgress, String[] batchIds, String[] courseIds) throws Exception {
        if(objectType.equalsIgnoreCase("batch-detail-update")){
            updateBatchDetailsForCourses(courseIds, objectType);
        } else {
            String index = esIndexObjecTypeMap.get(objectType);
            ElasticSearchUtil.initialiseESClient(index, Platform.config.getString("search.lms_es_conn_info"));

            //FetchData from cassandra
            int lmt = (StringUtils.isNotBlank(limit)) ? Integer.parseInt(limit) : 0;
            List<Row> rows = read(tableObjecTypeMap.get(objectType), lmt, batchIds);
            System.out.println("Number of rows to be synced : " + rows.size());
            //Prepare ES Docs
            List<String> docids = new ArrayList<>();
            if(StringUtils.equalsIgnoreCase("course-batch", objectType))
                docids = Arrays.asList("batchId");
            if(StringUtils.equalsIgnoreCase("user-courses", objectType)){
                docids = Arrays.asList("batchId", "userId");
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
    }

    private void pushEventsToKafka(List<Row> rows) throws Exception {
        long startTime = System.currentTimeMillis();
        long total = ((Number) rows.size()).longValue();
        long current = 0;
        for(Row row: rows) {
            Map<String, Object> rowMap = mapper.readValue(row.getString("[json]"), Map.class);
            String enrolSyncEvent = generateBatchSyncKafkaEvent(rowMap);
            KafkaClient.send(enrolSyncEvent, KAFKA_TOPIC);
            current += 1;
            printProgress(startTime, total, current);
        }

    }

    public String generateBatchSyncKafkaEvent(Map<String, Object> rowMap) throws JsonProcessingException {
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
                put("courseId", rowMap.get("courseid"));
                put("reset", Arrays.asList("completionPercentage","status","progress"));
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

                System.out.println(docMap);
                Map<String, Object> esDoc = new HashMap<>();
                for(String key : docMap.keySet()){
                    String esKey = CassandraColumns.COLUMNS.get(key);
                    if (StringUtils.isBlank(esKey))
                        esKey = key;
                    esDoc.put(esKey, docMap.get(key));
                }
                esDoc.put("startDate", getDate("start_date", "startdate", docMap));
                esDoc.put("endDate", getDate("end_date", "enddate", docMap));
                esDoc.put("enrollmentEndDate", getDate("enrollment_enddate", "enrollmentenddate", docMap));
                mapCertTemplate(esDoc);
                esDoc.put("identifier", docId);
                esDoc.put("id", docId);
                esDocs.put(docId, esDoc);
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

    private static List<Row> read(String table, int limit, String[] batchIds) {
        Session session = CassandraConnector.getSession("platform-courses");
        Select.Where selectQuery = null;
        if(null != batchIds && batchIds.length > 0 && StringUtils.equalsIgnoreCase("user_courses", table)){
            selectQuery = QueryBuilder.select().json().all().from(keyspace, table).where(QueryBuilder.in("batchid", batchIds));
        } else{
            selectQuery = QueryBuilder.select().json().all().from(keyspace, table).where();
        }
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

    private void updateBatchDetailsForCourses(String[] courseIds, String objectType) throws Exception {
        List<Row> rows = readBatch(courseIds);
        System.out.println("Rows :: " + rows);
        
        if(!rows.isEmpty()){
            Map<String, List<Map<String, Object>>> courseMetadata = new HashMap<>();
            rows.stream().filter(row -> 2 > row.getInt("status")).forEach(row -> {
                String courseId = row.getString("courseid");
                Map<String, Object> batchDetails = new HashMap<String, Object>() {{
                    put("batchId", row.getString("batchid"));
                    put("name", row.getString("name"));
                    put("status", row.getInt("status"));
                    put("startDate", getDate("start_date", "startdate", row));
                    put("endDate", getDate("end_date", "enddate", row));
                    put("enrollmentEndDate", getDate("enrollment_enddate", "enrollmentenddate", row));
                    put("enrollmentType", row.getString("enrollmenttype"));
                    put("createdFor", row.getList("createdfor", String.class));
                }};
                if (null != courseMetadata.get(courseId)) {
                    ((List<Map<String, Object>>) courseMetadata.get(courseId)).add(batchDetails);
                } else {
                    List<Map<String, Object>> batchDetailList = new ArrayList<>();
                    batchDetailList.add(batchDetails);
                    courseMetadata.put(courseId, batchDetailList);
                }
            });
                ControllerUtil util = new ControllerUtil();
                Response response = util.getDataNodes("domain", Arrays.asList(courseIds));
                if(null != response){
                    List<Node> nodeList = (List<Node>) response.get("node_list");
                    if(CollectionUtils.isNotEmpty(nodeList)){
                        nodeList.stream().forEach(node -> {
                            if(MapUtils.isNotEmpty(courseMetadata))
                                node.getMetadata().put("batches", courseMetadata.get(node.getIdentifier()));
                            else
                                node.getMetadata().put("batches", null);
                            Response updateResponse = util.updateNodeWithoutValidation(node);
                            if(util.checkError(updateResponse)){
                                System.out.println("Update failed for courseId: " + node.getIdentifier() + " :: "
                                        + updateResponse.getParams().getErr() + " :: " + updateResponse.getParams().getErrmsg() + " :: " + updateResponse.getResult());
                            }
                        });
                        List<String> ids = nodeList.stream().map(node -> node.getIdentifier()).collect(Collectors.toList());
                        System.out.println("Identifiers which are updated are: " + ids);
                        neo4jESSyncManager.syncByIds("domain", ids);
                    } else {
                        System.out.println("No courses found");
                    }
                } else {
                    System.out.println("Unable to fetch courses for update");
                }
        } else {
            System.out.println("No data found to be updated");
        }
    }

    public List<Row> readBatch(String[] courseIds) {
        Session session = CassandraConnector.getSession("platform-courses");
        Select.Where selectQuery = null;
        if(null != courseIds && courseIds.length > 0){
            selectQuery = QueryBuilder.select().all().from(keyspace, "course_batch").where(QueryBuilder.in("courseid", courseIds));
        } else{
            selectQuery = QueryBuilder.select().all().from(keyspace, "course_batch").where();
        }
        ResultSet results = session.execute(selectQuery);
        return results.all();
    }

    public List<Row> readEnrolment(String userId, String batchId) {
        Session session = CassandraConnector.getSession("platform-courses");
        Select.Where selectQuery = QueryBuilder.select().json().all().from(keyspace, "user_enrolments").where(QueryBuilder.eq("userid", userId));
        ResultSet results = session.execute(selectQuery);
        return results.all();
    }
    
    public String getDate(String columnName, String oldColumnName, Row row) {
        if(null != row.getTimestamp(columnName)) {
            return dateFormat.format(row.getTimestamp(columnName));
        } else {
            return row.getString(oldColumnName);
        }
        
    }

    public String getDate(String columnName, String oldColumnName, Map<String, Object> docMap) throws Exception {
        if(null != docMap.get(columnName)) {
            return dateFormat.format(dateFormat.parse((String)docMap.get(columnName)));
        } else {
            return (String)docMap.get(oldColumnName);
        }

    }

    private Map<String, Object> mapCertTemplate(Map<String, Object> courseBatch) throws Exception {
        Map<String, Map<String, Object>> certificateTemplates =
                (Map<String, Map<String, Object>>)
                        courseBatch.getOrDefault("certTemplates", null);
        if(MapUtils.isNotEmpty(certificateTemplates)){
            certificateTemplates
                    .entrySet()
                    .stream()
                    .forEach(
                            cert_template ->
                                    certificateTemplates.put(
                                            cert_template.getKey(), mapToObject(cert_template.getValue())));
            courseBatch.put("cert_templates", certificateTemplates);
        }
        return courseBatch;
    }

    private Map<String, Object> mapToObject(Map<String, Object> template) {
        try {
            template.put(
                    "criteria",
                    mapper.readValue(
                            (String) template.get("criteria"),
                            new TypeReference<HashMap<String, Object>>() {
                            }));
            if (StringUtils.isNotEmpty((String) template.get("signatoryList"))) {
                template.put(
                        "signatoryList",
                        mapper.readValue(
                                (String) template.get("signatoryList"),
                                new TypeReference<List<Object>>() {
                                }));
            }
            if (StringUtils.isNotEmpty((String) template.get("issuer"))) {
                template.put(
                        "issuer",
                        mapper.readValue(
                                (String) template.get("issuer"),
                                new TypeReference<HashMap<String, Object>>() {
                                }));
            }
            if (StringUtils.isNotEmpty((String) template.get("notifyTemplate"))) {
                template.put(
                        "notifyTemplate",
                        mapper.readValue(
                                (String) template.get("notifyTemplate"),
                                new TypeReference<HashMap<String, Object>>() {
                                }));
            }
        }catch (Exception e) {}
        return template;
    }
}
