package org.sunbird.jobs.samza.task;

import com.datastax.driver.core.Session;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.service.CourseBatchUpdaterService;
import org.sunbird.jobs.samza.service.util.CourseBatchUpdater;
import org.sunbird.jobs.samza.util.BatchStatusUtil;
import org.sunbird.jobs.samza.util.CassandraConnector;
import org.sunbird.jobs.samza.util.RedisConnect;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CourseBatchUpdaterTask extends BaseTask {
    private CourseBatchUpdaterService service;

    private static JobLogger LOGGER = new JobLogger(CourseBatchUpdaterTask.class);
    private Jedis redisConnect = null;
    private Session cassandraSession = null;
    private Map<String, Object> batchProgressEvents = new HashMap<>();
    private CourseBatchUpdater courseBatchUpdater;

    public ISamzaService initialize() throws Exception {
        LOGGER.info("Task initialized");
        this.redisConnect = new RedisConnect(config).getConnection();
        this.cassandraSession = new CassandraConnector(config).getSession();
        this.action = Arrays.asList("batch-enrolment-update", "batch-enrolment-sync", "batch-status-update","course-batch-update");
        this.jobStartMessage = "Started processing of course-batch-updater samza job";
        this.jobEndMessage = "course-batch-updater job processing complete";
        this.jobClass = "org.sunbird.jobs.samza.task.CourseBatchUpdaterTask";
        this.service = new CourseBatchUpdaterService(this.redisConnect, this.cassandraSession);
        courseBatchUpdater = new CourseBatchUpdater(redisConnect, cassandraSession);
        return service;
    }

    @Override
    public void process(Map<String, Object> message, MessageCollector collector, TaskCoordinator coordinator) {
        try {
            LOGGER.info("Starting to process for mid : " + message.get("mid") + " at :: " + System.currentTimeMillis());
            if(StringUtils.equalsIgnoreCase("batch-enrolment-update", (String)message.get("action"))) {
                courseBatchUpdater.processBatchProgress(message, batchProgressEvents);
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
        Map<String, Object> event = metrics.collect();
        collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", metrics.getTopic()), event));
        metrics.clear();

        courseBatchUpdater.updateBatchProgress(cassandraSession, batchProgressEvents);
        batchProgressEvents.clear();

        BatchStatusUtil.updateOnGoingBatch(cassandraSession, collector);
        BatchStatusUtil.updateCompletedBatch(cassandraSession, collector);
    }
}