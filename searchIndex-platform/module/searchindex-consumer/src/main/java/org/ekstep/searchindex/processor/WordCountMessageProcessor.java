package org.ekstep.searchindex.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.searchindex.util.ConsumerUtil;
import org.ekstep.searchindex.util.ObjectDefinitionCache;

import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

public class WordCountMessageProcessor implements IMessageProcessor {

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
			String languageId = (String) message.get("graphId");
			String uniqueId = (String) message.get("nodeUniqueId");
			if (objectType.equalsIgnoreCase(CompositeSearchConstants.OBJECT_TYPE_WORD)) {
				switch (nodeType) {
				case CompositeSearchConstants.NODE_TYPE_DATA: {
					String operationType = (String) message.get("operationType");
					switch (operationType) {
					case CompositeSearchConstants.OPERATION_CREATE: {
					
						break;
					}
					case CompositeSearchConstants.OPERATION_UPDATE: {
						Map<String, Object> indexDocument = getIndexDocument(message, definitionNode, true);
						String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
						addOrUpdateIndex(objectType, uniqueId, jsonIndexDocument);
						break;
					}
					case CompositeSearchConstants.OPERATION_DELETE: {
					
						break;
					}
					}
					break;
				}
				}
				System.out.println("Message processed");
			}
		}
	}

	private void addOrUpdateIndex(String objectType, String uniqueId, String jsonIndexDocument) throws Exception {
		elasticSearchUtil.addDocumentWithId(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, uniqueId, jsonIndexDocument);
	}

	private void createCompositeSearchIndex(String objectType) throws IOException {
		String settings = "{  \"settings\": {    \"index\": {      \"index\": \"compositesearch\",      \"type\": \""
				+ CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE
				+ "\",      \"analysis\": {        \"analyzer\": {          \"cs_index_analyzer\": {            \"type\": \"custom\",            \"tokenizer\": \"standard\",            \"filter\": [              \"lowercase\",              \"mynGram\"            ]          },          \"cs_search_analyzer\": {            \"type\": \"custom\",            \"tokenizer\": \"standard\",            \"filter\": [              \"standard\",              \"lowercase\"            ]          }        },        \"filter\": {          \"mynGram\": {            \"type\": \"nGram\",            \"min_gram\": 2,            \"max_gram\": 20          }        }      }    }  }}";
		String mappings = "{\"" + CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE
				+ "\": {  \"dynamic_templates\": [    {      \"longs\": {        \"match_mapping_type\": \"long\",        \"mapping\": {          \"type\": \"long\",          fields: {            \"raw\": {              \"type\": \"long\"            }          }        }      }    },    {      \"doubles\": {        \"match_mapping_type\": \"double\",        \"mapping\": {          \"type\": \"double\",          fields: {            \"raw\": {              \"type\": \"double\"            }          }        }      }    },    {      \"strings\": {        \"match_mapping_type\": \"string\",        \"mapping\": {          \"type\": \"string\",          \"copy_to\": \"all_fields\",          \"analyzer\": \"cs_index_analyzer\",          \"search_analyzer\": \"cs_search_analyzer\",          fields: {            \"raw\": {              \"type\": \"string\",              \"analyzer\": \"cs_search_analyzer\",              \"search_analyzer\": \"cs_search_analyzer\"            }          }        }      }    }  ],  \"properties\": {    \"all_fields\": {      \"type\": \"string\",      \"analyzer\": \"cs_index_analyzer\",      \"search_analyzer\": \"cs_index_analyzer\",      fields: {        \"raw\": {          \"type\": \"string\",          \"analyzer\": \"cs_index_analyzer\",          \"search_analyzer\": \"cs_index_analyzer\"        }      }    }  }}}";

		elasticSearchUtil.addIndex(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, settings, mappings);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, Object> getIndexDocument(Map<String, Object> message, Map<String, Object> definitionNode,
			boolean updateRequest) throws IOException {
		Map<String, Object> indexDocument = new HashMap<String, Object>();
		String uniqueId = (String) message.get("nodeUniqueId");
		if (updateRequest) {
			String documentJson = elasticSearchUtil.getDocumentAsStringById(
					CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
					CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, uniqueId);
			if (documentJson != null && !documentJson.isEmpty()) {
				indexDocument = mapper.readValue(documentJson, new TypeReference<Map<String, Object>>() {
				});
			}
		}
		indexDocument.put("graph_id", (String) message.get("graphId"));
		indexDocument.put("node_graph_id", (int) message.get("nodeGraphId"));
		indexDocument.put("node_unique_id", (String) message.get("nodeUniqueId"));
		indexDocument.put("object_type", (String) message.get("objectType"));
		indexDocument.put("node_type", (String) message.get("nodeType"));
		Map transactionData = (Map) message.get("transactionData");
		if (transactionData != null) {
			Map<String, Object> addedProperties = (Map<String, Object>) transactionData.get("addedProperties");
			if (addedProperties != null && !addedProperties.isEmpty()) {
				for (Map.Entry<String, Object> propertyMap : addedProperties.entrySet()) {
					if (propertyMap != null && propertyMap.getKey() != null) {
						String propertyName = (String) propertyMap.getKey();
						Map<String, Object> propertyDefinition = (Map<String, Object>) definitionNode.get(propertyName);
						if (propertyDefinition != null) {
							boolean indexed = (boolean) propertyDefinition.get("indexed");
							if (indexed) {
								indexDocument.put(propertyName, propertyMap.getValue());
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
				List<String> indexedTags = (List<String>) indexDocument.get(CompositeSearchConstants.INDEX_FIELD_TAGS);
				if (indexedTags == null || indexedTags.isEmpty()) {
					indexedTags = new ArrayList<String>();
				}
				for (String addedTag : addedTags) {
					if (!indexedTags.contains(addedTag)) {
						indexedTags.add(addedTag);
					}
				}
				indexDocument.put(CompositeSearchConstants.INDEX_FIELD_TAGS, indexedTags);
			}
			List<String> removedTags = (List<String>) transactionData.get("removedTags");
			if (removedTags != null && !removedTags.isEmpty()) {
				List<String> indexedTags = (List<String>) indexDocument.get(CompositeSearchConstants.INDEX_FIELD_TAGS);
				if (indexedTags != null && !indexedTags.isEmpty()) {
					for (String removedTag : removedTags) {
						if (indexedTags.contains(removedTag)) {
							indexedTags.remove(indexedTags.indexOf(removedTag));
						}
					}
					indexDocument.put(CompositeSearchConstants.INDEX_FIELD_TAGS, indexedTags);
				}
			}
		}
		return indexDocument;
	}

	public static void main(String arg[]) throws Exception {
		WordCountMessageProcessor processor = new WordCountMessageProcessor();
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

		builder.object().key("operationType").value(CompositeSearchConstants.OPERATION_UPDATE).key("graphId")
				.value("hi").key("nodeGraphId").value("1").key("nodeUniqueId").value("hi_2").key("objectType")
				.value(CompositeSearchConstants.OBJECT_TYPE_WORD).key("nodeType")
				.value(CompositeSearchConstants.NODE_TYPE_DEFINITION).endObject();

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
