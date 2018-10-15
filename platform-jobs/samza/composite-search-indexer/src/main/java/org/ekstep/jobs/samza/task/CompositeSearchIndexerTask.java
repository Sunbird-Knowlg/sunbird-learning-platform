package org.ekstep.jobs.samza.task;

import java.util.HashMap;
import java.util.Map;

import org.apache.samza.config.Config;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.InitableTask;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.apache.samza.task.WindowableTask;
import org.ekstep.jobs.samza.service.CompositeSearchIndexerService;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JobLogger;

import static java.lang.Long.parseLong;
import static org.ekstep.jobs.samza.service.util.TaskUtils.getAllDefinitions;

public class CompositeSearchIndexerTask implements StreamTask, InitableTask, WindowableTask {
	
	private JobLogger LOGGER = new JobLogger(CompositeSearchIndexerTask.class);
	
	private JobMetrics metrics;
	
	ISamzaService service = new CompositeSearchIndexerService();

	private long taskWindow, updateDefinitionsCounter = 0;

	private static final long updateDefinitionsWindow = 3600000;
	
	@Override
	public void init(Config config, TaskContext context) throws Exception {
		try {
			metrics = new JobMetrics(context, config.get("output.metrics.job.name"), config.get("output.metrics.topic.name"));
			service.initialize(config);
			taskWindow = parseLong(config.get("task.window.ms"));
			LOGGER.info("Task initialized");
		} catch (Exception ex) {
			LOGGER.error("Task initialization failed", ex);
			throw ex;
		}
	}
	
	
	@Override
	public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		Map<String, Object> outgoingMap = getMessage(envelope);
		try {
			service.processMessage(outgoingMap, metrics, collector);
		} catch (Exception e) {
			metrics.incErrorCounter();
			LOGGER.error("Error while processing message:",outgoingMap, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> getMessage(IncomingMessageEnvelope envelope) {
		try {
			return (Map<String, Object>) envelope.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Invalid message:" + envelope.getMessage(), e);
			return new HashMap<String, Object>();
		}
	}
	
	@Override
	public void window(MessageCollector collector, TaskCoordinator coordinator) {
		updateDefinitionsCounter += taskWindow;
		Map<String, Object> event = metrics.collect();
		collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", metrics.getTopic()), event));
		metrics.clear();
		if (updateDefinitionsCounter >= updateDefinitionsWindow) {
			LOGGER.info("Updating Definitions");
			getAllDefinitions();
			updateDefinitionsCounter = 0;
		}
	}
}