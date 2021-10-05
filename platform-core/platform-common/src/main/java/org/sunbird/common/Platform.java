package org.sunbird.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * 
 * @author Mahesh Kumar Gangula
 *
 */

public class Platform {
	private static Config defaultConf = ConfigFactory.load();
	private static Config envConf = ConfigFactory.systemEnvironment();
	public static Config config = defaultConf.withFallback(envConf);

	private static int requestTimeout = 30;
	private static Map<String, List<String>> graphIds = new HashMap<>();

	
	public static void loadProperties(Config conf) {
		config = config.withFallback(conf);
	}

	public static int getTimeout() {
		return requestTimeout;
	}

	public static List<String> getGraphIds(String... services) {
		List<String> ids = new ArrayList<>();
		for (String service: services) {
			ids.addAll(getGraphIds(service));
		}
		return ids;
	}
	
	private static List<String> getGraphIds(String service) {
		service = service.toLowerCase();
		if (!graphIds.containsKey(service)) {
			String key = service + ".graph_ids";
			if (config.hasPath(key)) {
				graphIds.put(service, config.getStringList(key));
			} else
				return Arrays.asList();
		}
		return graphIds.get(service);
	}

}
