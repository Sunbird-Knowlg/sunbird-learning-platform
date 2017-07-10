package org.ekstep.searchindex.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.typesafe.config.ConfigFactory;

public class PropertiesUtil {

	private static Properties prop = new Properties();
	private static InputStream input = null;

	static {
		loadProperties("elasticsearch.properties");
	}

	public static String getProperty(String key) {
		return prop.getProperty(key);
	}
	
	public static void loadProperties(String filename) {
		try {
			boolean isLoaded = loadFromConfigPath(filename);
			if (!isLoaded) {
				input = PropertiesUtil.class.getClassLoader().getResourceAsStream(filename);
				if (input == null) {
					throw new Exception("Unable to find " + filename);
				}
				prop.load(input);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static boolean loadFromConfigPath(String filename) {
		try {
			String configPath = ConfigFactory.load().getString("search.config.path");
			if (StringUtils.isNotBlank(configPath)) {
				File file = new File(configPath + File.separator + filename);
				if (file.exists()) {
					input = new FileInputStream(file);
					if (null != input) {
						prop.load(input);
						return true;
					}
				}
			}
		} catch (Exception e) {}
		return false;
	}
}
