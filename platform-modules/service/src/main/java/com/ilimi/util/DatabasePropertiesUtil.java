package com.ilimi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.ilimi.common.logger.PlatformLogger;

public class DatabasePropertiesUtil {
	private static Properties prop = new Properties();
	private static InputStream input = null;
	

	static {
		String filename = "database.properties";
		input = DatabasePropertiesUtil.class.getClassLoader().getResourceAsStream(
				filename);
		if (input == null) {
			PlatformLogger.log("Unable to find " + filename);
		}
		try {
			prop.load(input);
		} catch (IOException e) {
			PlatformLogger.log("Exception",e.getMessage(), e);
		}
	}
	
	public static String getProperty(String key){
		return prop.getProperty(key);
	}
}
