package com.ilimi.kafka.util;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

	private static Properties prop = new Properties();
	private static InputStream input = null;

	static {
		loadProperties("producer.properties");
	}

	public static String getProperty(String key) {
		return prop.getProperty(key);
	}
	
	public static void loadProperties(String filename) {
		try {
			input = PropertiesUtil.class.getClassLoader().getResourceAsStream(filename);
			if (input == null) {
				throw new Exception("Unable to find " + filename);
			}
			prop.load(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
