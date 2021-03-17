package org.sunbird.jobs.samza.task;

import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskCoordinator;
import org.sunbird.jobs.samza.service.ISamzaService;
import org.sunbird.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.service.CertificatePreProcessorService;

import java.util.Arrays;
import java.util.Map;

public class CertificatePreProcessorTask extends BaseTask {
    private static JobLogger LOGGER = new JobLogger(CertificatePreProcessorTask.class);
    private ISamzaService service = new CertificatePreProcessorService();
    
    
    @Override
    public ISamzaService initialize() throws Exception {
        LOGGER.info("Task initialized");
        this.action = Arrays.asList("issue-certificate");
        this.jobStartMessage = "Started processing of certificate-pre-processor samza job";
        this.jobEndMessage = "certificate-pre-processor job processing complete";
        this.jobClass = "org.sunbird.jobs.samza.task.CertificatePreProcessorTask";
        return service;
    }

    @Override
    public void process(Map<String, Object> message, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
        try {
            LOGGER.info("Starting to process for mid : " + message.get("mid") + " at :: " + System.currentTimeMillis());
            service.processMessage(message, metrics, collector);
            LOGGER.info("Successfully completed processing  for mid : " + message.get("mid") + " at :: " + System.currentTimeMillis());
        } catch (Exception e) {
            metrics.incErrorCounter();
            LOGGER.error("Message processing failed", message, e);
        }
    }
}
