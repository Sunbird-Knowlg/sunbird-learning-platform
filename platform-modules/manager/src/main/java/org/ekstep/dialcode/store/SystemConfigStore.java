/**
 * 
 */
package org.ekstep.dialcode.store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.cassandra.store.AbstractCassandraStore;
import org.ekstep.common.Platform;
import org.ekstep.dialcode.enums.DialCodeEnum;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Row;

/**
 * @author gauraw
 *
 */
@Component
public class SystemConfigStore extends AbstractCassandraStore {

	public SystemConfigStore() {
		super();
		String keyspace = Platform.config.hasPath("system.config.keyspace.name")
				? Platform.config.getString("system.config.keyspace.name")
				: "dialcode_store";
		String table = Platform.config.hasPath("system.config.table") ? Platform.config.getString("system.config.table")
				: "system_config";
		initialise(keyspace, table, "SystemConfig");
	}

	public Double getDialCodeIndex() throws Exception {
		List<Row> rows = read(DialCodeEnum.prop_key.name(), DialCodeEnum.dialcode_max_index.name());
		Row row = rows.get(0);
		return Double.valueOf(row.getString(DialCodeEnum.prop_value.name()));
	}

	public void setDialCodeIndex(double maxIndex) throws Exception {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(DialCodeEnum.prop_value.name(), String.valueOf((int) maxIndex));
		update(DialCodeEnum.prop_key.name(), DialCodeEnum.dialcode_max_index.name(), data);
	}
}
