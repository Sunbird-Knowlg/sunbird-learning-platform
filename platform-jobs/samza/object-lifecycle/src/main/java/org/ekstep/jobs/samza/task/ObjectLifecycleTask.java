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
import org.ekstep.jobs.samza.service.ObjectLifecycleService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JobLogger;

public class ObjectLifecycleTask implements StreamTask, InitableTask, WindowableTask {
	
	private JobLogger LOGGER = new JobLogger(ObjectLifecycleTask.class);

	private JobMetrics metrics;

	private ISamzaService service = new ObjectLifecycleService();

	@Override
	public void window(MessageCollector arg0, TaskCoordinator arg1) throws Exception {
		metrics.clear();
	}

	@Override
	public void init(Config config, TaskContext context) throws Exception {

		try {
			metrics = new JobMetrics(context);
			service.initialize(config);
			LOGGER.info("Task initialized");
		} catch(Exception ex) {
			LOGGER.error("Task initialization failed", ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) throws Exception {

		Map<String, Object> message = (Map<String, Object>) envelope.getMessage();
		try {
			service.processMessage(message, metrics, collector);
		} catch (Exception ex) {
			metrics.incFailedCounter();
			LOGGER.error("Message processing failed", message, ex);
		}
	}
}