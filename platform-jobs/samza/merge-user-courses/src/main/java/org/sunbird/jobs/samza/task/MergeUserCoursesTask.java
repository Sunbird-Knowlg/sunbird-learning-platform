package org.sunbird.jobs.samza.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.SamzaCommonParams;
import org.sunbird.jobs.samza.service.MergeUserCoursesService;
import org.sunbird.jobs.samza.task.BaseTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MergeUserCoursesTask extends BaseTask {

    private ISamzaService service = new MergeUserCoursesService();
    private static JobLogger LOGGER = new JobLogger(MergeUserCoursesTask.class);

    @Override
    public ISamzaService initialize() throws Exception {
        LOGGER.info("MergeUserCoursesTask:initialize: Task initialized");
        this.action = Arrays.asList("merge-user-courses");
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
