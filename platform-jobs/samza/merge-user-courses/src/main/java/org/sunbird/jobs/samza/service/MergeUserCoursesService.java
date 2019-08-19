package org.sunbird.jobs.samza.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ClientException;
import org.ekstep.jobs.samza.exception.PlatformErrorCodes;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.FailedEventsUtil;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.jobs.samza.model.BatchEnrollmentSyncModel;
import org.sunbird.jobs.samza.util.MergeUserCoursesParams;
import org.sunbird.jobs.samza.util.SunbirdCassandraUtil;

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

    private static final String KEYSPACE = Platform.config.hasPath("courses.keyspace.name") ?
            Platform.config.getString("courses.keyspace.name") : "sunbird_courses";

    private static final String CONTENT_CONSUMPTION_TABLE = Platform.config.hasPath("content.consumption.table") ?
            Platform.config.getString("content.consumption.table") : "content_consumption";

    private static final String USER_COURSES_TABLE = Platform.config.hasPath("user.courses.table") ?
            Platform.config.getString("user.courses.table") : "user_courses";

    private static final String USER_COURSE_ES_INDEX = Platform.config.hasPath("user.courses.es.index") ?
            Platform.config.getString("user.courses.es.index") : "user-courses";

    private static final String USER_COURSE_ES_TYPE = Platform.config.hasPath("user.courses.es.type") ?
            Platform.config.getString("user.courses.es.type") : "_doc";

    private static final String COURSE_BATCH_UPDATER_KAFKA_TOPIC = Platform.config.hasPath("course.batch.updater.kafka.topic") ?
            Platform.config.getString("course.batch.updater.kafka.topic") : "local.coursebatch.job.request";

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

    @Override
    public void initialize(Config config) throws Exception {
        this.config = config;
        JSONUtils.loadProperties(config);
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

        String action = (String) edata.get(MergeUserCoursesParams.action.name());
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

        List<String> fromBatchIds = fromBatches.stream().map(entry -> entry.getBatchId()).collect(Collectors.toList());
        List<String> toBatchIds = toBatches.stream().map(entry -> entry.getBatchId()).collect(Collectors.toList());

        List<String> batchIdsToBeMigrated = (List<String>) CollectionUtils.subtract(fromBatchIds, toBatchIds);

        //Migrate batch records in Cassandra and ES
        if (CollectionUtils.isNotEmpty(batchIdsToBeMigrated)) {
            for (String batchId : batchIdsToBeMigrated) {
                Map<String, Object> userCourse = getUserCourse(batchId, fromUserId);
                userCourse.put(MergeUserCoursesParams.userId.name(), toUserId);
                SunbirdCassandraUtil.upsert(KEYSPACE, USER_COURSES_TABLE, userCourse);

                String documentJson = ElasticSearchUtil.getDocumentAsStringById(USER_COURSE_ES_INDEX, USER_COURSE_ES_TYPE,
                        batchId + UNDERSCORE + fromUserId);
                Map<String, Object> userCourseDoc = mapper.readValue(documentJson, Map.class);
                userCourseDoc.put(MergeUserCoursesParams.userId.name(), toUserId);
                userCourseDoc.put(MergeUserCoursesParams.id.name(), batchId + UNDERSCORE + toUserId);
                userCourseDoc.put(MergeUserCoursesParams.identifier.name(), batchId + UNDERSCORE + toUserId);
                ElasticSearchUtil.addDocumentWithId(USER_COURSE_ES_INDEX, USER_COURSE_ES_TYPE,
                        batchId + UNDERSCORE + toUserId, mapper.writeValueAsString(userCourseDoc));
            }
        }

        //Clear fromUserId batches from Cassandra and ES
        if (CollectionUtils.isNotEmpty(fromBatchIds)) {
            for (String batchId : fromBatchIds) {
                Map<String, Object> key = new HashMap<>();
                key.put(MergeUserCoursesParams.batchId.name(), batchId);
                key.put(MergeUserCoursesParams.userId.name(), fromUserId);
                SunbirdCassandraUtil.delete(KEYSPACE, USER_COURSES_TABLE, key);
                ElasticSearchUtil.deleteDocument(USER_COURSE_ES_INDEX, USER_COURSE_ES_TYPE, batchId + UNDERSCORE + fromUserId);
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
                    int fromStatus = (int) contentConsumption.get(MergeUserCoursesParams.status.name());
                    int toStatus = (int) matchingRecord.get(MergeUserCoursesParams.status.name());
                    if (fromStatus > toStatus) {
                        matchingRecord.put(MergeUserCoursesParams.status.name(), fromStatus);
                    }
                }
                SunbirdCassandraUtil.upsert(KEYSPACE, CONTENT_CONSUMPTION_TABLE, matchingRecord);
            }

            //Clear fromUserId content consumption data
            Map<String, Object> key = new HashMap<>();
            key.put(MergeUserCoursesParams.userId.name(), fromUserId);
            SunbirdCassandraUtil.delete(KEYSPACE, CONTENT_CONSUMPTION_TABLE, key);
        }
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

    private Map<String, Object> getUserCourse(String batchId, String userId) {
        Map<String, Object> key = new HashMap<>();
        key.put(MergeUserCoursesParams.batchId.name(), batchId);
        key.put(MergeUserCoursesParams.userId.name(), userId);
        List<Map<String, Object>> data = SunbirdCassandraUtil.readAsListOfMap(KEYSPACE, USER_COURSES_TABLE, key);
        return CollectionUtils.isEmpty(data) ? new HashMap() : data.get(0);
    }

    private List<BatchEnrollmentSyncModel> getBatchDetailsOfUser(String userId) throws Exception {
        List<BatchEnrollmentSyncModel> objects = new ArrayList<>();
        Map<String, Object> searchQuery = new HashMap<>();
        List<String> userIdList = new ArrayList<>();
        userIdList.add(userId);
        searchQuery.put(MergeUserCoursesParams.userId.name(), userIdList);
        List<Map> documents = ElasticSearchUtil.textSearchReturningId(searchQuery, USER_COURSE_ES_INDEX, USER_COURSE_ES_TYPE);
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

}
