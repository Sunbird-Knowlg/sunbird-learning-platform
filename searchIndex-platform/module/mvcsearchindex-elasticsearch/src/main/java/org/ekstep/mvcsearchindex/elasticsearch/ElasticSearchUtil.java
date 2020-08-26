/**
 *
 */
package org.ekstep.mvcsearchindex.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
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
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.dispatch.Futures;
import scala.concurrent.Future;
import scala.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pradyumna
 *
 */
public class ElasticSearchUtil {
	private static final Logger logger = LoggerFactory.getLogger(ElasticSearchUtil.class);
	static {
		System.setProperty("es.set.netty.runtime.available.processors", "false");
		registerShutdownHook();
	}

	private static Map<String, RestHighLevelClient> esClient = new HashMap<String, RestHighLevelClient>();

	private static ObjectMapper mapper = new ObjectMapper();

	public static void initialiseESClient(String indexName, String connectionInfo) {
		if (StringUtils.isBlank(indexName))
			indexName = CompositeSearchConstants.MVC_SEARCH_INDEX;
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
			indexName = CompositeSearchConstants.MVC_SEARCH_INDEX;
		if (StringUtils.startsWith(indexName,"kp_audit_log"))
			return esClient.get("kp_audit_log");
		return esClient.get(indexName);
	}

	public void finalize() {
		cleanESClient();
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


	public static boolean addIndex(String indexName, String documentType, String settings, String mappings,String alias , String esvalues)
			throws IOException {
		boolean response = false;
		RestHighLevelClient client = getClient(indexName);
		if (!isIndexExists(indexName)) {
			CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
			if(esvalues != null) {
				createRequest.source(esvalues,XContentType.JSON);
			}
			else {
				if (StringUtils.isNotBlank(alias))
					createRequest.alias(new Alias(alias));
				if (StringUtils.isNotBlank(settings))
					createRequest.settings(Settings.builder().loadFromSource(settings, XContentType.JSON));
				if (StringUtils.isNotBlank(documentType) && StringUtils.isNotBlank(mappings))
					createRequest.mapping(mappings, XContentType.JSON);
			}
			CreateIndexResponse createIndexResponse = client.indices().create(createRequest,RequestOptions.DEFAULT);

			response = createIndexResponse.isAcknowledged();
		}
		return response;
	}


	public static void addDocumentWithId(String indexName, String documentId, String document) {
		try {
			logger.info("Inside addDocuemntwithId");
			IndexRequest indexRequest = new IndexRequest(indexName);
			indexRequest.id(documentId);
			indexRequest.source(document,XContentType.JSON);
			IndexResponse indexResponse = getClient(indexName).index(indexRequest,RequestOptions.DEFAULT);
			logger.info("Response after inserting inside ES :: " + indexResponse.toString());
			TelemetryManager.log("Added " + indexResponse.getId() + " to index " + indexResponse.getIndex());
		} catch (IOException e) {
			logger.info("Error after inserting inside ES :: " + indexName + " " + e);
			TelemetryManager.error("Error while adding document to index :" + indexName, e);
		}
	}


	public static void updateDocument(String indexName, String documentId, String document)
			throws InterruptedException, ExecutionException {
		try {Map<String, Object> doc = mapper.readValue(document, new TypeReference<Map<String, Object>>() {
		});
             logger.info("Inside updateDocument");
			UpdateRequest updateRequest = new UpdateRequest();
			updateRequest.index(indexName);
			updateRequest.id(documentId);
			updateRequest.doc(doc);
			UpdateResponse response = getClient(indexName).update(updateRequest,RequestOptions.DEFAULT);
			TelemetryManager.log("Updated " + response.getId() + " to index " + response.getIndex());
			logger.info("Response after updating inside ES :: " + response.toString());
		} catch (IOException e) {
			logger.info("Error after updating inside ES :: " + indexName + " " + e);
			TelemetryManager.error("Error while updating document to index :" + indexName, e);
		}

	}





	public static void deleteIndex(String indexName) throws InterruptedException, ExecutionException, IOException {
		AcknowledgedResponse response = getClient(indexName).indices().delete(new DeleteIndexRequest(indexName),RequestOptions.DEFAULT);
		esClient.remove(indexName);
		TelemetryManager.log("Deleted Index" + indexName + " : " + response.isAcknowledged());
	}

	public static String getDocumentAsStringById(String indexName, String documentId)
			throws IOException {
		GetResponse response = getClient(indexName).get(new GetRequest(indexName, documentId),RequestOptions.DEFAULT);
		return response.getSourceAsString();
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


}