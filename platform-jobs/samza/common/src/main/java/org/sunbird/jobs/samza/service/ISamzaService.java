package org.sunbird.jobs.samza.service;

import java.util.Map;

import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.sunbird.jobs.samza.service.task.JobMetrics;

public interface ISamzaService {

	/**
	 * 
	 * @param config
	 * @throws Exception
	 */
	public void initialize(Config config) throws Exception;
	
	/**
	 * The class processMessage is mainly responsible for processing the messages sent from consumers based on required
	 * specifications
	 *
	 * @param MessageData The messageData
	 */
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception;
	
}
