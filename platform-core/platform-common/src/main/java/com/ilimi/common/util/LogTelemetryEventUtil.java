package com.ilimi.common.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.TelemetryBEEvent;

public class LogTelemetryEventUtil {

	private static final Logger telemetryEventMessageLogger = LogManager.getLogger("TelemetryEventMessageLogger");
	private static ObjectMapper mapper = new ObjectMapper();
	
    public static void logContentLifecycleEvent(String contentId, Map<String, Object> metadata){

    	TelemetryBEEvent te=new TelemetryBEEvent();
    	long unixTime = System.currentTimeMillis() / 1000L;
    	te.setEid("BE_CONTENT_LIFECYCLE");
    	te.setEts(unixTime);
    	te.setVer("2.0");
    	te.setPdata("org.ekstep.content.platform", "", "1.0", "");
    	te.setEdata(contentId, metadata.get("status"), metadata.get("size"), metadata.get("pkgVersion"), metadata.get("concepts"), metadata.get("flags"));
		String jsonMessage ;
		try{
			jsonMessage= mapper.writeValueAsString(te);
			if (StringUtils.isNotBlank(jsonMessage))
				telemetryEventMessageLogger.info(jsonMessage);
		}catch(Exception e){
		
		}
    }
}
