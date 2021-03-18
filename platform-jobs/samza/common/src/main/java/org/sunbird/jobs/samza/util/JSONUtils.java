package org.sunbird.jobs.samza.util;

import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.samza.config.Config;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.Platform;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class JSONUtils {
	
	private static ObjectMapper mapper = new ObjectMapper();;

	public static String serialize(Object object) throws Exception {
			return mapper.writeValueAsString(object);
	}
	
	public static void loadProperties(Config config){
		Map<String, Object> props = new HashMap<String, Object>();
		for (Entry<String, String> entry : config.entrySet()) {
			if (StringUtils.equalsIgnoreCase("True", entry.getValue()) || StringUtils.equalsIgnoreCase("False", entry.getValue()))
				props.put(entry.getKey(), entry.getValue().toLowerCase());
			else
				props.put(entry.getKey(), entry.getValue());
		}
		com.typesafe.config.Config conf = ConfigFactory.parseMap(props);
		Platform.loadProperties(conf);
	}
}