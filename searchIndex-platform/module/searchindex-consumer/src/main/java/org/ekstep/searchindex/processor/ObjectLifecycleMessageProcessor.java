package org.ekstep.searchindex.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.learning.util.ControllerUtil;

import com.ilimi.common.logger.LogHelper;
import com.ilimi.common.util.LogTelemetryEventUtil;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

/**
 * The Class ObjectLifecycleMessageProcessor is a kafka consumer which provides
 * implementations of the object lifecycle operations defined in the
 * IMessageProcessor along with the methods to generate lifecycle event for all
 * platform objects
 * 
 * @author Rashmi
 * 
 * @see IMessageProcessor
 */
public class ObjectLifecycleMessageProcessor implements IMessageProcessor {

	/** The LOGGER */
	private static LogHelper LOGGER = LogHelper.getInstance(ObjectLifecycleMessageProcessor.class.getName());

	/** The ObjectMapper */
	private ObjectMapper mapper = new ObjectMapper();

	/** The controllerUtil */
	private ControllerUtil util = new ControllerUtil();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.searchindex.processor #processMessage(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@Override
	public void processMessage(String messageData) {
		try {
			Map<String, Object> message = new HashMap<String, Object>();
			if (StringUtils.isNotBlank(messageData)) {
				LOGGER.info("Reading from kafka consumer" + messageData);
				message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
				});
			}
			if (null != message)
				processMessage(message);
		} catch (Exception e) {
			LOGGER.error("Error while processing kafka message", e);
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.searchindex.processor #processMessage(java.lang.String
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void processMessage(Map<String, Object> message) throws Exception {
		Map<String, Object> objectMap = new HashMap<String, Object>();
		try {
			LOGGER.info("Checking if kafka message contains transactionData" + message.containsKey("transactionData"));
			if (message.containsKey("transactionData")) {
				Map<String, Object> transactionMap = (Map<String, Object>) message.get("transactionData");

				LOGGER.info(
						"Checking tarnsactionData contains propertiesMap" + transactionMap.containsKey("properties"));
				Map<String, Object> propertiesMap = (Map<String, Object>) transactionMap.get("properties");

				LOGGER.info("Checking if propertiesMap contains status" + propertiesMap.containsKey("status"));
				if (propertiesMap.containsKey("status")) {
					Map<String, Object> statusMap = (Map) propertiesMap.get("status");

					LOGGER.info("Setting prevState and current state for event generation");
					String prevstate = (String) statusMap.get("ov");
					String state = (String) statusMap.get("nv");

					LOGGER.info("prevstate of object:" + prevstate + "currentstate of object:" + state);
					objectMap.put("prevstate", prevstate);
					objectMap.put("state", state);
					String node_id = (String) message.get("nodeUniqueId");

					LOGGER.info("Checking if node_id is blank" + node_id);
					if (StringUtils.isNotBlank(node_id)) {

						LOGGER.info("Fetching Node metadata from graph" + node_id);
						Node node = util.getNode("domain", (String) message.get("nodeUniqueId"));
						objectMap.put("identifier", node.getIdentifier());
						objectMap.put("objectType", node.getObjectType());

						LOGGER.info("Checking if node metadata is null");
						if (null != node.getMetadata()) {
							Map<String, Object> nodeMap = new HashMap<String, Object>();
							nodeMap = (Map) node.getMetadata();

							LOGGER.info("Iterating over node metadata");
							for (Map.Entry<String, Object> entry : nodeMap.entrySet()) {
								if (entry.getKey().equals("name")) {
									LOGGER.info("Setting name field from node" + entry.getKey() + entry.getValue());
									objectMap.put("name", entry.getValue());
								}
								if (entry.getKey().equals("code")) {
									LOGGER.info("Setting code field from node" + entry.getKey() + entry.getValue());
									objectMap.put("code", entry.getValue());
								}
								if (entry.getKey().equals("contentType")) {
									if (entry.getValue().equals("Asset")) {
										LOGGER.info("Setting subtype field from mediaType" + entry.getKey()
												+ entry.getValue());
										objectMap.put("subtype", nodeMap.get("mediaType"));
									} else {
										LOGGER.info("Setting subType field form contentType" + entry.getKey()
												+ entry.getValue());
										objectMap.put("subtype", entry.getValue());
									}
								} else if (entry.getKey().equals("type")) {
									LOGGER.info("Setting subType field for type from node" + entry.getKey()
											+ entry.getValue());
									objectMap.put("subtype", entry.getValue());
								}
							}
							LOGGER.info("Getting relations from node");
							if (null != node.getInRelations()) {
								List<Relation> relations = node.getInRelations();
								for (Relation rel : relations) {
									if (rel.getEndNodeObjectType().equals("Content"))
										if (rel.getRelationType().equals("hasSequenceMember")) {
											objectMap.put("parentid", rel.getEndNodeId());
										} else if (rel.getEndNodeObjectType().equals("hasMember")) {
											objectMap.put("parentid", rel.getEndNodeId());
										}
								}
							}
						}
						LOGGER.info("Logging Telemetry for BE_OBJECT_LIFECYCLE event" + node_id);
						LogTelemetryEventUtil.logObjectLifecycleEvent(node_id, objectMap);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Something went wrong while processing the request", e);
		}
	}
}
