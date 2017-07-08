package org.ekstep.language.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;

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

	/** The logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	//load language-indexes.properties by default
	static {
		String filename = "language-indexes.properties";
		input = PropertiesUtil.class.getClassLoader().getResourceAsStream(filename);
		if (input == null) {
			LOGGER.log("Unable to find " + filename);
		}
		try {
			prop.load(input);
		} catch (IOException e) {
			e.printStackTrace();
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
}
