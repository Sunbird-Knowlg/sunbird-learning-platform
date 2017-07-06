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
/**
 * This is the custom logger implementation to carryout
 * platform Logging. This class holds methods used to log
 * Events which are pushed to kafka
 * 
 * @author Rashmi
 *
 */
public class PlatformLogger implements ILogger {

	private static ObjectMapper mapper = new ObjectMapper();
	private Logger logger = null;
	private String className;

	public PlatformLogger(String clsName) {
		className = clsName;
		logger = (Logger) LogManager.getLogger(clsName);
	}

	private void info(String message, Object data) {
		try {
			logger.info(mapper
					.writeValueAsString(getLogEvent(LoggerEnum.BE_LOG.name(), LoggerEnum.INFO.name(), message, data)));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private void debug(String message, Object data) {
		try {
			logger.debug(mapper
					.writeValueAsString(getLogEvent(LoggerEnum.BE_LOG.name(), LoggerEnum.DEBUG.name(), message, data)));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private void error(String message, Object data, Exception exception) {
		try {
			logger.error(mapper.writeValueAsString(
					getLogEvent(LoggerEnum.BE_LOG.name(), LoggerEnum.ERROR.name(), message, data, exception)));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private void warn(String message, Object data, Exception exception) {
		try {
			logger.warn(mapper.writeValueAsString(
					getLogEvent(LoggerEnum.BE_LOG.name(), LoggerEnum.WARN.name(), message, data, exception)));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	public void log(String message, Object data) {
		log(message, data, LoggerEnum.DEBUG.name());
	}

	public void log(String message) {
		log(message, null, LoggerEnum.DEBUG.name());
	}

	public void log(String message, Object data, String logLevel) {
		logData(message, data, null, logLevel);
	}

	public void log(String message, Object data, Exception e) {
		logData(message, data, e, LoggerEnum.ERROR.name());
	}

	public void log(String message, Object data, Exception e, String logLevel) {
		logData(message, data, e, logLevel);
	}

	private void logData(String message, Object data, Exception e, String logLevel) {
		if (StringUtils.isNotBlank(logLevel)) {
			switch (logLevel) {
			case "INFO":
				info(message, data);
				break;
			case "DEBUG":
				debug(message, data);
				break;
			case "WARN":
				warn(message, data, e);
				break;
			case "ERROR":
				error(message, data, e);
				break;
			}
		}
	}

	private String getLogEvent(String logName, String logLevel, String message, Object data) {
		String logData = getLogMap(logName, logLevel, message, data, null);
		return logData;
	}

	private String getLogEvent(String logName, String logLevel, String message, Object data, Exception e) {
		String logData = getLogMap(logName, logLevel, message, data, e);
		return logData;
	}

	private String getLogMap(String logName, String logLevel, String message, Object data, Exception exception) {
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
			logger.info(jsonMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonMessage;
	}
}
