/**
 * 
 */
package org.ekstep.telemetry.handler;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.telemetry.TelemetryGenerator;

/**
 * This is the custom logger implementation to carry out platform Logging. This
 * class holds methods used to log events which are pushed to Kafka topic.
 * @author mahesh
 *
 */
public class TelemetryLoggingHandler implements TelemetryHandler {

	private static final Logger rootLogger = LogManager.getLogger("DefaultPlatformLogger");
	private static final Logger telemetryLogger = LogManager.getLogger("TelemetryEventLogger");

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.telemetry.handler.TelemetryHandler#access(java.util.Map,
	 * java.util.Map)
	 */
	@Override
	public void access(Map<String, String> context, Map<String, Object> params) {
		String event = TelemetryGenerator.access(context, params);
		if (StringUtils.isNotBlank(event))
			telemetryLogger.info(event);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.telemetry.handler.TelemetryHandler#log(java.util.Map,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.util.List)
	 */
	@Override
	public void log(Map<String, String> context, String type, String level, String message, String pageid,
			Map<String, Object> params) {
		String event = TelemetryGenerator.log(context, type, level, message, pageid, params);
		if (StringUtils.isNotBlank(event)) {
			if (StringUtils.equalsIgnoreCase(Level.INFO.name(), level) && rootLogger.isInfoEnabled()) {
				rootLogger.info(event);
			} else if (StringUtils.equalsIgnoreCase(Level.DEBUG.name(), level) && rootLogger.isDebugEnabled()) {
				rootLogger.debug(event);
			} else if (StringUtils.equalsIgnoreCase(Level.ERROR.name(), level) && rootLogger.isErrorEnabled()) {
				rootLogger.error(event);
			} else if (StringUtils.equalsIgnoreCase(Level.WARN.name(), level) && rootLogger.isWarnEnabled()) {
				rootLogger.warn(event);
			} else if (StringUtils.equalsIgnoreCase(Level.TRACE.name(), level) && rootLogger.isTraceEnabled()) {
				rootLogger.trace(event);
			} else if (StringUtils.equalsIgnoreCase(Level.FATAL.name(), level) && rootLogger.isFatalEnabled()) {
				rootLogger.fatal(event);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.telemetry.handler.TelemetryHandler#log(java.util.Map,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void log(Map<String, String> context, String type, String level, String message) {
		log(context, type, level, message, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.telemetry.handler.TelemetryHandler#error(java.util.Map,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public void error(Map<String, String> context, String code, String type, String stacktrace, String pageid,
			Object object) {
		String event = TelemetryGenerator.error(context, code, type, stacktrace);
		if (StringUtils.isNotBlank(event) && rootLogger.isErrorEnabled()) {
			rootLogger.error(event);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.telemetry.handler.TelemetryHandler#error(java.util.Map,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void error(Map<String, String> context, String code, String type, String stacktrace) {
		error(context, code, type, stacktrace, null, null);
	}

	@Override
	public void audit(Map<String, String> context, List<String> props, String state, String prevState) {
		String event = TelemetryGenerator.audit(context, props, state, prevState);
		telemetryLogger.info(event);
		
	}

}
