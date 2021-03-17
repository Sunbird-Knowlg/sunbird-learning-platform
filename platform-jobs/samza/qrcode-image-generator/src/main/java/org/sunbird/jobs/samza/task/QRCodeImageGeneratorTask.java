package org.sunbird.jobs.samza.task;

import org.apache.samza.config.Config;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.InitableTask;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskCoordinator;
import org.sunbird.jobs.samza.service.ISamzaService;
import org.sunbird.jobs.samza.service.QRCodeImageGeneratorService;
import org.sunbird.jobs.samza.service.task.JobMetrics;
import org.sunbird.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.util.QRCodeImageGeneratorParams;

import java.util.HashMap;
import java.util.Map;

public class QRCodeImageGeneratorTask implements StreamTask, InitableTask {
	
	private JobLogger LOGGER = new JobLogger(QRCodeImageGeneratorTask.class);

	private JobMetrics metrics;

	private ISamzaService service = new QRCodeImageGeneratorService();

	@Override
	public void init(Config config, TaskContext context) throws Exception {
		try {
			metrics = new JobMetrics(context, config.get("output.metrics.job.name"), config.get("output.metrics.topic.name"));
			service.initialize(config);
			LOGGER.info("QRCodeImageGeneratorTask:init: Task initialized");
		} catch (Exception ex) {
			LOGGER.error("QRCodeImageGeneratorTask:init: Task initialization failed", ex);
			throw ex;
		}
	}

	
	@Override
	public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		Map<String, Object> outgoingMap = getMessage(envelope);
		try {
			service.processMessage(outgoingMap, metrics, collector);
		} catch (Exception e) {
			LOGGER.error("QRCodeImageGeneratorTask:process: Error while processing message for process_id:: " + 
					(String) outgoingMap.get(QRCodeImageGeneratorParams.processId.name()), outgoingMap, e);
			e.printStackTrace();
			//throw e;
		}
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> getMessage(IncomingMessageEnvelope envelope) {
		try {
			return (Map<String, Object>) envelope.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("QRCodeImageGeneratorTask:getMessage: Invalid message = " + envelope.getMessage(), e);
			return new HashMap<String, Object>();
		}
	}
	
}
