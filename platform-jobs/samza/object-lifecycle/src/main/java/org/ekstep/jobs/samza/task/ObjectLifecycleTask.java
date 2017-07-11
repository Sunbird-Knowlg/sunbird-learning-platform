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

public class ObjectLifecycleTask implements StreamTask, InitableTask, WindowableTask {

	private JobMetrics metrics;

	private ISamzaService service = new ObjectLifecycleService();

	@Override
	public void window(MessageCollector arg0, TaskCoordinator arg1) throws Exception {
		metrics.clear();
	}

	@Override
	public void init(Config config, TaskContext context) throws Exception {

		metrics = new JobMetrics(context);
		service.initialize(config);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) throws Exception {

		Map<String, Object> message = (Map<String, Object>) envelope.getMessage();
		try {
			service.processMessage(message, metrics, collector);
		} catch (Exception ex) {
			metrics.incFailedCounter();
			ex.printStackTrace();
		}
	}
}