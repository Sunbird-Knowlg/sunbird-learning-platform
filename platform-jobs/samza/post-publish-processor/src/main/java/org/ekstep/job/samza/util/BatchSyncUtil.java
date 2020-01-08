package org.ekstep.job.samza.util;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.util.SunbirdCassandraUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BatchSyncUtil {

    private static ObjectMapper mapper = new ObjectMapper();
    private static final String keyspace = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name"): "sunbird_courses";

    private static final String KAFKA_TOPIC = Platform.config.hasPath("courses.topic")
            ? Platform.config.getString("courses.topic"): "local.coursebatch.job.request";

    private static JobLogger LOGGER = new JobLogger(BatchSyncUtil.class);


    public void syncCourseBatch(String courseId, MessageCollector collector) {
        //Get Coursebatch from course_batch table using courseId
        List<Row> courseBatchRows = readbatch("course_batch", courseId);

        //For each batch exists. fetch enrollment from user_courses table and push the message to kafka
        for(Row row: courseBatchRows) {
            if(1 == row.getInt("status")) {
                List<Row> userCoursesRows = read("user_courses", Arrays.asList(row.getString("batchid")));
                pushEventsToKafka(userCoursesRows, collector);
                LOGGER.info("Pushed the events to sync courseBatch enrollment for : " + courseId);
            }
        }
    }


    private void pushEventsToKafka(List<Row> rows, MessageCollector collector) {
        for(Row row: rows) {
            try {
                Map<String, Object> rowMap = mapper.readValue(row.getString("[json]"), Map.class);
                Map<String, Object> event = generatekafkaEvent(rowMap);
                collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", KAFKA_TOPIC), event));
            } catch (Exception e) {
                LOGGER.error("Error while pushing the event for course batch enrollment sync", e);
            }

        }
    }


    private static List<Row> read(String table, List<String> batchIds) {
        Session session = CassandraConnector.getSession("sunbird");
        Select.Where selectQuery = null;
        if(null != batchIds && !batchIds.isEmpty() && StringUtils.equalsIgnoreCase("user_courses", table)){
            selectQuery = QueryBuilder.select().json().all().from(keyspace, table).where(QueryBuilder.in("batchid", batchIds));
        } else{
            selectQuery = QueryBuilder.select().json().all().from(keyspace, table).where();
        }
        ResultSet results = session.execute(selectQuery);
        return results.all();
    }

    private static List<Row> readbatch(String table, String courseId) {
        Session session = CassandraConnector.getSession("sunbird");
        Select.Where selectQuery = null;
        if(StringUtils.isNotBlank(courseId)){
            selectQuery = QueryBuilder.select().all().from(keyspace, table).where(QueryBuilder.eq("courseid", courseId));
        } else{
            selectQuery = QueryBuilder.select().all().from(keyspace, table).where();
        }
        ResultSet results = session.execute(selectQuery);
        return results.all();
    }

    private Map<String, Object> generatekafkaEvent(Map<String, Object> rowMap) throws JsonProcessingException {
        return new HashMap<String, Object>() {{
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
    }
}
