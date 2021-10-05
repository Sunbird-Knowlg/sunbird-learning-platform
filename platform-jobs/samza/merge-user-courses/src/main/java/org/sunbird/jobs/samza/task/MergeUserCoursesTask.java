package org.sunbird.jobs.samza.task;


import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskCoordinator;
import org.sunbird.jobs.samza.service.ISamzaService;
import org.sunbird.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.service.MergeUserCoursesService;

import java.util.Arrays;
import java.util.Map;

public class MergeUserCoursesTask extends BaseTask {

    private ISamzaService service = new MergeUserCoursesService();
    private static JobLogger LOGGER = new JobLogger(MergeUserCoursesTask.class);

    @Override
    public ISamzaService initialize() throws Exception {
        LOGGER.info("MergeUserCoursesTask:initialize: Task initialized");
        this.action = Arrays.asList("merge-user-courses-and-cert");
        this.jobStartMessage = "Started processing of merge-user-courses samza job";
        this.jobEndMessage = "merge-user-courses job processing complete";
        this.jobClass = "org.sunbird.jobs.samza.task.MergeUserCoursesTask";
        return service;
    }

    @Override
    public void process(Map<String, Object> message, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
        try {
            LOGGER.info("MergeUserCoursesTask:process: Starting to process for mid : " + message.get("mid") + " at :: " + System.currentTimeMillis());
            service.processMessage(message, metrics, collector);
            LOGGER.info("MergeUserCoursesTask:process: Successfully completed processing  for mid : " + message.get("mid") + " at :: " + System.currentTimeMillis());
        } catch (Exception e) {
            metrics.incErrorCounter();
            LOGGER.error("MergeUserCoursesTask:process: Message processing failed", message, e);
        }
    }

}
