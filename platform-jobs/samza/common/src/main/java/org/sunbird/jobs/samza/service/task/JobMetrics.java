package org.sunbird.jobs.samza.service.task;

import org.apache.samza.metrics.Counter;
import org.apache.samza.metrics.Metric;
import org.apache.samza.metrics.MetricsRegistry;
import org.apache.samza.metrics.MetricsRegistryMap;
import org.apache.samza.system.SystemStreamPartition;
import org.apache.samza.task.TaskContext;
import org.sunbird.jobs.samza.util.JobLogger;

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
	private int partition;

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

	/**
	 *
	 * @param containerMetricsRegistry
	 * @return
	 */
	public long computeConsumerLag(Map<String, ConcurrentHashMap<String, Metric>> containerMetricsRegistry) {
		long consumerLag = 0;
		try {
			for (SystemStreamPartition sysPartition : context.getSystemStreamPartitions()) {
				if (!sysPartition.getStream().endsWith("system.command")) {
					long highWatermarkOffset =
							Long.valueOf(containerMetricsRegistry.get("org.apache.samza.system.kafka.KafkaSystemConsumerMetrics")
									.get(getSamzaMetricKey(sysPartition, "high-watermark")).toString());
					long checkPointOffset = Long.valueOf(containerMetricsRegistry.get("org.apache.samza.checkpoint.OffsetManagerMetrics")
							.get(getSamzaMetricKey(sysPartition, "checkpointed-offset")).toString());
					String lagMessage = "Job Name : " + getJobName() + " , partition : " + sysPartition.getPartition().getPartitionId() + " , Stream : " + sysPartition.toString() + " , Current High Water Mark Offset : " + highWatermarkOffset + " , Current Checkpoint Offset : " + checkPointOffset + " , consumer lag : " + (highWatermarkOffset - checkPointOffset) + " , timestamp :" + System.currentTimeMillis();
					System.out.println(lagMessage);
					LOGGER.info(lagMessage);
					consumerLag += highWatermarkOffset - checkPointOffset;
					this.partition = sysPartition.getPartition().getPartitionId();
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception Occurred While Computing Consumer Lag. Exception is : ", "", e);
		}
		return consumerLag;
	}

	private String getSamzaMetricKey(SystemStreamPartition partition, String samzaMetricName) {
		return String.format("%s-%s-%s-%s",
				partition.getSystem(), partition.getStream(), partition.getPartition().getPartitionId(), samzaMetricName);
	}

	public Map<String, Object> collect() {
		LOGGER.info("collect is called for Job : "+getJobName()+" , partition : "+partition);
		Map<String, Object> metricsEvent = new HashMap<>();
		metricsEvent.put("job-name", jobName);
		metricsEvent.put("success-message-count", successMessageCount.getCount());
		metricsEvent.put("failed-message-count", failedMessageCount.getCount());
		metricsEvent.put("error-message-count", errorMessageCount.getCount());		
		metricsEvent.put("skipped-message-count", skippedMessageCount.getCount());
		metricsEvent.put("partition",partition);
		metricsEvent.put("consumer-lag",
				computeConsumerLag(((MetricsRegistryMap) context.getSamzaContainerContext().metricsRegistry).metrics()));
		return metricsEvent;
	}

}