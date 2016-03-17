package org.ekstep.searchindex.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.searchindex.util.Constants;
import org.ekstep.searchindex.util.ConsumerUtil;
import org.ekstep.searchindex.util.ElasticSearchUtil;
import org.ekstep.searchindex.util.ObjectDefinitionCache;

import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

public class MessageProcessor {

	private ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
	private ConsumerUtil consumerUtil = new ConsumerUtil();
	private ObjectMapper mapper = new ObjectMapper();

	public void processMessage(String messageData) {
		try {
			Map<String, Object> message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
			});
			processMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processMessage(Map<String, Object> message) throws Exception {
		if (message != null && message.get("operationType") != null) {
			String nodeType = (String) message.get("nodeType");
			String objectType = (String) message.get("objectType");
			objectType = WordUtils.capitalize(objectType.toLowerCase());
			createCompositeSearchIndex(objectType);
			String graphId = (String) message.get("graphId");
			String uniqueId = (String) message.get("nodeUniqueId");
			switch (nodeType) {
			case Constants.NODE_TYPE_DATA: {
				Map<String, Object> definitionNode = ObjectDefinitionCache.getDefinitionNode(objectType, graphId);
				String operationType = (String) message.get("operationType");
				switch (operationType) {
				case Constants.OPERATION_CREATE: {
					Map<String, Object> indexDocument = getIndexDocument(message, definitionNode, false);
					String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
					addOrUpdateIndex(objectType, uniqueId, jsonIndexDocument);
					break;
				}
				case Constants.OPERATION_UPDATE: {
					Map<String, Object> indexDocument = getIndexDocument(message, definitionNode, true);
					String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
					addOrUpdateIndex(objectType, uniqueId, jsonIndexDocument);
					break;
				}
				case Constants.OPERATION_DELETE: {
					elasticSearchUtil.deleteDocument(Constants.COMPOSITE_SEARCH_INDEX, objectType.toLowerCase(),
							uniqueId);
					break;
				}
				}
				break;
			}
			case Constants.NODE_TYPE_DEFINITION: {
				Map<String, Object> definitionNode = ObjectDefinitionCache.resyncDefinition(objectType, graphId);
				consumerUtil.reSyncNodes(objectType, graphId, definitionNode);
			}
			}
		}
	}

	private void addOrUpdateIndex(String objectType, String uniqueId, String jsonIndexDocument) throws Exception {
		elasticSearchUtil.addDocumentWithId(Constants.COMPOSITE_SEARCH_INDEX, objectType.toLowerCase(), uniqueId,
				jsonIndexDocument);
	}

	private void createCompositeSearchIndex(String objectType) throws IOException {
		elasticSearchUtil.addIndex(Constants.COMPOSITE_SEARCH_INDEX, objectType.toLowerCase(), null, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, Object> getIndexDocument(Map<String, Object> message, Map<String, Object> definitionNode,
			boolean updateRequest) throws IOException {
		Map<String, Object> indexDocument = new HashMap<String, Object>();
		String uniqueId = (String) message.get("nodeUniqueId");
		String objectType = (String) message.get("objectType");
		if (updateRequest) {
			String documentJson = elasticSearchUtil.getDocumentAsStringById(Constants.COMPOSITE_SEARCH_INDEX,
					objectType.toLowerCase(), uniqueId);
			if (documentJson != null && !documentJson.isEmpty()) {
				indexDocument = mapper.readValue(documentJson, new TypeReference<Map<String, Object>>() {
				});
			}
		}
		indexDocument.put("graph_id", (String) message.get("graphId"));
		indexDocument.put("node_graph_id", (String) message.get("nodeGraphId"));
		indexDocument.put("node_unique_id", (String) message.get("nodeUniqueId"));
		indexDocument.put("object_type", (String) message.get("objectType"));
		indexDocument.put("node_type", (String) message.get("nodeType"));
		Map transactionData = (Map) message.get("transactionData");
		if (transactionData != null) {
			List<Map> addedProperties = (List<Map>) transactionData.get("addedProperties");
			if (addedProperties != null && !addedProperties.isEmpty()) {
				for (Map propertyMap : addedProperties) {
					if (propertyMap != null && propertyMap.get("propertyName") != null) {
						String propertyName = (String) propertyMap.get("propertyName");
						Map<String, Object> propertyDefinition = (Map<String, Object>) definitionNode.get(propertyName);
						if (propertyDefinition != null) {
							boolean indexed = (boolean) propertyDefinition.get("indexed");
							if (indexed) {
								indexDocument.put(propertyName, propertyMap.get("value"));
							}
						}
					}
				}
			}
			List<String> removedProperties = (List<String>) transactionData.get("removedProperties");
			if (removedProperties != null && !removedProperties.isEmpty()) {
				for (String propertyName : removedProperties) {
					if (propertyName != null && !propertyName.isEmpty()) {
						indexDocument.remove(propertyName);
					}
				}
			}
			List<String> addedTags = (List<String>) transactionData.get("addedTags");
			if (addedTags != null && !addedTags.isEmpty()) {
				List<String> indexedTags = (List<String>) indexDocument.get(Constants.INDEX_FIELD_TAGS);
				if (indexedTags == null || indexedTags.isEmpty()) {
					indexedTags = new ArrayList<String>();
				}
				for (String addedTag : addedTags) {
					if (!indexedTags.contains(addedTag)) {
						indexedTags.add(addedTag);
					}
				}
				indexDocument.put(Constants.INDEX_FIELD_TAGS, indexedTags);
			}
			List<String> removedTags = (List<String>) transactionData.get("removedTags");
			if (removedTags != null && !removedTags.isEmpty()) {
				List<String> indexedTags = (List<String>) indexDocument.get(Constants.INDEX_FIELD_TAGS);
				if (indexedTags != null && !indexedTags.isEmpty()) {
					for (String removedTag : removedTags) {
						indexedTags.remove(indexedTags.indexOf(removedTag));
					}
					indexDocument.put(Constants.INDEX_FIELD_TAGS, indexedTags);
				}
			}
		}
		return indexDocument;
	}

	public static void main(String arg[]) throws Exception {
		MessageProcessor processor = new MessageProcessor();
		JSONBuilder builder = new JSONStringer();

		/*
		 * builder.object().key("operationType").value(Constants.
		 * OPERATION_CREATE).key("graphId").value("hi")
		 * .key("nodeGraphId").value("2").key("nodeUniqueId").value("hi_2").key(
		 * "objectType")
		 * .value(Constants.OBJECT_TYPE_WORD).key("nodeType").value(Constants.
		 * NODE_TYPE_DATA) .key("transactionData").object()
		 * .key("addedProperties").array().object()
		 * .key("propertyName").value("lemma") .key("value").value("Hi 2")
		 * .endObject() .endArray() .endObject() .endObject();
		 */

		/*
		 * builder.object().key("operationType").value(Constants.
		 * OPERATION_CREATE).key("graphId").value("hi")
		 * .key("nodeGraphId").value("1").key("nodeUniqueId").value("hi_s_1").
		 * key( "objectType")
		 * .value(Constants.OBJECT_TYPE_SYNSET).key("nodeType").value(Constants.
		 * NODE_TYPE_DATA) .key("transactionData").object()
		 * .key("addedProperties").array().object()
		 * .key("propertyName").value("gloss") .key("value").value(
		 * "Hi how are you") .endObject() .endArray() .endObject() .endObject();
		 */

		/*
		 * builder.object().key("operationType").value(Constants.
		 * OPERATION_DELETE).key("graphId").value("hi")
		 * .key("nodeGraphId").value("1").key("nodeUniqueId").value("hi_2").key(
		 * "objectType")
		 * .value(Constants.OBJECT_TYPE_WORD).key("nodeType").value(Constants.
		 * NODE_TYPE_DATA).endObject();
		 */

		builder.object().key("operationType").value(Constants.OPERATION_UPDATE).key("graphId").value("hi")
				.key("nodeGraphId").value("1").key("nodeUniqueId").value("hi_2").key("objectType")
				.value(Constants.OBJECT_TYPE_WORD).key("nodeType").value(Constants.NODE_TYPE_DEFINITION).endObject();

		/*
		 * builder.object().key("operationType").value(Constants.
		 * OPERATION_UPDATE).key("graphId").value("hi")
		 * .key("nodeGraphId").value("1").key("nodeUniqueId").value("hi_1").key(
		 * "objectType")
		 * .value(Constants.OBJECT_TYPE_WORD).key("nodeType").value(Constants.
		 * NODE_TYPE_DATA)
		 * .key("transactionData").object().key("addedProperties").array().
		 * object().key("propertyName")
		 * .value("notappli").key("value").array().value("class 1"
		 * ).value("rwo").endArray().endObject()
		 * .endArray().key("removedProperties").array().value("sourceTypes").
		 * endArray().key("addedTags").array() .value("grade one"
		 * ).endArray().key("removedTags").array().value("grade three"
		 * ).endArray().endObject() .endObject();
		 */
		Map<String, Object> message = processor.mapper.readValue(builder.toString(),
				new TypeReference<Map<String, Object>>() {
				});
		processor.processMessage(message);
	}
}
