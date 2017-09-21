package com.ilimi.common;

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
	public static void loadProperties(Config conf){
		config = defaultConf.withFallback(conf);
	}
}
