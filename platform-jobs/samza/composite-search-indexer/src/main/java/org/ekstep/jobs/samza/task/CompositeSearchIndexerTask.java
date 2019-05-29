package org.ekstep.jobs.samza.task;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
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
import org.ekstep.jobs.samza.service.CompositeSearchIndexerService;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.SamzaCommonParams;
import org.ekstep.learning.util.ControllerUtil;

import java.util.HashMap;
import java.util.Map;


public class CompositeSearchIndexerTask extends BaseTask {
	
	private JobLogger LOGGER = new JobLogger(CompositeSearchIndexerTask.class);
	private ControllerUtil controllerUtil = new ControllerUtil();

	private ISamzaService service;
	private JobMetrics metrics;

	public ISamzaService getService() {
		return service;
	}

	public CompositeSearchIndexerTask(Config config, TaskContext context, ISamzaService service) throws Exception {
		init(config, context, service);
	}

	public CompositeSearchIndexerTask() {

	}

	public void init(Config config, TaskContext context, ISamzaService service) throws Exception {
		try {
			metrics = new JobMetrics(context, config.get("output.metrics.job.name"), config.get("output.metrics.topic.name"));
			this.service = (service == null ? new CompositeSearchIndexerService() : service);
			this.service.initialize(config);
			LOGGER.info("Task initialized");
		} catch (Exception ex) {
			LOGGER.error("Task initialization failed", ex);
			throw ex;
		}
	}
	
	@Override
	public void init(Config config, TaskContext context) throws Exception {
		init(config, context, null);
	}
	
	
	@Override
	public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		Map<String, Object> outgoingMap = getMessage(envelope);
		try {
			if (outgoingMap.containsKey(SamzaCommonParams.edata.name())) {
				Map<String, Object> edata = (Map<String, Object>) outgoingMap.getOrDefault(SamzaCommonParams.edata.name(), new HashMap<String, Object>());
				if (MapUtils.isNotEmpty(edata) && StringUtils.equalsIgnoreCase("definition_update", edata.getOrDefault("action", "").toString())) {
					LOGGER.info("definition_update event received for objectType: " + edata.getOrDefault("objectType", "").toString());
					String graphId = edata.getOrDefault("graphId", "").toString();
					String objectType = edata.getOrDefault("objectType", "").toString();
					controllerUtil.updateDefinitionCache(graphId, objectType);
				}
			} else {
				service.processMessage(outgoingMap, metrics, collector);
				setMetricsOffset(getSystemStreamPartition(envelope), getOffset(envelope), metrics);
			}
		} catch (Exception e) {
			metrics.incErrorCounter();
			LOGGER.error("Error while processing message:", outgoingMap, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> getMessage(IncomingMessageEnvelope envelope) {
		try {
			return (Map<String, Object>) envelope.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Invalid message:" + envelope.getMessage(), e);
			return new HashMap<String, Object>();
		}
	}
	
	@Override
	public void window(MessageCollector collector, TaskCoordinator coordinator) {
		Map<String, Object> event = metrics.collect();
		collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", metrics.getTopic()), event));
		metrics.clear();
	}
}