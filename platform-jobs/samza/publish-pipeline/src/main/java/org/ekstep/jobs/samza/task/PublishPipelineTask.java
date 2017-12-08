package org.ekstep.jobs.samza.task;

import java.util.Map;

import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.PublishPipelineService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JobLogger;

public class PublishPipelineTask extends AbstractTask {

	static JobLogger LOGGER = new JobLogger(PublishPipelineTask.class);
	ISamzaService service = new PublishPipelineService();
	
	public ISamzaService initialize() throws Exception {
		LOGGER.info("Task initialized");
		return service;
	}

	@Override
	public void process(Map<String, Object> message, MessageCollector collector, JobMetrics metrics) throws Exception {
		try {
			System.out.println("Starting of service.processMessage...");
			service.processMessage(message,  metrics, collector);
			System.out.println("Completed service.processMessage...");
		} catch (Exception e) {
			metrics.incFailedCounter();
			LOGGER.error("Message processing failed", message, e);
		}
	}
}