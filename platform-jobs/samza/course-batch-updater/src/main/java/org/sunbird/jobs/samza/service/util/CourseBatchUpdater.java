package org.sunbird.jobs.samza.service.util;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ServerException;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.telemetry.util.LogTelemetryEventUtil;
import org.sunbird.jobs.samza.task.CourseProgressHandler;
import org.sunbird.jobs.samza.util.CourseBatchParams;
import org.sunbird.jobs.samza.util.SunbirdCassandraUtil;
import redis.clients.jedis.Jedis;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CourseBatchUpdater extends BaseCourseBatchUpdater {

    private static JobLogger LOGGER = new JobLogger(CourseBatchUpdater.class);
    private String keyspace = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name")
            : "sunbird_courses";
    private String table = "user_enrolments";
  /*  private static final String ES_INDEX_NAME = "user-courses";
    private static final String ES_DOC_TYPE = "_doc";*/
    private Jedis redisConnect= null;
    private Session cassandraSession = null;
    private SystemStream certificateInstructionStream = null;

    public CourseBatchUpdater(Jedis redisConnect, Session cassandraSession) {
        //ElasticSearchUtil.initialiseESClient(ES_INDEX_NAME, Platform.config.getString("search.es_conn_info"));
        this.redisConnect = redisConnect;
        this.cassandraSession = cassandraSession;
    }

    public CourseBatchUpdater(Jedis redisConnect, Session cassandraSession, SystemStream certificateInstructionStream) {
       // ElasticSearchUtil.initialiseESClient(ES_INDEX_NAME, Platform.config.getString("search.es_conn_info"));
        this.redisConnect = redisConnect;
        this.cassandraSession = cassandraSession;
        this.certificateInstructionStream = certificateInstructionStream;
    }

    public void updateBatchStatus(Map<String, Object> edata, CourseProgressHandler courseProgressHandler, MessageCollector collector) throws Exception {
        //Get data from content read
        String courseId = (String) edata.get("courseId");
        List<String> leafNodes = getLeafNodes(courseId);
        if(CollectionUtils.isEmpty(leafNodes)){
            LOGGER.info("Content does not have leafNodes. So, skipped processing for : " + courseId);
        } else {
            //Compute status
            updateData(edata, leafNodes, courseProgressHandler, collector);
        }
    }

    private List<String> getLeafNodes(String courseId) throws Exception {
        String key = courseId + ":" + courseId + ":leafnodes";
        List<String> leafNodes = getStringList(key);
        if (CollectionUtils.isEmpty(leafNodes)) {
            LOGGER.info("Cache not found from redis. Fetching from content read: " + courseId);
            Map<String, Object> content = getContent(courseId, "leafNodes");
            leafNodes = (List<String>) content.getOrDefault("leafNodes", new ArrayList<String>());
        }
        return leafNodes;
    }

    private void updateData(Map<String, Object> edata, List<String> leafNodes, CourseProgressHandler courseProgressHandler, MessageCollector collector)  throws Exception {
        List<Map<String, Object>> contents = (List<Map<String, Object>>) edata.get("contents");
        String batchId = (String)edata.get("batchId");
        String userId = (String)edata.get("userId");
        String courseId = (String) edata.get("courseId");

        if(CollectionUtils.isNotEmpty(contents)) {
            Map<String, Object> contentStatus = new HashMap<>();
            Map<String, Object> contentStatusDelta = new HashMap<>();
            Map<String, Object> lastReadContentStats = new HashMap<>(); 
            String key = courseProgressHandler.getKey(batchId, userId);
            if(courseProgressHandler.containsKey(key)) {// Get Progress from the unprocessed list
                populateContentStatusFromHandler(key, courseId, courseProgressHandler, contentStatus, contentStatusDelta, lastReadContentStats);
            } else { // Get progress from cassandra
               Boolean enrolled = populateContentStatusFromDB(batchId, courseId, userId, contentStatus, lastReadContentStats);
               if (!enrolled) {
                   LOGGER.warn("User not enrolled to batch: " + batchId + " :: userId: " + userId + " :: courseId: " + courseId);
                   Map<String, Object> propertiesToUpdate = new HashMap<String, Object>() {{
                    put("addedBy", "System");
                    put("enrolledDate", getDateFormatter().format(new Date()));
                    put("status", 1);
                    put("active", true);
                    put("dateTime", new Timestamp(new Date().getTime()));
                   }};
                   Map<String, Object> propertiesToSelect = new HashMap<String, Object>() {{
                       put("courseid", courseId);
                       put("batchid", batchId);
                       put("userid", userId);
                   }};
                   SunbirdCassandraUtil.update(cassandraSession, keyspace, table, propertiesToUpdate, propertiesToSelect);
                   LOGGER.info("User auto-enrolled to batch: " + batchId + " :: userId: " + userId + " :: courseId: " + courseId);
                   String event = generateAuditEvent(userId, courseId, batchId);
                   LOGGER.info("Audit event: " + event);
                   collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", Platform.config.getString("telemetry.raw.topic")), event));
                   LOGGER.info("Audit event generated and pushed to kafka.");
               }
            }

            contents.forEach(c -> {
                String id = (String) c.get("contentId");
                if(contentStatus.containsKey(id)) {
                    contentStatus.put(id, Math.max(((Integer) contentStatus.get(id)), ((Integer)c.get("status"))));
                    contentStatusDelta.put(id, Math.max(((Integer) contentStatus.get(id)), ((Integer)c.get("status"))));
                } else {
                    contentStatus.put(id, c.get("status"));
                    contentStatusDelta.put(id, c.get("status"));
                }
            });

            List<String> completedIds = contentStatus.entrySet().stream()
                    .filter(entry -> (2 == ((Number) entry.getValue()).intValue()))
                    .map(entry -> entry.getKey()).distinct().collect(Collectors.toList());

            int size = CollectionUtils.intersection(completedIds, leafNodes).size();
            double completionPercentage = (((Number)size).doubleValue()/((Number)leafNodes.size()).doubleValue())*100;

            int status = (size == leafNodes.size()) ? 2 : 1;

            Map<String, Object> dataToUpdate =  new HashMap<String, Object>() {{
                put("courseId", courseId);
                put("contentStatus", contentStatus);
                put("contentStatusDelta", contentStatusDelta);
                put("status", status);
                put("completionPercentage", ((Number)completionPercentage).intValue());
                put("progress", size);
                putAll(lastReadContentStats);
                if(status == 2) {
                    put("completedOn", new Timestamp(new Date().getTime()));
                    put("userCourseBatch", new HashMap<String, Object>() {{
                        put("userId", userId);
                        put("batchId", batchId);
                        put("courseId", courseId);
                    }});
                }
            }};
            courseProgressHandler.put(key, dataToUpdate);
        }
    }

    private String generateAuditEvent(String userId, String collectionId, String batchId) {
        Object[] params = new Object[]{"LP."+System.currentTimeMillis()+"."+UUID.randomUUID(), userId, collectionId, batchId, System.currentTimeMillis()+""};
        String eventTemplate = "|\"eid\":\"AUDIT\",\"ets\":{4},\"ver\":\"3.0\",\"mid\":\"{0}\",\"actor\":|\"id\":\"{1}\",\"type\":\"User\"#,\"context\":|\"channel\":\"ORG_001\",\"pdata\":|\"pid\":\"lms-service\",\"ver\":\"1.0\"#,\"env\":\"CourseBatch\",\"cdata\":[|\"id\":\"{2}\",\"type\":\"Course\"#,|\"id\":\"{3}\",\"type\":\"CourseBatch\"#]#,\"object\":|\"id\":\"{1}\",\"type\":\"User\",\"rollup\":|\"l1\":\"{2}\"##,\"edata\":|\"state\":\"Create\",\"type\":\"enrol\",\"props\":[\"courseId\",\"enrolledDate\",\"userId\",\"batchId\",\"active\"]##";
        String event = MessageFormat.format(eventTemplate, params);
        return event.replaceAll("\\|","{").replaceAll("#","}");
    }

    private boolean populateContentStatusFromDB(String batchId, String courseId, String userId, Map<String, Object> contentStatus, Map<String, Object> lastReadContentStats) {
        Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
            put("batchid", batchId);
            put("userid", userId);
            put("courseid", courseId);

        }};
        ResultSet resultSet =  SunbirdCassandraUtil.read(cassandraSession, keyspace, table, dataToSelect);
        List<Row> rows = resultSet.all();
        if(CollectionUtils.isNotEmpty(rows)){
            Map<String, Integer> contentStatusMap =  rows.get(0).getMap("contentStatus", String.class, Integer.class);
            lastReadContentStats.put("lastReadContentId", rows.get(0).getString("lastreadcontentid"));
            lastReadContentStats.put("lastReadContentStatus", rows.get(0).getInt("lastreadcontentstatus"));
            if(MapUtils.isNotEmpty(contentStatusMap))
                contentStatus.putAll(contentStatusMap);
            return true;
        }
        return false;
    }

    private void populateContentStatusFromHandler(String key, String courseId, CourseProgressHandler courseProgressHandler, Map<String, Object> contentStatus, Map<String, Object> contentStatusDelta, Map<String, Object> lastReadContentStats) {
        Map<String, Object> enrollmentDetails = (Map<String, Object>) courseProgressHandler.get(key);
        if(MapUtils.isNotEmpty(enrollmentDetails)) {
            Map<String, Integer> contentStatusMap = (Map<String, Integer>) enrollmentDetails.get("contentStatus");
            Map<String, Integer> contentStatusDeltaMap = (Map<String, Integer>) enrollmentDetails.getOrDefault("contentStatusDelta", new HashMap<>());
            if(StringUtils.isNotBlank((String) enrollmentDetails.getOrDefault("lastReadContentId", "")))
                lastReadContentStats.put("lastReadContentId", (String) enrollmentDetails.get("lastReadContentId"));
            if(null != enrollmentDetails.get("lastReadContentStatus"))
                lastReadContentStats.put("lastReadContentStatus", enrollmentDetails.get("lastReadContentStatus"));
            if(MapUtils.isNotEmpty(contentStatusMap)){
                contentStatus.putAll(contentStatusMap);
                contentStatusDelta.putAll(contentStatusDeltaMap);
            }
        }
    }

    private static SimpleDateFormat getDateFormatter() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSZ");
        simpleDateFormat.setLenient(false);
        return simpleDateFormat;
    }


    public List<String> getStringList(String key) {
        try {
            LOGGER.info("Redis Host :: " + redisConnect.getClient().getHost() + " \t Redis DB :: " + redisConnect.getDB());
            LOGGER.info("Key used to fetch leafNodes from Redis :: " + key);
            Set<String> set = redisConnect.smembers(key);
            List<String> list = new ArrayList<String>(set);
            return list;
        } catch (Exception e) {
            throw new ServerException("ERR_CACHE_GET_PROPERTY_ERROR", e.getMessage());
        }
    }

    public void updateBatchProgress(Session cassandraSession, CourseProgressHandler courseProgressHandler, List<Map<String, Object>> userCertificateEvents) {
        if (courseProgressHandler.isNotEmpty()) {
            List<Update.Where> updateQueryList = new ArrayList<>();
            courseProgressHandler.getMap().entrySet().forEach(event -> {
                try {
                    String batchId = event.getKey().split("_")[0];
                    String userId = event.getKey().split("_")[1];
                    Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
                        put("batchid", batchId);
                        put("userid", userId);
                    }};
                    Map<String, Object> dataToUpdate = new HashMap<>();
                    dataToUpdate.putAll((Map<String, Object>) event.getValue());
                    dataToSelect.put("courseid", dataToUpdate.remove("courseId"));
                    if(((Number) dataToUpdate.get("status")).intValue() == 2) {
                        int latestProgress = (int) dataToUpdate.getOrDefault("progress", 0);
                        //read status and completedOn from cassandra
                        Map<String, Object> result = readQuery(cassandraSession, dataToSelect);
                        LOGGER.info("CourseBatchUpdater:updateBatchProgress: result:: " + result);
                        if (MapUtils.isNotEmpty(result)) {
                            int dbProgress = (int) result.getOrDefault("progress", 0);
                            Map<String, Object> certEventData = (Map<String, Object>) dataToUpdate.get("userCourseBatch");
                            if (latestProgress > 0) {
                                if (latestProgress > dbProgress)
                                    certEventData.put("reIssue", true);
                                userCertificateEvents.add(certEventData);
                                LOGGER.info("CourseBatchUpdater:updateBatchProgress: auto certificate generation triggered for userId " + userId + " and batchId " + batchId);
                            } else {
                                LOGGER.info("CourseBatchUpdater:updateBatchProgress [2]: status is complete but, auto certificate generation not triggered for userId " + userId + " and batchId " + batchId);
                            }
                        } else {
                            LOGGER.info("CourseBatchUpdater:updateBatchProgress [1]: status is complete but, auto certificate generation not triggered for userId " + userId + " and batchId " + batchId);
                        }
                    }

                    dataToUpdate.remove("userCourseBatch");
                    //Update cassandra
                    updateQueryList.add(updateQuery(keyspace, table, dataToUpdate, dataToSelect));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            //TODO: enhance this to contain only 65k queries in a batch. It is the batch limit from cassandra.
            if(CollectionUtils.isNotEmpty(updateQueryList)){
                Batch batch = QueryBuilder.batch(updateQueryList.toArray(new RegularStatement[updateQueryList.size()]));
                cassandraSession.execute(batch);
            }
           /* try {
                ESUtil.updateBatches(ES_INDEX_NAME, ES_DOC_TYPE, courseProgressHandler.getMap());
            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }
    }

    public void processBatchProgress(Map<String, Object> message, CourseProgressHandler courseProgressHandler, MessageCollector collector) {
        try {
            Map<String, Object> eData = (Map<String, Object>) message.get(CourseBatchParams.edata.name());
            updateBatchStatus(eData, courseProgressHandler, collector);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Update.Where updateQuery(String keyspace, String table, Map<String, Object> propertiesToUpdate, Map<String, Object> propertiesToSelect) {
        Update.Where updateQuery = QueryBuilder.update(keyspace, table).where();
        updateQuery.with(QueryBuilder.putAll("contentStatus", (Map<String, Object>)propertiesToUpdate.getOrDefault("contentStatusDelta", new HashMap<>())));
        propertiesToUpdate.entrySet().stream().filter(entry -> !entry.getKey().contains("contentStatus"))
                .forEach(entry -> updateQuery.with(QueryBuilder.set(entry.getKey(), entry.getValue())));
        propertiesToSelect.entrySet().forEach(entry -> {
            if (entry.getValue() instanceof List)
                updateQuery.and(QueryBuilder.in(entry.getKey(), (List) entry.getValue()));
            else
                updateQuery.and(QueryBuilder.eq(entry.getKey(), entry.getValue()));
        });
        return updateQuery;
    }

    public void pushCertificateEvents(List<Map<String, Object>> userCertificateEvents, MessageCollector collector) {
        userCertificateEvents.stream().forEach(certificateEvent -> {
            try {
                Map<String, Object> updatedCertificateEvent = generateInstructionEvent(certificateEvent);
                LOGGER.info("CourseBatchUpdater:pushCertificateEvents: updatedCertificateEvent mid : " + mapper.writeValueAsString(updatedCertificateEvent.getOrDefault("mid", "")));
                collector.send(new OutgoingMessageEnvelope(certificateInstructionStream, updatedCertificateEvent));
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("CourseBatchUpdater:pushCertificateEvents: push user course certificate event failed: ", e);
            }
        });
    }

    private Map<String, Object> generateInstructionEvent(Map<String, Object> certificateEvent) throws Exception {
        Map<String, Object> actor = new HashMap<>();
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> object = new HashMap<>();
        Map<String, Object> edata = new HashMap<>();

        actor.put(CourseBatchParams.id.name(), "Course Certificate Generator");
        actor.put(CourseBatchParams.type.name(), "System");

        String id = certificateEvent.get("batchId") + "_" + certificateEvent.get("courseId");

        context.put(
                CourseBatchParams.pdata.name(),
                new HashMap<String, Object>() {
                    {
                        put(CourseBatchParams.id.name(), "org.sunbird.platform");
                        put(CourseBatchParams.ver.name(), "1.0");
                    }
                });

        object.put(CourseBatchParams.id.name(), id);
        object.put(CourseBatchParams.type.name(), "CourseCertificateGeneration");

        boolean reIssue = (Boolean) certificateEvent.getOrDefault("reIssue", false);

        edata.putAll(
                new HashMap<String, Object>() {
                    {
                        put(CourseBatchParams.userIds.name(), Arrays.asList((String) certificateEvent.get("userId")));
                        put(CourseBatchParams.batchId.name(), certificateEvent.get("batchId"));
                        put(CourseBatchParams.courseId.name(), certificateEvent.get("courseId"));
                        put(CourseBatchParams.action.name(), "issue-certificate");
                        put(CourseBatchParams.reIssue.name(), reIssue);
                        put("trigger", "auto-issue");
                    }
                });
        String beJobRequestEvent = LogTelemetryEventUtil.logInstructionEvent(actor, context, object, edata);
        LOGGER.info("CourseBatchUpdater:generateInstructionEvent: beJobRequestEvent " + beJobRequestEvent);
        return mapper.readValue(beJobRequestEvent, new TypeReference<Map<String, Object>>() {});
    }

    private Map<String, Object> readQuery(Session cassandraSession, Map<String, Object> dataToSelect) {
        String query = "SELECT status, completedOn, progress FROM " + keyspace +"." + table +
                " where courseid='" + dataToSelect.get("courseid") + "' AND batchid='" + dataToSelect.get("batchid") + "' AND userid='" + dataToSelect.get("userid") + "';";
        LOGGER.info("CourseBatchUpdater:readQuery: started + query " + query);
        ResultSet resultSet = SunbirdCassandraUtil.execute(cassandraSession, query);
        Iterator<Row> rows = resultSet.iterator();
        Map<String, Object> result = new HashMap<>();
        while(rows.hasNext()) {
            Row row = rows.next();
            LOGGER.info("CourseBatchUpdater:readQuery: row " + row);
            result.put("status", row.getInt("status"));
            result.put("completedOn", row.getTimestamp("completedOn"));
            result.put("progress", row.getInt("progress"));
        }
        LOGGER.info("CourseBatchUpdater:readQuery: completed : result :- " + result);
        return result;
    }
}
