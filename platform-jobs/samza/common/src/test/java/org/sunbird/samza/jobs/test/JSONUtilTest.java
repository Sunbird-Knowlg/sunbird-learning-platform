package org.eksep.samza.jobs.test;

import java.util.HashMap;
import java.util.Map;

import org.sunbird.common.Platform;
import org.junit.Assert;
import org.junit.Test;

import com.typesafe.config.ConfigFactory;

public class JSONUtilTest {

	static Map<String, String> configMap = new HashMap<String, String>();

	static {
		configMap.put("elastic-search-host", "http://localhost");
		configMap.put("elastic-search-port", "9200");
		configMap.put("graph.dir", "/data/graphDB/");
		configMap.put("route.bolt.write.domain", "bolt://localhost:7687");
		configMap.put("graph.bolt.enable", "true");
	}

	@Test
	public void loadConfigProps_1() {
		com.typesafe.config.Config conf = ConfigFactory.parseMap(configMap);
		Platform.loadProperties(conf);
		String route = Platform.config.getString("route.bolt.write.domain");
		Assert.assertEquals("bolt://localhost:7687", route);
		Assert.assertEquals("/data/graphDB/", Platform.config.getString("graph.dir"));
		Assert.assertTrue(Platform.config.getBoolean("graph.bolt.enable"));
	}
}
