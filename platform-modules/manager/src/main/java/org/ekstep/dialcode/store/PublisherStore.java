/**
 * 
 */
package org.ekstep.dialcode.store;

import java.util.List;
import java.util.Map;

import org.ekstep.cassandra.store.AbstractCassandraStore;
import org.ekstep.common.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Row;

/**
 * @author mahesh
 *
 */
@Component
public class PublisherStore extends AbstractCassandraStore {

	public PublisherStore() {
		super();
		String keyspace = Platform.config.getString("publisher.keyspace.name");
		initialise(keyspace, "publisher", "Publisher");
	}


	public void create(String id, Map<String, Object> props) {
		insert(id, props);
	}
	
	public List<Row> get(String id, Object value) {
		return read(id, value);
	}
	
	public void modify(String identifier, String idValue, Map<String, Object> request) {
		update(identifier, idValue, request);
	}
}
