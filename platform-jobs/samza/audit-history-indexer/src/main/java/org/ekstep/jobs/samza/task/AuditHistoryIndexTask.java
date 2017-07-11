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
import org.ekstep.jobs.samza.service.AuditHistoryService;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;

public class AuditHistoryIndexTask implements StreamTask, InitableTask, WindowableTask {

	private JobMetrics metrics;
	ISamzaService auditHistoryMsgProcessor = new AuditHistoryService();

	@Override
	public void init(Config config, TaskContext context) throws Exception {
		metrics = new JobMetrics(context);
		auditHistoryMsgProcessor.initialize(config);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		Map<String, Object> outgoingMap = (Map<String, Object>) envelope.getMessage();
		try {
			auditHistoryMsgProcessor.processMessage(outgoingMap, metrics);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void window(MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		metrics.clear();
	}

}
