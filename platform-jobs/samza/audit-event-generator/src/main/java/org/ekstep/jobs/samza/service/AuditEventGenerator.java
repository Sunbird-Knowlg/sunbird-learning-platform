/**
 * 
 */
package org.ekstep.jobs.samza.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.samza.config.Config;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.SystemProperties;
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
public class AuditEventGenerator implements ISamzaService {

	static JobLogger LOGGER = new JobLogger(AuditEventGenerator.class);
	private Config config = null;
	private static ObjectMapper mapper = new ObjectMapper();
	private SystemStream systemStream = null;
	private static List<String> systemPropsList = Stream.of(SystemProperties.values())
            .map(SystemProperties::name).collect(Collectors.toList());

	public AuditEventGenerator() {
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
		systemStream = new SystemStream("kafka", config.get("telemetry_raw_topic"));
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
		try {
			Map<String, Object> auditMap = getAuditMessage(message);
			String objectType = (String) ((Map<String, Object>) auditMap.get("object")).get("type");
			if (null != objectType) {
				collector.send(new OutgoingMessageEnvelope(systemStream, auditMap));
				LOGGER.debug("Telemetry Audit Message Sent to Topic : " + config.get("telemetry_raw_topic"));
				metrics.incSuccessCounter();
			} else {
				LOGGER.info("skipped event as the objectype is not available, event ="+auditMap);
				metrics.incSkippedCounter();
			}
		} catch (Exception e) {
			metrics.incErrorCounter();
			LOGGER.error("Failed to process message", message, e);
		}
	}

	private static Map<String, String> getContext(String channelId) {
		Map<String, String> context = new HashMap<String, String>();
		context.put(TelemetryParams.ACTOR.name(), "org.ekstep.learning.platform");
		context.put(TelemetryParams.CHANNEL.name(), channelId);
		context.put(TelemetryParams.ENV.name(), "system");
		return context;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getAuditMessage(Map<String, Object> message) throws Exception {
		Map<String, Object> auditMap = null;
		String objectId = (String) message.get(GraphDACParams.nodeUniqueId.name());
		String objectType = (String) message.get(GraphDACParams.objectType.name());
		String channelId = "in.ekstep";
		String channel = (String) message.get(GraphDACParams.channel.name());
		if (null != channel)
			channelId = channel;
		Map<String, Object> transactionData = (Map<String, Object>) message.get(GraphDACParams.transactionData.name());
		Map<String, Object> propertyMap = (Map<String, Object>) transactionData.get(GraphDACParams.properties.name());
		Map<String, Object> statusMap = (Map<String, Object>) propertyMap.get(GraphDACParams.status.name());

		String prevStatus = "";
		String currStatus = "";
		if (null != statusMap) {
			prevStatus = (String) statusMap.get("ov");
			currStatus = (String) statusMap.get("nv");
		}
		List<String> props = propertyMap.keySet().stream().collect(Collectors.toList());
		List<String> propsExceptSystemProps = props.stream()
				.filter(prop -> !systemPropsList.contains(prop))
				.collect(Collectors.toList());
		
		Map<String, String> context = getContext(channelId);
		context.put("objectId", objectId);
		context.put(GraphDACParams.objectType.name(), objectType);
		String auditMessage = TelemetryGenerator.audit(context, propsExceptSystemProps, currStatus, prevStatus);
		LOGGER.debug("Audit Message : " + auditMessage);
		auditMap = mapper.readValue(auditMessage, new TypeReference<Map<String, Object>>() {
		});

		return auditMap;
	}

}
