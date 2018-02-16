package org.ekstep.jobs.samza.task;

import java.util.Map;

import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.ImageTaggingService;
import org.ekstep.jobs.samza.util.JobLogger;
public class ImageTaggingTask extends AbstractTask{
	
	static JobLogger LOGGER = new JobLogger(ImageTaggingTask.class);
	ISamzaService service = new ImageTaggingService();
	
	public ISamzaService initialize() throws Exception {
		LOGGER.info("Task initialized");
		this.jobType = "imagetagging";
		this.jobStartMessage = "Started processing of imagetagging samza job";
		this.jobEndMessage = "Imagetagging job processing complete";
		this.jobClass = "org.ekstep.jobs.samza.task.ImageTaggingTask";
		
		return service;
	}

	@Override
	public void process(Map<String, Object> message, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
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
