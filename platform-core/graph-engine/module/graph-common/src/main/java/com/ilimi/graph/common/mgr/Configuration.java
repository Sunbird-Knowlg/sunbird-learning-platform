package com.ilimi.graph.common.mgr;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class Configuration {

    public static long TIMEOUT = 30000;

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
        } catch (Exception e) {
        }
    }

}
