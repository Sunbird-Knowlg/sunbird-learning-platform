package org.ekstep.language.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.ilimi.common.logger.PlatformLogger;

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


	//load language-indexes.properties by default
	static {
		String filename = "language-indexes.properties";
		try(InputStream input = PropertiesUtil.class.getClassLoader().getResourceAsStream(filename)){
			prop.load(input);
		} catch (Exception e) {
			PlatformLogger.log("Exception!", e.getMessage(), e);
		}
	}

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
	
	public static void loadProperties(Map<String, Object> props) {
		prop.putAll(props);
	}
	
	public static void loadProperties(Properties props) {
		prop.putAll(props);
	}
}
