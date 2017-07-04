package com.ilimi.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.TelemetryBEEvent;

public class PlatformLogger<T> {

	private static ObjectMapper mapper = new ObjectMapper();
	private static final org.apache.logging.log4j.Logger telemetryEventLogger = LogManager.getLogger("TelemetryEventLogger");
	private String className;
	public PlatformLogger(Class<T> cls) {
		className = cls.getName();
	}

	private Logger logger(String name) {	
	    Logger logger  = (Logger) LogManager.getLogger(name+".logger");
	    return logger;
	}

	private void info(String message, Object data) throws JsonProcessingException{
		logger(className).info(mapper.writeValueAsString(getLogEvent("PLATFORM_LOG", "INFO", message, data)));
	}
	
	private void debug(String message, Object data) throws JsonProcessingException{
		logger(className).debug(mapper.writeValueAsString(getLogEvent("PLATFORM_LOG", "DEBUG", message, data)));
	}
	
	private void error(String message, Object data, Exception e) throws JsonProcessingException{
		logger(className).error(mapper.writeValueAsString(getLogEvent("PLATFORM_LOG", "ERROR", message, data, e)));
	}
	
	private void warn(String message, Object data,  Exception e) throws JsonProcessingException{
		logger(className).warn(mapper.writeValueAsString(getLogEvent("PLATFORM_LOG", "WARN", message, data, e)));
	}

	public void log(String logLevel, String message, Object data) throws JsonProcessingException{
		logData(logLevel, message, data, null);
	}
	
	public void log(String logLevel, String message, Object data, Exception e) throws JsonProcessingException{
		logData(logLevel, message, data, e);
	}
	
	public void log(String logLevel, String message) throws JsonProcessingException{
		logData(logLevel, message, null, null);
	}
		
	private void logData(String logLevel, String message, Object data, Exception e) throws JsonProcessingException{
		if(StringUtils.isNotBlank(logLevel)){
			switch(logLevel){
				case "INFO" :  info(message,  data);
				               break;
				case "DEBUG":  debug(message, data);
				               break;
				case "WARN" :  warn(message,  data, e);
				               break;
				case "ERROR" : error(message, data, e);
				               break;
			}
		}
	}
	
	private String getLogEvent(String logName, String logLevel, String message, Object data) {
		String logData = getLogMap(logName, logLevel, message, data, null);
		return logData;	
	}

	private String getLogEvent(String logName, String logLevel, String message, Object data , Exception e) {
		String logData = getLogMap(logName, logLevel, message, data, e);
		return logData;
	}
	
	private String getLogMap(String logName, String logLevel, String message, Object data, Exception e) {
		String mid = "LP."+System.currentTimeMillis()+"."+UUID.randomUUID();
		TelemetryBEEvent te = new TelemetryBEEvent();
		long unixTime = System.currentTimeMillis();
		Map<String,Object> eks = new HashMap<String,Object>();
		eks.put("class", className);
		eks.put("level", logLevel);
		eks.put("message", message);
		if(data != null){
			eks.put("data", data);
		}
		if(e != null){
			eks.put("stacktrace", e);
		}
		te.setEid(logName);
		te.setEts(unixTime);
		te.setMid(mid);
		te.setVer("2.0");
		te.setPdata("org.ekstep.learning.platform", "", "1.0", "");
		String jsonMessage = null;
		try {
			te.setEdata(eks);
			jsonMessage = mapper.writeValueAsString(te);
			telemetryEventLogger.info(jsonMessage);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return jsonMessage;
	}
}
