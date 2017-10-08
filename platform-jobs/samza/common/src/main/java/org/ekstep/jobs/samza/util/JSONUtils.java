package org.ekstep.jobs.samza.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.samza.config.Config;
import org.codehaus.jackson.map.ObjectMapper;


import com.ilimi.common.Platform;
import com.typesafe.config.ConfigFactory;

public class JSONUtils {
	
	private static ObjectMapper mapper = new ObjectMapper();;
	
	public static String serialize(Object object) throws Exception {
		return mapper.writeValueAsString(object);
	}
	
	public static void loadProperties(Config config){
		Map<String, Object> props = new HashMap<String, Object>();
		for (Entry<String, String> entry : config.entrySet()) {
			props.put(entry.getKey(), entry.getValue());
		}
		com.typesafe.config.Config conf = ConfigFactory.parseMap(props);
		Platform.loadProperties(conf);
	}
}