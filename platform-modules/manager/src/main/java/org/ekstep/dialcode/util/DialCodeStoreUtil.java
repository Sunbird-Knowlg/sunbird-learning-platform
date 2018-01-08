package org.ekstep.dialcode.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.cassandra.store.CassandraStoreUtil;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.dialcode.common.DialCodeErrorCodes;
import org.ekstep.dialcode.common.DialCodeErrorMessage;
import org.ekstep.dialcode.enums.DialCodeEnum;
import org.ekstep.dialcode.model.DialCode;
import org.ekstep.telemetry.logger.TelemetryManager;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This Class is for all Dial Code CRUD Operation on Cassandra.
 * 
 * @author gauraw
 *
 */
public class DialCodeStoreUtil {

	private static ObjectMapper mapper = new ObjectMapper();

	public static String getKeyspaceName(String keyspace) {
		if (StringUtils.equals(keyspace, DialCodeEnum.dialcode.name()))
			return Platform.config.getString("dialcode.keyspace.name");
		if (StringUtils.equals(keyspace, DialCodeEnum.publisher.name()))
			return Platform.config.getString("publisher.keyspace.name");
		if (StringUtils.equals(keyspace, DialCodeEnum.system_config.name()))
			return Platform.config.getString("system.config.keyspace.name");
		return null;
	}

	public static String getKeyspaceTable(String table) {
		if (StringUtils.equals(table, DialCodeEnum.dialcode.name()))
			return Platform.config.getString("dialcode.keyspace.table");
		if (StringUtils.equals(table, DialCodeEnum.publisher.name()))
			return Platform.config.getString("publisher.keyspace.table");
		if (StringUtils.equals(table, DialCodeEnum.system_config.name()))
			return Platform.config.getString("system.config.table");
		return null;
	}

	public static Double getDialCodeIndex() throws Exception {
		List<Row> rows = CassandraStoreUtil.read(getKeyspaceName(DialCodeEnum.system_config.name()),
				getKeyspaceTable(DialCodeEnum.system_config.name()), DialCodeEnum.prop_key.name(),
				DialCodeEnum.dialcode_max_index.name());
		Row row = rows.get(0);
		return Double.valueOf(row.getString(DialCodeEnum.prop_value.name()));
	}

	public static void setDialCodeIndex(double maxIndex) throws Exception {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(DialCodeEnum.prop_value.name(), String.valueOf((int) maxIndex));
		CassandraStoreUtil.update(getKeyspaceName(DialCodeEnum.system_config.name()),
				getKeyspaceTable(DialCodeEnum.system_config.name()), DialCodeEnum.prop_key.name(),
				DialCodeEnum.dialcode_max_index.name(), data);
	}

	public static void save(String channel, String publisher, String batchCode, String dialCode, Double dialCodeIndex)
			throws Exception {
		Map<String, Object> data = getInsertData(channel, publisher, batchCode, dialCode, dialCodeIndex);
		CassandraStoreUtil.insert(getKeyspaceName(DialCodeEnum.dialcode.name()),
				getKeyspaceTable(DialCodeEnum.dialcode.name()), dialCode, data);
		List<String> keys = data.keySet().stream().collect(Collectors.toList());
		TelemetryManager.audit((String) dialCode, "Dialcode", keys, "Draft", null);
	}

	public static DialCode read(String dialCode) throws Exception {
		DialCode dialCodeObj = null;
		try {
			List<Row> rows = CassandraStoreUtil.read(getKeyspaceName(DialCodeEnum.dialcode.name()),
					getKeyspaceTable(DialCodeEnum.dialcode.name()), DialCodeEnum.identifier.name(), dialCode);
			Row row = rows.get(0);
			dialCodeObj = setDialCodeData(row);
		} catch (Exception e) {
			throw new ResourceNotFoundException(DialCodeErrorCodes.ERR_DIALCODE_INFO,
					DialCodeErrorMessage.ERR_DIALCODE_INFO);
		}
		return dialCodeObj;
	}

	public static void update(String id, Map<String, Object> data) throws Exception {
		CassandraStoreUtil.update(getKeyspaceName(DialCodeEnum.dialcode.name()),
				getKeyspaceTable(DialCodeEnum.dialcode.name()), DialCodeEnum.identifier.name(), id, data);
		List<String> keys = data.keySet().stream().collect(Collectors.toList());
		String status = (String) data.get("status");
		TelemetryManager.audit((String) id, "Dialcode", keys, status, null);
	}

	public static List<DialCode> list(String channelId, Map<String, Object> map) throws Exception {
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

	private static Map<String, Object> getInsertData(String channel, String publisher, String batchCode,
			String dialCode, Double dialCodeIndex) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(DialCodeEnum.identifier.name(), dialCode);
		data.put(DialCodeEnum.channel.name(), channel);
		data.put(DialCodeEnum.publisher.name(), publisher);
		data.put(DialCodeEnum.batchcode.name(), batchCode);
		data.put(DialCodeEnum.dialcode_index.name(), dialCodeIndex);
		data.put(DialCodeEnum.status.name(), DialCodeEnum.Draft.name());
		data.put(DialCodeEnum.generated_on.name(), LocalDateTime.now().toString());
		return data;
	}

	private static DialCode setDialCodeData(Row row) throws Exception {
		DialCode dialCodeObj = new DialCode();
		dialCodeObj.setIdentifier(row.getString(DialCodeEnum.identifier.name()));
		dialCodeObj.setChannel(row.getString(DialCodeEnum.channel.name()));
		dialCodeObj.setPublisher(row.getString(DialCodeEnum.publisher.name()));
		dialCodeObj.setBatchCode(row.getString(DialCodeEnum.batchCode.name()));
		dialCodeObj.setStatus(row.getString(DialCodeEnum.status.name()));
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

	// TODO: This method will point to Elasticsearch instead of Cassandra.
	private static String getListQuery(String channelId, Map<String, Object> map) {
		StringBuilder listQuery = new StringBuilder();
		listQuery.append("select * from " + getKeyspaceName(DialCodeEnum.dialcode.name()) + "."
				+ getKeyspaceTable(DialCodeEnum.dialcode.name()) + " where ");
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
