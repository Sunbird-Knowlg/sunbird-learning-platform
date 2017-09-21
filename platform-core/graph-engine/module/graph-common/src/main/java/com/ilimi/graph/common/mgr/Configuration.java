package com.ilimi.graph.common.mgr;

import java.util.ArrayList;
import java.util.List;

import com.ilimi.common.Platform;
import com.ilimi.common.logger.LoggerEnum;
import com.ilimi.common.logger.PlatformLogger;

public class Configuration {

    public static long TIMEOUT = 30000;
    public static List<String> graphIds = new ArrayList<String>();
    
    static {
    	try{
            int timeout = Platform.config.getInt("akka.request_timeout");
            if (timeout > 0) {
                TIMEOUT = timeout * 1000;
            }
             graphIds = Platform.config.getStringList("graph.ids");
        } catch (Exception e) {
        	PlatformLogger.log("Error! While Loading Graph Properties.", e.getMessage(),LoggerEnum.ERROR.name());
        }
    }
    
    public static void registerNewGraph(String graphId){
    	graphIds.add(graphId);
    }
}
