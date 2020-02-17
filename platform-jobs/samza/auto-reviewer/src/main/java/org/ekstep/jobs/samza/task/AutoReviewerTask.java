package org.ekstep.jobs.samza.task;

import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.jobs.samza.service.AutoReviewerService;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.util.JobLogger;
import java.util.Map;

public class AutoReviewerTask extends AbstractTask {

	private static JobLogger LOGGER = new JobLogger(AutoReviewerTask.class);
	private ISamzaService service = null;

	@Override
	public ISamzaService initialize() throws Exception {
		LOGGER.info("auto-reviewer Task initialized!");
		service = new AutoReviewerService();
		this.jobStartMessage = "Started processing of auto-reviewer samza job.";
		this.jobEndMessage = "Completed processing of auto-reviewer samza job.";
		this.jobClass = "org.ekstep.jobs.samza.task.AutoReviewerTask";
		return service;
	}

	@Override
	public void process(Map<String, Object> message, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		try {
			LOGGER.info("Starting Task Process for content curation operation.");
			long startTime = System.currentTimeMillis();
			service.processMessage(message, metrics, collector);
			long endTime = System.currentTimeMillis();
			LOGGER.info("Total execution time taken to complete content curation operation :: " + (endTime - startTime));
		} catch (Exception e) {
			LOGGER.error("Message processing failed", message, e);
		}
	}
}
