package org.ekstep.learning.framework;

import com.datastax.driver.core.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.cassandra.store.CassandraStore;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.learning.contentstore.ContentStoreParams;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * FrameworkStore handles all CRUD Operation into cassandra for Framework Hierarchy.
 * @author Kumar Gauraw
 */
public class FrameworkStore extends CassandraStore {

    private ObjectMapper mapper = new ObjectMapper();

    public FrameworkStore() {
        super();
        String keyspace = Platform.config.hasPath("hierarchy.keyspace.name")
                ? Platform.config.getString("hierarchy.keyspace.name")
                : "hierarchy_store";
        String table = Platform.config.hasPath("framework.hierarchy.table")
                ? Platform.config.getString("framework.hierarchy.table")
                : "framework_hierarchy";
        String objectType = "Framework";
        initialise(keyspace, table, objectType, false);
        nodeType = CompositeSearchConstants.NODE_TYPE_DATA;
    }

    public void saveOrUpdateHierarchy(String frameworkId, Map<String, Object> hierarchy) throws IOException {
        try {
            String query = "UPDATE " + getKeyspace() + "." + getTable() + " SET hierarchy = ? WHERE identifier = ?";
            String hierarchyData = mapper.writeValueAsString(hierarchy);
            Session session = CassandraConnector.getSession();
            PreparedStatement statement = session.prepare(query);
            BoundStatement boundStatement = new BoundStatement(statement);
            session.execute(boundStatement.bind(hierarchyData, frameworkId));
        } catch (JsonProcessingException e) {
            TelemetryManager.error("Error while saving/updating framework hierarchy for Id" + frameworkId, e);
        }

    }


    public String getHierarchy(String frameworkId) {
        String query = "SELECT hierarchy FROM " + getKeyspace() + "." + getTable() + " WHERE identifier=?";

        Session session = CassandraConnector.getSession();
        PreparedStatement ps = session.prepare(query);
        BoundStatement bound = ps.bind(frameworkId);
        try {
            ResultSet rs = session.execute(bound);
            if (null != rs && rs.iterator().hasNext()) {
                Row row = rs.iterator().next();
                return row.getString("hierarchy");
            } else {
                throw new ResourceNotFoundException(ResponseCode.RESOURCE_NOT_FOUND.name(), "Resource not found : " + frameworkId);
            }
        } catch (ResourceNotFoundException re) {
            throw re;
        } catch (Exception e) {
            TelemetryManager.error("Error! Executing get framework hierarchy: " + e.getMessage(), e);
            throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
                    "Error fetching hierarchy from hierarchy Store.", e);


        }
    }

    public void deleteHierarchy(List<String> identifiers) {
        String query = "DELETE FROM " + getKeyspace() + "." + getTable() + " WHERE identifier IN :ids";
        Session session = CassandraConnector.getSession();
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind();
        session.execute(bs.setList("ids", identifiers));
    }
}
