package org.ekstep.common.util;

import java.util.Map;
import java.util.Properties;
import com.ilimi.common.Platform;

public class S3PropertyReader {
	private static Properties prop = new Properties();
	
	public static void loadProperties(Map<String, Object> props) {
		prop.putAll(props);
	}
	
	public static void loadProperties(Properties props) {
		prop.putAll(props);
	}
	
	public static String getProperty(String key){
		return Platform.config.getString(key);
	}
	
	public static String getProperty(String key, String env){
		String property = key + "." + env;
		return Platform.config.getString(property);
	}
}
