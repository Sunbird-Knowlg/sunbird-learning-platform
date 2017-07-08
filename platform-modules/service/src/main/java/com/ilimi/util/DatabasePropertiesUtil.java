package com.ilimi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;

public class DatabasePropertiesUtil {
	private static Properties prop = new Properties();
	private static InputStream input = null;
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	static {
		String filename = "database.properties";
		input = DatabasePropertiesUtil.class.getClassLoader().getResourceAsStream(
				filename);
		if (input == null) {
			LOGGER.log("Unable to find " + filename);
		}
		try {
			prop.load(input);
		} catch (IOException e) {
			LOGGER.log("Exception",e.getMessage(), e);
		}
	}
	
	public static String getProperty(String key){
		return prop.getProperty(key);
	}
}
