package org.sunbird.jobs.samza.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.sunbird.jobs.samza.service.task.JobMetrics;

/**
 * @author gauraw
 *
 */
public class FailedEventsUtil {

	private static JobLogger LOGGER = new JobLogger(FailedEventsUtil.class);

	public static void pushEventForRetry(SystemStream sysStream, Map<String, Object> eventMessage, 
			JobMetrics metrics, MessageCollector collector, String errorCode, Throwable error) {
		Map<String, Object> failedEventMap = new HashMap<String, Object>();
		String errorString[] = ExceptionUtils.getStackTrace(error).split("\\n\\t");

		List<String> stackTrace;
		if(errorString.length > 21) {
			stackTrace = Arrays.asList(errorString).subList((errorString.length - 21), errorString.length - 1);
		}else{
			stackTrace = Arrays.asList(errorString);
		}

		failedEventMap.put("errorCode", errorCode);
		failedEventMap.put("error", error.getMessage() + " : : "  + stackTrace);
		eventMessage.put("jobName", metrics.getJobName());
		eventMessage.put("failInfo", failedEventMap);
		collector.send(new OutgoingMessageEnvelope(sysStream, eventMessage));
		LOGGER.debug("Event sent to fail topic for job : " + metrics.getJobName());
	}
}
