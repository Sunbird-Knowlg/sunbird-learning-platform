package org.ekstep.common.logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.ekstep.common.dto.TelemetryBEEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This is the custom logger implementation to carryout platform Logging. This
 * class holds methods used to log Events which are pushed to kafka
 * 
 * @author Rashmi
 *
 */
public class PlatformLogger {

	private static ObjectMapper mapper = new ObjectMapper();
	private static Logger rootLogger = (Logger) LogManager.getLogger("DefaultPlatformLogger");

	/**
	 * To log only message.
	 */
	public static void log(String message) {
		log(message, null, LoggerEnum.DEBUG.name());
	}
	
	/**
	 * To log message with some data.
	 */
	public static void log(String message, Object data) {
		log(message, data, LoggerEnum.DEBUG.name());
	}

	/**
	 * To log message, data in used defined log level.
	 */
	public static void log(String message,Object data, String logLevel) {
		backendLog(message,  data, null, logLevel);
	}

	/**
	 * To log exception with message and data.
	 */
	public static void log(String message, Object data, Throwable e) {
		backendLog(message,  data, e, LoggerEnum.ERROR.name());
	}

	/**
	 * To log exception with message and data for user specific log level.
	 */
	public static void log(String message,  Object data, Exception e, String logLevel) {
		backendLog(message,  data, e, logLevel);
	}

	private static void info(String message,  Object data) {
		if (rootLogger.isInfoEnabled())
			rootLogger.info(getBELogEvent(LoggerEnum.INFO.name(), message, data));
	}

	private static void debug(String message,  Object data) {
		if (rootLogger.isDebugEnabled())
			rootLogger.debug(getBELogEvent(LoggerEnum.DEBUG.name(), message, data));
	}

	private static void error(String message,  Object data, Throwable exception) {
		if (rootLogger.isErrorEnabled())
			rootLogger.error(getBELogEvent(LoggerEnum.ERROR.name(), message, data, exception));
	}

	private static void warn(String message, Object data, Throwable exception) {
		if (rootLogger.isWarnEnabled())
			rootLogger.warn(getBELogEvent(LoggerEnum.WARN.name(), message, data, exception));
	}

	private static void backendLog(String message,  Object data, Throwable e, String logLevel) {
		if (StringUtils.isNotBlank(logLevel)) {
			switch (logLevel) {
			case "INFO":
				info(message,  data);
				break;
			case "DEBUG":
				debug(message, data);
				break;
			case "WARN":
				warn(message,  data, e);
				break;
			case "ERROR":
				error(message, data, e);
				break;
			}
		}
	}

	private static String getBELogEvent(String logLevel, String message, Object data) {
		String logData = getBELog(logLevel, message, data, null);
		return logData;
	}

	private static String getBELogEvent(String logLevel, String message, Object data, Throwable e) {
		String logData = getBELog(logLevel, message, data, e);
		return logData;
	}

	public static String getBELog(String logLevel, String message, Object data, Throwable exception) {
		String mid = "LP." + System.currentTimeMillis() + "." + UUID.randomUUID();
		TelemetryBEEvent te = new TelemetryBEEvent();
		long unixTime = System.currentTimeMillis();
		Map<String, Object> eks = new HashMap<String, Object>();
		eks.put("level", logLevel);
		eks.put("message", message);
		if (data != null) {
			eks.put("data", data);
		}
		if (exception != null) {
			eks.put("stacktrace", ExceptionUtils.getStackTrace(exception));
		}
		te.setEid(LoggerEnum.BE_LOG.name());
		te.setEts(unixTime);
		te.setMid(mid);
		te.setVer("2.0");
		te.setPdata("org.ekstep.learning.platform", "", "1.0", "");
		String jsonMessage = null;
		try {
			te.setEdata(eks);
			jsonMessage = mapper.writeValueAsString(te);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonMessage;
	}
}
