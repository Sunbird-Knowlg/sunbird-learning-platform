package org.ekstep.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	
	public static String getProperty(String key){
		return prop.getProperty(key);
	}
	
	public static String getProperty(String key, String env){
		String property = key + "." + env;
		return prop.getProperty(property);
	}
}
