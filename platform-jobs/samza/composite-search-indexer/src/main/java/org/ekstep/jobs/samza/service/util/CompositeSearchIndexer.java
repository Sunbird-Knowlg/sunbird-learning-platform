/**
 * 
 */
package org.ekstep.jobs.samza.service.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.config.MapConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.Platform;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;

/**
 * @author pradyumna
 *
 */
public class CompositeSearchIndexer {

	private JobLogger LOGGER = new JobLogger(CompositeSearchIndexer.class);
	public ObjectMapper mapper = new ObjectMapper();
	private ElasticSearchUtil esUtil = null;
	private Map<String, String> nestedFields = new HashMap<String, String>();

	private ControllerUtil util = new ControllerUtil();
	public CompositeSearchIndexer(ElasticSearchUtil esUtil) {
		this.esUtil = esUtil;
		setNestedFields();
	}

	/**
	 * @return
	 */
	private void setNestedFields() {
		if (Platform.config.hasPath("nested.fields")) {
			String fieldsList = Platform.config.getString("nested.fields");
			for (String field : fieldsList.split(",")) {
				nestedFields.put(field.split(":")[0], field.split(":")[1]);
			}
		}
	}

	public void createCompositeSearchIndex() throws IOException {
		String settings = "{\"analysis\": {       \"analyzer\": {         \"cs_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"cs_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   }";
		String mappings = "{\"dynamic_templates\":[{\"nested\":{\"match_mapping_type\":\"nested\",\"mapping\":{\"type\":\"nested\",\"fields\":{\"type\":\"nested\"}}}},{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"string\",\"copy_to\":\"all_fields\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"string\",\"analyzer\":\"keylower\"}}}}}],\"properties\":{\"all_fields\":{\"type\":\"string\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"string\",\"analyzer\":\"keylower\"}}}}}";
		esUtil.addIndex(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, settings, mappings);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, Object> getIndexDocument(Map<String, Object> message,
			Map<String, String> relationMap, boolean updateRequest) throws Exception {
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
						// new value of the property
						Object propertyNewValue = ((Map<String, Object>) propertyMap.getValue()).get("nv");
						// New value from transaction data is null, then remove
						// the property from document
						if (propertyNewValue == null)
							indexDocument.remove(propertyName);
						else {
							if (nestedFields.containsKey(propertyName)) {
								propertyNewValue = getNestedPropertyValue(propertyName, propertyNewValue);
							}
							indexDocument.put(propertyName, propertyNewValue);
						}
					}
				}
			}
			List<Map<String, Object>> addedRelations = (List<Map<String, Object>>) transactionData
					.get("addedRelations");
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
			List<Map<String, Object>> removedRelations = (List<Map<String, Object>>) transactionData
					.get("removedRelations");
			if (null != removedRelations && !removedRelations.isEmpty()) {
				for (Map<String, Object> rel : removedRelations) {
					String key = rel.get("dir") + "_" + rel.get("type") + "_" + rel.get("rel");
					String title = (String) relationMap.get(key);
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
		}
		indexDocument.put("graph_id", (String) message.get("graphId"));
		indexDocument.put("node_id", (int) message.get("nodeGraphId"));
		indexDocument.put("identifier", (String) message.get("nodeUniqueId"));
		indexDocument.put("objectType", (String) message.get("objectType"));
		indexDocument.put("nodeType", (String) message.get("nodeType"));
		return indexDocument;
	}

	/**
	 * @param propertyNewValue
	 * @param propertyNewValue2
	 * @return
	 * @throws Exception
	 */
	private Object getNestedPropertyValue(String propertyName, Object propertyNewValue) throws Exception {
		String fieldType = nestedFields.get(propertyName);

		switch (fieldType) {
		case "JSONList":
			return mapper.readValue((String) propertyNewValue, new TypeReference<List<Map>>() {
			});
		}

		return propertyNewValue;
	}

	private void upsertDocument(String uniqueId, String jsonIndexDocument) throws Exception {
		esUtil.addDocumentWithId(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, uniqueId, jsonIndexDocument);
	}

	@SuppressWarnings("rawtypes")
	private Map<String, String> getRelationMap(String objectType, Map definitionNode) throws Exception {
		Map<String, String> relationDefinition = retrieveRelations(definitionNode, "IN", "inRelations");
		relationDefinition.putAll(retrieveRelations(definitionNode, "OUT", "outRelations"));
		return relationDefinition;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map<String, String> retrieveRelations(Map definitionNode, String direction, String relationProperty)
			throws Exception {
		Map<String, String> definition = new HashMap<String, String>();
		List<Map> inRelsList = (List<Map>) definitionNode.get(relationProperty);
		if (null != inRelsList && !inRelsList.isEmpty()) {
			for (Map relMap : inRelsList) {
				List<String> objectTypes = (List<String>) relMap.get("objectTypes");
				if (null != objectTypes && !objectTypes.isEmpty()) {
					for (String type : objectTypes) {
						String key = direction + "_" + type + "_" + (String) relMap.get("relationName");
						definition.put(key, (String) relMap.get("title"));
					}
				}
			}
		}
		return definition;
	}

	public void processESMessage(String graphId, String objectType, String uniqueId, Map<String, Object> message,
			JobMetrics metrics) throws Exception {

		DefinitionDTO definitionNode = util.getDefinition(graphId, objectType);
		if (null == definitionNode) {
			metrics.incFailedCounter();
			LOGGER.info("Failed to fetch definition node from cache");
		}
		Map<String, Object> definition = mapper.convertValue(definitionNode, new TypeReference<Map<String, Object>>() {
		});
		LOGGER.debug("definition fetched from cache: " + definitionNode.getIdentifier());
		LOGGER.info(uniqueId + " is indexing into compositesearch.");
		Map<String, String> relationMap = getRelationMap(objectType, definition);
		upsertDocument(uniqueId, message, relationMap);
	}

	private void upsertDocument(String uniqueId, Map<String, Object> message, Map<String, String> relationMap)
			throws Exception {
		String operationType = (String) message.get("operationType");
		switch (operationType) {
		case CompositeSearchConstants.OPERATION_CREATE: {
			Map<String, Object> indexDocument = getIndexDocument(message, relationMap, false);
			String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
			upsertDocument(uniqueId, jsonIndexDocument);
			break;
		}
		case CompositeSearchConstants.OPERATION_UPDATE: {
			Map<String, Object> indexDocument = getIndexDocument(message, relationMap, true);
			String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
			upsertDocument(uniqueId, jsonIndexDocument);
			break;
		}
		case CompositeSearchConstants.OPERATION_DELETE: {
			esUtil.deleteDocument(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
					CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, uniqueId);
			break;
		}
		}
	}

	public static void main(String[] args) throws Exception {
		Map<String, String> props = new HashMap<String, String>();
		props.put("search.es_conn_info", "localhost:9300");
		props.put("nested.fields", "badgeAssertions:jsonList");
		/*JobMetrics metrics = mock(JobMetrics.class);*/
		Config config = new MapConfig(props);
		JSONUtils.loadProperties(config);
		CompositeSearchIndexer indexer = new CompositeSearchIndexer(new ElasticSearchUtil());
		indexer.createCompositeSearchIndex();

		String emessage = "{\"ets\":1520922733196,\"channel\":\"in.ekstep\",\"transactionData\":{\"properties\":{\"code\":{\"ov\":null,\"nv\":\"test-content-badge4\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"mimeType\":{\"ov\":null,\"nv\":\"application/pdf\"},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2018-03-13T12:02:07.711+0530\"},\"badgeAssertions\":{\"ov\":null,\"nv\":\"[{\\\"id\\\":\\\"badge3\\\",\\\"issue\\\":{\\\"id\\\":\\\"ekstep\\\"}}]\"},\"contentDisposition\":{\"ov\":null,\"nv\":\"inline\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2018-03-13T12:02:07.711+0530\"},\"contentEncoding\":{\"ov\":null,\"nv\":\"identity\"},\"contentType\":{\"ov\":null,\"nv\":\"Resource\"},\"audience\":{\"ov\":null,\"nv\":[\"Learner\"]},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"mediaType\":{\"ov\":null,\"nv\":\"content\"},\"osId\":{\"ov\":null,\"nv\":\"org.ekstep.quiz.app\"},\"versionKey\":{\"ov\":null,\"nv\":\"1520922727711\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"framework\":{\"ov\":null,\"nv\":\"NCF\"},\"compatibilityLevel\":{\"ov\":null,\"nv\":1.0},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"Test content Badging\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"do_11245939898551500813\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"},\"resourceType\":{\"ov\":null,\"nv\":\"Story\"}}},\"label\":\"Test content Badging\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2018-03-13T12:02:13.196+0530\",\"objectType\":\"Content\",\"nodeUniqueId\":\"do_11245939898551500813\",\"requestId\":null,\"operationType\":\"CREATE\",\"nodeGraphId\":281654,\"graphId\":\"domain\"}";

		Map<String, Object> message = indexer.mapper.readValue(emessage, new TypeReference<Map<String, Object>>() {
		});

		String objectType = (String) message.get("objectType");
		String graphId = (String) message.get("graphId");
		String uniqueId = (String) message.get("nodeUniqueId");

		indexer.processESMessage(graphId, objectType, uniqueId, message, null);
	}

}
