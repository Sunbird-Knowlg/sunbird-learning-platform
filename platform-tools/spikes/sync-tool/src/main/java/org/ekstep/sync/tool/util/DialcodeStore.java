package org.ekstep.sync.tool.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.cassandra.store.CassandraStore;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ServerException;
import org.ekstep.learning.contentstore.ContentStoreParams;
import org.ekstep.telemetry.logger.TelemetryManager;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class DialcodeStore extends CassandraStore {

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
	}
	
	public Map<String, List<String>> getDialcodes(List<String> dialcodeIds) {
        String query = "SELECT identifier, channel FROM " + getKeyspace() + "." + getTable() + " WHERE identifier IN :dialcodeIds";

        Session session = CassandraConnector.getSession();
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind();
        try {
            ResultSet rs = session.execute(bs.setList("dialcodeIds", dialcodeIds));
            if (null != rs) {
            	Map<String, List<String>> dialcodes = new HashMap<String, List<String>>();
            	List<String> dialcodesWithNoChannel = new ArrayList<String>();
            	while(rs.iterator().hasNext()) {
                    Row row = rs.iterator().next();
                    String identifier = row.getString("identifier");
                    String channel = row.getString("channel");
                    if(StringUtils.isNotBlank(channel)) 
                        if(dialcodes.containsKey(channel)) 
                        	dialcodes.get(channel).add(identifier);
                        else 
                        	dialcodes.put(channel, new ArrayList<String>());
                    else 
                    	dialcodesWithNoChannel.add(identifier);
                }
            	if(CollectionUtils.isNotEmpty(dialcodesWithNoChannel))
            		System.out.println("Dialcodes with no Channel:: " + dialcodesWithNoChannel);
            	return dialcodes;
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
