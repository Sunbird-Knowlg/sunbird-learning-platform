package org.ekstep.jobs.samza.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.service.task.JobMetrics;

/**
 * @author gauraw
 *
 */
public class FailedEventsUtil {

	private static JobLogger LOGGER = new JobLogger(FailedEventsUtil.class);

	public static void pushEventForRetry(SystemStream sysStream, Map<String, Object> eventMessage, JobMetrics metrics,
			MessageCollector collector, String errorCode, String errorMessage) {
		Map<String, Object> failedEventMap = new HashMap<String, Object>();
		failedEventMap.put("jobName", metrics.getJobName());
		failedEventMap.put("errorCode", errorCode);
		failedEventMap.put("errorMessage", errorMessage);
		eventMessage.put("failInfo", failedEventMap);
		collector.send(new OutgoingMessageEnvelope(sysStream, eventMessage));
		LOGGER.debug("Event sent to fail topic for job : " + metrics.getJobName());
	}
}
