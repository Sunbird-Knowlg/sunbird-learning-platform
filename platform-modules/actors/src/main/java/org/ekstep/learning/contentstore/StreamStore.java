package org.ekstep.learning.contentstore;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.cassandra.store.CassandraStore;
import org.ekstep.common.Platform;
import org.ekstep.searchindex.util.CompositeSearchConstants;

public class StreamStore extends CassandraStore {

    private static ObjectMapper mapper = new ObjectMapper();

    public StreamStore() {
        super();
        String keyspace = Platform.config.hasPath("stream.keyspace.name")
                ? Platform.config.getString("stream.keyspace.name")
                : "stream_store";
        String table = Platform.config.hasPath("stream.keyspace.table")
                ? Platform.config.getString("stream.keyspace.table")
                : "stream_data";
        boolean index = Platform.config.hasPath("content.index") ? Platform.config.getBoolean("content.index") : false;
        String objectType = "Content";
        initialise(keyspace, table, objectType, index);
        nodeType = CompositeSearchConstants.NODE_TYPE_DATA;
    }

    public void insert(String content_id, String artifactUrl) {
        String query="INSERT INTO stream_store.stream_data(content_id,job_status,arrived_on,processed_on,artifact_url,media_job_id,media_job_status,retry_count,error) values(?,'ARRIVED',dateOf(now()),NULL,?,NULL,NULL,0,NULL) IF NOT EXISTS;";
        Session session = CassandraConnector.getSession();
        com.datastax.driver.core.PreparedStatement statement = session.prepare(query);
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(content_id, artifactUrl));
    }


}
