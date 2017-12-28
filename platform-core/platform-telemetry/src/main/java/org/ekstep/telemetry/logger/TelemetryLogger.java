package org.ekstep.telemetry.logger;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.telemetry.TelemetryGenerator;

public class TelemetryLogger {

	private static final Logger telemetryEventLogger = LogManager.getLogger("TelemetryEventLogger");

	public static void access(Map<String, Object> params, Map<String, String> context) {
		String event = TelemetryGenerator.access(params, context);
		if (StringUtils.isNotBlank(event))
			telemetryEventLogger.info(event);
	}
}
