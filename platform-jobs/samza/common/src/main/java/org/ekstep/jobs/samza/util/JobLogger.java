package org.ekstep.jobs.samza.util;

import java.text.MessageFormat;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobLogger {

	private final Logger logger;

	@SuppressWarnings("rawtypes")
	public JobLogger(Class clazz) {
		logger = LoggerFactory.getLogger(clazz);
	}

	public void debug(String msg, Map<String, Object> event) throws Exception {
		if (logger.isDebugEnabled())
			debug(msg, JSONUtils.serialize(event));
	}

	public void info(String msg, Map<String, Object> event) throws Exception {
		if (logger.isInfoEnabled())
			info(msg, JSONUtils.serialize(event));
	}

	public void error(String msg, Map<String, Object> event, Throwable t) throws Exception {
		if (logger.isErrorEnabled())
			error(msg, JSONUtils.serialize(event), t);
	}

	public void debug(String msg) {
		logger.debug(getLogMessage(msg, null));
	}

	public void debug(String msg, String event) {
		logger.debug(getLogMessage(msg, event));
	}

	public void info(String msg) {
		logger.info(getLogMessage(msg, null));
	}

	public void info(String msg, String event) {
		logger.info(getLogMessage(msg, event));
	}

	public void error(String msg, Throwable t) {
		logger.error(getLogMessage(msg, null), t);
	}

	public void error(String msg, String event, Throwable t) {
		logger.error(getLogMessage(msg, event), t);
	}

	private String getLogMessage(String msg, String event) {
		return event == null ? MessageFormat.format("Message: {0}", msg) : MessageFormat.format("Message: {0} | event:{1}", msg, event);
	}
}
