package org.ekstep.telemetry.logger;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.ekstep.common.dto.ExecutionContext;
import org.ekstep.common.dto.HeaderParam;
import org.ekstep.telemetry.TelemetryGenerator;
import org.ekstep.telemetry.TelemetryParams;

/**
 * This is the custom logger implementation to carryout platform Logging. This
 * class holds methods used to log Events which are pushed to kafka
 * 
 * @author Rashmi
 *
 */
public class PlatformLogger {

	private static Logger rootLogger = (Logger) LogManager.getLogger("DefaultPlatformLogger");

	/**
	 * To log only message.
	 */
	public static void log(String message) {
		log(message, null, Level.DEBUG.name());
	}
	
	/**
	 * To log message with some data.
	 */
	public static void log(String message, Object data) {
		log(message, data, Level.DEBUG.name());
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
		backendLog(message,  data, e, Level.ERROR.name());
	}

	/**
	 * To log exception with message and data for user specific log level.
	 */
	public static void log(String message,  Object data, Exception e, String logLevel) {
		backendLog(message,  data, e, logLevel);
	}

	private static void info(String message,  Object data) {
		if (rootLogger.isInfoEnabled())
			rootLogger.info(getBELogEvent(Level.INFO.name(), message, data));
	}

	private static void debug(String message,  Object data) {
		if (rootLogger.isDebugEnabled())
			rootLogger.debug(getBELogEvent(Level.DEBUG.name(), message, data));
	}

	private static void error(String message,  Object data, Throwable exception) {
		if (rootLogger.isErrorEnabled())
			rootLogger.error(getBELogEvent(Level.ERROR.name(), message, data, exception));
	}

	private static void warn(String message, Object data, Throwable exception) {
		if (rootLogger.isWarnEnabled())
			rootLogger.warn(getBELogEvent(Level.WARN.name(), message, data, exception));
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
		return getBELog(logLevel, message, data, null);
		
	}

	private static String getBELogEvent(String logLevel, String message, Object data, Throwable e) {
		return getBELog(logLevel, message, data, e);
	}

	public static String getBELog(String logLevel, String message, Object data, Throwable exception) {
		Map<String, String> context = new HashMap<String, String>();
		String cid = (String) ExecutionContext.getCurrent().getGlobalContext().get(TelemetryParams.ACTOR.name());
		context.put(TelemetryParams.ACTOR.name(), cid);
		context.put(TelemetryParams.CHANNEL.name(), (String) ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.CHANNEL_ID.name()));
		context.put("env", "content");
		if (exception != null) {
			String code = "SYSTEM_ERROR";
			// TODO: get code from exception.
			return TelemetryGenerator.error(context, code, "system", ExceptionUtils.getStackTrace(exception));
		} else {
			// TODO: Object data should become params. 
			return TelemetryGenerator.log(context, "system", logLevel, message);
		}
	}
}
