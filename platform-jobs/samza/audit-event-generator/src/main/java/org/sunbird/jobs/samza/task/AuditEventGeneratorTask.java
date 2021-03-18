package org.sunbird.jobs.samza.task;

import java.util.HashMap;
import java.util.Map;

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
import org.sunbird.jobs.samza.service.AuditEventGenerator;
import org.sunbird.jobs.samza.service.ISamzaService;
import org.sunbird.jobs.samza.service.task.JobMetrics;
import org.sunbird.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.util.SamzaCommonParams;
import org.sunbird.learning.util.ControllerUtil;

public class AuditEventGeneratorTask extends BaseTask {

	private static JobLogger LOGGER = new JobLogger(AuditEventGeneratorTask.class);

	private JobMetrics metrics;
	private ISamzaService auditEventGenerator = new AuditEventGenerator();
	private ControllerUtil controllerUtil = new ControllerUtil();

	@Override
	public void init(Config config, TaskContext context) throws Exception {

		try {
			metrics = new JobMetrics(context, config.get("output.metrics.job.name"), config.get("output.metrics.topic.name"));
			auditEventGenerator.initialize(config);
			LOGGER.info("Task initialized");
		} catch (Exception ex) {
			LOGGER.error("Task initialization failed", ex);
			throw ex;
		}
	}

	@Override
	public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator)
			throws Exception {
		Map<String, Object> outgoingMap = getMessage(envelope);
		try {
			if (outgoingMap.containsKey(SamzaCommonParams.edata.name())) {
				Map<String, Object> edata = (Map<String, Object>) outgoingMap.getOrDefault(SamzaCommonParams.edata.name(), new HashMap<String, Object>());
				if (MapUtils.isNotEmpty(edata) && StringUtils.equalsIgnoreCase("definition_update", edata.getOrDefault("action", "").toString())) {
					LOGGER.info("Definition Update event received for objectType: "+ edata.getOrDefault("objectType", "").toString());
					String graphId = edata.getOrDefault("graphId", "").toString();
					String objectType = edata.getOrDefault("objectType", "").toString();
					controllerUtil.updateDefinitionCache(graphId, objectType);
				}
			} else {
				auditEventGenerator.processMessage(outgoingMap, metrics, collector);
			}
		} catch (Exception e) {
			metrics.incErrorCounter();
			LOGGER.error("Message processing Error", e);
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
	public void window(MessageCollector collector, TaskCoordinator coordinator) throws Exception {
		Map<String, Object> event = metrics.collect();
		collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", metrics.getTopic()), event));
		metrics.clear();
	}
}
