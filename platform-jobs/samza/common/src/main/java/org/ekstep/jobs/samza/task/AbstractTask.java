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
	
	private JobMetrics metrics;
	
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
	public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		Map<String, Object> message = (Map<String, Object>) envelope.getMessage();
		preProcess(message, collector);
		process(message, collector, coordinator);
//		postProcess(message, collector);
	}
	
	public abstract void process(Map<String, Object> message, MessageCollector collector, TaskCoordinator coordinator) throws Exception;
	
	public void preProcess(Map<String,Object> message, MessageCollector collector) {
		if(null != message) {
			if(message.containsKey("serde")) {
				String event = generateEvent(LoggerEnum.ERROR.name(), "Samza Job De-serialization error" + message, null);
				collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", this.config.get("backend_telemetry_topic")), event));
			}
//			else {
//				String event = generateEvent(LoggerEnum.INFO.name(), "Job_Start_Event", message);
//				collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", this.config.get("backend_telemetry_topic")), event));
//			}
		}
		else {
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("message", "null value event from kafka event");
			String event = generateEvent(LoggerEnum.ERROR.name(), "Samza Job Null Event Error", map);
			collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", this.config.get("backend_telemetry_topic")), event));
		}
	}
	
	public void postProcess(Map<String,Object> message, MessageCollector collector) {
	}
	
	private String generateEvent(String logLevel, String message, Map<String, Object> data) {
		String event = PlatformLogger.getBELog(logLevel, message, data, null);
		return event;
	}
	
	@Override
	public void window(MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		metrics.clear();
	}
}
