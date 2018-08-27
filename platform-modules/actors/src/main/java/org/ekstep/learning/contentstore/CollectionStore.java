package org.ekstep.learning.contentstore;

import com.datastax.driver.core.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.cassandra.store.CassandraStore;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ServerException;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.Map;

public class CollectionStore extends CassandraStore {


    private String updateHierarhyQuery;
    private static ObjectMapper mapper = new ObjectMapper();

    public CollectionStore() {
        super();
        String keyspace = Platform.config.hasPath("hierarchy.keyspace.name")
                ? Platform.config.getString("hierarchy.keyspace.name")
                : "hierarchy_store";
        String table = Platform.config.hasPath("content.hierarchy.table")
                ? Platform.config.getString("content.hierarchy.table")
                : "content_hierarchy";
        String objectType = "Content";
        initialise(keyspace, table, objectType, false);
        nodeType = CompositeSearchConstants.NODE_TYPE_DATA;

        updateHierarhyQuery = "UPDATE " + getKeyspace() + "." + getTable() + " SET hierarchy = ? WHERE identifier = ?";
    }


    public void updateContentHierarchy(String contentId, Map<String, Object> hierarchy) {
        try {
            String hierarchyData = mapper.writeValueAsString(hierarchy);
            Session session = CassandraConnector.getSession();
            PreparedStatement statement = session.prepare(updateHierarhyQuery);
            BoundStatement boundStatement = new BoundStatement(statement);
            session.execute(boundStatement.bind(hierarchyData, contentId));
        } catch (JsonProcessingException e) {
            TelemetryManager.error("Error while updating collection hierarchy for ID" + contentId, e);

        }

    }


    public Map<String, Object> getCollectionHierarchy(String contentId) {
        String query = "SELECT hierarchy FROM " + getKeyspace() + "." + getTable() +" WHERE identifier=?";

        Session session = CassandraConnector.getSession();
        PreparedStatement ps = session.prepare(query);
        BoundStatement bound = ps.bind(contentId);
        try {
            ResultSet rs = session.execute(bound);
            if (null != rs) {
                while (rs.iterator().hasNext()) {
                    Row row = rs.iterator().next();
                    String value = row.getString("hierarchy");
                    Map<String, Object> hierarchy = mapper.readValue(value, Map.class);
                    return hierarchy;
                }
            }
        } catch (Exception e) {
            TelemetryManager.error("Error! Executing get collection hierarchy: " + e.getMessage(), e);
            throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
                    "Error fetching hierarchy from hierarchy Store.");
        }
        return null;

    }
}
