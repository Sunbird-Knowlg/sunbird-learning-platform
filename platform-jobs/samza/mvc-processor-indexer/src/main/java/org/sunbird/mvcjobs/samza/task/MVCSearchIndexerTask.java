package org.sunbird.mvcjobs.samza.task;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.sunbird.jobs.samza.task.BaseTask;
import org.sunbird.mvcjobs.samza.service.MVCProcessorService;
import org.sunbird.jobs.samza.service.ISamzaService;
import org.sunbird.jobs.samza.service.task.JobMetrics;
import org.sunbird.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.util.SamzaCommonParams;
import org.sunbird.learning.util.ControllerUtil;

import java.util.HashMap;
import java.util.Map;


public class MVCSearchIndexerTask extends BaseTask {
	
	private JobLogger LOGGER = new JobLogger(MVCSearchIndexerTask.class);
	private ControllerUtil controllerUtil = new ControllerUtil();

	private ISamzaService service;
	private JobMetrics metrics;

	public ISamzaService getService() {
		return service;
	}

	public MVCSearchIndexerTask(Config config, TaskContext context, ISamzaService service) throws Exception {
		init(config, context, service);
	}

	public MVCSearchIndexerTask() {

	}

	public void init(Config config, TaskContext context, ISamzaService service) throws Exception {
		try {
			metrics = new JobMetrics(context, config.get("output.metrics.job.name"), config.get("output.metrics.topic.name"));
			this.service = (service == null ? new MVCProcessorService() : service);
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