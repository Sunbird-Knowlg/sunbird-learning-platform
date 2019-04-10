package org.ekstep.jobs.samza.service;
import java.util.Map;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;

public class CollectionMigrationService implements ISamzaService {

	static JobLogger LOGGER = new JobLogger(CollectionMigrationService.class);

	public void initialize(Config config) throws Exception {
		JSONUtils.loadProperties(config);
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
			System.out.println("Printing the message received: " + message);
	}
}