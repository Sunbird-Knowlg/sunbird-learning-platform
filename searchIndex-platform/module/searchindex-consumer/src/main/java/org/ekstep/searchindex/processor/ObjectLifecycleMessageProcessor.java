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
				if (transactionMap.containsKey("properties")) {
					Map<String, Object> propertiesMap = (Map<String, Object>) transactionMap.get("properties");

					LOGGER.info("Checking if propertiesMap contains status" + propertiesMap.containsKey("status"));
					if (propertiesMap.containsKey("status")) {
						Map<String, Object> statusMap = (Map) propertiesMap.get("status");

						LOGGER.info("Setting prevState and current state for event generation");
						String prevstate = (String) statusMap.get("ov");
						String state = (String) statusMap.get("nv");

						LOGGER.info("Checking if node_id is blank" + message.get("nodeUniqueId"));
						if (StringUtils.isNotBlank((String) message.get("nodeUniqueId"))) {

							LOGGER.info("Fetching Node metadata from graph" + message.get("nodeUniqueId"));
							Node node = util.getNode("domain", (String) message.get("nodeUniqueId"));

							String node_id = node.getIdentifier();
							String objectType = node.getObjectType();

							LOGGER.info("prevstate of object:" + prevstate + "currentstate of object:" + state);
							if (StringUtils.equalsIgnoreCase(objectType, "ContentImage")
									&& StringUtils.equalsIgnoreCase(prevstate, null)
									&& StringUtils.equalsIgnoreCase(state, "Draft")) {
								LOGGER.info("Cropping img part from node_id" + node_id);
								String imageId = node_id.replace(".img", "");
								Node imageNode = util.getNode("domain", imageId);
								String node_status = (String)imageNode.getMetadata().get("status");
								LOGGER.info("Checking if node_status is flagged" + node_status);
								if (StringUtils.equalsIgnoreCase(node_status, "Flagged")) {
									objectMap.put("prevstate", "Flagged");
									objectMap.put("state", "FlagDraft");
								} else {
									objectMap.put("prevstate", "Live");
									objectMap.put("state", "Draft");
								}
							}
							objectMap.put("prevstate", prevstate);
							objectMap.put("state", state);
							if (StringUtils.endsWithIgnoreCase(node_id, ".img")
									&& StringUtils.endsWithIgnoreCase(objectType, "Image")) {
								LOGGER.info("Setting nodeId and objectType" + node_id + objectType);
								node_id = StringUtils.replace(node_id, ".img", "");
								objectType = StringUtils.replace(objectType, "Image", "");
							}
							objectMap.put("identifier", node_id);
							objectMap.put("objectType", objectType);
							LOGGER.info("Object Map" + objectMap);
							LOGGER.info("Checking if node metadata is null");
							if (null != node.getMetadata()) {
								Map<String, Object> nodeMap = new HashMap<String, Object>();
								nodeMap = (Map) node.getMetadata();
								if (nodeMap.containsKey("name"))
									objectMap.put("name", nodeMap.get("name"));
								if (nodeMap.containsKey("code"))
									objectMap.put("code", nodeMap.get("code"));
							}
							switch (objectType) {
							case "Content":
								setContentMetadata(node, objectMap);
								break;
							case "AssessmentItem":
								setItemMetadata(node, objectMap);
								break;
							case "ItemSet":
								setItemMetadata(node, objectMap);
								break;
							case "Concept":
								setConceptMetadata(node, objectMap);
								break;
							case "Dimension":
								setDimensionMetadata(node, objectMap);
								break;
							default:
								setDefaultMetadata(node, objectMap);
								break;
							}
							for(Map.Entry<String, Object> entry : objectMap.entrySet()){
								if(null == entry.getValue()){
									entry.setValue("");
								}
							} 
							LOGGER.info("Logging Telemetry for BE_OBJECT_LIFECYCLE event" + node_id);
							LogTelemetryEventUtil.logObjectLifecycleEvent(node_id, objectMap);
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Something occured while processing request to generate lifecycle event", e);
		}
	}

	/**
	 * This method holds logic to set metadata data to generate object lifecycle
	 * events for objectType concept
	 * 
	 * @param node
	 * @param objectMap
	 */
	private void setConceptMetadata(Node node, Map<String, Object> objectMap) {
		if (null != node.getInRelations()) {
			List<Relation> relations = node.getInRelations();
			for (Relation rel : relations) {
				if (rel.getEndNodeObjectType().equals("Concept") && rel.getRelationType().equals("isParentOf")) {
					LOGGER.info("Setting parentid for concept" + rel.getEndNodeId());
					objectMap.put("parentid", rel.getEndNodeId());
				} else if (rel.getEndNodeObjectType().equals("Dimension")
						&& rel.getRelationType().equals("isParentof")) {
					LOGGER.info("Setting parentid for relEndNodeType : Dimension" + rel.getEndNodeObjectType()
							+ rel.getEndNodeId());
					objectMap.put("parentid", rel.getEndNodeId());
					objectMap.put("parenttype", rel.getEndNodeObjectType());
				}
			}
		} else if (null != node.getOutRelations()) {
			List<Relation> relations = node.getOutRelations();
			for (Relation rel : relations) {
				if (rel.getEndNodeObjectType().equals("Concept") && rel.getRelationType().equals("isParentOf")) {
					LOGGER.info("Setting parentid for concept - outRelations of type concepts"
							+ rel.getEndNodeObjectType() + rel.getEndNodeId());
					objectMap.put("parentid", rel.getEndNodeId());
					objectMap.put("parenttype", rel.getEndNodeObjectType());
				}
			}
		}
	}

	/**
	 * This method holds logic to set metadata data to generate object lifecycle
	 * events for objectType dimensions
	 * 
	 * @param node
	 * @param objectMap
	 */
	private void setDimensionMetadata(Node node, Map<String, Object> objectMap) {
		if (null != node.getInRelations()) {
			List<Relation> relations = node.getInRelations();
			for (Relation rel : relations) {
				if (rel.getEndNodeObjectType().equals("Domain") && rel.getRelationType().equals("isParentOf")) {
					LOGGER.info("Setting parentid for dimension" + rel.getEndNodeObjectType() + rel.getEndNodeId());
					objectMap.put("parentid", rel.getEndNodeId());
				}
			}
		}
	}

	/**
	 * This method holds logic to set metadata data to generate object lifecycle
	 * events for objectType others
	 * 
	 * @param node
	 * @param objectMap
	 */
	private void setDefaultMetadata(Node node, Map<String, Object> objectMap) {
		objectMap.put("subtype", "");
		objectMap.put("parentid", "");
		objectMap.put("parenttype", "");
	}

	/**
	 * This method holds logic to set metadata data to generate object lifecycle
	 * events for objectType content
	 * 
	 * @param node
	 * @param objectMap
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setContentMetadata(Node node, Map<String, Object> objectMap) {
		if (null != node.getMetadata()) {
			Map<String, Object> nodeMap = new HashMap<String, Object>();
			nodeMap = (Map) node.getMetadata();
			for (Map.Entry<String, Object> entry : nodeMap.entrySet()) {
				if (entry.getKey().equals("contentType")) {
					if (entry.getValue().equals("Asset")) {
						LOGGER.info("Setting subtype field from mediaType" + entry.getKey() + entry.getValue());
						objectMap.put("subtype", nodeMap.get("mediaType"));
					} else {
						LOGGER.info("Setting subType field form contentType" + entry.getKey() + entry.getValue());
						objectMap.put("subtype", entry.getValue());
					}
				}
			}
		}
		LOGGER.info("Checking if objectType content has inRelations" + node.getInRelations());
		if (null != node.getInRelations()) {
			List<Relation> relations = node.getInRelations();
			for (Relation rel : relations) {
				if (rel.getEndNodeObjectType().equals("Content") && rel.getRelationType().equals("hasSequenceMember")) {
					LOGGER.info("Setting parentid for Content with inRelations" + rel.getEndNodeId());
					objectMap.put("parentid", rel.getEndNodeId());
					objectMap.put("parenttype", rel.getEndNodeObjectType());
				}
			}
		} else if (null != node.getOutRelations()) {
			List<Relation> relations = node.getOutRelations();
			for (Relation rel : relations) {
				if (rel.getEndNodeObjectType().equals("Content") && rel.getRelationType().equals("hasSequenceMember")) {
					LOGGER.info("Setting parentid for Content with outRelations" + rel.getEndNodeId());
					objectMap.put("parentid", rel.getEndNodeId());
					objectMap.put("parenttype", rel.getEndNodeObjectType());
				}
			}
		}
	}

	/**
	 * This method holds logic to set metadata data to generate object lifecycle
	 * events for objectType item or assessmentitem
	 * 
	 * @param node
	 * @param objectMap
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setItemMetadata(Node node, Map<String, Object> objectMap) {
		if (null != node.getMetadata()) {
			Map<String, Object> nodeMap = new HashMap<String, Object>();
			nodeMap = (Map) node.getMetadata();
			for (Map.Entry<String, Object> entry : nodeMap.entrySet()) {
				if (entry.getKey().equals("type")) {
					LOGGER.info("Setting subType field for type from node" + entry.getKey() + entry.getValue());
					objectMap.put("type", entry.getValue());
				}
			}
		}
		if (StringUtils.equalsIgnoreCase(node.getObjectType(), "AssessmentItem")) {
			LOGGER.info("Getting relations from AssessmentItem");
			if (null != node.getInRelations()) {
				List<Relation> relations = node.getInRelations();
				for (Relation rel : relations) {
					if (rel.getEndNodeObjectType().equals("ItemSet") && rel.getRelationType().equals("hasMember")) {
						LOGGER.info("Setting parentid for assessmentitem" + rel.getEndNodeId());
						objectMap.put("parentid", rel.getEndNodeId());
						objectMap.put("parenttype", rel.getEndNodeObjectType());
					}
				}
			}
		}
	}
}