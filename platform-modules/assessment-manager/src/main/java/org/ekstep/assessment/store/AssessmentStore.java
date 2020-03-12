/**
 * 
 */
package org.ekstep.assessment.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.cassandra.connector.util.CassandraConnectorStoreParam;
import org.ekstep.cassandra.store.Constants;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.learning.contentstore.ContentStoreParams;
import org.ekstep.telemetry.logger.TelemetryManager;
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
	private static final String PROPERTY_SUFFIX = "__txt";
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final List<String> assessmentExternalBlobProperties = Arrays.asList("body", "editorstate", "question", "solutions");
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

	public Map<String, Object> getAssessmentProperties(String questionId, List properties) {
		TelemetryManager.log("GetAssessmentProperties | Question: " + questionId + " | Properties: " + properties);
		Session session = CassandraConnector.getSession();
		String query = getSelectQuery(properties);
		if (StringUtils.isBlank(query))
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
					"Invalid properties list. Please specify a valid list of property names");
		PreparedStatement ps = session.prepare(query);
		BoundStatement bound = ps.bind(questionId);
		try {
			ResultSet rs = session.execute(bound);
			if (null != rs) {
				Map<String, Object> map = new HashMap<String, Object>();
				while (rs.iterator().hasNext()) {
					Row row = rs.iterator().next();
					for (Object prop : properties) {
						String value = row.getString(prop + PROPERTY_SUFFIX);
						try {
							if(StringUtils.equalsIgnoreCase("body", (String) prop) || 
							   StringUtils.equalsIgnoreCase("question", (String) prop)) {
								map.put((String) prop, value);
							} else {
								Object deserializedValue = mapper.readTree(value);
								map.put((String) prop, deserializedValue);
							}
						} catch (Exception e) {
							map.put((String) prop, value);
						}
					}
					return map;
				}
			}
		} catch (Exception e) {
			TelemetryManager.error("Error! Executing get content property: " + e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error fetching property from Assessment Store.");
		}
		return null;
	}

	public void update(String questionId, String body) throws Exception {
		String query = getUpdateStatement();
		Object[] objects = new Object[2];
		objects[0] = body;
		objects[1] = questionId;
		executeQuery(query, objects);
	}

	public void updateAssessmentProperties(String questionId, Map<String, Object> map) {
		TelemetryManager.log("UpdateAssessmentProperties | Question: " + questionId + " | Properties: " + map);
		Session session = CassandraConnector.getSession();
		if (null == map || map.isEmpty())
			throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_VALUES.name(),
					"Invalid property values. Please specify valid property values");
		String query = getUpdateQuery(map.keySet());
		PreparedStatement ps = session.prepare(query);
		Object[] values = new Object[map.size() + 1];
		try {
			int i = 0;
			for (Map.Entry<String, Object> entry : map.entrySet()) {
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
			values[i] = questionId;
			BoundStatement bound = ps.bind(values);

			session.execute(bound);
		} catch (Exception e) {
			TelemetryManager.error("Error! Executing update content property: " + e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error updating property in Assesment Store.");
		}
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

	private String getSelectQuery(List<String> properties) {
		StringBuilder sb = new StringBuilder();
		if (null != properties && !properties.isEmpty()) {
			sb.append("select ");
			StringBuilder selectFields = new StringBuilder();
			for (String property : properties) {
				if (StringUtils.isBlank(property))
						throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
								"Invalid property name. Please specify a valid property name");
				selectFields.append("blobAsText(").append(property).append(") as ");
				selectFields.append(property.trim()).append(PROPERTY_SUFFIX).append(", ");
			}
			sb.append(StringUtils.removeEnd(selectFields.toString(), ", "));
			sb.append(" from " + keyspace + Constants.DOT + table + " where question_id = ?");
		}
		return sb.toString();
	}

	private String getUpdateQuery(Set<String> properties) {
		StringBuilder sb = new StringBuilder();
		if (null != properties && !properties.isEmpty()) {
			sb.append("UPDATE " + keyspace + "." + table + " SET last_updated_on = dateOf(now()), ");
			StringBuilder updateFields = new StringBuilder();
			for (String property : properties) {
				if (StringUtils.isBlank(property))
					throw new ClientException(ContentStoreParams.ERR_INVALID_PROPERTY_NAME.name(),
							"Invalid property name. Please specify a valid property name");
				updateFields.append(property.toLowerCase().trim()).append(" = textAsBlob(?), ");
			}
			sb.append(StringUtils.removeEnd(updateFields.toString(), ", "));
			sb.append(" where question_id = ?");
		}
		return sb.toString();
	}

	public Map<String, Object> getItems(List<String> identifiers, List<String> properties) {
		TelemetryManager.log("Bulk Get Items for identifiers: " + identifiers + " | Properties: " + properties);
		Session session = CassandraConnector.getSession();
		List<String> propertiesTofetch = new ArrayList<String>(properties);
		
		assessmentExternalBlobProperties.stream().forEach(x -> {
			if(propertiesTofetch.contains(x)) {
				propertiesTofetch.add("blobAsText(" + x + ") as " + x);
				propertiesTofetch.remove(x);
			}
		});
		
		if(!propertiesTofetch.contains("question_id"))
			propertiesTofetch.add("question_id");
		String query = getSelectStatement(identifiers, propertiesTofetch);
		try {
		PreparedStatement ps = session.prepare(query);
		BoundStatement bound = new BoundStatement(ps);
		
			ResultSet rs = session.execute(bound);
			Map<String, Object> itemsMap = new HashMap<>();
			if (null != rs) {
				while (rs.iterator().hasNext()) {
					Row row = rs.iterator().next();
					Map<String, Object> propertyMap = new HashMap<String, Object>();
					properties.forEach(prop -> propertyMap.put((String) prop, row.getString(prop)));
					itemsMap.put(row.getString("question_id"), propertyMap);
				}
				return itemsMap;
			}
		} catch (Exception e) {
			TelemetryManager.error("Error! Executing get items: " + e.getMessage(), e);
			throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(),
					"Error fetching items from cassandra.");
		}
		return null;
	}


	private static String getSelectStatement(List<String> ids, List<String> properties) {
		StringBuilder query = new StringBuilder(
				Constants.SELECT + " ");
		query.append(String.join(",", properties));
		query.append(Constants.FROM + keyspace + Constants.DOT + table + Constants.WHERE + "question_id " + Constants.IN
				+ " ('" + String.join("','", ids) + "'); ");
		return query.toString();
	}
}
