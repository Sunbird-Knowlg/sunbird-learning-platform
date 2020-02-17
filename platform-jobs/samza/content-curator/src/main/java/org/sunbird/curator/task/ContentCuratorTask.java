package org.sunbird.curator.task;

import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.task.AbstractTask;
import org.ekstep.jobs.samza.util.JobLogger;
import org.sunbird.curator.service.CurationService;

import java.util.Arrays;
import java.util.Map;

public class ContentCuratorTask extends AbstractTask  {

	private static JobLogger LOGGER = new JobLogger(ContentCuratorTask.class);
	private ISamzaService service = null;

	@Override
	public ISamzaService initialize() throws Exception {
		LOGGER.info("content-curator Task initialized!");
		service = new CurationService();
		this.jobStartMessage = "Started processing of content-curator samza job.";
		this.jobEndMessage = "Completed processing of content-curator samza job.";
		this.jobClass = "org.sunbird.curator.task.ContentCuratorTask";
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
