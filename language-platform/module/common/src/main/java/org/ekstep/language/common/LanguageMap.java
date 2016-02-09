package org.ekstep.language.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.util.PropertiesUtil;

public class LanguageMap {

	private static Map<String, String> languageMap = new HashMap<String, String>();
	private static Properties prop = new Properties();
	private static InputStream input = null;
	private static Logger LOGGER = LogManager.getLogger(PropertiesUtil.class.getName());

	static {
		String filename = "language-map.properties";
		input = PropertiesUtil.class.getClassLoader().getResourceAsStream(filename);
		if (input == null) {
			LOGGER.error("Unable to find " + filename);
		}
		try {
			prop.load(input);
			Set<Object> keys = prop.keySet();
			for (Object k : keys) {
				String key = (String) k;
				String value = (String) getProperty(key);
				languageMap.put(key, value);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProperty(String key) {
		return prop.getProperty(key);
	}

	public static boolean containsLanguage(String languageId) {
		return languageMap.containsKey(languageId);
	}

	public static String getLanguage(String languageId) {
		return languageMap.get(languageId);
	}
}
