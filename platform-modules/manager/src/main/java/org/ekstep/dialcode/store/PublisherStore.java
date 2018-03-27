/**
 * 
 */
package org.ekstep.dialcode.store;

import java.util.List;
import java.util.Map;

import org.ekstep.cassandra.store.CassandraStore;
import org.ekstep.common.Platform;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Row;

/**
 * @author mahesh
 *
 */
@Component
public class PublisherStore extends CassandraStore {

	public PublisherStore() {
		super();
		String keyspace = "dialcode_store";
		if (Platform.config.hasPath("publisher.keyspace.name"))
			keyspace = Platform.config.getString("publisher.keyspace.name");
		initialise(keyspace, "publisher", "Publisher");
	}


	public void create(String id, Map<String, Object> props) {
		insert(id, props);
	}
	
	public List<Row> get(String id, Object value) {
		return read(id, value);
	}
	
	public void modify(String key, String idValue, Map<String, Object> props) {
		update(key, idValue, props);
	}
}
