package org.ekstep.jobs.samza.service;

import java.util.Map;

import org.apache.samza.config.Config;
import org.ekstep.jobs.samza.service.task.JobMetrics;

public interface ISamzaService {

	/**
	 * The class processMessage is mainly responsible for processing the messages sent from consumers based on required
	 * specifications
	 *
	 * @param MessageData The messageData
	 */
	public void processMessage(Map<String, Object> message, JobMetrics metrics);
	
	public void initialize(Config config) throws Exception;
}
