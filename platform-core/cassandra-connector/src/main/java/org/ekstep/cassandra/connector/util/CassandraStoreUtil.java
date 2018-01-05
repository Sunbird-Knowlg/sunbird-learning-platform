/**
 * 
 */
package org.ekstep.cassandra.connector.util;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ekstep.common.exception.ServerException;

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

	public static void insert(String keyspaceName, String tableName, Map<String, Object> request) {
		try {
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
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while inserting record", e);
		}
	}

	public static void update(String keyspaceName, String tableName, String id, Object idValue,
			Map<String, Object> request) {
		try {
			Session session = CassandraConnector.getSession();
			Set<String> keySet = request.keySet();
			String query = getUpdateQueryStatement(keyspaceName, tableName, id, keySet);
			String updateQuery = query + Constants.IF_EXISTS;
			PreparedStatement statement = session.prepare(updateQuery);
			Object[] array = new Object[request.size()];
			Iterator<String> iterator = keySet.iterator();
			int i = 0;
			while (iterator.hasNext()) {
				array[i++] = request.get(iterator.next());
			}
			array[i] = idValue;
			BoundStatement boundStatement = statement.bind(array);
			session.execute(boundStatement);
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while updating record for id : " + idValue, e);
		}
	}

	public static void delete(String keyspaceName, String tableName, String id, Object idValue) {
		try {
			Delete.Where delete = QueryBuilder.delete().from(keyspaceName, tableName).where(eq(id, idValue));
			CassandraConnector.getSession().execute(delete);
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while deleting record for id : " + idValue, e);
		}
	}

	public static List<Row> read(String keyspaceName, String tableName, String id, Object idValue) {
		try {
			Select selectQuery = QueryBuilder.select().all().from(keyspaceName, tableName);
			Where selectWhere = selectQuery.where();
			Clause clause = QueryBuilder.eq(id, idValue);
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

	public static List<Row> getPropertiesValueById(String keyspaceName, String tableName, String id, String idValue,
			String... properties) {
		try {
			Select selectQuery = QueryBuilder.select().all().from(keyspaceName, tableName);
			Where selectWhere = selectQuery.where();
			Clause clause = QueryBuilder.eq(id, idValue);
			selectWhere.and(clause);
			PreparedStatement statement = CassandraConnector.getSession().prepare(selectQuery);
			BoundStatement boundStatement = new BoundStatement(statement);
			ResultSet results = CassandraConnector.getSession().execute(boundStatement.bind(id));
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
	private static String getUpdateQueryStatement(String keyspaceName, String tableName, String id,
			Set<String> key) {
		StringBuilder query = new StringBuilder(
				Constants.UPDATE + keyspaceName + Constants.DOT + tableName + Constants.SET);
		query.append(String.join(" = ? ,", key));
		query.append(Constants.EQUAL_WITH_QUE_MARK + Constants.WHERE + id + Constants.EQUAL_WITH_QUE_MARK);
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


}
