package org.ekstep.dialcode.store;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.cassandra.store.CassandraStore;
import org.ekstep.cassandra.store.CassandraStoreParams;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.dialcode.common.DialCodeErrorCodes;
import org.ekstep.dialcode.common.DialCodeErrorMessage;
import org.ekstep.dialcode.enums.DialCodeEnum;
import org.ekstep.dialcode.model.DialCode;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Row;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This Class is for all Dial Code CRUD Operation on Cassandra.
 * 
 * @author gauraw
 *
 */
@Component
public class DialCodeStore extends CassandraStore {

	private static ObjectMapper mapper = new ObjectMapper();
	
	public DialCodeStore() {
		super();
		String keyspace = Platform.config.hasPath("dialcode.keyspace.name") ? Platform.config.getString("dialcode.keyspace.name") : "dialcode_store";
		String table = Platform.config.hasPath("dialcode.keyspace.table") ? Platform.config.getString("dialcode.keyspace.table") : "dial_code";
		boolean index = Platform.config.hasPath("dialcode.index") ? Platform.config.getBoolean("dialcode.index") : true;
		String objectType = Platform.config.hasPath("dialcode.object_type")
				? Platform.config.getString("dialcode.object_type")
				: DialCodeEnum.DialCode.name();
		initialise(keyspace, table, objectType, index);
	}

	public void save(String channel, String publisher, String batchCode, String dialCode, Double dialCodeIndex)
			throws Exception {
		Map<String, Object> data = getInsertData(channel, publisher, batchCode, dialCode, dialCodeIndex);
		insert(dialCode, data);
		List<String> keys = data.keySet().stream().collect(Collectors.toList());
		TelemetryManager.audit((String) dialCode, getObjectType(), keys, "Draft", null);
	}

	public DialCode read(String dialCode) throws Exception {
		DialCode dialCodeObj = null;
		try {
			List<Row> rows = read(DialCodeEnum.identifier.name(), dialCode);
			Row row = rows.get(0);
			dialCodeObj = setDialCodeData(row);
		} catch (Exception e) {
			throw new ResourceNotFoundException(DialCodeErrorCodes.ERR_DIALCODE_INFO,
					DialCodeErrorMessage.ERR_DIALCODE_INFO);
		}
		return dialCodeObj;
	}

	public void update(String id, Map<String, Object> data) throws Exception {
		update(DialCodeEnum.identifier.name(), id, data);
		List<String> keys = data.keySet().stream().collect(Collectors.toList());
		String status = (String) data.get("status");
		TelemetryManager.audit((String) id, getObjectType(), keys, status, null);
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

	/**
	 * @param identifiers
	 */
	public void sync(List<String> identifiers) {
		List<Object> ids = new ArrayList<Object>();
		ids.addAll(identifiers);
		List<Row> rows = getRecordsByProperty(DialCodeEnum.identifier.name(), ids);
		Map<String, Object> syncRequest = new HashMap<String, Object>();
		for (Row row : rows) {
			syncRequest = new HashMap<String, Object>();
			syncRequest.put(DialCodeEnum.identifier.name(), row.getString(DialCodeEnum.identifier.name()));
			syncRequest.put(DialCodeEnum.channel.name(), row.getString(DialCodeEnum.channel.name()));
			syncRequest.put(DialCodeEnum.publisher.name(), row.getString(DialCodeEnum.publisher.name()));
			syncRequest.put(DialCodeEnum.batchCode.name(), row.getString(DialCodeEnum.batchCode.name()));
			syncRequest.put(DialCodeEnum.status.name(), row.getString(DialCodeEnum.status.name()));
			syncRequest.put(DialCodeEnum.status.name(), row.getString(DialCodeEnum.status.name()));
			syncRequest.put(DialCodeEnum.metadata.name(), row.getString(DialCodeEnum.metadata.name()));
			logTransactionEvent(CassandraStoreParams.UPDATE.name(), row.getString(DialCodeEnum.identifier.name()),
					syncRequest);
		}

	}

}
