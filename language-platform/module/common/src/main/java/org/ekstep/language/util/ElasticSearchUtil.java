package org.ekstep.language.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;

public class ElasticSearchUtil {

	private Client client;
	private String hostName;
	
	public ElasticSearchUtil() throws UnknownHostException {
		super();
		Settings settings = Settings.settingsBuilder()
				.put("cluster.name", "my-application").build();
		client = TransportClient
				.builder()
				.settings(settings)
				.build()
				.addTransportAddress(
						new InetSocketTransportAddress(InetAddress
								.getByName(hostName), 9300))
				.addTransportAddress(
						new InetSocketTransportAddress(InetAddress
								.getByName(hostName), 9300));
	}

	public void initialize() {
		hostName = "localhost";
	}

	@SuppressWarnings("unused")
	private Client createClient() {
		return client;
	}

	public void closeClient() {
		client.close();
	}

	public void addIndexDocument(String indexName, String documentType,
			String documentId, String document) {
		client.prepareIndex(indexName, documentType, documentId)
				.setSource(document).execute().actionGet();
	}

	public void addIndex(String indexName, String documentType) {
		try {
			if (!isIndexExists(indexName)) {
				CreateIndexRequestBuilder cirb;
				XContentBuilder mapping = XContentFactory.jsonBuilder().startObject()
						.startObject(documentType)
						.startObject("properties")
						.startObject("date")
						.field("type", "date")
						.field("format", "dd-MMM-yyyy HH:mm:ss").endObject()
						.endObject().endObject()
						.endObject();
				
				cirb = client
						.admin()
						.indices()
						.prepareCreate(indexName);
				cirb.addMapping(documentType, mapping);

				CreateIndexResponse response = cirb.execute().actionGet();
				if (response.isAcknowledged()) {
					System.out.println("Index created.");
				} else {
					System.err.println("Index creation failed.");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addIndexDocument(String indexName, String documentType,
			String document) {
		client.prepareIndex(indexName, documentType).setSource(document)
				.execute().actionGet();
	}

	public boolean isIndexExists(String indexName) {
		return client.admin().indices().prepareExists(indexName).execute()
				.actionGet().isExists();
	}

	public void deleteDocument(String indexName, String documentType,
			String documentId) {
		client.prepareDelete(indexName, documentType, documentId).get();
	}

	public void bulkIndexWithIndexId(String indexName, String documentType,
			Map<String, String> jsonObjects) {
		if (!jsonObjects.isEmpty()) {
			BulkRequestBuilder brb = client.prepareBulk();
			for (Map.Entry<String, String> entry : jsonObjects.entrySet()) {
				IndexRequest irq = new IndexRequest(indexName, documentType,
						entry.getKey());
				irq.source(entry.getValue());
				brb.add(irq);
			}
			BulkResponse br = brb.execute().actionGet();

			if (br.hasFailures()) {
				// TODO
			}
		}
	}

	public void bulkIndexWithAutoGenerateIndexId(String indexName,
			String documentType, List<String> jsonObjects) {
		addIndex(indexName, documentType);
		if (!jsonObjects.isEmpty()) {
			BulkRequestBuilder brb = client.prepareBulk();
			for (String jsonString : jsonObjects) {
				IndexRequest irq = new IndexRequest(indexName, documentType);
				irq.source(jsonString);
				brb.add(irq);
			}
			BulkResponse br = brb.execute().actionGet();

			if (br.hasFailures()) {
				// TODO
			}
		}
	}

	public void search() {
		//TODO test search
		SearchResponse response = client.prepareSearch("citation_index_ta")
		        .setTypes("citation")
		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
		        .setQuery(QueryBuilders.termQuery("word", "தவான்"))                 // Query
		        //.setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
		        //.setFrom(0).setSize(60)
		        .setExplain(true)
		        .execute()
		        .actionGet();
		response.getHits();
	}
}
