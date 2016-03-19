package org.ekstep.searchindex.elasticsearch;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Bulk;
import io.searchbox.core.Bulk.Builder;
import io.searchbox.core.Delete;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
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
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.searchindex.util.PropertiesUtil;

import com.google.gson.internal.LinkedTreeMap;

public class ElasticSearchUtil {

	private JestClient client;
	private String hostName;
	private String port;
	private int defaultResultLimit = 10000;
	private int BATCH_SIZE = 10000;
	private int resultLimit = defaultResultLimit;
	private ObjectMapper mapper = new ObjectMapper();
	
	

	public void setDefaultResultLimit(int defaultResultLimit) {
		this.resultLimit = defaultResultLimit;
	}

	public ElasticSearchUtil(int resultSize) throws UnknownHostException {
		super();
		initialize();
		if (resultSize < defaultResultLimit) {
			this.resultLimit = resultSize;
		}
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig.Builder(hostName + ":" + port).multiThreaded(true).build());
		client = factory.getObject();

	}

	public ElasticSearchUtil() {
		super();
		initialize();
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig.Builder(hostName + ":" + port).multiThreaded(true).build());
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

	public void addDocumentWithId(String indexName, String documentType, String documentId, String document)
			throws IOException {
		Index index = new Index.Builder(document).index(indexName).type(documentType).id(documentId).build();
		client.execute(index);
	}

	public static void main(String args[]) throws IOException {
		JSONBuilder settingBuilder = new JSONStringer();
		settingBuilder.object().key("settings").object().key("analysis").object().key("filter").object()
				.key("nfkc_normalizer").object().key("type").value("icu_normalizer").key("name").value("nfkc")
				.endObject().endObject().key("analyzer").object().key("ind_normalizer").object().key("tokenizer")
				.value("icu_tokenizer").key("filter").array().value("nfkc_normalizer").endArray().endObject()
				.endObject().endObject().endObject().endObject();

		JSONBuilder mappingBuilder = new JSONStringer();
		mappingBuilder.object().key("test_type").object().key("properties").object().key("word").object().key("type")
				.value("string").key("analyzer").value("ind_normalizer").endObject().key("rootWord").object()
				.key("type").value("string").key("analyzer").value("ind_normalizer").endObject().key("date").object()
				.key("type").value("date").key("format").value("dd-MMM-yyyy HH:mm:ss").endObject().endObject()
				.endObject().endObject();

		ElasticSearchUtil util = new ElasticSearchUtil(10000);
		util.addIndex("test_index", "test_type", settingBuilder.toString(), mappingBuilder.toString());
	}

	public void addIndex(String indexName, String documentType, String settings, String mappings) throws IOException {
		if (!isIndexExists(indexName)) {
			if(settings != null){
				CreateIndex createIndex = new CreateIndex.Builder(indexName).settings(settings).build();
				client.execute(createIndex);
			}
			else{
				CreateIndex createIndex = new CreateIndex.Builder(indexName).build();
				client.execute(createIndex);
			}
			if (settings != null) {
				GetSettings getSettings = new GetSettings.Builder().addIndex(indexName).build();
				client.execute(getSettings);
			}

			if (mappings != null) {
				PutMapping putMapping = new PutMapping.Builder(indexName, documentType, mappings).build();
				client.execute(putMapping);
			}
		}
	}

	public void addDocument(String indexName, String documentType, String document) throws IOException {
		Index index = new Index.Builder(document).index(indexName).type(documentType).build();
		client.execute(index);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateDocument(String indexName, String documentType, String document, String documentId) throws IOException {
		Map<String, Object> updates = mapper.readValue(document, new TypeReference<Map<String, Object>>() {});
		Get get = new Get.Builder(indexName, documentId).type(documentType).build();
		JestResult result = client.execute(get);
		Map documentAsMap = result.getSourceAsObject(Map.class);
		for(Map.Entry<String, Object> entry: updates.entrySet()){
			documentAsMap.put(entry.getKey(), entry.getValue());
		}
		String updatedDocument =  mapper.writeValueAsString(documentAsMap);
		addDocumentWithId(indexName, documentType, documentId, updatedDocument);
	}
	
	public String getDocumentAsStringById(String indexName, String documentType, String documentId) throws IOException {
		Get get = new Get.Builder(indexName, documentId).type(documentType).build();
		JestResult result = client.execute(get);
		return result.getSourceAsString();
	}

	public boolean isIndexExists(String indexName) throws IOException {
		return client.execute(new IndicesExists.Builder(indexName).build()).isSucceeded();
	}

	public void deleteDocument(String indexName, String documentType, String documentId) throws IOException {
		client.execute(new Delete.Builder(documentId).index(indexName).type(documentType).build());
	}

	public void deleteIndex(String indexName) throws IOException {
		client.execute(new DeleteIndex.Builder(indexName).build());
	}

	public void bulkIndexWithIndexId(String indexName, String documentType, Map<String, String> jsonObjects)
			throws Exception {
		if (isIndexExists(indexName)) {
			if (!jsonObjects.isEmpty()) {
				int count=0;
				Builder bulkBuilder = new Bulk.Builder().defaultIndex(indexName).defaultType(documentType);
				for (Map.Entry<String, String> entry : jsonObjects.entrySet()) {
					count++;
					bulkBuilder.addAction(new Index.Builder(entry.getValue()).id(entry.getKey()).build());
					if(count%BATCH_SIZE ==0 || (count%BATCH_SIZE<BATCH_SIZE && count==jsonObjects.size())){
						Bulk bulk = bulkBuilder.build();
						client.execute(bulk);
						bulkBuilder = new Bulk.Builder().defaultIndex(indexName).defaultType(documentType);
					}
				}
			}
		} else {
			throw new Exception("Index does not exist");
		}
	}

	public void bulkIndexWithAutoGenerateIndexId(String indexName, String documentType, List<String> jsonObjects)
			throws Exception {
		if (isIndexExists(indexName)) {
			if (!jsonObjects.isEmpty()) {
				Builder bulkBuilder = new Bulk.Builder().defaultIndex(indexName).defaultType(documentType);
				for (String jsonString : jsonObjects) {
					bulkBuilder.addAction(new Index.Builder(jsonString).build());
				}
				Bulk bulk = bulkBuilder.build();
				client.execute(bulk);
			}
		} else {
			throw new Exception("Index does not exist");
		}
	}

	@SuppressWarnings({ "rawtypes" })
	public List<Object> textSearch(Class objectClass, Map<String, Object> matchCriterias, String IndexName,
			String IndexType) throws IOException {
		SearchResult result = search(matchCriterias, null, IndexName, IndexType, null, false);
		return getDocumentsFromSearchResult(result, objectClass);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Object> getDocumentsFromSearchResult(SearchResult result, Class objectClass) {
		List<Hit<Object, Void>> hits = result.getHits(objectClass);
		return getDocumentsFromHits(hits);
	}

	@SuppressWarnings("rawtypes")
	public List<Object> getDocumentsFromHits(List<Hit<Object, Void>> hits) {
		List<Object> documents = new ArrayList<Object>();
		for (Hit hit : hits) {
			documents.add(hit.source);
		}
		return documents;
	}

	@SuppressWarnings({ "rawtypes" })
	public List<Object> wildCardSearch(Class objectClass, String textKeyWord, String wordWildCard, String indexName,
			String indexType) throws IOException {
		SearchResult result = wildCardSearch(textKeyWord, wordWildCard, indexName, indexType);
		return getDocumentsFromSearchResult(result, objectClass);
	}

	public SearchResult wildCardSearch(String textKeyWord, String wordWildCard, String indexName, String indexType)
			throws IOException {
		String query = buildJsonForWildCardQuery(textKeyWord, wordWildCard);
		return search(indexName, indexType, query);
	}

	@SuppressWarnings({ "rawtypes" })
	public List<Object> textFiltersSearch(Class objectClass, Map<String, Object> searchCriteria,
			Map<String, Object> textFiltersMap, String indexName, String indexType) throws IOException {
		SearchResult result = search(searchCriteria, textFiltersMap, indexName, indexType, null, false);
		return getDocumentsFromSearchResult(result, objectClass);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> textFiltersGroupBySearch(Class objectClass, Map<String, Object> searchCriteria,
			Map<String, Object> textFiltersMap, List<Map<String, Object>> groupByList, String indexName,
			String indexType) throws IOException {
		SearchResult result = search(searchCriteria, textFiltersMap, indexName, indexType, groupByList, false);
		List<Object> documents = getDocumentsFromSearchResult(result, objectClass);
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("objects", documents);

		if (result.getAggregations() != null) {
			LinkedTreeMap<String, Object> aggregations = (LinkedTreeMap<String, Object>) result
					.getValue("aggregations");
			response.put("aggregations", getCountFromAggregation(aggregations, groupByList));
		}
		return response;
	}

	@SuppressWarnings({ "rawtypes" })
	public List<Object> textSearch(Class objectClass, Map<String, Object> matchCriterias,
			Map<String, Object> textFiltersMap, String IndexName, String IndexType) throws IOException {
		SearchResult result = search(matchCriterias, textFiltersMap, IndexName, IndexType, null, false);
		return getDocumentsFromSearchResult(result, objectClass);
	}

	@SuppressWarnings({ "rawtypes" })
	public List<Object> textSearch(Class objectClass, Map<String, Object> matchCriterias,
			Map<String, Object> textFiltersMap, String IndexName, String IndexType,
			List<Map<String, Object>> groupByList) throws IOException {
		SearchResult result = search(matchCriterias, textFiltersMap, IndexName, IndexType, groupByList, false);
		return getDocumentsFromSearchResult(result, objectClass);
	}

	public SearchResult search(Map<String, Object> matchCriterias, Map<String, Object> textFiltersMap, String IndexName,
			String IndexType, List<Map<String, Object>> groupBy, boolean isDistinct) throws IOException {
		String query = buildJsonForQuery(matchCriterias, textFiltersMap, groupBy, isDistinct);
		return search(IndexName, IndexType, query);
	}

	@SuppressWarnings("unused")
	public SearchResult search(String IndexName, String IndexType, String query) throws IOException {
		Search search = new Search.Builder(query).addIndex(IndexName).addType(IndexType)
				.setParameter("size", resultLimit).build();
		long startTime = System.currentTimeMillis();
		SearchResult result = client.execute(search);
		if (result.getErrorMessage() != null) {
			throw new IOException(result.getErrorMessage());
		}
		long endTime = System.currentTimeMillis();
		long diff = endTime - startTime;
		return result;
	}

	@SuppressWarnings("unused")
	public SearchResult search(String IndexName, String query) throws IOException {
		Search search = new Search.Builder(query).addIndex(IndexName)
				.setParameter("size", resultLimit).build();
		long startTime = System.currentTimeMillis();
		SearchResult result = client.execute(search);
		if (result.getErrorMessage() != null) {
			throw new IOException(result.getErrorMessage());
		}
		long endTime = System.currentTimeMillis();
		long diff = endTime - startTime;
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> getCountFromAggregation(LinkedTreeMap<String, Object> aggregations,
			List<Map<String, Object>> groupByList) {
		Map<String, Object> countMap = new HashMap<String, Object>();
		if (aggregations != null) {
			for (Map<String, Object> aggregationsMap : groupByList) {
				Map<String, Object> parentCountMap = new HashMap<String, Object>();
				String groupByParent = (String) aggregationsMap.get("groupByParent");
				Map aggKeyMap = (Map) aggregations.get(groupByParent);
				List<Map<String, Double>> aggKeyList = (List<Map<String, Double>>) aggKeyMap.get("buckets");
				List<Map<String, Object>> parentGroupList = new ArrayList<Map<String, Object>>();
				for (Map aggKeyListMap : aggKeyList) {
					Map<String, Object> parentCountObject = new HashMap<String, Object>();
					parentCountObject.put("count", ((Double) aggKeyListMap.get("doc_count")).longValue());
					List<String> groupByChildList = (List<String>) aggregationsMap.get("groupByChildList");
					if (groupByChildList != null && !groupByChildList.isEmpty()) {
						Map<String, Object> groupByChildMap = new HashMap<String, Object>();
						for (String groupByChild : groupByChildList) {
							List<Map<String, Long>> childGroupsList = new ArrayList<Map<String, Long>>();
							Map aggChildKeyMap = (Map) aggKeyListMap.get(groupByChild);
							List<Map<String, Double>> aggChildKeyList = (List<Map<String, Double>>) aggChildKeyMap
									.get("buckets");
							Map<String, Long> childCountMap = new HashMap<String, Long>();
							for (Map aggChildKeyListMap : aggChildKeyList) {
								childCountMap.put((String) aggChildKeyListMap.get("key"),
										((Double) aggChildKeyListMap.get("doc_count")).longValue());
								childGroupsList.add(childCountMap);
								groupByChildMap.put(groupByChild, childCountMap);
							}
						}
						parentCountObject.putAll(groupByChildMap);
					}
					parentCountMap.put((String) aggKeyListMap.get("key"), parentCountObject);
					parentGroupList.add(parentCountMap);
				}
				countMap.put(groupByParent, parentCountMap);
			}
		}
		return countMap;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> getCountOfSearch(Class objectClass, Map<String, Object> matchCriterias, String IndexName,
			String IndexType, List<Map<String, Object>> groupByList) throws IOException {
		SearchResult result = search(matchCriterias, null, IndexName, IndexType, groupByList, false);
		LinkedTreeMap<String, Object> aggregations = (LinkedTreeMap<String, Object>) result.getValue("aggregations");
		return getCountFromAggregation(aggregations, groupByList);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> getDistinctCountOfSearch(Map<String, Object> matchCriterias, String IndexName,
			String IndexType, List<Map<String, Object>> groupByList) throws IOException {
		Map<String, Object> countMap = new HashMap<String, Object>();
		SearchResult result = search(matchCriterias, null, IndexName, IndexType, groupByList, true);
		LinkedTreeMap<String, Object> aggregations = (LinkedTreeMap<String, Object>) result.getValue("aggregations");
		if (aggregations != null) {
			for (Map<String, Object> aggregationsMap : groupByList) {
				Map<String, Object> parentCountMap = new HashMap<String, Object>();
				String groupByParent = (String) aggregationsMap.get("groupBy");
				Map aggKeyMap = (Map) aggregations.get(groupByParent);
				List<Map<String, Double>> aggKeyList = (List<Map<String, Double>>) aggKeyMap.get("buckets");
				for (Map aggKeyListMap : aggKeyList) {
					String distinctKey = (String) aggregationsMap.get("distinctKey");
					Map aggChildKeyMap = (Map) aggKeyListMap.get("distinct_" + distinctKey + "s");
					Long count = ((Double) aggChildKeyMap.get("value")).longValue();
					String keyAsString = (String) aggKeyListMap.get("key_as_string");
					if (keyAsString != null) {
						parentCountMap.put(keyAsString, count);
					} else {
						parentCountMap.put((String) aggKeyListMap.get("key"), (Long) count);
					}
				}
				countMap.put(groupByParent, parentCountMap);
			}
		}
		return countMap;
	}

	@SuppressWarnings("unchecked")
	public String buildJsonForQuery(Map<String, Object> matchCriterias, Map<String, Object> textFiltersMap,
			List<Map<String, Object>> groupByList, boolean isDistinct)
					throws JsonGenerationException, JsonMappingException, IOException {

		JSONBuilder builder = new JSONStringer();
		builder.object().key("query").object().key("filtered").object();

		if (matchCriterias != null) {
			builder.key("query").object().key("bool").object().key("should").array();
			for (Map.Entry<String, Object> entry : matchCriterias.entrySet()) {
				if (entry.getValue() instanceof List) {
					for (String matchText : (ArrayList<String>) entry.getValue()) {
						builder.object().key("match").object().key(entry.getKey()).value(matchText).endObject()
								.endObject();
					}
				}
			}
			builder.endArray().endObject().endObject();
		}

		if (textFiltersMap != null && !textFiltersMap.isEmpty()) {
			builder.key("filter").object().key("bool").object().key("must").array();
			for (Map.Entry<String, Object> entry : textFiltersMap.entrySet()) {
				builder.object().key("terms").object().key(entry.getKey()).array();
				ArrayList<String> termValues = (ArrayList<String>) entry.getValue();
				for (String termValue : termValues) {
					builder.value(termValue);
				}
				builder.endArray().endObject().endObject();
			}
			builder.endArray().endObject().endObject();
		}

		builder.endObject().endObject();

		if (groupByList != null && !groupByList.isEmpty()) {
			if (!isDistinct) {
				for (Map<String, Object> groupByMap : groupByList) {
					String groupByParent = (String) groupByMap.get("groupByParent");
					List<String> groupByChildList = (List<String>) groupByMap.get("groupByChildList");
					builder.key("aggs").object().key(groupByParent).object().key("terms").object().key("field")
							.value(groupByParent).endObject();
					if (groupByChildList != null && !groupByChildList.isEmpty()) {
						builder.key("aggs").object();
						for (String childGroupBy : groupByChildList) {
							builder.key(childGroupBy).object().key("terms").object().key("field").value(childGroupBy)
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
					builder.key(groupBy).object().key("terms").object().key("field").value(groupBy).endObject();
					builder.key("aggs").object();
					builder.key("distinct_" + distinctKey + "s").object().key("cardinality").object().key("field")
							.value(distinctKey).endObject().endObject();
					builder.endObject().endObject();
				}
				builder.endObject();
			}
		}

		builder.endObject();
		return builder.toString();
	}

	private String buildJsonForWildCardQuery(String textKeyWord, String wordWildCard) {

		/*
		 * { "query": { "wildcard": { "word": "ಏ*" } } }
		 */

		JSONBuilder builder = new JSONStringer();
		builder.object().key("query").object().key("wildcard").object().key(textKeyWord).value(wordWildCard).endObject()
				.endObject().endObject();

		return builder.toString();
	}
}
