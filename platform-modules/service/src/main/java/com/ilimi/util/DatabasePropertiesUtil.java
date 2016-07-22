package com.ilimi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabasePropertiesUtil {
	private static Properties prop = new Properties();
	private static InputStream input = null;
	private static Logger LOGGER = LogManager.getLogger(DatabasePropertiesUtil.class
			.getName());

	static {
		String filename = "database.properties";
		input = DatabasePropertiesUtil.class.getClassLoader().getResourceAsStream(
				filename);
		if (input == null) {
			LOGGER.error("Unable to find " + filename);
		}
		try {
			prop.load(input);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	public static String getProperty(String key){
		return prop.getProperty(key);
	}
}
