package org.sunbird.jobs.samza.task;

import java.util.Map;

import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskCoordinator;
import org.sunbird.jobs.samza.service.ISamzaService;
import org.sunbird.jobs.samza.service.PublishPipelineService;
import org.sunbird.jobs.samza.util.JobLogger;

public class PublishPipelineTask extends AbstractTask {

	private static JobLogger LOGGER = new JobLogger(PublishPipelineTask.class);
	private ISamzaService service = new PublishPipelineService();
	
	public ISamzaService initialize() throws Exception {
		LOGGER.info("Task initialized");
		this.jobType = "publish";
		this.jobStartMessage = "Started processing of publish samza job";
		this.jobEndMessage = "Publish job processing complete";
		this.jobClass = "org.sunbird.jobs.samza.task.PublishPipelineTask";
		
		return service;
	}

	@Override
	public void process(Map<String, Object> message, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		try {
			//LOGGER.info("Starting of service.processMessage...");
			long startTime = System.currentTimeMillis();
			LOGGER.info("Starting of service.processMessage at :: " + startTime);
			service.processMessage(message,  metrics, collector);
			//LOGGER.info("Completed service.processMessage...");
			long endTime = System.currentTimeMillis();
			LOGGER.info("Completed service.processMessage at :: " + endTime);
			LOGGER.info("Total execution time to complete publish operation :: " + (endTime-startTime));
		} catch (Exception e) {
			metrics.incErrorCounter();
			LOGGER.error("Message processing failed", message, e);
		}
	}
}