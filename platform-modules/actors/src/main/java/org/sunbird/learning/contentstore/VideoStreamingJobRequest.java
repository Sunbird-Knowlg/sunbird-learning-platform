package org.sunbird.learning.contentstore;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sunbird.cassandra.connector.util.CassandraConnector;
import org.sunbird.cassandra.store.CassandraStore;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ServerException;
import org.sunbird.searchindex.util.CompositeSearchConstants;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.util.HashMap;
import java.util.Map;

public class VideoStreamingJobRequest extends CassandraStore {

    private ObjectMapper mapper = new ObjectMapper();

    public VideoStreamingJobRequest() {
        super();
        String keyspace = Platform.config.hasPath("stream.keyspace.name")
                ? Platform.config.getString("stream.keyspace.name")
                : "platform_db";
        String table = Platform.config.hasPath("stream.keyspace.table")
                ? Platform.config.getString("stream.keyspace.table")
                : "job_request";
        boolean index = false;
        String objectType = "Content";
        initialise(keyspace, table, objectType, index);
        nodeType = CompositeSearchConstants.NODE_TYPE_DATA;
    }

    public void insert(String contentId, String artifactUrl, String channel, String pkgVersion) {
        try {
        String query = "INSERT INTO " + getKeyspace() + "." + getTable() + "(client_key,request_id,job_name,job_id,status,request_data,location,dt_file_created,dt_first_event,dt_last_event,dt_expiration,iteration,dt_job_submitted,dt_job_processing,dt_job_completed,input_events,output_events,file_size,latency,execution_time,err_message,stage,stage_status) VALUES ('SYSTEM_LP',?,'VIDEO_STREAMING',NULL,'SUBMITTED',?,?,NULL,NULL,NULL,NULL,0,dateOf(now()),NULL,NULL,0,0,0,0,0,NULL,NULL,NULL);";
        String requestId = contentId + "_" + pkgVersion;

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("content_id", contentId);
        requestMap.put("artifactUrl", artifactUrl);
        requestMap.put("channel", channel);
        String requestData = mapper.writeValueAsString(requestMap);

        Session session = CassandraConnector.getSession("lpa");
        com.datastax.driver.core.PreparedStatement statement = session.prepare(query);
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(requestId, requestData, artifactUrl));
        } catch (Exception e) {
            TelemetryManager.error("Error while pushing video streaming job request",e);
            throw new ServerException("ERR_VIDEO_STREAMING_REQUEST","Error while pushing video streaming job request",e);
        }
    }

}
