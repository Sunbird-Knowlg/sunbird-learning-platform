/**
 * 
 */
package org.ekstep.jobs.samza.service.util;

import com.datastax.driver.core.*;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.task.Postman;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.mvcsearchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;

import java.io.IOException;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author pradyumna
 *
 */
public class MVCProcessorIndexer extends AbstractESIndexer {

	private JobLogger LOGGER = new JobLogger(MVCProcessorIndexer.class);
	private ObjectMapper mapper = new ObjectMapper();
	private List<String> nestedFields = new ArrayList<String>();
	private ControllerUtil util = new ControllerUtil();
	String arr[];
    String contentreadapi = "", mlworkbenchapirequest = "", mlvectorListRequest = "" , jobname = "" , mlkeywordapi = "" , mlvectorapi = "" , source_url = null ;
	List<String> ml_level1 = null, ml_level2  = null, ml_level3 = null , textbook_name , level1_name , level2_name , level3_name , source;
	public MVCProcessorIndexer() {
		setNestedFields();
			arr = CompositeSearchConstants.propertyArray;
			contentreadapi = CompositeSearchConstants.api;
			mlworkbenchapirequest = CompositeSearchConstants.mlworkbenchapirequest;
			mlvectorListRequest = CompositeSearchConstants.mlvectorListRequest;
			jobname = CompositeSearchConstants.jobname;
			mlkeywordapi = CompositeSearchConstants.mlkeywordapi;
			mlvectorapi = CompositeSearchConstants.mlvectorapi;
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
		String mappings = "{\"dynamic\":\"strict\",\"properties\":{\"all_fields\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"allowedContentTypes\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"appIcon\":{\"type\":\"text\",\"index\":false},\"appIconLabel\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"appId\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"artifactUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"board\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"channel\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"contentEncoding\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"contentType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"description\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"downloadUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"framework\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"gradeLevel\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"identifier\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"language\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"lastUpdatedOn\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}},\"launchUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"medium\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"mimeType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"ml_Keywords\":{\"type\":\"text\",\"analyzer\":\"ml_custom_analyzer\",\"search_analyzer\":\"standard\"},\"ml_contentText\":{\"type\":\"text\",\"analyzer\":\"ml_custom_analyzer\",\"search_analyzer\":\"standard\"},\"ml_contentTextVector\":{\"type\":\"dense_vector\",\"dims\":768},\"textbook_name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"sourceURL\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level1Name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level1Concept\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level2Name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level2Concept\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level3Name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level3Concept\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"nodeType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"node_id\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}},\"objectType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"organisation\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"pkgVersion\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}},\"posterImage\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"previewUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"resourceType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"source\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"status\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"streamingUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"subject\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"}}}";
		ElasticSearchUtil.addIndex(CompositeSearchConstants.MVC_SEARCH_INDEX,
				CompositeSearchConstants.MVC_SEARCH_INDEX_TYPE, settings, mappings,alias);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })


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
		List<String> ml_Keywords = obj.get("ml_Keywords") != null ? (List<String>) obj.get("ml_Keywords") : null;
		String ml_contentText = obj.get("ml_contentText") != null ? obj.get("ml_contentText").toString() : null;
		List<List<Double>> ml_contentTextVectorList = obj.get("ml_contentTextVector") != null ? (List<List<Double>>) obj.get("ml_contentTextVector") : null;
		if(ml_contentTextVectorList != null)
		{
			ml_contentTextVector = new HashSet<Double>(ml_contentTextVectorList.get(0));

		}

		// Connect to cassandra
		String serverIP = "127.0.0.1";
		String keyspace = "sunbirddev_content_store";
		Cluster cluster = Cluster.builder()
				.addContactPoints(serverIP)
				.build();

		Session session = cluster.connect(keyspace);
		if(StringUtils.isNotBlank(action)) {
			if(action.equalsIgnoreCase("update-es-index")) {
				obj = getContentDefinition(obj ,identifier);
				cqlquery = "UPDATE content_data SET textbook_name = ? ,level1_concept = ? , level2_concept = ? , level3_concept = ?  , level1_name = ? , level2_name = ? , level3_name = ? , source = ? , source_url = ? WHERE content_id = ?";
				PreparedStatement prepared = preparestaement(session,cqlquery);
				 bound = prepared.bind(textbook_name,ml_level1,ml_level2,ml_level3,level1_name ,level2_name,level3_name,source,source_url,identifier);
			} else if(action.equalsIgnoreCase("update-ml-keywords")) {
				cqlquery = "UPDATE content_data SET ml_keywords = ? , ml_content_text = ? WHERE content_id = ?";
				makepostreqForVectorApi(ml_contentText,identifier);
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
			extractFieldstobeinserted(contentobj);
			makepostreqForMlAPI(contentobj);
			newmap = filterData(newmap,contentobj);

		}catch (Exception e) {
            System.out.println(e);
		}
		return newmap;
}

   //Getting Fields to be inserted into cassandra
   private void extractFieldstobeinserted(JSONObject contentobj) {
		if(contentobj.has("level1Concept")) {
			ml_level1 =  (List<String>)contentobj.get("level1Concept") ;
		}
	   ml_level2 = contentobj.has("level2Concept")  ? (List<String>)contentobj.get("level2Concept") : null;
	   ml_level3 = contentobj.has("level3Concept")  ? (List<String>)contentobj.get("level3Concept") : null;
	   textbook_name = contentobj.has("textbook_name")  ? (List<String>)contentobj.get("textbook_name") : null;
	   level1_name = contentobj.has("level1Name")  ? (List<String>)contentobj.get("level1Name") : null;
	   level2_name = contentobj.has("level2Name")  ? (List<String>)contentobj.get("level2Name") : null;
	   level3_name = contentobj.has("level3Name")  ? (List<String>)contentobj.get("level3Name") : null;
	   source = contentobj.has("source")  ? (List<String>)contentobj.get("source") : null;
	   source_url = contentobj.has("sourceURL")  ? contentobj.get("sourceURL").toString() : null;
  }

	// POST reqeuest for ml keywords api
	void makepostreqForMlAPI(JSONObject contentdef) {
	JSONObject obj = new JSONObject(mlworkbenchapirequest);
	JSONObject req = ((JSONObject)(obj.get("request")));
	JSONObject input = (JSONObject)req.get("input");
		JSONArray content = (JSONArray)input.get("content");
		content.put(contentdef);
	req.put("job",jobname);
	String resp = Postman.POST(obj.toString(),mlkeywordapi);
}

   // Filter the params of content defintion to add in ES
	public  Map<String,Object> filterData(Map<String,Object> obj ,JSONObject content) {

		String key = null;
		Object value = null;
		for(int i = 0 ; i < arr.length ; i++ ) {
			key = (arr[i]);
			value = content.has(key)  ? content.get(key) : null;
			if(value != null) {
				obj.put(key,value);
				value = null;
			}
		}
		return obj;

	 }

	 // Post reqeuest for vector api
	public  void makepostreqForVectorApi(String contentText,String identifier) {
		try {
			JSONObject obj = new JSONObject(mlworkbenchapirequest);
			JSONObject req = ((JSONObject) (obj.get("request")));
			JSONArray text = (JSONArray) req.get("text");
			req.put("cid",identifier);
			text.put(contentText);
			String resp = Postman.POST(obj.toString(),mlvectorapi);
			JSONObject respobj = new JSONObject(resp);
			String status = (((JSONObject)respobj.get("result")).get("status")).toString();
			System.out.println("Vector list api status is " + status);
		}
		catch (Exception e) {

		}
	}


}
