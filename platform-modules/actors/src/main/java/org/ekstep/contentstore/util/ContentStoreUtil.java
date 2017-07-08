package org.ekstep.contentstore.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.searchindex.util.LogAsyncGraphEvent;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.enums.CompositeSearchParams;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;

public class ContentStoreUtil {

	private static final String PROPERTY_SUFFIX = "__txt";
	
	static ObjectMapper mapper = new ObjectMapper();
	static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	
	/** The Logger object. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	public static void updateContentBody(String contentId, String body) {
		updateContentProperty(contentId, "body", body);
	}

	public static String getContentBody(String contentId) {
		return getContentProperty(contentId, "body");
	}

	public static String getContentProperty(String contentId, String property) {
		LOGGER.log("GetContentProperty | Content: " + contentId + " | Property: " + property);
		Session session = CassandraConnector.getSession();
		String query = getSelectQuery(property);
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
					"Invalid property name. Please specify a valid property name");
		LOGGER.log("GetContentProperty | Query: " , query);
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
			LOGGER.log("Error! Executing Get Content Property.",e.getMessage(),  e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error fetching property from Content Store.");
		}
		return null;
	}
	
	public static Map<String, Object> getContentProperties(String contentId, List<String> properties) {
		LOGGER.log("GetContentProperties | Content: " + contentId + " | Properties: " + properties);
		Session session = CassandraConnector.getSession();
		String query = getSelectQuery(properties);
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
					"Invalid properties list. Please specify a valid list of property names");
		LOGGER.log("GetContentProperties | Query: " , query);
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
			LOGGER.log("Error! Executing Get Content Property.", e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error fetching property from Content Store.");
		}
		return null;
	}
	
	public static void updateContentProperty(String contentId, String property, String value) {
		LOGGER.log("UpdateContentProperty | Content: " + contentId + " | Property: " + property + " - Value: " + value);
		Session session = CassandraConnector.getSession();
		String query = getUpdateQuery(property);
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
					"Invalid property name. Please specify a valid property name");
		LOGGER.log("UpdateContentProperty | Query: " , query);
		PreparedStatement ps = session.prepare(query);
		BoundStatement bound = ps.bind(value, contentId);
		try {
			session.execute(bound);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(property, value);
			List<Map<String, Object>> nodeMessage = createKafkaMessage(contentId, map);
			LOGGER.log("Logging event to kafka on body changes" + nodeMessage);
			LogAsyncGraphEvent.pushMessageToLogger(nodeMessage);
		} catch (Exception e) {
			LOGGER.log("Error! Executing Update Content Property.", e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error updating property in Content Store.");
		}
	}

	public static void updateContentProperties(String contentId, Map<String, Object> map) {
		LOGGER.log("UpdateContentProperties | Content: " + contentId + " | Properties: " + map);
		Session session = CassandraConnector.getSession();
		if (null == map || map.isEmpty())
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_VALUES.name(),
					"Invalid property values. Please specify valid property values");
		String query = getUpdateQuery(map.keySet());
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_VALUES.name(),
					"Invalid property values. Please specify valid property values");
		LOGGER.log("UpdateContentProperties | Query: " , query);
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
			List<Map<String, Object>> nodeMessage = createKafkaMessage(contentId, map);
			LOGGER.log("Logging event to kafka on body change" , nodeMessage);
			LogAsyncGraphEvent.pushMessageToLogger(nodeMessage);
		} catch (Exception e) {
			LOGGER.log("Error! Executing Update Content Property.", e.getMessage(), e);
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
	
	private static List<Map<String, Object>> createKafkaMessage(String contentId, Map<String,Object> map) {
		if(null == map){
			LOGGER.log("Returning null as the map is is null" , map);
			return null;
		}
		else{	
			Map<String,Object> dataMap = new HashMap<String,Object>();
			Map<String,Object> transactionMap = new HashMap<String,Object>();
			Map<String,Object> propertiesMap = new HashMap<String, Object>();
			List<Map<String,Object>> listMap = new ArrayList<Map<String,Object>>();
			for(Map.Entry<String,Object> entry : map.entrySet()){
					Map<String,Object> valueMap = new HashMap<String,Object>();
					valueMap.put("ov", null);
					valueMap.put("nv", entry.getValue());
					LOGGER.log("Adding propertiesMap to log kafka message" , valueMap);
					propertiesMap.put(entry.getKey(), valueMap);
			}
			transactionMap.put(CompositeSearchParams.properties.name(), propertiesMap);
			dataMap.put(CompositeSearchParams.transactionData.name(), transactionMap);
			dataMap.put(CompositeSearchParams.nodeUniqueId.name(), contentId);
			dataMap.put(CompositeSearchParams.requestId.name(), null);
			dataMap.put(CompositeSearchParams.operationType.name(), "UPDATE");
			dataMap.put(CompositeSearchParams.label.name(), "");
			dataMap.put(CompositeSearchParams.graphId.name(), "domain");
			dataMap.put(CompositeSearchParams.nodeType.name(), "DATA_NODE");
			dataMap.put(CompositeSearchParams.userId.name(), "ANONYMOUS");
			dataMap.put(CompositeSearchParams.objectType.name(), "Content");
			dataMap.put(CompositeSearchParams.index.name(), false);
			dataMap.put(CompositeSearchParams.audit.name(), false);
			dataMap.put(CompositeSearchParams.ets.name(), System.currentTimeMillis());
			dataMap.put(CompositeSearchParams.createdOn.name(), df.format(new Date()));
			LOGGER.log("Adding dataMap to list" , dataMap);
			listMap.add(dataMap);
			return listMap;
		}
	}
}
