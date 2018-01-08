/**
 * 
 */
package org.ekstep.jobs.samza.service.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;

/**
 * @author pradyumna
 *
 */
public class DialCodeIndexUtil {

	private ObjectMapper mapper = new ObjectMapper();
	private ElasticSearchUtil esUtil = null;

	public DialCodeIndexUtil(ElasticSearchUtil esUtil) {
		this.esUtil = esUtil;
	}

	public void createDialCodeIndex() throws IOException {
		String settings = "{ \"settings\": {   \"index\": {     \"index\": \""
				+ CompositeSearchConstants.DIAL_CODE_INDEX + "\",     \"type\": \""
				+ CompositeSearchConstants.DIAL_CODE_INDEX_TYPE
				+ "\",     \"analysis\": {       \"analyzer\": {         \"dc_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"dc_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   } }}";
		String mappings = "{ \"" + CompositeSearchConstants.DIAL_CODE_INDEX_TYPE
				+ "\" : {    \"dynamic_templates\": [      {        \"longs\": {          \"match_mapping_type\": \"long\",          \"mapping\": {            \"type\": \"long\",            fields: {              \"raw\": {                \"type\": \"long\"              }            }          }        }      },      {        \"booleans\": {          \"match_mapping_type\": \"boolean\",          \"mapping\": {            \"type\": \"boolean\",            fields: {              \"raw\": {                \"type\": \"boolean\"              }            }          }        }      },{        \"doubles\": {          \"match_mapping_type\": \"double\",          \"mapping\": {            \"type\": \"double\",            fields: {              \"raw\": {                \"type\": \"double\"              }            }          }        }      },	  {        \"dates\": {          \"match_mapping_type\": \"date\",          \"mapping\": {            \"type\": \"date\",            fields: {              \"raw\": {                \"type\": \"date\"              }            }          }        }      },      {        \"strings\": {          \"match_mapping_type\": \"string\",          \"mapping\": {            \"type\": \"string\",            \"copy_to\": \"all_fields\",            \"analyzer\": \"dc_index_analyzer\",            \"search_analyzer\": \"dc_search_analyzer\",            fields: {              \"raw\": {                \"type\": \"string\",                \"analyzer\": \"keylower\"              }            }          }        }      }    ],    \"properties\": {      \"all_fields\": {        \"type\": \"string\",        \"analyzer\": \"dc_index_analyzer\",        \"search_analyzer\": \"dc_search_analyzer\",        fields: {          \"raw\": {            \"type\": \"string\",            \"analyzer\": \"keylower\"          }        }      }    }  }}";
		esUtil.addIndex(CompositeSearchConstants.DIAL_CODE_INDEX, CompositeSearchConstants.DIAL_CODE_INDEX_TYPE,
				settings, mappings);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, Object> getIndexDocument(Map<String, Object> message, boolean updateRequest)
			throws IOException {
		Map<String, Object> indexDocument = new HashMap<String, Object>();
		String uniqueId = (String) message.get("nodeUniqueId");
		if (updateRequest) {
			String documentJson = esUtil.getDocumentAsStringById(CompositeSearchConstants.DIAL_CODE_INDEX,
					CompositeSearchConstants.DIAL_CODE_INDEX_TYPE, uniqueId);
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
						// new value of the property
						Object propertyNewValue = ((Map<String, Object>) propertyMap.getValue()).get("nv");
						// New value from transaction data is null, then remove
						// the property from document
						if (propertyNewValue == null)
							indexDocument.remove(propertyName);
						else {
							indexDocument.put(propertyName, propertyNewValue);
						}
					}
				}
			}
		}
		indexDocument.put("identifier", (String) message.get("nodeUniqueId"));
		indexDocument.put("objectType", (String) message.get("objectType"));
		return indexDocument;
	}

	private void addOrUpdateIndex(String uniqueId, String jsonIndexDocument) throws Exception {
		esUtil.addDocumentWithId(CompositeSearchConstants.DIAL_CODE_INDEX,
				CompositeSearchConstants.DIAL_CODE_INDEX_TYPE, uniqueId, jsonIndexDocument);
	}

	public void addOrUpdateDoc(String uniqueId, Map<String, Object> message) throws Exception {
		String operationType = (String) message.get("operationType");
		switch (operationType) {
		case CompositeSearchConstants.OPERATION_CREATE: {
			Map<String, Object> indexDocument = getIndexDocument(message, false);
			String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
			addOrUpdateIndex(uniqueId, jsonIndexDocument);
			break;
		}
		case CompositeSearchConstants.OPERATION_UPDATE: {
			Map<String, Object> indexDocument = getIndexDocument(message, true);
			String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
			addOrUpdateIndex(uniqueId, jsonIndexDocument);
			break;
		}
		case CompositeSearchConstants.OPERATION_DELETE: {
			esUtil.deleteDocument(CompositeSearchConstants.DIAL_CODE_INDEX,
					CompositeSearchConstants.DIAL_CODE_INDEX_TYPE, uniqueId);
			break;
		}
		}
	}

}
