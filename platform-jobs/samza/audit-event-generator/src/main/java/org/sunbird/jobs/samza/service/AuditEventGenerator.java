package org.sunbird.jobs.samza.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.sunbird.common.Platform;
import org.sunbird.graph.common.DateUtils;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemProperties;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.graph.model.node.RelationDefinition;
import org.sunbird.jobs.samza.service.task.JobMetrics;
import org.sunbird.jobs.samza.util.JSONUtils;
import org.sunbird.jobs.samza.util.JobLogger;
import org.sunbird.learning.router.LearningRequestRouterPool;
import org.sunbird.learning.util.ControllerUtil;
import org.sunbird.telemetry.TelemetryGenerator;
import org.sunbird.telemetry.TelemetryParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author gauraw
 *
 */
public class AuditEventGenerator implements ISamzaService {

	private static JobLogger LOGGER = new JobLogger(AuditEventGenerator.class);
	private Config config = null;
	private static ObjectMapper mapper = new ObjectMapper();
	private SystemStream systemStream = null;
	private static List<String> systemPropsList = null;
	private ControllerUtil util = new ControllerUtil();
	private static final String IMAGE_SUFFIX = ".img";
	private static final String OBJECT_TYPE_IMAGE_SUFFIX = "Image";
	private static final String SKIP_AUDIT = "{\"object\": {\"type\":null}}";

	static {
		systemPropsList = Stream.of(SystemProperties.values()).map(SystemProperties::name).collect(Collectors.toList());
		systemPropsList.addAll(Arrays.asList("SYS_INTERNAL_LAST_UPDATED_ON", "lastUpdatedOn", "versionKey","lastStatusChangedOn"));
	}

	public AuditEventGenerator() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.jobs.samza.service.ISamzaService#initialize(org.apache.samza.
	 * config.Config)
	 */
	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		JSONUtils.loadProperties(config);
		TelemetryGenerator.setComponent("audit-event-generator");
		LOGGER.info("Initializing Actor System...");
		LearningRequestRouterPool.init();
		systemStream = new SystemStream("kafka", config.get("telemetry_raw_topic"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.jobs.samza.service.ISamzaService#processMessage(java.util.Map,
	 * org.sunbird.jobs.samza.service.task.JobMetrics,
	 * org.apache.samza.task.MessageCollector)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector)
			throws Exception {
		LOGGER.info("Input Message Received for : [" + message.get("nodeUniqueId") + "], Txn Event createdOn:"
				+ message.get("createdOn") + ", Operation Type:" + message.get("operationType"));
		try {
			Map<String, Object> auditMap = getAuditMessage(message);
			String objectType = (String) ((Map<String, Object>) auditMap.get("object")).get("type");
			if (StringUtils.isNotBlank(objectType)) {
				collector.send(new OutgoingMessageEnvelope(systemStream, auditMap));
				LOGGER.info("Telemetry Audit Message Successfully Sent for : "
						+ (String) ((Map<String, Object>) auditMap.get("object")).get("id"));
				metrics.incSuccessCounter();
			} else {
				LOGGER.info("Skipped event as the objectype is not available, event =" + auditMap);
				metrics.incSkippedCounter();
			}
		} catch (Exception e) {
			metrics.incErrorCounter();
			LOGGER.error("Failed to process message", message, e);
		}
	}

	/**
	 * @param channelId
	 * @param env
	 * @return
	 */
	private static Map<String, String> getContext(String channelId, String env) {
		Map<String, String> context = new HashMap<String, String>();
		context.put(TelemetryParams.ACTOR.name(), "org.sunbird.learning.platform");
		context.put(TelemetryParams.CHANNEL.name(), channelId);
		context.put(TelemetryParams.ENV.name(), env);
		return context;
	}

	/**
	 * @param message
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getAuditMessage(Map<String, Object> message) throws Exception {
		Map<String, Object> auditMap = new HashMap<>();
		String objectId = (String) message.get(GraphDACParams.nodeUniqueId.name());
		String objectType = (String) message.get(GraphDACParams.objectType.name());
		String env = (null != objectType) ? objectType.toLowerCase().replace("image", "") : "system";
		String graphId = (String) message.get(GraphDACParams.graphId.name());
		String userId = (String) message.get(GraphDACParams.userId.name());
		DefinitionDTO definitionNode = util.getDefinition(graphId, objectType);
		Map<String, String> inRelations = new HashMap<>();
		Map<String, String> outRelations = new HashMap<>();
		getRelationDefinitionMaps(definitionNode, inRelations, outRelations);

		String channelId = Platform.config.getString("channel.default");
		String channel = (String) message.get(GraphDACParams.channel.name());
		if (null != channel)
			channelId = channel;
		Map<String, Object> transactionData = (Map<String, Object>) message.get(GraphDACParams.transactionData.name());
		Map<String, Object> propertyMap = (Map<String, Object>) transactionData.get(GraphDACParams.properties.name());
		Map<String, Object> statusMap = (Map<String, Object>) propertyMap.get(GraphDACParams.status.name());
		Map<String, Object> lastStatusChangedOn = (Map<String, Object>) propertyMap.get("lastStatusChangedOn");
		List<Map<String, Object>> addedRelations = (List<Map<String, Object>>) transactionData
				.get(GraphDACParams.addedRelations.name());
		List<Map<String, Object>> removedRelations = (List<Map<String, Object>>) transactionData
				.get(GraphDACParams.removedRelations.name());

		String pkgVersion = "";
		Map<String, Object> pkgVerMap = (Map<String, Object>) propertyMap.get("pkgVersion");
		if (null != pkgVerMap)
			pkgVersion = String.valueOf(pkgVerMap.get("nv"));

		String prevStatus = "";
		String currStatus = "";
		String duration = "";
		if (null != statusMap) {
			prevStatus = (String) statusMap.get("ov");
			currStatus = (String) statusMap.get("nv");
			// Compute Duration for Status Change
			if (StringUtils.isNotBlank(currStatus) && StringUtils.isNotBlank(prevStatus) && null != lastStatusChangedOn) {
				String ov = (String) lastStatusChangedOn.get("ov");
				String nv = (String) lastStatusChangedOn.get("nv");
				if (null == ov) {
					ov = (String) ((Map<String, Object>) propertyMap.get("lastUpdatedOn")).get("ov");
				}
				if (null != ov && null != nv) {
					duration = String.valueOf(computeDuration(ov, nv));
				}
			}
		}
		List<String> props = propertyMap.keySet().stream().collect(Collectors.toList());
		props.addAll(getRelationProps(addedRelations, inRelations, outRelations));
		props.addAll(getRelationProps(removedRelations, inRelations, outRelations));
		List<String> propsExceptSystemProps = props.stream().filter(prop -> !systemPropsList.contains(prop))
				.collect(Collectors.toList());
		List<Map<String, Object>> cdata = getCData(addedRelations, removedRelations, propertyMap);
		Map<String, String> context = getContext(channelId, env);
		objectId = (null != objectId) ? objectId.replaceAll(IMAGE_SUFFIX, "") : objectId;
		objectType = (null != objectType) ? objectType.replaceAll(OBJECT_TYPE_IMAGE_SUFFIX, "") : objectType;
		context.put("objectId", objectId);
		context.put(GraphDACParams.objectType.name(), objectType);
		if (StringUtils.isNotBlank(duration))
			context.put("duration", duration);
		if (StringUtils.isNotBlank(pkgVersion))
			context.put("pkgVersion", pkgVersion);
		if (StringUtils.isNotBlank(userId))
			context.put(TelemetryParams.ACTOR.name(), userId);
		if (!CollectionUtils.isEmpty(propsExceptSystemProps)) {
			String auditMessage = TelemetryGenerator.audit(context, propsExceptSystemProps, currStatus, prevStatus,
					cdata);
			//LOGGER.info("Audit Message for Content Id [" + objectId + "] : " + auditMessage);
			auditMap = mapper.readValue(auditMessage, new TypeReference<Map<String, Object>>() {
			});
		} else {
			LOGGER.info("Skipping Audit log as props is null or empty");
			auditMap = mapper.readValue(SKIP_AUDIT, new TypeReference<Map<String, Object>>() {
			});
		}
		return auditMap;
	}

	/**
	 * @param addedRelations
	 * @param removedRelations
	 * @param propertyMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getCData(List<Map<String, Object>> addedRelations,
			List<Map<String, Object>> removedRelations, Map<String, Object> propertyMap) {

		List<Map<String, Object>> cdata = new ArrayList<Map<String, Object>>();

		if (null != propertyMap && !propertyMap.isEmpty() && propertyMap.containsKey("dialcodes")) {
			Map<String, Object> dialcodeMap = (Map<String, Object>) propertyMap.get("dialcodes");
			List<String> dialcodes = (List<String>) dialcodeMap.get("nv");
			if (null != dialcodes) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("id", dialcodes);
				map.put("type", "DialCode");
				cdata.add(map);
			}
		}

		if (null != addedRelations && !addedRelations.isEmpty())
			prepareCData(cdata, addedRelations);

		if (null != removedRelations && !removedRelations.isEmpty())
			prepareCData(cdata, addedRelations);

		return cdata;
	}

	/**
	 * @param cdata
	 * @param relations
	 */
	private void prepareCData(List<Map<String, Object>> cdata, List<Map<String, Object>> relations) {
		for (Map<String, Object> relation : relations) {
			HashMap<String, Object> cMap = new HashMap<String, Object>();
			cMap.put("id", relation.get("id"));
			cMap.put("type", relation.get("type"));
			cdata.add(cMap);
		}
	}

	/**
	 *
	 * @param relations
	 * @param inRelations
	 * @param outRelations
	 * @return
	 */
	private List<String> getRelationProps(List<Map<String, Object>> relations, Map<String, String> inRelations,
			Map<String, String> outRelations) {
		List<String> props = new ArrayList<>();
		if (null != relations && !relations.isEmpty()) {
			for (Map<String, Object> relation : relations) {
				String key = (String) relation.get("rel") + (String) relation.get("type");
				if (StringUtils.equalsIgnoreCase((String) relation.get("dir"), "IN")) {
					if (null != inRelations.get(key + "in"))
						props.add(inRelations.get(key + "in"));
				} else {
					if (null != outRelations.get(key + "out"))
						props.add(outRelations.get(key + "out"));
				}
			}
		}
		return props;

	}

	/**
	 * @param definition
	 * @param inRelations
	 * @param outRelations
	 */
	private void getRelationDefinitionMaps(DefinitionDTO definition, Map<String, String> inRelations,
			Map<String, String> outRelations) {
		if (null != definition) {
			if (null != definition.getInRelations() && !definition.getInRelations().isEmpty()) {
				for (RelationDefinition rDef : definition.getInRelations()) {
					getRelationDefinitionKey(rDef, inRelations, "in");
				}
			}
			if (null != definition.getOutRelations() && !definition.getOutRelations().isEmpty()) {
				for (RelationDefinition rDef : definition.getOutRelations()) {
					getRelationDefinitionKey(rDef, outRelations, "out");
				}
			}
		}
	}

	/**
	 * @param rDef
	 * @param relDefMap
	 * @param rel
	 */
	private static void getRelationDefinitionKey(RelationDefinition rDef, Map<String, String> relDefMap, String rel) {
		if (null != rDef.getObjectTypes() && !rDef.getObjectTypes().isEmpty()) {
			for (String type : rDef.getObjectTypes()) {
				String key = rDef.getRelationName() + type + rel;
				relDefMap.put(key, rDef.getTitle());
			}
		}
	}

	/**
	 * @param oldDate
	 * @param newDate
	 * @return
	 */
	public Long computeDuration(String oldDate, String newDate) {
		Date od = DateUtils.parse(oldDate);
		Date nd = DateUtils.parse(newDate);
		long diff = nd.getTime() - od.getTime();
		long diffSeconds = diff / 1000;
		return diffSeconds;
	}
}