package org.sunbird.jobs.samza.serializers;

import org.apache.samza.config.Config;
import org.apache.samza.serializers.SerdeFactory;

/**
 * 
 * @author Mahesh Kumar Gangula
 *
 */

public class EkstepJsonSerdeFactory implements SerdeFactory<Object> {
	public EkstepJsonSerde<Object> getSerde(String name, Config config) {
		return new EkstepJsonSerde<>();
	}
}
