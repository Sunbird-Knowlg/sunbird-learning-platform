package org.ekstep.jobs.samza.service.task;

import org.apache.samza.metrics.Counter;
import org.apache.samza.metrics.Metric;
import org.apache.samza.metrics.MetricsRegistry;
import org.apache.samza.metrics.MetricsRegistryMap;
import org.apache.samza.system.SystemStreamPartition;
import org.apache.samza.task.TaskContext;
import org.ekstep.jobs.samza.util.JobLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JobMetrics {

	private static JobLogger LOGGER = new JobLogger(JobMetrics.class);
	private String jobName;
	private String topic;
	private TaskContext context;
	private final Counter successMessageCount;
	private final Counter failedMessageCount;
	private final Counter skippedMessageCount;
	private final Counter errorMessageCount;
	private Map<String,Long> offsetMap = new HashMap<>();


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
		this.context=context;
	}

	public void clear() {
		successMessageCount.clear();
		failedMessageCount.clear();
		skippedMessageCount.clear();
		errorMessageCount.clear();
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

	public void setOffset(SystemStreamPartition systemStreamPartition, String offset) {
		String offsetMapKey = String.format("%s%s", systemStreamPartition.getStream(),
				systemStreamPartition.getPartition().getPartitionId());
		offsetMap.put(offsetMapKey, Long.valueOf(offset));
	}

	public long computeConsumerLag(Map<String, ConcurrentHashMap<String, Metric>> containerMetricsRegistry) {
		long consumerLag = 0;
		int partition = 0;
		try {
			for (SystemStreamPartition sysPartition : context.getSystemStreamPartitions()) {
				String offsetChangeKey = String.format("%s-%s-%s-offset-change",
						sysPartition.getSystem(), sysPartition.getStream(), sysPartition.getPartition().getPartitionId());
				long logEndOffset =
						Long.valueOf(containerMetricsRegistry.get("org.apache.samza.system.kafka.KafkaSystemConsumerMetrics")
								.get(offsetChangeKey).toString());
				long offset = offsetMap.getOrDefault(sysPartition.getStream() +
						sysPartition.getPartition().getPartitionId(), -1L) + 1L;
				consumerLag += logEndOffset - offset;
				partition = sysPartition.getPartition().getPartitionId();
			}
		} catch (Exception e) {
			LOGGER.error("Exception Occurred While Computing Consumer Lag. Exception is : ", "", e);
		}
		return consumerLag;
	}

	public Map<String, Object> collect() {
		Map<String, Object> metricsEvent = new HashMap<>();
		metricsEvent.put("job-name", jobName);
		metricsEvent.put("success-message-count", successMessageCount.getCount());
		metricsEvent.put("failed-message-count", failedMessageCount.getCount());
		metricsEvent.put("error-message-count", errorMessageCount.getCount());		
		metricsEvent.put("skipped-message-count", skippedMessageCount.getCount());
		metricsEvent.put("consumer-lag",
				computeConsumerLag(((MetricsRegistryMap) context.getSamzaContainerContext().metricsRegistry).metrics()));
		return metricsEvent;
	}

}