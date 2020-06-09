package org.ekstep.jobs.samza.task;

import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.service.AutoCreatorService;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.task.BaseTask;


import java.util.Arrays;
import java.util.Map;

public class AutoCreatorTask extends BaseTask {

    private ISamzaService service =  new AutoCreatorService();

    private static JobLogger LOGGER = new JobLogger(AutoCreatorTask.class);

    public ISamzaService initialize() throws Exception {
        LOGGER.info("Task initialized");
        this.action = Platform.config.hasPath("auto_creator.actions") ?
                Arrays.asList(Platform.config.getString("auto_creator.actions").split(",")) : Arrays.asList("auto-create");
        LOGGER.info("Available Actions : " + this.action);
        this.jobStartMessage = "Started processing of auto-creator samza job";
        this.jobEndMessage = "Completed processing of auto-creator job";
        this.jobClass = "org.ekstep.jobs.samza.task.AutoCreatorTask";
        return service;
    }

    @Override
    public void process(Map<String, Object> message, MessageCollector collector, TaskCoordinator coordinator) {
        try {
            LOGGER.info("Starting Task Process for auto-creator operation for mid : " + message.get("mid") + " at :: " + System.currentTimeMillis());
            long startTime = System.currentTimeMillis();
            service.processMessage(message, metrics, collector);
            long endTime = System.currentTimeMillis();
            LOGGER.info("Successfully completed processing for auto-creator operation for mid : " + message.get("mid") + " at :: " + System.currentTimeMillis());
        } catch (Exception e) {
            LOGGER.error("Message processing failed", message, e);
            metrics.incErrorCounter();
        }
    }

    @Override
    public void window(MessageCollector collector, TaskCoordinator coordinator) throws Exception {
        Map<String, Object> event = metrics.collect();
        collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", metrics.getTopic()), event));
        metrics.clear();
    }
}