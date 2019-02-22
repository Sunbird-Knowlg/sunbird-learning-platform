package org.ekstep.sync.tool.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.Platform;
import org.ekstep.common.enums.CompositeSearchParams;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.SystemNodeTypes;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.util.ControllerUtil;

public class SyncMessageGenerator {

	private static ObjectMapper mapper = new ObjectMapper();
	public static Map<String, Map<String, String>> definitionMap = new HashMap<>();
	private static ControllerUtil util = new ControllerUtil();
	private static List<String> nestedFields = Platform.config.getStringList("nested.fields");

	public static Map<String, Object> getMessages(List<Node> nodes, String objectType, Map<String, String> errors)
			throws Exception {
		Map<String, Object> messages = new HashMap<>();

		if (StringUtils.isBlank(objectType) && CollectionUtils.isNotEmpty(nodes))
			loadDefinitionsOf(nodes);

		for (Node node : nodes) {
			try {
				Map<String, String> relationMap = definitionMap.get(node.getObjectType());
				if (relationMap != null) {
					Map<String, Object> nodeMap = getMessage(node);
					Map<String, Object>  message = getJSONMessage(nodeMap, relationMap);
					messages.put(node.getIdentifier(), message);
				}
			} catch (Exception e) {
				errors.put(node.getIdentifier(), e.getMessage());
			}
		}
		return messages;
	}

	public static Map<String, Object> getJSONMessage(Map<String, Object> message, Map<String, String> relationMap) throws Exception {
		Map<String, Object> indexDocument = new HashMap<String, Object>();
		Map transactionData = (Map) message.get("transactionData");
		if (transactionData != null) {
			Map<String, Object> addedProperties = (Map<String, Object>) transactionData.get("properties");
			if (addedProperties != null && !addedProperties.isEmpty()) {
				for (Map.Entry<String, Object> propertyMap : addedProperties.entrySet()) {
					if (propertyMap != null && propertyMap.getKey() != null) {
						String propertyName = (String) propertyMap.getKey();
						// new value of the property
						Object propertyNewValue = ((Map<String, Object>) propertyMap.getValue()).get("nv");
						if (nestedFields.contains(propertyName)) {
							propertyNewValue = mapper.readValue((String) propertyNewValue, new TypeReference<Object>() {
							});
						}
						indexDocument.put(propertyName, propertyNewValue);
					}
				}
			}
		}
		List<Map<String, Object>> addedRelations = (List<Map<String, Object>>) transactionData.get("addedRelations");
		if (null != addedRelations && !addedRelations.isEmpty()) {
			for (Map<String, Object> rel : addedRelations) {
				String key = rel.get("dir") + "_" + rel.get("type") + "_" + rel.get("rel");
				String title = relationMap.get(key);
				if (StringUtils.isNotBlank(title)) {
					List<String> list = (List<String>) indexDocument.get(title);
					if (null == list)
						list = new ArrayList<String>();
					String id = (String) rel.get("id");
					if (StringUtils.isNotBlank(id) && !list.contains(id)) {
						list.add(id);
						indexDocument.put(title, list);
					}
				}
			}
		}
		indexDocument.put("graph_id", (String) message.get("graphId"));
		indexDocument.put("node_id", (Long) message.get("nodeGraphId"));
		indexDocument.put("identifier", (String) message.get("nodeUniqueId"));
		indexDocument.put("objectType", (String) message.get("objectType"));
		indexDocument.put("nodeType", (String) message.get("nodeType"));

		return indexDocument;
	}

	public static Map<String, Object> getMessage(Node node) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> transactionData = new HashMap<String, Object>();
		if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
			Map<String, Object> propertyMap = new HashMap<String, Object>();
			for (Entry<String, Object> entry : node.getMetadata().entrySet()) {
				String key = entry.getKey();
				if (StringUtils.isNotBlank(key)) {
					Map<String, Object> valueMap = new HashMap<String, Object>();
					valueMap.put("ov", null); // old value
					valueMap.put("nv", entry.getValue()); // new value
					// temporary check to not sync body and editorState
					if (!StringUtils.equalsIgnoreCase("body", key) && !StringUtils.equalsIgnoreCase("editorState", key))
						propertyMap.put(entry.getKey(), valueMap);
				}
			}
			transactionData.put(CompositeSearchParams.properties.name(), propertyMap);
		} else
			transactionData.put(CompositeSearchParams.properties.name(), new HashMap<String, Object>());

		// add IN relations
		List<Map<String, Object>> relations = new ArrayList<Map<String, Object>>();
		if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
			for (Relation rel : node.getInRelations()) {
				Map<String, Object> relMap = new HashMap<String, Object>();
				relMap.put("rel", rel.getRelationType());
				relMap.put("id", rel.getStartNodeId());
				relMap.put("dir", "IN");
				relMap.put("type", rel.getStartNodeObjectType());
				relMap.put("label", getLabel(rel.getStartNodeMetadata()));
				relations.add(relMap);
			}
		}

		// add OUT relations
		if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
			for (Relation rel : node.getOutRelations()) {
				Map<String, Object> relMap = new HashMap<String, Object>();
				relMap.put("rel", rel.getRelationType());
				relMap.put("id", rel.getEndNodeId());
				relMap.put("dir", "OUT");
				relMap.put("type", rel.getEndNodeObjectType());
				relMap.put("label", getLabel(rel.getEndNodeMetadata()));
				relations.add(relMap);
			}
		}
		transactionData.put(CompositeSearchParams.addedRelations.name(), relations);
		map.put(CompositeSearchParams.operationType.name(), GraphDACParams.UPDATE.name());
		map.put(CompositeSearchParams.graphId.name(), node.getGraphId());
		map.put(CompositeSearchParams.nodeGraphId.name(), node.getId());
		map.put(CompositeSearchParams.nodeUniqueId.name(), node.getIdentifier());
		map.put(CompositeSearchParams.objectType.name(), node.getObjectType());
		map.put(CompositeSearchParams.nodeType.name(), SystemNodeTypes.DATA_NODE.name());
		map.put(CompositeSearchParams.transactionData.name(), transactionData);
		map.put(CompositeSearchParams.syncMessage.name(), true);
		return map;
	}

	private static String getLabel(Map<String, Object> metadata) {
		if (null != metadata && !metadata.isEmpty()) {
			if (StringUtils.isNotBlank((String) metadata.get("name")))
				return (String) metadata.get("name");
			else if (StringUtils.isNotBlank((String) metadata.get("lemma")))
				return (String) metadata.get("lemma");
			else if (StringUtils.isNotBlank((String) metadata.get("title")))
				return (String) metadata.get("title");
			else if (StringUtils.isNotBlank((String) metadata.get("gloss")))
				return (String) metadata.get("gloss");
		}
		return "";
	}

	private static void loadDefinitionsOf(List<Node> nodes) {
		List<String> objectTypes = GraphUtil.getAllObjectTypes(nodes);
		String graphId = (nodes.get(0) != null) ? nodes.get(0).getGraphId() : "";
		if (!definitionMap.keySet().containsAll(objectTypes)) {
			objectTypes.removeAll(definitionMap.keySet());
			objectTypes.forEach(objectType -> {
				try {
					DefinitionDTO def = util.getDefinition(graphId, objectType);
					if (def != null) {
						Map<String, Object> definition = mapper.convertValue(def,
								new TypeReference<Map<String, Object>>() {
								});
						Map<String, String> relationMap = GraphUtil.getRelationMap(objectType, definition);
						definitionMap.put(objectType, relationMap);
					}
				} catch (Exception e) {
					System.out.println(e);
				}
			});
		}
	}
}
