/**
 * 
 */
package org.ekstep.jobs.samza.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.dto.ExecutionContext;
import org.ekstep.common.dto.HeaderParam;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.telemetry.TelemetryGenerator;
import org.ekstep.telemetry.TelemetryParams;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author gauraw
 *
 */
public class TelemetryV3ContentAuditService implements ISamzaService {

	static JobLogger LOGGER = new JobLogger(TelemetryV3ContentAuditService.class);
	private Config config = null;
	private static ObjectMapper mapper = new ObjectMapper();

	public TelemetryV3ContentAuditService() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.jobs.samza.service.ISamzaService#initialize(org.apache.samza.
	 * config.Config)
	 */
	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		JSONUtils.loadProperties(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.jobs.samza.service.ISamzaService#processMessage(java.util.Map,
	 * org.ekstep.jobs.samza.service.task.JobMetrics,
	 * org.apache.samza.task.MessageCollector)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector)
			throws Exception {
		LOGGER.debug("Telemetry Audit Started.");
		String objectId = message.get("nodeUniqueId").toString();
		Map<String, Object> transactionData = (Map<String, Object>) message.get("transactionData");
		Map<String, Object> propertyMap = (Map<String, Object>) transactionData.get("properties");
		Map<String, Object> statusMap = (Map<String, Object>) propertyMap.get("status");

		String prevStatus = "";
		String currStatus = "";

		if (null != statusMap) {
			prevStatus = statusMap.get("ov").toString();
			currStatus = statusMap.get("nv").toString();
		}

		if (prevStatus != currStatus) {
			try {
				LOGGER.debug("Content Status Change Detected.");
				String objectType = ((Map<String, Object>) propertyMap.get("IL_FUNC_OBJECT_TYPE")).get("nv").toString();
				List<String> props = propertyMap.keySet().stream().collect(Collectors.toList());
				Map<String, String> context = getContext();
				context.put("objectId", objectId);
				context.put("objectType", objectType);
				String auditMessage = TelemetryGenerator.audit(context, props, currStatus, prevStatus);
				LOGGER.debug("Audit Message : " + auditMessage);
				Map<String, Object> auditMap = mapper.readValue(auditMessage, new TypeReference<Map<String, Object>>() {
				});
				collector.send(new OutgoingMessageEnvelope(
						new SystemStream("kafka", config.get("telemetry_audit_topic")), auditMap));
				LOGGER.debug("Telemetry Audit Message Sent to Topic : " + config.get("telemetry_audit_topic"));
			} catch (Exception e) {
				LOGGER.error("Failed to process message", message, e);
			}

		}
	}

	private static Map<String, String> getContext() {
		Map<String, String> context = new HashMap<String, String>();
		context.put(TelemetryParams.ACTOR.name(),
				getContextValue(TelemetryParams.ACTOR.name(), "org.ekstep.learning.platform"));
		context.put(TelemetryParams.CHANNEL.name(), getContextValue(HeaderParam.CHANNEL_ID.name(), "in.ekstep"));
		context.put(TelemetryParams.ENV.name(), getContextValue(TelemetryParams.ENV.name(), "system"));
		return context;
	}

	private static String getContextValue(String key, String defaultValue) {
		String value = (String) ExecutionContext.getCurrent().getGlobalContext().get(key);
		if (StringUtils.isBlank(value))
			return defaultValue;
		else
			return value;
	}

}
