package com.ilimi.kafka.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Loads properties from a properties file and supports operations to retrieve
 * properties.
 * 
 * @author Amarnath
 * 
 */
public class PropertiesUtil {

	/** The properties object. */
	private static Properties prop = new Properties();

	/** The input. */
	private static InputStream input = null;

	/**
	 * Gets the property.
	 *
	 * @param key
	 *            the key
	 * @return the property
	 */
	public static String getProperty(String key) {
		return prop.getProperty(key);
	}

	/**
	 * Load properties from a file.
	 *
	 * @param filename
	 *            the filename
	 */
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
