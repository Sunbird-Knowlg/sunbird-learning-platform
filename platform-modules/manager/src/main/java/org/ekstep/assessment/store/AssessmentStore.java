/**
 * 
 */
package org.ekstep.assessment.store;

import java.util.List;

import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

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
		String query = getInsertQuery(questionId, body);
		Session session = CassandraConnector.getSession();
		session.execute(query);
	}

	public String read(String questionId) throws Exception {
		String bodyData = "";
		try {
			String query = getSelectQuery(questionId);
			Session session = CassandraConnector.getSession();
			ResultSet rs = session.execute(query);
			List<Row> rows = rs.all();
			Row row = rows.get(0);
			bodyData = row.getString("data");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResourceNotFoundException("ERR_ASSESSMENT_DATA", "Data Not Found.");
		}
		return bodyData;
	}

	public void update(String questionId, String body) throws Exception {
		String query = getUpdateQuery(questionId, body);
		Session session = CassandraConnector.getSession();
		session.execute(query);
	}

	private static String getInsertQuery(String questionId, String body) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into " + keyspace + "." + table + "(question_id,last_updated_on,body) values('" + questionId
				+ "',toTimestamp(now()),textAsBlob('");
		sb.append(body);
		sb.append("'));");
		return sb.toString();
	}

	private static String getSelectQuery(String questionId) {
		StringBuilder sb = new StringBuilder();
		sb.append("select blobAsText(body) as data from " + keyspace + "." + table + " where question_id='");
		sb.append(questionId);
		sb.append("';");
		return sb.toString();
	}

	private static String getUpdateQuery(String questionId, String body) {
		StringBuilder sb = new StringBuilder();
		sb.append("update " + keyspace + "." + table + " set body=textAsBlob('");
		sb.append(body);
		sb.append("'), last_updated_on=toTimestamp(now()) where question_id='");
		sb.append(questionId);
		sb.append("';");
		return sb.toString();
	}

}
