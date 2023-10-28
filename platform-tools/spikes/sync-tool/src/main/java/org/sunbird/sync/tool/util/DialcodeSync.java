package org.sunbird.sync.tool.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.sunbird.cassandra.connector.util.CassandraConnector;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ServerException;
import org.sunbird.learning.contentstore.ContentStoreParams;
import org.sunbird.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.telemetry.logger.TelemetryManager;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class DialcodeSync {

	
	private static String indexName = null;
	private static String documentType = null;
	private static String keyspace = null;
    private static String table = null;
   
    
    public DialcodeSync() {
    	indexName = Platform.config.hasPath("dialcode.index.name") 
    			? Platform.config.getString("dialcode.index.name") : "dialcode";
    	documentType = Platform.config.hasPath("dialcode.document.type") 
    			? Platform.config.getString("dialcode.document.type") : "dc";
    	keyspace = Platform.config.hasPath("dialcode.keyspace.name")
                ? Platform.config.getString("dialcode.keyspace.name") : "sunbirddev_dialcode_store";
        table = Platform.config.hasPath("dialcode.table") 
        		? Platform.config.getString("dialcode.table") : "dial_code";
				System.out.println("dialcode.index.name.samarth["+indexName+"]\n dialcode.document.type["+documentType+"]\n dialcode.keyspace.name["+keyspace+"] dialcode.table["+table+"]");
        ElasticSearchUtil.initialiseESClient(indexName, Platform.config.getString("search.es_conn_info"));
	}
    		
	public int sync(List<String> dialcodes) throws Exception {
		System.out.println("DialcodeSync:sync:message:: Total number of Dialcodes to be fetched from cassandra: " + dialcodes.size());
		// Get dialcodes data from cassandra
		Map<String, Object> messages = getDialcodesFromIds(dialcodes);
		if(MapUtils.isEmpty(messages)) {
			System.out.println("DialcodeSync:sync:message:: No dialcodes data fetched from cassandra.");
			return 0;
		}
		System.out.println("DialcodeSync:sync:message:: Total number of Dialcodes data fetched from cassandra: " + messages.size());
		upsertDocument(messages);
		System.out.println("DialcodeSync:sync:Dialcodes synced.");
		return messages.size();
	}
	
	private void upsertDocument( Map<String, Object> messages) throws Exception {
		ElasticSearchUtil.bulkIndexWithIndexId(indexName, documentType, messages);
	}
	
	public Map<String, Object> getDialcodesFromIds(List<String> identifiers) {
        try {
        	Map<String, Object> messages = new HashMap<String, Object>();
            ResultSet rs = getDialcodesFromDB(identifiers);
            if (null != rs) {
            	Map<String, Object> dialCodesFromDB = new HashMap<String, Object>();
            	while(rs.iterator().hasNext()) {
            		Row row = rs.iterator().next();
                    String dialcodeId = (String)row.getString("identifier");
                    dialCodesFromDB.put(dialcodeId, row);
                    
                    Map<String, Object> syncRequest = new HashMap<String, Object>(){{
                    	put("identifier", row.getString("identifier"));
            			put("channel", row.getString("channel"));
            			put("publisher", row.getString("publisher"));
            			put("batchcode", row.getString("batchCode"));
            			put("status", row.getString("status"));
            			put("metadata", row.getString("metadata"));
            			put("generated_on", row.getString("generated_on"));
            			put("published_on", row.getString("published_on"));
            			put("objectType", "DialCode");
                    }};
        			messages.put(dialcodeId, syncRequest);
            	}
            	System.out.println("total dialcodes fetched from cassandra: " + dialCodesFromDB);
            	return messages;
                
            } else {
               return null;
            }
        } catch (Exception e) {
            TelemetryManager.error("Error! Executing get dialcodes: " + e.getMessage(), e);
            throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
                    "Error fetching dialcode from dialcodes table.", e);
        }
    }
	
	private ResultSet getDialcodesFromDB(List<String> identifiers) {
		String dialcodes = String.join("', '", identifiers); 
		String query = "SELECT * FROM " + keyspace + "." + table + " WHERE identifier IN ('" + dialcodes + "')";
		Session session = CassandraConnector.getSession();
		return session.execute(query);
	}
}
