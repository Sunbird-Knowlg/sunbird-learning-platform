package org.ekstep.graph.service.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Config.EncryptionLevel;

public class ConfigUtil {
	
	private static Logger LOGGER = LogManager.getLogger(ConfigUtil.class.getName());
	
	public static Config getConfig() {
		Config config = Config.build().withRoutingFailureLimit(0).withMaxIdleSessions(0).withEncryptionLevel(EncryptionLevel.NONE).withMaxSessions(0).toConfig();
		return config;
	}

}
