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
import org.ekstep.jobs.samza.util.ObjectLifecycleParams;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.cache.factory.JedisFactory;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValueFactory;

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
		ConfigObject conf = ConfigValueFactory.fromMap(props);
		ConfigFactory.load(conf.toConfig());
		LOGGER.info("Service config initialized");
		digest = MessageDigest.getInstance("MD5");
		LearningRequestRouterPool.init();
		LOGGER.info("Learning actors initialized");
		JedisFactory.initialize(props);
		LOGGER.info("Redis connection factory initialized");
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
		if(null == message.get("syncMessage")){
			if(null != message.get(ObjectLifecycleParams.operationType.name()) && message.get(ObjectLifecycleParams.operationType.name()).equals(ObjectLifecycleParams.DELETE.name())){
				Event event = generateEventOnDelete(message);
				event.setEts(message);
				LOGGER.info("Event generated on deletion of node");
				publishEvent(event, collector);
				LOGGER.info("Event published on deletion of node");
				metrics.incSuccessCounter();
			}
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
	}

	private Event generateEventOnDelete(Map<String, Object> message) {
		Event event = new Event("BE_OBJECT_LIFECYCLE", "2.1", "ObjectLifecycleTask");
		LifecycleEvent lifecycleEvent = new LifecycleEvent();
		String nodeUniqueId = (String)message.get(ObjectLifecycleParams.nodeUniqueId.name());
		String objectType = (String)message.get(ObjectLifecycleParams.objectType.name());
		lifecycleEvent.setId(nodeUniqueId);
		lifecycleEvent.setType(objectType);
		event.setEts(message);
		event.setEdata(lifecycleEvent);
		LOGGER.info("Event generated for node deleted" + nodeUniqueId);
		return event;
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
	public Map<String, Object> getStateChangeEvent(Map<String, Object> message) {

		if (!message.containsKey(ObjectLifecycleParams.nodeUniqueId.name()))
			return null;
		if (!message.containsKey(ObjectLifecycleParams.transactionData.name()))
			return null;
		Map<String, Object> transactionMap = (Map<String, Object>) message.get(ObjectLifecycleParams.transactionData.name());
		if (!transactionMap.containsKey(ObjectLifecycleParams.properties.name()))
			return null;
		Map<String, Object> propertiesMap = (Map<String, Object>) transactionMap.get(ObjectLifecycleParams.properties.name());

		if (propertiesMap.containsKey(ObjectLifecycleParams.status.name())) {
			return (Map<String, Object>) propertiesMap.get(ObjectLifecycleParams.status.name());
		}
		return null;
	}

	private Node getNode(Map<String, Object> message) throws Exception {
		String nodeId = (String) message.get(ObjectLifecycleParams.nodeUniqueId.name());
		if (message.get(ObjectLifecycleParams.nodeType.name()).equals(ObjectLifecycleParams.SET.name())
				&& message.get(ObjectLifecycleParams.objectType.name()).equals(ObjectLifecycleParams.ItemSet.name())) {
			return getItemSetNode(nodeId);
		} else {
			return util.getNode(ObjectLifecycleParams.domain.name(), nodeId);
		}
	}

	private Node getItemSetNode(String identifier) throws Exception {
		ControllerUtil util = new ControllerUtil();
		Response resp = util.getSet(ObjectLifecycleParams.domain.name(), identifier);
		Map<String, Object> map = (Map<String, Object>) resp.getResult();
		Node node = (Node) map.get(ObjectLifecycleParams.node.name());
		if (null != node) {
			return node;
		}
		return null;
	}

	public Event generateLifecycleEvent(Map<String, Object> stateChangeEvent, Node node) {

		Event event = new Event("BE_OBJECT_LIFECYCLE", "2.1", "ObjectLifecycleTask");
		LifecycleEvent lifecycleEvent = new LifecycleEvent();
		String prevstate = (String) stateChangeEvent.get("ov");
		String state = (String) stateChangeEvent.get("nv");

		String nodeId = node.getIdentifier();
		String objectType = node.getObjectType();

		if (StringUtils.equalsIgnoreCase(objectType, ObjectLifecycleParams.ContentImage.name())
				&& StringUtils.equalsIgnoreCase(prevstate, null) && StringUtils.equalsIgnoreCase(state, ObjectLifecycleParams.Draft.name())) {
			lifecycleEvent.setPrevstate(ObjectLifecycleParams.Live.name());
			lifecycleEvent.setState(ObjectLifecycleParams.Draft.name());
		} else if (StringUtils.equalsIgnoreCase(objectType, ObjectLifecycleParams.ContentImage.name())
				&& StringUtils.equalsIgnoreCase(prevstate, null)
				&& StringUtils.equalsIgnoreCase(state, ObjectLifecycleParams.FlagDraft.name())) {
			lifecycleEvent.setPrevstate(ObjectLifecycleParams.Flagged.name());
			lifecycleEvent.setState(ObjectLifecycleParams.FlagDraft.name());
		} else {
			prevstate = (prevstate == null) ? "" : prevstate;
			lifecycleEvent.setPrevstate(prevstate);
			lifecycleEvent.setState(state);
		}

		if (StringUtils.endsWithIgnoreCase(nodeId, ".img")
				&& StringUtils.endsWithIgnoreCase(objectType, ObjectLifecycleParams.Image.name())) {
			nodeId = StringUtils.replace(nodeId, ".img", "");
			objectType = StringUtils.replace(objectType, ObjectLifecycleParams.Image.name(), "");
		}
		lifecycleEvent.setId(nodeId);
		lifecycleEvent.setType(objectType);

		if (null != node.getMetadata()) {
			Map<String, Object> nodeMap = (Map<String, Object>) node.getMetadata();
			if (nodeMap.containsKey(ObjectLifecycleParams.name.name())) {
				lifecycleEvent.setName((String) nodeMap.get(ObjectLifecycleParams.name.name()));
			}
			if (nodeMap.containsKey(ObjectLifecycleParams.code.name())) {
				lifecycleEvent.setCode((String) nodeMap.get(ObjectLifecycleParams.code.name()));
			}
			if (nodeMap.containsKey(ObjectLifecycleParams.channel.name())) {
				event.setChannel((String) nodeMap.get(ObjectLifecycleParams.channel.name()));
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
				if (rel.getEndNodeObjectType().equals(ObjectLifecycleParams.Concept.name())
						&& rel.getRelationType().equals(ObjectLifecycleParams.isParentOf.name())) {
					event.setParentid(rel.getEndNodeId());
					event.setParenttype(rel.getEndNodeObjectType());
				} else if (rel.getEndNodeObjectType().equals(ObjectLifecycleParams.Dimension.name())
						&& rel.getRelationType().equals(ObjectLifecycleParams.isParentOf.name())) {
					event.setParentid(rel.getEndNodeId());
					event.setParenttype(rel.getEndNodeObjectType());
				}
			}
		} else if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
			List<Relation> relations = node.getOutRelations();
			for (Relation rel : relations) {
				if (rel.getEndNodeObjectType().equals(ObjectLifecycleParams.Concept.name())
						&& rel.getRelationType().equals(ObjectLifecycleParams.isParentOf.name())) {
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
				if (rel.getEndNodeObjectType().equals(ObjectLifecycleParams.Domain.name())
						&& rel.getRelationType().equals(ObjectLifecycleParams.isParentOf.name())) {
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
					event.setSubtype((String) nodeMap.get(ObjectLifecycleParams.mediaType.name()));
				} else if (nodeMap.containsValue("Plugin")) {
					if (nodeMap.containsKey(ObjectLifecycleParams.category.name())) {
						String[] category = (String[]) nodeMap.get(ObjectLifecycleParams.category.name());
						String subtype = "";
						for (String str : category) {
							subtype = str;
						}
						event.setType(ObjectLifecycleParams.Plugin.name());
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
				if (rel.getEndNodeObjectType().equals(ObjectLifecycleParams.Content.name())
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
				if (entry.getKey().equals(ObjectLifecycleParams.type.name())) {
					event.setSubtype((String) entry.getValue());
				}
			}
		}
		if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
			List<Relation> relations = node.getInRelations();
			for (Relation rel : relations) {
				if (rel.getEndNodeObjectType().equals(ObjectLifecycleParams.ItemSet.name())
						&& rel.getRelationType().equals(ObjectLifecycleParams.hasMember.name())) {
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
				if (entry.getKey().equals(ObjectLifecycleParams.type.name())) {
					event.setSubtype((String) entry.getValue());
				}
			}
		}
	}
}
