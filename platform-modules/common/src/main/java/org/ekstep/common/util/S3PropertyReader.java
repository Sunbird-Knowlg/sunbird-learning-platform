package org.ekstep.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.ilimi.common.logger.LoggerEnum;
import com.ilimi.common.logger.PlatformLogger;

public class S3PropertyReader {
	private static Properties prop = new Properties();
	private static InputStream input = null;

	static {
		String filename = "amazonS3Config.properties";
		input = S3PropertyReader.class.getClassLoader().getResourceAsStream(
				filename);
		if (input == null) {
			PlatformLogger.log("Unable to find " , filename);
		}
		try {
			prop.load(input);
		} catch (IOException e) {
			PlatformLogger.log("Error", e.getMessage(), LoggerEnum.ERROR.name());
		}
	}
	
	public static void loadProperties(Map<String, Object> props) {
		prop.putAll(props);
	}
	
	public static void loadProperties(Properties props) {
		prop.putAll(props);
	}
	
	public static String getProperty(String key){
		return prop.getProperty(key);
	}
	
	public static String getProperty(String key, String env){
		String property = key + "." + env;
		return prop.getProperty(property);
	}
}
