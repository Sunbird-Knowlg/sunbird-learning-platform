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
 * @author mahesh
 *
 */
public abstract class AbstractCassandraStore {

	private String keyspace = null;
	private String table = null;
	private boolean index = false;
	private String objectType = null;

	protected void initialise(String keyspace, String table, String objectType) {
		initialise(keyspace, table, objectType, false);
	}

	protected void initialise(String keyspace, String table, String objectType, boolean index) {
		this.keyspace = keyspace;
		this.table = table;
		this.objectType = objectType;
		this.index = index;
	}

	protected void insert(Object idValue, Map<String, Object> request) {
		try {
			if (null == request || request.isEmpty()) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid record to Insert.");
			}
			Set<String> keySet = request.keySet();
			String query = getPreparedStatement(keySet);
			Object[] objects = getBindObjects(request);
			executeQuery(query, objects);
			logTransactionEvent(CassandraStoreParams.CREATE.name(), idValue, request);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while inserting record", e);
		}
	}

	protected void update(String identifier, Object idValue, Map<String, Object> request) {
		try {
			if (null == request || request.isEmpty()) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid record to Update.");
			}
			Set<String> keySet = request.keySet();
			String query = getUpdateQueryStatement(identifier, keySet);
			String updateQuery = query + Constants.IF_EXISTS;
			Object[] objects = new Object[request.size() + 1];
			Iterator<String> iterator = keySet.iterator();
			int i = 0;
			while (iterator.hasNext()) {
				objects[i++] = request.get(iterator.next());
			}
			objects[i] = idValue;
			executeQuery(updateQuery, objects);
			logTransactionEvent(CassandraStoreParams.UPDATE.name(), idValue, request);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while updating record for id : " + idValue, e);
		}
	}

	protected void delete(String identifier, Object idValue) {
		try {
			if (StringUtils.isBlank(identifier)) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid Identifier to delete");
			}
			Delete.Where delete = QueryBuilder.delete().from(keyspace, table).where(eq(identifier, idValue));
			CassandraConnector.getSession().execute(delete);
			logTransactionEvent(CassandraStoreParams.DELETE.name(), identifier, null);
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while deleting record for id : " + idValue, e);
		}
	}

	protected List<Row> read(String key, Object value) {
		try {
			if (StringUtils.isBlank(key)) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid Identifier to read");
			}
			Select selectQuery = QueryBuilder.select().all().from(keyspace, table);
			Where selectWhere = selectQuery.where();
			Clause clause = QueryBuilder.eq(key, value);
			selectWhere.and(clause);
			ResultSet results = CassandraConnector.getSession().execute(selectQuery);
			return results.all();
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while fetching record for ID : " + value, e);
		}

	}

	protected List<Row> getRecordsByProperty(String propertyName, List<Object> propertyValueList) {
		try {
			if (StringUtils.isBlank(propertyName) || propertyValueList.isEmpty()) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid propertyName to read");
			}
			Select selectQuery = QueryBuilder.select().all().from(keyspace, table);
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

	protected List<Row> getRecordsByProperties(Map<String, Object> propertyMap) {
		try {
			if (null == propertyMap || propertyMap.isEmpty()) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid propertyName to read");
			}

			Select selectQuery = QueryBuilder.select().all().from(keyspace, table);
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

	protected List<Row> getPropertiesValueById(String identifier, String idValue, String... properties) {
		try {
			if (StringUtils.isBlank(identifier)) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid Identifier to read");
			}
			String selectQuery = getSelectStatement(identifier, properties);
			PreparedStatement statement = CassandraConnector.getSession().prepare(selectQuery);
			BoundStatement boundStatement = new BoundStatement(statement);
			ResultSet results = CassandraConnector.getSession().execute(boundStatement.bind(identifier));
			return results.all();
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while fetching properties for ID : " + idValue, e);
		}
	}

	protected List<Row> getAllRecords() {
		try {
			Select selectQuery = QueryBuilder.select().all().from(keyspace, table);
			ResultSet results = CassandraConnector.getSession().execute(selectQuery);
			return results.all();
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while fetching all records", e);
		}
	}

	protected void upsertRecord(Map<String, Object> request) {
		try {
			if (null == request || request.isEmpty()) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid Identifier to read");
			}
			Session session = CassandraConnector.getSession();
			String query = getPreparedStatementFrUpsert(request);
			PreparedStatement statement = session.prepare(query);
			BoundStatement boundStatement = new BoundStatement(statement);
			Object[] objects = getBindObjects(request);
			session.execute(boundStatement.bind(objects));

		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(), "Error while upsert record",
					e);
		}
	}

	/**
	 * @desc This method is used to create prepared statement based on table name
	 *       and column name provided in request
	 * @param keyspaceName
	 *            String (data base keyspace name)
	 * @param tableName
	 *            String
	 * @param map
	 *            is key value pair (key is column name and value is value of
	 *            column)
	 * @return String String
	 */
	private String getPreparedStatement(Set<String> keySet) {
		StringBuilder query = new StringBuilder();
		query.append(Constants.INSERT_INTO + keyspace + Constants.DOT + table + Constants.OPEN_BRACE);
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
	private String getUpdateQueryStatement(String id, Set<String> key) {
		StringBuilder query = new StringBuilder(Constants.UPDATE + keyspace + Constants.DOT + table + Constants.SET);
		query.append(String.join(" = ? ,", key));
		query.append(Constants.EQUAL_WITH_QUE_MARK + Constants.WHERE + id + Constants.EQUAL_WITH_QUE_MARK);
		System.out.println(query.toString());
		return query.toString();
	}

	/**
	 * @desc This method is used to create prepared statement based on table name
	 *       and column name provided
	 * @param keyspaceName
	 *            String (data base keyspace name)
	 * @param tableName
	 *            String
	 * @param map
	 *            is key value pair (key is column name and value is value of
	 *            column)
	 * @return String String
	 */
	private String getPreparedStatementFrUpsert(Map<String, Object> map) {
		StringBuilder query = new StringBuilder();
		query.append(Constants.INSERT_INTO + keyspace + Constants.DOT + table + Constants.OPEN_BRACE);
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
	 * @desc This method is used to create prepared statement based on table name
	 *       and column name provided as varargs
	 * @param keyspaceName
	 *            String (data base keyspace name)
	 * @param tableName
	 *            String
	 * @param properties(String
	 *            varargs)
	 * @return String String
	 */
	private String getSelectStatement(String identifier, String... properties) {
		StringBuilder query = new StringBuilder(Constants.SELECT);
		query.append(String.join(",", properties));
		query.append(Constants.FROM + keyspace + Constants.DOT + table + Constants.WHERE + identifier + Constants.EQUAL
				+ " ?; ");
		return query.toString();
	}

	/**
	 * 
	 * @param query
	 * @param objects
	 * @return
	 */
	private ResultSet executeQuery(String query, Object... objects) {
		Session session = CassandraConnector.getSession();
		PreparedStatement statement = session.prepare(query);
		BoundStatement boundStatement = new BoundStatement(statement);
		return session.execute(boundStatement.bind(objects));
	}

	private Object[] getBindObjects(Map<String, Object> request) {
		Set<String> keySet = request.keySet();
		Iterator<String> iterator = keySet.iterator();
		Object[] objects = new Object[keySet.size()];
		int i = 0;
		while (iterator.hasNext()) {
			objects[i++] = request.get(iterator.next());
		}
		return objects;
	}

	private void logTransactionEvent(String operation, Object identifier, Map<String, Object> map) {
		if (index) {
			if (null == map && !StringUtils.equalsIgnoreCase(operation, CassandraStoreParams.DELETE.name())) {
				TelemetryManager.log("Returning null as the map is is null", map);
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid request for LoggingTransactionEvent");
			} else {
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
				dataMap.put(CompositeSearchParams.nodeType.name(), CassandraStoreParams.EXTERNAL.name());
				dataMap.put(CompositeSearchParams.userId.name(), CassandraStoreParams.ANONYMOUS.name());
				dataMap.put(CompositeSearchParams.objectType.name(), objectType);
				dataMap.put(CompositeSearchParams.index.name(), index);
				dataMap.put(CompositeSearchParams.audit.name(), false);
				dataMap.put(CompositeSearchParams.ets.name(), System.currentTimeMillis());
				dataMap.put(CompositeSearchParams.createdOn.name(), DateUtils.format(new Date()));
				message.add(dataMap);
				LogAsyncGraphEvent.pushMessageToLogger(message);
			}
		}
	}
}
