package org.ekstep.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class S3PropertyReader {
	private static Properties prop = new Properties();
	private static InputStream input = null;
	private static Logger LOGGER = LogManager.getLogger(S3PropertyReader.class
			.getName());

	static {
		String filename = "amazonS3Config.properties";
		input = S3PropertyReader.class.getClassLoader().getResourceAsStream(
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
	
	public static String getProperty(String key, String region){
		String property = key + "." + region;
		return prop.getProperty(property);
	}
}
