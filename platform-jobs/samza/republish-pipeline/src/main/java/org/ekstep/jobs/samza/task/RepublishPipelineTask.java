package org.ekstep.jobs.samza.task;

import java.util.Map;

import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.RepublishPipelineService;
import org.ekstep.jobs.samza.util.JobLogger;

public class RepublishPipelineTask extends AbstractTask {

	static JobLogger LOGGER = new JobLogger(RepublishPipelineTask.class);
	ISamzaService service = new RepublishPipelineService();
	
	public ISamzaService initialize() throws Exception {
		LOGGER.info("Task initialized");
		this.jobType = "publish";
		this.jobStartMessage = "Started processing of publish samza job";
		this.jobEndMessage = "Publish job processing complete";
		this.jobClass = "org.ekstep.jobs.samza.task.PublishPipelineTask";
		
		return service;
	}

	@Override
	public void process(Map<String, Object> message, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		try {
			LOGGER.info("Starting of service.processMessage...");
			service.processMessage(message,  metrics, collector);
			LOGGER.info("Completed service.processMessage...");
		} catch (Exception e) {
			metrics.incErrorCounter();
			LOGGER.error("Message processing failed", message, e);
		}
	}
}