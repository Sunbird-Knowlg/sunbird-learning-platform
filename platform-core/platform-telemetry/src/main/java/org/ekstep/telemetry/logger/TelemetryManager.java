package org.ekstep.telemetry.logger;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ekstep.common.dto.ExecutionContext;
import org.ekstep.common.dto.HeaderParam;
import org.ekstep.common.exception.MiddlewareException;
import org.ekstep.telemetry.TelemetryParams;
import org.ekstep.telemetry.handler.Level;
import org.ekstep.telemetry.handler.TelemetryHandler;
import org.ekstep.telemetry.handler.TelemetryLoggingHandler;

/**
 * This class is used to generate and handle telemetry. 
 * 
 * @author Rashmi & Mahesh
 *
 */
public class TelemetryManager {

	/**
	 * 
	 */
	private static TelemetryHandler telemetryHandler = new TelemetryLoggingHandler();

	/**
	 * To log api_access as a telemetry event.
	 * 
	 * @param context
	 * @param params
	 */

	public static void access(Map<String, String> context, Map<String, Object> params) {
		telemetryHandler.access(context, params);
	}

	/**
	 * To log only message as a telemetry event.
	 * 
	 * @param message
	 */
	public static void log(String message) {
		log(message, null, Level.DEBUG.name());
	}

	/**
	 * To log message with params as a telemetry event.
	 * 
	 * @param message
	 * @param data
	 */
	public static void log(String message, Object data) {
		log(message, data, Level.DEBUG.name());
	}

	/**
	 * To log message, params in user defined log level as a telemetry event.
	 * 
	 * @param message
	 * @param data
	 * @param logLevel
	 */
	public static void log(String message, Object data, String logLevel) {
		log(message, data, null, logLevel);
	}

	/**
	 * To log exception with message and params as a telemetry event.
	 * 
	 * @param message
	 * @param data
	 * @param e
	 */
	public static void log(String message, Object data, Throwable e) {
		log(message, data, e, Level.ERROR.name());
	}

	/**
	 * To log exception with message and params for user specified log level as a
	 * telemetry event.
	 * 
	 * @param message
	 * @param data
	 * @param e
	 * @param logLevel
	 */
	public static void log(String message, Object data, Throwable e, String logLevel) {
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

	private static void info(String message, Object data) {
		log(Level.INFO.name(), message, data, null);
	}

	private static void debug(String message, Object data) {
		log(Level.DEBUG.name(), message, data, null);
	}

	private static void error(String message, Object data, Throwable exception) {
		log(Level.ERROR.name(), message, data, exception);
	}

	private static void warn(String message, Object data, Throwable exception) {
		log(Level.WARN.name(), message, data, exception);
	}

	private static void log(String logLevel, String message, Object data, Throwable exception) {
		Map<String, String> context = getContext();
		if (exception != null) {
			String code = "SYSTEM_ERROR";
			if (exception instanceof MiddlewareException) {
				code = ((MiddlewareException) exception).getErrCode();
			}
			telemetryHandler.error(context, code, "system", ExceptionUtils.getStackTrace(exception));
		} else {
			// TODO: Object data should become params.
			telemetryHandler.log(context, "system", logLevel, message);
		}
	}

	private static Map<String, String> getContext() {
		Map<String, String> context = new HashMap<String, String>();
		context.put(TelemetryParams.ACTOR.name(),
				getContextValue(TelemetryParams.ACTOR.name(), "org.ekstep.learning.platform"));
		context.put(TelemetryParams.CHANNEL.name(), getContextValue(HeaderParam.CHANNEL_ID.name(), "in.ekstep"));
		context.put(TelemetryParams.ENV.name(), getContextValue(TelemetryParams.ENV.name(), "system"));
		return context;
	}
	
	private static String getContextValue(String key, String defaultValue) {
		String value = (String) ExecutionContext.getCurrent().getGlobalContext().get(key);
		if (StringUtils.isBlank(value))
			return defaultValue;
		else
			return value;
	}
}
