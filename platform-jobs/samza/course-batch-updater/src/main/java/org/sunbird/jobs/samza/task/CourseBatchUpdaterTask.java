package org.sunbird.jobs.samza.task;

import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.service.CourseBatchUpdaterService;
import org.sunbird.jobs.samza.util.BatchStatusUtil;

import java.util.Arrays;
import java.util.Map;

public class CourseBatchUpdaterTask extends BaseTask {
    private ISamzaService service =  new CourseBatchUpdaterService();

    private static JobLogger LOGGER = new JobLogger(CourseBatchUpdaterTask.class);

    public ISamzaService initialize() throws Exception {
        LOGGER.info("Task initialized");
        this.action = Arrays.asList("batch-enrolment-update", "batch-enrolment-sync", "batch-status-update","course-batch-update");
        this.jobStartMessage = "Started processing of course-batch-updater samza job";
        this.jobEndMessage = "course-batch-updater job processing complete";
        this.jobClass = "org.sunbird.jobs.samza.task.CourseBatchUpdaterTask";
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
        Map<String, Object> event = metrics.collect();
        collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", metrics.getTopic()), event));
        metrics.clear();

        BatchStatusUtil.updateOnGoingBatch(collector);
        BatchStatusUtil.updateCompletedBatch(collector);
    }
}