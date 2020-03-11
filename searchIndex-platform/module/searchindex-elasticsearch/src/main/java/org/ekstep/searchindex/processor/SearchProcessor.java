package org.ekstep.searchindex.processor;

import akka.dispatch.Mapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.Platform;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.transformer.AggregationsResultTransformer;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder.Type;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder.FilterFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.NestedSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchProcessor {

	private ObjectMapper mapper = new ObjectMapper();
	private static final String ASC_ORDER = "asc";
	private static final String AND = "AND";
	private boolean relevanceSort = false;

	public SearchProcessor() {
		ElasticSearchUtil.initialiseESClient(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				Platform.config.getString("search.es_conn_info"));
	}
	
	public SearchProcessor(String indexName) {
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Future<Map<String, Object>> processSearch(SearchDTO searchDTO, boolean includeResults)
			throws Exception {
		List<Map<String, Object>> groupByFinalList = new ArrayList<Map<String, Object>>();
		SearchSourceBuilder query = processSearchQuery(searchDTO, groupByFinalList, true);
		Future<SearchResponse> searchResponse = ElasticSearchUtil.search(
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				query);

		return searchResponse.map(new Mapper<SearchResponse, Map<String, Object>>() {
			public Map<String, Object> apply(SearchResponse searchResult) {
				Map<String, Object> resp = new HashMap<>();
				if (includeResults) {
					if (searchDTO.isFuzzySearch()) {
						List<Map> results = ElasticSearchUtil.getDocumentsFromSearchResultWithScore(searchResult);
						resp.put("results", results);
					} else {
						List<Object> results = ElasticSearchUtil.getDocumentsFromSearchResult(searchResult, Map.class);
						resp.put("results", results);
					}
				}
				Aggregations aggregations = searchResult.getAggregations();
				if (null != aggregations) {
					AggregationsResultTransformer transformer = new AggregationsResultTransformer();
					if(CollectionUtils.isNotEmpty(searchDTO.getFacets())) {
						resp.put("facets", (List<Map<String, Object>>) ElasticSearchUtil
								.getCountFromAggregation(aggregations, groupByFinalList, transformer));
					} else if(CollectionUtils.isNotEmpty(searchDTO.getAggregations())){
						resp.put("aggregations", aggregateResult(aggregations));
					}

				}
				resp.put("count", (int) searchResult.getHits().getTotalHits());
				return resp;
			}
		}, ExecutionContext.Implicits$.MODULE$.global());
	}

	public Map<String, Object> processCount(SearchDTO searchDTO) throws Exception {
		Map<String, Object> response = new HashMap<String, Object>();
		SearchSourceBuilder searchSourceBuilder = processSearchQuery(searchDTO, null, false);
		searchSourceBuilder.from(searchDTO.getOffset()).size(0);
		int countResult = ElasticSearchUtil.count(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				searchSourceBuilder);
		response.put("count", countResult);

		return response;
	}

	/**
	 * Returns the list of words which are synonyms of the synsetIds passed in the
	 * request
	 * 
	 * @param synsetIds
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public Map<String, Object> multiWordDocSearch(List<String> synsetIds) throws Exception {
		Map<String, Object> response = new HashMap<String, Object>();
		Map<String, Object> translations = new HashMap<String, Object>();
		Map<String, Object> synsets = new HashMap<String, Object>();
		if (synsetIds != null && synsetIds.size() > 0) {
			List<String> resultList = ElasticSearchUtil.getMultiDocumentAsStringByIdList(
					CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
					CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, synsetIds);
			for (String synsetDoc : resultList) {
				Map<String, Object> wordTranslationList = new HashMap<String, Object>();
				Map<String, Object> indexDocument = new HashMap<String, Object>();
				if (synsetDoc != null && !synsetDoc.isEmpty()) {
					indexDocument = mapper.readValue(synsetDoc, new TypeReference<Map<String, Object>>() {
					});
					Object words = indexDocument.get("synonyms");
					String identifier = (String) indexDocument.get("identifier");
					String gloss = (String) indexDocument.get("gloss");
					wordTranslationList.put("gloss", gloss);
					if (words != null) {
						List<String> wordIdList = (List<String>) words;
						if (wordIdList != null && wordIdList.size() > 0) {
							List<String> wordResultList = ElasticSearchUtil.getMultiDocumentAsStringByIdList(
									CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
									CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, wordIdList);
							for (String wordDoc : wordResultList) {
								List<Map> synsetWordLangList = new ArrayList<Map>();
								Map<String, Object> indexWordDocument = new HashMap<String, Object>();
								indexWordDocument = mapper.readValue(wordDoc, new TypeReference<Map<String, Object>>() {
								});
								String wordId = (String) indexWordDocument.get("identifier");
								String graphId = (String) indexWordDocument.get("graph_id");
								if (wordTranslationList.containsKey(graphId)) {
									synsetWordLangList = (List<Map>) wordTranslationList.get(graphId);
								}
								String lemma = (String) indexWordDocument.get("lemma");
								String status = (String) indexWordDocument.get("status");
								if (!StringUtils.equalsIgnoreCase(status, "Retired")) {
									Map<String, String> wordMap = new HashMap<String, String>();
									wordMap.put("id", wordId);
									wordMap.put("lemma", lemma);
									synsetWordLangList.add(wordMap);
								}
								wordTranslationList.put(graphId, synsetWordLangList);
							}

						}
					}
					synsets.put(identifier, wordTranslationList);
				}

			}
			response.put("translations", synsets);
		}

		return response;
	}

	/**
	 * Returns list of synsetsIds which has valid documents in composite index
	 * 
	 * @param synsetIds
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> multiSynsetDocSearch(List<String> synsetIds) throws Exception {
		Map<String, Object> synsetDocList = new HashMap<String, Object>();
		List<String> identifierList = new ArrayList<String>();
		if (synsetIds != null && synsetIds.size() > 0) {
			List<String> resultList = ElasticSearchUtil.getMultiDocumentAsStringByIdList(
					CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
					CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, synsetIds);
			for (String synsetDoc : resultList) {
				Map<String, Object> indexDocument = new HashMap<String, Object>();
				if (synsetDoc != null && !synsetDoc.isEmpty()) {
					indexDocument = mapper.readValue(synsetDoc, new TypeReference<Map<String, Object>>() {
					});
					String identifier = (String) indexDocument.get("identifier");
					identifierList.add(identifier);

				}

			}
		}
		synsetDocList.put("synsets", identifierList);

		return synsetDocList;
	}

	public void destroy() {
		// ElasticSearchUtil.cleanESClient();
	}

	/**
	 * @param searchDTO
	 * @param groupByFinalList
	 * @return
	 */
	private SearchSourceBuilder processSearchQuery(SearchDTO searchDTO, List<Map<String, Object>> groupByFinalList,
			boolean sortBy) {

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		List<String> fields = searchDTO.getFields();
		if (null != fields && !fields.isEmpty()) {
			fields.add("objectType");
			fields.add("identifier");
			searchSourceBuilder.fetchSource(fields.toArray(new String[fields.size()]), null);
		}

		if (searchDTO.getFacets() != null && groupByFinalList != null) {
			for (String facet : searchDTO.getFacets()) {
				Map<String, Object> groupByMap = new HashMap<String, Object>();
				groupByMap.put("groupByParent", facet);
				groupByFinalList.add(groupByMap);
			}
		}

		searchSourceBuilder.size(searchDTO.getLimit());
		searchSourceBuilder.from(searchDTO.getOffset());
		QueryBuilder query = getSearchQuery(searchDTO);
		if (searchDTO.isFuzzySearch())
			relevanceSort = true;

		searchSourceBuilder.query(query);

		if (sortBy && !relevanceSort
				&& (null == searchDTO.getSoftConstraints() || searchDTO.getSoftConstraints().isEmpty())) {
			Map<String, String> sorting = searchDTO.getSortBy();
			if (sorting == null || sorting.isEmpty()) {
				sorting = new HashMap<String, String>();
				sorting.put("name", "asc");
				sorting.put("lastUpdatedOn", "desc");
			}
			for (String key : sorting.keySet()){
				if(key.contains(".")){
					String nestedPath = key.split("\\.")[0];
					searchSourceBuilder.sort(SortBuilders.fieldSort(key + CompositeSearchConstants.RAW_FIELD_EXTENSION).order(getSortOrder(sorting.get(key))).setNestedSort(new NestedSortBuilder(nestedPath)));
				} else{
					searchSourceBuilder.sort(key + CompositeSearchConstants.RAW_FIELD_EXTENSION,
							getSortOrder(sorting.get(key)));
				}
			}
		}
		setAggregations(groupByFinalList, searchSourceBuilder);
		setAggregations(searchSourceBuilder, searchDTO.getAggregations());
		searchSourceBuilder.trackScores(true);
		return searchSourceBuilder;
	}

	/**
	 * @param groupByList
	 * @param searchSourceBuilder
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void setAggregations(List<Map<String, Object>> groupByList,
			SearchSourceBuilder searchSourceBuilder) {
		TermsAggregationBuilder termBuilder = null;
		if (groupByList != null && !groupByList.isEmpty()) {
			for (Map<String, Object> groupByMap : groupByList) {
				String groupByParent = (String) groupByMap.get("groupByParent");
				termBuilder = AggregationBuilders.terms(groupByParent)
						.field(groupByParent + CompositeSearchConstants.RAW_FIELD_EXTENSION)
						.size(ElasticSearchUtil.defaultResultLimit);
				List<String> groupByChildList = (List<String>) groupByMap.get("groupByChildList");
				if (groupByChildList != null && !groupByChildList.isEmpty()) {
					for (String childGroupBy : groupByChildList) {
						termBuilder.subAggregation(AggregationBuilders.terms(childGroupBy)
								.field(childGroupBy + CompositeSearchConstants.RAW_FIELD_EXTENSION)
								.size(ElasticSearchUtil.defaultResultLimit));
					}
				}
				searchSourceBuilder.aggregation(termBuilder);
			}
		}
	}

	/**
	 * @param searchDTO
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private QueryBuilder prepareSearchQuery(SearchDTO searchDTO) {
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		QueryBuilder queryBuilder = null;
		String totalOperation = searchDTO.getOperation();
		List<Map> properties = searchDTO.getProperties();
		for (Map<String, Object> property : properties) {
			String opertation = (String) property.get("operation");

			List<Object> values;
			try {
				values = (List<Object>) property.get("values");
			} catch (Exception e) {
				values = Arrays.asList(property.get("values"));
			}
			values = values.stream().filter(value -> (null != value)).collect(Collectors.toList());


			String propertyName = (String) property.get("propertyName");
			if (propertyName.equals("*")) {
				relevanceSort = true;
				propertyName = "all_fields";
				queryBuilder = getAllFieldsPropertyQuery(values);
				boolQuery.must(queryBuilder);
				continue;
			}

			propertyName = propertyName + CompositeSearchConstants.RAW_FIELD_EXTENSION;

			switch (opertation) {
			case CompositeSearchConstants.SEARCH_OPERATION_EQUAL: {
				queryBuilder = getMustTermQuery(propertyName, values, true);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL: {
				queryBuilder = getMustTermQuery(propertyName, values, false);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_NOT_IN: {
				queryBuilder = getNotInQuery(propertyName, values);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_ENDS_WITH: {
				queryBuilder = getRegexQuery(propertyName, values);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_LIKE:
			case CompositeSearchConstants.SEARCH_OPERATION_CONTAINS: {
				queryBuilder = getMatchPhraseQuery(propertyName, values, true);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_NOT_LIKE: {
				queryBuilder = getMatchPhraseQuery(propertyName, values, false);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_STARTS_WITH: {
				queryBuilder = getMatchPhrasePrefixQuery(propertyName, values);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_EXISTS: {
				queryBuilder = getExistsQuery(propertyName, values, true);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_NOT_EXISTS: {
				queryBuilder = getExistsQuery(propertyName, values, false);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN: {
				queryBuilder = getRangeQuery(propertyName, values,
						CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN_EQUALS: {
				queryBuilder = getRangeQuery(propertyName, values,
						CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN_EQUALS);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN: {
				queryBuilder = getRangeQuery(propertyName, values, CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN_EQUALS: {
				queryBuilder = getRangeQuery(propertyName, values,
						CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN_EQUALS);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_RANGE: {
				queryBuilder = getRangeQuery(propertyName, values);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_AND: {
				queryBuilder = getAndQuery(propertyName, values);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			}
			if (totalOperation.equalsIgnoreCase(AND)) {
				boolQuery.must(queryBuilder);
			} else {
				boolQuery.should(queryBuilder);
			}

		}

		Map<String, Object> softConstraints = searchDTO.getSoftConstraints();
		if (null != softConstraints && !softConstraints.isEmpty()) {
			boolQuery.should(getSoftConstraintQuery(softConstraints));
			searchDTO.setSortBy(null);
			// relevanceSort = true;
		}
		return boolQuery;
	}

	private QueryBuilder checkNestedProperty(QueryBuilder queryBuilder, String propertyName) {
		if(propertyName.replaceAll(CompositeSearchConstants.RAW_FIELD_EXTENSION, "").contains(".")) {
			queryBuilder = QueryBuilders.nestedQuery(propertyName.split("\\.")[0], queryBuilder, org.apache.lucene.search.join.ScoreMode.None);
		}
		return queryBuilder;
	}

	/**
	 * @param searchDTO
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private QueryBuilder prepareFilteredSearchQuery(SearchDTO searchDTO) {
		List<FilterFunctionBuilder> filterFunctionBuilder = new ArrayList<>();

		Map<String, Float> weightages = (Map<String, Float>) searchDTO.getAdditionalProperty("weightagesMap");
		if (weightages == null) {
			weightages = new HashMap<String, Float>();
			weightages.put("default_weightage", 1.0f);
		}
		List<String> querySearchFeilds = ElasticSearchUtil.getQuerySearchFields();
		List<Map> properties = searchDTO.getProperties();
		for (Map<String, Object> property : properties) {
			String opertation = (String) property.get("operation");

			List<Object> values;
			try {
				values = (List<Object>) property.get("values");
			} catch (Exception e) {
				values = Arrays.asList(property.get("values"));
			}

			values = values.stream().filter(value -> (null != value)).collect(Collectors.toList());
			String propertyName = (String) property.get("propertyName");
			if (propertyName.equals("*")) {
				relevanceSort = true;
				propertyName = "all_fields";
				filterFunctionBuilder
						.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(getAllFieldsPropertyQuery(values),
								ScoreFunctionBuilders.weightFactorFunction(weightages.get("default_weightage"))));
				continue;
			}

			propertyName = propertyName + CompositeSearchConstants.RAW_FIELD_EXTENSION;
			float weight = getweight(querySearchFeilds, propertyName);
			switch (opertation) {
			case CompositeSearchConstants.SEARCH_OPERATION_EQUAL: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getMustTermQuery(propertyName, values, true),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getMustTermQuery(propertyName, values, true),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_ENDS_WITH: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getRegexQuery(propertyName, values), ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_LIKE:
			case CompositeSearchConstants.SEARCH_OPERATION_CONTAINS: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getMatchPhraseQuery(propertyName, values, true),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_NOT_LIKE: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getMatchPhraseQuery(propertyName, values, false),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_STARTS_WITH: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getMatchPhrasePrefixQuery(propertyName, values),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_EXISTS: {
				filterFunctionBuilder.add(
						new FunctionScoreQueryBuilder.FilterFunctionBuilder(getExistsQuery(propertyName, values, true),
								ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_NOT_EXISTS: {
				filterFunctionBuilder.add(
						new FunctionScoreQueryBuilder.FilterFunctionBuilder(getExistsQuery(propertyName, values, false),
								ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_NOT_IN: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getNotInQuery(propertyName, values), ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getRangeQuery(propertyName, values, CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN_EQUALS: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getRangeQuery(propertyName, values,
								CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN_EQUALS),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getRangeQuery(propertyName, values, CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN_EQUALS: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getRangeQuery(propertyName, values, CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN_EQUALS),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_AND: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getAndQuery(propertyName, values),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			}
		}

		FunctionScoreQueryBuilder queryBuilder = QueryBuilders
				.functionScoreQuery(
						filterFunctionBuilder.toArray(new FilterFunctionBuilder[filterFunctionBuilder.size()]))
				.boostMode(CombineFunction.REPLACE).scoreMode(ScoreMode.SUM);
		return queryBuilder;

	}

	private QueryBuilder getAndQuery(String propertyName, List<Object> values) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (Object value : values) {
				queryBuilder.must(
						QueryBuilders.matchQuery(propertyName, value).operator(Operator.AND));
		}
		return queryBuilder;
	}

	/**
	 * @param querySearchFeilds
	 * @param propertyName
	 * @return
	 */
	private float getweight(List<String> querySearchFeilds, String propertyName) {
		float weight = 1.0F;
		if (querySearchFeilds.contains(propertyName)) {
			for (String field : querySearchFeilds) {
				if (field.contains(propertyName)) {
					weight = Float
							.parseFloat((StringUtils.isNotBlank(field.split("^")[1])) ? field.split("^")[1] : "1.0");
				}
			}
		}
		return weight;
	}

	/**
	 * @param values
	 * @return
	 */
	private QueryBuilder getAllFieldsPropertyQuery(List<Object> values) {
		List<String> queryFields = ElasticSearchUtil.getQuerySearchFields();
		Map<String, Float> queryFieldsMap = new HashMap<>();
		for (String field : queryFields) {
			if (field.contains("^"))
				queryFieldsMap.put(field.split("\\^")[0], Float.valueOf(field.split("\\^")[1]));
			else
				queryFieldsMap.put(field, 1.0f);
		}
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (Object value : values) {
			queryBuilder
					.should(QueryBuilders.multiMatchQuery(value).fields(queryFieldsMap)
							.operator(Operator.AND).type(Type.CROSS_FIELDS).lenient(true));
		}

		return queryBuilder;
	}

	/**
	 * @param softConstraints
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private QueryBuilder getSoftConstraintQuery(Map<String, Object> softConstraints) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (String key : softConstraints.keySet()) {
			List<Object> data = (List<Object>) softConstraints.get(key);
			if(data.get(1) instanceof List) {
				List<Object> dataList = (List<Object>) data.get(1);
				for(Object value: dataList) {
					queryBuilder
							.should(QueryBuilders.matchQuery(key + CompositeSearchConstants.RAW_FIELD_EXTENSION, value)
									.boost(Integer.valueOf((int) data.get(0)).floatValue()));
				}
			}
			else {
				queryBuilder.should(
						QueryBuilders.matchQuery(key + CompositeSearchConstants.RAW_FIELD_EXTENSION, data.get(1))
								.boost(Integer.valueOf((int) data.get(0)).floatValue()));
			}
		}
		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @return
	 */
	private QueryBuilder getRangeQuery(String propertyName, List<Object> values, String operation) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (Object value : values) {
			switch (operation) {
			case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN: {
				queryBuilder.should(QueryBuilders
						.rangeQuery(propertyName).gt(value));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN_EQUALS: {
				queryBuilder.should(QueryBuilders
						.rangeQuery(propertyName).gte(value));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN: {
				queryBuilder.should(QueryBuilders
						.rangeQuery(propertyName).lt(value));
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN_EQUALS: {
				queryBuilder.should(QueryBuilders
						.rangeQuery(propertyName).lte(value));
				break;
			}
			}
		}

		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @return
	 */
	private QueryBuilder getExistsQuery(String propertyName, List<Object> values, boolean exists) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (Object value : values) {
			if (exists) {
				queryBuilder.should(QueryBuilders.existsQuery(String.valueOf(value)));
			} else {
				queryBuilder.mustNot(QueryBuilders.existsQuery(String.valueOf(value)));
			}
		}
		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @return
	 */
	private QueryBuilder getNotInQuery(String propertyName, List<Object> values) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		queryBuilder
				.mustNot(QueryBuilders.termsQuery(propertyName, values));
		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @return
	 */
	private QueryBuilder getMatchPhrasePrefixQuery(String propertyName, List<Object> values) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (Object value : values) {
			queryBuilder.should(QueryBuilders.prefixQuery(
					propertyName, ((String) value).toLowerCase()));
		}
		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @param match
	 * @return
	 */
	private QueryBuilder getMatchPhraseQuery(String propertyName, List<Object> values, boolean match) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (Object value : values) {
			String stringValue = String.valueOf(value);
			if (match) {
				queryBuilder.should(QueryBuilders
						.regexpQuery(propertyName,
								".*" + stringValue.toLowerCase() + ".*"));
			} else {
				queryBuilder.mustNot(QueryBuilders
						.regexpQuery(propertyName,
								".*" + stringValue.toLowerCase() + ".*"));
			}
		}
		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @return
	 */
	private QueryBuilder getRegexQuery(String propertyName, List<Object> values) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (Object value : values) {
			String stringValue = String.valueOf(value);
			queryBuilder.should(QueryBuilders.regexpQuery(propertyName,
					".*" + stringValue.toLowerCase()));
		}
		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @param match
	 * @return
	 */
	private QueryBuilder getMustTermQuery(String propertyName, List<Object> values, boolean match) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (Object value : values) {
			if (match) {
				queryBuilder.should(
						QueryBuilders.matchQuery(propertyName, value));
			} else {
				queryBuilder.mustNot(
						QueryBuilders.matchQuery(propertyName, value));
			}
		}

		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private QueryBuilder getRangeQuery(String propertyName, List<Object> values) {
		RangeQueryBuilder queryBuilder = new RangeQueryBuilder(propertyName);
		for (Object value : values) {
			Map<String, Object> rangeMap = (Map<String, Object>) value;
			if (!rangeMap.isEmpty()) {
				for (String key : rangeMap.keySet()) {
					switch (key) {
					case CompositeSearchConstants.SEARCH_OPERATION_RANGE_GTE: {
						queryBuilder.from(rangeMap.get(key));
						break;
					}
					case CompositeSearchConstants.SEARCH_OPERATION_RANGE_LTE: {
						queryBuilder.to(rangeMap.get(key));
						break;
					}
					}
				}
			}
		}
		return queryBuilder;
	}

	/**
	 * @param value
	 * @return
	 */
	private SortOrder getSortOrder(String value) {
		return value.equalsIgnoreCase(ASC_ORDER) ? SortOrder.ASC : SortOrder.DESC;
	}

	public Future<List<Object>> processSearchQuery(SearchDTO searchDTO, boolean includeResults, String index)
			throws Exception {
		return processSearchQuery(searchDTO, includeResults, index, true);
	}

	public Future<List<Object>> processSearchQuery(SearchDTO searchDTO, boolean includeResults, String index,
			boolean sort)
			throws Exception {
		List<Map<String, Object>> groupByFinalList = new ArrayList<Map<String, Object>>();
		if (searchDTO.getLimit() == 0)
			searchDTO.setLimit(ElasticSearchUtil.defaultResultLimit);
		SearchSourceBuilder query = processSearchQuery(searchDTO, groupByFinalList, sort);
		TelemetryManager.log(" search query: " + query);
		Future<SearchResponse> searchResponse = ElasticSearchUtil.search(index, query);
		
		return searchResponse.map(new Mapper<SearchResponse, List<Object>>() {
			public List<Object> apply(SearchResponse searchResult) {
				List<Object> response = new ArrayList<Object>();
				TelemetryManager.log("search result from elastic search" + searchResult);
				SearchHits resultMap = searchResult.getHits();
				SearchHit[] result = resultMap.getHits();
				for (SearchHit hit : result) {
					response.add(hit.getSourceAsMap());
				}
				TelemetryManager.log("search response size: " + response.size());
				return response;
			}
		}, ExecutionContext.Implicits$.MODULE$.global());
		
	}

	public Future<SearchResponse> processSearchQueryWithSearchResult(SearchDTO searchDTO, boolean includeResults,
			String index,
			boolean sort) throws Exception {
		List<Map<String, Object>> groupByFinalList = new ArrayList<Map<String, Object>>();
		if (searchDTO.getLimit() == 0)
			searchDTO.setLimit(ElasticSearchUtil.defaultResultLimit);
		SearchSourceBuilder query = processSearchQuery(searchDTO, groupByFinalList, sort);
		TelemetryManager.log(" search query: " + query);
		Future<SearchResponse> searchResult = ElasticSearchUtil.search(index, query);
		TelemetryManager.log("search result from elastic search" + searchResult);
		return searchResult;
	}

	private void setAggregations(SearchSourceBuilder searchSourceBuilder, List<Map<String, Object>> aggregations) {
		if(CollectionUtils.isNotEmpty(aggregations)){
			for(Map<String, Object> aggregate: aggregations){
				TermsAggregationBuilder termBuilder = AggregationBuilders.terms((String)aggregate.get("l1"))
						.field(aggregate.get("l1") + CompositeSearchConstants.RAW_FIELD_EXTENSION)
						.size(ElasticSearchUtil.defaultResultLimit);
				int level = 2;
				termBuilder.subAggregation(getNextLevelAggregation(aggregate, level));
				searchSourceBuilder.aggregation(termBuilder);
			}
		}
	}

	private AggregationBuilder getNextLevelAggregation(Map<String, Object> aggregate, int level) {
        TermsAggregationBuilder termBuilder = AggregationBuilders.terms((String)aggregate.get("l" + level))
                .field(aggregate.get("l" + level) + CompositeSearchConstants.RAW_FIELD_EXTENSION)
                .size(ElasticSearchUtil.defaultResultLimit);


		if(level == aggregate.keySet().size()){
			return termBuilder;
		}else {
		    level += 1;
			return termBuilder.subAggregation(getNextLevelAggregation(aggregate, level));
		}
	}

    private List<Map<String,Object>> aggregateResult(Aggregations aggregations) {
        List<Map<String, Object>> aggregationList = new ArrayList<>();
	    if(null != aggregations){
            Map<String, Aggregation> aggregationMap = aggregations.getAsMap();
            for(String key: aggregationMap.keySet()){
                Terms terms = (Terms) aggregationMap.get(key);
                List<Terms.Bucket> buckets = (List<Terms.Bucket>) terms.getBuckets();
                List<Map<String, Object>> values = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(buckets)) {
                    for(Terms.Bucket bucket: buckets) {
                        Map<String, Object> termBucket = new HashMap<String, Object>() {{
                            put("count", bucket.getDocCount());
                            put("name", bucket.getKey());
                            List<Map<String,Object>> subAggregations = aggregateResult(bucket.getAggregations());
                            if(CollectionUtils.isNotEmpty(subAggregations))
                                put("aggregations", subAggregations);
                        }};
                        values.add(termBucket);
                    }
                    aggregationList.add(new HashMap<String, Object>(){{
                        put("values", values);
                        put("name", key);
                    }});
                }
            }

        }
        return aggregationList;
    }

	private QueryBuilder getSearchQuery(SearchDTO searchDTO) {
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		QueryBuilder origFilterQry = getQuery(searchDTO);
		QueryBuilder implFilterQuery = null;

		if (CollectionUtils.isNotEmpty(searchDTO.getImplicitFilterProperties())) {
			List<Map> properties = searchDTO.getProperties();
			searchDTO.setProperties(searchDTO.getImplicitFilterProperties());
			implFilterQuery = getQuery(searchDTO);
			searchDTO.setProperties(properties);
			boolQuery.should(origFilterQry);
			boolQuery.should(implFilterQuery);
			return boolQuery;
		} else {
			return origFilterQry;
		}
	}

	private QueryBuilder getQuery(SearchDTO searchDTO) {
		return searchDTO.isFuzzySearch() ? prepareFilteredSearchQuery(searchDTO) : prepareSearchQuery(searchDTO);
	}


}