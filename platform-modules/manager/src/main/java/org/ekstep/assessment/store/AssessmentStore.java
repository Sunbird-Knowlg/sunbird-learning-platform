/**
 * 
 */
package org.ekstep.assessment.store;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.cassandra.connector.util.CassandraConnectorStoreParam;
import org.ekstep.cassandra.store.Constants;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ServerException;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * @author gauraw
 *
 */
@Component
public class AssessmentStore {

	static String keyspace = "";
	static String table = "";

	public AssessmentStore() {
		keyspace = Platform.config.hasPath("assessment.keyspace.name")
				? Platform.config.getString("assessment.keyspace.name") : "content_store";
		table = Platform.config.hasPath("assessment.keyspace.table")
				? Platform.config.getString("assessment.keyspace.table") : "question_data";
	}

	public void save(String questionId, String body) throws Exception {
		String query = getInsertStatement();
		Object[] objects = new Object[2];
		objects[0] = questionId;
		objects[1] = body;
		executeQuery(query, objects);
	}

	public String read(String questionId) throws Exception {
		String bodyData = "";
		try {
			List<Row> rows = getPropertiesValueById("question_id", questionId, "blobAsText(body) as data");
			Row row = rows.get(0);
			bodyData = row.getString("data");
		} catch (Exception e) {
			throw new ResourceNotFoundException("ERR_ASSESSMENT_DATA", "Data Not Found.");
		}
		return bodyData;
	}

	public void update(String questionId, String body) throws Exception {
		String query = getUpdateStatement();
		Object[] objects = new Object[2];
		objects[0] = body;
		objects[1] = questionId;
		executeQuery(query, objects);
	}

	private List<Row> getPropertiesValueById(String identifier, String idValue, String... properties) {
		try {
			if (StringUtils.isBlank(identifier)) {
				throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
						"Invalid Identifier to read");
			}
			String selectQuery = getSelectStatement(identifier, properties);
			Session session = CassandraConnector.getSession();
			PreparedStatement statement = session.prepare(selectQuery);
			BoundStatement boundStatement = new BoundStatement(statement);
			ResultSet results = CassandraConnector.getSession().execute(boundStatement.bind(idValue));
			return results.all();
		} catch (Exception e) {
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error while fetching properties for ID : " + idValue, e);
		}
	}

	private String getSelectStatement(String identifier, String... properties) {
		StringBuilder query = new StringBuilder(Constants.SELECT + " ");
		query.append(String.join(",", properties));
		query.append(Constants.FROM + keyspace + Constants.DOT + table + Constants.WHERE + identifier + Constants.EQUAL
				+ " ?; ");
		return query.toString();
	}

	private String getInsertStatement() {
		StringBuilder query = new StringBuilder(
				Constants.INSERT_INTO + keyspace + Constants.DOT + table + Constants.OPEN_BRACE);
		query.append("question_id" + Constants.COMMA + "last_updated_on" + Constants.COMMA + "body"
				+ Constants.VALUES_WITH_BRACE);
		query.append(Constants.QUE_MARK + Constants.COMMA + "toTimestamp(now())" + Constants.COMMA);
		query.append("textAsBlob(" + Constants.QUE_MARK + "))" + Constants.IF_NOT_EXISTS);
		return query.toString();
	}

	private String getUpdateStatement() {
		StringBuilder query = new StringBuilder(Constants.UPDATE + keyspace + Constants.DOT + table + Constants.SET);
		query.append(" body=textAsBlob(?)" + Constants.COMMA);
		query.append("last_updated_on=toTimestamp(now())");
		query.append(Constants.WHERE + "question_id" + Constants.EQUAL_WITH_QUE_MARK);
		return query.toString();
	}

	private ResultSet executeQuery(String query, Object... objects) {
		Session session = CassandraConnector.getSession();
		PreparedStatement statement = session.prepare(query);
		BoundStatement boundStatement = new BoundStatement(statement);
		return session.execute(boundStatement.bind(objects));
	}

}
