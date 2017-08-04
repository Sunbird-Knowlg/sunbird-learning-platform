package org.ekstep.jobs.samza.test;

import java.util.Map;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.dac.dto.AuditHistoryRecord;
import com.ilimi.dac.enums.AuditHistoryConstants;

public class ElasticSearchUtil {

	private final Client client;

	public ElasticSearchUtil(Client client) {
		this.client = client;
	}

	private ObjectMapper mapper = new ObjectMapper();

	public void add(AuditHistoryRecord record) throws JsonProcessingException {
		IndexRequest indexRequest = new IndexRequest(AuditHistoryConstants.AUDIT_HISTORY_INDEX,
				AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE, record.getObjectId());
		String request = mapper.writeValueAsString(record);
		indexRequest.source(request);
		client.index(indexRequest).actionGet();
	}

	public Map<String, Object> findById(String identifier) {
		SearchResponse response = client.prepareSearch(AuditHistoryConstants.AUDIT_HISTORY_INDEX)
				.setTypes(AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE)
				.setQuery(QueryBuilders.termQuery("graphId", "domain")).execute().actionGet();
		SearchHits hits = response.getHits();
		for (SearchHit hit : hits.getHits()) {
			Map<String, Object> fields = hit.getSource();
			return fields;
		}
		return null;
	}
}
