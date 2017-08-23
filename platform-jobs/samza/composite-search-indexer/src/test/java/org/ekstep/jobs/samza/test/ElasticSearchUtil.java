package org.ekstep.jobs.samza.test;

import java.util.Map;

import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticSearchUtil {

	private final Client client;

	public ElasticSearchUtil(Client client) {
		this.client = client;
	}

	private ObjectMapper mapper = new ObjectMapper();

	public void add(Map<String,Object> record) throws JsonProcessingException {
		IndexRequest indexRequest = new IndexRequest(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, (String)record.get("identifier"));
		String request = mapper.writeValueAsString(record);
		indexRequest.source(request);
		client.index(indexRequest).actionGet();
	}

	public Map<String, Object> findById(String identifier) {
		SearchResponse response = client.prepareSearch(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX)
				.setTypes(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE)
				.setQuery(QueryBuilders.termQuery("_id", identifier)).execute().actionGet();
		SearchHits hits = response.getHits();
		for (SearchHit hit : hits.getHits()) {
			Map<String, Object> fields = hit.getSource();
			return fields;
		}
		return null;
	}
}