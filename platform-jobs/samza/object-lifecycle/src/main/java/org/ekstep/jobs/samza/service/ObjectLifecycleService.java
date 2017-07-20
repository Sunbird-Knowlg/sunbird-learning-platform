package org.ekstep.jobs.samza.service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.model.Event;
import org.ekstep.jobs.samza.model.LifecycleEvent;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.ConsumerWorkflowEnums;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.cache.factory.JedisFactory;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class ObjectLifecycleService implements ISamzaService {
	
	private JobLogger LOGGER = new JobLogger(ObjectLifecycleService.class);

	private MessageDigest digest = null;

	private Config config = null;

	private ControllerUtil util = new ControllerUtil();

	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		Map<String, Object> props = new HashMap<String, Object>();
		for (Entry<String, String> entry : config.entrySet()) {
			props.put(entry.getKey(), entry.getValue());
		}
		Configuration.loadProperties(props);
		LOGGER.info("Service config initialized");
		digest = MessageDigest.getInstance("MD5");
		LearningRequestRouterPool.init();
		LOGGER.info("Learning actors initialized");
		JedisFactory.initialize(props);
		LOGGER.info("Redis connection factory initialized");
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
		Map<String, Object> stateChangeEvent = getStateChangeEvent(message);
		if (stateChangeEvent != null) {
			LOGGER.debug("State change identified - creating lifecycle event");
			try {
				Node node = getNode(message);
				if(null == node) {
					metrics.incSkippedCounter();
					return;
				}
				LOGGER.info("Node fetched from graph");
				Event event = generateLifecycleEvent(stateChangeEvent, node);
				event.setEts(message);
				LOGGER.info("Event generated");
				publishEvent(event, collector);
				LOGGER.info("Event published");
				metrics.incSuccessCounter();
			} catch (Exception ex) {
				metrics.incFailedCounter();
				LOGGER.error("Failed to process message", message, ex);
			}
		} else {
			LOGGER.info("Learning event not qualified for lifecycle event");
			metrics.incSkippedCounter();
		}
	}

	private String getMD5Hash(String event) {
		digest.update(event.getBytes(), 0, event.length());
		return new BigInteger(1, digest.digest()).toString(16);
	}

	private void publishEvent(Event event, MessageCollector collector) throws Exception {
		event.setMid("LP:" + getMD5Hash(event.toString()));
		collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", config.get("object_lifecycle_topic")), event.getMap()));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getStateChangeEvent(Map<String, Object> message) {

		if (!message.containsKey(ConsumerWorkflowEnums.nodeUniqueId.name()))
			return null;
		if (!message.containsKey(ConsumerWorkflowEnums.transactionData.name()))
			return null;
		Map<String, Object> transactionMap = (Map<String, Object>) message.get(ConsumerWorkflowEnums.transactionData.name());

		if (!transactionMap.containsKey(ConsumerWorkflowEnums.properties.name()))
			return null;
		Map<String, Object> propertiesMap = (Map<String, Object>) transactionMap.get(ConsumerWorkflowEnums.properties.name());

		if (propertiesMap.containsKey(ConsumerWorkflowEnums.status.name())) {
			return (Map<String, Object>) propertiesMap.get(ConsumerWorkflowEnums.status.name());
		}

		return null;
	}

	private Node getNode(Map<String, Object> message) throws Exception {
		String nodeId = (String) message.get(ConsumerWorkflowEnums.nodeUniqueId.name());
		if (message.get(ConsumerWorkflowEnums.nodeType.name()).equals(ConsumerWorkflowEnums.SET.name())
				&& message.get(ConsumerWorkflowEnums.objectType.name()).equals(ConsumerWorkflowEnums.ItemSet.name())) {
			return getItemSetNode(nodeId);
		} else {
			return util.getNode(ConsumerWorkflowEnums.domain.name(), nodeId);
		}
	}

	private Node getItemSetNode(String identifier) throws Exception {
		ControllerUtil util = new ControllerUtil();
		Response resp = util.getSet(ConsumerWorkflowEnums.domain.name(), identifier);
		Map<String, Object> map = (Map<String, Object>) resp.getResult();
		Node node = (Node) map.get(ConsumerWorkflowEnums.node.name());
		if (null != node) {
			return node;
		}
		return null;
	}

	private Event generateLifecycleEvent(Map<String, Object> stateChangeEvent, Node node) {

		Event event = new Event("BE_OBJECT_LIFECYCLE", "2.1", "ObjectLifecycleTask");
		LifecycleEvent lifecycleEvent = new LifecycleEvent();
		String prevstate = (String) stateChangeEvent.get("ov");
		String state = (String) stateChangeEvent.get("nv");

		String nodeId = node.getIdentifier();
		String objectType = node.getObjectType();

		if (StringUtils.equalsIgnoreCase(objectType, ConsumerWorkflowEnums.ContentImage.name())
				&& StringUtils.equalsIgnoreCase(prevstate, null) && StringUtils.equalsIgnoreCase(state, ConsumerWorkflowEnums.Draft.name())) {
			lifecycleEvent.setPrevstate(ConsumerWorkflowEnums.Live.name());
			lifecycleEvent.setState(ConsumerWorkflowEnums.Draft.name());
		} else if (StringUtils.equalsIgnoreCase(objectType, ConsumerWorkflowEnums.ContentImage.name())
				&& StringUtils.equalsIgnoreCase(prevstate, null)
				&& StringUtils.equalsIgnoreCase(state, ConsumerWorkflowEnums.FlagDraft.name())) {
			lifecycleEvent.setPrevstate(ConsumerWorkflowEnums.Flagged.name());
			lifecycleEvent.setState(ConsumerWorkflowEnums.FlagDraft.name());
		} else {
			prevstate = (prevstate == null) ? "" : prevstate;
			lifecycleEvent.setPrevstate(prevstate);
			lifecycleEvent.setState(state);
		}

		if (StringUtils.endsWithIgnoreCase(nodeId, ".img")
				&& StringUtils.endsWithIgnoreCase(objectType, ConsumerWorkflowEnums.Image.name())) {
			nodeId = StringUtils.replace(nodeId, ".img", "");
			objectType = StringUtils.replace(objectType, ConsumerWorkflowEnums.Image.name(), "");
		}
		lifecycleEvent.setId(nodeId);
		lifecycleEvent.setType(objectType);

		if (null != node.getMetadata()) {
			Map<String, Object> nodeMap = (Map<String, Object>) node.getMetadata();
			if (nodeMap.containsKey(ConsumerWorkflowEnums.name.name())) {
				lifecycleEvent.setName((String) nodeMap.get(ConsumerWorkflowEnums.name.name()));
			}
			if (nodeMap.containsKey(ConsumerWorkflowEnums.code.name())) {
				lifecycleEvent.setCode((String) nodeMap.get(ConsumerWorkflowEnums.code.name()));
			}
			if (nodeMap.containsKey(ConsumerWorkflowEnums.channel.name())) {
				event.setChannel((String) nodeMap.get(ConsumerWorkflowEnums.channel.name()));
			}
		}
		switch (objectType) {
		case "Content":
			setContentMetadata(node, lifecycleEvent);
			break;
		case "AssessmentItem":
			setItemMetadata(node, lifecycleEvent);
			break;
		case "ItemSet":
			setItemSetMetadata(node, lifecycleEvent);
			break;
		case "Concept":
			setConceptMetadata(node, lifecycleEvent);
			break;
		case "Dimension":
			setDimensionMetadata(node, lifecycleEvent);
			break;
		default:
			break;
		}
		event.setEdata(lifecycleEvent);
		return event;
	}

	/**
	 * This method holds logic to set metadata data to generate object lifecycle events for objectType concept
	 * 
	 * @param node
	 * @param objectMap
	 */
	private void setConceptMetadata(Node node, LifecycleEvent event) {
		if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
			List<Relation> relations = node.getInRelations();
			for (Relation rel : relations) {
				if (rel.getEndNodeObjectType().equals(ConsumerWorkflowEnums.Concept.name())
						&& rel.getRelationType().equals(ConsumerWorkflowEnums.isParentOf.name())) {
					event.setParentid(rel.getEndNodeId());
				} else if (rel.getEndNodeObjectType().equals(ConsumerWorkflowEnums.Dimension.name())
						&& rel.getRelationType().equals(ConsumerWorkflowEnums.isParentOf.name())) {
					event.setParentid(rel.getEndNodeId());
					event.setParenttype(rel.getEndNodeObjectType());
				}
			}
		} else if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
			List<Relation> relations = node.getOutRelations();
			for (Relation rel : relations) {
				if (rel.getEndNodeObjectType().equals(ConsumerWorkflowEnums.Concept.name())
						&& rel.getRelationType().equals(ConsumerWorkflowEnums.isParentOf.name())) {
					event.setParentid(rel.getEndNodeId());
					event.setParenttype(rel.getEndNodeObjectType());
				}
			}
		}
	}

	/**
	 * This method holds logic to set metadata data to generate object lifecycle events for objectType dimensions
	 * 
	 * @param node
	 * @param objectMap
	 */
	private void setDimensionMetadata(Node node, LifecycleEvent event) {
		if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
			List<Relation> relations = node.getInRelations();
			for (Relation rel : relations) {
				if (rel.getEndNodeObjectType().equals(ConsumerWorkflowEnums.Domain.name())
						&& rel.getRelationType().equals(ConsumerWorkflowEnums.isParentOf.name())) {
					event.setParentid(rel.getEndNodeId());
					event.setParenttype(rel.getEndNodeObjectType());
				}
			}
		}
	}

	/**
	 * This method holds logic to set metadata data to generate object lifecycle events for objectType content
	 * 
	 * @param node
	 * @param objectMap
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setContentMetadata(Node node, LifecycleEvent event) {
		if (null != node.getMetadata()) {
			Map<String, Object> nodeMap = new HashMap<String, Object>();
			nodeMap = (Map) node.getMetadata();
			if (null != nodeMap && nodeMap.containsKey("contentType")) {
				if (nodeMap.containsValue("Asset")) {
					event.setType((String) nodeMap.get("contentType"));
					event.setSubtype((String) nodeMap.get(ConsumerWorkflowEnums.mediaType.name()));
				} else if (nodeMap.containsValue("Plugin")) {
					if (nodeMap.containsKey(ConsumerWorkflowEnums.category.name())) {
						String[] category = (String[]) nodeMap.get(ConsumerWorkflowEnums.category.name());
						String subtype = "";
						for (String str : category) {
							subtype = str;
						}
						event.setType(ConsumerWorkflowEnums.Plugin.name());
						event.setSubtype(subtype);
					}
				} else {
					event.setSubtype((String) nodeMap.get("contentType"));
				}
			}
		}
		if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
			List<Relation> relations = node.getInRelations();
			for (Relation rel : relations) {
				if (rel.getEndNodeObjectType().equals("Content") && rel.getRelationType().equals("hasSequenceMember")) {
					event.setParentid(rel.getEndNodeId());
					event.setParenttype(rel.getEndNodeObjectType());
				}
			}
		} else if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
			List<Relation> relations = node.getOutRelations();
			for (Relation rel : relations) {
				if (rel.getEndNodeObjectType().equals(ConsumerWorkflowEnums.Content.name())
						&& rel.getRelationType().equals("hasSequenceMember")) {
					event.setParentid(rel.getEndNodeId());
					event.setParenttype(rel.getEndNodeObjectType());
				}
			}
		}
	}

	/**
	 * This method holds logic to set metadata data to generate object lifecycle events for objectType item or
	 * assessmentitem
	 * 
	 * @param node
	 * @param objectMap
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setItemMetadata(Node node, LifecycleEvent event) {
		if (null != node.getMetadata()) {
			Map<String, Object> nodeMap = new HashMap<String, Object>();
			nodeMap = (Map) node.getMetadata();
			for (Map.Entry<String, Object> entry : nodeMap.entrySet()) {
				if (entry.getKey().equals(ConsumerWorkflowEnums.type.name())) {
					event.setSubtype((String) entry.getValue());
				}
			}
		}
		if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
			List<Relation> relations = node.getInRelations();
			for (Relation rel : relations) {
				if (rel.getEndNodeObjectType().equals(ConsumerWorkflowEnums.ItemSet.name())
						&& rel.getRelationType().equals(ConsumerWorkflowEnums.hasMember.name())) {
					event.setParentid(rel.getEndNodeId());
					event.setParenttype(rel.getEndNodeObjectType());
				}
			}
		}
	}

	/**
	 * This Method holds logic to set metadata for ItemSets
	 * 
	 * @param node
	 * @param objectMap
	 */
	private void setItemSetMetadata(Node node, LifecycleEvent event) {
		if (null != node.getMetadata()) {
			Map<String, Object> nodeMap = (Map<String, Object>) node.getMetadata();
			for (Map.Entry<String, Object> entry : nodeMap.entrySet()) {
				if (entry.getKey().equals(ConsumerWorkflowEnums.type.name())) {
					event.setSubtype((String) entry.getValue());
				}
			}
		}
	}
}
