package org.sunbird.learning.contentstore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.cassandra.connector.util.CassandraConnector;
import org.sunbird.cassandra.store.CassandraStore;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.searchindex.util.CompositeSearchConstants;
import org.sunbird.telemetry.logger.TelemetryManager;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ContentStore extends CassandraStore {

	private static final String PROPERTY_SUFFIX = "__txt";
	private static ObjectMapper mapper = new ObjectMapper();
	private static final String table = "content_data";


	public ContentStore() {
		super();
		String keyspace = Platform.config.hasPath("content.keyspace.name")
				? Platform.config.getString("content.keyspace.name")
				: "content_store";

		boolean index = Platform.config.hasPath("content.index") ? Platform.config.getBoolean("content.index") : false;
		String objectType = "Content";
		initialise(keyspace, table, objectType, index);
		nodeType = CompositeSearchConstants.NODE_TYPE_DATA;
	}

	public void updateContentBody(String contentId, String body) {
		updateContentProperty(contentId, "body", body);
	}
	public void updateContentOldBody(String contentId, String body) {
		updateContentProperty(contentId, "oldBody", body);
	}
	public String getContentBody(String contentId) {
		return getContentProperty(contentId, "body");
	}

	public String getContentProperty(String contentId, String property) {
		TelemetryManager.log("GetContentProperty | Content: " + contentId + " | Property: " + property);
		Session session = CassandraConnector.getSession();
		String query = getSelectQuery(property);
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
					"Invalid property name. Please specify a valid property name");
		PreparedStatement ps = session.prepare(query);
		BoundStatement bound = ps.bind(contentId);
		try {
			ResultSet rs = session.execute(bound);
			if (null != rs) {
				while (rs.iterator().hasNext()) {
					Row row = rs.iterator().next();
					String value = row.getString(property + PROPERTY_SUFFIX);
					return value;
				}
			}
		} catch (Exception e) {
			TelemetryManager.error("Error! Executing get content property: " + e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error fetching property from Content Store.");
		}
		return null;
	}

	public Map<String, Object> getContentProperties(String contentId, List<String> properties) {
		TelemetryManager.log("GetContentProperties | Content: " + contentId + " | Properties: " + properties);
		Session session = CassandraConnector.getSession();
		String query = getSelectQuery(properties);
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
					"Invalid properties list. Please specify a valid list of property names");
		PreparedStatement ps = session.prepare(query);
		BoundStatement bound = ps.bind(contentId);
		try {
			ResultSet rs = session.execute(bound);
			if (null != rs) {
				Map<String, Object> map = new HashMap<String, Object>();
				while (rs.iterator().hasNext()) {
					Row row = rs.iterator().next();
					for (String prop : properties) {
						String value = row.getString(prop + PROPERTY_SUFFIX);
						map.put(prop, value);
					}
					return map;
				}
			}
		} catch (Exception e) {
			TelemetryManager.error("Error! Executing get content property: " + e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error fetching property from Content Store.");
		}
		return null;
	}

	public void updateContentProperty(String contentId, String property, String value) {
		TelemetryManager.log(
				"UpdateContentProperty | Content: " + contentId + " | Property: " + property + " - Value: " + value);
		Session session = CassandraConnector.getSession();
		String query = getUpdateQuery(property);
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
					"Invalid property name. Please specify a valid property name");
		PreparedStatement ps = session.prepare(query);
		BoundStatement bound = ps.bind(value, contentId);
		try {
			session.execute(bound);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(property, value);
			logTransactionEvent("UPDATE", contentId, map);
		} catch (Exception e) {
			TelemetryManager.error("Error! Executing update content property:" + e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error updating property in Content Store.");
		}
	}

	public void updateContentProperties(String contentId, Map<String, Object> map) {
		TelemetryManager.log("UpdateContentProperties | Content: " + contentId + " | Properties: " + map);
		Session session = CassandraConnector.getSession();
		if (null == map || map.isEmpty())
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_VALUES.name(),
					"Invalid property values. Please specify valid property values");
		String query = getUpdateQuery(map.keySet());
		PreparedStatement ps = session.prepare(query);
		Object[] values = new Object[map.size() + 1];
		try {
			int i = 0;
			for (Entry<String, Object> entry : map.entrySet()) {
				String value = "";
				if (null == entry.getValue()) {
					value = "";
				} else if (entry.getValue() instanceof String) {
					value = (String) entry.getValue();
				} else {
					value = mapper.writeValueAsString(entry.getValue());
				}
				values[i] = value;
				i += 1;
			}
			values[i] = contentId;
			BoundStatement bound = ps.bind(values);

			session.execute(bound);
			logTransactionEvent("UPDATE", contentId, map);
		} catch (Exception e) {
			TelemetryManager.error("Error! Executing update content property: " + e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error updating property in Content Store.");
		}
	}

	private String getSelectQuery(String property) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(property)) {
			sb.append("select blobAsText(").append(property).append(") as ");
			sb.append(property.trim()).append(PROPERTY_SUFFIX)
					.append(" from " + getKeyspace() + "." + getTable() + " where content_id = ?");
		}
		return sb.toString();
	}

	private String getSelectQuery(List<String> properties) {
		StringBuilder sb = new StringBuilder();
		if (null != properties && !properties.isEmpty()) {
			sb.append("select ");
			StringBuilder selectFields = new StringBuilder();
			for (String property : properties) {
				if (StringUtils.isBlank(property))
					if (StringUtils.isBlank(property))
						throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
								"Invalid property name. Please specify a valid property name");
				selectFields.append("blobAsText(").append(property).append(") as ");
				selectFields.append(property.trim()).append(PROPERTY_SUFFIX).append(", ");
			}
			sb.append(StringUtils.removeEnd(selectFields.toString(), ", "));
			sb.append(" from " + getKeyspace() + "." + getTable() + " where content_id = ?");
		}
		return sb.toString();
	}

	private String getUpdateQuery(String property) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(property)) {
			sb.append("UPDATE " + getKeyspace() + "." + getTable() + " SET last_updated_on = dateOf(now()), ");
			sb.append(property.trim()).append(" = textAsBlob(?) where content_id = ?");
		}
		return sb.toString();
	}

	private String getUpdateQuery(Set<String> properties) {
		StringBuilder sb = new StringBuilder();
		if (null != properties && !properties.isEmpty()) {
			sb.append("UPDATE " + getKeyspace() + "." + getTable() + " SET last_updated_on = dateOf(now()), ");
			StringBuilder updateFields = new StringBuilder();
			for (String property : properties) {
				if (StringUtils.isBlank(property))
					if (StringUtils.isBlank(property))
						throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
								"Invalid property name. Please specify a valid property name");
				updateFields.append(property.trim()).append(" = textAsBlob(?), ");
			}
			sb.append(StringUtils.removeEnd(updateFields.toString(), ", "));
			sb.append(" where content_id = ?");
		}
		return sb.toString();
	}
	
	public void updateExternalLink(String contentId, List<Map<String, Object>> externalLinks) {
        try {
            String query = "UPDATE " + getKeyspace() + "." + getTable() + " SET externallink = ? WHERE content_id = ?";
            String externalLinksData = mapper.writeValueAsString(externalLinks);
            Session session = CassandraConnector.getSession();
            PreparedStatement statement = session.prepare(query);
            BoundStatement boundStatement = new BoundStatement(statement);
            session.execute(boundStatement.bind(externalLinksData, contentId));
        } catch (JsonProcessingException e) {
            TelemetryManager.error("Error while updating collection hierarchy for ID" + contentId, e);
        }

    }

}
