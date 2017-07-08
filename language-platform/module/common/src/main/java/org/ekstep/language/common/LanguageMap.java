package org.ekstep.language.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.ekstep.language.util.PropertiesUtil;

import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;

/**
 * The Class LanguageMap loads the language graph Id to language name mappings
 * from the language-map.properties file and caches them.
 * 
 * @author Amarnath
 * 
 */
public class LanguageMap {

	/** The language map. */
	private static Map<String, String> languageMap = new HashMap<String, String>();

	/** The language graph map. */
	private static Map<String, String> languageGraphMap = new HashMap<String, String>();

	/** The prop. */
	private static Properties prop = new Properties();

	/** The input. */
	private static InputStream input = null;

	/** The logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	static {
		String filename = "language-map.properties";
		input = PropertiesUtil.class.getClassLoader().getResourceAsStream(filename);
		if (input == null) {
			LOGGER.log("Unable to find " + filename);
		}
		try {
			// loads the data from the properties file into two maps. One with
			// Graph Id to Language name mapping and one with Language name to
			// Graph Id mapping.
			prop.load(input);
			Set<Object> keys = prop.keySet();
			for (Object k : keys) {
				String key = (String) k;
				String value = (String) getProperty(key);
				languageMap.put(key, value);
				languageGraphMap.put(value, key);
			}
		} catch (IOException e) {
			LOGGER.log("Exception!", e.getMessage(), e);
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

	/**
	 * Check if the cache contains language.
	 *
	 * @param languageId
	 *            the language id
	 * @return true, if successful
	 */
	public static boolean containsLanguage(String languageId) {
		return languageMap.containsKey(languageId);
	}

	/**
	 * Gets the language from the cache.
	 *
	 * @param languageId
	 *            the language id
	 * @return the language
	 */
	public static String getLanguage(String languageId) {
		return languageMap.get(languageId);
	}

	/**
	 * Gets the language graph id from the cache.
	 *
	 * @param language
	 *            the language
	 * @return the language graph
	 */
	public static String getLanguageGraph(String language) {
		return languageGraphMap.get(language);
	}
}
