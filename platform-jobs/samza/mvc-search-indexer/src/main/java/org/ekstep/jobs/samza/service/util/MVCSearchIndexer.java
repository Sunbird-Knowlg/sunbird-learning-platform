/**
 * 
 */
package org.ekstep.jobs.samza.service.util;

import com.datastax.driver.core.*;
import net.sf.json.JSON;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.task.Postman;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author pradyumna
 *
 */
public class MVCSearchIndexer extends AbstractESIndexer {

	private JobLogger LOGGER = new JobLogger(MVCSearchIndexer.class);
	private ObjectMapper mapper = new ObjectMapper();
	private List<String> nestedFields = new ArrayList<String>();
	private ControllerUtil util = new ControllerUtil();
	JSONArray arr;
    String contentreadapi = "", mlworkbenchapirequest = "", mlvectorListRequest = "" , jobname = "" , mlkeywordapi = "" , mlvectorapi = "";
	public MVCSearchIndexer() {
		setNestedFields();
		Properties prop = new Properties();
		try {
			InputStream ip = (this.getClass().getResourceAsStream("/MVCSearchConstants.properties"));
			prop.load(ip);
			arr = new JSONArray(prop.getProperty("propertyArray"));
			contentreadapi = prop.getProperty("api");
			mlworkbenchapirequest = prop.getProperty("mlworkbenchapirequest");
			mlvectorListRequest = prop.getProperty("mlvectorListRequest");
			jobname = prop.getProperty("jobname");
			mlkeywordapi = prop.getProperty("mlkeywordapi");
			mlvectorapi = prop.getProperty("mlvectorapi");

		}
		catch (Exception e) {
         System.out.println(e);
		}
	}

	@Override
	public void init() {
		ElasticSearchUtil.initialiseESClient(CompositeSearchConstants.MVC_SEARCH_INDEX,
				Platform.config.getString("search.es_conn_info"));
	}

	/**
	 * @return
	 */
	private void setNestedFields() {
		if (Platform.config.hasPath("nested.fields")) {
			String fieldsList = Platform.config.getString("nested.fields");
			for (String field : fieldsList.split(",")) {
				nestedFields.add(field);
			}
		}
	}

	public void createMVCSearchIndex() throws IOException {
		String alias = "mvc-content";
		String settings = "{\"settings\":{\"index\":{\"max_ngram_diff\":\"29\",\"mapping\":{\"total_fields\":{\"limit\":\"1500\"}},\"number_of_shards\":\"5\",\"provided_name\":\"mvc-content-v1\",\"creation_date\":\"1591163797342\",\"analysis\":{\"filter\":{\"mynGram\":{\"token_chars\":[\"letter\",\"digit\",\"whitespace\",\"punctuation\",\"symbol\"],\"min_gram\":\"1\",\"type\":\"nGram\",\"max_gram\":\"30\"}},\"analyzer\":{\"cs_index_analyzer\":{\"filter\":[\"lowercase\",\"mynGram\"],\"type\":\"custom\",\"tokenizer\":\"standard\"},\"keylower\":{\"filter\":\"lowercase\",\"tokenizer\":\"keyword\"},\"ml_custom_analyzer\":{\"type\":\"standard\",\"stopwords\":[\"_english_\",\"_hindi_\"]},\"cs_search_analyzer\":{\"filter\":[\"lowercase\"],\"type\":\"custom\",\"tokenizer\":\"standard\"}}},\"number_of_replicas\":\"1\"}}}";
		String mappings = "{\"dynamic\":\"strict\",\"properties\":{\"all_fields\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"label\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"} ,\"allowedContentTypes\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"appIcon\":{\"type\":\"text\",\"index\":false},\"appIconLabel\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"appId\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"artifactUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"board\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"channel\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"contentEncoding\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"contentType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"description\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"downloadUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"framework\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"gradeLevel\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"identifier\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"language\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"lastUpdatedOn\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}},\"launchUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"medium\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"mimeType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"ml_Keywords\":{\"type\":\"text\",\"analyzer\":\"ml_custom_analyzer\",\"search_analyzer\":\"standard\"},\"ml_contentText\":{\"type\":\"text\",\"analyzer\":\"ml_custom_analyzer\",\"search_analyzer\":\"standard\"},\"ml_contentTextVector\":{\"type\":\"dense_vector\",\"dims\":768},\"ml_level1Concept\":{\"type\":\"text\",\"analyzer\":\"ml_custom_analyzer\",\"search_analyzer\":\"standard\"},\"ml_level2Concept\":{\"type\":\"text\",\"analyzer\":\"ml_custom_analyzer\",\"search_analyzer\":\"standard\"},\"ml_level3Concept\":{\"type\":\"text\",\"analyzer\":\"ml_custom_analyzer\",\"search_analyzer\":\"standard\"},\"name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"nodeType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"node_id\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}},\"objectType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"organisation\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"pkgVersion\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}},\"posterImage\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"previewUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"resourceType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"source\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"status\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"streamingUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"subject\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"}}}";
		ElasticSearchUtil.addIndex(CompositeSearchConstants.MVC_SEARCH_INDEX,
				CompositeSearchConstants.MVC_SEARCH_INDEX_TYPE, settings, mappings,alias);
	}

	private Map<String, Object> getIndexDocument(String id) throws Exception {
		Map<String, Object> indexDocument = new HashMap<String, Object>();
		String documentJson = ElasticSearchUtil.getDocumentAsStringById(
				CompositeSearchConstants.MVC_SEARCH_INDEX,
				CompositeSearchConstants.MVC_SEARCH_INDEX_TYPE, id);
		if (documentJson != null && !documentJson.isEmpty()) {
			indexDocument = mapper.readValue(documentJson, new TypeReference<Map<String, Object>>() {
			});
		}
		return indexDocument;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
/*
	private Map<String, Object> getIndexDocument(Map<String, Object> message,
												 Map<String, String> relationMap, boolean updateRequest, List<String> indexableProps) throws Exception {
		Map<String, Object> indexDocument = new HashMap<String, Object>();
		String uniqueId = (String) message.get("nodeUniqueId");
		if (updateRequest) {
			String documentJson = ElasticSearchUtil.getDocumentAsStringById(
					CompositeSearchConstants.MVC_SEARCH_INDEX,
					CompositeSearchConstants.MVC_SEARCH_INDEX_TYPE, uniqueId);
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
						// filter metadata based on definition
						if (CollectionUtils.isNotEmpty(indexableProps)) {
							if (indexableProps.contains(propertyName)) {
								addMetadataToDocument(propertyMap, propertyName, indexDocument);
							}
						} else {
							addMetadataToDocument(propertyMap, propertyName, indexDocument);
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
*/

	public void upsertDocument(String uniqueId, Map<String,Object> jsonIndexDocument) throws Exception {

		jsonIndexDocument = insertintoCassandra(jsonIndexDocument,uniqueId);
		String stage = jsonIndexDocument.get("stage").toString();
		jsonIndexDocument = removeExtraParams(jsonIndexDocument);
		if(stage.equalsIgnoreCase("1")) {

			// Insert a new doc
			ElasticSearchUtil.addDocumentWithId(CompositeSearchConstants.MVC_SEARCH_INDEX,
					uniqueId, mapper.writeValueAsString(jsonIndexDocument));
		} else {
			// Update a doc
			ElasticSearchUtil.updateDocument(CompositeSearchConstants.MVC_SEARCH_INDEX,
					uniqueId, mapper.writeValueAsString(jsonIndexDocument));
		}

	}

	// Adding label key and value
	Map<String,Object> addLabel(Map<String,Object> obj) {
		obj.put("label","MVC");
		return obj;
	}

	// Remove params which should not be inserted into ES
	Map<String,Object> removeExtraParams(Map<String,Object> obj) {
		obj.remove("action");
		obj.remove("stage");
		 return obj;
	}
	// Insert to cassandra
	Map<String,Object> insertintoCassandra(Map<String,Object> obj,String identifier) {
		String action = obj.get("action").toString();
		String cqlquery = "";
		Set<Double> ml_contentTextVector = null;
		BoundStatement bound = null;
		String label = obj.get("label") != null ? obj.get("label").toString() : null;
        String ml_level1 = obj.get("ml_level1Concept") != null ? obj.get("ml_level1Concept").toString() : null;
		String ml_level2 = obj.get("ml_level2Concept") != null ? obj.get("ml_level2Concept").toString() : null;
		String ml_level3 = obj.get("ml_level3Concept") != null ? obj.get("ml_level3Concept").toString() : null;
		List<String> ml_Keywords = obj.get("ml_Keywords") != null ? (List<String>) obj.get("ml_Keywords") : null;
		String ml_contentText = obj.get("ml_contentText") != null ? obj.get("ml_contentText").toString() : null;
		List<Double> ml_contentTextVectorList = obj.get("ml_contentTextVector") != null ? (List<Double>) obj.get("ml_contentTextVector") : null;
		if(ml_contentTextVectorList != null)
		 ml_contentTextVector = new HashSet<Double>(ml_contentTextVectorList);
        
		String serverIP = "127.0.0.1";
		String keyspace = "sunbirddev_content_store";
		Cluster cluster = Cluster.builder()
				.addContactPoints(serverIP)
				.build();

		Session session = cluster.connect(keyspace);
		if(StringUtils.isNotBlank(action)) {
			if(action.equalsIgnoreCase("update-es-index")) {
				obj = addLabel(obj);
				obj = getContentDefinition(obj ,identifier);
				cqlquery = "UPDATE content_data SET ml_level1_concept = ? , ml_level2_concept = ? , ml_level3_concept = ? , label = ? WHERE content_id = ?";
				PreparedStatement prepared = preparestaement(session,cqlquery);
				 bound = prepared.bind(ml_level1,ml_level2,ml_level3,label ,identifier);
			} else if(action.equalsIgnoreCase("update-ml-keywords")) {
				cqlquery = "UPDATE content_data SET ml_keywords = ? , ml_content_text = ? WHERE content_id = ?";
				makepostreqForVectorApi(ml_contentText);
				PreparedStatement prepared = preparestaement(session,cqlquery);
				 bound = prepared.bind(ml_Keywords,ml_contentText,identifier);
			}
			else  if(action.equalsIgnoreCase("update-ml-contenttextvector")) {
				cqlquery = "UPDATE content_data SET ml_content_text_vector = ? WHERE content_id = ?";
				PreparedStatement prepared = preparestaement(session,cqlquery);
				 bound = prepared.bind(ml_contentTextVector,identifier);
			}
		}
		if(cqlquery != "" && bound != null) {
			session.execute(bound);
		}
		return obj;
	}
	PreparedStatement  preparestaement(Session session,String query) {
		return session.prepare(query);
	}

	// Read content definition
	Map<String,Object> getContentDefinition(Map<String,Object> newmap , String identifer) {
		try {
			String content = Postman.getContent(contentreadapi,identifer);
			JSONObject obj = new JSONObject(content);
			JSONObject contentobj = (JSONObject) (((JSONObject)obj.get("result")).get("content"));
			makepostreqForMlAPI(contentobj);
			newmap = filterData(newmap,contentobj);

		}catch (Exception e) {
            System.out.println(e);
		}
		return newmap;
}
    void makepostreqForMlAPI(JSONObject contentdef) {
	JSONObject obj = new JSONObject(mlworkbenchapirequest);
	JSONObject req = ((JSONObject)(obj.get("request")));
	JSONObject input = (JSONObject)req.get("input");
		JSONArray content = (JSONArray)input.get("content");
		content.put(contentdef);
	req.put("job",jobname);
	// input.put("content",contentdef);
	Postman.POST(obj.toString(),mlkeywordapi);
}
   // Filter the params of content defintion to add in ES
	public  Map<String,Object> filterData(Map<String,Object> obj ,JSONObject content) {

		String key = null;
		Object value = null;
		for(int i = 0 ; i < arr.length() ; i++ ) {
			key = (arr.get(i)).toString();
			value = content.has(key)  ? content.get(key) : null;
			if(value != null) {
				obj.put(key,value);
				value = null;
			}
		}
		return obj;

	 }
	public  void makepostreqForVectorApi(String contentText) {
		try {
			JSONObject obj = new JSONObject(mlworkbenchapirequest);
			JSONObject req = ((JSONObject) (obj.get("request")));
			JSONArray text = (JSONArray) req.get("text");
			text.put(contentText);
			Postman.POST(obj.toString(),mlvectorapi);
		}
		catch (Exception e) {

		}
	}
	/*@SuppressWarnings("rawtypes")
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

	public void processESMessage(String graphId, String objectType, String uniqueId, String messageId,
			 Map<String, Object> message, JobMetrics metrics) throws Exception {
		List<String> indexablePropslist = new ArrayList<String>();
		DefinitionDTO definitionNode = util.getDefinition(graphId, objectType);
		if (null == definitionNode) {
			LOGGER.info("Failed to fetch definition node from cache");
			throw new PlatformException(PlatformErrorCodes.ERR_DEFINITION_NOT_FOUND.name(),
					"defnition node for graphId:" + graphId + " and objectType:" + objectType
							+ " is null due to some issue");
		}
		Map<String, Object> definition = mapper.convertValue(definitionNode, new TypeReference<Map<String, Object>>() {
		});
		LOGGER.debug("definition fetched from cache: " + definitionNode.getIdentifier());
		//Create List of metadata which should be indexed, if objectType is enabled for metadata filtration.
		List<String> objectTypeList = Platform.config.hasPath("restrict.metadata.objectTypes") ?
				Arrays.asList(Platform.config.getString("restrict.metadata.objectTypes").split(",")) : Collections.emptyList();
		if (objectTypeList.contains(objectType))
			indexablePropslist = getIndexableProperties(definition);

		LOGGER.info("Message Id: " + messageId + ", " + "Unique Id: " + uniqueId + " is indexing into mvcsearch.");
		Map<String, String> relationMap = getRelationMap(objectType, definition);
		*//*upsertDocument(uniqueId, message, relationMap, indexablePropslist);*//*
	}

*//*	private void upsertDocument(String uniqueId, Map<String, Object> message, Map<String, String> relationMap, List<String> indexableProps)
			throws Exception {
		String operationType = (String) message.get("operationType");
		switch (operationType) {
		case CompositeSearchConstants.OPERATION_CREATE: {
			Map<String, Object> indexDocument = getIndexDocument(message, relationMap, false, indexableProps);
			String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
			upsertDocument(uniqueId, jsonIndexDocument);
			break;
		}
		case CompositeSearchConstants.OPERATION_UPDATE: {
			Map<String, Object> indexDocument = getIndexDocument(message, relationMap, true, indexableProps);
			String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
			upsertDocument(uniqueId, jsonIndexDocument);
			break;
		}
		case CompositeSearchConstants.OPERATION_DELETE: {
			String id = (String) message.get("nodeUniqueId");
			Map<String, Object> indexDocument = getIndexDocument(id);
			String visibility = (String) indexDocument.get("visibility");
			if (StringUtils.equalsIgnoreCase("Parent", visibility)) {
				LOGGER.info("Not deleting the document (visibility: Parent) with ID:" + id);
			} else {
				ElasticSearchUtil.deleteDocument(CompositeSearchConstants.MVC_SEARCH_INDEX,
						CompositeSearchConstants.MVC_SEARCH_INDEX_TYPE, uniqueId);
			}
			break;
		}
		}
	}*/

/*
	private List<String> getIndexableProperties(Map<String, Object> definition) {
		List<String> propsList = new ArrayList<>();
		List<Map<String, Object>> properties = (List<Map<String, Object>>) definition.get("properties");
		for (Map<String, Object> property : properties) {
			if ((Boolean) property.get("indexed")) {
				propsList.add((String) property.get("propertyName"));
			}
		}
		return propsList;
	}
*/

/*
	private void addMetadataToDocument(Map.Entry<String, Object> propertyMap, String propertyName, Map<String, Object> indexDocument) throws Exception {
		// new value of the property
		Object propertyNewValue = ((Map<String, Object>) propertyMap.getValue()).get("nv");
		// New value from transaction data is null, then remove the property from document
		if (propertyNewValue == null)
			indexDocument.remove(propertyName);
		else {
			if (nestedFields.contains(propertyName)) {
				propertyNewValue = mapper.readValue((String) propertyNewValue,
						new TypeReference<Object>() {
						});
			}
			indexDocument.put(propertyName, propertyNewValue);
		}
	}
*/

}
