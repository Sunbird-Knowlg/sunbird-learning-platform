package org.ekstep.sync.tool.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.cassandra.store.CassandraStore;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ServerException;
import org.ekstep.learning.contentstore.ContentStoreParams;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.telemetry.logger.TelemetryManager;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

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
	
	public int sync(List<String> dialcodes) throws Exception {
		//Map<String, Object> syncRequest = new HashMap<String, Object>();
		Map<String, Object> messages = getDialcodesFromIds(dialcodes);
		/*for (Row row : rows) {
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
		}*/
		upsertDocument(messages);
		System.out.println("Content synced: " + messages.size());
		return messages.size();
	}
	
	private void upsertDocument( Map<String, Object> messages) throws Exception {
		ElasticSearchUtil.bulkIndexWithIndexId(indexName, documentType, messages);
	}
	
	public Map<String, Object> getDialcodesFromIds(List<String> identifiers) {
        String query = "SELECT * FROM " + getKeyspace() + "." + getTable() + " WHERE identifier IN :ids";
        Map<String, Object> messages = new HashMap<String, Object>();
        Session session = CassandraConnector.getSession();
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind();
        session.execute(bs.setList("ids", identifiers));
        try {
            ResultSet rs = session.execute(bs.setList("ids", identifiers));
            if (null != rs) {
            	Iterator<Row> iterator = rs.iterator();
            	while(iterator.hasNext()) {
            		Row row = iterator.next();
                    Map<String, Object> syncRequest = new HashMap<String, Object>();
                    String dialcodeId = (String)row.getString("identifier");
                    System.out.println(dialcodeId + ": " + row);
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
            	return messages;
                
            } else {
               return null;
            }
        } catch (Exception e) {
            TelemetryManager.error("Error! Executing get collection hierarchy: " + e.getMessage(), e);
            throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
                    "Error fetching hierarchy from hierarchy Store.", e);
        }
    }
}
