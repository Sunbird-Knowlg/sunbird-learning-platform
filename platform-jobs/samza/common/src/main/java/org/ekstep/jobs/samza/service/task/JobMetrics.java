package org.ekstep.jobs.samza.service.task;

import java.util.HashMap;
import java.util.Map;

import org.apache.samza.metrics.Counter;
import org.apache.samza.metrics.MetricsRegistry;
import org.apache.samza.task.TaskContext;

public class JobMetrics {

	private String jobName;
	private String topic;
	private final Counter successMessageCount;
	private final Counter failedMessageCount;
	private final Counter skippedMessageCount;
	private final Counter errorMessageCount;

	public JobMetrics(TaskContext context) {
		this(context, null, null);
	}

	public JobMetrics(TaskContext context, String jName, String topic) {
		MetricsRegistry metricsRegistry = context.getMetricsRegistry();
		successMessageCount = metricsRegistry.newCounter(getClass().getName(), "success-message-count");
		failedMessageCount = metricsRegistry.newCounter(getClass().getName(), "failed-message-count");
		skippedMessageCount = metricsRegistry.newCounter(getClass().getName(), "skipped-message-count");
		errorMessageCount = metricsRegistry.newCounter(getClass().getName(), "error-message-count");
		jobName = jName;
		this.topic = topic;
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

	public void incErrorCounter() {
        errorMessageCount.inc();
    }

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Map<String, Object> collect() {
		Map<String, Object> metricsEvent = new HashMap<>();
		metricsEvent.put("job-name", jobName);
		metricsEvent.put("success-message-count", successMessageCount.getCount());
		metricsEvent.put("failed-message-count", failedMessageCount.getCount());
		metricsEvent.put("error-message-count", errorMessageCount.getCount());		
		metricsEvent.put("skipped-message-count", skippedMessageCount.getCount());
		return metricsEvent;
	}

}