package com.ilimi.dialcode.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.cassandra.connector.util.CassandraConnector;
import com.ilimi.common.Platform;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.dialcode.enums.DialCodeEnum;
import com.ilimi.dialcode.model.DialCode;

/**
 * Util Class for all Dial Code CRUD Operation on Cassandra.
 * 
 * @author gauraw
 *
 */
public class DialCodeStoreUtil {

	private static String keyspaceName = "";
	private static String keyspaceTable = "";
	private static ObjectMapper mapper = new ObjectMapper();

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

	// get dialcode index from cassandra
	public static Integer getDialCodeIndex() throws Exception {
		Integer dialcode_index = 0;
		Session session = CassandraConnector.getSession();
		String query = "select max(dialcode_index) as dialcode_index from " + getKeyspaceName() + "."
				+ getKeyspaceTable() + ";";
		ResultSet rs = session.execute(query);
		if (null != rs) {
			while (rs.iterator().hasNext()) {
				Row row = rs.iterator().next();
				dialcode_index = row.getInt("dialcode_index");
			}
		}

		return dialcode_index;
	}

	public static void saveDialCode(String channel, String publisher, String batchCode, Map<Integer, String> codeMap)
			throws Exception {
		Session session = CassandraConnector.getSession();
		String query = getInsertQuery(channel, publisher, batchCode, codeMap);
		session.execute(query);
	}

	public static DialCode readDialCode(String dialCode) throws Exception {
		DialCode dialCodeObj = null;

		Session session = CassandraConnector.getSession();
		String query = "select * from " + getKeyspaceName() + "." + getKeyspaceTable() + " where identifier='"
				+ dialCode + "'";
		ResultSet rs = session.execute(query);
		if (null != rs) {
			while (rs.iterator().hasNext()) {
				Row row = rs.iterator().next();
				dialCodeObj = setDialCodeData(row);
			}
		}

		if (null == dialCodeObj)
			throw new ResourceNotFoundException("ERR_DIALCODE_INFO", "Dial Code Not Found.");

		return dialCodeObj;
	}

	public static String updateData(String dialCodeId, String publisher, String metadata) {
		String id = null;
		Session session = CassandraConnector.getSession();

		StringBuilder updateQuery = new StringBuilder();
		updateQuery.append("update ");
		updateQuery.append(getKeyspaceName() + "." + getKeyspaceTable() + " set ");
		updateQuery.append("publisher='" + publisher + "', ");
		updateQuery.append("metadata='" + metadata + "' ");
		updateQuery.append("where identifier='" + dialCodeId + "'");

		ResultSet rs = session.execute(updateQuery.toString());

		if (null != rs) {
			id = dialCodeId;
		}

		return id;
	}

	public static String updateData(String dialCodeId) {
		String id = null;

		Session session = CassandraConnector.getSession();

		StringBuilder updateQuery = new StringBuilder();
		updateQuery.append("update ");
		updateQuery.append(getKeyspaceName() + "." + getKeyspaceTable() + " set ");
		updateQuery.append("status='" + DialCodeEnum.Live.name() + "', ");
		updateQuery.append("published_on='" + LocalDateTime.now() + "' ");
		updateQuery.append("where identifier='" + dialCodeId + "'");

		ResultSet rs = session.execute(updateQuery.toString());

		if (null != rs) {
			id = dialCodeId;
		}

		return id;
	}

	public static List<DialCode> getDialCodeList(String channelId, Map<String, Object> map) throws Exception {
		DialCode dialCodeObj = null;
		List<DialCode> list = new ArrayList<DialCode>();
		String listQuery = getListQuery(channelId, map);
		Session session = CassandraConnector.getSession();
		ResultSet rs = session.execute(listQuery);
		if (null != rs) {
			while (rs.iterator().hasNext()) {
				Row row = rs.iterator().next();
				dialCodeObj = setDialCodeData(row);
				list.add(dialCodeObj);
			}
		}

		return list;
	}

	private static String getInsertQuery(String channel, String publisher, String batchCode,
			Map<Integer, String> codeMap) {
		StringBuilder insertQuery = new StringBuilder();
		insertQuery.append("BEGIN BATCH ");
		for (Integer key : codeMap.keySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append("insert into " + getKeyspaceName() + "." + getKeyspaceTable());
			sb.append("(identifier,batchcode,channel,count,dialcode_index,generated_on,publisher,status) values(");
			sb.append("'" + codeMap.get(key) + "',");
			sb.append("'" + batchCode + "',");
			sb.append("'" + channel + "',");
			sb.append("0,");
			sb.append(key + ",");
			sb.append("'" + LocalDateTime.now() + "',");
			sb.append("'" + publisher + "',");
			sb.append("'" + DialCodeEnum.Draft.name() + "');");
			insertQuery.append(sb.toString());
		}
		insertQuery.append(" APPLY BATCH;");
		return insertQuery.toString();
	}

	private static DialCode setDialCodeData(Row row) throws Exception {

		DialCode dialCodeObj = new DialCode();
		dialCodeObj.setIdentifier(row.getString(DialCodeEnum.identifier.name()));
		dialCodeObj.setChannel(row.getString(DialCodeEnum.channel.name()));
		dialCodeObj.setPublisher(row.getString(DialCodeEnum.publisher.name()));
		dialCodeObj.setBatchCode(row.getString(DialCodeEnum.batchCode.name()));
		dialCodeObj.setStatus(row.getString(DialCodeEnum.status.name()));
		dialCodeObj.setCount(row.getInt(DialCodeEnum.count.name()));
		dialCodeObj.setGeneratedOn(row.getString(DialCodeEnum.generated_on.name()));
		dialCodeObj.setPublishedOn(row.getString(DialCodeEnum.published_on.name()));

		String metadata = row.getString(DialCodeEnum.metadata.name());
		Map<String, Object> metaData = null;
		if (!StringUtils.isBlank(metadata)) {
			metaData = mapper.readValue(metadata, new TypeReference<Map<String, Object>>() {
			});

		}
		dialCodeObj.setMetadata(metaData);
		return dialCodeObj;
	}

	private static String getListQuery(String channelId, Map<String, Object> map) {
		StringBuilder listQuery = new StringBuilder();
		listQuery.append("select * from " + getKeyspaceName() + "." + getKeyspaceTable() + " where ");
		listQuery.append("channel='" + channelId + "' ");

		for (String key : map.keySet()) {
			String value = (String) map.get(key);

			if (!StringUtils.isBlank(value)) {
				listQuery.append(" and " + key + "='" + value + "'");
			}
		}
		listQuery.append(" allow filtering;");

		return listQuery.toString();
	}
}
