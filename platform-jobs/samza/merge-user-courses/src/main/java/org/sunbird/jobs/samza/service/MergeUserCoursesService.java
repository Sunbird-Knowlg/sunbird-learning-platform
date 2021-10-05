package org.sunbird.jobs.samza.service;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ClientException;
import org.sunbird.jobs.samza.exception.PlatformErrorCodes;
import org.sunbird.jobs.samza.service.ISamzaService;
import org.sunbird.jobs.samza.service.task.JobMetrics;
import org.sunbird.jobs.samza.util.FailedEventsUtil;
import org.sunbird.jobs.samza.util.JSONUtils;
import org.sunbird.jobs.samza.util.JobLogger;
import org.sunbird.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.jobs.samza.model.BatchEnrollmentSyncModel;
import org.sunbird.jobs.samza.util.CassandraConnector;
import org.sunbird.jobs.samza.util.MergeUserCoursesParams;
import org.sunbird.jobs.samza.util.SunbirdCassandraUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MergeUserCoursesService implements ISamzaService {
    private static JobLogger LOGGER = new JobLogger(MergeUserCoursesService.class);
    private SystemStream systemStream;
    private Config config = null;
    private static final String UNDERSCORE = "_";
    private ObjectMapper mapper = new ObjectMapper();
    private static final String ACTION = "merge-user-courses-and-cert";
    private static int MAXITERTIONCOUNT = 2;

    private static String KEYSPACE;
    private static String CONTENT_CONSUMPTION_TABLE;
    private static String USER_COURSES_TABLE;
    private static String USER_COURSE_ES_INDEX;
    private static String USER_COURSE_ES_TYPE;
    private static String COURSE_BATCH_UPDATER_KAFKA_TOPIC;
    private static String COURSE_DATE_FORMAT;
    private static SimpleDateFormat DateFormatter;
    private static String USER_ACTIVITY_AGG;
    private Session cassandraSession = null;

    protected int getMaxIterations() {
        if (Platform.config.hasPath("max.iteration.count.samza.job"))
            return Platform.config.getInt("max.iteration.count.samza.job");
        else
            return MAXITERTIONCOUNT;
    }

    private boolean validateObject(Map<String, Object> edata) {
        String action = (String) edata.get(MergeUserCoursesParams.action.name());
        Integer iteration = (Integer) edata.get(MergeUserCoursesParams.iteration.name());
        if (StringUtils.equalsIgnoreCase(ACTION, action) && (iteration <= getMaxIterations())) {
            return true;
        }
        return false;
    }

    private static void initializeConfigurations() {
        KEYSPACE = Platform.config.hasPath("courses.keyspace.name") ?
                Platform.config.getString("courses.keyspace.name") : "sunbird_courses";

        CONTENT_CONSUMPTION_TABLE = Platform.config.hasPath("content.consumption.table") ?
                Platform.config.getString("content.consumption.table") : "user_content_consumption";

        USER_COURSES_TABLE = Platform.config.hasPath("user.courses.table") ?
                Platform.config.getString("user.courses.table") : "user_enrolments";

        USER_COURSE_ES_INDEX = Platform.config.hasPath("user.courses.es.index") ?
                Platform.config.getString("user.courses.es.index") : "user-courses";

        USER_COURSE_ES_TYPE = Platform.config.hasPath("user.courses.es.type") ?
                Platform.config.getString("user.courses.es.type") : "_doc";

        COURSE_BATCH_UPDATER_KAFKA_TOPIC = Platform.config.getString("course.batch.updater.kafka.topic");

        COURSE_DATE_FORMAT = Platform.config.hasPath("course.date.format") ?
                Platform.config.getString("course.date.format") : "yyyy-MM-dd HH:mm:ss:SSSZ";
        
        USER_ACTIVITY_AGG = "user_activity_agg";

        DateFormatter = new SimpleDateFormat(COURSE_DATE_FORMAT);
    }

    @Override
    public void initialize(Config config) throws Exception {
        this.config = config;
        JSONUtils.loadProperties(config);
        initializeConfigurations();
        this.cassandraSession = new CassandraConnector(config).getSession();
        LOGGER.info("MergeUserCoursesService:initialize: Service config initialized");
        ElasticSearchUtil.initialiseESClient(USER_COURSE_ES_INDEX, Platform.config.getString("search.es_conn_info"));
        LOGGER.info("MergeUserCoursesService:initialize: ESClient initialized for index:" + USER_COURSE_ES_INDEX);
        systemStream = new SystemStream("kafka", config.get("output.failed.events.topic.name"));
        LOGGER.info("MergeUserCoursesService:initialize: Stream initialized for Failed Events");
    }

    @Override
    public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
        if (MapUtils.isEmpty(message)) {
            LOGGER.info("MergeUserCoursesService:processMessage: Ignoring the event since message is empty.");
            FailedEventsUtil.pushEventForRetry(systemStream, message, metrics, collector,
                    PlatformErrorCodes.DATA_ERROR.name(), new ClientException("ERR_MERGE_USER_COURSES_SAMZA", "message is empty"));
            metrics.incSkippedCounter();
            return;
        }

        Map<String, Object> edata = (Map<String, Object>) message.get(MergeUserCoursesParams.edata.name());
        if (MapUtils.isEmpty(edata)) {
            LOGGER.info("MergeUserCoursesService:processMessage: Ignoring the event since edata is empty.");
            FailedEventsUtil.pushEventForRetry(systemStream, message, metrics, collector,
                    PlatformErrorCodes.DATA_ERROR.name(), new ClientException("ERR_MERGE_USER_COURSES_SAMZA", "message.edata is empty"));
            metrics.incSkippedCounter();
            return;
        }

        String fromUserId = (String) edata.get(MergeUserCoursesParams.fromAccountId.name());
        String toUserId = (String) edata.get(MergeUserCoursesParams.toAccountId.name());

        if (StringUtils.isBlank(fromUserId) || StringUtils.isBlank(toUserId) || !validateObject(edata)) {
            LOGGER.info("MergeUserCoursesService:processMessage: Ignoring the event due to invalid edata:" + edata);
            FailedEventsUtil.pushEventForRetry(systemStream, message, metrics, collector,
                    PlatformErrorCodes.DATA_ERROR.name(), new ClientException("ERR_MERGE_USER_COURSES_SAMZA", "message.edata values are not valid"));
            metrics.incSkippedCounter();
            return;
        }

        try {
            mergeContentConsumption(fromUserId, toUserId);
            mergeUserBatches(fromUserId, toUserId);
            generateBatchEnrollmentSyncEvents(toUserId, collector);
            mergeUserActivityAggregates(fromUserId, toUserId);
            metrics.incSuccessCounter();
            LOGGER.info("MergeUserCoursesService:processMessage: Event processed successfully", message);
        } catch (Exception e) {
            edata.put(MergeUserCoursesParams.status.name(), MergeUserCoursesParams.FAILED.name());
            FailedEventsUtil.pushEventForRetry(systemStream, message, metrics, collector,
                    PlatformErrorCodes.PROCESSING_ERROR.name(), e);
            throw e;
        }

    }

    private void generateBatchEnrollmentSyncEvents(String userId, MessageCollector collector) throws Exception {
        List<BatchEnrollmentSyncModel> objects = getBatchDetailsOfUser(userId);
        if (CollectionUtils.isNotEmpty(objects)) {
            for (BatchEnrollmentSyncModel model : objects) {
                Map<String, Object> event = getBatchEnrollmentSyncEvent(model);
                collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", COURSE_BATCH_UPDATER_KAFKA_TOPIC), event));
            }
        }
    }

    private void mergeUserBatches(String fromUserId, String toUserId) throws Exception {
        List<BatchEnrollmentSyncModel> fromBatches = getBatchDetailsOfUser(fromUserId);
        List<BatchEnrollmentSyncModel> toBatches = getBatchDetailsOfUser(toUserId);

        Map<String, BatchEnrollmentSyncModel> fromBatchIds = new HashMap<>();
        Map<String, BatchEnrollmentSyncModel> toBatchIds = new HashMap<>();
        if (CollectionUtils.isNotEmpty(fromBatches)) {
            for (BatchEnrollmentSyncModel fromBatch : fromBatches) {
                if (StringUtils.isNotBlank(fromBatch.getBatchId()))
                    fromBatchIds.put(fromBatch.getBatchId(), fromBatch);
            }
        }
        if (CollectionUtils.isNotEmpty(toBatches)) {
            for (BatchEnrollmentSyncModel toBatch : toBatches) {
                if (StringUtils.isNotBlank(toBatch.getBatchId()))
                    toBatchIds.put(toBatch.getBatchId(), toBatch);
            }
        }

        List<String> batchIdsToBeMigrated = (List<String>) CollectionUtils.subtract(fromBatchIds.keySet(), toBatchIds.keySet());

        //Migrate batch records in Cassandra and ES
        if (CollectionUtils.isNotEmpty(batchIdsToBeMigrated)) {
            for (String batchId : batchIdsToBeMigrated) {
                String courseId = fromBatchIds.get(batchId).getCourseId();
                Map<String, Object> userCourse = getUserCourse(batchId, fromUserId, courseId);
                if (MapUtils.isNotEmpty(userCourse)) {
                    userCourse.put(MergeUserCoursesParams.userId.name(), toUserId);
                    LOGGER.info("MergeUserCoursesService:mergeUserBatches: Merging batch:" + batchId + " updated record:" + userCourse);
                    SunbirdCassandraUtil.upsert(KEYSPACE, USER_COURSES_TABLE, userCourse);

                    /*String documentJson = ElasticSearchUtil.getDocumentAsStringById(USER_COURSE_ES_INDEX, USER_COURSE_ES_TYPE,
                            batchId + UNDERSCORE + fromUserId);
                    Map<String, Object> userCourseDoc = mapper.readValue(documentJson, Map.class);
                    userCourseDoc.put(MergeUserCoursesParams.userId.name(), toUserId);
                    userCourseDoc.put(MergeUserCoursesParams.id.name(), batchId + UNDERSCORE + toUserId);
                    userCourseDoc.put(MergeUserCoursesParams.identifier.name(), batchId + UNDERSCORE + toUserId);
                    ElasticSearchUtil.addDocumentWithId(USER_COURSE_ES_INDEX, USER_COURSE_ES_TYPE,
                            batchId + UNDERSCORE + toUserId, mapper.writeValueAsString(userCourseDoc));*/
                } else {
                    LOGGER.info("MergeUserCoursesService:mergeUserBatches: user_courses record with batchId:" + batchId + " userId:" + fromUserId + " found in ES but not in Cassandra");
                }
            }
        }
    }

    private void mergeContentConsumption(String fromUserId, String toUserId) {
        //Get content consumption data
        List<Map<String, Object>> fromContentConsumptionList = getContentConsumption(fromUserId);
        List<Map<String, Object>> toContentConsumptionList = getContentConsumption(toUserId);

        if (CollectionUtils.isNotEmpty(fromContentConsumptionList)) {
            for (Map<String, Object> contentConsumption : fromContentConsumptionList) {
                Map<String, Object> matchingRecord = getMatchingRecord(contentConsumption, toContentConsumptionList);
                if (MapUtils.isEmpty(matchingRecord)) {
                    matchingRecord = contentConsumption;
                    matchingRecord.put(MergeUserCoursesParams.userId.name(), toUserId);
                } else {
                    mergeContentConsumptionRecord(contentConsumption, matchingRecord);
                }
                SunbirdCassandraUtil.upsert(KEYSPACE, CONTENT_CONSUMPTION_TABLE, matchingRecord);
            }
        }
    }

    private void mergeContentConsumptionRecord(Map<String, Object> oldRecord, Map<String, Object> newRecord) {
        /*
         * for status, progress, datetime, lastaccesstime, lastcompletedtime, lastupdatedtime fields,
         * max value should be considered
         * for completedcount, viewcount fields, sum of both records should be considered
         * */
        newRecord.put(MergeUserCoursesParams.status.name(), getUpdatedValue("Integer", "Max",
                MergeUserCoursesParams.status.name(), oldRecord, newRecord));
        newRecord.put(MergeUserCoursesParams.progress.name(), getUpdatedValue("Integer", "Max",
                MergeUserCoursesParams.progress.name(), oldRecord, newRecord));
        newRecord.put(MergeUserCoursesParams.viewCount.name(), getUpdatedValue("Integer", "Sum",
                MergeUserCoursesParams.viewCount.name(), oldRecord, newRecord));
        newRecord.put(MergeUserCoursesParams.completedCount.name(), getUpdatedValue("Integer", "Sum",
                MergeUserCoursesParams.completedCount.name(), oldRecord, newRecord));

        newRecord.put(MergeUserCoursesParams.dateTime.name(), getUpdatedValue("Date", "Max",
                MergeUserCoursesParams.dateTime.name(), oldRecord, newRecord));
        newRecord.put(MergeUserCoursesParams.lastAccessTime.name(), getUpdatedValue("DateString", "Max",
                MergeUserCoursesParams.lastAccessTime.name(), oldRecord, newRecord));
        newRecord.put(MergeUserCoursesParams.lastCompletedTime.name(), getUpdatedValue("DateString", "Max",
                MergeUserCoursesParams.lastCompletedTime.name(), oldRecord, newRecord));
        newRecord.put(MergeUserCoursesParams.lastUpdatedTime.name(), getUpdatedValue("DateString", "Max",
                MergeUserCoursesParams.lastUpdatedTime.name(), oldRecord, newRecord));
    }

    private Object getUpdatedValue(String dataType, String operation, String fieldName, Map<String, Object> oldRecord, Map<String, Object> newRecord) {
        if (null == oldRecord.get(fieldName)) {
            return newRecord.get(fieldName);
        }
        if (null == newRecord.get(fieldName)) {
            return oldRecord.get(fieldName);
        }
        switch (dataType) {
            case "Integer":
                if (oldRecord.get(fieldName) instanceof Integer &&
                        newRecord.get(fieldName) instanceof Integer) {
                    int val1 = (int) oldRecord.get(fieldName);
                    int val2 = (int) newRecord.get(fieldName);
                    if (StringUtils.equalsIgnoreCase("Sum", operation)) {
                        return val1 + val2;
                    } else if (StringUtils.equalsIgnoreCase("Max", operation)) {
                        return val1 > val2 ? val1 : val2;
                    }
                }
                break;
            case "DateString":
                if (oldRecord.get(fieldName) instanceof String &&
                        newRecord.get(fieldName) instanceof String) {
                    String dateStr1 = (String) oldRecord.get(fieldName);
                    String dateStr2 = (String) newRecord.get(fieldName);
                    Date date1;
                    Date date2;
                    try {
                        date1 = DateFormatter.parse(dateStr1);
                    } catch (ParseException pe) {
                        LOGGER.info("MergeUserCoursesService:getUpdatedValue: Date Parsing failed for field:" + fieldName + " value:" + dateStr1);
                        return dateStr2;
                    }
                    try {
                        date2 = DateFormatter.parse(dateStr2);
                    } catch (ParseException pe) {
                        LOGGER.info("MergeUserCoursesService:getUpdatedValue: Date Parsing failed for field:" + fieldName + " value:" + dateStr2);
                        return dateStr1;
                    }
                    if (StringUtils.equalsIgnoreCase("Max", operation)) {
                        if (date1.after(date2)) {
                            return dateStr1;
                        } else {
                            return dateStr2;
                        }
                    }
                }
                break;
            case "Date":
                if (oldRecord.get(fieldName) instanceof Date &&
                        newRecord.get(fieldName) instanceof Date) {
                    Date date1 = (Date) oldRecord.get(fieldName);
                    Date date2 = (Date) newRecord.get(fieldName);
                    if (StringUtils.equalsIgnoreCase("Max", operation)) {
                        if (date1.after(date2)) {
                            return date1;
                        } else {
                            return date2;
                        }
                    }
                }
                break;
        }
        return newRecord.get(fieldName);
    }

    private Map<String, Object> getMatchingRecord(Map<String, Object> contentConsumption, List<Map<String, Object>> toContentConsumptionList) {
        Map<String, Object> matchingRecord = new HashMap();
        if (CollectionUtils.isNotEmpty(toContentConsumptionList)) {
            for (Map<String, Object> toContentConsumption : toContentConsumptionList) {
                if (StringUtils.equalsIgnoreCase((String) contentConsumption.get(MergeUserCoursesParams.contentId.name()), (String) toContentConsumption.get(MergeUserCoursesParams.contentId.name())) &&
                        StringUtils.equalsIgnoreCase((String) contentConsumption.get(MergeUserCoursesParams.batchId.name()), (String) toContentConsumption.get(MergeUserCoursesParams.batchId.name())) &&
                        StringUtils.equalsIgnoreCase((String) contentConsumption.get(MergeUserCoursesParams.courseId.name()), (String) toContentConsumption.get(MergeUserCoursesParams.courseId.name()))) {
                    matchingRecord = toContentConsumption;
                    break;
                }
            }
        }
        return matchingRecord;
    }

    private List<Map<String, Object>> getContentConsumption(String userId) {
        Map<String, Object> key = new HashMap<>();
        key.put(MergeUserCoursesParams.userId.name(), userId);
        return SunbirdCassandraUtil.readAsListOfMap(KEYSPACE, CONTENT_CONSUMPTION_TABLE, key);
    }

    private Map<String, Object> getUserCourse(String batchId, String userId, String courseId) {
        Map<String, Object> key = new HashMap<>();
        key.put(MergeUserCoursesParams.batchId.name(), batchId);
        key.put(MergeUserCoursesParams.userId.name(), userId);
        key.put(MergeUserCoursesParams.courseId.name(), courseId);
        List<Map<String, Object>> data = SunbirdCassandraUtil.readAsListOfMap(KEYSPACE, USER_COURSES_TABLE, key);
        return CollectionUtils.isEmpty(data) ? new HashMap() : data.get(0);
    }

    private List<BatchEnrollmentSyncModel> getBatchDetailsOfUser(String userId) throws Exception {
        List<BatchEnrollmentSyncModel> objects = new ArrayList<>();
        Map<String, Object> searchQuery = new HashMap<>();
        List<String> userIdList = new ArrayList<>();
        userIdList.add(userId);
        searchQuery.put(MergeUserCoursesParams.userId.name(), userIdList);
        Map<String, Object> key = new HashMap<>();
        key.put(MergeUserCoursesParams.userId.name(), userIdList);
        List<Map<String, Object>> documents = SunbirdCassandraUtil.readAsListOfMap(KEYSPACE, USER_COURSES_TABLE, key);
        //List<Map> documents = ElasticSearchUtil.textSearchReturningId(searchQuery, USER_COURSE_ES_INDEX, USER_COURSE_ES_TYPE);
        if (CollectionUtils.isNotEmpty(documents)) {
            documents.forEach(doc -> {
                BatchEnrollmentSyncModel model = new BatchEnrollmentSyncModel();
                model.setBatchId((String) doc.get(MergeUserCoursesParams.batchId.name()));
                model.setUserId((String) doc.get(MergeUserCoursesParams.userId.name()));
                model.setCourseId((String) doc.get(MergeUserCoursesParams.courseId.name()));
                objects.add(model);
            });
        }
        return objects;
    }

    private Map<String, Object> getBatchEnrollmentSyncEvent(BatchEnrollmentSyncModel model) {
        return new HashMap<String, Object>() {{
            put("actor", new HashMap<String, Object>() {{
                put("id", "Course Batch Updater");
                put("type", "System");
            }});
            put("eid", "BE_JOB_REQUEST");
            put("edata", new HashMap<String, Object>() {{
                put("action", "batch-enrolment-sync");
                put("iteration", 1);
                put("batchId", model.getBatchId());
                put("userId", model.getUserId());
                put("courseId", model.getCourseId());
                put("reset", Arrays.asList("completionPercentage", "status", "progress"));
            }});
            put("ets", System.currentTimeMillis());
            put("context", new HashMap<String, Object>() {{
                put("pdata", new HashMap<String, Object>() {{
                    put("ver", "1.0");
                    put("id", "org.sunbird.platform");
                }});
            }});
            put("mid", "LP." + System.currentTimeMillis() + "." + UUID.randomUUID());
            put("object", new HashMap<String, Object>() {{
                put("id", model.getBatchId() + UNDERSCORE + model.getUserId());
                put("type", "CourseBatchEnrolment");
            }});
        }};
    }


    private void mergeUserActivityAggregates(String fromUserId, String toUserId) throws Exception {
        List<BatchEnrollmentSyncModel> fromBatches = getBatchDetailsOfUser(fromUserId);
        if(CollectionUtils.isNotEmpty(fromBatches)) {
            List<String> fromCourseIds = fromBatches.stream().map(enrol -> enrol.getCourseId()).collect(Collectors.toList());
            List<String> toCourseIds = fromBatches.stream().map(enrol -> enrol.getCourseId()).collect(Collectors.toList());
            Map<String, Object> key = new HashMap<>();
            key.put(MergeUserCoursesParams.activity_type.name(), "Course");
            key.put(MergeUserCoursesParams.user_id.name(), fromUserId);
            key.put(MergeUserCoursesParams.activity_id.name(), fromCourseIds);
            List<Map<String, Object>> fromData = SunbirdCassandraUtil.readAsListOfMap(KEYSPACE, USER_ACTIVITY_AGG, key);
            key.put(MergeUserCoursesParams.activity_id.name(), toCourseIds);
            List<Map<String, Object>> toData = SunbirdCassandraUtil.readAsListOfMap(KEYSPACE, USER_ACTIVITY_AGG, key);
            Map<String, Object> toDataMap = toData.stream().collect(Collectors.toMap(m -> (String)m.get("context_id"), m -> m));
            List<Update.Where> updateQueryList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(fromData)) {
                fromData.stream().filter(data -> MapUtils.isNotEmpty(data)).collect(Collectors.toList()).forEach(data -> {
                    data.put(MergeUserCoursesParams.user_id.name(), toUserId);
                    Map<String, Integer> fromAgg = (Map<String, Integer>) data.get("agg");
                    Map<String, Integer> toAgg = (Map<String, Integer>) ((Map<String, Object>)toDataMap.getOrDefault(data.get("context_id"), new HashMap<String, Object>())).getOrDefault("agg", new HashMap<String, Integer>());
                    data.put("agg", new HashMap<String, Integer>(){{
                        put("completedCount", Math.max(fromAgg.getOrDefault("completedCount", 0), toAgg.getOrDefault("completedCount", 0)));
                    }});
                    data.put("agg_last_updated", new HashMap<String, Date>(){{
                        put("completedCount", new Date());
                    }});
                    Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
                        put(MergeUserCoursesParams.activity_type.name(), "Course");
                        put(MergeUserCoursesParams.activity_id.name(), data.get("activity_id"));
                        put(MergeUserCoursesParams.user_id.name(), toUserId);
                        put("context_id", data.get("context_id"));
                    }};
                    updateQueryList.add(updateQuery(KEYSPACE, USER_ACTIVITY_AGG, data, dataToSelect));
                });
            }
            if(CollectionUtils.isNotEmpty(updateQueryList)){
                Batch batch = QueryBuilder.batch(updateQueryList.toArray(new RegularStatement[updateQueryList.size()]));
                cassandraSession.execute(batch);
            }
        }
        
    }


    public Update.Where updateQuery(String keyspace, String table, Map<String, Object> propertiesToUpdate, Map<String, Object> propertiesToSelect) {
        Update.Where updateQuery = QueryBuilder.update(keyspace, table).where();
        propertiesToUpdate.entrySet().forEach(entry -> updateQuery.with(QueryBuilder.set(entry.getKey(), entry.getValue())));
        propertiesToSelect.entrySet().forEach(entry -> {
            if (entry.getValue() instanceof List)
                updateQuery.and(QueryBuilder.in(entry.getKey(), (List) entry.getValue()));
            else
                updateQuery.and(QueryBuilder.eq(entry.getKey(), entry.getValue()));
        });
        return updateQuery;
    }

}
