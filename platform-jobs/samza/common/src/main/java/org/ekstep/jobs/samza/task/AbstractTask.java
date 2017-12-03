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
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;

import com.ilimi.common.logger.LoggerEnum;
import com.ilimi.common.logger.PlatformLogger;

public abstract class AbstractTask implements StreamTask, InitableTask, WindowableTask {

	protected JobMetrics metrics;

	private Config config = null;

	@Override
	public void init(Config config, TaskContext context) throws Exception {
		metrics = new JobMetrics(context);
		ISamzaService service = initialize();
		service.initialize(config);
		this.config = config;
	}

	public abstract ISamzaService initialize() throws Exception;

	@SuppressWarnings("unchecked")
	@Override
	public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator)
			throws Exception {
		Map<String, Object> message = (Map<String, Object>) envelope.getMessage();
		preProcess(message, collector);
		process(message, collector, coordinator);
		postProcess(message, collector);
	}

	public abstract void process(Map<String, Object> message, MessageCollector collector, TaskCoordinator coordinator)
			throws Exception;

	public void preProcess(Map<String, Object> message, MessageCollector collector) {
		if (isInvalidMessage(message)) {
			String event = generateEvent(LoggerEnum.ERROR.name(), "Samza job de-serialization error", message);
			collector.send(new OutgoingMessageEnvelope(
					new SystemStream("kafka", this.config.get("backend_telemetry_topic")), event));
		}
		// check for valid instruction to process. action=publish, max retries less than iteration value.
		// generate job start event
		
	}

	public void postProcess(Map<String, Object> message, MessageCollector collector) {
		// check status of the processed event.
		// generate job end event with execution stats.
	}

	private String generateEvent(String logLevel, String message, Map<String, Object> data) {
		String event = PlatformLogger.getBELog(logLevel, message, data, null);
		return event;
	}

	protected boolean isInvalidMessage(Map<String, Object> message) {
		return (message == null || (null != message && message.containsKey("serde")
				&& "error".equalsIgnoreCase((String) message.get("serde"))));
	}

	@Override
	public void window(MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		metrics.clear();
	}
}
