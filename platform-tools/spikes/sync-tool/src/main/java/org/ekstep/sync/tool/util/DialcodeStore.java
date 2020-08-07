package org.ekstep.sync.tool.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.cassandra.store.CassandraStore;
import org.ekstep.common.Platform;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;

import com.datastax.driver.core.Row;

public class DialcodeStore extends CassandraStore {

	private ObjectMapper mapper = new ObjectMapper();
	
	private static String indexName;
	private static String documentType;
	public DialcodeStore() {
		super();
        String keyspace = Platform.config.hasPath("dialcode.keyspace.name")
                ? Platform.config.getString("dialcode.keyspace.name")
                : "sunbirddev_dialcode_store";
        String table = Platform.config.hasPath("dialcode.table")
                ? Platform.config.getString("dialcode.table")
                : "dial_code";
        String objectType = "Dialcode";
        initialise(keyspace, table, objectType, false);
        
        indexName = Platform.config.hasPath("dialcode.index.name") ? Platform.config.getString("dialcode.index.name")
				: "dialcode";
		documentType = Platform.config.hasPath("dialcode.document.type") ? Platform.config.getString("dialcode.document.type")
				: "dc";
		ElasticSearchUtil.initialiseESClient(indexName, Platform.config.getString("search.es_conn_info"));
	}
	
	public int sync(Map<String, Object> map) throws Exception {
		Map<String, Object> syncRequest = new HashMap<String, Object>();
		Map<String, Object> messages = new HashMap<String, Object>();
		
		List<Row> rows = getRecordsByProperties(map);
		for (Row row : rows) {
			String dialcodeId = (String)row.getString("identifier");
			syncRequest = new HashMap<String, Object>();
			syncRequest.put("identifier", row.getString("identifier"));
			syncRequest.put("channel", row.getString("channel"));
			syncRequest.put("publisher", row.getString("publisher"));
			syncRequest.put("batchcode", row.getString("batchCode"));
			syncRequest.put("status", row.getString("status"));
			syncRequest.put("metadata", row.getString("metadata"));
			syncRequest.put("generated_on", row.getString("generated_on"));
			syncRequest.put("published_on", row.getString("published_on"));
			syncRequest.put("objectType", "DialCode");
			messages.put(dialcodeId, syncRequest);
		}
		upsertDocument(messages);
		System.out.println("Content synced: " + rows.size());
		return rows.size();
	}
	
	private void upsertDocument( Map<String, Object> messages) throws Exception {
		ElasticSearchUtil.bulkIndexWithIndexId(indexName, documentType, messages);
	}
}
