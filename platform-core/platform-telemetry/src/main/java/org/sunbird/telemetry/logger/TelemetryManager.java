package org.sunbird.telemetry.logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.ExecutionContext;
import org.sunbird.common.dto.HeaderParam;
import org.sunbird.common.exception.MiddlewareException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.telemetry.TelemetryGenerator;
import org.sunbird.telemetry.TelemetryParams;
import org.sunbird.telemetry.handler.Level;
import org.sunbird.telemetry.handler.TelemetryHandler;
import org.sunbird.telemetry.handler.TelemetryLoggingHandler;

/**
 * This class is used to generate and handle telemetry. 
 * 
 * @author Rashmi and Mahesh
 *
 */
public class TelemetryManager {

	private static TelemetryHandler telemetryHandler = new TelemetryLoggingHandler();

	private static final String DEFAULT_CHANNEL_ID = Platform.config.hasPath("channel.default") ? Platform.config.getString("channel.default") : "in.ekstep";

	/**
	 * To log api_access as a telemetry event.
	 * 
	 * @param context context of the event
	 * @param params params of the event
	 */

	public static void access(Map<String, String> context, Map<String, Object> params) {
		String event = TelemetryGenerator.access(context, params);
		telemetryHandler.send(event, Level.INFO, true);
	}

	/**
	 * To log only message as a telemetry event.
	 * 
	 * @param message message of the event
	 */
	public static void log(String message) {
		log(message, null, Level.DEBUG.name());
	}

	/**
	 * To log message with params as a telemetry event.
	 * 
	 * @param message message of the event
	 * @param params params of the event
	 */
	public static void log(String message, Map<String, Object> params) {
		log(message, params, Level.DEBUG.name());
	}
	
	/**
	 * To log only message as a telemetry event.
	 * 
	 * @param message message of the event
	 */
	public static void info(String message) {
		log(message, null, Level.INFO.name());
	}

	/**
	 * To log message with params as a telemetry event.
	 * 
	 * @param message message of the event
	 * @param params params of the event
	 */
	public static void info(String message, Map<String, Object> params) {
		log(message, params, Level.INFO.name());
	}
	
	/**
	 * 
	 * @param message message of the event
	 */
	
	public static void warn(String message) {
		log(message, null, Level.WARN.name());
	}
	
	/**
	 * 
	 * @param message message of the event
	 * @param params params of the event
	 */
	
	public static void warn(String message, Map<String, Object> params) {
		log(message, params, Level.WARN.name());
	}
	
	/**
	 * 
	 * @param message message of the event
	 */
	public static void error(String message) {
		log(message, null, Level.ERROR.name());
	}
	
	/**
	 * 
	 * @param message message of the event
	 * @param params params of the event
	 */
	public static void error(String message, Map<String, Object> params) {
		log(message, params, Level.ERROR.name());
	}

	/**
	 * To log exception with message and params as a telemetry event.
	 * 
	 * @param message message of the event
	 * @param e exception details in the event
	 */
	public static void error(String message, Throwable e) {
		error(message, e, null);
	}
	
	/**
	 * 
	 * @param message message of the event
	 * @param e exception details in the event
	 * @param object object of the event
	 */
	public static void error(String message, Throwable e, Object object) {
		Map<String, String> context = getContext();
		String stacktrace = ExceptionUtils.getStackTrace(e);
		String code = ResponseCode.SERVER_ERROR.name();
		if (e instanceof MiddlewareException) {
			code = ((MiddlewareException) e).getErrCode();
		}
		String event = TelemetryGenerator.error(context, code, "system", stacktrace);
		telemetryHandler.send(event, Level.ERROR);
	}
	
	
	public static void audit(String id, String type, List<String> props) {
		audit(id, type, props, null, null);
	}
	
	public static void audit(String id, String type, List<String> props, String state, String prevState) {
		Map<String, String> context = getContext();
		context.put("objectId", id);
		context.put("objectType", type);
		String event = TelemetryGenerator.audit(context, props, state, prevState);
		telemetryHandler.send(event, Level.INFO);
	}

	/**
	 * @param query query in the event.
	 * @param filters filters of the event
	 * @param sort sort for the search event
	 * @param size size for the search result.
	 * @param topN  size for the search result.
	 * @param type type of the event
	 */
	public static void search(String query, Object filters, Object sort, int size, Object topN,
			String type) {
		search(null, query, filters, sort, size, topN, type);
	}

	/**
	 * @param context context of the event
	 * @param query query for the search
	 * @param filters filters of the event
	 * @param sort sort for the search
	 * @param size size for the search result.
	 * @param topN  size for the search result.
	 * @param type type of the event
	 */
	public static void search(Map<String, Object> context, String query, Object filters, Object sort,
							  int size, Object topN, String type) {
		Map<String, String> reqContext=null;
		String deviceId=null;
		String appId=null;

		if(null!=context){
			reqContext=new HashMap<String,String>();
			reqContext.put(TelemetryParams.ACTOR.name(),(String) context.get(TelemetryParams.ACTOR.name()));
			reqContext.put(TelemetryParams.ENV.name(),(String) context.get(TelemetryParams.ENV.name()));
			reqContext.put(TelemetryParams.CHANNEL.name(),(String) context.get(HeaderParam.CHANNEL_ID.name()));
			reqContext.put(TelemetryParams.APP_ID.name(),(String) context.get(HeaderParam.APP_ID.name()));
			deviceId = (String) context.get(HeaderParam.DEVICE_ID.name());
			if(StringUtils.isNotBlank(deviceId))
				reqContext.put("did", deviceId);
			if (null != context.get("objectId") && null != context.get("objectType")) {
				reqContext.put("objectId", (String) context.get("objectId"));
				reqContext.put("objectType", (String) context.get("objectType"));
			}
		}else{
			reqContext = getContext();
			deviceId = (String) ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.DEVICE_ID.name());
			if(StringUtils.isNotBlank(deviceId))
				reqContext.put("did", deviceId);
			appId=(String) ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.APP_ID.name());
			if(StringUtils.isNotBlank(appId))
				reqContext.put(TelemetryParams.APP_ID.name(), appId);
		}

		String event = TelemetryGenerator.search(reqContext, query, filters, sort, null, size, topN, type);
		telemetryHandler.send(event, Level.INFO, true);
	}

	/**
	 * To log exception with message and params for user specified log level as a
	 * telemetry event.
	 * @param message message of the event
	 * @param params params of the event
	 * @param logLevel logLevel of the event
	 */
	private static void log(String message, Map<String, Object> params, String logLevel) {
		Map<String, String> context = getContext();
		String event = TelemetryGenerator.log(context, "system", logLevel, message, null, params);
		telemetryHandler.send(event, Level.getLevel(logLevel));
	}

	private static Map<String, String> getContext() {
		Map<String, String> context = new HashMap<String, String>();
		context.put(TelemetryParams.ACTOR.name(),
				getContextValue(TelemetryParams.ACTOR.name(), "org.sunbird.learning.platform"));
		context.put(TelemetryParams.CHANNEL.name(), getContextValue(HeaderParam.CHANNEL_ID.name(), DEFAULT_CHANNEL_ID));
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
