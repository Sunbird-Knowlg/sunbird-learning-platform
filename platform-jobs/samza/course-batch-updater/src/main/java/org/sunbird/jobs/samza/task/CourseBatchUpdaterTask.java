package org.sunbird.jobs.samza.task;

import java.util.HashMap;
import java.util.Map;

import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskCoordinator;


import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.task.AbstractTask;
import org.ekstep.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.service.CourseBatchUpdaterService;

public class CourseBatchUpdaterTask extends AbstractTask {
    private ISamzaService service =  new CourseBatchUpdaterService();

    private static JobLogger LOGGER = new JobLogger(CourseBatchUpdaterTask.class);

    public ISamzaService initialize() throws Exception {
        LOGGER.info("Task initialized");
        this.jobType = "batch-enrolment-update";
        this.jobStartMessage = "Started processing of course-batch-updater samza job";
        this.jobEndMessage = "course-batch-updater job processing complete";
        this.jobClass = "org.sunbird.jobs.samza.task.CourseBatchUpdaterTask";
        return service;
    }

    @Override
    public void process(Map<String, Object> message, MessageCollector collector, TaskCoordinator coordinator) {
        try {
            service.processMessage(message, null, null);
        } catch (Exception e) {
            metrics.incErrorCounter();
            LOGGER.error("Message processing failed", message, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMessage(IncomingMessageEnvelope envelope) {
        try {
            return (Map<String, Object>) envelope.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("IndexerTask:getMessage: Invalid message = " + envelope.getMessage() + " with error : " + e);
            return new HashMap<String, Object>();
        }
    }
}