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
				log(Level.INFO.name(), message, data, e);
				break;
			case "DEBUG":
				log(Level.DEBUG.name(), message, data, e);
				break;
			case "WARN":
				log(Level.WARN.name(), message, data, e);
				break;
			case "ERROR":
				log(Level.ERROR.name(), message, data, e);
				break;
			case "TRACE":
				log(Level.TRACE.name(), message, data, e);
				break;
			case "FATAL":
				log(Level.FATAL.name(), message, data, e);
				break;
			}
		}
	}

	private static void log(String logLevel, String message, Object data, Throwable e) {
		Map<String, String> context = getContext();
		if (e != null) {
			String code = "SYSTEM_ERROR";
			if (e instanceof MiddlewareException) {
				code = ((MiddlewareException) e).getErrCode();
			}
			telemetryHandler.error(context, code, "system", ExceptionUtils.getStackTrace(e));
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
