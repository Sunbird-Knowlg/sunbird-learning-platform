package org.ekstep.jobs.samza.task;

import java.util.Map;

import org.apache.samza.config.Config;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.task.InitableTask;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.apache.samza.task.WindowableTask;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;

public abstract class AbstractTask implements StreamTask, InitableTask, WindowableTask {

	protected JobMetrics metrics;
    
	@Override
	public void init(Config config, TaskContext context) throws Exception {
		metrics = new JobMetrics(context);
		ISamzaService service = initialize();
		service.initialize(config);
	}

	public abstract ISamzaService initialize() throws Exception;

	@SuppressWarnings("unchecked")
	@Override
	public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		Map<String, Object> message = (Map<String, Object>) envelope.getMessage();
		System.out.println("Message received: " + message);
		process(message, collector, metrics);
	}

	public abstract void process(Map<String, Object> message, MessageCollector collector, JobMetrics metrics) throws Exception;

	@Override
	public void window(MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		metrics.clear();
	}
}
