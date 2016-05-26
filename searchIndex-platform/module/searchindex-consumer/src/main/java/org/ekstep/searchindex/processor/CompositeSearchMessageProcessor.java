package org.ekstep.searchindex.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.searchindex.util.ConsumerUtil;
import org.ekstep.searchindex.util.ObjectDefinitionCache;

import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

public class CompositeSearchMessageProcessor implements IMessageProcessor {

	private ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
	private ConsumerUtil consumerUtil = new ConsumerUtil();
	private ObjectMapper mapper = new ObjectMapper();

	public CompositeSearchMessageProcessor() {
		super();
	}

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
			createCompositeSearchIndex();
			String graphId = (String) message.get("graphId");
			String uniqueId = (String) message.get("nodeUniqueId");
			//System.out.println("message node type: " + nodeType + " object type: " + objectType);
			switch (nodeType) {
			case CompositeSearchConstants.NODE_TYPE_DATA: {
				Map<String, Object> definitionNode = ObjectDefinitionCache.getDefinitionNode(objectType, graphId);
				// objectType = WordUtils.capitalize(objectType.toLowerCase());
				String operationType = (String) message.get("operationType");
				switch (operationType) {
				case CompositeSearchConstants.OPERATION_CREATE: {
					Map<String, Object> indexDocument = getIndexDocument(message, definitionNode, false);
					String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
					addOrUpdateIndex(uniqueId, jsonIndexDocument);
					break;
				}
				case CompositeSearchConstants.OPERATION_UPDATE: {
					Map<String, Object> indexDocument = getIndexDocument(message, definitionNode, true);
					String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
					addOrUpdateIndex(uniqueId, jsonIndexDocument);
					break;
				}
				case CompositeSearchConstants.OPERATION_DELETE: {
					elasticSearchUtil.deleteDocument(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
							CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, uniqueId);
					break;
				}
				}
				break;
			}
			case CompositeSearchConstants.NODE_TYPE_DEFINITION: {
				//System.out.println("processing definition nodes");
				ObjectDefinitionCache.resyncDefinition(objectType, graphId);
				//consumerUtil.reSyncNodes(objectType, graphId, definitionNode);
			}
			}
			//System.out.println("Message processed by Composite search index porocessor");
		}
	}

	private void addOrUpdateIndex(String uniqueId, String jsonIndexDocument) throws Exception {
		elasticSearchUtil.addDocumentWithId(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, uniqueId, jsonIndexDocument);
	}

	private void createCompositeSearchIndex() throws IOException {
		String settings = "{ \"settings\": {   \"index\": {     \"index\": \"compositesearch\",     \"type\": \""+CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE+"\",     \"analysis\": {       \"analyzer\": {         \"cs_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"cs_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   } }}";
		String mappings = "{ \""+CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE+"\" : {    \"dynamic_templates\": [      {        \"longs\": {          \"match_mapping_type\": \"long\",          \"mapping\": {            \"type\": \"long\",            fields: {              \"raw\": {                \"type\": \"long\"              }            }          }        }      },      {        \"doubles\": {          \"match_mapping_type\": \"double\",          \"mapping\": {            \"type\": \"double\",            fields: {              \"raw\": {                \"type\": \"double\"              }            }          }        }      },	  {        \"dates\": {          \"match_mapping_type\": \"date\",          \"mapping\": {            \"type\": \"date\",            fields: {              \"raw\": {                \"type\": \"date\"              }            }          }        }      },      {        \"strings\": {          \"match_mapping_type\": \"string\",          \"mapping\": {            \"type\": \"string\",            \"copy_to\": \"all_fields\",            \"analyzer\": \"cs_index_analyzer\",            \"search_analyzer\": \"cs_search_analyzer\",            fields: {              \"raw\": {                \"type\": \"string\",                \"analyzer\": \"keylower\"              }            }          }        }      }    ],    \"properties\": {      \"all_fields\": {        \"type\": \"string\",        \"analyzer\": \"cs_index_analyzer\",        \"search_analyzer\": \"cs_search_analyzer\",        fields: {          \"raw\": {            \"type\": \"string\",            \"analyzer\": \"keylower\"          }        }      }    }  }}";
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
		Map transactionData = (Map) message.get("transactionData");
		if (transactionData != null) {
			Map<String, Object> addedProperties = (Map<String, Object>) transactionData.get("properties");
			if (addedProperties != null && !addedProperties.isEmpty()) {
				for (Map.Entry<String, Object> propertyMap : addedProperties.entrySet()) {
					if (propertyMap != null && propertyMap.getKey() != null) {
						String propertyName = (String) propertyMap.getKey();
						Object propertyNewValue=((Map<String, Object>) propertyMap.getValue()).get("nv"); //new value of the property
						if(propertyNewValue==null) //New value from transaction data is null, then remove the property from document
							indexDocument.remove(propertyName);
						else{
							Map<String, Object> propertyDefinition = (Map<String, Object>) definitionNode.get(propertyName);
							if (propertyDefinition != null) {
								boolean indexed = (boolean) propertyDefinition.get("indexed");
								if (indexed) {
									indexDocument.put(propertyName, propertyNewValue);
								}
							}
						}

					}
				}
			}
			/*List<String> removedProperties = (List<String>) transactionData.get("removedProperties");
			if (removedProperties != null && !removedProperties.isEmpty()) {
				for (String propertyName : removedProperties) {
					if (propertyName != null && !propertyName.isEmpty()) {
						indexDocument.remove(propertyName);
					}
				}
			}*/
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
		indexDocument.put("graph_id", (String) message.get("graphId"));
		indexDocument.put("node_id", (int) message.get("nodeGraphId"));
		indexDocument.put("identifier", (String) message.get("nodeUniqueId"));
		indexDocument.put("objectType", (String) message.get("objectType"));
		indexDocument.put("nodeType", (String) message.get("nodeType"));
		return indexDocument;
	}

	public static void main(String arg[]) throws Exception {
		CompositeSearchMessageProcessor processor = new CompositeSearchMessageProcessor();
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

		/*builder.object().key("operationType").value(CompositeSearchConstants.OPERATION_UPDATE).key("graphId")
				.value("hi").key("nodeGraphId").value("1").key("nodeUniqueId").value("hi_2").key("objectType")
				.value(CompositeSearchConstants.OBJECT_TYPE_WORD).key("nodeType")
				.value(CompositeSearchConstants.NODE_TYPE_DEFINITION).endObject();*/

		builder.object().key("operationType").value(CompositeSearchConstants.OPERATION_UPDATE).key("graphId").value("hi")
		.key("nodeGraphId").value(1).key("nodeUniqueId").value("hi_1").key("objectType")
		.value("Word").key("nodeType").value(CompositeSearchConstants.NODE_TYPE_DATA)
		.key("transactionData").object().key("addedProperties").object()
		.key("lemma").value("இடையேயான கருத்து").key("status").value("Live").endObject()
		.key("removedProperties").array().value("sourceTypes").endArray().key("addedTags").array()
		.value("grade one").endArray().key("removedTags").array().value("grade three").endArray().endObject()
		.endObject();
		Map<String, Object> message = processor.mapper.readValue(builder.toString(),
				new TypeReference<Map<String, Object>>() {
				});
		processor.processMessage(message);
	}
}
