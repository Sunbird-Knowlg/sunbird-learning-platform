/**
 * 
 */
package org.ekstep.cassandra.store;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.cassandra.connector.util.CassandraConnectorStoreParam;
import org.ekstep.common.Platform;
import org.ekstep.common.enums.CompositeSearchParams;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.common.DateUtils;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.ekstep.telemetry.util.LogAsyncGraphEvent;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Select.Where;

/**
 * @author pradyumna
 *
 */
public class CassandraStoreUtil {

	public static void insert(String keyspaceName, String tableName, Object idValue, Map<String, Object> request) {
		try {
			if (null == request || request.isEmpty()) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid record to Insert.");
			}
			Set<String> keySet = request.keySet();
			String query = getPreparedStatement(keyspaceName, tableName, keySet);
			Session session = CassandraConnector.getSession();
			PreparedStatement statement = session.prepare(query);
			BoundStatement boundStatement = new BoundStatement(statement);
			Iterator<String> iterator = keySet.iterator();
			Object[] array = new Object[keySet.size()];
			int i = 0;
			while (iterator.hasNext()) {
				array[i++] = request.get(iterator.next());
			}
			session.execute(boundStatement.bind(array));
			logTransactionEvent(keyspaceName, tableName, CassandraStoreParams.CREATE.name(), idValue, request);
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while inserting record", e);
		}
	}

	public static void update(String keyspaceName, String tableName, String identifier, Object idValue,
			Map<String, Object> request) {
		try {
			if (null == request || request.isEmpty()) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid record to Update.");
			}
			Session session = CassandraConnector.getSession();
			Set<String> keySet = request.keySet();
			String query = getUpdateQueryStatement(keyspaceName, tableName, identifier, keySet);
			String updateQuery = query + Constants.IF_EXISTS;
			PreparedStatement statement = session.prepare(updateQuery);
			Object[] array = new Object[request.size() + 1];
			Iterator<String> iterator = keySet.iterator();
			int i = 0;
			while (iterator.hasNext()) {
				array[i++] = request.get(iterator.next());
			}
			array[i] = idValue;
			BoundStatement boundStatement = statement.bind(array);
			session.execute(boundStatement);
			logTransactionEvent(keyspaceName, tableName, CassandraStoreParams.UPDATE.name(), idValue, request);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while updating record for id : " + idValue, e);
		}
	}

	public static void delete(String keyspaceName, String tableName, String identifier, Object idValue) {
		try {
			if (StringUtils.isBlank(identifier)) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid Identifier to delete");
			}
			Delete.Where delete = QueryBuilder.delete().from(keyspaceName, tableName).where(eq(identifier, idValue));
			CassandraConnector.getSession().execute(delete);
			logTransactionEvent(keyspaceName, tableName, CassandraStoreParams.DELETE.name(), identifier, null);
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while deleting record for id : " + idValue, e);
		}
	}

	public static List<Row> read(String keyspaceName, String tableName, String identifier, Object idValue) {
		try {
			if (StringUtils.isBlank(identifier)) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid Identifier to read");
			}
			Select selectQuery = QueryBuilder.select().all().from(keyspaceName, tableName);
			Where selectWhere = selectQuery.where();
			Clause clause = QueryBuilder.eq(identifier, idValue);
			selectWhere.and(clause);
			ResultSet results = CassandraConnector.getSession().execute(selectQuery);
			return results.all();
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while fetching record for ID : " + idValue, e);
		}

	}

	public static List<Row> getRecordsByProperty(String keyspaceName, String tableName, String propertyName,
			List<Object> propertyValueList) {
		try {
			if (StringUtils.isBlank(propertyName) || propertyValueList.isEmpty()) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid propertyName to read");
			}
			Select selectQuery = QueryBuilder.select().all().from(keyspaceName, tableName);
			Where selectWhere = selectQuery.where();
			Clause clause = QueryBuilder.in(propertyName, propertyValueList);
			selectWhere.and(clause);
			ResultSet results = CassandraConnector.getSession().execute(selectQuery);
			return results.all();
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while fetching record for Property : " + propertyName, e);
		}
	}

	public static List<Row> getRecordsByProperties(String keyspaceName, String tableName,
			Map<String, Object> propertyMap) {
		try {
			if (null == propertyMap || propertyMap.isEmpty()) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid propertyName to read");
			}

			Select selectQuery = QueryBuilder.select().all().from(keyspaceName, tableName);
			Where selectWhere = selectQuery.where();
			for (Entry<String, Object> entry : propertyMap.entrySet()) {
				if (entry.getValue() instanceof List) {
					Clause clause = QueryBuilder.in(entry.getKey(), entry.getValue());
					selectWhere.and(clause);
				} else {
					Clause clause = QueryBuilder.eq(entry.getKey(), entry.getValue());
					selectWhere.and(clause);
				}
			}
			ResultSet results = CassandraConnector.getSession().execute(selectQuery.allowFiltering());
			return results.all();
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while fetching records", e);
		}
	}

	public static List<Row> getPropertiesValueById(String keyspaceName, String tableName, String identifier,
			String idValue, String... properties) {
		try {
			if (StringUtils.isBlank(identifier)) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid Identifier to read");
			}
			String selectQuery = getSelectStatement(keyspaceName, tableName, identifier, properties);
			PreparedStatement statement = CassandraConnector.getSession().prepare(selectQuery);
			BoundStatement boundStatement = new BoundStatement(statement);
			ResultSet results = CassandraConnector.getSession().execute(boundStatement.bind(identifier));
			return results.all();
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while fetching properties for ID : " + idValue, e);
		}
	}

	public static List<Row> getAllRecords(String keyspaceName, String tableName) {
		try {
			Select selectQuery = QueryBuilder.select().all().from(keyspaceName, tableName);
			ResultSet results = CassandraConnector.getSession().execute(selectQuery);
			return results.all();
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while fetching all records", e);
		}
	}

	public static void upsertRecord(String keyspaceName, String tableName, Map<String, Object> request) {
		try {
			if (null == request || request.isEmpty()) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid Identifier to read");
			}
			Session session = CassandraConnector.getSession();
			String query = getPreparedStatementFrUpsert(keyspaceName, tableName, request);
			PreparedStatement statement = session.prepare(query);
			BoundStatement boundStatement = new BoundStatement(statement);
			Iterator<Object> iterator = request.values().iterator();
			Object[] array = new Object[request.keySet().size()];
			int i = 0;
			while (iterator.hasNext()) {
				array[i++] = iterator.next();
			}
			session.execute(boundStatement.bind(array));

		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(), "Error while upsert record",
					e);
		}
	}

	/**
	 * @desc This method is used to create prepared statement based on table
	 *       name and column name provided in request
	 * @param keyspaceName
	 *            String (data base keyspace name)
	 * @param tableName
	 *            String
	 * @param map
	 *            is key value pair (key is column name and value is value of
	 *            column)
	 * @return String String
	 */
	private static String getPreparedStatement(String keyspaceName, String tableName, Set<String> keySet) {
		StringBuilder query = new StringBuilder();
		query.append(Constants.INSERT_INTO + keyspaceName + Constants.DOT + tableName + Constants.OPEN_BRACE);
		query.append(String.join(",", keySet) + Constants.VALUES_WITH_BRACE);
		StringBuilder commaSepValueBuilder = new StringBuilder();
		for (int i = 0; i < keySet.size(); i++) {
			commaSepValueBuilder.append(Constants.QUE_MARK);
			if (i != keySet.size() - 1) {
				commaSepValueBuilder.append(Constants.COMMA);
			}
		}
		query.append(commaSepValueBuilder + ")" + Constants.IF_NOT_EXISTS);
		return query.toString();

	}

	/**
	 * @desc This method is used to create update query statement based on table
	 *       name and column name provided
	 * @param keyspaceName
	 *            String (data base keyspace name)
	 * @param tableName
	 *            String
	 * @param map
	 *            Map<String, Object>
	 * @return String String
	 */
	private static String getUpdateQueryStatement(String keyspaceName, String tableName, String id, Set<String> key) {
		StringBuilder query = new StringBuilder(
				Constants.UPDATE + keyspaceName + Constants.DOT + tableName + Constants.SET);
		query.append(String.join(" = ? ,", key));
		query.append(Constants.EQUAL_WITH_QUE_MARK + Constants.WHERE + id + Constants.EQUAL_WITH_QUE_MARK);
		return query.toString();
	}

	/**
	 * @desc This method is used to create prepared statement based on table
	 *       name and column name provided
	 * @param keyspaceName
	 *            String (data base keyspace name)
	 * @param tableName
	 *            String
	 * @param map
	 *            is key value pair (key is column name and value is value of
	 *            column)
	 * @return String String
	 */
	private static String getPreparedStatementFrUpsert(String keyspaceName, String tableName, Map<String, Object> map) {
		StringBuilder query = new StringBuilder();
		query.append(Constants.INSERT_INTO + keyspaceName + Constants.DOT + tableName + Constants.OPEN_BRACE);
		Set<String> keySet = map.keySet();
		query.append(String.join(",", keySet) + Constants.VALUES_WITH_BRACE);
		StringBuilder commaSepValueBuilder = new StringBuilder();
		for (int i = 0; i < keySet.size(); i++) {
			commaSepValueBuilder.append(Constants.QUE_MARK);
			if (i != keySet.size() - 1) {
				commaSepValueBuilder.append(Constants.COMMA);
			}
		}
		query.append(commaSepValueBuilder + Constants.CLOSING_BRACE);
		return query.toString();

	}

	/**
	 * @desc This method is used to create prepared statement based on table
	 *       name and column name provided as varargs
	 * @param keyspaceName
	 *            String (data base keyspace name)
	 * @param tableName
	 *            String
	 * @param properties(String
	 *            varargs)
	 * @return String String
	 */
	public static String getSelectStatement(String keyspaceName, String tableName, String identifier,
			String... properties) {
		StringBuilder query = new StringBuilder(Constants.SELECT);
		query.append(String.join(",", properties));
		query.append(Constants.FROM + keyspaceName + Constants.DOT + tableName + Constants.WHERE + identifier
				+ Constants.EQUAL + " ?; ");
		return query.toString();

	}

	public static void logTransactionEvent(String keyspace, String tableName, String operation, Object identifier,
			Map<String, Object> map) {

		try {
			boolean index = Platform.config
					.hasPath(keyspace + Constants.DOT + tableName + Constants.DOT + CassandraStoreParams.index.name())
							? Platform.config.getBoolean(keyspace + Constants.DOT + tableName + Constants.DOT
									+ CassandraStoreParams.index.name())
							: false;
			if (index) {
				if (null == map && !StringUtils.equalsIgnoreCase(operation, CassandraStoreParams.DELETE.name())) {
					TelemetryManager.log("Returning null as the map is is null", map);
					throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
							"Invalid request for LoggingTransactionEvent");
				} else {

					if (Platform.config.hasPath(keyspace + Constants.DOT + tableName + Constants.DOT
							+ CassandraStoreParams.object_type.name())) {
						String objectType = Platform.config.getString(keyspace + Constants.DOT + tableName
								+ Constants.DOT + CassandraStoreParams.object_type.name());
						Map<String, Object> dataMap = new HashMap<String, Object>();
						Map<String, Object> transactionMap = new HashMap<String, Object>();
						Map<String, Object> propertiesMap = new HashMap<String, Object>();
						List<Map<String, Object>> message = new ArrayList<Map<String, Object>>();
						for (Map.Entry<String, Object> entry : map.entrySet()) {
							Map<String, Object> valueMap = new HashMap<String, Object>();
							valueMap.put("ov", null);
							valueMap.put("nv", entry.getValue());
							TelemetryManager.log("Adding propertiesMap to log kafka message", valueMap);
							propertiesMap.put(entry.getKey(), valueMap);
						}
						transactionMap.put(CompositeSearchParams.properties.name(), propertiesMap);
						dataMap.put(CompositeSearchParams.transactionData.name(), transactionMap);
						dataMap.put(CompositeSearchParams.nodeUniqueId.name(), identifier);
						dataMap.put(CompositeSearchParams.requestId.name(), null);
						dataMap.put(CompositeSearchParams.operationType.name(), operation);
						dataMap.put(CompositeSearchParams.label.name(), "");
						dataMap.put(CompositeSearchParams.nodeType.name(), CassandraStoreParams.EXTERNAL.name());
						dataMap.put(CompositeSearchParams.userId.name(), CassandraStoreParams.ANONYMOUS.name());
						dataMap.put(CompositeSearchParams.objectType.name(), objectType);
						dataMap.put(CompositeSearchParams.index.name(), index);
						dataMap.put(CompositeSearchParams.audit.name(), false);
						dataMap.put(CompositeSearchParams.ets.name(), System.currentTimeMillis());
						dataMap.put(CompositeSearchParams.createdOn.name(), DateUtils.format(new Date()));
						TelemetryManager.log("Adding dataMap to list", dataMap);
						message.add(dataMap);
						TelemetryManager.log("Logging event to kafka on body changes" + message);
						LogAsyncGraphEvent.pushMessageToLogger(message);
					} else {
						throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
								"Object Type not defined for the table to be indexed");
					}
				}
			}
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while  Logging TransactionEvent", e);
		}

	}

}
