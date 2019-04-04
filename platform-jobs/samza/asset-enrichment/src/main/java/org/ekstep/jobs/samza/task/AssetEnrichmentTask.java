package org.ekstep.jobs.samza.task;

import java.util.Map;

import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.AssetEnrichmentService;
import org.ekstep.jobs.samza.util.JobLogger;
public class AssetEnrichmentTask extends AbstractTask{
	
	static JobLogger LOGGER = new JobLogger(AssetEnrichmentTask.class);
	ISamzaService service = new AssetEnrichmentService();

	public AssetEnrichmentTask() {

	}

	public AssetEnrichmentTask(Config config, TaskContext context) {
		try {
			init(config, context);
		} catch (Exception e) {
			LOGGER.error("Exception unhandled", e);
		}
	}
	
	public ISamzaService initialize() throws Exception {
		LOGGER.info("Task initialized");
		this.jobType = "assetenrichment";
		this.jobStartMessage = "Started processing of asset enrichment samza job";
		this.jobEndMessage = "asset enrichment job processing complete";
		this.jobClass = "org.ekstep.jobs.samza.task.AssetEnrichmentTask";
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
