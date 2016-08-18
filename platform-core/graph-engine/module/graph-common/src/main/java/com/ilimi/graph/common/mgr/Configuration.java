package com.ilimi.graph.common.mgr;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class Configuration {

    public static long TIMEOUT = 30000;
    public static List<String> graphIds = new ArrayList<String>();

    private static Properties props;
    static {
        try {
            InputStream inputStream = Configuration.class.getClassLoader().getResourceAsStream("graph.properties");
            props = new Properties();
            props.load(inputStream);
            String timeout = props.getProperty("akka.request_timeout");
            if (StringUtils.isNotBlank(timeout)) {
                long seconds = Long.parseLong(timeout);
                if (seconds > 0) {
                    TIMEOUT = seconds * 1000;
                }
            }
            String ids = props.getProperty("graph.ids");
            if (StringUtils.isNotBlank(ids)) {
            	String[] array = ids.split(",");
            	if (null != array && array.length > 0) {
            		for (String id : array) {
            			if (StringUtils.isNotBlank(id))
            				graphIds.add(id);
            		}
            	}
            }
        } catch (Exception e) {
        }
    }
    
    public static void registerNewGraph(String graphId){
    	graphIds.add(graphId);
    }

}
