package org.ekstep.telemetry.logger;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.dto.ExecutionContext;
import org.ekstep.common.dto.HeaderParam;
import org.ekstep.common.exception.MiddlewareException;
import org.ekstep.telemetry.TelemetryGenerator;
import org.ekstep.telemetry.TelemetryParams;

/**
 * This is the custom logger implementation to carry out platform Logging. This
 * class holds methods used to log events which are pushed to Kafka topic.
 * 
 * @author Rashmi & Mahesh
 *
 */
public class PlatformLogger {

	private static final Logger rootLogger = (Logger) LogManager.getLogger("DefaultPlatformLogger");
	private static final Logger telemetryLogger = LogManager.getLogger("TelemetryEventLogger");

	/**
	 * To log api_access as a telemetry event.
	 * @param context
	 * @param params
	 */
	
	public static void access(Map<String, String> context, Map<String, Object> params) {
		String event = TelemetryGenerator.access(context, params);
		if (StringUtils.isNotBlank(event))
			telemetryLogger.info(event);
	}

	/**
	 * To log only message as a telemetry event.
	 * @param message
	 */
	public static void log(String message) {
		log(message, null, Level.DEBUG.name());
	}
	
	/**
	 * To log message with params as a telemetry event.
	 * @param message
	 * @param data
	 */
	public static void log(String message, Object data) {
		log(message, data, Level.DEBUG.name());
	}

	/**
	 * To log message, params in user defined log level as a telemetry event.
	 * @param message
	 * @param data
	 * @param logLevel
	 */
	public static void log(String message,Object data, String logLevel) {
		backendLog(message,  data, null, logLevel);
	}

	/**
	 * To log exception with message and params as a telemetry event.
	 * @param message
	 * @param data
	 * @param e
	 */
	public static void log(String message, Object data, Throwable e) {
		backendLog(message,  data, e, Level.ERROR.name());
	}

	/**
	 * To log exception with message and params for user specified log level as a telemetry event.
	 * @param message
	 * @param data
	 * @param e
	 * @param logLevel
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
		context.put(TelemetryParams.ACTOR.name(), getContextValue(TelemetryParams.ACTOR.name(), "org.ekstep.learning.platform"));
		context.put(TelemetryParams.CHANNEL.name(), getContextValue(HeaderParam.CHANNEL_ID.name(), "in.ekstep"));
		context.put(TelemetryParams.ENV.name(), getContextValue(TelemetryParams.ENV.name(), "system"));
		if (exception != null) {
			String code = "SYSTEM_ERROR";
			if (exception instanceof MiddlewareException) {
				code = ((MiddlewareException) exception).getErrCode();
			}
			return TelemetryGenerator.error(context, code, "system", ExceptionUtils.getStackTrace(exception));
		} else {
			// TODO: Object data should become params. 
			return TelemetryGenerator.log(context, "system", logLevel, message);
		}
	}
	
	private static String getContextValue(String key, String defaultValue) {
		String value = (String) ExecutionContext.getCurrent().getGlobalContext().get(key);
		if (StringUtils.isBlank(value))
			return defaultValue;
		else 
			return value;
	}
}
