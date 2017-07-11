package org.ekstep.jobs.samza.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.searchindex.util.ObjectDefinitionCache;
import org.ekstep.searchindex.util.PropertiesUtil;

public class CompositeSearchIndexerService implements ISamzaService {

	private ObjectMapper mapper = new ObjectMapper();

	private ElasticSearchUtil esUtil = null;

	@Override
	public void initialize(Config config) throws Exception {
		Map<String, Object> props = new HashMap<String, Object>();
		for (Entry<String, String> entry : config.entrySet()) {
			props.put(entry.getKey(), entry.getValue());
		}
		PropertiesUtil.loadProperties(props);
		esUtil = new ElasticSearchUtil();
		createCompositeSearchIndex();
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) {

		Object index = message.get("index");
		Boolean shouldindex = BooleanUtils.toBoolean(null == index ? "true" : index.toString());
		if (!BooleanUtils.isFalse(shouldindex)) {
			try {
				processMessage(message);
				metrics.incSuccessCounter();
			} catch (Exception ex) {
				metrics.incFailedCounter();
			}
		} else {
			metrics.incSkippedCounter();
		}
	}

	public void processMessage(Map<String, Object> message) throws Exception {
		if (message != null && message.get("operationType") != null) {
			String nodeType = (String) message.get("nodeType");
			String objectType = (String) message.get("objectType");
			String graphId = (String) message.get("graphId");
			String uniqueId = (String) message.get("nodeUniqueId");
			switch (nodeType) {
			case CompositeSearchConstants.NODE_TYPE_SET:
			case CompositeSearchConstants.NODE_TYPE_DATA: {
				Map<String, Object> definitionNode = ObjectDefinitionCache.getDefinitionNode(objectType, graphId);
				Map<String, String> relationMap = ObjectDefinitionCache.getRelationDefinition(objectType, graphId);
				String operationType = (String) message.get("operationType");
				switch (operationType) {
				case CompositeSearchConstants.OPERATION_CREATE: {
					Map<String, Object> indexDocument = getIndexDocument(message, definitionNode, relationMap, false);
					String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
					addOrUpdateIndex(uniqueId, jsonIndexDocument);
					break;
				}
				case CompositeSearchConstants.OPERATION_UPDATE: {
					Map<String, Object> indexDocument = getIndexDocument(message, definitionNode, relationMap, true);
					String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
					addOrUpdateIndex(uniqueId, jsonIndexDocument);
					break;
				}
				case CompositeSearchConstants.OPERATION_DELETE: {
					esUtil.deleteDocument(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
							CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, uniqueId);
					break;
				}
				}
				break;
			}
			case CompositeSearchConstants.NODE_TYPE_DEFINITION: {
				ObjectDefinitionCache.resyncDefinition(objectType, graphId);
			}
			}
		}
	}

	private void addOrUpdateIndex(String uniqueId, String jsonIndexDocument) throws Exception {
		esUtil.addDocumentWithId(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE,
				uniqueId, jsonIndexDocument);
	}

	private void createCompositeSearchIndex() throws IOException {
		String settings = "{ \"settings\": {   \"index\": {     \"index\": \""
				+ CompositeSearchConstants.COMPOSITE_SEARCH_INDEX
				+ "\",     \"type\": \""
				+ CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE
				+ "\",     \"analysis\": {       \"analyzer\": {         \"cs_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"cs_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   } }}";
		String mappings = "{ \""
				+ CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE
				+ "\" : {    \"dynamic_templates\": [      {        \"longs\": {          \"match_mapping_type\": \"long\",          \"mapping\": {            \"type\": \"long\",            fields: {              \"raw\": {                \"type\": \"long\"              }            }          }        }      },      {        \"booleans\": {          \"match_mapping_type\": \"boolean\",          \"mapping\": {            \"type\": \"boolean\",            fields: {              \"raw\": {                \"type\": \"boolean\"              }            }          }        }      },{        \"doubles\": {          \"match_mapping_type\": \"double\",          \"mapping\": {            \"type\": \"double\",            fields: {              \"raw\": {                \"type\": \"double\"              }            }          }        }      },	  {        \"dates\": {          \"match_mapping_type\": \"date\",          \"mapping\": {            \"type\": \"date\",            fields: {              \"raw\": {                \"type\": \"date\"              }            }          }        }      },      {        \"strings\": {          \"match_mapping_type\": \"string\",          \"mapping\": {            \"type\": \"string\",            \"copy_to\": \"all_fields\",            \"analyzer\": \"cs_index_analyzer\",            \"search_analyzer\": \"cs_search_analyzer\",            fields: {              \"raw\": {                \"type\": \"string\",                \"analyzer\": \"keylower\"              }            }          }        }      }    ],    \"properties\": {      \"all_fields\": {        \"type\": \"string\",        \"analyzer\": \"cs_index_analyzer\",        \"search_analyzer\": \"cs_search_analyzer\",        fields: {          \"raw\": {            \"type\": \"string\",            \"analyzer\": \"keylower\"          }        }      }    }  }}";
		esUtil.addIndex(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, settings,
				mappings);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, Object> getIndexDocument(Map<String, Object> message, Map<String, Object> definitionNode,
			Map<String, String> relationDefinition, boolean updateRequest) throws IOException {
		Map<String, Object> indexDocument = new HashMap<String, Object>();
		String uniqueId = (String) message.get("nodeUniqueId");
		if (updateRequest) {
			String documentJson = esUtil.getDocumentAsStringById(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
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
						Object propertyNewValue = ((Map<String, Object>) propertyMap.getValue()).get("nv"); // new value
																											// of the
																											// property
						if (propertyNewValue == null) // New value from transaction data is null, then remove the
														// property from document
							indexDocument.remove(propertyName);
						else {
							indexDocument.put(propertyName, propertyNewValue);
						}

					}
				}
			}
			List<Map<String, Object>> addedRelations = (List<Map<String, Object>>) transactionData.get("addedRelations");
			if (null != addedRelations && !addedRelations.isEmpty()) {
				for (Map<String, Object> rel : addedRelations) {
					String key = rel.get("dir") + "_" + rel.get("type") + "_" + rel.get("rel");
					String title = relationDefinition.get(key);
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
			List<Map<String, Object>> removedRelations = (List<Map<String, Object>>) transactionData.get("removedRelations");
			if (null != removedRelations && !removedRelations.isEmpty()) {
				for (Map<String, Object> rel : removedRelations) {
					String key = rel.get("dir") + "_" + rel.get("type") + "_" + rel.get("rel");
					String title = relationDefinition.get(key);
					if (StringUtils.isNotBlank(title)) {
						List<String> list = (List<String>) indexDocument.get(title);
						if (null != list && !list.isEmpty()) {
							String id = (String) rel.get("id");
							if (StringUtils.isNotBlank(id) && list.contains(id)) {
								list.remove(id);
								indexDocument.put(title, list);
							}
						}
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
		indexDocument.put("graph_id", (String) message.get("graphId"));
		indexDocument.put("node_id", (int) message.get("nodeGraphId"));
		indexDocument.put("identifier", (String) message.get("nodeUniqueId"));
		indexDocument.put("objectType", (String) message.get("objectType"));
		indexDocument.put("nodeType", (String) message.get("nodeType"));
		return indexDocument;
	}

}
