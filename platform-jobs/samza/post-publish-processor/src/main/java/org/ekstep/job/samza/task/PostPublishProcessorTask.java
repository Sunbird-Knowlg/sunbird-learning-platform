package org.ekstep.job.samza.task;

import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.job.samza.service.PostPublishProcessor;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.task.AbstractTask;
import org.ekstep.jobs.samza.util.JobLogger;

import java.util.Map;

/**
 * Task Class for Post Publish Processor Samza Job
 *
 * @author Kumar Gauraw
 */
public class PostPublishProcessorTask extends AbstractTask {

    private static JobLogger LOGGER = new JobLogger(PostPublishProcessorTask.class);
    private ISamzaService service = new PostPublishProcessor();

    @Override
    public ISamzaService initialize() throws Exception {
        LOGGER.info("post-publish-processor Task initialized!");
        this.jobType = "post-publish-processor";
        this.jobStartMessage = "Started processing of post-publish-processor samza job.";
        this.jobEndMessage = "Completed processing of post-publish-processor samza job.";
        this.jobClass = "org.ekstep.job.samza.task.PostPublishProcessorTask";
        return service;
    }

    @Override
    public void process(Map<String, Object> message, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
        try {
            LOGGER.info("Starting Task Process for post-publish-processor operation.");
            long startTime = System.currentTimeMillis();
            service.processMessage(message, metrics, collector);
            long endTime = System.currentTimeMillis();
            LOGGER.info("Total execution time taken to complete post-publish-processor operation :: " + (endTime - startTime));
        } catch (Exception e) {
            LOGGER.error("Message processing failed", message, e);
        }
    }
}
