/**
 * 
 */
package org.ekstep.mvcsearchindex.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ServerException;
import org.ekstep.mvcsearchindex.transformer.IESResultTransformer;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
// import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.dispatch.Futures;
import scala.concurrent.Future;
import scala.concurrent.Promise;

/**
 * @author pradyumna
 *
 */
public class ElasticSearchUtil {

	static {
		System.setProperty("es.set.netty.runtime.available.processors", "false");
		registerShutdownHook();
	}

	private static Map<String, RestHighLevelClient> esClient = new HashMap<String, RestHighLevelClient>();

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
			Map<String, Integer> hostPort = new HashMap<String, Integer>();
			for (String info : connectionInfo.split(",")) {
				hostPort.put(info.split(":")[0], Integer.valueOf(info.split(":")[1]));
			}
			List<HttpHost> httpHosts = new ArrayList<>();
			for (String host : hostPort.keySet()) {
				httpHosts.add(new HttpHost(host, hostPort.get(host)));
			}
            RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[httpHosts.size()]))
                    .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                        @Override
                        public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                            return requestConfigBuilder.setConnectionRequestTimeout(-1);
                        }
                    });
            RestHighLevelClient client = new RestHighLevelClient(builder);
			if (null != client)
				esClient.put(indexName, client);
		}
	}

	private static RestHighLevelClient getClient(String indexName) {
		if (StringUtils.isBlank(indexName))
			indexName = CompositeSearchConstants.COMPOSITE_SEARCH_INDEX;
		if (StringUtils.startsWith(indexName,"kp_audit_log"))
			return esClient.get("kp_audit_log");
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
		Response response;
		try {
			Request request = new Request("HEAD", "/" + indexName);
			response = getClient(indexName).getLowLevelClient().performRequest(request);
			return (200 == response.getStatusLine().getStatusCode());
		} catch (IOException e) {
			return false;
		}

	}

	public static boolean addIndex(String indexName, String documentType, String settings, String mappings)
			throws IOException {
		boolean response = false;
		RestHighLevelClient client = getClient(indexName);
		if (!isIndexExists(indexName)) {
			CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
			if (StringUtils.isNotBlank(settings))
				createRequest.settings(Settings.builder().loadFromSource(settings, XContentType.JSON));
			if (StringUtils.isNotBlank(documentType) && StringUtils.isNotBlank(mappings))
				createRequest.mapping( mappings, XContentType.JSON);
			CreateIndexResponse createIndexResponse = client.indices().create(createRequest,RequestOptions.DEFAULT);

			response = createIndexResponse.isAcknowledged();
		}
		return response;
	}
	public static boolean addIndex(String indexName, String documentType, String settings, String mappings,String alias)
			throws IOException {
		boolean response = false;
		RestHighLevelClient client = getClient(indexName);
		if (!isIndexExists(indexName)) {
			CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
			if(StringUtils.isNotBlank(alias))
				createRequest.alias(new Alias(alias));
			if (StringUtils.isNotBlank(settings))
				createRequest.settings(Settings.builder().loadFromSource(settings, XContentType.JSON));
			if (StringUtils.isNotBlank(documentType) && StringUtils.isNotBlank(mappings))
				createRequest.mapping( mappings, XContentType.JSON);
			CreateIndexResponse createIndexResponse = client.indices().create(createRequest,RequestOptions.DEFAULT);

			response = createIndexResponse.isAcknowledged();
		}
		return response;
	}

	public static void addDocumentWithId(String indexName, String documentId, String document) {
		try {
			/*Map<String, Object> doc = mapper.readValue(document, new TypeReference<Map<String, Object>>() {
			});*/
			IndexRequest indexRequest = new IndexRequest(indexName);
			indexRequest.id(documentId);
			indexRequest.source(document,XContentType.JSON);
			IndexResponse indexResponse = getClient(indexName).index(indexRequest,RequestOptions.DEFAULT);
/*			IndexResponse response = getClient(indexName)
					.index(new IndexRequest(indexName, "_doc", documentId).source(doc),RequestOptions.DEFAULT); */
			TelemetryManager.log("Added " + indexResponse.getId() + " to index " + indexResponse.getIndex());
		} catch (IOException e) {
			TelemetryManager.error("Error while adding document to index :" + indexName, e);
		}
	}
	public static void updateDocument(String indexName, String documentId, String document)
			throws InterruptedException, ExecutionException {
		try {Map<String, Object> doc = mapper.readValue(document, new TypeReference<Map<String, Object>>() {
		});

			UpdateRequest updateRequest = new UpdateRequest();
			updateRequest.index(indexName);
			updateRequest.id(documentId);
			updateRequest.doc(doc);
			UpdateResponse response = getClient(indexName).update(updateRequest,RequestOptions.DEFAULT);
			TelemetryManager.log("Updated " + response.getId() + " to index " + response.getIndex());
		} catch (IOException e) {
			TelemetryManager.error("Error while updating document to index :" + indexName, e);
		}

	}
	public static void addDocumentWithId(String indexName, String documentType, String documentId, String document) {
		try {
			Map<String, Object> doc = mapper.readValue(document, new TypeReference<Map<String, Object>>() {
			});
			IndexResponse response = getClient(indexName)
					.index(new IndexRequest(indexName, documentType, documentId).source(doc),RequestOptions.DEFAULT);
			TelemetryManager.log("Added " + response.getId() + " to index " + response.getIndex());
		} catch (IOException e) {
			TelemetryManager.error("Error while adding document to index :" + indexName, e);
		}
	}
	public static void addDocument(String indexName, String documentType, String document) {
		try {
			Map<String, Object> doc = mapper.readValue(document, new TypeReference<Map<String, Object>>() {});

			IndexResponse response = getClient(indexName).index(new IndexRequest(indexName, documentType).source(doc),RequestOptions.DEFAULT);
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
			UpdateResponse response = getClient(indexName).update(request,RequestOptions.DEFAULT);
			TelemetryManager.log("Updated " + response.getId() + " to index " + response.getIndex());
		} catch (IOException e) {
			TelemetryManager.error("Error while updating document to index :" + indexName, e);
		}

	}

	public static void deleteDocument(String indexName, String documentType, String documentId)
			throws IOException {
		DeleteResponse response = getClient(indexName).delete(new DeleteRequest(indexName, documentType, documentId),RequestOptions.DEFAULT);
		TelemetryManager.log("Deleted " + response.getId() + " to index " + response.getIndex());
	}

	public static void deleteDocumentsByQuery(QueryBuilder query, String indexName, String indexType)
			throws IOException {
		Request request = new Request("POST",
				indexName + "/_delete_by_query" + query);
		Response response = getClient(indexName).getLowLevelClient().performRequest(request);

		TelemetryManager.log("Deleted Documents by Query" + EntityUtils.toString(response.getEntity()));
	}

	public static void deleteIndex(String indexName) throws InterruptedException, ExecutionException, IOException {
		AcknowledgedResponse response = getClient(indexName).indices().delete(new DeleteIndexRequest(indexName),RequestOptions.DEFAULT);
		esClient.remove(indexName);
		TelemetryManager.log("Deleted Index" + indexName + " : " + response.isAcknowledged());
	}

	public static String getDocumentAsStringById(String indexName, String documentType, String documentId)
			throws IOException {
		GetResponse response = getClient(indexName).get(new GetRequest(indexName, documentType, documentId),RequestOptions.DEFAULT);
		return response.getSourceAsString();
	}

	public static List<String> getMultiDocumentAsStringByIdList(String indexName, String documentType,
			List<String> documentIdList) throws IOException {
		List<String> finalResult = new ArrayList<String>();
		MultiGetRequest request = new MultiGetRequest();
		documentIdList.forEach(docId -> request.add(indexName, documentType, docId));
		MultiGetResponse multiGetItemResponses = getClient(indexName).multiGet(request,RequestOptions.DEFAULT);
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
			RestHighLevelClient client = getClient(indexName);
			if (!jsonObjects.isEmpty()) {
				int count = 0;
				BulkRequest request = new BulkRequest();
				for (String key : jsonObjects.keySet()) {
					count++;
					request.add(new IndexRequest(indexName, documentType, key)
							.source((Map<String, Object>) jsonObjects.get(key)));
					if (count % BATCH_SIZE == 0 || (count % BATCH_SIZE < BATCH_SIZE && count == jsonObjects.size())) {
						BulkResponse bulkResponse = client.bulk(request,RequestOptions.DEFAULT);
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
			RestHighLevelClient client = getClient(indexName);
			if (!jsonObjects.isEmpty()) {
				int count = 0;
				BulkRequest request = new BulkRequest();
				for (Map<String, Object> json : jsonObjects) {
					count++;
					request.add(new IndexRequest(indexName, documentType).source(json));
					if (count % BATCH_SIZE == 0 || (count % BATCH_SIZE < BATCH_SIZE && count == jsonObjects.size())) {
						BulkResponse bulkResponse = client.bulk(request,RequestOptions.DEFAULT);
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
		SearchSourceBuilder query = buildJsonForWildCardQuery(textKeyWord, wordWildCard, indexName);
		query.size(limit);
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
		SearchSourceBuilder query = buildJsonForQuery(matchCriterias, textFiltersMap, groupBy, isDistinct, indexName);
		query.size(limit);
		return search(indexName, indexType, query);
	}

	public static SearchResponse search(String indexName, String indexType, SearchSourceBuilder query)
			throws Exception {
		return getClient(indexName).search(new SearchRequest(indexName).source(query),RequestOptions.DEFAULT);
	}

	public static Future<SearchResponse> search(String indexName, SearchSourceBuilder searchSourceBuilder)
			throws IOException {
		TelemetryManager.log("searching in ES index: " + indexName);
		Promise<SearchResponse> promise = Futures.promise();
		getClient(indexName).searchAsync(new SearchRequest().indices(indexName).source(searchSourceBuilder),RequestOptions.DEFAULT,
				new ActionListener<SearchResponse>() {

					@Override
					public void onResponse(SearchResponse response) {
						promise.success(response);
					}

					@Override
					public void onFailure(Exception e) {
						promise.failure(e);
					}
				});
		return promise.future();
	}

	public static int count(String indexName, SearchSourceBuilder searchSourceBuilder) throws IOException {
		SearchResponse response = getClient(indexName)
				.search(new SearchRequest().indices(indexName).source(searchSourceBuilder),RequestOptions.DEFAULT);
		return (int) response.getHits().getTotalHits().value;

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
	public static SearchSourceBuilder buildJsonForQuery(Map<String, Object> matchCriterias,
			Map<String, Object> textFiltersMap, List<Map<String, Object>> groupByList, boolean isDistinct,
			String indexName)
			throws JsonGenerationException, JsonMappingException, IOException {

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

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

		searchSourceBuilder.query(QueryBuilders.boolQuery().filter(queryBuilder));

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
					searchSourceBuilder.aggregation(termBuilder);
				}
			} else {
				for (Map<String, Object> groupByMap : groupByList) {
					String groupBy = (String) groupByMap.get("groupBy");
					String distinctKey = (String) groupByMap.get("distinctKey");
					searchSourceBuilder.aggregation(
							AggregationBuilders.terms(groupBy).field(groupBy).subAggregation(AggregationBuilders
									.cardinality("distinct_" + distinctKey + "s").field(distinctKey)));
				}
			}
		}

		return searchSourceBuilder;
	}

	private static SearchSourceBuilder buildJsonForWildCardQuery(String textKeyWord, String wordWildCard,
			String indexName) {
		return new SearchSourceBuilder().query(QueryBuilders.wildcardQuery(textKeyWord, wordWildCard));

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
			for (RestHighLevelClient client : esClient.values()) {
				if (null != client)
					try {
						client.close();
					} catch (IOException e) {
					}
			}
	}

	/**
	 * This method perform delete operation in bulk using document ids.
	 *
	 * @param indexName
	 * @param documentType
	 * @param identifiers
	 * @throws Exception
	 */
	public static void bulkDeleteDocumentById(String indexName, String documentType, List<String> identifiers) throws Exception {
		if (isIndexExists(indexName)) {
			if (null != identifiers && !identifiers.isEmpty()) {
				int count = 0;
				BulkRequest request = new BulkRequest();
				for (String documentId : identifiers) {
					count++;
					request.add(new DeleteRequest(indexName, documentType, documentId));
					if (count % BATCH_SIZE == 0 || (count % BATCH_SIZE < BATCH_SIZE && count == identifiers.size())) {
						BulkResponse bulkResponse = getClient(indexName).bulk(request,RequestOptions.DEFAULT);
						List<String> failedIds = Arrays.stream(bulkResponse.getItems()).filter(
								itemResp -> !StringUtils.equals(itemResp.getResponse().getResult().getLowercase(),"deleted")
						).map(r -> r.getResponse().getId()).collect(Collectors.toList());
						if (CollectionUtils.isNotEmpty(failedIds))
							TelemetryManager.log("Failed Id's While Deleting Elasticsearch Documents (Bulk Delete) : " + failedIds);
						if (bulkResponse.hasFailures()) {
							//TODO: Implement Retry Mechanism
							TelemetryManager
									.log("Error Occured While Deleting Elasticsearch Documents in Bulk : " + bulkResponse.buildFailureMessage());
						}
					}
				}
			}
		} else {
			throw new ServerException("ERR_BULK_DELETE_ES_DATA", "ES Index Not Found With Id : " + indexName);
		}
	}

}