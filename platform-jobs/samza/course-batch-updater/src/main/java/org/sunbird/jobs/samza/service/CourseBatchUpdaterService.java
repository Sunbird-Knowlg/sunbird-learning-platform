package org.sunbird.jobs.samza.service;

import org.apache.samza.config.Config;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;

import java.util.Map;

public class CourseBatchUpdaterService implements ISamzaService {
    private static JobLogger LOGGER = new JobLogger(CourseBatchUpdaterService.class);
    private SystemStream systemStream;

    @Override
    public void initialize(Config config) throws Exception {
        JSONUtils.loadProperties(config);
        LOGGER.info("Service config initialized");
        systemStream = new SystemStream("kafka", config.get("output.failed.events.topic.name"));
    }

    @Override
    public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
        LOGGER.info("This is a message" +message);
        System.out.println("This is a message" + message);
        System.out.println("Hey it's working");
    }
}
