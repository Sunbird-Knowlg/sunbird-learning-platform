package org.ekstep.jobs.samza.service.task;

import org.apache.samza.metrics.Counter;
import org.apache.samza.metrics.MetricsRegistry;
import org.apache.samza.task.TaskContext;

public class JobMetrics {

	private final Counter successMessageCount;
	private final Counter failedMessageCount;
	private final Counter skippedMessageCount;

	public JobMetrics(TaskContext context) {
		MetricsRegistry metricsRegistry = context.getMetricsRegistry();
		successMessageCount = metricsRegistry.newCounter(getClass().getName(), "success-message-count");
		failedMessageCount = metricsRegistry.newCounter(getClass().getName(), "failed-message-count");
		skippedMessageCount = metricsRegistry.newCounter(getClass().getName(), "skipped-message-count");
	}

	public void clear() {
		successMessageCount.clear();
		failedMessageCount.clear();
		skippedMessageCount.clear();
	}

	public void incSuccessCounter() {
		successMessageCount.inc();
	}

	public void incFailedCounter() {
		failedMessageCount.inc();
	}

	public void incSkippedCounter() {
		skippedMessageCount.inc();
	}
}