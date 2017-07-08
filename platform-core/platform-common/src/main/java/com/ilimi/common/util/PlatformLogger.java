package com.ilimi.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.TelemetryBEEvent;

/**
 * This is the custom logger implementation to carryout platform Logging. This
 * class holds methods used to log Events which are pushed to kafka
 * 
 * @author Rashmi
 *
 */
public class PlatformLogger implements ILogger {

	private static ObjectMapper mapper = new ObjectMapper();
	private Logger logger = null;

	public void PlatformLoger(){
		logger = (Logger) LogManager.getLogger();
	}

	private void info(String message, String className, String method, Object data) {
		logger.info(getLogEvent(LoggerEnum.BE_LOG.name(), className, method, LoggerEnum.INFO.name(), message, data));
	}

	private void debug(String message, String className, String method, Object data) {
		logger.debug(getLogEvent(LoggerEnum.BE_LOG.name(),className, method,  LoggerEnum.DEBUG.name(), message, data));
	}

	private void error(String message, String className, String method, Object data, Exception exception) {
		logger.error(getLogEvent(LoggerEnum.BE_LOG.name(), className, method, LoggerEnum.ERROR.name(), message, data, exception));
	}

	private void warn(String message,String className, String method, Object data, Exception exception) {
		logger.warn(getLogEvent(LoggerEnum.BE_LOG.name(),className, method, LoggerEnum.WARN.name(), message, data, exception));
	}

	public void log(String message, String className, String method, Object data) {
		log(message,className, method, data, LoggerEnum.DEBUG.name());
	}

	public void log(String message, String className, String method) {
		log(message, className, method, null, LoggerEnum.DEBUG.name());
	}

	public void log(String message, String className, String method, Object data, String logLevel) {
		logData(message, className, method, data, null, logLevel);
	}

	public void log(String message,String className, String method, Object data, Exception e) {
		logData(message,className, method,  data, e, LoggerEnum.ERROR.name());
	}

	public void log(String message, String className, String method, Object data, Exception e, String logLevel) {
		logData(message, className, method, data, e, logLevel);
	}

	private void logData(String message, String className, String method, Object data, Exception e, String logLevel) {
		if (StringUtils.isNotBlank(logLevel)) {
			switch (logLevel) {
			case "INFO":
				info(message, className, method, data);
				break;
			case "DEBUG":
				debug(message, className, method, data);
				break;
			case "WARN":
				warn(message, className, method, data, e);
				break;
			case "ERROR":
				error(message, className, method, data, e);
				break;
			}
		}
	}

	private String getLogEvent(String logName, String className, String method, String logLevel, String message, Object data) {
		String logData = getLogMap(logName,className, method, logLevel, message, data, null);
		return logData;
	}

	private String getLogEvent(String logName, String className, String method, String logLevel, String message, Object data, Exception e) {
		String logData = getLogMap(logName, className, method, logLevel, message, data, e);
		return logData;
	}

	private String getLogMap(String logName, String className, String method, String logLevel, String message, Object data, Exception exception) {
		String mid = "LP." + System.currentTimeMillis() + "." + UUID.randomUUID();
		TelemetryBEEvent te = new TelemetryBEEvent();
		long unixTime = System.currentTimeMillis();
		Map<String, Object> eks = new HashMap<String, Object>();
		eks.put("class", className);
		eks.put("level", logLevel);
		eks.put("message", message);
		if (data != null) {
			eks.put("data", data);
		}
		if (exception != null) {
			eks.put("stacktrace", exception);
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
			// logger.info(jsonMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonMessage;
	}
}
