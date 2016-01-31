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
import io.searchbox.indices.settings.GetSettings;

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

	public static void main(String args[]) throws UnknownHostException {
		JSONBuilder settingBuilder = new JSONStringer();
		settingBuilder.object().key("settings").object().key("analysis")
				.object().key("filter").object().key("nfkc_normalizer")
				.object().key("type").value("icu_normalizer").key("name")
				.value("nfkc").endObject().endObject().key("analyzer").object()
				.key("ind_normalizer").object().key("tokenizer")
				.value("icu_tokenizer").key("filter").array()
				.value("nfkc_normalizer").endArray().endObject().endObject()
				.endObject().endObject().endObject();

		JSONBuilder mappingBuilder = new JSONStringer();
		mappingBuilder.object().key("test_type").object().key("properties")
				.object().key("word").object().key("type").value("string")
				.key("analyzer").value("ind_normalizer").endObject()
				.key("rootWord").object().key("type").value("string")
				.key("analyzer").value("ind_normalizer").endObject()
				.key("date").object().key("type").value("date").key("format")
				.value("dd-MMM-yyyy HH:mm:ss").endObject().endObject()
				.endObject().endObject();

		ElasticSearchUtil util = new ElasticSearchUtil();
		util.addIndex("test_index", "test_type", settingBuilder.toString(),
				mappingBuilder.toString());
	}

	public void addIndex(String indexName, String documentType,
			String settings, String mappings) {
		try {
			if (!isIndexExists(indexName)) {
				CreateIndex createIndex = new CreateIndex.Builder(indexName)
						.settings(settings).build();
				client.execute(createIndex);

				GetSettings getSettings = new GetSettings.Builder().addIndex(
						indexName).build();
				client.execute(getSettings);

				PutMapping putMapping = new PutMapping.Builder(indexName,
						documentType, mappings).build();
				client.execute(putMapping);
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
			Map<String, String> jsonObjects) throws Exception {
		if (isIndexExists(indexName)) {
			if (!jsonObjects.isEmpty()) {
				Builder bulkBuilder = new Bulk.Builder()
						.defaultIndex(indexName).defaultType(documentType);
				for (Map.Entry<String, String> entry : jsonObjects.entrySet()) {
					bulkBuilder.addAction(new Index.Builder(entry.getValue())
							.id(entry.getKey()).build());
				}
				Bulk bulk = bulkBuilder.build();
				client.execute(bulk);
			}
		} else {
			throw new Exception("Index does not exist");
		}
	}

	public void bulkIndexWithAutoGenerateIndexId(String indexName,
			String documentType, List<String> jsonObjects) throws Exception {
		if (isIndexExists(indexName)) {
			if (!jsonObjects.isEmpty()) {
				Builder bulkBuilder = new Bulk.Builder()
						.defaultIndex(indexName).defaultType(documentType);
				for (String jsonString : jsonObjects) {
					bulkBuilder
							.addAction(new Index.Builder(jsonString).build());
				}
				Bulk bulk = bulkBuilder.build();
				client.execute(bulk);
			}
		} else {
			throw new Exception("Index does not exist");
		}
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	public List<Object> textSearch(Class objectClass,
			Map<String, Object> matchCriterias, String IndexName,
			String IndexType) throws IOException {
		SearchResult result = search(matchCriterias, null, IndexName,
				IndexType, null, false);
		List<Object> documents = result.getSourceAsObjectList(objectClass);
		return documents;
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	public List<Object> textSearch(Class objectClass,
			Map<String, Object> matchCriterias,
			Map<String, Object> textFiltersMap, String IndexName,
			String IndexType) throws IOException {
		SearchResult result = search(matchCriterias, textFiltersMap, IndexName,
				IndexType, null, false);
		List<Object> documents = result.getSourceAsObjectList(objectClass);
		return documents;
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	public List<Object> textSearch(Class objectClass,
			Map<String, Object> matchCriterias,
			Map<String, Object> textFiltersMap, String IndexName,
			String IndexType, List<Map<String, Object>> groupByList)
			throws IOException {
		SearchResult result = search(matchCriterias, textFiltersMap, IndexName,
				IndexType, groupByList, false);
		List<Object> documents = result.getSourceAsObjectList(objectClass);
		return documents;
	}

	public SearchResult search(Map<String, Object> matchCriterias,
			Map<String, Object> textFiltersMap, String IndexName,
			String IndexType, List<Map<String, Object>> groupBy,
			boolean isDistinct) throws IOException {
		String query = buildJsonForQuery(matchCriterias, textFiltersMap,
				groupBy, isDistinct);
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
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> getCountOfSearch(Class objectClass,
			Map<String, Object> matchCriterias, String IndexName,
			String IndexType, List<Map<String, Object>> groupByList)
			throws IOException {
		Map<String, Object> countMap = new HashMap<String, Object>();
		SearchResult result = search(matchCriterias, null, IndexName,
				IndexType, groupByList, false);
		LinkedTreeMap<String, Object> aggregations = (LinkedTreeMap<String, Object>) result
				.getValue("aggregations");
		if (aggregations != null) {
			for (Map<String, Object> aggregationsMap : groupByList) {
				Map<String, Object> parentCountMap = new HashMap<String, Object>();
				String groupByParent = (String) aggregationsMap
						.get("groupByParent");
				Map aggKeyMap = (Map) aggregations.get(groupByParent);
				List<Map<String, Double>> aggKeyList = (List<Map<String, Double>>) aggKeyMap
						.get("buckets");
				List<Map<String, Object>> parentGroupList = new ArrayList<Map<String, Object>>();
				for (Map aggKeyListMap : aggKeyList) {
					Map<String, Object> parentCountObject = new HashMap<String, Object>();
					parentCountObject.put("count", ((Double) aggKeyListMap
							.get("doc_count")).longValue());
					List<String> groupByChildList = (List<String>) aggregationsMap
							.get("groupByChildList");
					if (groupByChildList != null && !groupByChildList.isEmpty()) {
						Map<String, Object> groupByChildMap = new HashMap<String, Object>();
						for (String groupByChild : groupByChildList) {
							List<Map<String, Long>> childGroupsList = new ArrayList<Map<String, Long>>();
							Map aggChildKeyMap = (Map) aggKeyListMap
									.get(groupByChild);
							List<Map<String, Double>> aggChildKeyList = (List<Map<String, Double>>) aggChildKeyMap
									.get("buckets");
							Map<String, Long> childCountMap = new HashMap<String, Long>();
							for (Map aggChildKeyListMap : aggChildKeyList) {
								childCountMap.put((String) aggChildKeyListMap
										.get("key"),
										((Double) aggChildKeyListMap
												.get("doc_count")).longValue());
								childGroupsList.add(childCountMap);
								groupByChildMap
										.put(groupByChild, childCountMap);
							}
						}
						parentCountObject.putAll(groupByChildMap);
					}
					parentCountMap.put((String) aggKeyListMap.get("key"),
							parentCountObject);
					parentGroupList.add(parentCountMap);
				}
				countMap.put(groupByParent, parentCountMap);
			}
		}
		return countMap;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> getDistinctCountOfSearch(
			Map<String, Object> matchCriterias, String IndexName,
			String IndexType, List<Map<String, Object>> groupByList)
			throws IOException {
		Map<String, Object> countMap = new HashMap<String, Object>();
		SearchResult result = search(matchCriterias, null, IndexName,
				IndexType, groupByList, true);
		LinkedTreeMap<String, Object> aggregations = (LinkedTreeMap<String, Object>) result
				.getValue("aggregations");
		if (aggregations != null) {
			for (Map<String, Object> aggregationsMap : groupByList) {
				Map<String, Object> parentCountMap = new HashMap<String, Object>();
				String groupByParent = (String) aggregationsMap
						.get("groupBy");
				Map aggKeyMap = (Map) aggregations.get(groupByParent);
				List<Map<String, Double>> aggKeyList = (List<Map<String, Double>>) aggKeyMap
						.get("buckets");
				for (Map aggKeyListMap : aggKeyList) {
					String distinctKey = (String) aggregationsMap
							.get("distinctKey");
					Map aggChildKeyMap = (Map) aggKeyListMap.get("distinct_"
							+ distinctKey + "s");
					Long count = ((Double)aggChildKeyMap.get("value")).longValue();
					String keyAsString = (String) aggKeyListMap.get("key_as_string");
					if(keyAsString != null){
						parentCountMap
						.put(keyAsString, count);
					}
					else{
						parentCountMap
								.put((String) aggKeyListMap.get("key"), (Long)count);
					}
				}
				countMap.put(groupByParent, parentCountMap);
			}
		}
		return countMap;
	}

	@SuppressWarnings("unchecked")
	public String buildJsonForQuery(Map<String, Object> matchCriterias,
			Map<String, Object> textFiltersMap,
			List<Map<String, Object>> groupByList, boolean isDistinct)
			throws JsonGenerationException, JsonMappingException, IOException {

		JSONBuilder builder = new JSONStringer();
		builder.object();

		if (matchCriterias != null) {
			builder.key("query").object().key("bool").object().key("should")
					.array();
			for (Map.Entry<String, Object> entry : matchCriterias.entrySet()) {
				if (entry.getValue() instanceof List) {
					for (String matchText : (ArrayList<String>) entry
							.getValue()) {
						builder.object().key("match").object()
								.key(entry.getKey()).value(matchText)
								.endObject().endObject();
					}
				}
			}
			builder.endArray().endObject().endObject();
		}

		if (groupByList != null && !groupByList.isEmpty()) {
			if (!isDistinct) {
				for (Map<String, Object> groupByMap : groupByList) {
					String groupByParent = (String) groupByMap
							.get("groupByParent");
					List<String> groupByChildList = (List<String>) groupByMap
							.get("groupByChildList");
					builder.key("aggs").object().key(groupByParent).object()
							.key("terms").object().key("field")
							.value(groupByParent).endObject();
					if (groupByChildList != null && !groupByChildList.isEmpty()) {
						builder.key("aggs").object();
						for (String childGroupBy : groupByChildList) {
							builder.key(childGroupBy).object().key("terms")
									.object().key("field").value(childGroupBy)
									.endObject().endObject();
						}
						builder.endObject();
					}
					builder.endObject().endObject();
				}
			} else {
				builder.key("aggs").object();
				for (Map<String, Object> groupByMap : groupByList) {
					String groupBy = (String) groupByMap.get("groupBy");
					String distinctKey = (String) groupByMap.get("distinctKey");
					builder.key(groupBy).object().key("terms").object()
							.key("field").value(groupBy).endObject();
					builder.key("aggs").object();
					builder.key("distinct_" + distinctKey + "s").object()
							.key("cardinality").object().key("field")
							.value(distinctKey).endObject().endObject();
					builder.endObject().endObject();
				}
				builder.endObject();
			}
		}
		
		builder.endObject();
		return builder.toString();
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private JSONBuilder addGroupByQuery(List<Map<String, Object>> groupByList,
			boolean isDistinct) {

		JSONBuilder builder = new JSONStringer();

		if (!isDistinct) {
			if (groupByList != null && !groupByList.isEmpty()) {
				builder.key("aggs").object();
				for (Map<String, Object> groupByMap : groupByList) {
					String groupByParent = (String) groupByMap
							.get("groupByParent");
					List<String> groupByChildList = (List<String>) groupByMap
							.get("groupByChildList");
					builder.key("aggs").object().key(groupByParent).object()
							.key("terms").object().key("field")
							.value(groupByParent).endObject();
					if (groupByChildList != null && !groupByChildList.isEmpty()) {
						builder.key("aggs").object();
						for (String childGroupBy : groupByChildList) {
							builder.key(childGroupBy).object().key("terms")
									.object().key("field").value(childGroupBy)
									.endObject().endObject();
						}
						builder.endObject();
					}
					builder.endObject();
				}
				builder.endObject();
			}
		}
		/*
		 * { "aggs": { "sourceType": { "terms": { "field": "sourceType" },
		 * "aggs": { "distinct_rootWords" : { "cardinality" : { "field" :
		 * "rootWord" } } } } } }
		 */
		else {
			if (groupByList != null && !groupByList.isEmpty()) {
				builder.key("aggs").object();
				for (Map<String, Object> groupByMap : groupByList) {
					String groupBy = (String) groupByMap.get("groupBy");
					String distinctKey = (String) groupByMap.get("distinctKey");
					builder.key(groupBy).object().key("terms").object()
							.key("field").value(groupBy).endObject();
					builder.key("aggs").object();
					builder.key("distinct_" + distinctKey + "s").object()
							.key("cardinality").object().key("field")
							.value(distinctKey).endObject().endObject();
					builder.endObject().endObject();
				}
				builder.endObject();
			}
		}
		return builder;
	}

}
