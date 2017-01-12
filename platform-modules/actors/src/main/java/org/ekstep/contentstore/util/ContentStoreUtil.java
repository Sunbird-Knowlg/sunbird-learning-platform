package org.ekstep.contentstore.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.LogHelper;

public class ContentStoreUtil {

	private static final String PROPERTY_SUFFIX = "__txt";
	
	/** The Logger object. */
	private static LogHelper LOGGER = LogHelper.getInstance(ContentStoreUtil.class.getName());

	public static void updateContentBody(String contentId, String body) {
		updateContentProperty(contentId, "body", body);
	}

	public static String getContentBody(String contentId) {
		return getContentProperty(contentId, "body");
	}

	public static String getContentProperty(String contentId, String property) {
		LOGGER.info("GetContentProperty | Content: " + contentId + " | Property: " + property);
		Session session = CassandraConnector.getSession();
		String query = getSelectQuery(property);
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
					"Invalid property name. Please specify a valid property name");
		LOGGER.info("GetContentProperty | Query: " + query);
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
			LOGGER.error("Error! Executing Get Content Property.", e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error fetching property from Content Store.");
		}
		return null;
	}
	
	public static Map<String, Object> getContentProperties(String contentId, List<String> properties) {
		LOGGER.info("GetContentProperties | Content: " + contentId + " | Properties: " + properties);
		Session session = CassandraConnector.getSession();
		String query = getSelectQuery(properties);
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
					"Invalid properties list. Please specify a valid list of property names");
		LOGGER.info("GetContentProperties | Query: " + query);
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
			LOGGER.error("Error! Executing Get Content Property.", e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error fetching property from Content Store.");
		}
		return null;
	}
	
	public static void updateContentProperty(String contentId, String property, String value) {
		LOGGER.info("UpdateContentProperty | Content: " + contentId + " | Property: " + property + " - Value: " + value);
		Session session = CassandraConnector.getSession();
		String query = getUpdateQuery(property);
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
					"Invalid property name. Please specify a valid property name");
		LOGGER.info("UpdateContentProperty | Query: " + query);
		PreparedStatement ps = session.prepare(query);
		BoundStatement bound = ps.bind(value, contentId);
		try {
			session.execute(bound);
		} catch (Exception e) {
			LOGGER.error("Error! Executing Update Content Property.", e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error updating property in Content Store.");
		}
	}
	
	public static void updateContentProperties(String contentId, Map<String, Object> map) {
		LOGGER.info("UpdateContentProperties | Content: " + contentId + " | Properties: " + map);
		Session session = CassandraConnector.getSession();
		if (null == map || map.isEmpty())
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_VALUES.name(),
					"Invalid property values. Please specify valid property values");
		String query = getUpdateQuery(map.keySet());
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_VALUES.name(),
					"Invalid property values. Please specify valid property values");
		LOGGER.info("UpdateContentProperties | Query: " + query);
		PreparedStatement ps = session.prepare(query);
		Object[] values = new Object[map.size() + 1];
		int i = 0;
		for (Entry<String, Object> entry : map.entrySet()) {
			String value = (String) entry.getValue();
			values[i] = value;
			i += 1;
		}
		values[i] = contentId;
		BoundStatement bound = ps.bind(values);
		try {
			session.execute(bound);
		} catch (Exception e) {
			LOGGER.error("Error! Executing Update Content Property.", e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error updating property in Content Store.");
		}
	}

	private static String getSelectQuery(String property) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(property)) {
			sb.append("select blobAsText(").append(property).append(") as ");
			sb.append(property.trim()).append(PROPERTY_SUFFIX).append(" from content_store.content_data where content_id = ?");
		}
		return sb.toString();
	}

	private static String getSelectQuery(List<String> properties) {
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
			sb.append(" from content_store.content_data where content_id = ?");
		}
		return sb.toString();
	}
	
	private static String getUpdateQuery(String property) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(property)) {
			sb.append("UPDATE content_store.content_data SET last_updated_on = dateOf(now()), ");
			sb.append(property.trim()).append(" = textAsBlob(?) where content_id = ?");
		}
		return sb.toString();
	}
	
	private static String getUpdateQuery(Set<String> properties) {
		StringBuilder sb = new StringBuilder();
		if (null != properties && !properties.isEmpty()) {
			sb.append("UPDATE content_store.content_data SET last_updated_on = dateOf(now()), ");
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
}
