package org.ekstep.searchindex.elasticsearch;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.searchindex.transformer.IESResultTransformer;
import org.ekstep.searchindex.util.PropertiesUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.ilimi.common.logger.LogHelper;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Bulk;
import io.searchbox.core.Bulk.Builder;
import io.searchbox.core.Count;
import io.searchbox.core.CountResult;
import io.searchbox.core.Delete;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import io.searchbox.core.MultiGet;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.IndicesExists;
import io.searchbox.indices.mapping.PutMapping;
import io.searchbox.indices.settings.GetSettings;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

public class ElasticSearchUtil {

	private static LogHelper LOGGER = LogHelper.getInstance(ElasticSearchUtil.class.getName());
	private JestClient client;
	private String hostName;
	private String port;
	public int defaultResultLimit = 10000;
	private int BATCH_SIZE = 1000;
	private int CONNECTION_TIMEOUT = 30;
	private long MAX_IDLE_CONNECTION_TIME_LIMIT = 10;		// In Seconds
	private int MAX_TOTAL_CONNECTION_LIMIT = 500;
	public int resultLimit = defaultResultLimit;
	private ObjectMapper mapper = new ObjectMapper();

	public void setResultLimit(int resultLimit) {
		this.resultLimit = resultLimit;
	}

	public ElasticSearchUtil(int resultSize) throws UnknownHostException {
		super();
		initialize();
		if (resultSize < defaultResultLimit) {
			this.resultLimit = resultSize;
		}
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig.Builder(hostName + ":" + port).multiThreaded(true)
				.connTimeout(CONNECTION_TIMEOUT).maxConnectionIdleTime(MAX_IDLE_CONNECTION_TIME_LIMIT, TimeUnit.SECONDS)
				.maxTotalConnection(MAX_TOTAL_CONNECTION_LIMIT).build());
		client = factory.getObject();

	}

	public ElasticSearchUtil() {
		super();
		initialize();
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig.Builder(hostName + ":" + port).multiThreaded(true)
				.connTimeout(CONNECTION_TIMEOUT).build());
		client = factory.getObject();
	}


	public void initialize() {
		hostName = PropertiesUtil.getProperty("elastic-search-host");
		port = PropertiesUtil.getProperty("elastic-search-port");
		if (PropertiesUtil.getProperty("bulk-load-batch-size") != null) {
			BATCH_SIZE = Integer.parseInt(PropertiesUtil.getProperty("bulk-load-batch-size"));
		}
		if (PropertiesUtil.getProperty("connection-timeout") != null) {
			CONNECTION_TIMEOUT = Integer.parseInt(PropertiesUtil.getProperty("connection-timeout"));
		}
	}
	
	public void finalize() {
		if (null != client)
			client.shutdownClient();
	}

	public List<String> getQuerySearchFields() {
		String querySearchFieldsProperty = PropertiesUtil.getProperty("query-search-fields");
		List<String> querySearchFields = new ArrayList<String>();
		if (querySearchFieldsProperty != null && !querySearchFieldsProperty.isEmpty()) {
			String[] querySearchFieldsArray = querySearchFieldsProperty.split(",");
			querySearchFields = Arrays.asList(querySearchFieldsArray);
		}
		return querySearchFields;
	}

	public List<String> getDateFields() {
		String querySearchFieldsProperty = PropertiesUtil.getProperty("date-fields");
		List<String> querySearchFields = new ArrayList<String>();
		if (querySearchFieldsProperty != null && !querySearchFieldsProperty.isEmpty()) {
			String[] querySearchFieldsArray = querySearchFieldsProperty.split(",");
			querySearchFields = Arrays.asList(querySearchFieldsArray);
		}
		return querySearchFields;
	}

	public String getTimeZone() {
		String timeZoneProperty = PropertiesUtil.getProperty("time-zone");
		if (timeZoneProperty == null) {
			timeZoneProperty = "0000";
		}
		return timeZoneProperty;
	}

	@SuppressWarnings("unused")
	private JestClient createClient() {
		return client;
	}

	public void addDocumentWithId(String indexName, String documentType, String documentId, String document)
			throws IOException {
		Index index = new Index.Builder(document).index(indexName).type(documentType).id(documentId).build();
		client.execute(index);
		LOGGER.info("Added " + documentId + " to index " + indexName);
	}

	public void addIndex(String indexName, String documentType, String settings, String mappings) throws IOException {
		if (!isIndexExists(indexName)) {
			if (settings != null) {
				CreateIndex createIndex = new CreateIndex.Builder(indexName).settings(settings).build();
				client.execute(createIndex);
			} else {
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
	public void updateDocument(String indexName, String documentType, String document, String documentId)
			throws IOException {
		Map<String, Object> updates = mapper.readValue(document, new TypeReference<Map<String, Object>>() {
		});
		Get get = new Get.Builder(indexName, documentId).type(documentType).build();
		JestResult result = client.execute(get);
		Map documentAsMap = result.getSourceAsObject(Map.class);
		for (Map.Entry<String, Object> entry : updates.entrySet()) {
			documentAsMap.put(entry.getKey(), entry.getValue());
		}
		String updatedDocument = mapper.writeValueAsString(documentAsMap);
		addDocumentWithId(indexName, documentType, documentId, updatedDocument);
	}

	public String getDocumentAsStringById(String indexName, String documentType, String documentId) throws IOException {
		Get get = new Get.Builder(indexName, documentId).type(documentType).build();
		JestResult result = client.execute(get);
		return result.getSourceAsString();
	}
	
	public List<String> getMultiDocumentAsStringByIdList(String indexName, String documentType, List<String> documentIdList) throws IOException {
		List<String> finalResult = new ArrayList<String>();
		MultiGet get = new MultiGet.Builder.ById(indexName, documentType).addId(documentIdList).build();
		JestResult result = client.execute(get);
		JsonArray actualDocs = result.getJsonObject().getAsJsonArray("docs");
		for(int i=0;i<actualDocs.size();i++)
		{
			JsonObject actualDoc1 = actualDocs.get(i).getAsJsonObject();
			if(actualDoc1.get("found").getAsBoolean())
			{
				JsonObject actualSource = actualDoc1.getAsJsonObject("_source");
				finalResult.add(actualSource.toString());
			}
		}
		return finalResult;

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
				int count = 0;
				Builder bulkBuilder = new Bulk.Builder().defaultIndex(indexName).defaultType(documentType);
				for (Map.Entry<String, String> entry : jsonObjects.entrySet()) {
					count++;
					bulkBuilder.addAction(new Index.Builder(entry.getValue()).id(entry.getKey()).build());
					if (count % BATCH_SIZE == 0 || (count % BATCH_SIZE < BATCH_SIZE && count == jsonObjects.size())) {
						Bulk bulk = bulkBuilder.build();
						client.execute(bulk);
						System.out.println("Bulk indexed " + BATCH_SIZE + " documents");
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

	@SuppressWarnings("rawtypes")
	public List<Map> getDocumentsFromSearchResultWithScore(SearchResult result) {
		List<Hit<Map, Void>> hits = result.getHits(Map.class);
		return getDocumentsFromHitsWithScore(hits);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Map> getDocumentsFromHitsWithScore(List<Hit<Map, Void>> hits) {
		List<Map> documents = new ArrayList<Map>();
		for (Hit hit : hits) {
			Map<String, Object> hitDocument = (Map) hit.source;
			hitDocument.put("score", hit.score);
			documents.add(hitDocument);
		}
		return documents;
	}
	
	@SuppressWarnings({ "rawtypes" })
	public List<Map> textSearchReturningId(Map<String, Object> matchCriterias, String IndexName,
			String IndexType) throws IOException {
		SearchResult result = search(matchCriterias, null, IndexName, IndexType, null, false);
		return getDocumentsFromSearchResultWithId(result);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map> getDocumentsFromSearchResultWithId(SearchResult result) {
		List<Hit<Map, Void>> hits = result.getHits(Map.class);
		return getDocumentsFromHitsWithId(hits);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Map> getDocumentsFromHitsWithId(List<Hit<Map, Void>> hits) {
		List<Map> documents = new ArrayList<Map>();
		for (Hit hit : hits) {
			Map<String, Object> hitDocument = (Map) hit.source;
			hitDocument.put("id", hit.index);
			documents.add(hitDocument);
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
		Search search = new Search.Builder(query).addIndex(IndexName).setParameter("size", resultLimit).build();
		long startTime = System.currentTimeMillis();
		SearchResult result = client.execute(search);
		if (result.getErrorMessage() != null) {
			throw new IOException(result.getErrorMessage());
		}
		long endTime = System.currentTimeMillis();
		long diff = endTime - startTime;
		return result;
	}

	public CountResult count(String IndexName, String query) throws IOException {
		Count count = new Count.Builder().query(query).build();
		CountResult result = client.execute(count);
		if (result.getErrorMessage() != null) {
			throw new IOException(result.getErrorMessage());
		}
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

		JSONBuilder builder = new JSONStringer();
		builder.object().key("query").object().key("wildcard").object().key(textKeyWord).value(wordWildCard).endObject()
				.endObject().endObject();

		return builder.toString();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getCountFromAggregation(LinkedTreeMap<String, Object> aggregations,
			List<Map<String, Object>> groupByList, IESResultTransformer transformer) {

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
					
					parentCountMap.put(aggKeyListMap.get("key").toString(), parentCountObject);
					parentGroupList.add(parentCountMap);
				}
				countMap.put(groupByParent, parentCountMap);
			}
		}
		return transformer.getTransformedObject(countMap);
	}
}
