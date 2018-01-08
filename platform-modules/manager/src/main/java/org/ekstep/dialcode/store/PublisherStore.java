/**
 * 
 */
package org.ekstep.dialcode.store;

import java.util.Map;

import org.ekstep.cassandra.store.AbstractCassandraStore;
import org.ekstep.common.Platform;

/**
 * @author mahesh
 *
 */
public class PublisherStore extends AbstractCassandraStore {

	public PublisherStore() {
		super();
		String keyspace = Platform.config.getString("dialcode.publisher.keyspace");
		initialise(keyspace, "publisher", "Publisher");
	}


	public void create(String id, Map<String, Object> props) {
		insert(id, props);
	}
}
