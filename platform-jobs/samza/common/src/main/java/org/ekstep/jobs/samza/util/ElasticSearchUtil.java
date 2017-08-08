package org.ekstep.jobs.samza.util;

import java.util.Map;
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

	public void add(Map<String,Object> record, String index, String indexType) throws JsonProcessingException {
		IndexRequest indexRequest = new IndexRequest(index, indexType, (String)record.get("ObjectId"));
		String request = mapper.writeValueAsString(record);
		indexRequest.source(request);
		client.index(indexRequest).actionGet();
	}

	public Map<String, Object> findAuditLogById(String index, String indexType) {
		SearchResponse response = client.prepareSearch(index)
				.setTypes(indexType)
				.setQuery(QueryBuilders.termQuery("graphId", "domain")).execute().actionGet();
		SearchHits hits = response.getHits();
		for (SearchHit hit : hits.getHits()) {
			Map<String, Object> fields = hit.getSource();
			return fields;
		}
		return null;
	}
	
	public Map<String, Object> searchById(String identifier, String index, String indexType) {
		SearchResponse response = client.prepareSearch(index)
				.setTypes(indexType)
				.setQuery(QueryBuilders.termQuery("_id", identifier)).execute().actionGet();
		SearchHits hits = response.getHits();
		for (SearchHit hit : hits.getHits()) {
			Map<String, Object> fields = hit.getSource();
			return fields;
		}
		return null;
	}
}
