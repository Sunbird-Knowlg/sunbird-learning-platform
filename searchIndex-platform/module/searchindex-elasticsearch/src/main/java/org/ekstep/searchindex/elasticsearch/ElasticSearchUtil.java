/**
 * 
 */
package org.ekstep.searchindex.elasticsearch;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.ekstep.common.Platform;
import org.ekstep.searchindex.transformer.IESResultTransformer;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author pradyumna
 *
 */
public class ElasticSearchUtil {

	static {
		System.setProperty("es.set.netty.runtime.available.processors", "false");
		registerShutdownHook();
	}

	private static Map<String, TransportClient> esClient = new HashMap<String, TransportClient>();

	public static int defaultResultLimit = 10000;
	private static final int resultLimit = 100;
	public int defaultResultOffset = 0;
	private static int BATCH_SIZE = (Platform.config.hasPath("search.batch.size"))
			? Platform.config.getInt("search.batch.size")
			: 1000;
	private static ObjectMapper mapper = new ObjectMapper();

	public static void initialiseESClient(String indexName, String connectionInfo) {
		if (StringUtils.isBlank(indexName))
			indexName = CompositeSearchConstants.COMPOSITE_SEARCH_INDEX;
		createClient(indexName, connectionInfo);
	}

	/**
	 * 
	 */
	private static void createClient(String indexName, String connectionInfo) {
		if (!esClient.containsKey(indexName)) {
			try {
				Map<String, Integer> hostPort = new HashMap<String, Integer>();
				for (String info : connectionInfo.split(",")) {
					hostPort.put(info.split(":")[0], Integer.valueOf(info.split(":")[1]));
				}
				Settings settings = Settings.builder().put("client.transport.sniff", true)
						.put("client.transport.ignore_cluster_name", true).build();
				TransportClient client = new PreBuiltTransportClient(settings);
				for (String host : hostPort.keySet()) {
					client.addTransportAddress(new TransportAddress(InetAddress.getByName(host), hostPort.get(host)));
				}
				if (null != client)
					esClient.put(indexName, client);
			} catch (UnknownHostException e) {
				TelemetryManager.error("Error while creating elasticsearch client ", e);
			}
		}
	}

	private static TransportClient getClient(String indexName) {
		if (StringUtils.isBlank(indexName))
			indexName = CompositeSearchConstants.COMPOSITE_SEARCH_INDEX;
		return esClient.get(indexName);
	}

	public void finalize() {
		cleanESClient();
	}

	public static List<String> getQuerySearchFields() {
		List<String> querySearchFields = Platform.config.getStringList("search.fields.query");
		return querySearchFields;
	}

	public List<String> getDateFields() {
		List<String> dateFields = Platform.config.getStringList("search.fields.date");
		return dateFields;
	}

	public String getTimeZone() {
		String timeZoneProperty = Platform.config.getString("time-zone");
		if (timeZoneProperty == null) {
			timeZoneProperty = "0000";
		}
		return timeZoneProperty;
	}

	public static boolean isIndexExists(String indexName) {
		ActionFuture<IndicesExistsResponse> exists = getClient(indexName).admin().indices()
				.exists(new IndicesExistsRequest(indexName));
		IndicesExistsResponse actionGet = exists.actionGet();
		return actionGet.isExists();
	}

	public static boolean addIndex(String indexName, String documentType, String settings, String mappings)
			throws IOException {
		boolean response = false;
		CreateIndexResponse createIndexResponse = null;
		TransportClient client = getClient(indexName);
		if (!isIndexExists(indexName)) {
			CreateIndexRequestBuilder createIndexBuilder = client.admin().indices().prepareCreate(indexName);
			if (StringUtils.isNotBlank(settings)) {
				createIndexResponse = createIndexBuilder.setSettings(settings, XContentType.JSON).get();
				response = true;
			} else {
				createIndexResponse = createIndexBuilder.get();
				response = true;
			}
			if (null != createIndexResponse && createIndexResponse.isAcknowledged()) {
				if (StringUtils.isNotBlank(documentType) && StringUtils.isNotBlank(mappings)) {
					PutMappingResponse mappingResponse = client.admin().indices().preparePutMapping(indexName)
							.setType(documentType).setSource(mappings, XContentType.JSON).get();
					if (mappingResponse.isAcknowledged()) {
						response = true;
					} else {
						response = false;
					}
				} else {
					response = false;
				}
			} else {
				response = false;
			}
		}
		return response;
	}

	public static void addDocumentWithId(String indexName, String documentType, String documentId, String document) {
		try {
			Map<String, Object> doc = mapper.readValue(document, new TypeReference<Map<String, Object>>() {
			});
			IndexResponse response = getClient(indexName).prepareIndex(indexName, documentType, documentId)
					.setSource(doc)
					.get();
			TelemetryManager.log("Added " + response.getId() + " to index " + response.getIndex());
		} catch (IOException e) {
			TelemetryManager.error("Error while adding document to index :" + indexName, e);
		}
	}

	public static void addDocument(String indexName, String documentType, String document) {
		try {
			Map<String, Object> doc = mapper.readValue(document, new TypeReference<Map<String, Object>>() {
			});
			IndexResponse response = getClient(indexName).prepareIndex(indexName, documentType).setSource(doc).get();
			TelemetryManager.log("Added " + response.getId() + " to index " + response.getIndex());
		} catch (IOException e) {
			TelemetryManager.error("Error while adding document to index :" + indexName, e);
		}
	}

	public static void updateDocument(String indexName, String documentType, String document, String documentId)
			throws InterruptedException, ExecutionException {
		try {
			Map<String, Object> doc = mapper.readValue(document, new TypeReference<Map<String, Object>>() {
			});
			IndexRequest indexRequest = new IndexRequest(indexName, documentType, documentId).source(doc);
			UpdateRequest request = new UpdateRequest().index(indexName).type(documentType).id(documentId).doc(doc)
					.upsert(indexRequest);
			UpdateResponse response = getClient(indexName).update(request).get();
			TelemetryManager.log("Updated " + response.getId() + " to index " + response.getIndex());
		} catch (IOException e) {
			TelemetryManager.error("Error while updating document to index :" + indexName, e);
		}

	}

	public static void deleteDocument(String indexName, String documentType, String documentId)
			throws IOException {
		DeleteResponse response = getClient(indexName).prepareDelete(indexName, documentType, documentId).get();
		TelemetryManager.log("Deleted " + response.getId() + " to index " + response.getIndex());
	}

	public static void deleteDocumentsByQuery(QueryBuilder query, String indexName, String indexType)
			throws IOException {
		BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(getClient(indexName))
				.source(indexName)
				.filter(query).get();
		TelemetryManager.log("Deleted Documents by Query" + response.getDeleted());
	}

	public static void deleteIndex(String indexName) throws InterruptedException, ExecutionException {
		DeleteIndexResponse response = getClient(indexName).admin().indices().delete(new DeleteIndexRequest(indexName))
				.get();
		esClient.remove(indexName);
		TelemetryManager.log("Deleted Index" + indexName + " : " + response.isAcknowledged());
	}

	public static String getDocumentAsStringById(String indexName, String documentType, String documentId) {
		GetResponse response = getClient(indexName).prepareGet(indexName, documentType, documentId).get();
		return response.getSourceAsString();
	}

	public static List<String> getMultiDocumentAsStringByIdList(String indexName, String documentType,
			List<String> documentIdList) throws IOException {
		List<String> finalResult = new ArrayList<String>();
		MultiGetResponse multiGetItemResponses = getClient(indexName).prepareMultiGet()
				.add(indexName, documentType, documentIdList)
				.get();
		for (MultiGetItemResponse itemResponse : multiGetItemResponses) {
			GetResponse response = itemResponse.getResponse();
			if (response.isExists()) {
				finalResult.add(response.getSourceAsString());
			}
		}
		return finalResult;

	}

	@SuppressWarnings("unchecked")
	public static void bulkIndexWithIndexId(String indexName, String documentType, Map<String, Object> jsonObjects)
			throws Exception {
		if (isIndexExists(indexName)) {
			TransportClient client = getClient(indexName);
			if (!jsonObjects.isEmpty()) {
				int count = 0;
				BulkRequestBuilder bulkRequest = client.prepareBulk();
				for (String key : jsonObjects.keySet()) {
					count++;
					bulkRequest.add(client.prepareIndex(indexName, documentType).setId(key)
							.setSource((Map<String, Object>) jsonObjects.get(key)));
					if (count % BATCH_SIZE == 0 || (count % BATCH_SIZE < BATCH_SIZE && count == jsonObjects.size())) {
						BulkResponse bulkResponse = bulkRequest.get();
						if (bulkResponse.hasFailures()) {
							TelemetryManager
									.log("Failures in Elasticsearch bulkIndex : " + bulkResponse.buildFailureMessage());
						}
					}
				}
			}
		} else {
			throw new Exception("Index does not exist");
		}
	}

	public static void bulkIndexWithAutoGenerateIndexId(String indexName, String documentType,
			List<Map<String, Object>> jsonObjects)
			throws Exception {
		if (isIndexExists(indexName)) {
			if (!jsonObjects.isEmpty()) {
				TransportClient client = getClient(indexName);
				BulkRequestBuilder bulkRequest = client.prepareBulk();
				for (Map<String, Object> json : jsonObjects) {
					bulkRequest.add(client.prepareIndex(indexName, documentType).setSource(json));
				}
				BulkResponse bulkResponse = bulkRequest.get();
				if (bulkResponse.hasFailures()) {
					TelemetryManager.log("Failures in Elasticsearch bulkIndex : " + bulkResponse.buildFailureMessage());
				}
			}
		} else {
			throw new Exception("Index does not exist");
		}
	}

	@SuppressWarnings("rawtypes")
	public static List<Object> textSearch(Class objectClass, Map<String, Object> matchCriterias, String indexName,
			String indexType, int limit) throws Exception {
		SearchResponse result = search(matchCriterias, null, indexName, indexType, null, false, limit);
		return getDocumentsFromSearchResult(result, objectClass);
	}

	@SuppressWarnings("rawtypes")
	public static List<Object> getDocumentsFromSearchResult(SearchResponse result, Class objectClass) {
		SearchHits hits = result.getHits();
		return getDocumentsFromHits(hits);
	}

	public static List<Object> getDocumentsFromHits(SearchHits hits) {
		List<Object> documents = new ArrayList<Object>();
		for (SearchHit hit : hits) {
			documents.add(hit.getSourceAsMap());
		}
		return documents;
	}

	@SuppressWarnings("rawtypes")
	public static List<Map> getDocumentsFromSearchResultWithScore(SearchResponse result) {
		SearchHits hits = result.getHits();
		return getDocumentsFromHitsWithScore(hits);
	}

	@SuppressWarnings("rawtypes")
	public static List<Map> getDocumentsFromHitsWithScore(SearchHits hits) {
		List<Map> documents = new ArrayList<Map>();
		for (SearchHit hit : hits) {
			Map<String, Object> hitDocument = hit.getSourceAsMap();
			hitDocument.put("score", hit.getScore());
			documents.add(hitDocument);
		}
		return documents;
	}

	@SuppressWarnings({ "rawtypes" })
	public static List<Map> textSearchReturningId(Map<String, Object> matchCriterias, String indexName,
			String indexType)
			throws Exception {
		SearchResponse result = search(matchCriterias, null, indexName, indexType, null, false, 100);
		return getDocumentsFromSearchResultWithId(result);
	}

	@SuppressWarnings({ "rawtypes" })
	public static List<Map> getDocumentsFromSearchResultWithId(SearchResponse result) {
		SearchHits hits = result.getHits();
		return getDocumentsFromHitsWithId(hits);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Map> getDocumentsFromHitsWithId(SearchHits hits) {
		List<Map> documents = new ArrayList<Map>();
		for (SearchHit hit : hits) {
			Map<String, Object> hitDocument = (Map) hit.getSourceAsMap();
			hitDocument.put("id", hit.getId());
			documents.add(hitDocument);
		}
		return documents;
	}

	@SuppressWarnings({ "rawtypes" })
	public static List<Object> wildCardSearch(Class objectClass, String textKeyWord, String wordWildCard,
			String indexName, String indexType, int limit) throws Exception {
		SearchResponse result = wildCardSearch(textKeyWord, wordWildCard, indexName, indexType, limit);
		return getDocumentsFromSearchResult(result, objectClass);
	}

	public static SearchResponse wildCardSearch(String textKeyWord, String wordWildCard, String indexName,
			String indexType, int limit)
			throws Exception {
		SearchRequestBuilder query = buildJsonForWildCardQuery(textKeyWord, wordWildCard, indexName);
		query.setSize(limit);
		return search(indexName, indexType, query);
	}

	@SuppressWarnings({ "rawtypes" })
	public static List<Object> textFiltersSearch(Class objectClass, Map<String, Object> searchCriteria,
			Map<String, Object> textFiltersMap, String indexName, String indexType, int limit)
			throws Exception {
		SearchResponse result = search(searchCriteria, textFiltersMap, indexName, indexType, null, false, limit);
		return getDocumentsFromSearchResult(result, objectClass);
	}

	@SuppressWarnings("rawtypes")
	public static Map<String, Object> textFiltersGroupBySearch(Class objectClass, Map<String, Object> searchCriteria,
			Map<String, Object> textFiltersMap, List<Map<String, Object>> groupByList, String indexName,
			String indexType) throws Exception {
		SearchResponse result = search(searchCriteria, textFiltersMap, indexName, indexType, groupByList, false,
				resultLimit);
		List<Object> documents = getDocumentsFromSearchResult(result, objectClass);
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("objects", documents);

		if (result.getAggregations() != null) {
			Aggregations aggregations = result.getAggregations();
			response.put("aggregations", getCountFromAggregation(aggregations, groupByList));
		}
		return response;
	}

	@SuppressWarnings("rawtypes")
	public static List<Object> textSearch(Class objectClass, Map<String, Object> matchCriterias,
			Map<String, Object> textFiltersMap, String indexName, String indexType) throws Exception {
		SearchResponse result = search(matchCriterias, textFiltersMap, indexName, indexType, null, false, resultLimit);
		return getDocumentsFromSearchResult(result, objectClass);
	}

	@SuppressWarnings("rawtypes")
	public static List<Object> textSearch(Class objectClass, Map<String, Object> matchCriterias,
			Map<String, Object> textFiltersMap, String indexName, String indexType,
			List<Map<String, Object>> groupByList, int limit) throws Exception {
		SearchResponse result = search(matchCriterias, textFiltersMap, indexName, indexType, groupByList, false,
				limit);
		return getDocumentsFromSearchResult(result, objectClass);
	}

	public static SearchResponse search(Map<String, Object> matchCriterias, Map<String, Object> textFiltersMap,
			String indexName, String indexType, List<Map<String, Object>> groupBy, boolean isDistinct, int limit)
			throws Exception {
		SearchRequestBuilder query = buildJsonForQuery(matchCriterias, textFiltersMap, groupBy, isDistinct, indexName);
		query.setSize(limit);
		return search(indexName, indexType, query);
	}

	public static SearchResponse search(String indexName, String indexType, SearchRequestBuilder searchRequestBuilder)
			throws Exception {
		SearchResponse response = searchRequestBuilder.setIndices(indexName).execute().actionGet();
		return response;
	}

	public static SearchResponse search(String indexName, SearchRequestBuilder searchRequestBuilder)
			throws IOException {
		TelemetryManager.log("searching in ES index: " + indexName);

		searchRequestBuilder.setIndices(indexName);
		SearchResponse response = null;
		response = searchRequestBuilder.execute().actionGet();
		return response;
	}

	public static int count(String indexName, SearchRequestBuilder searchRequestBuilder) throws IOException {
		SearchResponse response = searchRequestBuilder.setIndices(indexName).execute()
				.actionGet();
		return (int) response.getHits().getTotalHits();

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<String, Object> getCountFromAggregation(Aggregations aggregations,
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

	@SuppressWarnings("rawtypes")
	public static Map<String, Object> getCountOfSearch(Class objectClass, Map<String, Object> matchCriterias,
			String indexName, String indexType, List<Map<String, Object>> groupByList, int limit)
			throws Exception {
		SearchResponse result = search(matchCriterias, null, indexName, indexType, groupByList, false, limit);
		Aggregations aggregations = result.getAggregations();
		return getCountFromAggregation(aggregations, groupByList);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<String, Object> getDistinctCountOfSearch(Map<String, Object> matchCriterias, String IndexName,
			String IndexType, List<Map<String, Object>> groupByList) throws Exception {
		Map<String, Object> countMap = new HashMap<String, Object>();
		SearchResponse result = search(matchCriterias, null, IndexName, IndexType, groupByList, true, 0);
		Aggregations aggregations = result.getAggregations();
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
	public static SearchRequestBuilder buildJsonForQuery(Map<String, Object> matchCriterias,
			Map<String, Object> textFiltersMap, List<Map<String, Object>> groupByList, boolean isDistinct,
			String indexName)
			throws JsonGenerationException, JsonMappingException, IOException {

		SearchRequestBuilder searchRequestBuilder = getSearchRequestBuilder(indexName);

		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		if (matchCriterias != null) {

			for (Map.Entry<String, Object> entry : matchCriterias.entrySet()) {
				if (entry.getValue() instanceof List) {
					for (String matchText : (ArrayList<String>) entry.getValue()) {
						queryBuilder.should(QueryBuilders.matchQuery(entry.getKey(), matchText));
					}
				}
			}
		}

		if (textFiltersMap != null && !textFiltersMap.isEmpty()) {
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			for (Map.Entry<String, Object> entry : textFiltersMap.entrySet()) {
				ArrayList<String> termValues = (ArrayList<String>) entry.getValue();
				for (String termValue : termValues) {
					boolQuery.must(QueryBuilders.termQuery(entry.getKey(), termValue));
				}
			}
			queryBuilder.filter(boolQuery);
		}

		searchRequestBuilder.setQuery(QueryBuilders.boolQuery().filter(queryBuilder));

		if (groupByList != null && !groupByList.isEmpty()) {
			if (!isDistinct) {
				for (Map<String, Object> groupByMap : groupByList) {
					String groupByParent = (String) groupByMap.get("groupByParent");
					List<String> groupByChildList = (List<String>) groupByMap.get("groupByChildList");
					TermsAggregationBuilder termBuilder = AggregationBuilders.terms(groupByParent).field(groupByParent);
					if (groupByChildList != null && !groupByChildList.isEmpty()) {
						for (String childGroupBy : groupByChildList) {
							termBuilder.subAggregation(AggregationBuilders.terms(childGroupBy).field(childGroupBy));
						}

					}
					searchRequestBuilder.addAggregation(termBuilder);
				}
			} else {
				for (Map<String, Object> groupByMap : groupByList) {
					String groupBy = (String) groupByMap.get("groupBy");
					String distinctKey = (String) groupByMap.get("distinctKey");
					searchRequestBuilder.addAggregation(
							AggregationBuilders.terms(groupBy).field(groupBy).subAggregation(AggregationBuilders
									.cardinality("distinct_" + distinctKey + "s").field(distinctKey)));
				}
			}
		}

		return searchRequestBuilder;
	}

	private static SearchRequestBuilder buildJsonForWildCardQuery(String textKeyWord, String wordWildCard,
			String indexName) {
		return getSearchRequestBuilder(indexName).setQuery(QueryBuilders.wildcardQuery(textKeyWord, wordWildCard));

	}

	@SuppressWarnings("unchecked")
	public static Object getCountFromAggregation(Aggregations aggregations, List<Map<String, Object>> groupByList,
			IESResultTransformer transformer) {

		Map<String, Object> countMap = new HashMap<String, Object>();
		if (aggregations != null) {
			for (Map<String, Object> aggregationsMap : groupByList) {
				Map<String, Object> parentCountMap = new HashMap<String, Object>();
				String groupByParent = (String) aggregationsMap.get("groupByParent");
				Terms terms = aggregations.get(groupByParent);
				List<Map<String, Object>> parentGroupList = new ArrayList<Map<String, Object>>();
				List<Bucket> buckets = (List<Bucket>) terms.getBuckets();
				for (Bucket bucket : buckets) {
					Map<String, Object> parentCountObject = new HashMap<String, Object>();
					parentCountObject.put("count", bucket.getDocCount());
					List<String> groupByChildList = (List<String>) aggregationsMap.get("groupByChildList");
					Aggregations subAggregations = bucket.getAggregations();
					if (null != groupByChildList && !groupByChildList.isEmpty() && null != subAggregations) {
						Map<String, Object> groupByChildMap = new HashMap<String, Object>();
						for (String groupByChild : groupByChildList) {
							Terms subTerms = subAggregations.get(groupByChild);
							List<Bucket> childBuckets = (List<Bucket>) subTerms.getBuckets();
							Map<String, Long> childCountMap = new HashMap<String, Long>();
							for (Bucket childBucket : childBuckets) {
								childCountMap.put(childBucket.getKeyAsString(), childBucket.getDocCount());
								groupByChildMap.put(groupByChild, childCountMap);
							}
						}
						parentCountObject.putAll(groupByChildMap);
					}
					parentCountMap.put(bucket.getKeyAsString(), parentCountObject);
					parentGroupList.add(parentCountMap);
				}

				countMap.put(groupByParent, parentCountMap);
			}
		}
		return transformer.getTransformedObject(countMap);
	}

	/**
	 * @return
	 */
	public static SearchRequestBuilder getSearchRequestBuilder(String indexName) {
		return new SearchRequestBuilder(getClient(indexName), SearchAction.INSTANCE);
	}

	private static void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					cleanESClient();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void cleanESClient() {
		if (!esClient.isEmpty())
			for (TransportClient client : esClient.values()) {
				if (null != client)
					client.close();
			}
	}

}
