package org.ekstep.jobs.samza.task;

import java.util.Map;

import com.google.gson.Gson;
import org.apache.samza.config.Config;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.InitableTask;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.apache.samza.task.WindowableTask;
import org.ekstep.jobs.samza.service.ContentAutoTaggingService;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.DaggitServiceClient;
import org.ekstep.jobs.samza.util.JobLogger;


public class ContentAutoTaggingTask implements StreamTask, InitableTask, WindowableTask {
	
	private static JobLogger LOGGER = new JobLogger(ContentAutoTaggingTask.class);

	private ContentAutoTaggingService service;
	private JobMetrics metrics;
	private ContentAutoTaggingConfig config;
	private DaggitServiceClient serviceClient;

	public ContentAutoTaggingTask() {}

	public ContentAutoTaggingTask(Config config, TaskContext context, DaggitServiceClient client, ContentAutoTaggingService service) throws Exception {
		init(config, context, client);
	}

	@Override
	public void init(Config config, TaskContext context) throws Exception {
		init(config, context, serviceClient);
	}

	public void init(Config config, TaskContext context, DaggitServiceClient client) throws Exception {
		this.config = new ContentAutoTaggingConfig(config);
		this.serviceClient =
				client == null ?
						new DaggitServiceClient(this.config.getAPIEndPoint(), this.config.getExperimentName())
						: client;

		this.metrics = new JobMetrics(context, this.config.getJobName(), this.config.getMetricsTopic());
		this.service = new ContentAutoTaggingService(this.config, this.serviceClient);
		LOGGER.info("ContentAutoTagging Task initialized");
	}
	
	
	@Override
	public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator)
			throws Exception {

		ContentAutoTaggingSource source = new ContentAutoTaggingSource(envelope);
		ContentAutoTaggingSink sink = new ContentAutoTaggingSink(collector, metrics, config);
		service.process(source, sink);
	}
	
	@Override
	public void window(MessageCollector collector, TaskCoordinator coordinator) {
		Map<String, Object> event = metrics.collect();
		String strEvent = new Gson().toJson(event, Map.class);
		collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", config.getMetricsTopic()), strEvent));
		metrics.clear();
	}
}