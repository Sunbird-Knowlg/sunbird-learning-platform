package org.ekstep.graph.common.mgr;

import java.util.ArrayList;
import java.util.List;

import org.ekstep.common.Platform;
import org.ekstep.common.logger.LoggerEnum;
import org.ekstep.common.logger.PlatformLogger;

public class Configuration {

    public static long TIMEOUT = 30000;
    public static List<String> graphIds = new ArrayList<String>();
    
    static {
    	try{
    		if (Platform.config.hasPath("akka.request_timeout")) {
    			int timeout = Platform.config.getInt("akka.request_timeout");
    			if (timeout > 0)
    				TIMEOUT = timeout * 1000;
    		}
            if(Platform.config.hasPath("graph.ids"))
            	graphIds = Platform.config.getStringList("graph.ids");
        } catch (Exception e) {
        	PlatformLogger.log("Error! While Loading Graph Properties.", e.getMessage(),LoggerEnum.ERROR.name());
        }
    }
    
    public static void registerNewGraph(String graphId){
    	graphIds.add(graphId);
    }
}
