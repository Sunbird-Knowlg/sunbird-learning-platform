package org.ekstep.language.util;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Bulk;
import io.searchbox.core.Bulk.Builder;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.IndicesExists;
import io.searchbox.indices.mapping.PutMapping;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.gson.internal.LinkedTreeMap;

public class ElasticSearchUtil {

	private JestClient client;
	private String hostName;
	private String port;

	public ElasticSearchUtil() throws UnknownHostException {
		super();
		initialize();
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig.Builder(hostName + ":"
				+ port).multiThreaded(true).build());
		client = factory.getObject();
	}

	public void initialize() {
		hostName = PropertiesUtil.getProperty("elastic-search-host");
		port = PropertiesUtil.getProperty("elastic-search-port");
	}

	@SuppressWarnings("unused")
	private JestClient createClient() {
		return client;
	}

	public void addDocumentWithId(String indexName, String documentType,
			String documentId, String document) throws IOException {
		Index index = new Index.Builder(document).index(indexName)
				.type(documentType).id(documentId).build();
		client.execute(index);
	}

	public void addIndex(String indexName, String documentType) {
		try {
			if (!isIndexExists(indexName)) {

				/*
				 * JSONBuilder settingsBuilder = new JSONStringer();
				 * settingsBuilder.object() .key("settings").object()
				 * .key("analysis").object() .key("filter").object()
				 * .key("nfkc_normalizer").object()
				 * .key("type").value("icu_normalizer")
				 * .key("name").value("nfkc") .endObject() .endObject() .key(
				 * "analyzer").object() .key("my_normalizer").object()
				 * .key("tokenizer").value("icu_tokenizer")
				 * .key("filter").value(JSONArray.fromObject(new
				 * String[]{"nfkc_normalizer"})) .endObject() .endObject()
				 * .endObject() .endObject();
				 */

				/*
				 * JSONBuilder settingsBuilder = new JSONStringer();
				 * settingsBuilder.object() .key("analysis").object()
				 * .key("filter").object() .key("nfkc_normalizer").object()
				 * .key("type").value("icu_normalizer")
				 * .key("name").value("nfkc") .endObject() .endObject() .key(
				 * "analyzer").object() .key("my_normalizer").object()
				 * .key("tokenizer").value("icu_tokenizer")
				 * .key("filter").value(JSONArray.fromObject(new
				 * String[]{"nfkc_normalizer"})) .endObject() .endObject()
				 * .endObject();
				 */

				String settings = "\"settings\" : {\n"
						+ "        \"number_of_shards\" : 10,\n"
						+ "        \"number_of_replicas\" : 2\n" + "    }\n";

				client.execute(new CreateIndex.Builder(indexName).settings(
						settings).build());

				// client.execute(new CreateIndex.Builder(indexName).build());

				PutMapping putMapping = new PutMapping.Builder(
						indexName,
						documentType,
						"{ \"citation\" : { \"properties\" : { \"date\" : {\"type\" : \"date\", \"format\" : \"dd-MMM-yyyy HH:mm:ss\"} } } }")
						.build();

				client.execute(putMapping);

				/*
				 * PutMapping putMapping = new PutMapping.Builder(indexName,
				 * documentType, obj.toJSONString()).build();
				 * client.execute(putMapping);
				 * 
				 * obj = new JSONObject(); JSONObject wordObj = new
				 * JSONObject(); wordObj.put("type", "string");
				 * wordObj.put("analyzer", "my_normalizer");
				 * obj.put(documentType, new JSONObject().put("properties", new
				 * JSONObject().put("word", wordObj))); putMapping = new
				 * PutMapping.Builder(indexName, documentType,
				 * obj.toJSONString()).build(); client.execute(putMapping);
				 */
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addDocument(String indexName, String documentType,
			String document) throws IOException {
		Index index = new Index.Builder(document).index(indexName)
				.type(documentType).build();
		client.execute(index);
	}

	public boolean isIndexExists(String indexName) throws IOException {
		return client.execute(new IndicesExists.Builder(indexName).build())
				.isSucceeded();
	}

	public void deleteDocument(String indexName, String documentType,
			String documentId) throws IOException {
		client.execute(new Delete.Builder(documentId).index(indexName)
				.type(documentType).build());
	}

	public void bulkIndexWithIndexId(String indexName, String documentType,
			Map<String, String> jsonObjects) throws IOException {
		addIndex(indexName, documentType);

		if (!jsonObjects.isEmpty()) {
			Builder bulkBuilder = new Bulk.Builder().defaultIndex(indexName)
					.defaultType(documentType);
			for (Map.Entry<String, String> entry : jsonObjects.entrySet()) {
				bulkBuilder.addAction(new Index.Builder(entry.getValue()).id(
						entry.getKey()).build());
			}
			Bulk bulk = bulkBuilder.build();
			client.execute(bulk);
		}
	}

	public void bulkIndexWithAutoGenerateIndexId(String indexName,
			String documentType, List<String> jsonObjects) throws IOException {
		addIndex(indexName, documentType);
		if (!jsonObjects.isEmpty()) {
			Builder bulkBuilder = new Bulk.Builder().defaultIndex(indexName)
					.defaultType(documentType);
			for (String jsonString : jsonObjects) {
				bulkBuilder.addAction(new Index.Builder(jsonString).build());
			}
			Bulk bulk = bulkBuilder.build();
			client.execute(bulk);
		}
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	public List<Object> textSearch(Class objectClass,
			Map<String, Object> textFilters, String IndexName, String IndexType)
			throws IOException {
		SearchResult result = search(textFilters, IndexName, IndexType, null);
		List<Object> documents = result.getSourceAsObjectList(objectClass);
		return documents;
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	public List<Object> textSearch(Class objectClass,
			Map<String, Object> textFilters, String IndexName, String IndexType, List<Map<String, Object>> groupByList)
			throws IOException {
		SearchResult result = search(textFilters, IndexName, IndexType, groupByList);
		List<Object> documents = result.getSourceAsObjectList(objectClass);
		return documents;
	}

	
	public SearchResult search(Map<String, Object> textFilters,
			String IndexName, String IndexType, List<Map<String, Object>> groupBy)
			throws IOException {
		String query = buildJsonForQuery(textFilters, groupBy);
		return search(IndexName, IndexType, query);
	}

	public SearchResult search(String IndexName, String IndexType, String query)
			throws IOException {
		Search search = new Search.Builder(query).addIndex(IndexName)
				.addType(IndexType).build();
		long startTime = System.currentTimeMillis();
		SearchResult result = client.execute(search);
		long endTime = System.currentTimeMillis();
		long diff = endTime - startTime;
		System.out.println("Time taken for search :" + diff + "ms");
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> getCountOfSearch(Class objectClass,
			Map<String, Object> textFilters, String IndexName,
			String IndexType, List<Map<String, Object>> groupByList) throws IOException {
		Map<String, Object> countMap = new HashMap<String, Object>();
		SearchResult result = search(textFilters, IndexName, IndexType, groupByList);
		LinkedTreeMap<String, Object> aggregations = (LinkedTreeMap<String, Object>) result.getValue("aggregations");
		for(Map<String, Object> aggregationsMap : groupByList){
			Map<String, Object> parentCountMap = new HashMap<String, Object>();
			String groupByParent = (String) aggregationsMap.get("groupByParent");
			Map aggKeyMap = (Map) aggregations.get(groupByParent);;
			List<Map<String, Double>> aggKeyList = (List<Map<String, Double>>) aggKeyMap.get("buckets");
			List<Map<String, Object>> parentGroupList = new ArrayList<Map<String,Object>>();
			for(Map aggKeyListMap: aggKeyList){
				Map<String, Object> parentCountObject = new HashMap<String, Object>();
				parentCountObject.put("count", ((Double)aggKeyListMap.get("doc_count")).longValue());
				List<String> groupByChildList = (List<String>) aggregationsMap.get("groupByChildList");
				if(groupByChildList != null && !groupByChildList.isEmpty()){
					Map<String, Object> groupByChildMap = new HashMap<String, Object>();
					for(String groupByChild : groupByChildList){
						List<Map<String, Long>> childGroupsList =  new ArrayList<Map<String,Long>>();
						Map aggChildKeyMap = (Map) aggKeyListMap.get(groupByChild);
						List<Map<String, Double>> aggChildKeyList = (List<Map<String, Double>>) aggChildKeyMap.get("buckets");
						Map<String, Long> childCountMap = new HashMap<String, Long>();
						for(Map aggChildKeyListMap: aggChildKeyList){
							childCountMap.put((String)aggChildKeyListMap.get("key"), ((Double)aggChildKeyListMap.get("doc_count")).longValue());
							childGroupsList.add(childCountMap);
							groupByChildMap.put(groupByChild, childCountMap);
						}
					}
					parentCountObject.put("childGroups", groupByChildMap);
				}
				parentCountMap.put((String)aggKeyListMap.get("key"), parentCountObject);
				parentGroupList.add(parentCountMap);
			}
			countMap.put(groupByParent, parentCountMap);
		}
		return countMap;
	}

	@SuppressWarnings("unchecked")
	public String buildJsonForQuery(Map<String, Object> texts,
			List<Map<String, Object>> groupByList) throws JsonGenerationException,
			JsonMappingException, IOException {

		JSONBuilder builder = new JSONStringer();
		builder.object().key("query").object().key("bool").object()
				.key("should").array();

		for (Map.Entry<String, Object> entry : texts.entrySet()) {
			if (entry.getValue() instanceof List) {
				for (String matchText : (ArrayList<String>) entry.getValue()) {
					builder.object().key("match").object().key(entry.getKey())
							.value(matchText).endObject().endObject();
				}
			}
		}
		builder.endArray().endObject().endObject();
		if (groupByList != null && !groupByList.isEmpty()) {
			for (Map<String, Object> groupByMap : groupByList) {
				String groupByParent = (String) groupByMap.get("groupByParent");
				List<String> groupByChildList = (List<String>) groupByMap.get("groupByChildList");
				builder.key("aggs").object()
					.key(groupByParent).object()
						.key("terms").object()
							.key("field").value(groupByParent)
						.endObject();
						if(groupByChildList != null && !groupByChildList.isEmpty()){
							builder.key("aggs").object();
							for(String childGroupBy : groupByChildList){
								builder.key(childGroupBy).object()
									.key("terms").object()
										.key("field").value(childGroupBy)
									.endObject()
								.endObject();
							}
							builder.endObject();
						}
					builder.endObject()
				.endObject();
			}
		}

		builder.endObject();
		return builder.toString();

	}
}
