package com.ilimi.dialcode.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.contentstore.util.ContentStoreParams;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Batch;
import com.ilimi.cassandra.connector.util.CassandraConnector;
import com.ilimi.cassandra.connector.util.CassandraConnectorStoreParam;
import com.ilimi.common.Platform;
import com.ilimi.common.enums.CompositeSearchParams;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.LoggerEnum;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.common.util.LogAsyncGraphEvent;
import com.ilimi.dialcode.enums.DialCodeEnum;

import iot.jcypher.query.ast.separate.SeparateExpression;

/**
 * 
 * @author gauraw
 *
 */
public class DialCodeStoreUtil {

	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private static String keyspaceName = "";
	private static String keyspaceTable = "";
	
	// get keyspace name
	public static String getKeyspaceName() {
		if (StringUtils.isBlank(keyspaceName) && Platform.config.hasPath("dialcode.keyspace.name"))
			return Platform.config.getString("dialcode.keyspace.name");
		else 
			return keyspaceName;
	}
	
	// get table name
	public static String getKeyspaceTable() {
		if (StringUtils.isBlank(keyspaceTable) && Platform.config.hasPath("dialcode.keyspace.table")) 
			return Platform.config.getString("dialcode.keyspace.table");
		else 
			return keyspaceTable;
	}
	
	//get dialcode index
	public static Integer getDialCodeIndex(){
		Integer dialcode_index=0;
		try{
		Session session=CassandraConnector.getSession();
		String query="select max(dialcode_index) as dialcode_index from "+getKeyspaceName()+"."+getKeyspaceTable()+";";
		ResultSet rs=session.execute(query);
		if (null != rs) {
			while (rs.iterator().hasNext()) {
				Row row = rs.iterator().next();
				dialcode_index=row.getInt("dialcode_index");
			}
		}
		}catch(Exception e){
			PlatformLogger.log("Exception Occured while reading index of Dial Code : ", e.getMessage(), e);
		}
		return dialcode_index;
	}
	
	@SuppressWarnings("unused")
	public static void saveDialCode(String channel, String publisher, String batchCode, Map<Integer,String> codeMap) throws Exception{
		try{
			Session session=CassandraConnector.getSession();
			StringBuilder insertQuery = new StringBuilder();
			insertQuery.append("BEGIN BATCH ");
			BatchStatement batch=new BatchStatement();
			Statement stmt=null;
			for(Integer key:codeMap.keySet()){
				StringBuilder sb = new StringBuilder();
				sb.append("insert into "+getKeyspaceName()+"."+getKeyspaceTable());
				sb.append("(identifier,batchcode,channel,count,dialcode_index,generated_on,publisher,status) values(");
				sb.append("'"+codeMap.get(key)+"',");
				sb.append("'"+batchCode+"',");
				sb.append("'"+channel+"',");
				sb.append("0,");
				sb.append(key+",");
				sb.append("dateOf(now()),");	
				sb.append("'"+publisher+"',");
				sb.append("'"+DialCodeEnum.Draft.name()+"');");
				insertQuery.append(sb.toString());
			}
			
			insertQuery.append(" APPLY BATCH;");
			
			session.execute(insertQuery.toString());
			System.out.println(insertQuery.toString());
			
			System.out.println("DIAL Code Stored Successfully..");
			
		}catch(Exception e){
			System.out.println("Exception::::::"+e.getMessage());
			e.printStackTrace();
			PlatformLogger.log("Exception Occured while inserting Dial Codes to Cassandra : ", e.getMessage(), e);
		}
	}
	
	/*commented
	
	// get insert query
	private static String getInsertQuery(Map<String, String> scriptMap) {
		StringBuilder sb = new StringBuilder();
		Set<String> properties = scriptMap.keySet();
		if (null != properties && !properties.isEmpty()) {
			sb.append("INSERT INTO ").append(getKeyspaceName()).append(".").append(getKeyspaceTable()).append(" ");
			StringBuilder insertFields = new StringBuilder();
			StringBuilder insertValues = new StringBuilder();
			insertFields.append("(");
			insertValues.append("(");
			for (String property : properties) {
				if (null != property && StringUtils.isBlank(property))
					throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(), "Invalid property name. Please specify a valid property name");
				
				if(null != (String)scriptMap.get(property)) {
					insertFields.append(property.trim()).append(", ");
					insertValues.append("'").append(scriptMap.get(property)).append("', ");
				}
			}
			sb.append(StringUtils.removeEnd(insertFields.toString(), ", ")).append(")")
				.append(" VALUES ")
				.append(StringUtils.removeEnd(insertValues.toString(), ", ")).append(")");
		}
		return sb.toString();
	}
	
	private static String getSelectQuery(String property) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(property)) {
			sb.append("select blobAsText(").append(property).append(") as ");
			sb.append(property.trim()).append(PROPERTY_SUFFIX).append(" from " + getKeyspaceName() +"."+keyspaceTable  +  " where content_id = ?");
			PlatformLogger.log("Fetched keyspace names for get Operation: " + getKeyspaceName() + keyspaceTable, null, LoggerEnum.INFO.name());
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
			sb.append(" from " + getKeyspaceName() +"."+keyspaceTable + " where content_id = ?");
		}
		return sb.toString();
	}
	
	private static String getUpdateQuery(String property) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(property)) {
			sb.append("UPDATE " + getKeyspaceName() +"."+keyspaceTable + " SET last_updated_on = dateOf(now()), ");
			sb.append(property.trim()).append(" = textAsBlob(?) where content_id = ?");
			PlatformLogger.log("Fetched keyspace names for update Operation: " + getKeyspaceName() + keyspaceTable, null, LoggerEnum.INFO.name());
		}
		return sb.toString();
	}
    
	public static void saveDialCode(String channel, String publisher, String[] dial){}
	public static void updateContentBody(String contentId, String body) {
		updateContentProperty(contentId, "body", body);
	}

	public static String getContentBody(String contentId) {
		return getContentProperty(contentId, "body");
	}

	public static String getContentProperty(String contentId, String property) {
		PlatformLogger.log("GetContentProperty | Content: " + contentId + " | Property: " + property);
		Session session = CassandraConnector.getSession();
		String query = getSelectQuery(property);
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
					"Invalid property name. Please specify a valid property name");
		PlatformLogger.log("GetContentProperty | Query: " , query);
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
			PlatformLogger.log("Error! Executing Get Content Property.",e.getMessage(),  e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error fetching property from Content Store.");
		}
		return null;
	}
	
	public static Map<String, Object> getContentProperties(String contentId, List<String> properties) {
		PlatformLogger.log("GetContentProperties | Content: " + contentId + " | Properties: " + properties);
		Session session = CassandraConnector.getSession();
		String query = getSelectQuery(properties);
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
					"Invalid properties list. Please specify a valid list of property names");
		PlatformLogger.log("GetContentProperties | Query: " , query);
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
			PlatformLogger.log("Error! Executing Get Content Property.", e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error fetching property from Content Store.");
		}
		return null;
	}
	
	public static void updateContentProperty(String contentId, String property, String value) {
		PlatformLogger.log("UpdateContentProperty | Content: " + contentId + " | Property: " + property + " - Value: " + value);
		Session session = CassandraConnector.getSession();
		String query = getUpdateQuery(property);
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
					"Invalid property name. Please specify a valid property name");
		PlatformLogger.log("UpdateContentProperty | Query: " , query);
		PreparedStatement ps = session.prepare(query);
		BoundStatement bound = ps.bind(value, contentId);
		try {
			session.execute(bound);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(property, value);
			List<Map<String, Object>> nodeMessage = createKafkaMessage(contentId, map);
			PlatformLogger.log("Logging event to kafka on body changes" + nodeMessage);
			LogAsyncGraphEvent.pushMessageToLogger(nodeMessage);
		} catch (Exception e) {
			PlatformLogger.log("Error! Executing Update Content Property.", e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error updating property in Content Store.");
		}
	}

	public static void updateContentProperties(String contentId, Map<String, Object> map) {
		PlatformLogger.log("UpdateContentProperties | Content: " + contentId + " | Properties: " + map);
		Session session = CassandraConnector.getSession();
		if (null == map || map.isEmpty())
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_VALUES.name(),
					"Invalid property values. Please specify valid property values");
		String query = getUpdateQuery(map.keySet());
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_VALUES.name(),
					"Invalid property values. Please specify valid property values");
		PlatformLogger.log("UpdateContentProperties | Query: " , query);
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
			PlatformLogger.log("Logging event to kafka on body change" , nodeMessage);
			LogAsyncGraphEvent.pushMessageToLogger(nodeMessage);
		} catch (Exception e) {
			PlatformLogger.log("Error! Executing Update Content Property.", e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error updating property in Content Store.");
		}
	}

	
	
	private static String getUpdateQuery(Set<String> properties) {
		StringBuilder sb = new StringBuilder();
		if (null != properties && !properties.isEmpty()) {
			sb.append("UPDATE " + getKeyspaceName() +"."+keyspaceTable + " SET last_updated_on = dateOf(now()), ");
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
			PlatformLogger.log("Returning null as the map is is null" , map);
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
					PlatformLogger.log("Adding propertiesMap to log kafka message" , valueMap);
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
			PlatformLogger.log("Adding dataMap to list" , dataMap);
			listMap.add(dataMap);
			return listMap;
		}
	}*/
}
