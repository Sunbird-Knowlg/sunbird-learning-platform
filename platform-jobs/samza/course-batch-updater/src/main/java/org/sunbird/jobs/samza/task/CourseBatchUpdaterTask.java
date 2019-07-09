package org.sunbird.jobs.samza.task;

import java.util.HashMap;
import java.util.Map;

import org.apache.samza.config.Config;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.task.InitableTask;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
//import org.sunbird.common.models.util.LoggerEnum;
//import org.sunbird.common.models.util.ProjectLogger;

import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.service.CourseBatchUpdaterService;

public class CourseBatchUpdaterTask implements StreamTask, InitableTask {
    private ISamzaService service;

    private static JobLogger LOGGER = new JobLogger(CourseBatchUpdaterTask.class);

    @Override
    public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator)
            throws Exception {
        Map<String, Object> event = getMessage(envelope);
        service.processMessage(event, null, null);
        LOGGER.info("CourseBatchUpdaterTask: process: event = " + event);
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

    @Override
    public void init(Config config, TaskContext taskContext) throws Exception {
        LOGGER.info("IndexerTask:init: config = " + config.toString());
        service = new CourseBatchUpdaterService();
        service.initialize(config);
    }
}