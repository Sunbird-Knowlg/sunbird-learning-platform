package org.ekstep.job.samza.task;

import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.common.Platform;
import org.ekstep.job.samza.service.PostPublishProcessor;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.task.BaseTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Task Class for Post Publish Processor Samza Job
 *
 * @author Kumar Gauraw
 */
public class PostPublishProcessorTask extends BaseTask {

    private static JobLogger LOGGER = new JobLogger(PostPublishProcessorTask.class);
    private ISamzaService service = null;

    @Override
    public ISamzaService initialize() throws Exception {
        LOGGER.info("post-publish-processor Task initialized!");
        service = new PostPublishProcessor();
        this.action = Platform.config.hasPath("post_publish_processor.actions") ?
                Arrays.asList(Platform.config.getString("post_publish_processor.actions").split(",")) : Arrays.asList("link-dialcode","coursebatch-sync","publish-shallow-content", "coursebatch-create");
        LOGGER.info("Available Actions : " + this.action);
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
