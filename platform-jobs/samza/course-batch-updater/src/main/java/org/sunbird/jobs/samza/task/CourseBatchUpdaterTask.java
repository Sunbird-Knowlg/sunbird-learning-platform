package org.sunbird.jobs.samza.task;

import com.datastax.driver.core.Session;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.service.CourseBatchUpdaterService;
import org.sunbird.jobs.samza.util.BatchStatusUtil;
import org.sunbird.jobs.samza.util.CassandraConnector;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

public class CourseBatchUpdaterTask extends BaseTask {
    private CourseBatchUpdaterService service;

    private static JobLogger LOGGER = new JobLogger(CourseBatchUpdaterTask.class);
    private Session cassandraSession = null;
    private DateTimeFormatter dateTimeFormatter;
    private static String executionHour = "00";


    public ISamzaService initialize() throws Exception {
        LOGGER.info("Task initialized");
        JSONUtils.loadProperties(config);
        this.cassandraSession = new CassandraConnector(config).getSession();
        this.action = Arrays.asList("batch-status-update","course-batch-update");
        this.jobStartMessage = "Started processing of course-batch-updater samza job";
        this.jobEndMessage = "course-batch-updater job processing complete";
        this.jobClass = "org.sunbird.jobs.samza.task.CourseBatchUpdaterTask";
        this.service = new CourseBatchUpdaterService(this.cassandraSession);
        String pattern = Platform.config.hasPath("course.batch.update_time") ? Platform.config.getString("course.batch.update_time") : "HH";
        dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return service;
    }

    @Override
    public void process(Map<String, Object> message, MessageCollector collector, TaskCoordinator coordinator) {
        try {
            LOGGER.info("Starting to process for mid : " + message.get("mid") + " at :: " + System.currentTimeMillis());
            service.processMessage(message, null, null);
            LOGGER.info("Successfully completed processing  for mid : " + message.get("mid") + " at :: " + System.currentTimeMillis());
        } catch (Exception e) {
            metrics.incErrorCounter();
            LOGGER.error("Message processing failed", message, e);
        }
    }

    @Override
    public void window(MessageCollector collector, TaskCoordinator coordinator) throws Exception {

        if(!StringUtils.equalsIgnoreCase(executionHour, dateTimeFormatter.format(LocalDateTime.now()))){
            BatchStatusUtil.updateOnGoingBatch(cassandraSession, collector);
            BatchStatusUtil.updateCompletedBatch(cassandraSession, collector);
            executionHour = dateTimeFormatter.format(LocalDateTime.now());
        }
        super.window(collector, coordinator);
    }
}
