/**
 * 
 */
package org.ekstep.telemetry.handler;

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

	/**
	 * 
	 */
	public void send(String event, Level level) {
		rootLogger.info(event);
	}

}
