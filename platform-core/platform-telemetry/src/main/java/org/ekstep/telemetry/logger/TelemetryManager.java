package org.ekstep.telemetry.logger;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ekstep.common.dto.ExecutionContext;
import org.ekstep.common.dto.HeaderParam;
import org.ekstep.common.exception.MiddlewareException;
import org.ekstep.common.exception.ResponseCode;
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
	public static void log(String message, Map<String, Object> params) {
		log(message, params, Level.DEBUG.name());
	}
	
	/**
	 * To log only message as a telemetry event.
	 * 
	 * @param message
	 */
	public static void info(String message) {
		log(message, null, Level.INFO.name());
	}

	/**
	 * To log message with params as a telemetry event.
	 * 
	 * @param message
	 * @param data
	 */
	public static void info(String message, Map<String, Object> params) {
		log(message, params, Level.INFO.name());
	}
	
	/**
	 * 
	 * @param message
	 */
	
	public static void warn(String message) {
		log(message, null, Level.WARN.name());
	}
	
	/**
	 * 
	 * @param message
	 * @param params
	 */
	
	public static void warn(String message, Map<String, Object> params) {
		log(message, params, Level.WARN.name());
	}
	
	/**
	 * 
	 * @param message
	 */
	public static void error(String message) {
		log(message, null, Level.ERROR.name());
	}
	
	/**
	 * 
	 * @param message
	 * @param params
	 */
	public static void error(String message, Map<String, Object> params) {
		log(message, params, Level.ERROR.name());
	}

	/**
	 * To log exception with message and params as a telemetry event.
	 * 
	 * @param message
	 * @param e
	 */
	public static void error(String message, Throwable e) {
		error(message, e, null);
	}
	
	/**
	 * 
	 * @param message
	 * @param e
	 * @param object
	 */
	public static void error(String message, Throwable e, Object object) {
		Map<String, String> context = getContext();
		String stacktrace = ExceptionUtils.getStackTrace(e);
		String code = ResponseCode.SERVER_ERROR.name();
		if (e instanceof MiddlewareException) {
			code = ((MiddlewareException) e).getErrCode();
		}
		telemetryHandler.error(context, code, "system", stacktrace, null, object);
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
	private static void log(String message, Map<String, Object> params, String logLevel) {
		Map<String, String> context = getContext();
		telemetryHandler.log(context, "system", logLevel, message, null, params);
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
