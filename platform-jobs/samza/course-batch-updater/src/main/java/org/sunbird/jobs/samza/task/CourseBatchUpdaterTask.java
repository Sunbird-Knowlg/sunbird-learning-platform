package org.sunbird.jobs.samza.task;

import com.datastax.driver.core.Session;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.service.CourseBatchUpdaterService;
import org.sunbird.jobs.samza.service.util.CourseBatchUpdater;
import org.sunbird.jobs.samza.util.BatchStatusUtil;
import org.sunbird.jobs.samza.util.CassandraConnector;
import org.sunbird.jobs.samza.util.CourseBatchParams;
import org.sunbird.jobs.samza.util.RedisConnect;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class CourseBatchUpdaterTask extends BaseTask {
    private CourseBatchUpdaterService service;

    private static JobLogger LOGGER = new JobLogger(CourseBatchUpdaterTask.class);
    private Jedis redisConnect = null;
    private Session cassandraSession = null;
    private CourseProgressHandler courseProgressHandler;
    private CourseBatchUpdater courseBatchUpdater;
    private int courseProgressBatchSize = 5000;
    private DateTimeFormatter dateTimeFormatter;
    private static String executionHour = "00";
    private SystemStream certificateInstructionStream = null;
    private Boolean certificateAutoGenerateEnable = Platform.config.hasPath("certificate.auto.generate.enable") ? Platform.config.getBoolean("certificate.auto.generate.enable") : true;
    private int redisDBIndex = Platform.config.hasPath("redis.dbIndex") ? Platform.config.getInt("redis.dbIndex") : 0;

    public ISamzaService initialize() throws Exception {
        LOGGER.info("Task initialized");

        this.redisConnect = new RedisConnect(config).getConnection(redisDBIndex, 0L);
        LOGGER.info("Redis configuration settings :: " + redisConnect.getDB() + "    :: Redis host :: " + redisConnect.getClient().getHost() + "\t RedisDBIndex :: " + redisDBIndex );
        this.cassandraSession = new CassandraConnector(config).getSession();
        this.certificateInstructionStream = new SystemStream("kafka", config.get("course.batch.certificate.topic"));
        this.action = Arrays.asList("batch-enrolment-update", "batch-enrolment-sync", "batch-status-update","course-batch-update");
        this.jobStartMessage = "Started processing of course-batch-updater samza job";
        this.jobEndMessage = "course-batch-updater job processing complete";
        this.jobClass = "org.sunbird.jobs.samza.task.CourseBatchUpdaterTask";
        JSONUtils.loadProperties(config);
        this.service = new CourseBatchUpdaterService(this.redisConnect, this.cassandraSession);
        courseBatchUpdater = new CourseBatchUpdater(redisConnect, cassandraSession, certificateInstructionStream);
        courseProgressHandler = new CourseProgressHandler();
        this.courseProgressBatchSize = Platform.config.hasPath("course.progress.batch_size") ? Platform.config.getInt("course.progress.batch_size"): 5000;
        String pattern = Platform.config.hasPath("course.batch.update_time") ? Platform.config.getString("course.batch.update_time") : "HH";
        dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return service;
    }

    @Override
    public void process(Map<String, Object> message, MessageCollector collector, TaskCoordinator coordinator) {
        try {
            LOGGER.info("Starting to process for mid : " + message.get("mid") + " at :: " + System.currentTimeMillis());
            Map<String, Object> edata = (Map<String, Object>) message.getOrDefault(CourseBatchParams.edata.name(), new HashMap<>());
            if(MapUtils.isNotEmpty(edata) && StringUtils.equalsIgnoreCase("batch-enrolment-update", (CharSequence) edata.get("action"))) {
                if(courseProgressBatchSize < courseProgressHandler.size()) {
                    executeCourseProgressBatch(collector);
                }
                courseBatchUpdater.processBatchProgress(message, courseProgressHandler, collector);
                LOGGER.info("CourseBatchUpdaterTask:process: message mid : " + message.get("mid"));
            } else {
                service.processMessage(message, null, null);
            }
            LOGGER.info("Successfully completed processing  for mid : " + message.get("mid") + " at :: " + System.currentTimeMillis());
        } catch (Exception e) {
            metrics.incErrorCounter();
            LOGGER.error("Message processing failed", message, e);
        }
    }

    @Override
    public void window(MessageCollector collector, TaskCoordinator coordinator) throws Exception {
        executeCourseProgressBatch(collector);
        if(!StringUtils.equalsIgnoreCase(executionHour, dateTimeFormatter.format(LocalDateTime.now()))){
            BatchStatusUtil.updateOnGoingBatch(cassandraSession, collector);
            BatchStatusUtil.updateCompletedBatch(cassandraSession, collector);
            executionHour = dateTimeFormatter.format(LocalDateTime.now());
        }
        super.window(collector, coordinator);
    }
    
    public void executeCourseProgressBatch(MessageCollector collector) {
        LOGGER.info("CourseBatchUpdaterTask:executeCourseProgressBatch: Starting CourseBatch updater process :: " + System.currentTimeMillis());
        List<Map<String, Object>> userCertificateEvents = new ArrayList<>();
        courseBatchUpdater.updateBatchProgress(cassandraSession, courseProgressHandler, userCertificateEvents);
        if(certificateAutoGenerateEnable && CollectionUtils.isNotEmpty(userCertificateEvents)) {
            LOGGER.info("CourseBatchUpdaterTask:executeCourseProgressBatch: Pushing user certificate event : starts :: " + System.currentTimeMillis());
            courseBatchUpdater.pushCertificateEvents(userCertificateEvents, collector);
            LOGGER.info("CourseBatchUpdaterTask:executeCourseProgressBatch: Pushing user certificate event : ends :: " + System.currentTimeMillis());
        }
        courseProgressHandler.clear();
        LOGGER.info("CourseBatchUpdaterTask:executeCourseProgressBatch: Completed CourseBatch updater process :: " + System.currentTimeMillis());
    }
}
