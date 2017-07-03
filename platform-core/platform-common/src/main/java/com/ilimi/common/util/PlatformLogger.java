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

public class PlatformLogger {

	private static ObjectMapper mapper = new ObjectMapper();
	private static final org.apache.logging.log4j.Logger telemetryEventLogger = LogManager.getLogger("TelemetryEventLogger");
	
	private Logger logger(String name) {	
	    Logger logger  = (Logger) LogManager.getLogger(name+".logger");
	    return logger;
	}

	private void info(String message, Object data, String className) throws JsonProcessingException{
		logger(className).info(mapper.writeValueAsString(getLogEvent("PLATFORM_LOG", "INFO", message, data, className)));
	}
	
	private void debug(String message, Object data, String className) throws JsonProcessingException{
		logger(className).debug(mapper.writeValueAsString(getLogEvent("PLATFORM_LOG", "DEBUG", message, data, className)));
	}
	
	private void error(String message, Object data, String className, Exception e) throws JsonProcessingException{
		logger(className).error(mapper.writeValueAsString(getLogEvent("PLATFORM_LOG", "ERROR", message, data, className, e)));
	}
	
	private void warn(String message, Object data, String className, Exception e) throws JsonProcessingException{
		logger(className).warn(mapper.writeValueAsString(getLogEvent("PLATFORM_LOG", "WARN", message, data, className, e)));
	}
	
	@SuppressWarnings("unused")
	private void start(String message, Object data, String className) throws JsonProcessingException{
		logger(className).info(mapper.writeValueAsString(getLogEvent("JOB_START", "INFO", message, data, className)));
	}
	
	@SuppressWarnings("unused")
	private void stop(String message, Object data, String className) throws JsonProcessingException{
		logger(className).info(mapper.writeValueAsString(getLogEvent("JOB_END", "INFO",  message, data, className)));
	}
	
	public void log(String logLevel, String message, Object data, String className) throws JsonProcessingException{
		logData(logLevel, message, data, className, null);
	}
	
	public void log(String logLevel, String message, Object data, String className, Exception e) throws JsonProcessingException{
		logData(logLevel, message, data, className, e);
	}
	
	private void logData(String logLevel, String message, Object data,  String className, Exception e) throws JsonProcessingException{
		if(StringUtils.isNotBlank(logLevel)){
			switch(logLevel){
				case "INFO" :  info(message,  data, className);
				               break;
				case "DEBUG":  debug(message, data, className);
				               break;
				case "WARN" :  warn(message,  data, className, e);
				               break;
				case "ERROR" : error(message, data, className, e);
				               break;
			}
		}
	}
	
	private String getLogEvent(String logName, String logLevel, String message, Object data, String className) {
		String logData = getLogMap(logName, logLevel, message, data, className, null);
		return logData;	
	}

	private String getLogEvent(String logName, String logLevel, String message, Object data , String className, Exception e) {
		String logData = getLogMap(logName, logLevel, message, data, className, e);
		return logData;
	}
	
	private String getLogMap(String logName, String logLevel, String message, Object data, String className, Exception e) {
		String mid = "LP."+System.currentTimeMillis()+"."+UUID.randomUUID();
		TelemetryBEEvent te = new TelemetryBEEvent();
		long unixTime = System.currentTimeMillis();
		Map<String,Object> eks = new HashMap<String,Object>();
		eks.put("class", className);
		eks.put("level", logLevel);
		eks.put("message", message);
		eks.put("data", data);
		eks.put("stacktrace", e);
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
