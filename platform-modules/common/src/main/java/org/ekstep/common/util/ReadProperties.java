package org.ekstep.common.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class ReadProperties {
    String result = "";
    
   private static Properties prop = new Properties();
    
    InputStream inputStream = new InputStream() {
        @Override
        public int read() throws IOException {
            return 0;
        }
    };

    public String getPropValues(String key) throws IOException {
        try {
            String propFileName = "content.properties";
            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
            result = prop.getProperty(key);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            inputStream.close();
        }
        return result;
    }
    

	public static void loadProperties(Map<String, Object> props) {
		prop.putAll(props);
	}
	
	public static void loadProperties(Properties props) {
		prop.putAll(props);
	}
}
