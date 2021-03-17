/**
 * 
 */
package org.sunbird.telemetry.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is the custom logger implementation to carry out platform Logging. This
 * class holds methods used to log events which are pushed to Kafka topic.
 * 
 * @author mahesh
 *
 */
public class TelemetryLoggingHandler implements TelemetryHandler {

	private static final Logger rootLogger = LogManager.getLogger("DefaultPlatformLogger");
	private static final Logger telemetryLogger = LogManager.getLogger("TelemetryEventLogger");
	

	public void send(String event, Level level) {
		send(event, level, false);
	}
	
	/**
	 * 
	 */
	public void send(String event, Level level, boolean telemetry) {
		if (telemetry) {
			telemetryLogger.info(event);
		} else {
			switch(level) {
				case INFO:
					rootLogger.info(event);
					break;
				case DEBUG:
					rootLogger.debug(event);
					break;
				case ERROR:
					rootLogger.error(event);
					break;
				case WARN:
					rootLogger.warn(event);
					break;
				case TRACE:
					rootLogger.trace(event);
					break;
				case FATAL:
					rootLogger.fatal(event);
					break;				
			}
		}
		
	}

}
